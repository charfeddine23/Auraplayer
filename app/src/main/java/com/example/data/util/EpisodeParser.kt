package com.example.data.util

import com.example.data.model.LocalDeviceVideo
import java.net.URLDecoder
import java.util.Locale

data class ParsedEpisodeInfo(
    val episodeNumber: Int,
    val seasonNumber: Int? = null
) {
    fun formatCompactBadge(isArabic: Boolean): String {
        val epText = episodeNumber.toString().padStart(2, '0')
        return if (seasonNumber != null) {
            if (isArabic) "م$seasonNumber • ح $epText" else "S${seasonNumber} · E$epText"
        } else {
            if (isArabic) "ح $epText" else "EP $epText"
        }
    }

    fun formatReadableLabel(isArabic: Boolean): String {
        val epText = episodeNumber.toString().padStart(2, '0')
        return if (seasonNumber != null) {
            if (isArabic) "الموسم $seasonNumber • الحلقة $epText" else "Season $seasonNumber • Episode $epText"
        } else {
            if (isArabic) "الحلقة $epText" else "Episode $epText"
        }
    }
}

object EpisodeParser {

    private val EXTENSION_REGEX = Regex(
        "\\.(mp4|mkv|avi|mov|flv|webm|ts|m4v|3gp|wmv)$",
        RegexOption.IGNORE_CASE
    )

    // Strip common technical metadata tokens so numbers like 1080, 720, 2024, x264, 60fps never masquerade as episodes.
    private val TECH_NOISE_REGEX = Regex(
        "(?i)(?:" +
            "\\b(?:240|360|480|540|576|720|1080|1440|2160|4320)[pi]\\b" +
            "|\\b(?:2k|4k|8k|uhd|fhd|hd|sd)\\b" +
            "|\\b(?:x264|x265|h\\.?264|h\\.?265|hevc|avc|av1|vp9|xvid|divx|10bit|8bit|12bit)\\b" +
            "|\\b(?:24|25|30|50|60|120|144)fps\\b" +
            "|\\b(?:aac|ac3|eac3|dts|ddp5\\.?1|dd5\\.?1|5\\.1|7\\.1|flac|mp3)\\b" +
            "|\\[(?:[0-9a-f]{6,10})]" + // CRC32 hashes like [A1B2C3D4]
            "|\\b(?:19|20)\\d{2}\\b" + // Release years 1900..2099
            "|\\b\\d{3,4}x\\d{3,4}\\b" + // Dimensions like 1920x1080
            ")"
    )

    // 1. S01E12 / S1 E12 / S01.E05
    private val SEASON_EPISODE_REGEX = Regex(
        "(?i)(?:^|[\\s.\\-_\\[(])s\\s*0*(\\d{1,2})\\s*[.\\-_ ]?\\s*e(?:p)?\\s*0*(\\d{1,4})(?=\\b|[\\s.\\-_\\])]|$)"
    )

    // 2. Arabic explicit markers: الحلقة 12 / حلقة 05 / ح12 / ح 12
    private val ARABIC_EPISODE_REGEX = Regex(
        "(?:^|[\\s.\\-_\\[(:])(?:الحلقة|حلقة|الحلقه|حلقه|ح)(?:\\s*رقم)?[\\s.\\-_:#]*0*(\\d{1,4})(?=$|[\\s.\\-_\\])|،])"
    )

    // 3. Explicit English/Latin episode markers: Episode 12, Ep 04, Ep.12, E08, #12
    private val EXPLICIT_EPISODE_REGEX = Regex(
        "(?i)(?:^|[\\s.\\-_\\[(])(?:episode|episodio|ep|e)[\\s.\\-_:#]*0*(\\d{1,4})(?=$|[\\s.\\-_\\])|])"
    )

    // 4. Hash marker: #12
    private val HASH_EPISODE_REGEX = Regex(
        "(?:^|\\s)#0*(\\d{1,4})(?=$|[\\s.\\-_\\])])"
    )

    // 5. Separated trailing/bracketed episode number: "Naruto - 105", "Show [12]", "Show (04)", "Show - 07 v2"
    private val DELIMITED_NUMBER_REGEX = Regex(
        "(?:\\s[-–—]\\s*|\\[|\\(|_)(?:0*)(\\d{1,4})(?:v\\d)?(?=\\s*(?:\\]|\\)|[-–—]|$))"
    )

    // 6. Fallback trailing space-separated number at end of cleaned title: "Series Name 08"
    private val TRAILING_NUMBER_REGEX = Regex(
        "\\s0*(\\d{1,3})$"
    )

