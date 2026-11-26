package com.example.data.util

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.LruCache
import android.util.Size
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.data.model.LocalDeviceVideo
import com.example.data.model.MAX_SHORT_VIDEO_DURATION_MS
import com.example.data.model.isEligibleShortVideo
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

object LocalMediaScanner {

    private const val THUMB_MAX_WIDTH = 1280
    private const val THUMB_MAX_HEIGHT = 720
    private const val THUMB_MIN_ACCEPTABLE_OS_LONG_SIDE = 960
    private const val DISK_CACHE_DIR = "video_thumbs_hd_720"
    private const val LEGACY_HD_DISK_CACHE_DIR = "video_thumbs_hd"
    private const val LEGACY_DISK_CACHE_DIR = "video_thumbs"
    private const val DISK_JPEG_QUALITY = 92

    private val maxCacheKb: Int =
        (Runtime.getRuntime().maxMemory() / 1024L / 4L)
            .toInt()
            .coerceIn(48 * 1024, 128 * 1024)

    private val thumbnailMemoryCache = object : LruCache<String, Bitmap>(maxCacheKb) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return (value.allocationByteCount / 1024).coerceAtLeast(1)
        }
    }

    private val diskWriteLock = Any()

    fun getMemoryCachedThumbnail(uriString: String?): Bitmap? {
        if (uriString.isNullOrBlank()) return null
        val cached = thumbnailMemoryCache.get(uriString)
        return if (cached != null && !cached.isRecycled) cached else null
    }

    private fun diskCacheFile(context: Context, key: String): File {
        val dir = File(context.cacheDir, DISK_CACHE_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())
        val hex = buildString(digest.size * 2) {
            for (b in digest) {
                append(String.format(Locale.US, "%02x", b))
            }
        }
        return File(dir, "$hex.jpg")
    }

    private fun readDiskCachedThumbnail(context: Context, key: String): Bitmap? {
        return try {
            val file = diskCacheFile(context, key)
            if (!file.exists() || file.length() <= 0L) return null
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(file.absolutePath, options)?.also { bmp ->
                if (!bmp.isRecycled) {
                    thumbnailMemoryCache.put(key, bmp)
                }
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun writeDiskCachedThumbnail(context: Context, key: String, bitmap: Bitmap) {
        if (bitmap.isRecycled) return
        try {
            val file = diskCacheFile(context, key)
            synchronized(diskWriteLock) {
                if (file.exists() && file.length() > 0L) return
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, DISK_JPEG_QUALITY, out)
                }
            }
        } catch (_: Throwable) {
        }
    }

    fun clearThumbnailCaches(context: Context): Long {
        val ramBytes = thumbnailMemoryCache.size().toLong() * 1024L
        thumbnailMemoryCache.evictAll()
        var diskBytes = 0L
        try {
            listOf(DISK_CACHE_DIR, LEGACY_HD_DISK_CACHE_DIR, LEGACY_DISK_CACHE_DIR).forEach { dirName ->
                val dir = File(context.cacheDir, dirName)
                if (dir.exists()) {
                    dir.listFiles()?.forEach { file ->
                        diskBytes += file.length()
                        file.delete()
                    }
                }
            }
        } catch (_: Throwable) {
        }
        val totalBytes = ramBytes + diskBytes
        return if (totalBytes > 0L) {
            (totalBytes / (1024L * 1024L)).coerceAtLeast(1L)
        } else {
            0L
        }
    }

    fun extractFastVideoThumbnail(context: Context, uri: Uri, durationMs: Long): Bitmap? =
        extractFastVideoThumbnail(context, uri, durationMs, onQuickPreview = null)

    fun extractFastVideoThumbnail(
        context: Context,
        uri: Uri,
        durationMs: Long,
        onQuickPreview: ((Bitmap) -> Unit)? = null
    ): Bitmap? {
        val key = uri.toString()

        // Tier 1: L1 RAM cache (< 0.1 ms)
        getMemoryCachedThumbnail(key)?.let { return it }

        // Tier 2: L2 Disk JPEG cache (~1-3 ms)
        readDiskCachedThumbnail(context, key)?.let { return it }

        var decoded: Bitmap? = null
        var osFallback: Bitmap? = null
        var emittedPreview = false

        // Tier 3: Android Q+ MediaStore OS-cached thumbnail (instant preview; accepted directly if 720p HD+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            uri.scheme == "content" &&
            uri.authority == MediaStore.AUTHORITY
        ) {
            val osThumb = try {
                context.contentResolver.loadThumbnail(
                    uri,
                    Size(THUMB_MAX_WIDTH, THUMB_MAX_HEIGHT),
                    null
                )
            } catch (_: Throwable) {
                null
            }
            if (osThumb != null && !osThumb.isRecycled) {
                if (maxOf(osThumb.width, osThumb.height) >= THUMB_MIN_ACCEPTABLE_OS_LONG_SIDE) {
                    decoded = osThumb
                } else {
                    osFallback = osThumb
                    if (onQuickPreview != null) {
                        emittedPreview = true
                        onQuickPreview(osThumb)
                    }
                }
            }
        }

        // Tier 4: True 720p HD hardware-scaled keyframe decoding via MediaMetadataRetriever
        if (decoded == null) {
            val retriever = MediaMetadataRetriever()
            decoded = try {
                retriever.setDataSource(context, uri)
                val effectiveDurationMs = if (durationMs > 0L) {
                    durationMs
                } else {
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull() ?: 0L
                }
                val timeUs = when {
                    effectiveDurationMs > 3000L -> (effectiveDurationMs * 350L) // 35% keyframe
                    effectiveDurationMs > 0L -> (effectiveDurationMs * 500L)
                    else -> 1_000_000L
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    retriever.getScaledFrameAtTime(
                        timeUs,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                        THUMB_MAX_WIDTH,
                        THUMB_MAX_WIDTH
                    ) ?: retriever.getScaledFrameAtTime(
                        0L,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                        THUMB_MAX_WIDTH,
                        THUMB_MAX_WIDTH
                    )
                } else {
                    val raw = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?: retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC)
                        ?: retriever.frameAtTime
                    downscaleBitmapIfNeeded(raw)
                }
            } catch (_: Throwable) {
                null
            } finally {
                try {
                    retriever.release()
                } catch (_: Throwable) {
                }
            }
        }

        val chosen = when {
            decoded != null && !decoded.isRecycled -> {
                if (osFallback != null && osFallback !== decoded && !osFallback.isRecycled) {
                    val fallbackMax = maxOf(osFallback.width, osFallback.height)
                    val decodedMax = maxOf(decoded.width, decoded.height)
                    if (fallbackMax > decodedMax) {
                        decoded.recycle()
                        osFallback
                    } else {
                        // Never recycle osFallback if Compose already received it as an instant preview
                        if (!emittedPreview) {
                            osFallback.recycle()
                        }
                        decoded
                    }
                } else {
                    decoded
                }
            }
            osFallback != null && !osFallback.isRecycled -> osFallback
            else -> null
        }

        if (chosen != null && !chosen.isRecycled) {
            val finalBitmap = downscaleBitmapIfNeeded(chosen) ?: chosen
            thumbnailMemoryCache.put(key, finalBitmap)
            writeDiskCachedThumbnail(context, key, finalBitmap)
            return finalBitmap
        }
        return null
    }

    private fun downscaleBitmapIfNeeded(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null || bitmap.isRecycled) return null
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return bitmap
        val maxW = if (w >= h) THUMB_MAX_WIDTH else THUMB_MAX_HEIGHT
        val maxH = if (w >= h) THUMB_MAX_HEIGHT else THUMB_MAX_WIDTH
        if (w <= maxW && h <= maxH) return bitmap
        val scale = minOf(
            maxW.toFloat() / w.toFloat(),
            maxH.toFloat() / h.toFloat()
        ).coerceAtMost(1f)
        val targetW = (w * scale).toInt().coerceAtLeast(1)
        val targetH = (h * scale).toInt().coerceAtLeast(1)
        return try {
            val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            if (scaled !== bitmap && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            scaled
        } catch (_: Throwable) {
            bitmap
        }
    }

    suspend fun extractThumbnailsParallel(
        context: Context,
        videos: List<LocalDeviceVideo>,
        concurrency: Int = 6,
        batchSize: Int = 2,
        onBatchReady: suspend (Map<String, Bitmap>) -> Unit
    ) = coroutineScope {
        val pending = videos.filter { it.bitmap == null && !it.uriString.isNullOrBlank() }
        if (pending.isEmpty()) return@coroutineScope

        val maxFlushSize = batchSize.coerceIn(1, 6)
        val semaphore = Semaphore(concurrency.coerceIn(2, 6))
        val resultsChannel = Channel<Pair<String, Bitmap>>(Channel.UNLIMITED)

        // Collector streams completed thumbnails immediately without waiting for slow neighbors.
        val collectorJob = launch(Dispatchers.Main) {
            val buffer = LinkedHashMap<String, Bitmap>(maxFlushSize)
            while (true) {
                val first = resultsChannel.receiveCatching().getOrNull() ?: break
                buffer[first.first] = first.second
                while (buffer.size < maxFlushSize) {
                    val next = resultsChannel.tryReceive().getOrNull() ?: break
                    buffer[next.first] = next.second
                }
                if (buffer.size < maxFlushSize) {
                    delay(18L)
                    while (buffer.size < maxFlushSize) {
                        val next = resultsChannel.tryReceive().getOrNull() ?: break
                        buffer[next.first] = next.second
                    }
                }
                onBatchReady(buffer.toMap())
                buffer.clear()
            }
        }

        // Fast-path pass: serve already cached L1/L2 thumbnails immediately in micro-batches
        val remaining = ArrayList<LocalDeviceVideo>(pending.size)
        for (vid in pending) {
            val uriStr = vid.uriString ?: continue
            val fastCached = getMemoryCachedThumbnail(uriStr) ?: withContext(Dispatchers.IO) {
                readDiskCachedThumbnail(context, uriStr)
            }
            if (fastCached != null && !fastCached.isRecycled) {
                resultsChannel.trySend(vid.id to fastCached)
            } else {
                remaining.add(vid)
            }
        }

        // Concurrent progressive HD pass: emit OS instant preview (~5ms) then upgrade in-place to 720p HD
        val workersJob = launch(Dispatchers.IO) {
            coroutineScope {
                for (vid in remaining) {
                    launch {
                        semaphore.withPermit {
                            val uriStr = vid.uriString ?: return@withPermit
                            val uri = try {
                                Uri.parse(uriStr)
                            } catch (_: Throwable) {
                                return@withPermit
                            }
                            val durMs = parseDurationTextToMs(vid.durationText)
                            val thumb = extractFastVideoThumbnail(
                                context = context,
                                uri = uri,
                                durationMs = durMs,
                                onQuickPreview = { preview ->
                                    if (!preview.isRecycled) {
                                        resultsChannel.trySend(vid.id to preview)
                                    }
                                }
                            )
                            if (thumb != null && !thumb.isRecycled) {
                                resultsChannel.trySend(vid.id to thumb)
                            }
                        }
                    }
                }
            }
        }

        workersJob.join()
        resultsChannel.close()
        collectorJob.join()
    }

    fun parseDurationTextToMs(durationText: String): Long {
        return try {
            val parts = durationText.split(":")
            when (parts.size) {
                2 -> (parts[0].toLong() * 60L + parts[1].toLong()) * 1000L
                3 -> (parts[0].toLong() * 3600L + parts[1].toLong() * 60L + parts[2].toLong()) * 1000L
                else -> 0L
            }
        } catch (_: Exception) {
            0L
        }
    }

    fun hasStorageOrAudioPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getAudioPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun hasStorageOrVideoPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val videoGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED

            val partialGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED
            } else false

            videoGranted || partialGranted
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun scanDeviceAudios(context: Context): List<com.example.data.model.LocalAudioTrack> {
        val tracks = mutableListOf<com.example.data.model.LocalAudioTrack>()
        val projectionList = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projectionList.add(MediaStore.Audio.Media.RELATIVE_PATH)
        }
        val projection = projectionList.toTypedArray()
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )
            cursor?.use { c ->
                val idCol = c.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationCol = c.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val albumIdCol = c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val dateCol = c.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    c.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                } else -1

                while (c.moveToNext()) {
                    val id = if (idCol >= 0 && !c.isNull(idCol)) c.getLong(idCol) else tracks.size.toLong()
                    val title = (if (titleCol >= 0 && !c.isNull(titleCol)) c.getString(titleCol) else null) ?: "Audio Track #$id"
                    val artist = (if (artistCol >= 0 && !c.isNull(artistCol)) c.getString(artistCol) else null) ?: "Unknown Artist"
                    val album = (if (albumCol >= 0 && !c.isNull(albumCol)) c.getString(albumCol) else null) ?: "Device Audio"
                    val durationMs = if (durationCol >= 0 && !c.isNull(durationCol)) c.getLong(durationCol) else 0L
                    val albumId = if (albumIdCol >= 0 && !c.isNull(albumIdCol)) c.getLong(albumIdCol) else -1L
                    val dateAddedSecs = if (dateCol >= 0 && !c.isNull(dateCol)) c.getLong(dateCol) else 0L
                    val relativePath = if (relPathCol >= 0 && !c.isNull(relPathCol)) c.getString(relPathCol) else null
                    val folderName = relativePath
                        ?.trim('/')
                        ?.substringAfterLast('/')
                        ?.ifBlank { "Device Music" }
                        ?: "Device Music"

                    val contentUri = try {
                        ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                    } catch (_: Throwable) {
                        null
                    }

                    val albumArtUri = if (albumId > 0) {
                        try {
                            ContentUris.withAppendedId(
                                Uri.parse("content://media/external/audio/albumart"),
                                albumId
                            ).toString()
                        } catch (_: Throwable) {
                            null
                        }
                    } else null

                    val durationSeconds = (durationMs / 1000).toInt().coerceAtLeast(1)

                    tracks.add(
                        com.example.data.model.LocalAudioTrack(
                            id = "device_audio_${id}",
                            title = title,
                            artist = if (artist.equals("<unknown>", ignoreCase = true)) "Local Device Music" else artist,
                            album = if (album.equals("<unknown>", ignoreCase = true)) "Storage" else album,
                            durationSeconds = durationSeconds,
                            albumCoverRes = com.example.R.drawable.img_hero_poster,
                            lyricsLines = listOf(
                                "Playing local audio track from phone storage.",
                                "Title: $title",
                                "Artist: $artist"
                            ),
                            audioUri = contentUri?.toString(),
                            albumArtUri = albumArtUri,
                            isFromDevice = true,
                            folderName = folderName,
                            dateAddedMs = dateAddedSecs * 1000L
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            // Guard against any security or cursor errors
        }
        return tracks
    }

    fun extractAudioCoverArt(
        context: Context,
        audioUriStr: String?,
        albumArtUriStr: String?
    ): Bitmap? {
        val key = "audio_art_${audioUriStr ?: albumArtUriStr ?: return null}"
        getMemoryCachedThumbnail(key)?.let { return it }

        var bmp: Bitmap? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !audioUriStr.isNullOrBlank()) {
            bmp = try {
                context.contentResolver.loadThumbnail(Uri.parse(audioUriStr), Size(512, 512), null)
            } catch (_: Throwable) {
                null
            }
        }
        if (bmp == null && !audioUriStr.isNullOrBlank()) {
            val retriever = MediaMetadataRetriever()
            bmp = try {
                retriever.setDataSource(context, Uri.parse(audioUriStr))
                val artBytes = retriever.embeddedPicture
                if (artBytes != null && artBytes.isNotEmpty()) {
                    BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
                } else null
            } catch (_: Throwable) {
                null
            } finally {
                try {
                    retriever.release()
                } catch (_: Throwable) {}
            }
        }
        if (bmp == null && !albumArtUriStr.isNullOrBlank()) {
            bmp = try {
                context.contentResolver.openInputStream(Uri.parse(albumArtUriStr))?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (_: Throwable) {
                null
            }
        }
        if (bmp != null && !bmp.isRecycled) {
            thumbnailMemoryCache.put(key, bmp)
            return bmp
        }
        return null
    }

    fun parseUriToAudioTrack(context: Context, uri: Uri): com.example.data.model.LocalAudioTrack {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {}

        var displayName: String? = null
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    displayName = cursor.getString(nameIndex)
                }
            }
        } catch (_: Exception) {}

        if (displayName.isNullOrBlank()) {
            displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "Audio_${System.currentTimeMillis()}.mp3"
        }

        var durationMs = 0L
        var artist = "Local Artist"
        var album = "Phone Storage"
        var title = displayName ?: "Audio Track"

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let {
                if (it.isNotBlank()) artist = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.let {
                if (it.isNotBlank()) album = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.let {
                if (it.isNotBlank()) title = it
            }
        } catch (_: Exception) {
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        val durationSeconds = (durationMs / 1000).toInt().coerceAtLeast(1)

        return com.example.data.model.LocalAudioTrack(
            id = "picked_audio_${System.currentTimeMillis()}_${(1000..9999).random()}",
            title = title,
            artist = artist,
            album = album,
            durationSeconds = durationSeconds,
            albumCoverRes = com.example.R.drawable.img_hero_poster,
            lyricsLines = listOf(
                "Playing selected device audio file.",
                "File: $displayName"
            ),
            audioUri = uri.toString(),
            albumArtUri = null,
            isFromDevice = true,
            folderName = "Imported Files",
            dateAddedMs = System.currentTimeMillis()
        )
    }

    private fun computeIsPortrait(width: Int, height: Int, rotationDegrees: Int): Boolean {
        if (width <= 0 || height <= 0) return false
        val normalized = ((rotationDegrees % 360) + 360) % 360
        val effectiveW = if (normalized == 90 || normalized == 270) height else width
        val effectiveH = if (normalized == 90 || normalized == 270) width else height
        return effectiveH > effectiveW
    }

    fun scanDeviceVideos(context: Context): List<LocalDeviceVideo> {
        val videos = mutableListOf<LocalDeviceVideo>()
        val projectionList = mutableListOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projectionList.add(MediaStore.Video.Media.RELATIVE_PATH)
            projectionList.add(MediaStore.Video.Media.ORIENTATION)
        }
        val projection = projectionList.toTypedArray()
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )
            cursor?.use { c ->
                val idCol = c.getColumnIndex(MediaStore.Video.Media._ID)
                val nameCol = c.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val durationCol = c.getColumnIndex(MediaStore.Video.Media.DURATION)
                val sizeCol = c.getColumnIndex(MediaStore.Video.Media.SIZE)
                val widthCol = c.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = c.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val dateCol = c.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                val bucketCol = c.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    c.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                } else -1
                val orientationCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    c.getColumnIndex(MediaStore.Video.Media.ORIENTATION)
                } else -1

                while (c.moveToNext()) {
                    val id = if (idCol >= 0 && !c.isNull(idCol)) c.getLong(idCol) else videos.size.toLong()
                    val name = (if (nameCol >= 0 && !c.isNull(nameCol)) c.getString(nameCol) else null) ?: "Video_$id.mp4"
                    val durationMs = if (durationCol >= 0 && !c.isNull(durationCol)) c.getLong(durationCol) else 0L
                    val sizeBytes = if (sizeCol >= 0 && !c.isNull(sizeCol)) c.getLong(sizeCol) else 0L
                    val width = if (widthCol >= 0 && !c.isNull(widthCol)) c.getInt(widthCol) else 0
                    val height = if (heightCol >= 0 && !c.isNull(heightCol)) c.getInt(heightCol) else 0
                    val dateAddedSecs = if (dateCol >= 0 && !c.isNull(dateCol)) c.getLong(dateCol) else 0L
                    val bucketName = if (bucketCol >= 0 && !c.isNull(bucketCol)) c.getString(bucketCol) else null
                    val relativePath = if (relPathCol >= 0 && !c.isNull(relPathCol)) c.getString(relPathCol) else null
                    val orientationDeg = if (orientationCol >= 0 && !c.isNull(orientationCol)) c.getInt(orientationCol) else 0

                    val contentUri = try {
                        ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                    } catch (_: Throwable) {
                        null
                    }

                    val dateAddedMs = if (dateAddedSecs > 0L) dateAddedSecs * 1000L else 0L
                    val isPortrait = computeIsPortrait(width, height, orientationDeg)

                    videos.add(
                        LocalDeviceVideo(
                            id = "device_vid_${id}_${videos.size}",
                            title = name,
                            durationText = formatDuration(durationMs),
                            sizeText = formatSize(sizeBytes),
                            resolutionText = formatResolution(width, height),
                            dateAddedText = formatDate(dateAddedMs),
                            dateAddedTimestampMs = dateAddedMs,
                            uriString = contentUri?.toString(),
                            thumbnailRes = 0,
                            bitmap = null,
                            isFromGallery = true,
                            folderName = bucketName?.ifBlank { "Internal Storage" } ?: "Internal Storage",
                            relativePath = relativePath,
                            durationMs = durationMs,
                            width = width,
                            height = height,
                            rotationDegrees = orientationDeg,
                            isPortrait = isPortrait
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            // Guard against any security or cursor errors
        }
        return videos
    }

    /**
     * Scans device videos and strictly keeps ONLY vertically-filmed (portrait) videos
     * whose duration is greater than 0 and does not exceed 30 minutes (1,800,000 ms).
     */
    fun scanShortVerticalVideos(context: Context): List<LocalDeviceVideo> {
        val allVideos = scanDeviceVideos(context)
        val result = ArrayList<LocalDeviceVideo>(allVideos.size)
        for (video in allVideos) {
            // Quick pre-filter: if MediaStore already guarantees duration > 30 min, skip immediately
            if (video.durationMs > MAX_SHORT_VIDEO_DURATION_MS) continue

            val needsRetrieverCheck = video.width <= 0 ||
                video.height <= 0 ||
                video.durationMs <= 0L ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

            val resolved = if (needsRetrieverCheck && !video.uriString.isNullOrBlank()) {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, Uri.parse(video.uriString))
                    val rDur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: video.durationMs
                    val rW = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: video.width
                    val rH = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: video.height
                    val rRot = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: video.rotationDegrees
                    val portrait = computeIsPortrait(rW, rH, rRot)
                    video.copy(
                        durationMs = rDur,
                        durationText = if (rDur > 0L) formatDuration(rDur) else video.durationText,
                        width = rW,
                        height = rH,
                        rotationDegrees = rRot,
                        resolutionText = if (rW > 0 && rH > 0) formatResolution(rW, rH) else video.resolutionText,
                        isPortrait = portrait
                    )
                } catch (_: Throwable) {
                    video
                } finally {
                    runCatching { retriever.release() }
                }
            } else {
                video
            }

            if (resolved.isEligibleShortVideo()) {
                result.add(resolved)
            }
        }
        return result
    }

    fun parseUriToLocalVideo(context: Context, uri: Uri): LocalDeviceVideo {
        // Persist read permission if granted through SAF
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
            // Some pickers do not support persistable URIs
        }

        var displayName: String? = null
        var sizeBytes: Long = 0L
        var resolvedTimestampMs: Long = 0L
        var resolvedBucketName: String? = null
        var resolvedRelativePath: String? = null

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                val dateAddedIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
                val lastModifiedIndex = cursor.getColumnIndex("last_modified")
                val bucketIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val relPathIndex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                } else -1
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) displayName = cursor.getString(nameIndex)
                    if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
                    if (bucketIndex != -1 && !cursor.isNull(bucketIndex)) {
                        resolvedBucketName = cursor.getString(bucketIndex)
                    }
                    if (relPathIndex != -1 && !cursor.isNull(relPathIndex)) {
                        resolvedRelativePath = cursor.getString(relPathIndex)
                    }
                    val dateAddedSecs = if (dateAddedIndex != -1 && !cursor.isNull(dateAddedIndex)) {
                        cursor.getLong(dateAddedIndex)
                    } else 0L
                    val lastModMs = if (lastModifiedIndex != -1 && !cursor.isNull(lastModifiedIndex)) {
                        cursor.getLong(lastModifiedIndex)
                    } else 0L
                    resolvedTimestampMs = when {
                        dateAddedSecs > 0L -> if (dateAddedSecs < 10_000_000_000L) dateAddedSecs * 1000L else dateAddedSecs
                        lastModMs > 0L -> lastModMs
                        else -> 0L
                    }
                }
            }
        } catch (_: Exception) {}
        if (resolvedTimestampMs <= 0L) {
            resolvedTimestampMs = System.currentTimeMillis()
        }

        if (displayName.isNullOrBlank()) {
            displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "Video_${System.currentTimeMillis()}.mp4"
        }

        var durationMs = 0L
        var width = 0
        var height = 0
        var rotationDegrees = 0
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            rotationDegrees = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
        } catch (_: Exception) {
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        val folderName = when {
            !resolvedBucketName.isNullOrBlank() -> resolvedBucketName!!
            !resolvedRelativePath.isNullOrBlank() -> resolvedRelativePath!!
                .trim('/')
                .substringAfterLast('/')
                .ifBlank { "Phone Files (ملفات الهاتف)" }
            uri.scheme == "file" -> {
                uri.path?.let { path ->
                    val segments = path.split("/").filter { it.isNotBlank() }
                    if (segments.size >= 2) segments[segments.size - 2] else "Phone Files"
                } ?: "Phone Files"
            }
            else -> "Phone Files (ملفات الهاتف)"
        }

        val isPortrait = computeIsPortrait(width, height, rotationDegrees)

        return LocalDeviceVideo(
            id = "picked_${System.currentTimeMillis()}_${(1000..9999).random()}",
            title = displayName ?: "Video.mp4",
            durationText = if (durationMs > 0) formatDuration(durationMs) else "00:00",
            sizeText = if (sizeBytes > 0) formatSize(sizeBytes) else "Local File",
            resolutionText = formatResolution(width, height),
            dateAddedText = formatDate(resolvedTimestampMs),
            dateAddedTimestampMs = resolvedTimestampMs,
            uriString = uri.toString(),
            thumbnailRes = 0,
            bitmap = null,
            isFromGallery = true,
            folderName = folderName,
            relativePath = resolvedRelativePath,
            durationMs = durationMs,
            width = width,
            height = height,
            rotationDegrees = rotationDegrees,
            isPortrait = isPortrait
        )
    }

    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "00:00"
        val totalSeconds = durationMs / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    fun formatResolution(width: Int, height: Int): String {
        val maxDim = maxOf(width, height)
        val minDim = minOf(width, height)
        return when {
            maxDim >= 3800 || minDim >= 2100 -> "4K UHD"
            maxDim >= 1900 || minDim >= 1000 -> "1080p FHD"
            maxDim >= 1200 || minDim >= 700 -> "720p HD"
            maxDim > 0 -> "${width}x${height}"
            else -> "HD Video"
        }
    }

    fun formatDate(timestampMs: Long): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(Date(timestampMs))
        } catch (_: Exception) {
            "Device Storage"
        }
    }

    sealed class PendingMediaOp {
        data class Rename(val video: LocalDeviceVideo, val newDisplayName: String) : PendingMediaOp()
        data class Delete(val video: LocalDeviceVideo) : PendingMediaOp()
        data class Move(
            val video: LocalDeviceVideo,
            val targetFolderName: String,
            val targetRelativePath: String?
        ) : PendingMediaOp()
        data class RenameFolder(
            val oldFolderName: String,
            val videos: List<LocalDeviceVideo>,
            val newFolderName: String
        ) : PendingMediaOp()
        data class DeleteFolder(
            val folderName: String,
            val videos: List<LocalDeviceVideo>
        ) : PendingMediaOp()
    }

    sealed class MediaFileOpResult {
        data class Success(
            val updatedTitle: String? = null,
            val updatedFolder: String? = null,
            val updatedRelativePath: String? = null,
            val newUriString: String? = null
        ) : MediaFileOpResult()

        data class RequiresUserIntent(
            val intentSender: IntentSender,
            val pendingOp: PendingMediaOp,
            val alreadyHandledBySystemDialog: Boolean = false
        ) : MediaFileOpResult()

        data class Error(
            val messageEn: String,
            val messageAr: String
        ) : MediaFileOpResult()
    }

    data class VideoDetailsInfo(
        val fileName: String,
        val folderName: String,
        val storagePath: String,
        val resolution: String,
        val exactDimensions: String?,
        val duration: String,
        val size: String,
        val mimeType: String,
        val dateAdded: String,
        val bitrateText: String?
    )

    data class FolderDetailsInfo(
        val folderName: String,
        val videoCount: Int,
        val totalSizeText: String,
        val storagePath: String
    )

    private fun isMediaStoreUri(uri: Uri): Boolean {
        return uri.scheme == "content" && uri.authority == MediaStore.AUTHORITY
    }

    fun renameVideo(
        context: Context,
        video: LocalDeviceVideo,
        newDisplayName: String,
        allowSystemPrompt: Boolean = true
    ): MediaFileOpResult {
        val rawUri = video.uriString ?: return MediaFileOpResult.Error(
            "Video file URI is unavailable.",
            "مسار الفيديو غير متوفر."
        )
        val uri = Uri.parse(rawUri)
        val cleanName = newDisplayName.trim()
        if (cleanName.isBlank()) {
            return MediaFileOpResult.Error("Name cannot be empty.", "لا يمكن أن يكون الاسم فارغاً.")
        }

        if (uri.scheme == "file") {
            return try {
                val src = File(uri.path ?: "")
                if (!src.exists()) {
                    MediaFileOpResult.Error("File not found.", "لم يتم العثور على الملف.")
                } else {
                    val dst = File(src.parentFile, cleanName)
                    if (src.renameTo(dst)) {
                        MediaFileOpResult.Success(
                            updatedTitle = dst.name,
                            newUriString = Uri.fromFile(dst).toString()
                        )
                    } else {
                        MediaFileOpResult.Error("Unable to rename file.", "تعذر تعديل اسم الملف.")
                    }
                }
            } catch (_: Throwable) {
                MediaFileOpResult.Error("Failed to rename file.", "فشل تعديل اسم الملف.")
            }
        }

        if (isMediaStoreUri(uri)) {
            return try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, cleanName)
                }
                val rows = context.contentResolver.update(uri, values, null, null)
                if (rows > 0) {
                    MediaFileOpResult.Success(updatedTitle = cleanName)
                } else if (allowSystemPrompt && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val pi = MediaStore.createWriteRequest(context.contentResolver, listOf(uri))
                    MediaFileOpResult.RequiresUserIntent(
                        intentSender = pi.intentSender,
                        pendingOp = PendingMediaOp.Rename(video, cleanName)
                    )
                } else {
                    MediaFileOpResult.Error("Could not update video name.", "تعذر تحديث اسم الفيديو.")
                }
            } catch (sec: SecurityException) {
                if (allowSystemPrompt) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val pi = MediaStore.createWriteRequest(context.contentResolver, listOf(uri))
                        return MediaFileOpResult.RequiresUserIntent(
                            intentSender = pi.intentSender,
                            pendingOp = PendingMediaOp.Rename(video, cleanName)
                        )
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && sec is RecoverableSecurityException) {
                        return MediaFileOpResult.RequiresUserIntent(
                            intentSender = sec.userAction.actionIntent.intentSender,
                            pendingOp = PendingMediaOp.Rename(video, cleanName)
                        )
                    }
                }
                MediaFileOpResult.Error("Permission denied to rename this file.", "تم رفض إذن تعديل اسم هذا الملف.")
            } catch (_: Throwable) {
                MediaFileOpResult.Error("Unable to rename this video.", "تعذر إعادة تسمية هذا الفيديو.")
            }
        }

        if (DocumentsContract.isDocumentUri(context, uri)) {
            return try {
                val renamedUri = DocumentsContract.renameDocument(context.contentResolver, uri, cleanName)
                if (renamedUri != null) {
                    MediaFileOpResult.Success(
                        updatedTitle = cleanName,
                        newUriString = renamedUri.toString()
                    )
                } else {
                    MediaFileOpResult.Error("Provider does not allow renaming.", "مزود الملفات لا يسمح بإعادة التسمية.")
                }
            } catch (_: Throwable) {
                MediaFileOpResult.Error("Unable to rename this document.", "تعذر تعديل اسم هذا الملف.")
            }
        }

        return MediaFileOpResult.Error("Unsupported storage provider.", "مصدر التخزين غير مدعوم.")
    }

    fun deleteVideo(
        context: Context,
        video: LocalDeviceVideo,
        allowSystemPrompt: Boolean = true
    ): MediaFileOpResult {
        val rawUri = video.uriString ?: return MediaFileOpResult.Error(
            "Video file URI is unavailable.",
            "مسار الفيديو غير متوفر."
        )
        val uri = Uri.parse(rawUri)

        if (uri.scheme == "file") {
            return try {
                val file = File(uri.path ?: "")
                if (!file.exists() || file.delete()) {
                    MediaFileOpResult.Success()
                } else {
                    MediaFileOpResult.Error("Could not delete file.", "تعذر حذف الملف.")
                }
            } catch (_: Throwable) {
                MediaFileOpResult.Error("Failed to delete file.", "فشل حذف الملف.")
            }
        }

        if (isMediaStoreUri(uri)) {
            if (allowSystemPrompt && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return try {
                    val pi = MediaStore.createDeleteRequest(context.contentResolver, listOf(uri))
                    MediaFileOpResult.RequiresUserIntent(
                        intentSender = pi.intentSender,
                        pendingOp = PendingMediaOp.Delete(video),
                        alreadyHandledBySystemDialog = true
                    )
                } catch (_: Throwable) {
                    MediaFileOpResult.Error("Unable to request file deletion.", "تعذر طلب حذف الفيديو.")
                }
            }
            return try {
                val deleted = context.contentResolver.delete(uri, null, null)
                if (deleted > 0) {
                    MediaFileOpResult.Success()
                } else {
                    MediaFileOpResult.Error("Video was not deleted.", "لم يتم حذف الفيديو.")
                }
            } catch (sec: SecurityException) {
                if (allowSystemPrompt && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && sec is RecoverableSecurityException) {
                    return MediaFileOpResult.RequiresUserIntent(
                        intentSender = sec.userAction.actionIntent.intentSender,
                        pendingOp = PendingMediaOp.Delete(video)
                    )
                }
                MediaFileOpResult.Error("Permission denied to delete this file.", "تم رفض إذن حذف هذا الملف.")
            } catch (_: Throwable) {
                MediaFileOpResult.Error("Failed to delete video.", "فشل حذف الفيديو.")
            }
        }

        if (DocumentsContract.isDocumentUri(context, uri)) {
            return try {
                if (DocumentsContract.deleteDocument(context.contentResolver, uri)) {
                    MediaFileOpResult.Success()
                } else {
                    MediaFileOpResult.Error("Document could not be deleted.", "تعذر حذف الملف.")
                }
            } catch (_: Throwable) {
                MediaFileOpResult.Error("Provider does not allow deletion.", "مزود الملفات لا يدعم الحذف.")
            }
        }

        return MediaFileOpResult.Error("Unsupported storage provider.", "مصدر التخزين غير مدعوم.")
    }

    fun moveVideoToFolder(
        context: Context,
        video: LocalDeviceVideo,
        targetFolderName: String,
        targetRelativePath: String? = null,
        allowSystemPrompt: Boolean = true
    ): MediaFileOpResult {
        val rawUri = video.uriString ?: return MediaFileOpResult.Error(
            "Video file URI is unavailable.",
            "مسار الفيديو غير متوفر."
        )
        val uri = Uri.parse(rawUri)
        val cleanFolder = targetFolderName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "")
        if (cleanFolder.isBlank()) {
            return MediaFileOpResult.Error("Folder name cannot be empty.", "اسم المجلد غير صالح.")
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return MediaFileOpResult.Error(
                "Moving folders via MediaStore requires Android 10 or newer.",
                "يتطلب نقل الفيديوهات نظام أندرويد 10 أو أحدث."
            )
        }

        if (!isMediaStoreUri(uri)) {
            return MediaFileOpResult.Error(
                "Moving is supported for device library videos only.",
                "النقل مدعوم لفيديوهات مكتبة الهاتف فقط."
            )
        }

        val resolvedRelPath = run {
            val candidate = targetRelativePath?.trim()
            val formatted = if (!candidate.isNullOrBlank()) {
                if (candidate.endsWith("/")) candidate else "$candidate/"
            } else {
                "${Environment.DIRECTORY_MOVIES}/$cleanFolder/"
            }
            formatted
        }

        return try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.RELATIVE_PATH, resolvedRelPath)
            }
            val rows = context.contentResolver.update(uri, values, null, null)
            if (rows > 0) {
                MediaFileOpResult.Success(
                    updatedFolder = cleanFolder,
                    updatedRelativePath = resolvedRelPath
                )
            } else if (allowSystemPrompt && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val pi = MediaStore.createWriteRequest(context.contentResolver, listOf(uri))
                MediaFileOpResult.RequiresUserIntent(
                    intentSender = pi.intentSender,
                    pendingOp = PendingMediaOp.Move(video, cleanFolder, resolvedRelPath)
                )
            } else {
                MediaFileOpResult.Error("Could not move video to folder.", "تعذر نقل الفيديو إلى المجلد.")
            }
        } catch (sec: SecurityException) {
            if (allowSystemPrompt) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val pi = MediaStore.createWriteRequest(context.contentResolver, listOf(uri))
                    return MediaFileOpResult.RequiresUserIntent(
                        intentSender = pi.intentSender,
                        pendingOp = PendingMediaOp.Move(video, cleanFolder, resolvedRelPath)
                    )
                } else if (sec is RecoverableSecurityException) {
                    return MediaFileOpResult.RequiresUserIntent(
                        intentSender = sec.userAction.actionIntent.intentSender,
                        pendingOp = PendingMediaOp.Move(video, cleanFolder, resolvedRelPath)
                    )
                }
            }
            MediaFileOpResult.Error("Permission denied to move this video.", "تم رفض إذن نقل هذا الفيديو.")
        } catch (_: Throwable) {
            MediaFileOpResult.Error(
                "Target folder is restricted by Android scoped storage.",
                "المجلد المستهدف مقيد بصلاحيات التخزين، جرب مجلداً داخل Movies."
            )
        }
    }

    fun resolveShareableUri(context: Context, rawUriString: String?): Uri? {
        if (rawUriString.isNullOrBlank()) return null
        return try {
            val rawParsed = Uri.parse(rawUriString)
            if (rawParsed.scheme == "file") {
                val rawPath = rawParsed.path ?: return null
                val file = File(rawPath)
                if (!file.exists()) return null
                val canonical = file.canonicalPath
                val dataRoot = runCatching { context.dataDir.canonicalPath }.getOrNull()
                val allowedSharedFiles = runCatching { File(context.filesDir, "shared").canonicalPath }.getOrNull()
                val allowedSharedCache = runCatching { File(context.cacheDir, "shared").canonicalPath }.getOrNull()
                val isInsidePrivateData = dataRoot != null && (canonical == dataRoot || canonical.startsWith("$dataRoot/"))
                val isInsideAllowedShared =
                    (allowedSharedFiles != null && canonical.startsWith("$allowedSharedFiles/")) ||
                        (allowedSharedCache != null && canonical.startsWith("$allowedSharedCache/"))
                if (isInsidePrivateData && !isInsideAllowedShared) {
                    return null
                }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else if (rawParsed.scheme == "content") {
                rawParsed
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun shareVideo(context: Context, video: LocalDeviceVideo): Boolean {
        val shareUri = resolveShareableUri(context, video.uriString) ?: return false
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = context.contentResolver.getType(shareUri) ?: "video/*"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                putExtra(Intent.EXTRA_SUBJECT, video.title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, video.title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun resolveVideoDetails(context: Context, video: LocalDeviceVideo): VideoDetailsInfo {
        var mimeType = "video/mp4"
        var width = 0
        var height = 0
        var bitrate: Long = 0L
        var relPath = video.relativePath

        val uri = video.uriString?.let { runCatching { Uri.parse(it) }.getOrNull() }
        if (uri != null) {
            try {
                mimeType = context.contentResolver.getType(uri) ?: mimeType
                if (isMediaStoreUri(uri)) {
                    val cols = mutableListOf(
                        MediaStore.Video.Media.WIDTH,
                        MediaStore.Video.Media.HEIGHT,
                        MediaStore.Video.Media.MIME_TYPE
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        cols.add(MediaStore.Video.Media.RELATIVE_PATH)
                    }
                    context.contentResolver.query(uri, cols.toTypedArray(), null, null, null)?.use { c ->
                        if (c.moveToFirst()) {
                            val wCol = c.getColumnIndex(MediaStore.Video.Media.WIDTH)
                            val hCol = c.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                            val mCol = c.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                            if (wCol >= 0) width = c.getInt(wCol)
                            if (hCol >= 0) height = c.getInt(hCol)
                            if (mCol >= 0 && !c.isNull(mCol)) mimeType = c.getString(mCol)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val rCol = c.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                                if (rCol >= 0 && !c.isNull(rCol)) relPath = c.getString(rCol)
                            }
                        }
                    }
                }
            } catch (_: Throwable) {
            }

            if (width <= 0 || height <= 0) {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: width
                    height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: height
                    bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L
                } catch (_: Throwable) {
                } finally {
                    runCatching { retriever.release() }
                }
            }
        }

        val storagePathText = when {
            !relPath.isNullOrBlank() -> "${relPath.trimEnd('/')}/${video.title}"
            uri?.scheme == "file" -> uri.path ?: video.folderName
            else -> "${video.folderName}/${video.title}"
        }
        val exactDims = if (width > 0 && height > 0) "${width} × ${height}" else null
        val bitrateStr = if (bitrate > 0L) {
            val mbps = bitrate / 1_000_000.0
            if (mbps >= 1.0) String.format(Locale.US, "%.1f Mbps", mbps)
            else "${bitrate / 1000L} kbps"
        } else null

        return VideoDetailsInfo(
            fileName = video.title,
            folderName = video.folderName,
            storagePath = storagePathText,
            resolution = video.resolutionText,
            exactDimensions = exactDims,
            duration = video.durationText,
            size = video.sizeText,
            mimeType = mimeType,
            dateAdded = video.dateAddedText,
            bitrateText = bitrateStr
        )
    }

    private fun resolveRenamedRelativePath(currentRelPath: String?, cleanFolder: String): String {
        val trimmed = currentRelPath?.trim()?.trim('/')
        if (!trimmed.isNullOrBlank()) {
            val segments = trimmed.split('/').filter { it.isNotBlank() }
            if (segments.size >= 2) {
                return (segments.dropLast(1) + cleanFolder).joinToString("/") + "/"
            }
        }
        return "${Environment.DIRECTORY_MOVIES}/$cleanFolder/"
    }

    fun renameFolder(
        context: Context,
        oldFolderName: String,
        videos: List<LocalDeviceVideo>,
        newFolderName: String,
        allowSystemPrompt: Boolean = true
    ): MediaFileOpResult {
        val cleanFolder = newFolderName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "")
        if (cleanFolder.isBlank()) {
            return MediaFileOpResult.Error("Folder name cannot be empty.", "اسم المجلد غير صالح.")
        }
        if (cleanFolder.equals(oldFolderName.trim(), ignoreCase = false)) {
            return MediaFileOpResult.Success(updatedFolder = cleanFolder)
        }
        if (videos.isEmpty()) {
            return MediaFileOpResult.Error("Folder has no videos to rename.", "لا يحتوي المجلد على فيديوهات.")
        }

        val mediaStoreUris = mutableListOf<Uri>()
        var updatedCount = 0
        var lastRelPath: String? = null
        var needsSystemPermission = false

        for (video in videos) {
            val rawUri = video.uriString ?: continue
            val uri = runCatching { Uri.parse(rawUri) }.getOrNull() ?: continue
            if (isMediaStoreUri(uri)) {
                mediaStoreUris.add(uri)
            }
        }

        for (video in videos) {
            val rawUri = video.uriString ?: continue
            val uri = runCatching { Uri.parse(rawUri) }.getOrNull() ?: continue
            if (uri.scheme == "file") {
                try {
                    val srcFile = File(uri.path ?: "")
                    val parentDir = srcFile.parentFile
                    if (parentDir != null && parentDir.exists()) {
                        val dstDir = File(parentDir.parentFile, cleanFolder)
                        if (parentDir.renameTo(dstDir)) {
                            updatedCount++
                            lastRelPath = dstDir.absolutePath
                        } else if (dstDir.exists() || dstDir.mkdirs()) {
                            val dstFile = File(dstDir, srcFile.name)
                            if (srcFile.renameTo(dstFile)) {
                                updatedCount++
                                lastRelPath = dstDir.absolutePath
                            }
                        }
                    }
                } catch (_: Throwable) {
                }
            } else if (isMediaStoreUri(uri)) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    return MediaFileOpResult.Error(
                        "Renaming MediaStore folders requires Android 10 or newer.",
                        "يتطلب تعديل اسم المجلد أندرويد 10 أو أحدث."
                    )
                }
                val newRelPath = resolveRenamedRelativePath(video.relativePath, cleanFolder)
                lastRelPath = newRelPath
                try {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, newRelPath)
                    }
                    val rows = context.contentResolver.update(uri, values, null, null)
                    if (rows > 0) {
                        updatedCount++
                    } else {
                        needsSystemPermission = true
                    }
                } catch (_: SecurityException) {
                    needsSystemPermission = true
                } catch (_: Throwable) {
                }
            }
        }

        if (needsSystemPermission && allowSystemPrompt && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && mediaStoreUris.isNotEmpty()) {
            return try {
                val pi = MediaStore.createWriteRequest(context.contentResolver, mediaStoreUris)
                MediaFileOpResult.RequiresUserIntent(
                    intentSender = pi.intentSender,
                    pendingOp = PendingMediaOp.RenameFolder(oldFolderName, videos, cleanFolder)
                )
            } catch (_: Throwable) {
                MediaFileOpResult.Error(
                    "Permission denied to rename this folder.",
                    "تم رفض إذن تعديل اسم هذا المجلد."
                )
            }
        }

        return if (updatedCount > 0) {
            MediaFileOpResult.Success(
                updatedFolder = cleanFolder,
                updatedRelativePath = lastRelPath
            )
        } else {
            MediaFileOpResult.Error(
                "Unable to rename this folder due to Android storage restrictions.",
                "تعذر تعديل اسم هذا المجلد بسبب قيود تخزين أندرويد."
            )
        }
    }

    fun deleteFolderVideos(
        context: Context,
        folderName: String,
        videos: List<LocalDeviceVideo>,
        allowSystemPrompt: Boolean = true
    ): MediaFileOpResult {
        if (videos.isEmpty()) {
            return MediaFileOpResult.Error("Folder is already empty.", "المجلد فارغ بالفعل.")
        }

        val mediaStoreUris = videos.mapNotNull { vid ->
            vid.uriString?.let { runCatching { Uri.parse(it) }.getOrNull() }?.takeIf { isMediaStoreUri(it) }
        }

        if (allowSystemPrompt && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && mediaStoreUris.isNotEmpty()) {
            return try {
                val pi = MediaStore.createDeleteRequest(context.contentResolver, mediaStoreUris)
                MediaFileOpResult.RequiresUserIntent(
                    intentSender = pi.intentSender,
                    pendingOp = PendingMediaOp.DeleteFolder(folderName, videos),
                    alreadyHandledBySystemDialog = true
                )
            } catch (_: Throwable) {
                MediaFileOpResult.Error("Unable to request folder deletion.", "تعذر طلب حذف المجلد.")
            }
        }

        var deletedCount = 0
        for (video in videos) {
            val rawUri = video.uriString ?: continue
            val uri = runCatching { Uri.parse(rawUri) }.getOrNull() ?: continue
            try {
                when {
                    uri.scheme == "file" -> {
                        val file = File(uri.path ?: "")
                        if (!file.exists() || file.delete()) deletedCount++
                    }
                    isMediaStoreUri(uri) -> {
                        if (context.contentResolver.delete(uri, null, null) > 0) deletedCount++
                    }
                    DocumentsContract.isDocumentUri(context, uri) -> {
                        if (DocumentsContract.deleteDocument(context.contentResolver, uri)) deletedCount++
                    }
                }
            } catch (_: Throwable) {
            }
        }

        return if (deletedCount > 0) {
            MediaFileOpResult.Success()
        } else {
            MediaFileOpResult.Error("Could not delete folder videos.", "تعذر حذف محتويات المجلد.")
        }
    }

    fun shareFolderVideos(context: Context, folderName: String, videos: List<LocalDeviceVideo>): Boolean {
        if (videos.isEmpty()) return false
        if (videos.size == 1) return shareVideo(context, videos.first())
        return try {
            val uris = ArrayList<Uri>()
            for (video in videos) {
                val rawUri = video.uriString ?: continue
                val parsed = Uri.parse(rawUri)
                val shareUri = if (parsed.scheme == "file") {
                    val file = File(parsed.path ?: continue)
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                } else {
                    parsed
                }
                uris.add(shareUri)
            }
            if (uris.isEmpty()) return false
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "video/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                putExtra(Intent.EXTRA_SUBJECT, folderName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, folderName).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun resolveFolderDetails(folderName: String, videos: List<LocalDeviceVideo>): FolderDetailsInfo {
        var totalBytes = 0L
        for (video in videos) {
            val clean = video.sizeText.trim().uppercase()
            val num = clean.takeWhile { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
            totalBytes += when {
                clean.contains("GB") -> (num * 1024.0 * 1024.0 * 1024.0).toLong()
                clean.contains("MB") -> (num * 1024.0 * 1024.0).toLong()
                clean.contains("KB") -> (num * 1024.0).toLong()
                else -> num.toLong()
            }
        }
        val formattedSize = when {
            totalBytes >= 1024L * 1024L * 1024L ->
                String.format(Locale.US, "%.2f GB", totalBytes / (1024.0 * 1024.0 * 1024.0))
            totalBytes >= 1024L * 1024L ->
                String.format(Locale.US, "%.1f MB", totalBytes / (1024.0 * 1024.0))
            totalBytes > 0L -> "${totalBytes / 1024L} KB"
            else -> "--"
        }
        val firstRel = videos.firstNotNullOfOrNull { it.relativePath }
        val storagePath = when {
            !firstRel.isNullOrBlank() -> firstRel.trimEnd('/')
            else -> folderName
        }
        return FolderDetailsInfo(
            folderName = folderName,
            videoCount = videos.size,
            totalSizeText = formattedSize,
            storagePath = storagePath
        )
    }
}
