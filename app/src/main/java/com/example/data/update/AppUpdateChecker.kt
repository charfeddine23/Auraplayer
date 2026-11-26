package com.example.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
    val versionName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val releaseSizeBytes: Long = -1L,
    val releasePageUrl: String,
    val publishedAt: String? = null
)

sealed class UpdateCheckResult {
    data class UpdateAvailable(val updateInfo: AppUpdateInfo) : UpdateCheckResult()
    object UpToDate : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

object AppUpdateChecker {
    const val REPO_URL = "https://github.com/charfeddine23/Aura"
    private const val RELEASES_API_URL = "https://api.github.com/repos/charfeddine23/Aura/releases/latest"
    private const val RELEASES_LIST_API_URL = "https://api.github.com/repos/charfeddine23/Aura/releases?per_page=5"

    private val apiClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    suspend fun checkForUpdates(
        currentVersion: String = BuildConfig.VERSION_NAME
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val latestRequest = Request.Builder()
                .url(RELEASES_API_URL)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "AuraPulse-Android/${BuildConfig.VERSION_NAME}")
                .build()

            apiClient.newCall(latestRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    if (body.isNotBlank()) {
                        val json = JSONObject(body)
                        parseReleaseObject(json, currentVersion)?.let { return@withContext it }
                    }
                }
            }

            // Fallback: inspect recent releases list in case latest release is tagged differently
            val listRequest = Request.Builder()
                .url(RELEASES_LIST_API_URL)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "AuraPulse-Android/${BuildConfig.VERSION_NAME}")
                .build()

            apiClient.newCall(listRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val array = JSONArray(body)
                    for (i in 0 until array.length()) {
                        val releaseObj = array.getJSONObject(i)
                        if (releaseObj.optBoolean("draft", false)) continue
                        parseReleaseObject(releaseObj, currentVersion)?.let { result ->
                            return@withContext result
                        }
                        break
                    }
                    return@withContext UpdateCheckResult.UpToDate
                } else {
                    return@withContext UpdateCheckResult.Error("HTTP ${response.code}")
                }
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.localizedMessage ?: "Network unavailable")
        }
    }

    private fun parseReleaseObject(
        json: JSONObject,
        currentVersion: String
    ): UpdateCheckResult? {
        val tagName = json.optString("tag_name", "").trim()
        val cleanRemote = normalizeVersion(tagName)
        if (cleanRemote.isEmpty()) return null

        if (!isNewerVersion(cleanRemote, currentVersion)) {
            return UpdateCheckResult.UpToDate
        }

        val releaseTitle = json.optString("name", "").ifBlank { "v$cleanRemote" }
        val notes = json.optString("body", "").trim()
        val htmlUrl = json.optString("html_url", "").ifBlank { "$REPO_URL/releases/latest" }
        val publishedAt = json.optString("published_at", "").takeIf { it.isNotBlank() }

        var assetSize = -1L
        val assets = json.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val size = asset.optLong("size", -1L)
                if (size > 0L) {
                    assetSize = size
                    break
                }
            }
        }

        return UpdateCheckResult.UpdateAvailable(
            AppUpdateInfo(
                versionName = cleanRemote,
                releaseTitle = releaseTitle,
                releaseNotes = notes,
                releaseSizeBytes = assetSize,
                releasePageUrl = htmlUrl,
                publishedAt = publishedAt
            )
        )
    }

    /**
     * Opens the official HTTPS release page in the user's browser via ACTION_VIEW,
     * adhering strictly to Google Play Protect & Play Policy guidelines
     * (no in-app APK downloading or REQUEST_INSTALL_PACKAGES sideloading).
     */
    fun openReleasePage(context: Context, releasePageUrl: String): Boolean {
        cleanupLegacyUpdateCache(context)
        val trimmed = releasePageUrl.trim()
        val safeUrl = if (trimmed.startsWith("https://", ignoreCase = true)) {
            trimmed
        } else {
            "$REPO_URL/releases/latest"
        }

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun cleanupLegacyUpdateCache(context: Context) {
        runCatching {
            val legacyDir = File(context.cacheDir, "updates")
            if (legacyDir.exists()) {
                legacyDir.deleteRecursively()
            }
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return ""
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        return if (mb >= 1.0) {
            String.format(Locale.US, "%.1f MB", mb)
        } else {
            val kb = bytes.toDouble() / 1024.0
            String.format(Locale.US, "%.0f KB", kb)
        }
    }

    fun normalizeVersion(raw: String): String {
        return raw.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-")
            .trim()
    }

    fun isNewerVersion(remoteVersion: String, localVersion: String): Boolean {
        val remoteParts = normalizeVersion(remoteVersion)
            .split(".")
            .map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        val localParts = normalizeVersion(localVersion)
            .split(".")
            .map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }

        val maxLen = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }
}
