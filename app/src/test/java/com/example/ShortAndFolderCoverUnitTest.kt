package com.example

import com.example.data.model.LocalDeviceVideo
import com.example.data.model.SHORT_MAX_DURATION_MS
import com.example.data.model.isShortEligible
import com.example.data.util.OnlineFolderCoverFetcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortAndFolderCoverUnitTest {

    @Test
    fun isShortEligible_acceptsPortraitUpToThirtyMinutesOnly() {
        val validShort = LocalDeviceVideo(
            id = "v1",
            title = "Reel",
            folderName = "Camera",
            durationText = "00:45",
            sizeText = "12 MB",
            resolutionText = "1080x1920",
            durationMs = 45_000L,
            width = 1080,
            height = 1920,
            isPortrait = true
        )
        val exactLimit = validShort.copy(id = "v2", durationMs = SHORT_MAX_DURATION_MS)
        val overThirtyMin = validShort.copy(id = "v3", durationMs = SHORT_MAX_DURATION_MS + 1L)
        val landscape = validShort.copy(id = "v4", width = 1920, height = 1080, isPortrait = false)
        val zeroDuration = validShort.copy(id = "v5", durationMs = 0L)

        assertTrue(validShort.isShortEligible())
        assertTrue(exactLimit.isShortEligible())
        assertFalse(overThirtyMin.isShortEligible())
        assertFalse(landscape.isShortEligible())
        assertFalse(zeroDuration.isShortEligible())
    }

    @Test
    fun upgradeArtworkUrlToHd_upgradesKnownSourcesAndForcesHttps() {
        assertEquals(
            "https://image.tmdb.org/t/p/w780/abcposter.jpg",
            OnlineFolderCoverFetcher.upgradeArtworkUrlToHd("http://image.tmdb.org/t/p/w185/abcposter.jpg")
        )
        assertEquals(
            "https://image.tmdb.org/t/p/original/abcposter.jpg",
            OnlineFolderCoverFetcher.upgradeArtworkUrlToHd("https://image.tmdb.org/t/p/original/abcposter.jpg")
        )
        assertEquals(
            "https://is1-ssl.mzstatic.com/image/thumb/Music/1000x1000bb.jpg",
            OnlineFolderCoverFetcher.upgradeArtworkUrlToHd("https://is1-ssl.mzstatic.com/image/thumb/Music/100x100bb.jpg")
        )
        assertEquals(
            "https://static.tvmaze.com/uploads/images/original_portrait/12/34.jpg",
            OnlineFolderCoverFetcher.upgradeArtworkUrlToHd("http://static.tvmaze.com/uploads/images/medium_portrait/12/34.jpg")
        )
        assertEquals(
            "",
            OnlineFolderCoverFetcher.upgradeArtworkUrlToHd("ftp://example.com/poster.jpg")
        )
    }

    @Test
    fun isCoverMetadataValid_enforcesStrictResolutionAndAspectRatioGatekeeper() {
        // Posters: require >= 600w x 800h and portrait aspect ratio
        assertTrue(OnlineFolderCoverFetcher.isCoverMetadataValid(680, 1000, isMusicFolder = false))
        assertTrue(OnlineFolderCoverFetcher.isCoverMetadataValid(780, 1170, isMusicFolder = false))
        assertFalse(OnlineFolderCoverFetcher.isCoverMetadataValid(420, 600, isMusicFolder = false))
        assertFalse(OnlineFolderCoverFetcher.isCoverMetadataValid(600, 799, isMusicFolder = false))
        assertFalse(OnlineFolderCoverFetcher.isCoverMetadataValid(1280, 720, isMusicFolder = false))

        // Square album covers: require >= 600x600 and square aspect ratio
        assertTrue(OnlineFolderCoverFetcher.isCoverMetadataValid(1000, 1000, isMusicFolder = true))
        assertFalse(OnlineFolderCoverFetcher.isCoverMetadataValid(500, 500, isMusicFolder = true))
        assertFalse(OnlineFolderCoverFetcher.isCoverMetadataValid(1000, 450, isMusicFolder = true))
    }

    @Test
    fun normalizeFolderQuery_filtersGenericSystemFoldersAndStripsReleaseNoise() {
        assertNull(OnlineFolderCoverFetcher.normalizeFolderQuery("Camera"))
        assertNull(OnlineFolderCoverFetcher.normalizeFolderQuery("Downloads"))
        assertNull(OnlineFolderCoverFetcher.normalizeFolderQuery("WhatsApp Video"))

        assertEquals(
            "Attack on Titan",
            OnlineFolderCoverFetcher.normalizeFolderQuery("Attack.on.Titan.S01.[1080p].HEVC")
        )
        assertEquals(
            "Inception",
            OnlineFolderCoverFetcher.normalizeFolderQuery("Inception (2010) BluRay")
        )
    }
}
