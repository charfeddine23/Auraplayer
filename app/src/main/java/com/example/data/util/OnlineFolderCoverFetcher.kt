package com.example.data.util

import android.content.Context
import android.graphics.BitmapFactory
import com.example.data.local.FolderCoverCacheDao
import com.example.data.local.FolderCoverCacheEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class FolderCoverCandidate(
    val url: String,
    val provider: String
)

/**
 * Fetches online HD artwork for media folders using public, key-free HTTPS metadata endpoints
 * while enforcing strict resolution, aspect-ratio, and magic-byte validation BEFORE caching
 * locally on disk + Room DB.
 */
object OnlineFolderCoverFetcher {

    const val MIN_POSTER_WIDTH_PX = 600
    const val MIN_POSTER_HEIGHT_PX = 800
    const val MIN_SQUARE_SIZE_PX = 600

    const val MIN_POSTER_ASPECT_RATIO = 0.50f
    const val MAX_POSTER_ASPECT_RATIO = 0.88f
    const val MIN_SQUARE_ASPECT_RATIO = 0.85f
    const val MAX_SQUARE_ASPECT_RATIO = 1.18f

    private const val COVERS_DIR_NAME = "hd_folder_covers"
    private const val MAX_JSON_BYTES = 512 * 1024
    private const val MAX_IMAGE_BYTES = 8 * 1024 * 1024
    private const val NO_MATCH_RETRY_TTL_MS = 24L * 60L * 60L * 1000L

    private val networkSemaphore = Semaphore(permits = 3)
    private val inFlightMutexes = ConcurrentHashMap<String, Mutex>()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val genericSystemFolders = setOf(
        "camera",
        "dcim",
        "download",
        "downloads",
        "movie",
        "movies",
        "video",
        "videos",
        "music",
        "audio",
        "recordings",
        "voice recorder",
        "soundrecorder",
        "screenshots",
        "screenrecordings",
        "screen recordings",
        "screenrecorder",
        "whatsapp",
        "whatsapp video",
        "whatsapp audio",
        "whatsapp images",
        "telegram",
        "telegram video",
        "telegram audio",
        "instagram",
        "tiktok",
        "snapchat",
        "facebook",
        "pictures",
        "internal storage",
        "storage",
        "bluetooth",
        "alarms",
        "notifications",
        "ringtones",
        "podcasts",
        "0",
        "root"
    )

    fun buildFolderCacheKey(folderName: String, isMusicFolder: Boolean): String {
        val prefix = if (isMusicFolder) "music" else "video"
        return "$prefix::${folderName.trim().lowercase(Locale.ROOT)}"
    }