    fun isCameraVideo(video: LocalDeviceVideo): Boolean {
        return isCameraVideo(
            folderName = video.folderName,
            relativePath = video.relativePath,
            uriString = video.uriString
        )
    }

    fun isCameraVideo(
        folderName: String? = null,
        relativePath: String? = null,
        uriString: String? = null
    ): Boolean {
        val cleanFolder = folderName?.trim()?.lowercase(Locale.ROOT).orEmpty()
        if (cleanFolder == "camera" || cleanFolder == "الكاميرا" || cleanFolder == "dcim/camera") {
            return true
        }

        val cleanRelPath = relativePath
            ?.replace('\\', '/')
            ?.trim('/')
            ?.lowercase(Locale.ROOT)
            .orEmpty()
        if (cleanRelPath.isNotEmpty()) {
            val segments = cleanRelPath.split('/').filter { it.isNotBlank() }
            if (segments.lastOrNull() == "camera" || segments.lastOrNull() == "الكاميرا") {
                return true
            }
            for (i in 0 until segments.size - 1) {
                if (segments[i] == "dcim" && segments[i + 1] == "camera") {
                    return true
                }
            }
        }

        if (!uriString.isNullOrBlank()) {
            val decodedUri = try {
                URLDecoder.decode(uriString, "UTF-8")
            } catch (_: Exception) {
                uriString
            }.replace('\\', '/').lowercase(Locale.ROOT)

            if (decodedUri.contains("/dcim/camera/") ||
                decodedUri.endsWith("/dcim/camera") ||
                decodedUri.contains(":dcim/camera/") ||
                decodedUri.endsWith(":dcim/camera")
            ) {
                return true
            }
        }

        return false
    }

    fun parseForVideo(video: LocalDeviceVideo): ParsedEpisodeInfo? {
        if (isCameraVideo(video)) return null
        return parse(video.title)
    }

    fun parse(rawTitle: String): ParsedEpisodeInfo? {
        if (rawTitle.isBlank()) return null

        val normalized = normalizeDigits(rawTitle)
            .replace(EXTENSION_REGEX, "")
            .trim()
        if (normalized.isEmpty()) return null

        // Check Season + Episode first before noise stripping (in case of odd tags)
        SEASON_EPISODE_REGEX.find(normalized)?.let { match ->
            val season = match.groupValues[1].toIntOrNull()
            val ep = match.groupValues[2].toIntOrNull()
            if (season != null && ep != null && season in 1..99 && ep in 1..2500) {
                return ParsedEpisodeInfo(episodeNumber = ep, seasonNumber = season)
            }
        }

        // Arabic explicit match before stripping anything else
        ARABIC_EPISODE_REGEX.find(normalized)?.let { match ->
            val ep = match.groupValues[1].toIntOrNull()
            if (ep != null && ep in 1..2500) {
                return ParsedEpisodeInfo(episodeNumber = ep)
            }
        }

        // Clean technical noise tokens for subsequent patterns
        val cleaned = normalized
            .replace(TECH_NOISE_REGEX, " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleaned.isEmpty()) return null

        EXPLICIT_EPISODE_REGEX.find(cleaned)?.let { match ->
            val ep = match.groupValues[1].toIntOrNull()
            if (ep != null && ep in 1..2500) {
                return ParsedEpisodeInfo(episodeNumber = ep)
            }
        }

        HASH_EPISODE_REGEX.find(cleaned)?.let { match ->
            val ep = match.groupValues[1].toIntOrNull()
            if (ep != null && ep in 1..2500) {
                return ParsedEpisodeInfo(episodeNumber = ep)
            }
        }

        DELIMITED_NUMBER_REGEX.findAll(cleaned).lastOrNull()?.let { match ->
            val ep = match.groupValues[1].toIntOrNull()
            if (ep != null && ep in 1..2500) {
                return ParsedEpisodeInfo(episodeNumber = ep)
            }
        }

        TRAILING_NUMBER_REGEX.find(cleaned)?.let { match ->
            val ep = match.groupValues[1].toIntOrNull()
            if (ep != null && ep in 1..999) {
                return ParsedEpisodeInfo(episodeNumber = ep)
            }
        }

        return null
    }

    private fun normalizeDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '\u0660'..'\u0669' -> sb.append(('0'.code + (ch.code - 0x0660)).toChar())
                in '\u06F0'..'\u06F9' -> sb.append(('0'.code + (ch.code - 0x06F0)).toChar())
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
}
