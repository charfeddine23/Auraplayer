package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.VaultProgressionRegistry
import com.example.data.preferences.StreakPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoadmapXpUnitTest {

    @Test
    fun `xp curve starts at 50 for level 1, ends at 50M for level 365, and increases monotonically`() {
        assertEquals(50L, VaultProgressionRegistry.xpRequiredForLevel(1))
        assertEquals(50_000_000L, VaultProgressionRegistry.xpRequiredForLevel(365))

        var previous = 0L
        for (lvl in 1..365) {
            val req = VaultProgressionRegistry.xpRequiredForLevel(lvl)
            assertTrue("Level $lvl XP ($req) must exceed previous ($previous)", req > previous)
            previous = req
        }
    }

    @Test
    fun `streak xp scales 100 per day up to 365 and stays fixed at 36500 above 365`() {
        assertEquals(0L, VaultProgressionRegistry.streakDayXpReward(0))
        assertEquals(100L, VaultProgressionRegistry.streakDayXpReward(1))
        assertEquals(200L, VaultProgressionRegistry.streakDayXpReward(2))
        assertEquals(36_500L, VaultProgressionRegistry.streakDayXpReward(365))
        assertEquals(36_500L, VaultProgressionRegistry.streakDayXpReward(366))
        assertEquals(36_500L, VaultProgressionRegistry.streakDayXpReward(1000))
    }

    @Test
    fun `non-cumulative per-level xp advances levels independently and watch time gives 10 xp per minute`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StreakPreferencesRepository.getInstance(context)

        // Ensure clean prefs for deterministic test
        context.getSharedPreferences("video_pulse_persistent_store", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        val initialXp = repo.loadRoadmapXpState()
        assertEquals(0, initialXp.unlockedLevel)
        assertEquals(0L, initialXp.currentLevelXp)
        assertEquals(50L, initialXp.requiredLevelXp)

        // Watch 59 seconds -> 0 XP yet, 59s accumulated
        val tick59 = repo.tickDailyWatchAndXp(seconds = 59)
        assertEquals(0L, tick59.watchXpEarned)
        assertEquals(59, tick59.xpState.watchXpSecondsAccum)
        assertEquals(0, tick59.xpState.unlockedLevel)

        // Watch 1 more second -> reaches 60s -> +10 XP
        val tick1 = repo.tickDailyWatchAndXp(seconds = 1)
        assertEquals(10L, tick1.watchXpEarned)
        assertEquals(0, tick1.xpState.watchXpSecondsAccum)
        assertEquals(10L, tick1.xpState.currentLevelXp)
        assertEquals(0, tick1.xpState.unlockedLevel)

        // Watch remaining 240s (4 minutes) to finish 5-min daily goal -> +40 XP Watch + +100 XP Day 1 Streak = +140 XP
        // Total = 10 + 140 = 150 XP -> Level 1 costs 50 XP, Level 2 costs xpRequiredForLevel(2), remainder toward Level 3!
        val tickGoal = repo.tickDailyWatchAndXp(seconds = 240)
        assertTrue(tickGoal.newlyCompletedDailyGoal)
        assertEquals(1, tickGoal.streakState.streakDays)
        assertEquals(40L, tickGoal.watchXpEarned)
        assertEquals(100L, tickGoal.streakXpEarned)

        val l1Cost = VaultProgressionRegistry.xpRequiredForLevel(1)
        val l2Cost = VaultProgressionRegistry.xpRequiredForLevel(2)
        val expectedRemainingAfterL1L2 = 150L - l1Cost - l2Cost
        assertEquals(2, tickGoal.xpState.unlockedLevel)
        assertEquals(expectedRemainingAfterL1L2, tickGoal.xpState.currentLevelXp)
        assertEquals(3, tickGoal.xpState.targetLevel)
    }
}
