package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AmbientBlackBlurMode
import com.example.data.model.FLAME_SHIELD_CRAFT_DURATION_MS
import com.example.data.model.FLAME_SHIELD_LABS_COUNT
import com.example.data.model.FlameShieldCraftOption
import com.example.data.model.FlameShieldForgeSlotState
import com.example.data.model.ForgeKeyTier
import com.example.data.model.ForgeSlotState
import com.example.data.model.ForgeUiState
import com.example.data.model.HomeMimicItem
import com.example.data.model.PlayerSeekbarStyle
import com.example.data.model.RoadmapXpState
import com.example.data.model.StoreAvatarItem
import com.example.data.model.VaultProgressionRegistry
import com.example.data.util.SecurityUtils
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.StoreTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.StreakUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyWatchTickOutcome(
    val streakState: StreakUiState,
    val xpState: RoadmapXpState,
    val newlyCompletedDailyGoal: Boolean,
    val streakXpEarned: Long,
    val watchXpEarned: Long,
    val previousRoadmapLevel: Int,
    val newRoadmapLevel: Int
)

class StreakPreferencesRepository private constructor(context: Context) {

    private val appContext: Context = context.applicationContext

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "video_pulse_persistent_store"

        private const val KEY_FIRST_RUN_DONE = "key_first_run_done"
        private const val KEY_PREFS_VERSION = "key_prefs_version"
        private const val CURRENT_PREFS_VERSION = 2

        private const val KEY_STREAK_DAYS = "key_streak_days"
        private const val KEY_SHIELDS_COUNT = "key_shields_count"
        private const val KEY_MAX_SHIELDS = "key_max_shields"
        const val MAX_SHIELDS_CAPACITY = 1000
        private const val KEY_DAILY_WATCH_SECONDS = "key_daily_watch_seconds"
        private const val KEY_DAILY_GOAL_SECONDS = "key_daily_goal_seconds"
        private const val KEY_IS_GOAL_COMPLETED_TODAY = "key_is_goal_completed_today"
        private const val KEY_LAST_ACTIVE_DATE = "key_last_active_date"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_ACTIVE_STORE_THEME = "key_active_store_theme"
        private const val KEY_LAST_OWNED_STORE_THEME = "key_last_owned_store_theme"
        private const val KEY_PURCHASED_STORE_THEMES = "key_purchased_store_themes"
        private const val KEY_TRIAL_THEME_ID = "key_trial_theme_id"
        private const val KEY_TRIAL_EXPIRES_AT_MS = "key_trial_expires_at_ms"
        private const val TRIAL_DURATION_MS = 60_000L
        private const val KEY_WAVEFORM_SEEKBAR_PURCHASED = "key_waveform_seekbar_purchased"
        private const val KEY_WAVEFORM_SEEKBAR_ENABLED = "key_waveform_seekbar_enabled"
        private const val KEY_SHOOTING_STAR_SEEKBAR_PURCHASED = "key_shooting_star_seekbar_purchased"
        private const val KEY_ANGRY_SNAKE_SEEKBAR_PURCHASED = "key_angry_snake_seekbar_purchased"
        private const val KEY_PURCHASED_SEEKBAR_IDS = "key_purchased_seekbar_ids"
        private const val KEY_ACTIVE_SEEKBAR_STYLE = "key_active_seekbar_style"
        const val SHIELDS_PER_FARM_HOUR = 10
        val WAVEFORM_SEEKBAR_COST_TIER = PlayerSeekbarStyle.WAVEFORM.costTier ?: ForgeKeyTier.BRONZE
        val WAVEFORM_SEEKBAR_COST_AMOUNT = PlayerSeekbarStyle.WAVEFORM.costAmount
        val WAVEFORM_SEEKBAR_SHIELDS_COST = PlayerSeekbarStyle.WAVEFORM.shieldsCost
        val SHOOTING_STAR_SEEKBAR_COST_TIER = PlayerSeekbarStyle.SHOOTING_STAR.costTier ?: ForgeKeyTier.SILVER
        val SHOOTING_STAR_SEEKBAR_COST_AMOUNT = PlayerSeekbarStyle.SHOOTING_STAR.costAmount
        val SHOOTING_STAR_SEEKBAR_SHIELDS_COST = PlayerSeekbarStyle.SHOOTING_STAR.shieldsCost
        val ANGRY_SNAKE_SEEKBAR_COST_TIER = PlayerSeekbarStyle.ANGRY_SNAKE.costTier ?: ForgeKeyTier.GOLD
        val ANGRY_SNAKE_SEEKBAR_COST_AMOUNT = PlayerSeekbarStyle.ANGRY_SNAKE.costAmount
        val ANGRY_SNAKE_SEEKBAR_SHIELDS_COST = PlayerSeekbarStyle.ANGRY_SNAKE.shieldsCost
        private const val KEY_MIMIC_FLAME_ENABLED = "key_mimic_flame_enabled"
        private const val KEY_MIMIC_DNA_PURCHASED = "key_mimic_dna_purchased"
        private const val KEY_MIMIC_DNA_ENABLED = "key_mimic_dna_enabled"
        private const val KEY_PURCHASED_MIMIC_IDS = "key_purchased_mimic_ids"
        private const val KEY_PURCHASED_AMBIENT_MODES = "key_purchased_ambient_modes"
        private const val KEY_PURCHASED_CYBER_AVATARS = "key_purchased_cyber_avatars"
        val DNA_MIMIC_COST_TIER = HomeMimicItem.DNA_CHROMOSOME.costTier ?: ForgeKeyTier.GOLD
        val DNA_MIMIC_COST_AMOUNT = HomeMimicItem.DNA_CHROMOSOME.costAmount
        val DNA_MIMIC_SHIELDS_COST = HomeMimicItem.DNA_CHROMOSOME.shieldsCost
        private const val KEY_APP_LANGUAGE = "key_app_language"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_AVATAR_URI = "key_user_avatar_uri"
        private const val KEY_USER_AVATAR_PRESET = "key_user_avatar_preset"
        private const val KEY_WATCH_VIEW_MODE = "key_watch_view_mode"
        private const val KEY_HIDE_FILE_EXTENSIONS = "key_hide_file_extensions"
        private const val KEY_LAST_WATCHED_ID = "key_last_watched_id"
        private const val KEY_LAST_WATCHED_TITLE = "key_last_watched_title"
        private const val KEY_LAST_WATCHED_URI = "key_last_watched_uri"
        private const val KEY_LAST_WATCHED_DURATION_MS = "key_last_watched_duration_ms"
        private const val KEY_LAST_WATCHED_POSITION_MS = "key_last_watched_position_ms"
        private const val KEY_LAST_WATCHED_THUMBNAIL_RES = "key_last_watched_thumbnail_res"
        private const val KEY_LAST_WATCHED_TIMESTAMP = "key_last_watched_timestamp"
        private const val KEY_OPENED_VIDEO_KEYS = "key_opened_video_keys"
        private const val KEY_PINNED_VIDEO_KEYS = "key_pinned_video_keys"
        private const val KEY_PINNED_FOLDER_NAMES = "key_pinned_folder_names"

        // Shield Farming (60-Min Rule) & Analytics
        private const val KEY_TOTAL_ACTIVE_WATCH_SECONDS = "key_total_active_watch_seconds"
        private const val KEY_SHIELD_FARM_SECONDS = "key_shield_farm_seconds"
        private const val KEY_SHIELDS_EARNED_TODAY_DATE = "key_shields_earned_today_date"
        private const val KEY_SHIELDS_EARNED_TODAY_COUNT = "key_shields_earned_today_count"
        private const val KEY_TOTAL_TRACKS_LISTENED = "key_total_tracks_listened"
        private const val KEY_LISTEN_TOTAL_SECONDS = "key_listen_total_seconds"
        private const val KEY_LISTEN_MEMBER_SINCE = "key_listen_member_since"
        private const val KEY_LISTEN_PLAYLISTS_JSON = "key_listen_playlists_json"
        private const val KEY_LISTEN_FAVORITE_IDS = "key_listen_favorite_ids"
        private const val KEY_LISTEN_DOWNLOADED_IDS = "key_listen_downloaded_ids"
        private const val KEY_LISTEN_HISTORY_JSON = "key_listen_history_json"
        private const val KEY_LISTEN_TRACK_PLAYS_JSON = "key_listen_track_plays_json"

        // Hidden Vault & 365-Level XP Roadmap
        private const val KEY_VAULT_PIN = "key_vault_pin"
        private const val KEY_VAULT_PIN_HASH = "key_vault_pin_hash"
        private const val KEY_VAULT_PIN_SALT = "key_vault_pin_salt"
        private const val KEY_VAULT_MEDIA_IDS = "key_vault_media_ids"
        private const val KEY_CLAIMED_VAULT_LEVELS = "key_claimed_vault_levels"
        private const val KEY_ROADMAP_UNLOCKED_LEVEL = "key_roadmap_unlocked_level"
        private const val KEY_ROADMAP_CURRENT_LEVEL_XP = "key_roadmap_current_level_xp"
        private const val KEY_WATCH_XP_SECONDS_ACCUM = "key_watch_xp_seconds_accum"
        private const val KEY_LAST_STREAK_XP_DATE = "key_last_streak_xp_date"

        // System Settings & Premium Gate
        private const val KEY_RGB_ACCENT = "key_rgb_accent"
        private const val KEY_OLED_BLACK = "key_oled_black"
        private const val KEY_AUTO_RESUME = "key_auto_resume"
        private const val KEY_DEFAULT_SPEED = "key_default_speed"
        private const val KEY_BLACKLISTED_FOLDERS = "key_blacklisted_folders"
        private const val KEY_PREMIUM_ACTIVATED = "key_premium_activated"
        private const val KEY_SELECTED_THEME_PRESET = "key_selected_theme_preset"
        private const val KEY_WIFI_DIRECT_SHARING = "key_wifi_direct_sharing"
        private const val KEY_SMART_NOTIFICATIONS = "key_smart_notifications"
        private const val KEY_BACKGROUND_PLAY = "key_background_play"
        private const val KEY_PIP_MODE = "key_pip_mode"
        private const val KEY_EYE_PROTECTION = "key_eye_protection"
        private const val KEY_FULL_SCREEN_MODE = "key_full_screen_mode"
        private const val KEY_LAST_STATUS_BAR_HEIGHT_DP = "key_last_status_bar_height_dp"

        // The Forge (Shields & Keys Smelting + The Flame Shield)
        private const val KEY_FORGE_BRONZE_KEYS = "key_forge_bronze_keys"
        private const val KEY_FORGE_SILVER_KEYS = "key_forge_silver_keys"
        private const val KEY_FORGE_GOLDEN_KEYS = "key_forge_golden_keys"
        private const val KEY_FORGE_DIAMOND_KEYS = "key_forge_diamond_keys"
        private const val KEY_FLAME_SHIELDS_COUNT = "key_flame_shields_count"
        private const val KEY_PENDING_FLAME_EVENT = "key_pending_flame_event"
        private const val KEY_FLAME_FORGE_ACTIVE = "key_flame_forge_active"
        private const val KEY_FLAME_FORGE_OPTION = "key_flame_forge_option"
        private const val KEY_FLAME_FORGE_STARTED_AT_MS = "key_flame_forge_started_at_ms"
        private const val KEY_FLAME_FORGE_REFUND_SHIELDS = "key_flame_forge_refund_shields"
        private const val KEY_FLAME_FORGE_REFUND_BRONZE = "key_flame_forge_refund_bronze"
        private const val KEY_FLAME_FORGE_REFUND_SILVER = "key_flame_forge_refund_silver"
        private const val KEY_FLAME_FORGE_REFUND_DIAMOND = "key_flame_forge_refund_diamond"
        private const val KEY_FLAME_FORGE_2_ACTIVE = "key_flame_forge_2_active"
        private const val KEY_FLAME_FORGE_2_OPTION = "key_flame_forge_2_option"
        private const val KEY_FLAME_FORGE_2_STARTED_AT_MS = "key_flame_forge_2_started_at_ms"
        private const val KEY_FLAME_FORGE_2_REFUND_SHIELDS = "key_flame_forge_2_refund_shields"
        private const val KEY_FLAME_FORGE_2_REFUND_BRONZE = "key_flame_forge_2_refund_bronze"
        private const val KEY_FLAME_FORGE_2_REFUND_SILVER = "key_flame_forge_2_refund_silver"
        private const val KEY_FLAME_FORGE_2_REFUND_DIAMOND = "key_flame_forge_2_refund_diamond"
        private const val KEY_MY_LIST_MEDIA_IDS = "key_my_list_media_ids"
        private const val KEY_SHORT_LIKED_VIDEO_KEYS = "key_short_liked_video_keys"
        private const val KEY_SHORT_SAVED_VIDEO_KEYS = "key_short_saved_video_keys"

        @Volatile
        private var instance: StreakPreferencesRepository? = null

