package com.example

import com.example.data.model.LocalDeviceVideo
import com.example.data.model.MAX_SHORT_VIDEO_DURATION_MS
import com.example.data.model.effectiveDisplayDimensions
import com.example.data.model.isEligibleShortVideo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortVideoFilterUnitTest {

    @Test
    fun portraitVideoWithin30MinutesIsEligible() {
        val video = LocalDeviceVideo(
            id = "v1",
            title = "reel_1.mp4",
            durationText = "02:30",
            sizeText = "12 MB",
            durationMs = 150_000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            isPortrait = true
        )
        assertTrue(video.isEligibleShortVideo())
        assertEquals(1080 to 1920, video.effectiveDisplayDimensions())
    }

    @Test
    fun portraitVideoExactly30MinutesIsEligible() {
        val video = LocalDeviceVideo(
            id = "v2",
            title = "reel_30m.mp4",
            durationText = "30:00",
            sizeText = "400 MB",
            durationMs = MAX_SHORT_VIDEO_DURATION_MS,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            isPortrait = true
        )
        assertTrue(video.isEligibleShortVideo())
    }

    @Test
    fun portraitVideoOver30MinutesIsRejected() {
        val video = LocalDeviceVideo(
            id = "v3",
            title = "long_portrait.mp4",
            durationText = "30:01",
            sizeText = "420 MB",
            durationMs = MAX_SHORT_VIDEO_DURATION_MS + 1_000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            isPortrait = true
        )
        assertFalse(video.isEligibleShortVideo())
    }

    @Test
    fun landscapeVideoIsRejectedEvenIfShort() {
        val video = LocalDeviceVideo(
            id = "v4",
            title = "landscape_clip.mp4",
            durationText = "00:45",
            sizeText = "15 MB",
            durationMs = 45_000L,
            width = 1920,
            height = 1080,
            rotationDegrees = 0,
            isPortrait = false
        )
        assertFalse(video.isEligibleShortVideo())
    }

    @Test
    fun rotatedVerticalVideoSwapsEffectiveDimensions() {
        val video = LocalDeviceVideo(
            id = "v5",
            title = "camera_vertical.mp4",
            durationText = "01:10",
            sizeText = "20 MB",
            durationMs = 70_000L,
            width = 1920,
            height = 1080,
            rotationDegrees = 90,
            isPortrait = true
        )
        assertEquals(1080 to 1920, video.effectiveDisplayDimensions())
        assertTrue(video.isEligibleShortVideo())
    }
}
