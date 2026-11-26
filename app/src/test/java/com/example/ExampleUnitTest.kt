package com.example

import com.example.data.local.WatchSettingsConfig
import com.example.data.local.WatchSettingsEntity
import com.example.data.model.LocalDeviceVideo
import com.example.data.model.isNewWithinDays
import com.example.data.util.EpisodeParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Local JVM unit tests for core utilities and defaults.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun watchSettings_newVideoBadgeDefaultsToEnabledFor7Days() {
    val entity = WatchSettingsEntity()
    val config = WatchSettingsConfig()
    assertTrue(entity.showNewVideoBadge)
    assertEquals(7, entity.newVideoBadgeDays)
    assertTrue(config.showNewVideoBadge)
    assertEquals(7, config.newVideoBadgeDays)
  }

  @Test
  fun localDeviceVideo_isNewWithinDaysRespectsWindowAndBoundaries() {
    val nowMs = 1_700_000_000_000L
    val oneDayMs = 86_400_000L

    val addedToday = LocalDeviceVideo(
      id = "v1",
      title = "clip1.mp4",
      durationText = "02:00",
      sizeText = "12 MB",
      dateAddedTimestampMs = nowMs - (3 * oneDayMs)
    )
    assertTrue(addedToday.isNewWithinDays(days = 7, nowMs = nowMs))
    assertFalse(addedToday.isNewWithinDays(days = 2, nowMs = nowMs))
    assertFalse(addedToday.isNewWithinDays(days = 0, nowMs = nowMs))

    val added8DaysAgo = addedToday.copy(dateAddedTimestampMs = nowMs - (8 * oneDayMs))
    assertFalse(added8DaysAgo.isNewWithinDays(days = 7, nowMs = nowMs))
    assertTrue(added8DaysAgo.isNewWithinDays(days = 14, nowMs = nowMs))

    val missingTimestamp = addedToday.copy(dateAddedTimestampMs = 0L)
    assertFalse(missingTimestamp.isNewWithinDays(days = 7, nowMs = nowMs))
  }

  @Test
  fun episodeParser_ignoresCameraFolderAndDetectsOtherFolders() {
    val cameraByFolder = LocalDeviceVideo(
      id = "cam_1",
      title = "VID_20261122_05.mp4",
      durationText = "01:12",
      sizeText = "24 MB",
      folderName = "Camera"
    )
    assertNull(EpisodeParser.parseForVideo(cameraByFolder))

    val cameraByRelPath = LocalDeviceVideo(
      id = "cam_2",
      title = "Episode 03.mp4",
      durationText = "00:45",
      sizeText = "18 MB",
      folderName = "DCIM",
      relativePath = "DCIM/Camera/"
    )
    assertNull(EpisodeParser.parseForVideo(cameraByRelPath))

    val seriesVideo = LocalDeviceVideo(
      id = "series_1",
      title = "Solo Leveling S02E07 1080p.mkv",
      durationText = "23:40",
      sizeText = "420 MB",
      folderName = "Anime",
      relativePath = "Movies/Anime/"
    )
    val parsed = EpisodeParser.parseForVideo(seriesVideo)
    assertEquals(7, parsed?.episodeNumber)
    assertEquals(2, parsed?.seasonNumber)
  }

  @Test
  fun forgeUiState_tracksTwoIndependentFlameShieldLabsAndWorkshops() {
    val startEpochMs = 1_700_000_000_000L
    val lab0Active = com.example.data.model.FlameShieldForgeSlotState(
      slotIndex = 0,
      isActive = true,
      selectedOption = com.example.data.model.FlameShieldCraftOption.SHIELDS_100,
      startedAtEpochMs = startEpochMs
    )
    val lab1Ready = com.example.data.model.FlameShieldForgeSlotState(
      slotIndex = 1,
      isActive = true,
      selectedOption = com.example.data.model.FlameShieldCraftOption.DIAMOND_1,
      startedAtEpochMs = startEpochMs - com.example.data.model.FLAME_SHIELD_CRAFT_DURATION_MS - 1_000L
    )

    assertFalse(lab0Active.isReadyToClaim(nowEpochMs = startEpochMs + 60_000L))
    assertTrue(lab0Active.progress(nowEpochMs = startEpochMs + com.example.data.model.FLAME_SHIELD_CRAFT_DURATION_MS / 2L) in 0.49f..0.51f)
    assertTrue(lab1Ready.isReadyToClaim(nowEpochMs = startEpochMs))

    val nowForActive = System.currentTimeMillis()
    val state = com.example.data.model.ForgeUiState(
      flameShieldSlot = lab0Active.copy(startedAtEpochMs = nowForActive - 5_000L),
      flameShieldSlots = listOf(
        lab0Active.copy(startedAtEpochMs = nowForActive - 5_000L),
        lab1Ready
      ),
      slots = com.example.data.model.ForgeKeyTier.entries.associateWith { tier ->
        if (tier == com.example.data.model.ForgeKeyTier.BRONZE) {
          com.example.data.model.ForgeSlotState(tier = tier, isActive = true, elapsedSeconds = 120)
        } else {
          com.example.data.model.ForgeSlotState(tier = tier)
        }
      }
    )

    assertEquals(2, state.flameShieldSlots.size)
    assertEquals(1, state.activeFlameLabsCount)
    assertEquals(1, state.readyFlameLabsCount)
    assertEquals(2, state.activeSlotsCount)
    assertEquals(1, state.readySlotsCount)
    assertEquals(0, state.getFlameShieldSlot(0).slotIndex)
    assertEquals(1, state.getFlameShieldSlot(1).slotIndex)
  }

  @Test
  fun holdSpeedDragRight_stepsThroughSpeedTiersAndRespectsHysteresisAndEdgeMargin() {
    val viewportWidth = 1080f
    val baseStep = 132f
    val minStep = 66f
    val edgeMargin = 60f

    // Starting at center: dragging right cycles 2.0x -> 2.5x -> 3.0x -> 3.5x -> 4.0x
    val centerAnchor = 540f
    val stepCenter = com.example.ui.components.computeHoldSpeedStepPx(
      anchorX = centerAnchor,
      viewportWidthPx = viewportWidth,
      baseStepPx = baseStep,
      minStepPx = minStep,
      edgeMarginPx = edgeMargin
    )
    assertEquals(
      0,
      com.example.ui.components.computeHoldSpeedTierIndexWithHysteresis(
        deltaRightPx = 0f,
        currentIndex = 0,
        anchorX = centerAnchor,
        viewportWidthPx = viewportWidth,
        baseStepPx = baseStep,
        minStepPx = minStep,
        edgeMarginPx = edgeMargin
      )
    )
    assertEquals(2.0f, com.example.ui.components.HOLD_SPEED_TIERS[0], 0.001f)

    // Advance 2.1 steps right -> 3.0x (index 2)
    val idxAtTwoSteps = com.example.ui.components.computeHoldSpeedTierIndexWithHysteresis(
      deltaRightPx = stepCenter * 2.1f,
      currentIndex = 0,
      anchorX = centerAnchor,
      viewportWidthPx = viewportWidth,
      baseStepPx = baseStep,
      minStepPx = minStep,
      edgeMarginPx = edgeMargin
    )
    assertEquals(2, idxAtTwoSteps)
    assertEquals(3.0f, com.example.ui.components.HOLD_SPEED_TIERS[idxAtTwoSteps], 0.001f)

    // Small backward jitter (1.92 * stepCenter) stays on index 2 due to hysteresis
    val idxWithJitter = com.example.ui.components.computeHoldSpeedTierIndexWithHysteresis(
      deltaRightPx = stepCenter * 1.92f,
      currentIndex = 2,
      anchorX = centerAnchor,
      viewportWidthPx = viewportWidth,
      baseStepPx = baseStep,
      minStepPx = minStep,
      edgeMarginPx = edgeMargin
    )
    assertEquals(2, idxWithJitter)

    // Clear pull back (1.70 * stepCenter) drops cleanly to index 1 (2.5x)
    val idxPulledBack = com.example.ui.components.computeHoldSpeedTierIndexWithHysteresis(
      deltaRightPx = stepCenter * 1.70f,
      currentIndex = 2,
      anchorX = centerAnchor,
      viewportWidthPx = viewportWidth,
      baseStepPx = baseStep,
      minStepPx = minStep,
      edgeMarginPx = edgeMargin
    )
    assertEquals(1, idxPulledBack)

    // Starting near right bezel compresses step spacing down toward minStepPx so 4.0x remains reachable
    val nearRightEdgeAnchor = 920f
    val stepEdge = com.example.ui.components.computeHoldSpeedStepPx(
      anchorX = nearRightEdgeAnchor,
      viewportWidthPx = viewportWidth,
      baseStepPx = baseStep,
      minStepPx = minStep,
      edgeMarginPx = edgeMargin
    )
    assertTrue(stepEdge in minStep..baseStep)
    val maxIdx = com.example.ui.components.computeHoldSpeedTierIndexWithHysteresis(
      deltaRightPx = stepEdge * 4.2f,
      currentIndex = 0,
      anchorX = nearRightEdgeAnchor,
      viewportWidthPx = viewportWidth,
      baseStepPx = baseStep,
      minStepPx = minStep,
      edgeMarginPx = edgeMargin
    )
    assertEquals(com.example.ui.components.HOLD_SPEED_TIERS.lastIndex, maxIdx)
    assertEquals(4.0f, com.example.ui.components.HOLD_SPEED_TIERS[maxIdx], 0.001f)
  }
}