    /**
     * Cleans noisy release/quality tags from folder names and skips generic OS directories
     * like Camera/Downloads so personal clips never fetch unrelated online artwork.
     */
    fun normalizeFolderQuery(rawFolderName: String): String? {
        val trimmed = rawFolderName.trim()
        if (trimmed.isEmpty()) return null
        val lowerTrimmed = trimmed.lowercase(Locale.ROOT)
        if (lowerTrimmed in genericSystemFolders) return null

        var cleaned = trimmed
            .replace(Regex("\\[[^\\]]*]"), " ")
            .replace(Regex("\\([^)]*\\)"), " ")
            .replace(Regex("[._]+"), " ")
            .replace(
                Regex(
                    "\\b(19\\d{2}|20\\d{2}|2160p|1080p|720p|480p|4k|uhd|hdr10?|web-?dl|webrip|blu-?ray|bdrip|dvdrip|remux|hevc|x264|x265|h264|h265|aac|dts|ac3|dub|sub|multi|ost|flac|mp3|320kbps)\\b",
                    RegexOption.IGNORE_CASE
                ),
                " "
            )
            .replace(Regex("\\bS\\d{1,2}(E\\d{1,3})?\\b", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("\\bSeason\\s*\\d+\\b", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        // Strip trailing hyphen or punctuation leftovers
        cleaned = cleaned.trim('-', '_', ':', '|', ' ')
        if (cleaned.length < 2) return null
        if (cleaned.lowercase(Locale.ROOT) in genericSystemFolders) return null
        return cleaned
    }

    /**
     * Upgrades thumbnail/low-res artwork URLs to their highest resolution variants across
     * TMDB, AniList, Jikan/MAL, TVMaze, and iTunes/Apple Music endpoints, enforcing HTTPS.
     */
    fun upgradeArtworkUrlToHd(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return ""
        val httpsUrl = if (trimmed.startsWith("http://", ignoreCase = true)) {
            "https://" + trimmed.substring(7)
        } else {
            trimmed
        }
        if (!httpsUrl.startsWith("https://", ignoreCase = true)) return ""

        var upgraded = httpsUrl

        // 1. TMDB URLs -> request w780 minimum (keep original if already original)
        if (upgraded.contains("image.tmdb.org/t/p/", ignoreCase = true)) {
            upgraded = upgraded.replace(
                Regex("/t/p/(?!original/)w\\d+/"),
                "/t/p/w780/"
            )
        }

        // 2. iTunes / Apple Music mzstatic artwork -> 1000x1000bb high-res artwork
        if (upgraded.contains("mzstatic.com/", ignoreCase = true)) {
            upgraded = upgraded.replace(
                Regex("/\\d{2,4}x\\d{2,4}[a-zA-Z-]*(\\.[a-zA-Z0-9]+)$"),
                "/1000x1000bb$1"
            )
        }

        // 3. TVMaze -> upgrade medium_portrait to original_portrait
        if (upgraded.contains("tvmaze.com/", ignoreCase = true)) {
            upgraded = upgraded.replace("/medium_portrait/", "/original_portrait/")
                .replace("/medium_landscape/", "/original_landscape/")
        }

        // 4. MyAnimeList / Jikan -> strip /r/WxH/ thumbnail resizers for full image
        if (upgraded.contains("myanimelist.net/", ignoreCase = true)) {
            upgraded = upgraded.replace(Regex("/r/\\d+x\\d+/"), "/")
        }

        // 5. AniList -> upgrade /medium/ or /large/ paths when present
        if (upgraded.contains("anilist.co/", ignoreCase = true)) {
            upgraded = upgraded.replace("/cover/medium/", "/cover/large/")
                .replace("/cover/small/", "/cover/large/")
        }

        return upgraded
    }

    /**
     * Strict Minimum Resolution & Aspect-Ratio Gatekeeper:
     * - Posters / Video folders: >= 600px width, >= 800px height, portrait ratio in 0.50..0.88
     * - Square Music album art: >= 600x600px, square ratio in 0.85..1.18
     */
    fun isCoverMetadataValid(width: Int, height: Int, isMusicFolder: Boolean): Boolean {
        if (width <= 0 || height <= 0) return false
        val aspectRatio = width.toFloat() / height.toFloat()
        return if (isMusicFolder) {
            width >= MIN_SQUARE_SIZE_PX &&
                height >= MIN_SQUARE_SIZE_PX &&
                aspectRatio in MIN_SQUARE_ASPECT_RATIO..MAX_SQUARE_ASPECT_RATIO
        } else {
            width >= MIN_POSTER_WIDTH_PX &&
                height >= MIN_POSTER_HEIGHT_PX &&
                aspectRatio in MIN_POSTER_ASPECT_RATIO..MAX_POSTER_ASPECT_RATIO
        }
    }

    fun hasValidImageHeader(bytes: ByteArray): Boolean {
        if (bytes.size < 12) return false
        val isJpeg = bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()
        val isPng = bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
        val isWebp = bytes[0] == 'R'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() &&
            bytes[11] == 'P'.code.toByte()
        return isJpeg || isPng || isWebp
    }

    suspend fun resolveAndCacheFolderCover(
        context: Context,
        dao: FolderCoverCacheDao,
        folderName: String,
        isMusicFolder: Boolean,
        forceRefresh: Boolean = false
    ): FolderCoverCacheEntity? = withContext(Dispatchers.IO) {
        val cleanQuery = normalizeFolderQuery(folderName) ?: return@withContext null
        val folderKey = buildFolderCacheKey(folderName, isMusicFolder)
        val mutex = inFlightMutexes.getOrPut(folderKey) { Mutex() }

        mutex.withLock {
            val now = System.currentTimeMillis()
            if (!forceRefresh) {
                val existing = dao.getByKey(folderKey)
                if (existing != null) {
                    if (existing.status == FolderCoverCacheEntity.STATUS_CACHED &&
                        !existing.localFilePath.isNullOrBlank() &&
                        File(existing.localFilePath).exists() &&
                        isCoverMetadataValid(existing.width, existing.height, isMusicFolder)
                    ) {
                        return@withLock existing
                    }
                    if (existing.status == FolderCoverCacheEntity.STATUS_NO_HD_MATCH &&
                        (now - existing.fetchedAtMs) < NO_MATCH_RETRY_TTL_MS
                    ) {
                        return@withLock null
                    }
                }
            }

            networkSemaphore.withPermit {
                val candidates = if (isMusicFolder) {
                    fetchMusicFolderCandidates(cleanQuery)
                } else {
                    fetchVideoFolderCandidates(cleanQuery)
                }

                if (candidates.isEmpty()) {
                    // Offline or empty search result: record short-term backoff only if we got nothing
                    dao.upsert(
                        FolderCoverCacheEntity(
                            folderKey = folderKey,
                            displayName = folderName,
                            isMusicFolder = isMusicFolder,
                            status = FolderCoverCacheEntity.STATUS_NO_HD_MATCH,
                            fetchedAtMs = now
                        )
                    )
                    return@withLock null
                }

                val seenUrls = HashSet<String>(candidates.size)
                for (candidate in candidates) {
                    val hdUrl = upgradeArtworkUrlToHd(candidate.url)
                    if (hdUrl.isEmpty() || !seenUrls.add(hdUrl)) continue

                    val imageBytes = downloadCappedBytes(hdUrl, MAX_IMAGE_BYTES) ?: continue
                    if (!hasValidImageHeader(imageBytes)) continue

                    val boundsOpts = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, boundsOpts)
                    val w = boundsOpts.outWidth
                    val h = boundsOpts.outHeight

                    // Strict Minimum Resolution & Aspect-Ratio Gatekeeper
                    if (!isCoverMetadataValid(w, h, isMusicFolder)) {
                        continue
                    }

                    val coverFile = persistCoverAtomically(context, folderKey, imageBytes) ?: continue
                    val cachedEntry = FolderCoverCacheEntity(
                        folderKey = folderKey,
                        displayName = folderName,
                        sourceUrl = hdUrl,
                        localFilePath = coverFile.absolutePath,
                        sourceProvider = candidate.provider,
                        width = w,
                        height = h,
                        isMusicFolder = isMusicFolder,
                        status = FolderCoverCacheEntity.STATUS_CACHED,
                        fetchedAtMs = System.currentTimeMillis()
                    )
                    dao.upsert(cachedEntry)
                    return@withLock cachedEntry
                }

                // Every candidate failed quality validation or network download
                dao.upsert(
                    FolderCoverCacheEntity(
                        folderKey = folderKey,
                        displayName = folderName,
                        isMusicFolder = isMusicFolder,
                        status = FolderCoverCacheEntity.STATUS_NO_HD_MATCH,
                        fetchedAtMs = System.currentTimeMillis()
                    )
                )
                null
            }
        }
    }

    suspend fun clearAllCachedCovers(context: Context, dao: FolderCoverCacheDao) = withContext(Dispatchers.IO) {
        val dir = File(context.applicationContext.filesDir, COVERS_DIR_NAME)
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.forEach { file ->
                runCatching { file.delete() }
            }
        }
        dao.clearAll()
    }

    private fun fetchVideoFolderCandidates(query: String): List<FolderCoverCandidate> {
        val results = mutableListOf<FolderCoverCandidate>()
        // 1. TVMaze original high-res show/series posters (typically 680x1000+ or 1000x1426)
        results += queryTvMazeShows(query)
        // 2. iTunes movie posters upgraded to 1000x1000bb (preserves vertical movie poster aspect ratio e.g. 667x1000)
        results += queryItunesArtwork(query, entity = "movie", provider = "iTunes Movies")
        // 3. AniList GraphQL extraLarge covers (high-res anime posters)
        results += queryAniListAnime(query)
        // 4. Jikan (MyAnimeList) large anime covers
        results += queryJikanAnime(query)
        return results
    }

    private fun fetchMusicFolderCandidates(query: String): List<FolderCoverCandidate> {
        // iTunes album search upgraded to 1000x1000bb square HD artwork
        return queryItunesArtwork(query, entity = "album", provider = "iTunes Music")
    }

    private fun queryTvMazeShows(query: String): List<FolderCoverCandidate> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val body = downloadText("https://api.tvmaze.com/search/shows?q=$encoded") ?: return emptyList()
            val arr = JSONArray(body)
            buildList {
                val limit = minOf(arr.length(), 4)
                for (i in 0 until limit) {
                    val show = arr.optJSONObject(i)?.optJSONObject("show") ?: continue
                    val original = show.optJSONObject("image")?.optString("original").orEmpty()
                    if (original.isNotBlank()) {
                        add(FolderCoverCandidate(original, "TVMaze"))
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun queryAniListAnime(query: String): List<FolderCoverCandidate> {
        return try {
            val graphqlQuery = """
                query (${'$'}search: String) {
                  Page(page: 1, perPage: 4) {
                    media(search: ${'$'}search, type: ANIME) {
                      coverImage {
                        extraLarge
                      }
                    }
                  }
                }
            """.trimIndent()
            val payload = JSONObject().apply {
                put("query", graphqlQuery)
                put("variables", JSONObject().apply { put("search", query) })
            }
            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .header("Accept", "application/json")
                .build()

            val responseText = httpClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val bytes = readStreamCapped(resp.body?.byteStream(), MAX_JSON_BYTES) ?: return emptyList()
                bytes.toString(Charsets.UTF_8)
            }

            val mediaArr = JSONObject(responseText)
                .optJSONObject("data")
                ?.optJSONObject("Page")
                ?.optJSONArray("media")
                ?: return emptyList()

            buildList {
                for (i in 0 until mediaArr.length()) {
                    val extraLarge = mediaArr.optJSONObject(i)
                        ?.optJSONObject("coverImage")
                        ?.optString("extraLarge")
                        .orEmpty()
                    if (extraLarge.isNotBlank()) {
                        add(FolderCoverCandidate(extraLarge, "AniList"))
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun queryJikanAnime(query: String): List<FolderCoverCandidate> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val body = downloadText("https://api.jikan.moe/v4/anime?q=$encoded&limit=4&sfw=true") ?: return emptyList()
            val data = JSONObject(body).optJSONArray("data") ?: return emptyList()
            buildList {
                for (i in 0 until data.length()) {
                    val images = data.optJSONObject(i)?.optJSONObject("images") ?: continue
                    val webpLarge = images.optJSONObject("webp")?.optString("large_image_url").orEmpty()
                    val jpgLarge = images.optJSONObject("jpg")?.optString("large_image_url").orEmpty()
                    if (webpLarge.isNotBlank()) add(FolderCoverCandidate(webpLarge, "Jikan"))
                    if (jpgLarge.isNotBlank()) add(FolderCoverCandidate(jpgLarge, "Jikan"))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun queryItunesArtwork(query: String, entity: String, provider: String): List<FolderCoverCandidate> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val body = downloadText("https://itunes.apple.com/search?term=$encoded&entity=$entity&limit=5")
                ?: return emptyList()
            val results = JSONObject(body).optJSONArray("results") ?: return emptyList()
            buildList {
                for (i in 0 until results.length()) {
                    val art = results.optJSONObject(i)?.optString("artworkUrl100").orEmpty()
                    if (art.isNotBlank()) {
                        add(FolderCoverCandidate(upgradeArtworkUrlToHd(art), provider))
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun downloadText(url: String): String? {
        val bytes = downloadCappedBytes(url, MAX_JSON_BYTES) ?: return null
        return bytes.toString(Charsets.UTF_8)
    }

    private fun downloadCappedBytes(url: String, maxBytes: Int): ByteArray? {
        if (!url.startsWith("https://", ignoreCase = true)) return null
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "VideoPulsePlayer/1.0 (Android)")
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1L
                if (contentLength > maxBytes) return null
                readStreamCapped(response.body?.byteStream(), maxBytes)
            }
        } catch (_: IOException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun readStreamCapped(stream: java.io.InputStream?, maxBytes: Int): ByteArray? {
        if (stream == null) return null
        val buffer = ByteArray(8192)
        val output = java.io.ByteArrayOutputStream()
        var total = 0
        while (true) {
            val read = stream.read(buffer)
            if (read == -1) break
            total += read
            if (total > maxBytes) return null
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    private fun persistCoverAtomically(context: Context, folderKey: String, bytes: ByteArray): File? {
        return try {
            val dir = File(context.applicationContext.filesDir, COVERS_DIR_NAME)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val fileHash = sha256Hex(folderKey)
            val targetFile = File(dir, "$fileHash.img")
            val tempFile = File(dir, "$fileHash.tmp")
            tempFile.writeBytes(bytes)
            if (targetFile.exists()) {
                targetFile.delete()
            }
            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
            targetFile
        } catch (_: Exception) {
            null
        }
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