        fun getInstance(context: Context): StreakPreferencesRepository {
            val appCtx = context.applicationContext ?: context
            val existing = instance
            if (existing != null && existing.appContext === appCtx) {
                return existing
            }
            return synchronized(this) {
                val doubleCheck = instance
                if (doubleCheck != null && doubleCheck.appContext === appCtx) {
                    doubleCheck
                } else {
                    StreakPreferencesRepository(appCtx).also { instance = it }
                }
            }
        }
    }

    private fun getTodayDateString(): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.format(Date())
        } catch (_: Exception) {
            "today"
        }
    }

    private fun calculateCalendarDaysBetween(startDateStr: String, endDateStr: String): Int? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                isLenient = false
            }
            val start = sdf.parse(startDateStr) ?: return null
            val end = sdf.parse(endDateStr) ?: return null
            val diffMs = end.time - start.time
            val dayMs = 86_400_000L
            ((diffMs + (dayMs / 2L)) / dayMs).toInt()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Loads the persistent Streak, Shield, and Watch state from disk.
     * Automatically enforces consecutive streak rules across day boundaries:
     * missing a day consumes Shields or resets streakDays to 0 (re-freezing Mimics).
     */
    fun loadInitialState(): StreakUiState {
        val today = getTodayDateString()
        val isFirstRun = !prefs.getBoolean(KEY_FIRST_RUN_DONE, false)
        val currentVersion = prefs.getInt(KEY_PREFS_VERSION, 0)

        if (isFirstRun || currentVersion < CURRENT_PREFS_VERSION) {
            val defaultStreak = 0
            val defaultShields = 0
            val defaultWatchSeconds = 0

            prefs.edit()
                .putBoolean(KEY_FIRST_RUN_DONE, true)
                .putInt(KEY_PREFS_VERSION, CURRENT_PREFS_VERSION)
                .putInt(KEY_STREAK_DAYS, defaultStreak)
                .putInt(KEY_SHIELDS_COUNT, defaultShields)
                .putInt(KEY_MAX_SHIELDS, MAX_SHIELDS_CAPACITY)
                .putInt(KEY_DAILY_WATCH_SECONDS, defaultWatchSeconds)
                .putInt(KEY_DAILY_GOAL_SECONDS, 300)
                .putBoolean(KEY_IS_GOAL_COMPLETED_TODAY, false)
                .putString(KEY_LAST_ACTIVE_DATE, today)
                .putString(KEY_SHIELDS_EARNED_TODAY_DATE, today)
                .putInt(KEY_SHIELDS_EARNED_TODAY_COUNT, 0)
                .commit()

            return StreakUiState(
                streakDays = defaultStreak,
                shieldsCount = defaultShields,
                maxShields = MAX_SHIELDS_CAPACITY,
                dailyWatchSeconds = defaultWatchSeconds,
                dailyGoalSeconds = 300,
                isGoalCompletedToday = false
            )
        }

        var savedStreak = prefs.getInt(KEY_STREAK_DAYS, 0)
        val storedMaxShields = prefs.getInt(KEY_MAX_SHIELDS, MAX_SHIELDS_CAPACITY)
        val maxShields = storedMaxShields.coerceAtLeast(MAX_SHIELDS_CAPACITY)
        if (storedMaxShields < MAX_SHIELDS_CAPACITY) {
            prefs.edit().putInt(KEY_MAX_SHIELDS, MAX_SHIELDS_CAPACITY).apply()
        }
        val savedShields = prefs.getInt(KEY_SHIELDS_COUNT, 3).coerceIn(0, maxShields)
        var savedFlameShields = prefs.getInt(KEY_FLAME_SHIELDS_COUNT, 0).coerceAtLeast(0)
        val goalSeconds = prefs.getInt(KEY_DAILY_GOAL_SECONDS, 300)
        val lastDate = prefs.getString(KEY_LAST_ACTIVE_DATE, null)

        var watchSeconds = prefs.getInt(KEY_DAILY_WATCH_SECONDS, 0)
        var isGoalCompleted = prefs.getBoolean(KEY_IS_GOAL_COMPLETED_TODAY, false)

        // Day transition check: enforce consecutive strike continuity using The Flame Shield ONLY
        // Regular Shields no longer protect the Streak Flame when idle.
        if (lastDate != null && lastDate != today) {
            val daysElapsed = calculateCalendarDaysBetween(lastDate, today)
            var pendingEvent = prefs.getString(KEY_PENDING_FLAME_EVENT, "") ?: ""
            if (daysElapsed != null && daysElapsed > 0 && savedStreak > 0) {
                val missedDays = (daysElapsed - if (isGoalCompleted) 1 else 0).coerceAtLeast(0)
                if (missedDays > 0) {
                    if (savedFlameShields >= missedDays) {
                        savedFlameShields -= missedDays
                        pendingEvent = "protected:$missedDays:$savedFlameShields"
                    } else {
                        // No Flame Shield available to protect from inactivity: flame extinguishes & ice returns
                        savedFlameShields = 0
                        savedStreak = 0
                        pendingEvent = "extinguished:$missedDays"
                    }
                }
            }

            // New day started: reset daily watch progress and daily shield farming quota
            watchSeconds = 0
            isGoalCompleted = false
            prefs.edit()
                .putInt(KEY_STREAK_DAYS, savedStreak)
                .putInt(KEY_FLAME_SHIELDS_COUNT, savedFlameShields)
                .putString(KEY_PENDING_FLAME_EVENT, pendingEvent)
                .putInt(KEY_DAILY_WATCH_SECONDS, 0)
                .putBoolean(KEY_IS_GOAL_COMPLETED_TODAY, false)
                .putString(KEY_LAST_ACTIVE_DATE, today)
                .putString(KEY_SHIELDS_EARNED_TODAY_DATE, today)
                .putInt(KEY_SHIELDS_EARNED_TODAY_COUNT, 0)
                .apply()
        }

        return StreakUiState(
            streakDays = savedStreak,
            shieldsCount = savedShields,
            maxShields = maxShields,
            dailyWatchSeconds = watchSeconds,
            dailyGoalSeconds = goalSeconds,
            isGoalCompletedToday = isGoalCompleted
        )
    }

    /**
     * Immediately commits streak, shield count, watch progress, and goal status to disk.
     */
    fun saveStreakState(state: StreakUiState) {
        val today = getTodayDateString()
        prefs.edit()
            .putBoolean(KEY_FIRST_RUN_DONE, true)
            .putInt(KEY_PREFS_VERSION, CURRENT_PREFS_VERSION)
            .putInt(KEY_STREAK_DAYS, state.streakDays)
            .putInt(KEY_SHIELDS_COUNT, state.shieldsCount)
            .putInt(KEY_MAX_SHIELDS, state.maxShields)
            .putInt(KEY_DAILY_WATCH_SECONDS, state.dailyWatchSeconds)
            .putInt(KEY_DAILY_GOAL_SECONDS, state.dailyGoalSeconds)
            .putBoolean(KEY_IS_GOAL_COMPLETED_TODAY, state.isGoalCompletedToday)
            .putString(KEY_LAST_ACTIVE_DATE, today)
            .commit()
    }

    fun saveThemeMode(themeMode: ThemeMode) {
        prefs.edit()
            .putString(KEY_THEME_MODE, themeMode.name)
            .apply()
    }

    fun loadThemeMode(): ThemeMode {
        val saved = prefs.getString(KEY_THEME_MODE, null)
        return try {
            if (saved != null) ThemeMode.valueOf(saved) else ThemeMode.DARK
        } catch (_: Exception) {
            ThemeMode.DARK
        }
    }

    fun saveStoreTheme(storeTheme: StoreTheme) {
        prefs.edit()
            .putString(KEY_ACTIVE_STORE_THEME, storeTheme.id)
            .apply()
    }

    fun loadStoreTheme(): StoreTheme {
        expireTrialIfNeeded()
        val savedId = prefs.getString(KEY_ACTIVE_STORE_THEME, null)
        return StoreTheme.fromId(savedId)
    }

    fun getPurchasedStoreThemeIds(): Set<String> {
        val saved = prefs.getStringSet(KEY_PURCHASED_STORE_THEMES, emptySet()) ?: emptySet()
        return saved + StoreTheme.DEFAULT.id
    }

    fun isStoreThemePermanentlyOwned(theme: StoreTheme): Boolean {
        return theme.isUnlockedByDefault || getPurchasedStoreThemeIds().contains(theme.id)
    }

    fun isStoreThemeUnlocked(theme: StoreTheme): Boolean {
        return isStoreThemePermanentlyOwned(theme) || isPremiumActivated()
    }

    fun loadLastOwnedStoreTheme(): StoreTheme {
        val savedId = prefs.getString(KEY_LAST_OWNED_STORE_THEME, null)
        val candidate = StoreTheme.fromId(savedId)
        return if (isStoreThemePermanentlyOwned(candidate) || (isPremiumActivated() && isStoreThemeUnlocked(candidate))) {
            candidate
        } else {
            StoreTheme.DEFAULT
        }
    }

    fun equipOwnedStoreTheme(theme: StoreTheme): Boolean {
        if (!isStoreThemeUnlocked(theme)) return false
        val editor = prefs.edit()
            .putString(KEY_ACTIVE_STORE_THEME, theme.id)
            .remove(KEY_TRIAL_THEME_ID)
            .remove(KEY_TRIAL_EXPIRES_AT_MS)
        if (isStoreThemePermanentlyOwned(theme)) {
            editor.putString(KEY_LAST_OWNED_STORE_THEME, theme.id)
        }
        editor.commit()
        return true
    }

    fun purchaseStoreTheme(theme: StoreTheme): Boolean {
        if (isStoreThemeUnlocked(theme)) {
            return equipOwnedStoreTheme(theme)
        }
        val tier = theme.costTier
        val keyCost = theme.costAmount.coerceAtLeast(0)
        val shieldsCost = theme.shieldsCost.coerceAtLeast(0)
        val currentStreak = loadInitialState()
        val forgeState = loadForgeState()

        if (currentStreak.shieldsCount < shieldsCost) return false
        val availableKeys = if (tier != null) forgeState.getKeyCount(tier) else 0
        if (tier != null && availableKeys < keyCost) return false

        if (shieldsCost > 0) {
            val updatedShields = (currentStreak.shieldsCount - shieldsCost).coerceAtLeast(0)
            saveStreakState(currentStreak.copy(shieldsCount = updatedShields))
        }

        val updatedSet = getPurchasedStoreThemeIds().toMutableSet().apply { add(theme.id) }
        val editor = prefs.edit()
            .putStringSet(KEY_PURCHASED_STORE_THEMES, updatedSet)
            .putString(KEY_ACTIVE_STORE_THEME, theme.id)
            .putString(KEY_LAST_OWNED_STORE_THEME, theme.id)
            .remove(KEY_TRIAL_THEME_ID)
            .remove(KEY_TRIAL_EXPIRES_AT_MS)

        if (tier != null && keyCost > 0) {
            val keyPref = when (tier) {
                ForgeKeyTier.BRONZE -> KEY_FORGE_BRONZE_KEYS
                ForgeKeyTier.SILVER -> KEY_FORGE_SILVER_KEYS
                ForgeKeyTier.GOLD -> KEY_FORGE_GOLDEN_KEYS
                ForgeKeyTier.DIAMOND -> KEY_FORGE_DIAMOND_KEYS
            }
            editor.putInt(keyPref, (availableKeys - keyCost).coerceAtLeast(0))
        }

        editor.commit()
        return true
    }

    fun activateTrialTheme(theme: StoreTheme, nowMillis: Long = System.currentTimeMillis()): Long? {
        if (isStoreThemeUnlocked(theme)) {
            equipOwnedStoreTheme(theme)
            return null
        }

        // Preserve current permanently owned theme before starting free 1-minute trial
        val activeBefore = StoreTheme.fromId(prefs.getString(KEY_ACTIVE_STORE_THEME, null))
        val lastOwned = if (isStoreThemeUnlocked(activeBefore)) activeBefore else loadLastOwnedStoreTheme()

        val expiresAt = nowMillis + TRIAL_DURATION_MS
        prefs.edit()
            .putString(KEY_LAST_OWNED_STORE_THEME, lastOwned.id)
            .putString(KEY_TRIAL_THEME_ID, theme.id)
            .putLong(KEY_TRIAL_EXPIRES_AT_MS, expiresAt)
            .putString(KEY_ACTIVE_STORE_THEME, theme.id)
            .commit()
        return expiresAt
    }

    fun getActiveTrialInfo(nowMillis: Long = System.currentTimeMillis()): Pair<StoreTheme, Int>? {
        val trialId = prefs.getString(KEY_TRIAL_THEME_ID, null) ?: return null
        val expiresAt = prefs.getLong(KEY_TRIAL_EXPIRES_AT_MS, 0L)
        if (expiresAt <= nowMillis) return null
        val theme = StoreTheme.fromId(trialId)
        if (isStoreThemeUnlocked(theme)) return null
        val remainingSec = (((expiresAt - nowMillis) + 999L) / 1000L).toInt().coerceIn(1, 60)
        return theme to remainingSec
    }

    fun expireTrialIfNeeded(nowMillis: Long = System.currentTimeMillis()): StoreTheme? {
        val trialId = prefs.getString(KEY_TRIAL_THEME_ID, null) ?: return null
        val expiresAt = prefs.getLong(KEY_TRIAL_EXPIRES_AT_MS, 0L)
        if (nowMillis < expiresAt) return null

        val trialTheme = StoreTheme.fromId(trialId)
        val revertTheme = loadLastOwnedStoreTheme()
        prefs.edit()
            .remove(KEY_TRIAL_THEME_ID)
            .remove(KEY_TRIAL_EXPIRES_AT_MS)
            .putString(KEY_ACTIVE_STORE_THEME, revertTheme.id)
            .commit()

        return if (!isStoreThemeUnlocked(trialTheme)) revertTheme else null
    }

    // --- Store Seekbar Skins (Waveform, Shooting Star, Angry Snake & 5 Liquid Glass Seekbars) ---
    fun getPurchasedSeekbarIds(): Set<String> {
        val stored = prefs.getStringSet(KEY_PURCHASED_SEEKBAR_IDS, emptySet()) ?: emptySet()
        val combined = stored.toMutableSet()
        combined.add(PlayerSeekbarStyle.CLASSIC.id)
        if (prefs.getBoolean(KEY_WAVEFORM_SEEKBAR_PURCHASED, false)) {
            combined.add(PlayerSeekbarStyle.WAVEFORM.id)
        }
        if (prefs.getBoolean(KEY_SHOOTING_STAR_SEEKBAR_PURCHASED, false)) {
            combined.add(PlayerSeekbarStyle.SHOOTING_STAR.id)
        }
        if (prefs.getBoolean(KEY_ANGRY_SNAKE_SEEKBAR_PURCHASED, false)) {
            combined.add(PlayerSeekbarStyle.ANGRY_SNAKE.id)
        }
        return combined
    }

    fun isSeekbarPurchased(style: PlayerSeekbarStyle): Boolean =
        style == PlayerSeekbarStyle.CLASSIC || getPurchasedSeekbarIds().contains(style.id)

    fun isSeekbarUnlocked(style: PlayerSeekbarStyle): Boolean {
        return isSeekbarPurchased(style) || isPremiumActivated()
    }

    fun getActiveSeekbarStyle(): PlayerSeekbarStyle {
        val savedId = prefs.getString(KEY_ACTIVE_SEEKBAR_STYLE, null)
        val candidate = if (savedId != null) {
            PlayerSeekbarStyle.fromId(savedId)
        } else if (prefs.getBoolean(KEY_WAVEFORM_SEEKBAR_ENABLED, false)) {
            PlayerSeekbarStyle.WAVEFORM
        } else {
            PlayerSeekbarStyle.CLASSIC
        }
        return if (isSeekbarUnlocked(candidate)) candidate else PlayerSeekbarStyle.CLASSIC
    }

    fun setActiveSeekbarStyle(style: PlayerSeekbarStyle): Boolean {
        if (!isSeekbarUnlocked(style)) return false
        prefs.edit()
            .putString(KEY_ACTIVE_SEEKBAR_STYLE, style.id)
            .putBoolean(KEY_WAVEFORM_SEEKBAR_ENABLED, style == PlayerSeekbarStyle.WAVEFORM)
            .commit()
        return true
    }

    fun purchaseSeekbarStyle(style: PlayerSeekbarStyle): Boolean {
        if (style == PlayerSeekbarStyle.CLASSIC) {
            return setActiveSeekbarStyle(PlayerSeekbarStyle.CLASSIC)
        }
        if (isSeekbarUnlocked(style)) {
            return setActiveSeekbarStyle(style)
        }
        val costTier = style.costTier
        val costAmount = style.costAmount.coerceAtLeast(0)
        val shieldsCost = style.shieldsCost.coerceAtLeast(0)
        val currentStreak = loadInitialState()
        val forgeState = loadForgeState()

        if (currentStreak.shieldsCount < shieldsCost) return false
        val availableKeys = if (costTier != null) forgeState.getKeyCount(costTier) else 0
        if (costTier != null && availableKeys < costAmount) return false

        if (shieldsCost > 0) {
            val updatedShields = (currentStreak.shieldsCount - shieldsCost).coerceAtLeast(0)
            saveStreakState(currentStreak.copy(shieldsCount = updatedShields))
        }

        val updatedPurchasedIds = getPurchasedSeekbarIds().toMutableSet().apply { add(style.id) }
        val editor = prefs.edit()
            .putStringSet(KEY_PURCHASED_SEEKBAR_IDS, updatedPurchasedIds)
            .putString(KEY_ACTIVE_SEEKBAR_STYLE, style.id)
            .putBoolean(KEY_WAVEFORM_SEEKBAR_ENABLED, style == PlayerSeekbarStyle.WAVEFORM)

        when (style) {
            PlayerSeekbarStyle.WAVEFORM -> editor.putBoolean(KEY_WAVEFORM_SEEKBAR_PURCHASED, true)
            PlayerSeekbarStyle.SHOOTING_STAR -> editor.putBoolean(KEY_SHOOTING_STAR_SEEKBAR_PURCHASED, true)
            PlayerSeekbarStyle.ANGRY_SNAKE -> editor.putBoolean(KEY_ANGRY_SNAKE_SEEKBAR_PURCHASED, true)
            else -> Unit
        }

        if (costTier != null && costAmount > 0) {
            val keyPref = when (costTier) {
                ForgeKeyTier.BRONZE -> KEY_FORGE_BRONZE_KEYS
                ForgeKeyTier.SILVER -> KEY_FORGE_SILVER_KEYS
                ForgeKeyTier.GOLD -> KEY_FORGE_GOLDEN_KEYS
                ForgeKeyTier.DIAMOND -> KEY_FORGE_DIAMOND_KEYS
            }
            editor.putInt(keyPref, (availableKeys - costAmount).coerceAtLeast(0))
        }

        editor.commit()
        return true
    }

    fun isWaveformSeekbarPurchased(): Boolean = isSeekbarPurchased(PlayerSeekbarStyle.WAVEFORM)

    fun isWaveformSeekbarUnlocked(): Boolean = isSeekbarUnlocked(PlayerSeekbarStyle.WAVEFORM)

    fun isWaveformSeekbarEnabled(): Boolean = getActiveSeekbarStyle() == PlayerSeekbarStyle.WAVEFORM

    fun isShootingStarSeekbarPurchased(): Boolean = isSeekbarPurchased(PlayerSeekbarStyle.SHOOTING_STAR)

    fun isShootingStarSeekbarEnabled(): Boolean = getActiveSeekbarStyle() == PlayerSeekbarStyle.SHOOTING_STAR

    fun isAngrySnakeSeekbarPurchased(): Boolean = isSeekbarPurchased(PlayerSeekbarStyle.ANGRY_SNAKE)

    fun isAngrySnakeSeekbarEnabled(): Boolean = getActiveSeekbarStyle() == PlayerSeekbarStyle.ANGRY_SNAKE

    fun purchaseWaveformSeekbar(): Boolean = purchaseSeekbarStyle(PlayerSeekbarStyle.WAVEFORM)

    fun setWaveformSeekbarEnabled(enabled: Boolean): Boolean {
        return if (enabled) {
            setActiveSeekbarStyle(PlayerSeekbarStyle.WAVEFORM)
        } else if (getActiveSeekbarStyle() == PlayerSeekbarStyle.WAVEFORM) {
            setActiveSeekbarStyle(PlayerSeekbarStyle.CLASSIC)
        } else {
            true
        }
    }

    // --- Store Ambient Video Fill Effects (10 Blur & Fade / Black Blur Modes) ---
    fun getPurchasedAmbientModeIds(): Set<String> {
        return prefs.getStringSet(KEY_PURCHASED_AMBIENT_MODES, emptySet()) ?: emptySet()
    }

    fun isAmbientModePurchased(mode: AmbientBlackBlurMode): Boolean {
        if (mode.isUnlockedByDefault) return true
        return getPurchasedAmbientModeIds().contains(mode.name)
    }

    fun isAmbientModeUnlocked(mode: AmbientBlackBlurMode): Boolean {
        return isAmbientModePurchased(mode) || isPremiumActivated()
    }

    fun purchaseAmbientMode(mode: AmbientBlackBlurMode): Boolean {
        if (isAmbientModeUnlocked(mode)) return true
        val costTier = mode.costTier
        val costAmount = mode.costAmount.coerceAtLeast(0)
        val shieldsCost = mode.shieldsCost.coerceAtLeast(0)
        val currentStreak = loadInitialState()
        val forgeState = loadForgeState()

        if (currentStreak.shieldsCount < shieldsCost) return false
        val availableKeys = if (costTier != null) forgeState.getKeyCount(costTier) else 0
        if (costTier != null && availableKeys < costAmount) return false

        if (shieldsCost > 0) {
            val updatedShields = (currentStreak.shieldsCount - shieldsCost).coerceAtLeast(0)
            saveStreakState(currentStreak.copy(shieldsCount = updatedShields))
        }

        val updatedPurchased = getPurchasedAmbientModeIds().toMutableSet().apply {
            add(mode.name)
        }
        val editor = prefs.edit().putStringSet(KEY_PURCHASED_AMBIENT_MODES, updatedPurchased)
        if (costTier != null && costAmount > 0) {
            val keyPref = when (costTier) {
                ForgeKeyTier.BRONZE -> KEY_FORGE_BRONZE_KEYS
                ForgeKeyTier.SILVER -> KEY_FORGE_SILVER_KEYS
                ForgeKeyTier.GOLD -> KEY_FORGE_GOLDEN_KEYS
                ForgeKeyTier.DIAMOND -> KEY_FORGE_DIAMOND_KEYS
            }
            editor.putInt(keyPref, (availableKeys - costAmount).coerceAtLeast(0))
        }
        editor.commit()
        return true
    }

    // --- Store Mimic Items (Single Home Centerpiece Slot: Flame, DNA, or 5 Next-Gen Cores) ---
    fun getPurchasedMimicIds(): Set<String> {
        val stored = prefs.getStringSet(KEY_PURCHASED_MIMIC_IDS, emptySet()) ?: emptySet()
        val combined = stored.toMutableSet()
        combined.add(HomeMimicItem.STREAK_FLAME.id)
        if (prefs.getBoolean(KEY_MIMIC_DNA_PURCHASED, false)) {
            combined.add(HomeMimicItem.DNA_CHROMOSOME.id)
        }
        return combined
    }

    fun isMimicPurchased(item: HomeMimicItem): Boolean {
        if (item.isUnlockedByDefault) return true
        return getPurchasedMimicIds().contains(item.id)
    }

    fun isMimicUnlocked(item: HomeMimicItem): Boolean {
        return isMimicPurchased(item) || isPremiumActivated()
    }

    fun purchaseMimicItem(item: HomeMimicItem): Boolean {
        if (item == HomeMimicItem.DNA_CHROMOSOME) {
            return purchaseMimicDna()
        }
        if (isMimicUnlocked(item)) return true
        val costTier = item.costTier
        val costAmount = item.costAmount.coerceAtLeast(0)
        val shieldsCost = item.shieldsCost.coerceAtLeast(0)
        val currentStreak = loadInitialState()
        val forgeState = loadForgeState()

        if (currentStreak.shieldsCount < shieldsCost) return false
        val availableKeys = if (costTier != null) forgeState.getKeyCount(costTier) else 0
        if (costTier != null && availableKeys < costAmount) return false

        if (shieldsCost > 0) {
            val updatedShields = (currentStreak.shieldsCount - shieldsCost).coerceAtLeast(0)
            saveStreakState(currentStreak.copy(shieldsCount = updatedShields))
        }

        val updatedPurchased = getPurchasedMimicIds().toMutableSet().apply { add(item.id) }
        val editor = prefs.edit()
            .putStringSet(KEY_PURCHASED_MIMIC_IDS, updatedPurchased)
            .putBoolean(KEY_MIMIC_FLAME_ENABLED, false)
            .putBoolean(KEY_MIMIC_DNA_ENABLED, false)

        if (costTier != null && costAmount > 0) {
            val keyPref = when (costTier) {
                ForgeKeyTier.BRONZE -> KEY_FORGE_BRONZE_KEYS
                ForgeKeyTier.SILVER -> KEY_FORGE_SILVER_KEYS
                ForgeKeyTier.GOLD -> KEY_FORGE_GOLDEN_KEYS
                ForgeKeyTier.DIAMOND -> KEY_FORGE_DIAMOND_KEYS
            }
            editor.putInt(keyPref, (availableKeys - costAmount).coerceAtLeast(0))
        }
        editor.commit()
        return true
    }

    fun isMimicFlameEnabled(): Boolean {
        val dnaEnabled = isMimicDnaEnabled()
        if (dnaEnabled) return false
        return prefs.getBoolean(KEY_MIMIC_FLAME_ENABLED, true)
    }

    fun setMimicFlameEnabled(enabled: Boolean): Boolean {
        val editor = prefs.edit().putBoolean(KEY_MIMIC_FLAME_ENABLED, enabled)
        if (enabled) {
            editor.putBoolean(KEY_MIMIC_DNA_ENABLED, false)
        }
        editor.commit()
        return true
    }

    fun isMimicDnaPurchased(): Boolean = prefs.getBoolean(KEY_MIMIC_DNA_PURCHASED, false)

    fun isMimicDnaUnlocked(): Boolean = isMimicDnaPurchased() || isPremiumActivated()

    fun isMimicDnaEnabled(): Boolean = isMimicDnaUnlocked() && prefs.getBoolean(KEY_MIMIC_DNA_ENABLED, false)

    fun setMimicDnaEnabled(enabled: Boolean): Boolean {
        if (enabled && !isMimicDnaUnlocked()) return false
        val editor = prefs.edit().putBoolean(KEY_MIMIC_DNA_ENABLED, enabled)
        if (enabled) {
            editor.putBoolean(KEY_MIMIC_FLAME_ENABLED, false)
        }
        editor.commit()
        return true
    }

    fun purchaseMimicDna(): Boolean {
        if (isMimicDnaUnlocked()) {
            return setMimicDnaEnabled(true)
        }
        val costTier = DNA_MIMIC_COST_TIER
        val costAmount = DNA_MIMIC_COST_AMOUNT
        val shieldsCost = DNA_MIMIC_SHIELDS_COST
        val currentStreak = loadInitialState()
        val forgeState = loadForgeState()
        val availableKeys = forgeState.getKeyCount(costTier)

        if (currentStreak.shieldsCount < shieldsCost || availableKeys < costAmount) return false

        if (shieldsCost > 0) {
            val updatedShields = (currentStreak.shieldsCount - shieldsCost).coerceAtLeast(0)
            saveStreakState(currentStreak.copy(shieldsCount = updatedShields))
        }

        val updatedPurchased = getPurchasedMimicIds().toMutableSet().apply {
            add(HomeMimicItem.DNA_CHROMOSOME.id)
        }
        val keyPref = when (costTier) {
            ForgeKeyTier.BRONZE -> KEY_FORGE_BRONZE_KEYS
            ForgeKeyTier.SILVER -> KEY_FORGE_SILVER_KEYS
            ForgeKeyTier.GOLD -> KEY_FORGE_GOLDEN_KEYS
            ForgeKeyTier.DIAMOND -> KEY_FORGE_DIAMOND_KEYS
        }
        prefs.edit()
            .putInt(keyPref, (availableKeys - costAmount).coerceAtLeast(0))
            .putStringSet(KEY_PURCHASED_MIMIC_IDS, updatedPurchased)
            .putBoolean(KEY_MIMIC_DNA_PURCHASED, true)
            .putBoolean(KEY_MIMIC_DNA_ENABLED, true)
            .putBoolean(KEY_MIMIC_FLAME_ENABLED, false)
            .commit()
        return true
    }

    // --- Store Cyberpunk Avatars & Holographic Aura Frames ---
    fun getPurchasedCyberAvatarIds(): Set<String> {
        return prefs.getStringSet(KEY_PURCHASED_CYBER_AVATARS, emptySet()) ?: emptySet()
    }

    fun isCyberAvatarPurchased(avatar: StoreAvatarItem): Boolean =
        getPurchasedCyberAvatarIds().contains(avatar.id)

    fun isCyberAvatarUnlocked(avatar: StoreAvatarItem): Boolean =
        isCyberAvatarPurchased(avatar) || isPremiumActivated()

    fun purchaseCyberAvatar(avatar: StoreAvatarItem): Boolean {
        if (isCyberAvatarUnlocked(avatar)) return true
        val costTier = avatar.costTier
        val costAmount = avatar.costAmount.coerceAtLeast(0)
        val shieldsCost = avatar.shieldsCost.coerceAtLeast(0)
        val currentStreak = loadInitialState()
        val forgeState = loadForgeState()
        val availableKeys = forgeState.getKeyCount(costTier)

        if (currentStreak.shieldsCount < shieldsCost || availableKeys < costAmount) return false

        if (shieldsCost > 0) {
            val updatedShields = (currentStreak.shieldsCount - shieldsCost).coerceAtLeast(0)
            saveStreakState(currentStreak.copy(shieldsCount = updatedShields))
        }

        val updatedSet = getPurchasedCyberAvatarIds().toMutableSet().apply { add(avatar.id) }
        val keyPref = when (costTier) {
            ForgeKeyTier.BRONZE -> KEY_FORGE_BRONZE_KEYS
            ForgeKeyTier.SILVER -> KEY_FORGE_SILVER_KEYS
            ForgeKeyTier.GOLD -> KEY_FORGE_GOLDEN_KEYS
            ForgeKeyTier.DIAMOND -> KEY_FORGE_DIAMOND_KEYS
        }
        prefs.edit()
            .putStringSet(KEY_PURCHASED_CYBER_AVATARS, updatedSet)
            .putInt(keyPref, (availableKeys - costAmount).coerceAtLeast(0))
            .commit()
        return true
    }

    fun saveLanguage(language: AppLanguage) {
        prefs.edit()
            .putString(KEY_APP_LANGUAGE, language.name)
            .apply()
    }

    fun loadLanguage(): AppLanguage {
        val saved = prefs.getString(KEY_APP_LANGUAGE, null)
        return try {
            if (saved != null) AppLanguage.valueOf(saved) else AppLanguage.ARABIC
        } catch (_: Exception) {
            AppLanguage.ARABIC
        }
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Charfeddine Kahlessenane") ?: "Charfeddine Kahlessenane"
    }

    fun saveUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name.trim().ifBlank { "Charfeddine Kahlessenane" }).commit()
    }

    fun getUserAvatarUri(): String? {
        return prefs.getString(KEY_USER_AVATAR_URI, null)
    }

    fun saveUserAvatarUri(uriString: String?) {
        prefs.edit().putString(KEY_USER_AVATAR_URI, uriString).commit()
    }

    fun getUserAvatarPreset(): Int {
        return prefs.getInt(KEY_USER_AVATAR_PRESET, 0)
    }

    fun saveUserAvatarPreset(presetId: Int) {
        prefs.edit().putInt(KEY_USER_AVATAR_PRESET, presetId).commit()
    }

    fun getWatchViewMode(): String {
        return prefs.getString(KEY_WATCH_VIEW_MODE, "LIST") ?: "LIST"
    }

    fun saveWatchViewMode(modeName: String) {
        prefs.edit().putString(KEY_WATCH_VIEW_MODE, modeName).apply()
    }

    fun getHideFileExtensions(): Boolean {
        return prefs.getBoolean(KEY_HIDE_FILE_EXTENSIONS, true)
    }

    fun saveHideFileExtensions(hide: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_FILE_EXTENSIONS, hide).apply()
    }

    fun saveLastWatched(
        id: String,
        title: String,
        uri: String?,
        durationMs: Long,
        positionMs: Long,
        thumbnailRes: Int = 0
    ) {
        prefs.edit()
            .putString(KEY_LAST_WATCHED_ID, id)
            .putString(KEY_LAST_WATCHED_TITLE, title)
            .putString(KEY_LAST_WATCHED_URI, uri)
            .putLong(KEY_LAST_WATCHED_DURATION_MS, durationMs)
            .putLong(KEY_LAST_WATCHED_POSITION_MS, positionMs)
            .putInt(KEY_LAST_WATCHED_THUMBNAIL_RES, thumbnailRes)
            .putLong(KEY_LAST_WATCHED_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getLastWatched(): LastWatchedVideoInfo? {
        val id = prefs.getString(KEY_LAST_WATCHED_ID, null) ?: return null
        val title = prefs.getString(KEY_LAST_WATCHED_TITLE, "") ?: ""
        val uri = prefs.getString(KEY_LAST_WATCHED_URI, null)
        val durationMs = prefs.getLong(KEY_LAST_WATCHED_DURATION_MS, 0L)
        val positionMs = prefs.getLong(KEY_LAST_WATCHED_POSITION_MS, 0L)
        val thumbRes = prefs.getInt(KEY_LAST_WATCHED_THUMBNAIL_RES, 0)
        val timestamp = prefs.getLong(KEY_LAST_WATCHED_TIMESTAMP, 0L)

        return LastWatchedVideoInfo(
            id = id,
            title = title,
            uriString = uri,
            durationMs = durationMs,
            positionMs = positionMs,
            thumbnailRes = thumbRes,
            timestamp = timestamp
        )
    }

    fun getMyListIds(): Set<String> {
        return prefs.getStringSet(KEY_MY_LIST_MEDIA_IDS, emptySet())?.toSet() ?: emptySet()
    }

    fun toggleMyListId(mediaId: String): Set<String> {
        val current = getMyListIds().toMutableSet()
        if (!current.add(mediaId)) {
            current.remove(mediaId)
        }
        prefs.edit().putStringSet(KEY_MY_LIST_MEDIA_IDS, current).apply()
        return current
    }

    fun getSavedMediaIds(): Set<String> = getMyListIds()

    fun toggleSavedMediaId(mediaId: String): Boolean {
        val current = getMyListIds().toMutableSet()
        val isNowSaved = if (current.contains(mediaId)) {
            current.remove(mediaId)
            false
        } else {
            current.add(mediaId)
            true
        }
        prefs.edit().putStringSet(KEY_MY_LIST_MEDIA_IDS, current).apply()
        return isNowSaved
    }

    fun loadRoadmapXpState(): RoadmapXpState {
        val rawLevel = prefs.getInt(KEY_ROADMAP_UNLOCKED_LEVEL, 0)
            .coerceIn(0, VaultProgressionRegistry.MAX_ROADMAP_LEVEL)
        val rawXp = prefs.getLong(KEY_ROADMAP_CURRENT_LEVEL_XP, 0L).coerceAtLeast(0L)
        val watchAccum = prefs.getInt(KEY_WATCH_XP_SECONDS_ACCUM, 0).coerceIn(0, 59)

        var level = rawLevel
        var levelXp = rawXp
        while (level < VaultProgressionRegistry.MAX_ROADMAP_LEVEL) {
            val req = VaultProgressionRegistry.xpRequiredForLevel(level + 1)
            if (levelXp >= req) {
                levelXp -= req
                level++
            } else {
                break
            }
        }
        if (level >= VaultProgressionRegistry.MAX_ROADMAP_LEVEL) {
            level = VaultProgressionRegistry.MAX_ROADMAP_LEVEL
            levelXp = VaultProgressionRegistry.xpRequiredForLevel(VaultProgressionRegistry.MAX_ROADMAP_LEVEL)
                .coerceAtMost(levelXp.coerceAtLeast(VaultProgressionRegistry.LEVEL_365_XP))
        }
        if (level != rawLevel || levelXp != rawXp) {
            prefs.edit()
                .putInt(KEY_ROADMAP_UNLOCKED_LEVEL, level)
                .putLong(KEY_ROADMAP_CURRENT_LEVEL_XP, levelXp)
                .apply()
        }
        return RoadmapXpState(
            unlockedLevel = level,
            currentLevelXp = levelXp,
            watchXpSecondsAccum = watchAccum
        )
    }

    /**
     * Awards non-cumulative per-level XP.
     * When [currentLevelXp] reaches the target level's independent requirement,
     * that cost is spent, [unlockedLevel] increments, and any leftover XP
     * starts filling the next level independently from 0.
     */
    fun awardRoadmapXp(deltaXp: Long): Pair<Int, RoadmapXpState> {
        val before = loadRoadmapXpState()
        if (deltaXp <= 0L || before.isMaxLevelReached) {
            return before.unlockedLevel to before
        }
        var level = before.unlockedLevel
        var levelXp = before.currentLevelXp + deltaXp
        while (level < VaultProgressionRegistry.MAX_ROADMAP_LEVEL) {
            val req = VaultProgressionRegistry.xpRequiredForLevel(level + 1)
            if (levelXp >= req) {
                levelXp -= req
                level++
            } else {
                break
            }
        }
        if (level >= VaultProgressionRegistry.MAX_ROADMAP_LEVEL) {
            level = VaultProgressionRegistry.MAX_ROADMAP_LEVEL
            levelXp = VaultProgressionRegistry.LEVEL_365_XP
        }
        prefs.edit()
            .putInt(KEY_ROADMAP_UNLOCKED_LEVEL, level)
            .putLong(KEY_ROADMAP_CURRENT_LEVEL_XP, levelXp)
            .commit()
        val updated = RoadmapXpState(
            unlockedLevel = level,
            currentLevelXp = levelXp,
            watchXpSecondsAccum = before.watchXpSecondsAccum
        )
        return before.unlockedLevel to updated
    }

    fun tickDailyWatchAndXp(seconds: Int = 1): DailyWatchTickOutcome {
        val safeSeconds = seconds.coerceAtLeast(0)
        val today = getTodayDateString()
        val current = loadInitialState()
        val updatedSeconds = (current.dailyWatchSeconds + safeSeconds).coerceAtMost(current.dailyGoalSeconds)
        val reachedGoal = updatedSeconds >= current.dailyGoalSeconds
        val newlyCompleted = reachedGoal && !current.isGoalCompletedToday
        val newStreak = if (newlyCompleted) current.streakDays + 1 else current.streakDays

        // CRITICAL: Reaching 5-minute daily watch goal feeds the streak flame ONLY, NOT awarding free shields!
        val newState = current.copy(
            dailyWatchSeconds = updatedSeconds,
            isGoalCompletedToday = reachedGoal || current.isGoalCompletedToday,
            streakDays = newStreak
        )
        saveStreakState(newState)

        // 1. Watch Time XP: 10 XP per full minute (60s) of active video playback
        val prevWatchAccum = prefs.getInt(KEY_WATCH_XP_SECONDS_ACCUM, 0).coerceIn(0, 59)
        val totalWatchAccum = prevWatchAccum + safeSeconds
        val completedMinutes = totalWatchAccum / 60
        val remainingAccum = totalWatchAccum % 60
        val watchXpEarned = completedMinutes.toLong() * VaultProgressionRegistry.WATCH_XP_PER_MINUTE

        // 2. Streak XP: Day 1 = 100 XP, Day 2 = 200 XP, ..., Day 365 = 36,500 XP (capped above 365)
        var streakXpEarned = 0L
        val lastStreakXpDate = prefs.getString(KEY_LAST_STREAK_XP_DATE, "") ?: ""
        val editor = prefs.edit().putInt(KEY_WATCH_XP_SECONDS_ACCUM, remainingAccum)
        if (newlyCompleted && lastStreakXpDate != today) {
            streakXpEarned = VaultProgressionRegistry.streakDayXpReward(newStreak)
            editor.putString(KEY_LAST_STREAK_XP_DATE, today)
        }
        editor.commit()

        val totalXpToAdd = watchXpEarned + streakXpEarned
        val (prevLevel, updatedXp) = if (totalXpToAdd > 0L) {
            awardRoadmapXp(totalXpToAdd)
        } else {
            val loaded = loadRoadmapXpState()
            loaded.unlockedLevel to loaded
        }

        return DailyWatchTickOutcome(
            streakState = newState,
            xpState = updatedXp.copy(watchXpSecondsAccum = remainingAccum),
            newlyCompletedDailyGoal = newlyCompleted,
            streakXpEarned = streakXpEarned,
            watchXpEarned = watchXpEarned,
            previousRoadmapLevel = prevLevel,
            newRoadmapLevel = updatedXp.unlockedLevel
        )
    }

    fun tickDailyWatchTime(seconds: Int = 1): StreakUiState {
        return tickDailyWatchAndXp(seconds).streakState
    }

    fun getShieldsEarnedToday(): Int {
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_SHIELDS_EARNED_TODAY_DATE, "") ?: ""
        return if (savedDate == today) {
            prefs.getInt(KEY_SHIELDS_EARNED_TODAY_COUNT, 0)
        } else {
            0
        }
    }

    /**
     * Ticks active foreground app usage time.
     * Strict Rules:
     * - Background usage does NOT count.
     * - 10 shields awarded per 1 full hour (3600s) of active app usage.
     * - Unlimited daily shield farming.
     * - Maximum inventory capacity: 1000 shields.
     */
    fun tickActiveForegroundUsage(seconds: Int = 1): Pair<StreakUiState, Boolean> {
        val current = loadInitialState()
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_SHIELDS_EARNED_TODAY_DATE, "") ?: ""
        var todayEarned = if (savedDate == today) {
            prefs.getInt(KEY_SHIELDS_EARNED_TODAY_COUNT, 0)
        } else {
            0
        }

        val totalActiveSecs = prefs.getLong(KEY_TOTAL_ACTIVE_WATCH_SECONDS, 0L) + seconds
        var farmSeconds = prefs.getInt(KEY_SHIELD_FARM_SECONDS, 0)
        var newShields = current.shieldsCount
        var earnedShieldNow = false

        if (current.shieldsCount < current.maxShields) {
            farmSeconds += seconds
            if (farmSeconds >= 3600) {
                val completedHours = farmSeconds / 3600
                farmSeconds %= 3600
                val possibleShields = completedHours * SHIELDS_PER_FARM_HOUR
                val remainingCapacity = (current.maxShields - newShields).coerceAtLeast(0)
                val allowable = possibleShields.coerceAtMost(remainingCapacity)
                if (allowable > 0) {
                    todayEarned += allowable
                    newShields = (newShields + allowable).coerceAtMost(current.maxShields)
                    earnedShieldNow = true
                }
            }
        }

        val editor = prefs.edit()
            .putString(KEY_SHIELDS_EARNED_TODAY_DATE, today)
            .putInt(KEY_SHIELDS_EARNED_TODAY_COUNT, todayEarned)
            .putInt(KEY_SHIELD_FARM_SECONDS, farmSeconds)
            .putLong(KEY_TOTAL_ACTIVE_WATCH_SECONDS, totalActiveSecs)

        // Advance any active Forge smelting slots (all tiers progress on active foreground app usage)
        for (tier in ForgeKeyTier.entries) {
            if (prefs.getBoolean(forgeActiveKey(tier), false)) {
                val prevElapsed = prefs.getInt(forgeElapsedKey(tier), 0)
                if (prevElapsed < tier.durationSeconds) {
                    val nextElapsed = (prevElapsed + seconds).coerceAtMost(tier.durationSeconds)
                    editor.putInt(forgeElapsedKey(tier), nextElapsed)
                }
            }
        }
        editor.apply()

        if (newShields != current.shieldsCount) {
            val updatedState = current.copy(shieldsCount = newShields)
            saveStreakState(updatedState)
            return Pair(updatedState, true)
        }
        return Pair(current, false)
    }

    fun getShieldFarmAccumulatedSeconds(): Int {
        return prefs.getInt(KEY_SHIELD_FARM_SECONDS, 0)
    }

    fun getTotalWatchHours(): Float {
        val totalSecs = prefs.getLong(KEY_TOTAL_ACTIVE_WATCH_SECONDS, 7200L) // Default 2.0h offline starter
        return (totalSecs.toFloat() / 3600f)
    }

    fun getTotalTracksListened(): Int {
        return prefs.getInt(KEY_TOTAL_TRACKS_LISTENED, 14)
    }

    fun incrementTrackListened() {
        val current = prefs.getInt(KEY_TOTAL_TRACKS_LISTENED, 14)
        prefs.edit().putInt(KEY_TOTAL_TRACKS_LISTENED, current + 1).apply()
    }

    // --- Hidden Vault (PIN / Biometric) ---
    fun hasCustomVaultPin(): Boolean {
        val hash = prefs.getString(KEY_VAULT_PIN_HASH, null)
        val salt = prefs.getString(KEY_VAULT_PIN_SALT, null)
        if (!hash.isNullOrBlank() && !salt.isNullOrBlank()) return true
        val legacy = prefs.getString(KEY_VAULT_PIN, null)
        return !legacy.isNullOrBlank()
    }

    fun verifyVaultPin(pin: String): Boolean {
        if (pin.isBlank()) return false
        val savedHash = prefs.getString(KEY_VAULT_PIN_HASH, null)
        val savedSalt = prefs.getString(KEY_VAULT_PIN_SALT, null)
        if (!savedHash.isNullOrBlank() && !savedSalt.isNullOrBlank()) {
            return SecurityUtils.verifyVaultPinHash(pin, savedSalt, savedHash)
        }
        val legacyPin = prefs.getString(KEY_VAULT_PIN, null)
        if (!legacyPin.isNullOrBlank()) {
            val matched = legacyPin == pin
            if (matched) {
                saveVaultPin(legacyPin)
            }
            return matched
        }
        val defaultHash = SecurityUtils.hashDefaultVaultPin("1234")
        return SecurityUtils.verifyVaultPinHash(pin, SecurityUtils.defaultVaultSalt(), defaultHash)
    }

    fun saveVaultPin(pin: String): Boolean {
        val trimmed = pin.trim()
        if (trimmed.length != 4 || !trimmed.all { it.isDigit() }) return false
        val salt = SecurityUtils.generateSaltHex()
        val digest = SecurityUtils.hashVaultPin(trimmed, salt)
        prefs.edit()
            .putString(KEY_VAULT_PIN_SALT, salt)
            .putString(KEY_VAULT_PIN_HASH, digest)
            .remove(KEY_VAULT_PIN)
            .apply()
        return true
    }

    fun getVaultMediaIds(): Set<String> {
        return prefs.getStringSet(KEY_VAULT_MEDIA_IDS, setOf("vault_video_1")) ?: setOf("vault_video_1")
    }

    fun toggleVaultMedia(mediaId: String): Boolean {
        val current = getVaultMediaIds().toMutableSet()
        val isNowInVault = if (current.contains(mediaId)) {
            current.remove(mediaId)
            false
        } else {
            current.add(mediaId)
            true
        }
        prefs.edit().putStringSet(KEY_VAULT_MEDIA_IDS, current).apply()
        return isNowInVault
    }

    // --- System Settings Hub ---
    fun getRgbAccentColor(): Long {
        // Defaults to Neon Cyan: 0xFF00FFCC
        return prefs.getLong(KEY_RGB_ACCENT, 0xFF00FFCCL)
    }

    fun saveRgbAccentColor(colorLong: Long) {
        prefs.edit().putLong(KEY_RGB_ACCENT, colorLong).apply()
    }

    fun isOledBlackEnabled(): Boolean {
        return prefs.getBoolean(KEY_OLED_BLACK, false)
    }

    fun saveOledBlackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OLED_BLACK, enabled).apply()
    }

    fun getAutoResumePlayback(): Boolean {
        return prefs.getBoolean(KEY_AUTO_RESUME, true)
    }

    fun saveAutoResumePlayback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_RESUME, enabled).apply()
    }

    fun getDefaultPlaybackSpeed(): Float {
        return prefs.getFloat(KEY_DEFAULT_SPEED, 1.0f)
    }

    fun saveDefaultPlaybackSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_DEFAULT_SPEED, speed).apply()
    }

    fun getBlacklistedFolders(): Set<String> {
        return prefs.getStringSet(KEY_BLACKLISTED_FOLDERS, emptySet()) ?: emptySet()
    }

    fun toggleFolderBlacklist(folderName: String): Boolean {
        val current = getBlacklistedFolders().toMutableSet()
        val isBlacklisted = if (current.contains(folderName)) {
            current.remove(folderName)
            false
        } else {
            current.add(folderName)
            true
        }
        prefs.edit().putStringSet(KEY_BLACKLISTED_FOLDERS, current).apply()
        return isBlacklisted
    }

    fun clearThumbnailCache(): Long {
        return com.example.data.util.LocalMediaScanner.clearThumbnailCaches(appContext)
    }

    // --- Aura Pulse Premium & Moved System Settings ---
    fun isPremiumActivated(): Boolean = prefs.getBoolean(KEY_PREMIUM_ACTIVATED, false)
    fun setPremiumActivated(activated: Boolean) {
        val editor = prefs.edit().putBoolean(KEY_PREMIUM_ACTIVATED, activated)
        if (activated) {
            editor.remove(KEY_TRIAL_THEME_ID).remove(KEY_TRIAL_EXPIRES_AT_MS)
        }
        editor.apply()
    }

    fun getSelectedThemePresetIndex(): Int = prefs.getInt(KEY_SELECTED_THEME_PRESET, 0)
    fun saveSelectedThemePresetIndex(index: Int) {
        prefs.edit().putInt(KEY_SELECTED_THEME_PRESET, index).apply()
    }

    fun isWifiDirectSharingEnabled(): Boolean = prefs.getBoolean(KEY_WIFI_DIRECT_SHARING, true)
    fun setWifiDirectSharingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIFI_DIRECT_SHARING, enabled).apply()
    }

    fun isSmartNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_SMART_NOTIFICATIONS, true)
    fun setSmartNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SMART_NOTIFICATIONS, enabled).apply()
    }

    fun isBackgroundPlayEnabled(): Boolean = prefs.getBoolean(KEY_BACKGROUND_PLAY, true)
    fun setBackgroundPlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BACKGROUND_PLAY, enabled).apply()
    }

    fun isPipModeEnabled(): Boolean = prefs.getBoolean(KEY_PIP_MODE, true)
    fun setPipModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PIP_MODE, enabled).apply()
    }

    fun isEyeProtectionEnabled(): Boolean = prefs.getBoolean(KEY_EYE_PROTECTION, false)
    fun setEyeProtectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EYE_PROTECTION, enabled).apply()
    }

    fun isFullScreenModeEnabled(): Boolean = prefs.getBoolean(KEY_FULL_SCREEN_MODE, false)
    fun setFullScreenModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FULL_SCREEN_MODE, enabled).apply()
    }

    fun getLastStatusBarHeightDp(): Float = prefs.getFloat(KEY_LAST_STATUS_BAR_HEIGHT_DP, 0f)
    fun saveLastStatusBarHeightDp(heightDp: Float) {
        if (heightDp > 0f) {
            prefs.edit().putFloat(KEY_LAST_STATUS_BAR_HEIGHT_DP, heightDp).apply()
        }
    }

    fun exportPreferencesToJson(): String {
        val state = loadInitialState()
        val xpState = loadRoadmapXpState()
        return """
            {
              "app": "Aura Pulse / Video Pulse",
              "exportedAt": "${System.currentTimeMillis()}",
              "streakDays": ${state.streakDays},
              "shieldsCount": ${state.shieldsCount},
              "flameShieldsCount": ${loadForgeState().flameShieldsCount},
              "dailyWatchSeconds": ${state.dailyWatchSeconds},
              "roadmapUnlockedLevel": ${xpState.unlockedLevel},
              "roadmapCurrentLevelXp": ${xpState.currentLevelXp},
              "userName": "${getUserName()}",
              "totalWatchHours": ${getTotalWatchHours()},
              "rgbAccent": ${getRgbAccentColor()},
              "oledBlack": ${isOledBlackEnabled()},
              "fullScreenMode": ${isFullScreenModeEnabled()},
              "autoResume": ${getAutoResumePlayback()},
              "defaultSpeed": ${getDefaultPlaybackSpeed()}
            }
        """.trimIndent()
    }

    fun importPreferencesFromJson(json: String): Boolean {
        return try {
            val jsonObject = org.json.JSONObject(json)
            val streak = jsonObject.optInt("streakDays", 0)
            val shields = jsonObject.optInt("shieldsCount", 3).coerceIn(0, MAX_SHIELDS_CAPACITY)
            val flameShields = jsonObject.optInt("flameShieldsCount", 0).coerceAtLeast(0)
            val roadmapLvl = jsonObject.optInt("roadmapUnlockedLevel", 0)
                .coerceIn(0, VaultProgressionRegistry.MAX_ROADMAP_LEVEL)
            val roadmapXp = jsonObject.optLong("roadmapCurrentLevelXp", 0L).coerceAtLeast(0L)
            val name = jsonObject.optString("userName", "Charfeddine Kahlessenane")
            val rgb = jsonObject.optLong("rgbAccent", 0xFF00FFCCL)
            val oled = jsonObject.optBoolean("oledBlack", false)
            val fullScreen = jsonObject.optBoolean("fullScreenMode", false)

            prefs.edit()
                .putInt(KEY_STREAK_DAYS, streak)
                .putInt(KEY_SHIELDS_COUNT, shields)
                .putInt(KEY_FLAME_SHIELDS_COUNT, flameShields)
                .putInt(KEY_MAX_SHIELDS, MAX_SHIELDS_CAPACITY)
                .putInt(KEY_ROADMAP_UNLOCKED_LEVEL, roadmapLvl)
                .putLong(KEY_ROADMAP_CURRENT_LEVEL_XP, roadmapXp)
                .putString(KEY_USER_NAME, name)
                .putLong(KEY_RGB_ACCENT, rgb)
                .putBoolean(KEY_OLED_BLACK, oled)
                .putBoolean(KEY_FULL_SCREEN_MODE, fullScreen)
                .apply()
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun videoStorageKey(id: String, uriString: String?): String {
        val raw = if (!uriString.isNullOrBlank()) uriString else id
        return "vp_" + raw.hashCode()
    }

    fun saveVideoProgress(
        id: String,
        uri: String?,
        title: String,
        durationMs: Long,
        positionMs: Long,
        thumbnailRes: Int = 0
    ) {
        if (durationMs <= 0L) return
        val key = videoStorageKey(id, uri)
        prefs.edit()
            .putString("key_vid_id_$key", id)
            .putString("key_vid_uri_$key", uri)
            .putString("key_vid_title_$key", title)
            .putLong("key_vid_dur_$key", durationMs)
            .putLong("key_vid_pos_$key", positionMs)
            .putInt("key_vid_thumb_$key", thumbnailRes)
            .putLong("key_vid_time_$key", System.currentTimeMillis())
            .apply()

        // Also update last watched
        saveLastWatched(id, title, uri, durationMs, positionMs, thumbnailRes)
    }

    fun getVideoProgress(id: String, uriString: String? = null): VideoProgressInfo? {
        val key = videoStorageKey(id, uriString)
        val savedPos = prefs.getLong("key_vid_pos_$key", -1L)
        if (savedPos < 0L) {
            val lw = getLastWatched()
            if (lw != null && (lw.id == id || (uriString != null && lw.uriString == uriString))) {
                return VideoProgressInfo(
                    id = lw.id,
                    uriString = lw.uriString,
                    title = lw.title,
                    durationMs = lw.durationMs,
                    positionMs = lw.positionMs,
                    thumbnailRes = lw.thumbnailRes,
                    timestamp = lw.timestamp
                )
            }
            return null
        }

        val savedDur = prefs.getLong("key_vid_dur_$key", 0L)
        val title = prefs.getString("key_vid_title_$key", "") ?: ""
        val uri = prefs.getString("key_vid_uri_$key", uriString)
        val thumb = prefs.getInt("key_vid_thumb_$key", 0)
        val time = prefs.getLong("key_vid_time_$key", 0L)

        return VideoProgressInfo(
            id = id,
            uriString = uri,
            title = title,
            durationMs = savedDur,
            positionMs = savedPos,
            thumbnailRes = thumb,
            timestamp = time
        )
    }

    fun clearVideoProgress(id: String, uriString: String? = null) {
        val key = videoStorageKey(id, uriString)
        prefs.edit()
            .remove("key_vid_id_$key")
            .remove("key_vid_uri_$key")
            .remove("key_vid_title_$key")
            .remove("key_vid_dur_$key")
            .remove("key_vid_pos_$key")
            .remove("key_vid_thumb_$key")
            .remove("key_vid_time_$key")
            .apply()
    }

    fun canonicalVideoKey(id: String, uriString: String?): String {
        if (!uriString.isNullOrBlank()) return "uri:$uriString"
        if (id.startsWith("device_vid_")) {
            val parts = id.removePrefix("device_vid_").split("_")
            if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                return "ms_id:${parts[0]}"
            }
        }
        return "id:$id"
    }

    fun getOpenedVideoKeys(): Set<String> {
        return prefs.getStringSet(KEY_OPENED_VIDEO_KEYS, emptySet())?.toSet() ?: emptySet()
    }

    fun markVideoOpened(id: String, uriString: String?): Set<String> {
        val key = canonicalVideoKey(id, uriString)
        val current = getOpenedVideoKeys()
        if (current.contains(key)) return current
        val updated = current + key
        prefs.edit().putStringSet(KEY_OPENED_VIDEO_KEYS, updated).apply()
        return updated
    }

    private fun readOrderedStringList(prefKey: String): List<String> {
        val raw = prefs.getString(prefKey, null) ?: return emptyList()
        return try {
            val arr = org.json.JSONArray(raw)
            buildList(arr.length()) {
                for (i in 0 until arr.length()) {
                    val value = arr.optString(i)
                    if (value.isNotBlank() && !contains(value)) add(value)
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveOrderedStringList(prefKey: String, items: List<String>) {
        val arr = org.json.JSONArray()
        items.forEach { arr.put(it) }
        prefs.edit().putString(prefKey, arr.toString()).apply()
    }

    fun getPinnedVideoKeys(): List<String> = readOrderedStringList(KEY_PINNED_VIDEO_KEYS)

    fun togglePinnedVideo(id: String, uriString: String?): List<String> {
        val key = canonicalVideoKey(id, uriString)
        val current = getPinnedVideoKeys().toMutableList()
        if (current.contains(key)) {
            current.removeAll { it == key }
        } else {
            current.add(0, key)
        }
        saveOrderedStringList(KEY_PINNED_VIDEO_KEYS, current)
        return current
    }

    fun removePinnedVideo(id: String, uriString: String?): List<String> {
        val key = canonicalVideoKey(id, uriString)
        val current = getPinnedVideoKeys()
        if (!current.contains(key)) return current
        val updated = current.filterNot { it == key }
        saveOrderedStringList(KEY_PINNED_VIDEO_KEYS, updated)
        return updated
    }

    fun migratePinnedVideoKey(oldId: String, oldUri: String?, newId: String, newUri: String?): List<String> {
        val oldKey = canonicalVideoKey(oldId, oldUri)
        val newKey = canonicalVideoKey(newId, newUri)
        val current = getPinnedVideoKeys()
        if (oldKey == newKey || !current.contains(oldKey)) return current
        val updated = current.map { if (it == oldKey) newKey else it }.distinct()
        saveOrderedStringList(KEY_PINNED_VIDEO_KEYS, updated)
        return updated
    }

    fun canonicalFolderPinKey(folderName: String): String = folderName.trim().lowercase()

    fun getPinnedFolderNames(): List<String> = readOrderedStringList(KEY_PINNED_FOLDER_NAMES)

    fun togglePinnedFolder(folderName: String): List<String> {
        val key = canonicalFolderPinKey(folderName)
        if (key.isBlank()) return getPinnedFolderNames()
        val current = getPinnedFolderNames().toMutableList()
        if (current.contains(key)) {
            current.removeAll { it == key }
        } else {
            current.add(0, key)
        }
        saveOrderedStringList(KEY_PINNED_FOLDER_NAMES, current)
        return current
    }

    fun removePinnedFolder(folderName: String): List<String> {
        val key = canonicalFolderPinKey(folderName)
        val current = getPinnedFolderNames()
        if (!current.contains(key)) return current
        val updated = current.filterNot { it == key }
        saveOrderedStringList(KEY_PINNED_FOLDER_NAMES, updated)
        return updated
    }

    fun renamePinnedFolder(oldFolderName: String, newFolderName: String): List<String> {
        val oldKey = canonicalFolderPinKey(oldFolderName)
        val newKey = canonicalFolderPinKey(newFolderName)
        val current = getPinnedFolderNames()
        if (oldKey == newKey || newKey.isBlank() || !current.contains(oldKey)) return current
        val updated = current.map { if (it == oldKey) newKey else it }.distinct()
        saveOrderedStringList(KEY_PINNED_FOLDER_NAMES, updated)
        return updated
    }

    fun getLikedShortKeys(): Set<String> = getShortLikedVideoKeys()

    fun getSavedShortKeys(): Set<String> = getShortSavedVideoKeys().toSet()

    fun toggleShortLike(videoKey: String): Set<String> {
        val current = getShortLikedVideoKeys()
        val updated = if (current.contains(videoKey)) current - videoKey else current + videoKey
        prefs.edit().putStringSet(KEY_SHORT_LIKED_VIDEO_KEYS, updated).apply()
        return updated
    }

    fun setShortLiked(videoKey: String, liked: Boolean): Set<String> {
        val current = getShortLikedVideoKeys()
        val updated = if (liked) current + videoKey else current - videoKey
        prefs.edit().putStringSet(KEY_SHORT_LIKED_VIDEO_KEYS, updated).apply()
        return updated
    }

    fun toggleShortSave(videoKey: String): Set<String> {
        val current = getShortSavedVideoKeys().toMutableList()
        if (current.contains(videoKey)) {
            current.removeAll { it == videoKey }
        } else {
            current.add(0, videoKey)
        }
        saveOrderedStringList(KEY_SHORT_SAVED_VIDEO_KEYS, current)
        return current.toSet()
    }

    fun getShortLikedVideoKeys(): Set<String> {
        return prefs.getStringSet(KEY_SHORT_LIKED_VIDEO_KEYS, emptySet())?.toSet() ?: emptySet()
    }

    fun setShortLiked(id: String, uriString: String?, liked: Boolean): Set<String> {
        val key = canonicalVideoKey(id, uriString)
        val current = getShortLikedVideoKeys()
        val updated = if (liked) current + key else current - key
        prefs.edit().putStringSet(KEY_SHORT_LIKED_VIDEO_KEYS, updated).apply()
        return updated
    }

    fun toggleShortLike(id: String, uriString: String?): Set<String> {
        val key = canonicalVideoKey(id, uriString)
        val current = getShortLikedVideoKeys()
        val updated = if (current.contains(key)) current - key else current + key
        prefs.edit().putStringSet(KEY_SHORT_LIKED_VIDEO_KEYS, updated).apply()
        return updated
    }

    fun getShortSavedVideoKeys(): List<String> = readOrderedStringList(KEY_SHORT_SAVED_VIDEO_KEYS)

    fun toggleShortSaved(id: String, uriString: String?): List<String> {
        val key = canonicalVideoKey(id, uriString)
        val current = getShortSavedVideoKeys().toMutableList()
        if (current.contains(key)) {
            current.removeAll { it == key }
        } else {
            current.add(0, key)
        }
        saveOrderedStringList(KEY_SHORT_SAVED_VIDEO_KEYS, current)
        return current
    }

    fun getWatchHistory(): List<VideoProgressInfo> {
        val items = mutableListOf<VideoProgressInfo>()
        val seenKeys = mutableSetOf<String>()
        val allKeys = prefs.all.keys
        for (prefKey in allKeys) {
            if (!prefKey.startsWith("key_vid_id_")) continue
            val storageKey = prefKey.removePrefix("key_vid_id_")
            val id = prefs.getString("key_vid_id_$storageKey", null) ?: continue
            val pos = prefs.getLong("key_vid_pos_$storageKey", -1L)
            val dur = prefs.getLong("key_vid_dur_$storageKey", 0L)
            if (pos < 0L || dur <= 0L) continue
            val uri = prefs.getString("key_vid_uri_$storageKey", null)
            val title = prefs.getString("key_vid_title_$storageKey", "") ?: ""
            val thumb = prefs.getInt("key_vid_thumb_$storageKey", 0)
            val time = prefs.getLong("key_vid_time_$storageKey", 0L)
            seenKeys.add(videoStorageKey(id, uri))
            items.add(
                VideoProgressInfo(
                    id = id,
                    uriString = uri,
                    title = title,
                    durationMs = dur,
                    positionMs = pos,
                    thumbnailRes = thumb,
                    timestamp = time
                )
            )
        }
        val lastWatched = getLastWatched()
        if (lastWatched != null && lastWatched.durationMs > 0L) {
            val lwKey = videoStorageKey(lastWatched.id, lastWatched.uriString)
            if (seenKeys.add(lwKey)) {
                items.add(
                    VideoProgressInfo(
                        id = lastWatched.id,
                        uriString = lastWatched.uriString,
                        title = lastWatched.title,
                        durationMs = lastWatched.durationMs,
                        positionMs = lastWatched.positionMs,
                        thumbnailRes = lastWatched.thumbnailRes,
                        timestamp = lastWatched.timestamp
                    )
                )
            }
        }
        return items.sortedByDescending { it.timestamp }
    }

    fun removeWatchHistoryItem(id: String, uriString: String? = null) {
        clearVideoProgress(id, uriString)
        val lastWatched = getLastWatched()
        if (lastWatched != null &&
            (lastWatched.id == id || (!uriString.isNullOrBlank() && lastWatched.uriString == uriString))
        ) {
            prefs.edit()
                .remove(KEY_LAST_WATCHED_ID)
                .remove(KEY_LAST_WATCHED_TITLE)
                .remove(KEY_LAST_WATCHED_URI)
                .remove(KEY_LAST_WATCHED_DURATION_MS)
                .remove(KEY_LAST_WATCHED_POSITION_MS)
                .remove(KEY_LAST_WATCHED_THUMBNAIL_RES)
                .remove(KEY_LAST_WATCHED_TIMESTAMP)
                .apply()
        }
    }

    fun clearWatchHistory() {
        val editor = prefs.edit()
        for (prefKey in prefs.all.keys) {
            if (prefKey.startsWith("key_vid_")) {
                editor.remove(prefKey)
            }
        }
        editor
            .remove(KEY_LAST_WATCHED_ID)
            .remove(KEY_LAST_WATCHED_TITLE)
            .remove(KEY_LAST_WATCHED_URI)
            .remove(KEY_LAST_WATCHED_DURATION_MS)
            .remove(KEY_LAST_WATCHED_POSITION_MS)
            .remove(KEY_LAST_WATCHED_THUMBNAIL_RES)
            .remove(KEY_LAST_WATCHED_TIMESTAMP)
            .apply()
    }

    fun getClaimedVaultLevels(): Set<Int> {
        val raw = prefs.getStringSet(KEY_CLAIMED_VAULT_LEVELS, emptySet()) ?: emptySet()
        return raw.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun claimVaultLevelReward(level: Int, shieldsToAward: Int): Boolean {
        val claimed = getClaimedVaultLevels().toMutableSet()
        if (claimed.contains(level)) return false
        claimed.add(level)

        val current = loadInitialState()
        val newShields = (current.shieldsCount + shieldsToAward).coerceAtMost(current.maxShields)

        prefs.edit()
            .putStringSet(KEY_CLAIMED_VAULT_LEVELS, claimed.map { it.toString() }.toSet())
            .putInt(KEY_SHIELDS_COUNT, newShields)
            .apply()

        saveStreakState(current.copy(shieldsCount = newShields))
        return true
    }

    fun claimAllUnlockedVaultRewards(levelsWithRewards: List<Pair<Int, Int>>): Int {
        val claimed = getClaimedVaultLevels().toMutableSet()
        var newlyClaimed = 0
        var totalShields = 0
        for ((level, reward) in levelsWithRewards) {
            if (!claimed.contains(level)) {
                claimed.add(level)
                newlyClaimed++
                totalShields += reward
            }
        }
        if (newlyClaimed == 0) return 0

        val current = loadInitialState()
        val newShields = (current.shieldsCount + totalShields).coerceAtMost(current.maxShields)

        prefs.edit()
            .putStringSet(KEY_CLAIMED_VAULT_LEVELS, claimed.map { it.toString() }.toSet())
            .putInt(KEY_SHIELDS_COUNT, newShields)
            .apply()

        saveStreakState(current.copy(shieldsCount = newShields))
        return newlyClaimed
    }

    // --- The Forge Persistence ---
    private fun forgeActiveKey(tier: ForgeKeyTier) = "key_forge_active_${tier.id}"
    private fun forgeElapsedKey(tier: ForgeKeyTier) = "key_forge_elapsed_${tier.id}"
    private fun forgeRefundShieldsKey(tier: ForgeKeyTier) = "key_forge_refund_shields_${tier.id}"
    private fun forgeRefundBronzeKey(tier: ForgeKeyTier) = "key_forge_refund_bronze_${tier.id}"
    private fun forgeRefundSilverKey(tier: ForgeKeyTier) = "key_forge_refund_silver_${tier.id}"
    private fun forgeRefundGoldKey(tier: ForgeKeyTier) = "key_forge_refund_gold_${tier.id}"

    private data class FlameForgePrefKeys(
        val active: String,
        val option: String,
        val startedAtMs: String,
        val refundShields: String,
        val refundBronze: String,
        val refundSilver: String,
        val refundDiamond: String
    )

    private fun flameForgePrefKeys(slotIndex: Int): FlameForgePrefKeys = if (slotIndex == 1) {
        FlameForgePrefKeys(
            active = KEY_FLAME_FORGE_2_ACTIVE,
            option = KEY_FLAME_FORGE_2_OPTION,
            startedAtMs = KEY_FLAME_FORGE_2_STARTED_AT_MS,
            refundShields = KEY_FLAME_FORGE_2_REFUND_SHIELDS,
            refundBronze = KEY_FLAME_FORGE_2_REFUND_BRONZE,
            refundSilver = KEY_FLAME_FORGE_2_REFUND_SILVER,
            refundDiamond = KEY_FLAME_FORGE_2_REFUND_DIAMOND
        )
    } else {
        FlameForgePrefKeys(
            active = KEY_FLAME_FORGE_ACTIVE,
            option = KEY_FLAME_FORGE_OPTION,
            startedAtMs = KEY_FLAME_FORGE_STARTED_AT_MS,
            refundShields = KEY_FLAME_FORGE_REFUND_SHIELDS,
            refundBronze = KEY_FLAME_FORGE_REFUND_BRONZE,
            refundSilver = KEY_FLAME_FORGE_REFUND_SILVER,
            refundDiamond = KEY_FLAME_FORGE_REFUND_DIAMOND
        )
    }

    private fun loadFlameShieldSlot(slotIndex: Int): FlameShieldForgeSlotState {
        val keys = flameForgePrefKeys(slotIndex)
        val flameOptionId = prefs.getString(keys.option, null)
        return FlameShieldForgeSlotState(
            slotIndex = slotIndex,
            isActive = prefs.getBoolean(keys.active, false),
            selectedOption = FlameShieldCraftOption.entries.firstOrNull { it.id == flameOptionId },
            startedAtEpochMs = prefs.getLong(keys.startedAtMs, 0L).coerceAtLeast(0L),
            durationMs = FLAME_SHIELD_CRAFT_DURATION_MS,
            refundShields = prefs.getInt(keys.refundShields, 0).coerceAtLeast(0),
            refundBronzeKeys = prefs.getInt(keys.refundBronze, 0).coerceAtLeast(0),
            refundSilverKeys = prefs.getInt(keys.refundSilver, 0).coerceAtLeast(0),
            refundDiamondKeys = prefs.getInt(keys.refundDiamond, 0).coerceAtLeast(0)
        )
    }

    fun loadForgeState(): ForgeUiState {
        val bronze = prefs.getInt(KEY_FORGE_BRONZE_KEYS, 0).coerceAtLeast(0)
        val silver = prefs.getInt(KEY_FORGE_SILVER_KEYS, 0).coerceAtLeast(0)
        val gold = prefs.getInt(KEY_FORGE_GOLDEN_KEYS, 0).coerceAtLeast(0)
        val diamond = prefs.getInt(KEY_FORGE_DIAMOND_KEYS, 0).coerceAtLeast(0)
        val flameShields = prefs.getInt(KEY_FLAME_SHIELDS_COUNT, 0).coerceAtLeast(0)
        val slots = ForgeKeyTier.entries.associateWith { tier ->
            ForgeSlotState(
                tier = tier,
                isActive = prefs.getBoolean(forgeActiveKey(tier), false),
                elapsedSeconds = prefs.getInt(forgeElapsedKey(tier), 0).coerceIn(0, tier.durationSeconds),
                refundShields = prefs.getInt(forgeRefundShieldsKey(tier), 0).coerceAtLeast(0),
                refundBronzeKeys = prefs.getInt(forgeRefundBronzeKey(tier), 0).coerceAtLeast(0),
                refundSilverKeys = prefs.getInt(forgeRefundSilverKey(tier), 0).coerceAtLeast(0),
                refundGoldKeys = prefs.getInt(forgeRefundGoldKey(tier), 0).coerceAtLeast(0)
            )
        }
        val flameSlot0 = loadFlameShieldSlot(0)
        val flameSlot1 = loadFlameShieldSlot(1)
        return ForgeUiState(
            bronzeKeys = bronze,
            silverKeys = silver,
            goldenKeys = gold,
            diamondKeys = diamond,
            flameShieldsCount = flameShields,
            flameShieldSlot = flameSlot0,
            flameShieldSlots = listOf(flameSlot0, flameSlot1),
            slots = slots
        )
    }

    fun consumePendingFlameEvent(): String {
        val event = prefs.getString(KEY_PENDING_FLAME_EVENT, "") ?: ""
        if (event.isNotEmpty()) {
            prefs.edit().putString(KEY_PENDING_FLAME_EVENT, "").apply()
        }
        return event
    }

    fun craftFlameShield(
        option: FlameShieldCraftOption,
        slotIndex: Int = 0,
        nowEpochMs: Long = System.currentTimeMillis()
    ): Boolean {
        if (slotIndex !in 0 until FLAME_SHIELD_LABS_COUNT) return false
        val currentStreak = loadInitialState()
        val currentForge = loadForgeState()
        if (currentForge.getFlameShieldSlot(slotIndex).isActive) return false
        if (!option.canAfford(currentStreak.shieldsCount, currentForge)) return false

        val keys = flameForgePrefKeys(slotIndex)
        val editor = prefs.edit()
        var refundShields = 0
        var refundBronze = 0
        var refundSilver = 0
        var refundDiamond = 0

        when (option) {
            FlameShieldCraftOption.SHIELDS_100 -> {
                val nextShields = (currentStreak.shieldsCount - option.shieldsCost).coerceAtLeast(0)
                saveStreakState(currentStreak.copy(shieldsCount = nextShields))
                refundShields = option.shieldsCost
            }
            FlameShieldCraftOption.BRONZE_3 -> {
                editor.putInt(KEY_FORGE_BRONZE_KEYS, (currentForge.bronzeKeys - option.bronzeKeysCost).coerceAtLeast(0))
                refundBronze = option.bronzeKeysCost
            }
            FlameShieldCraftOption.SILVER_2 -> {
                editor.putInt(KEY_FORGE_SILVER_KEYS, (currentForge.silverKeys - option.silverKeysCost).coerceAtLeast(0))
                refundSilver = option.silverKeysCost
            }
            FlameShieldCraftOption.DIAMOND_1 -> {
                editor.putInt(KEY_FORGE_DIAMOND_KEYS, (currentForge.diamondKeys - option.diamondKeysCost).coerceAtLeast(0))
                refundDiamond = option.diamondKeysCost
            }
        }

        editor
            .putBoolean(keys.active, true)
            .putString(keys.option, option.id)
            .putLong(keys.startedAtMs, nowEpochMs)
            .putInt(keys.refundShields, refundShields)
            .putInt(keys.refundBronze, refundBronze)
            .putInt(keys.refundSilver, refundSilver)
            .putInt(keys.refundDiamond, refundDiamond)
            .commit()
        return true
    }

    fun claimFlameShieldCraft(
        slotIndex: Int = 0,
        nowEpochMs: Long = System.currentTimeMillis()
    ): Boolean {
        if (slotIndex !in 0 until FLAME_SHIELD_LABS_COUNT) return false
        val currentForge = loadForgeState()
        val slot = currentForge.getFlameShieldSlot(slotIndex)
        if (!slot.isReadyToClaim(nowEpochMs)) return false

        val keys = flameForgePrefKeys(slotIndex)
        val nextFlameShields = currentForge.flameShieldsCount + 1
        prefs.edit()
            .putInt(KEY_FLAME_SHIELDS_COUNT, nextFlameShields)
            .putBoolean(keys.active, false)
            .remove(keys.option)
            .putLong(keys.startedAtMs, 0L)
            .putInt(keys.refundShields, 0)
            .putInt(keys.refundBronze, 0)
            .putInt(keys.refundSilver, 0)
            .putInt(keys.refundDiamond, 0)
            .commit()
        return true
    }

    fun cancelFlameShieldCraft(
        slotIndex: Int = 0,
        nowEpochMs: Long = System.currentTimeMillis()
    ): Boolean {
        if (slotIndex !in 0 until FLAME_SHIELD_LABS_COUNT) return false
        val currentStreak = loadInitialState()
        val currentForge = loadForgeState()
        val slot = currentForge.getFlameShieldSlot(slotIndex)
        if (!slot.isActive || slot.isReadyToClaim(nowEpochMs)) return false

        if (slot.refundShields > 0) {
            val restoredShields = (currentStreak.shieldsCount + slot.refundShields).coerceAtMost(currentStreak.maxShields)
            saveStreakState(currentStreak.copy(shieldsCount = restoredShields))
        }

        val keys = flameForgePrefKeys(slotIndex)
        prefs.edit()
            .putInt(KEY_FORGE_BRONZE_KEYS, currentForge.bronzeKeys + slot.refundBronzeKeys)
            .putInt(KEY_FORGE_SILVER_KEYS, currentForge.silverKeys + slot.refundSilverKeys)
            .putInt(KEY_FORGE_DIAMOND_KEYS, currentForge.diamondKeys + slot.refundDiamondKeys)
            .putBoolean(keys.active, false)
            .remove(keys.option)
            .putLong(keys.startedAtMs, 0L)
            .putInt(keys.refundShields, 0)
            .putInt(keys.refundBronze, 0)
            .putInt(keys.refundSilver, 0)
            .putInt(keys.refundDiamond, 0)
            .commit()
        return true
    }

    fun consumeFlameShieldOnMissedDay(): Pair<Boolean, Int> {
        val current = prefs.getInt(KEY_FLAME_SHIELDS_COUNT, 0).coerceAtLeast(0)
        return if (current > 0) {
            val remaining = current - 1
            prefs.edit().putInt(KEY_FLAME_SHIELDS_COUNT, remaining).commit()
            true to remaining
        } else {
            false to 0
        }
    }

    fun startForge(tier: ForgeKeyTier): Boolean {
        val currentStreak = loadInitialState()
        val currentForge = loadForgeState()
        if (currentForge.getSlot(tier).isActive) return false

        return if (tier.usesShields) {
            if (currentStreak.shieldsCount < tier.requiredShields) return false
            val newShields = (currentStreak.shieldsCount - tier.requiredShields).coerceAtLeast(0)
            saveStreakState(currentStreak.copy(shieldsCount = newShields))
            prefs.edit()
                .putBoolean(forgeActiveKey(tier), true)
                .putInt(forgeElapsedKey(tier), 0)
                .putInt(forgeRefundShieldsKey(tier), tier.requiredShields)
                .putInt(forgeRefundBronzeKey(tier), 0)
                .putInt(forgeRefundSilverKey(tier), 0)
                .putInt(forgeRefundGoldKey(tier), 0)
                .commit()
            true
        } else {
            if (currentForge.availableKeysFor(tier) < tier.requiredKeys) return false
            var remainingNeeded = tier.requiredKeys
            val usedBronze = minOf(currentForge.bronzeKeys, remainingNeeded)
            remainingNeeded -= usedBronze
            val usedSilver = minOf(currentForge.silverKeys, remainingNeeded)
            remainingNeeded -= usedSilver
            val usedGold = if (tier == ForgeKeyTier.DIAMOND) minOf(currentForge.goldenKeys, remainingNeeded) else 0
            remainingNeeded -= usedGold
            if (remainingNeeded > 0) return false

            prefs.edit()
                .putInt(KEY_FORGE_BRONZE_KEYS, (currentForge.bronzeKeys - usedBronze).coerceAtLeast(0))
                .putInt(KEY_FORGE_SILVER_KEYS, (currentForge.silverKeys - usedSilver).coerceAtLeast(0))
                .putInt(KEY_FORGE_GOLDEN_KEYS, (currentForge.goldenKeys - usedGold).coerceAtLeast(0))
                .putBoolean(forgeActiveKey(tier), true)
                .putInt(forgeElapsedKey(tier), 0)
                .putInt(forgeRefundShieldsKey(tier), 0)
                .putInt(forgeRefundBronzeKey(tier), usedBronze)
                .putInt(forgeRefundSilverKey(tier), usedSilver)
                .putInt(forgeRefundGoldKey(tier), usedGold)
                .commit()
            true
        }
    }

    fun claimForgeKey(tier: ForgeKeyTier): Boolean {
        val currentForge = loadForgeState()
        val slot = currentForge.getSlot(tier)
        if (!slot.isReadyToClaim) return false

        val targetKeyPref = when (tier) {
            ForgeKeyTier.BRONZE -> KEY_FORGE_BRONZE_KEYS
            ForgeKeyTier.SILVER -> KEY_FORGE_SILVER_KEYS
            ForgeKeyTier.GOLD -> KEY_FORGE_GOLDEN_KEYS
            ForgeKeyTier.DIAMOND -> KEY_FORGE_DIAMOND_KEYS
        }
        val nextCount = currentForge.getKeyCount(tier) + 1

        prefs.edit()
            .putInt(targetKeyPref, nextCount)
            .putBoolean(forgeActiveKey(tier), false)
            .putInt(forgeElapsedKey(tier), 0)
            .putInt(forgeRefundShieldsKey(tier), 0)
            .putInt(forgeRefundBronzeKey(tier), 0)
            .putInt(forgeRefundSilverKey(tier), 0)
            .putInt(forgeRefundGoldKey(tier), 0)
            .commit()
        return true
    }

    fun cancelForge(tier: ForgeKeyTier): Boolean {
        val currentStreak = loadInitialState()
        val currentForge = loadForgeState()
        val slot = currentForge.getSlot(tier)
        if (!slot.isActive || slot.isReadyToClaim) return false

        if (slot.refundShields > 0) {
            val restoredShields = (currentStreak.shieldsCount + slot.refundShields).coerceAtMost(currentStreak.maxShields)
            saveStreakState(currentStreak.copy(shieldsCount = restoredShields))
        }

        prefs.edit()
            .putInt(KEY_FORGE_BRONZE_KEYS, currentForge.bronzeKeys + slot.refundBronzeKeys)
            .putInt(KEY_FORGE_SILVER_KEYS, currentForge.silverKeys + slot.refundSilverKeys)
            .putInt(KEY_FORGE_GOLDEN_KEYS, currentForge.goldenKeys + slot.refundGoldKeys)
            .putBoolean(forgeActiveKey(tier), false)
            .putInt(forgeElapsedKey(tier), 0)
            .putInt(forgeRefundShieldsKey(tier), 0)
            .putInt(forgeRefundBronzeKey(tier), 0)
            .putInt(forgeRefundSilverKey(tier), 0)
            .putInt(forgeRefundGoldKey(tier), 0)
            .commit()
        return true
    }

    // --- Listen Library Telemetry, History, Favorites, Downloads & Playlists ---
    fun getListenMemberSince(): String {
        val existing = prefs.getString(KEY_LISTEN_MEMBER_SINCE, null)
        if (!existing.isNullOrBlank()) return existing
        val formatted = try {
            SimpleDateFormat("MM/yy", Locale.US).format(Date())
        } catch (_: Throwable) {
            "09/26"
        }
        prefs.edit().putString(KEY_LISTEN_MEMBER_SINCE, formatted).apply()
        return formatted
    }

    fun getListenTotalSeconds(): Long {
        return prefs.getLong(KEY_LISTEN_TOTAL_SECONDS, 0L)
    }

    fun addListenSeconds(deltaSeconds: Long) {
        if (deltaSeconds <= 0L) return
        val next = getListenTotalSeconds() + deltaSeconds
        prefs.edit().putLong(KEY_LISTEN_TOTAL_SECONDS, next).apply()
    }

    fun getListenTrackPlays(): Map<String, Int> {
        val raw = prefs.getString(KEY_LISTEN_TRACK_PLAYS_JSON, null) ?: return emptyMap()
        return try {
            val obj = org.json.JSONObject(raw)
            val map = LinkedHashMap<String, Int>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.optInt(key, 0)
            }
            map
        } catch (_: Throwable) {
            emptyMap()
        }
    }

    fun recordListenTrackPlay(trackId: String): Map<String, Int> {
        val current = getListenTrackPlays().toMutableMap()
        current[trackId] = (current[trackId] ?: 0) + 1
        val obj = org.json.JSONObject()
        current.forEach { (k, v) -> obj.put(k, v) }

        val history = getListenHistoryIds().toMutableList()
        history.remove(trackId)
        history.add(0, trackId)
        val trimmedHistory = history.take(40)
        val historyArr = org.json.JSONArray()
        trimmedHistory.forEach { historyArr.put(it) }

        val currentPlays = prefs.getInt(KEY_TOTAL_TRACKS_LISTENED, 0)
        prefs.edit()
            .putString(KEY_LISTEN_TRACK_PLAYS_JSON, obj.toString())
            .putString(KEY_LISTEN_HISTORY_JSON, historyArr.toString())
            .putInt(KEY_TOTAL_TRACKS_LISTENED, currentPlays + 1)
            .apply()
        return current
    }

    fun getListenTotalPlays(): Int {
        return prefs.getInt(KEY_TOTAL_TRACKS_LISTENED, 0)
    }

    fun getListenHistoryIds(): List<String> {
        val raw = prefs.getString(KEY_LISTEN_HISTORY_JSON, null) ?: return emptyList()
        return try {
            val arr = org.json.JSONArray(raw)
            buildList(arr.length()) {
                for (i in 0 until arr.length()) {
                    val id = arr.optString(i)
                    if (id.isNotBlank()) add(id)
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun getListenFavoriteIds(): Set<String> {
        return prefs.getStringSet(KEY_LISTEN_FAVORITE_IDS, emptySet())?.toSet() ?: emptySet()
    }

    fun toggleListenFavorite(trackId: String): Set<String> {
        val updated = getListenFavoriteIds().toMutableSet()
        if (updated.contains(trackId)) updated.remove(trackId) else updated.add(trackId)
        prefs.edit().putStringSet(KEY_LISTEN_FAVORITE_IDS, updated).apply()
        return updated
    }

    fun getListenDownloadedIds(): Set<String> {
        return prefs.getStringSet(KEY_LISTEN_DOWNLOADED_IDS, emptySet())?.toSet() ?: emptySet()
    }

    fun toggleListenDownloaded(trackId: String, forceSaved: Boolean? = null): Set<String> {
        val updated = getListenDownloadedIds().toMutableSet()
        val shouldSave = forceSaved ?: !updated.contains(trackId)
        if (shouldSave) updated.add(trackId) else updated.remove(trackId)
        prefs.edit().putStringSet(KEY_LISTEN_DOWNLOADED_IDS, updated).apply()
        return updated
    }

    fun getUserAudioPlaylists(): List<UserAudioPlaylist> {
        val raw = prefs.getString(KEY_LISTEN_PLAYLISTS_JSON, null) ?: return emptyList()
        return try {
            val arr = org.json.JSONArray(raw)
            buildList(arr.length()) {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id")
                    val name = obj.optString("name")
                    val tracksArr = obj.optJSONArray("trackIds")
                    val trackIds = if (tracksArr != null) {
                        buildList(tracksArr.length()) {
                            for (j in 0 until tracksArr.length()) {
                                val tid = tracksArr.optString(j)
                                if (tid.isNotBlank()) add(tid)
                            }
                        }
                    } else emptyList()
                    if (id.isNotBlank() && name.isNotBlank()) {
                        add(UserAudioPlaylist(id = id, name = name, trackIds = trackIds))
                    }
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveUserAudioPlaylists(playlists: List<UserAudioPlaylist>) {
        val arr = org.json.JSONArray()
        playlists.forEach { playlist ->
            val obj = org.json.JSONObject()
            obj.put("id", playlist.id)
            obj.put("name", playlist.name)
            val tArr = org.json.JSONArray()
            playlist.trackIds.forEach { tArr.put(it) }
            obj.put("trackIds", tArr)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_LISTEN_PLAYLISTS_JSON, arr.toString()).apply()
    }
}

data class UserAudioPlaylist(
    val id: String,
    val name: String,
    val trackIds: List<String> = emptyList()
)

data class LastWatchedVideoInfo(
    val id: String,
    val title: String,
    val uriString: String?,
    val durationMs: Long,
    val positionMs: Long,
    val thumbnailRes: Int = 0,
    val timestamp: Long = 0L
) {
    val progress: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progress * 100).toInt().coerceIn(0, 100)

    val remainingMs: Long
        get() = (durationMs - positionMs).coerceAtLeast(0L)
}

data class VideoProgressInfo(
    val id: String,
    val uriString: String?,
    val title: String,
    val durationMs: Long,
    val positionMs: Long,
    val thumbnailRes: Int = 0,
    val timestamp: Long = 0L
) {
    val progress: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progress * 100).toInt().coerceIn(0, 100)

    val remainingMs: Long
        get() = (durationMs - positionMs).coerceAtLeast(0L)
}
