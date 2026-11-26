package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AmbientBlackBlurMode
import com.example.data.local.AppDatabase
import com.example.data.local.CyberAvatarConfig
import com.example.data.local.CyberAvatarRepository
import com.example.data.local.HomeMimicConfig
import com.example.data.local.HomeMimicRepository
import com.example.data.local.MediaPlaybackActivityBus
import com.example.data.model.ForgeKeyTier
import com.example.data.model.ForgeUiState
import com.example.data.model.HomeMimicItem
import com.example.data.model.PlayerSeekbarStyle
import com.example.data.model.RoadmapXpState
import com.example.data.model.StoreAvatarItem
import com.example.data.model.VaultProgressionRegistry
import com.example.data.preferences.StreakPreferencesRepository
import com.example.data.preferences.VideoProgressInfo
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.StoreTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WatchXpNotice(
    val id: Long,
    val xpAmount: Long
)

data class ThemeTrialState(
    val theme: StoreTheme,
    val remainingSeconds: Int
)

enum class MilestoneStatus {
    UNLOCKED,
    CURRENT_TARGET,
    LOCKED
}

data class MilestoneItem(
    val day: Int,
    val title: String,
    val titleArabic: String,
    val description: String,
    val descriptionArabic: String,
    val iconType: String
)

data class StreakUiState(
    val streakDays: Int = 0,
    val shieldsCount: Int = 3,
    val maxShields: Int = StreakPreferencesRepository.MAX_SHIELDS_CAPACITY,
    val dailyWatchSeconds: Int = 0, // 0 sec / 5 min
    val dailyGoalSeconds: Int = 300, // 5 minutes actual playback
    val isGoalCompletedToday: Boolean = false,
    val isGenuinePlaybackActive: Boolean = false,
    val feedbackMessage: String? = null
) {
    val dailyProgress: Float
        get() = (dailyWatchSeconds.toFloat() / dailyGoalSeconds.toFloat()).coerceIn(0f, 1f)

    val remainingMinutes: Int
        get() = ((dailyGoalSeconds - dailyWatchSeconds).coerceAtLeast(0)) / 60

    val remainingSeconds: Int
        get() = ((dailyGoalSeconds - dailyWatchSeconds).coerceAtLeast(0)) % 60
}

class StreakViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = StreakPreferencesRepository.getInstance(application)
    private val appDb = AppDatabase.getInstance(application)
    private val homeMimicRepository = HomeMimicRepository(appDb.homeMimicDao())
    private val cyberAvatarRepository = CyberAvatarRepository(appDb.cyberAvatarDao())

    private val initialMimicConfig = HomeMimicConfig(
        equippedMimic = when {
            repository.isMimicDnaEnabled() -> HomeMimicItem.DNA_CHROMOSOME
            else -> HomeMimicItem.STREAK_FLAME
        },
        isMimicEnabled = repository.isMimicDnaEnabled() || repository.isMimicFlameEnabled()
    )

    val homeMimicConfig: StateFlow<HomeMimicConfig> = homeMimicRepository.configFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = initialMimicConfig
        )

    val cyberAvatarConfig: StateFlow<CyberAvatarConfig> = cyberAvatarRepository.configFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CyberAvatarConfig(
                unlockedAvatarIds = repository.getPurchasedCyberAvatarIds()
            )
        )

    val isAnyMediaPlaying: StateFlow<Boolean> = MediaPlaybackActivityBus.isAnyPlayerActive
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = false
        )

    init {
        viewModelScope.launch {
            homeMimicRepository.seedIfEmpty(
                initialItem = initialMimicConfig.equippedMimic,
                isEnabled = initialMimicConfig.isMimicEnabled
            )
            cyberAvatarRepository.seedIfEmpty(
                initialUnlockedIds = repository.getPurchasedCyberAvatarIds(),
                initialEquippedId = null
            )
        }
    }

    // Load initial state directly from persistent local storage on startup
    private val _uiState = MutableStateFlow(repository.loadInitialState().copy(feedbackMessage = null))
    val uiState: StateFlow<StreakUiState> = _uiState.asStateFlow()

    private val _watchXpEarnedEvents = MutableSharedFlow<WatchXpNotice>(extraBufferCapacity = 8)
    val watchXpEarnedEvents: SharedFlow<WatchXpNotice> = _watchXpEarnedEvents.asSharedFlow()

    init {
        // Consume any pending flame event quietly without triggering popup snackbars
        repository.consumePendingFlameEvent()
    }

    private val _xpState = MutableStateFlow(repository.loadRoadmapXpState())
    val xpState: StateFlow<RoadmapXpState> = _xpState.asStateFlow()

    private val _userName = MutableStateFlow(repository.getUserName())
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userAvatarUri = MutableStateFlow(repository.getUserAvatarUri())
    val userAvatarUri: StateFlow<String?> = _userAvatarUri.asStateFlow()

    private val _userAvatarPreset = MutableStateFlow(repository.getUserAvatarPreset())
    val userAvatarPreset: StateFlow<Int> = _userAvatarPreset.asStateFlow()

    private val _shieldFarmSeconds = MutableStateFlow(repository.getShieldFarmAccumulatedSeconds())
    val shieldFarmSeconds: StateFlow<Int> = _shieldFarmSeconds.asStateFlow()

    private val _forgeState = MutableStateFlow(repository.loadForgeState())
    val forgeState: StateFlow<ForgeUiState> = _forgeState.asStateFlow()

    private val _activeStoreTheme = MutableStateFlow(repository.loadStoreTheme())
    val activeStoreTheme: StateFlow<StoreTheme> = _activeStoreTheme.asStateFlow()

    private val _purchasedThemeIds = MutableStateFlow(repository.getPurchasedStoreThemeIds())
    val purchasedThemeIds: StateFlow<Set<String>> = _purchasedThemeIds.asStateFlow()

    private val _activeThemeTrial = MutableStateFlow(
        repository.getActiveTrialInfo()?.let { (t, sec) -> ThemeTrialState(t, sec) }
    )
    val activeThemeTrial: StateFlow<ThemeTrialState?> = _activeThemeTrial.asStateFlow()

    private val _isWaveformSeekbarPurchased = MutableStateFlow(repository.isWaveformSeekbarPurchased())
    val isWaveformSeekbarPurchased: StateFlow<Boolean> = _isWaveformSeekbarPurchased.asStateFlow()

    private val _isWaveformSeekbarEnabled = MutableStateFlow(repository.isWaveformSeekbarEnabled())
    val isWaveformSeekbarEnabled: StateFlow<Boolean> = _isWaveformSeekbarEnabled.asStateFlow()

    private val _isShootingStarSeekbarPurchased = MutableStateFlow(repository.isShootingStarSeekbarPurchased())
    val isShootingStarSeekbarPurchased: StateFlow<Boolean> = _isShootingStarSeekbarPurchased.asStateFlow()

    private val _isAngrySnakeSeekbarPurchased = MutableStateFlow(repository.isAngrySnakeSeekbarPurchased())
    val isAngrySnakeSeekbarPurchased: StateFlow<Boolean> = _isAngrySnakeSeekbarPurchased.asStateFlow()

    private val _purchasedSeekbarIds = MutableStateFlow(repository.getPurchasedSeekbarIds())
    val purchasedSeekbarIds: StateFlow<Set<String>> = _purchasedSeekbarIds.asStateFlow()

    private val _activeSeekbarStyle = MutableStateFlow(repository.getActiveSeekbarStyle())
    val activeSeekbarStyle: StateFlow<PlayerSeekbarStyle> = _activeSeekbarStyle.asStateFlow()

    private val _isMimicFlameEnabled = MutableStateFlow(repository.isMimicFlameEnabled())
    val isMimicFlameEnabled: StateFlow<Boolean> = _isMimicFlameEnabled.asStateFlow()

    private val _isMimicDnaPurchased = MutableStateFlow(repository.isMimicDnaPurchased())
    val isMimicDnaPurchased: StateFlow<Boolean> = _isMimicDnaPurchased.asStateFlow()

    private val _isMimicDnaEnabled = MutableStateFlow(repository.isMimicDnaEnabled())
    val isMimicDnaEnabled: StateFlow<Boolean> = _isMimicDnaEnabled.asStateFlow()

    private val _likedShortKeys = MutableStateFlow(repository.getLikedShortKeys())
    val likedShortKeys: StateFlow<Set<String>> = _likedShortKeys.asStateFlow()

    private val _savedShortKeys = MutableStateFlow(repository.getSavedShortKeys())
    val savedShortKeys: StateFlow<Set<String>> = _savedShortKeys.asStateFlow()

    private val _purchasedMimicIds = MutableStateFlow(repository.getPurchasedMimicIds())
    val purchasedMimicIds: StateFlow<Set<String>> = _purchasedMimicIds.asStateFlow()

    private val _purchasedAmbientModeIds = MutableStateFlow(repository.getPurchasedAmbientModeIds())
    val purchasedAmbientModeIds: StateFlow<Set<String>> = _purchasedAmbientModeIds.asStateFlow()

    private val _purchasedAvatarIds = MutableStateFlow(repository.getPurchasedCyberAvatarIds())
    val purchasedAvatarIds: StateFlow<Set<String>> = _purchasedAvatarIds.asStateFlow()

    private val _savedMediaIds = MutableStateFlow(repository.getSavedMediaIds())
    val savedMediaIds: StateFlow<Set<String>> = _savedMediaIds.asStateFlow()

    private val _watchHistory = MutableStateFlow(repository.getWatchHistory())
    val watchHistory: StateFlow<List<VideoProgressInfo>> = _watchHistory.asStateFlow()

    fun updateProfile(name: String, avatarUri: String?, presetId: Int) {
        val safeName = name.trim().ifBlank { "Charfeddine Kahlessenane" }
        repository.saveUserName(safeName)
        repository.saveUserAvatarUri(avatarUri)
        repository.saveUserAvatarPreset(presetId)
        _userName.value = safeName
        _userAvatarUri.value = avatarUri
        _userAvatarPreset.value = presetId
    }

    fun getUserTierBadge(roadmapLevel: Int, isArabic: Boolean): String {
        return when {
            roadmapLevel >= 365 -> if (isArabic) "🏆 Yearly Aura Legend (Lvl 365)" else "🏆 Yearly Aura Legend (Lvl 365)"
            roadmapLevel >= 200 -> if (isArabic) "🎖️ Local System Master (Lvl 200+)" else "🎖️ Local System Master (Lvl 200+)"
            roadmapLevel >= 100 -> if (isArabic) "✨ Aura Pro Title (Lvl 100+)" else "✨ Aura Pro Title (Lvl 100+)"
            roadmapLevel >= 50 -> if (isArabic) "💎 Acoustic Master (Lvl 50+)" else "💎 Acoustic Master (Lvl 50+)"
            roadmapLevel >= 20 -> if (isArabic) "🛡️ مستخدم مميز (المستوى $roadmapLevel)" else "🛡️ Special User (Lvl $roadmapLevel)"
            else -> if (isArabic) "المستوى $roadmapLevel • التأسيسي" else "Level $roadmapLevel • Foundation"
        }
    }

    val milestones = listOf(
        MilestoneItem(
            day = 5,
            title = "Glowing Starter Shield",
            titleArabic = "درع البداية المتوهج",
            description = "Bonus streak shield drop & neon border aura badge.",
            descriptionArabic = "مكافأة دروع إضافية وشارة توهج الزجاج السائل للملف الشخصي.",
            iconType = "shield"
        ),
        MilestoneItem(
            day = 10,
            title = "Silver Flame Guardian",
            titleArabic = "حارس الشعلة الفضي",
            description = "Awarded 3 extra Streak Shields & silver prestige badge.",
            descriptionArabic = "منح 3 دروع حماية إضافية مع وسام الحارس الفضي في مسار الإنجاز.",
            iconType = "trophy"
        ),
        MilestoneItem(
            day = 15,
            title = "Perseverance Shield Pack",
            titleArabic = "حزمة دروع المثابرة",
            description = "Half-month milestone drop with +3 Streak Shields.",
            descriptionArabic = "إنجاز منتصف الشهر مع مكافأة فورية +3 دروع حماية للسلسلة.",
            iconType = "shield"
        ),
        MilestoneItem(
            day = 20,
            title = "Golden Royal Shield",
            titleArabic = "الدرع الذهبي الملكي",
            description = "Rare Golden Royal crest awarded for 20 consecutive days.",
            descriptionArabic = "وسام الدرع الذهبي الملكي النادر لإنجاز 20 يوماً متتالياً.",
            iconType = "gold"
        ),
        MilestoneItem(
            day = 25,
            title = "Radiant Aura Emblem",
            titleArabic = "وسام الهالة المشعّة",
            description = "Exclusive +5 Shields bonus & luminous glass crest.",
            descriptionArabic = "شعار هالة نيوني لامع مع مكافأة +5 دروع في الخزينة.",
            iconType = "sparkle"
        ),
        MilestoneItem(
            day = 30,
            title = "Month-1 Grand Drop",
            titleArabic = "مكافأة الشهر الأول الكبرى",
            description = "Complete 30 days to earn +10 Shields & Month-1 Guardian title.",
            descriptionArabic = "اكتمال 30 يوماً متواصلة مع مكافأة كبرى +10 دروع ووسام حارس الشهر.",
            iconType = "crown"
        ),
        MilestoneItem(
            day = 45,
            title = "Elite Streak Champion",
            titleArabic = "وسام بطل السلسلة المتواصلة",
            description = "45-day dedication trophy & +12 Shields inventory boost.",
            descriptionArabic = "إنجاز 45 يوماً من الاستمرارية مع تعزيز +12 درعاً في الخزينة.",
            iconType = "trophy"
        ),
        MilestoneItem(
            day = 60,
            title = "Legendary Diamond Shield",
            titleArabic = "الدرع الماسي الأسطوري",
            description = "Mythic VIP Diamond crest & full 60-day Mimic ice thaw mastery.",
            descriptionArabic = "الوسام الماسي الأسطوري وذوبان جليد الميميكس بالكامل في اليوم 60.",
            iconType = "diamond"
        )
    )

    fun getMilestoneStatus(day: Int, currentStreak: Int): MilestoneStatus {
        return when {
            currentStreak >= day -> MilestoneStatus.UNLOCKED
            milestones.firstOrNull { it.day > currentStreak }?.day == day -> MilestoneStatus.CURRENT_TARGET
            else -> MilestoneStatus.LOCKED
        }
    }

    /**
     * Ticks actual playback time towards the 5-minute daily watch goal AND continuous Watch XP (10 XP/min).
     * Completing the daily goal also awards Streak XP (Day 1 = 100 XP ... Day 365+ = 36,500 XP).
     * Core Rule: Seeking or fast-forwarding MUST NOT count.
     * Automatically persists state to disk immediately.
     */
    fun tickGenuineWatchTime(seconds: Int = 1, isSeeking: Boolean = false) {
        if (isSeeking) return

        val outcome = repository.tickDailyWatchAndXp(seconds)
        _xpState.value = outcome.xpState
        val totalXpEarned = outcome.watchXpEarned + outcome.streakXpEarned
        if (totalXpEarned > 0L) {
            _watchXpEarnedEvents.tryEmit(
                WatchXpNotice(
                    id = System.nanoTime(),
                    xpAmount = totalXpEarned
                )
            )
        }

        _uiState.update {
            outcome.streakState.copy(feedbackMessage = null)
        }
    }

    /**
     * Ticks active foreground app usage time.
     * Core Rule: Background usage does NOT count.
     * 10 shields awarded per 1 full hour of active app usage (unlimited daily farming, max 1000 total).
     */
    fun tickActiveForegroundUsage(seconds: Int = 1) {
        val (updatedState, earnedShield) = repository.tickActiveForegroundUsage(seconds)
        _shieldFarmSeconds.value = repository.getShieldFarmAccumulatedSeconds()
        _forgeState.value = repository.loadForgeState()
        if (earnedShield) {
            _uiState.update { current ->
                current.copy(
                    shieldsCount = updatedState.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
    }

    /**
     * Automatically consumes 1 The Flame Shield to protect the streak flame when idle/missing a day.
     * Regular Shields NO LONGER protect the Streak Flame.
     * Strict Penalty: If 0 Flame Shields, streak resets to 0 and Mimics re-freeze!
     */
    fun simulateMissedDay() {
        val (protected, _) = repository.consumeFlameShieldOnMissedDay()
        _forgeState.value = repository.loadForgeState()
        _uiState.update { current ->
            val newState = if (protected) {
                current.copy(
                    dailyWatchSeconds = 0,
                    isGoalCompletedToday = false,
                    feedbackMessage = null
                )
            } else {
                current.copy(
                    streakDays = 0,
                    dailyWatchSeconds = 0,
                    isGoalCompletedToday = false,
                    feedbackMessage = null
                )
            }
            repository.saveStreakState(newState)
            newState
        }
    }

    /**
     * Add a shield up to max 1000 and save immediately.
     */
    fun addShield() {
        _uiState.update { current ->
            val newState = if (current.shieldsCount < current.maxShields) {
                current.copy(
                    shieldsCount = current.shieldsCount + 1,
                    feedbackMessage = null
                )
            } else {
                current.copy(feedbackMessage = null)
            }
            repository.saveStreakState(newState)
            newState
        }
    }

    /**
     * Set streak count and persist to local storage.
     */
    fun setStreak(days: Int) {
        _uiState.update { current ->
            val newState = current.copy(
                streakDays = days.coerceAtLeast(0),
                feedbackMessage = null
            )
            repository.saveStreakState(newState)
            newState
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    // Shield Farming & Analytics
    fun getShieldFarmAccumulatedSeconds(): Int = repository.getShieldFarmAccumulatedSeconds()
    fun getShieldsEarnedToday(): Int = repository.getShieldsEarnedToday()
    fun getTotalWatchHours(): Float = repository.getTotalWatchHours()
    fun getTotalTracksListened(): Int = repository.getTotalTracksListened()
    fun incrementTrackListened() = repository.incrementTrackListened()

    // Hidden Vault
    fun hasCustomVaultPin(): Boolean = repository.hasCustomVaultPin()
    fun verifyVaultPin(pin: String): Boolean = repository.verifyVaultPin(pin)
    fun saveVaultPin(pin: String): Boolean = repository.saveVaultPin(pin)
    fun getVaultMediaIds(): Set<String> = repository.getVaultMediaIds()
    fun toggleVaultMedia(id: String): Boolean = repository.toggleVaultMedia(id)

    // Watchlist (Saved) & Watch History
    fun toggleSavedMedia(mediaId: String, title: String? = null, isArabic: Boolean = false): Boolean {
        val savedNow = repository.toggleSavedMediaId(mediaId)
        _savedMediaIds.value = repository.getSavedMediaIds()
        return savedNow
    }

    fun refreshWatchHistory() {
        _watchHistory.value = repository.getWatchHistory()
    }

    fun removeWatchHistoryItem(id: String, uriString: String?) {
        repository.removeWatchHistoryItem(id, uriString)
        _watchHistory.value = repository.getWatchHistory()
    }

    fun clearWatchHistory(isArabic: Boolean = false) {
        repository.clearWatchHistory()
        _watchHistory.value = emptyList()
    }

    // System Settings Hub & Aura Pulse Premium
    fun getRgbAccentColor(): Long = repository.getRgbAccentColor()
    fun saveRgbAccentColor(color: Long) = repository.saveRgbAccentColor(color)
    fun isOledBlackEnabled(): Boolean = repository.isOledBlackEnabled()
    fun saveOledBlackEnabled(enabled: Boolean) = repository.saveOledBlackEnabled(enabled)
    fun getAutoResume(): Boolean = repository.getAutoResumePlayback()
    fun saveAutoResume(enabled: Boolean) = repository.saveAutoResumePlayback(enabled)
    fun getDefaultSpeed(): Float = repository.getDefaultPlaybackSpeed()
    fun saveDefaultSpeed(speed: Float) = repository.saveDefaultPlaybackSpeed(speed)
    fun clearThumbnailCache(): Long = repository.clearThumbnailCache()
    fun exportPreferencesToJson(): String = repository.exportPreferencesToJson()
    fun importPreferencesFromJson(json: String): Boolean {
        val ok = repository.importPreferencesFromJson(json)
        if (ok) {
            _uiState.value = repository.loadInitialState()
            _xpState.value = repository.loadRoadmapXpState()
            _userName.value = repository.getUserName()
            _isFullScreenMode.value = repository.isFullScreenModeEnabled()
        }
        return ok
    }

    private val _isPremiumActivated = MutableStateFlow(repository.isPremiumActivated())
    val isPremiumActivated: StateFlow<Boolean> = _isPremiumActivated.asStateFlow()

    fun setPremiumActivated(activated: Boolean, isArabic: Boolean = true) {
        repository.setPremiumActivated(activated)
        _isPremiumActivated.value = activated
        if (activated) {
            _activeThemeTrial.value = null
            val currentSeekbar = repository.getActiveSeekbarStyle()
            _activeSeekbarStyle.value = currentSeekbar
            _isWaveformSeekbarEnabled.value = (currentSeekbar == PlayerSeekbarStyle.WAVEFORM)
            _isMimicDnaEnabled.value = repository.isMimicDnaEnabled()
            _isMimicFlameEnabled.value = repository.isMimicFlameEnabled()
        } else {
            val currentTheme = _activeStoreTheme.value
            if (!repository.isStoreThemePermanentlyOwned(currentTheme)) {
                val fallback = repository.loadLastOwnedStoreTheme()
                repository.saveStoreTheme(fallback)
                _activeStoreTheme.value = fallback
            }
            val currentSeekbar = _activeSeekbarStyle.value
            if (!repository.isSeekbarPurchased(currentSeekbar)) {
                repository.setActiveSeekbarStyle(PlayerSeekbarStyle.CLASSIC)
            }
            val syncedSeekbar = repository.getActiveSeekbarStyle()
            _activeSeekbarStyle.value = syncedSeekbar
            _isWaveformSeekbarEnabled.value = (syncedSeekbar == PlayerSeekbarStyle.WAVEFORM)
            val currentMimic = homeMimicConfig.value.equippedMimic
            if (!repository.isMimicPurchased(currentMimic)) {
                repository.setMimicFlameEnabled(true)
                viewModelScope.launch {
                    homeMimicRepository.equipMimic(HomeMimicItem.STREAK_FLAME)
                }
            }
            val currentAvatar = cyberAvatarConfig.value.equippedAvatar
            if (currentAvatar != null && !repository.isCyberAvatarPurchased(currentAvatar)) {
                viewModelScope.launch {
                    cyberAvatarRepository.unequipAvatar()
                }
            }
            _isMimicDnaEnabled.value = repository.isMimicDnaEnabled()
            _isMimicFlameEnabled.value = repository.isMimicFlameEnabled()
        }
    }

    fun getSelectedThemePresetIndex(): Int = repository.getSelectedThemePresetIndex()
    fun saveSelectedThemePresetIndex(index: Int) = repository.saveSelectedThemePresetIndex(index)
    fun isWifiDirectSharingEnabled(): Boolean = repository.isWifiDirectSharingEnabled()
    fun setWifiDirectSharingEnabled(enabled: Boolean) = repository.setWifiDirectSharingEnabled(enabled)
    fun isSmartNotificationsEnabled(): Boolean = repository.isSmartNotificationsEnabled()
    fun setSmartNotificationsEnabled(enabled: Boolean) = repository.setSmartNotificationsEnabled(enabled)
    fun isBackgroundPlayEnabled(): Boolean = repository.isBackgroundPlayEnabled()
    fun setBackgroundPlayEnabled(enabled: Boolean) = repository.setBackgroundPlayEnabled(enabled)
    fun isPipModeEnabled(): Boolean = repository.isPipModeEnabled()
    fun setPipModeEnabled(enabled: Boolean) = repository.setPipModeEnabled(enabled)
    private val _isEyeProtectionEnabled = MutableStateFlow(repository.isEyeProtectionEnabled())
    val isEyeProtectionEnabled: StateFlow<Boolean> = _isEyeProtectionEnabled.asStateFlow()

    fun isEyeProtectionEnabled(): Boolean = repository.isEyeProtectionEnabled()
    fun setEyeProtectionEnabled(enabled: Boolean) {
        repository.setEyeProtectionEnabled(enabled)
        _isEyeProtectionEnabled.value = enabled
    }

    private val _isFullScreenMode = MutableStateFlow(repository.isFullScreenModeEnabled())
    val isFullScreenMode: StateFlow<Boolean> = _isFullScreenMode.asStateFlow()

    fun setFullScreenModeEnabled(enabled: Boolean) {
        repository.setFullScreenModeEnabled(enabled)
        _isFullScreenMode.value = enabled
    }

    fun getLastStatusBarHeightDp(): Float = repository.getLastStatusBarHeightDp()
    fun saveLastStatusBarHeightDp(heightDp: Float) = repository.saveLastStatusBarHeightDp(heightDp)

    fun toggleShortLike(videoKey: String) {
        _likedShortKeys.value = repository.toggleShortLike(videoKey)
    }

    fun ensureShortLiked(videoKey: String) {
        _likedShortKeys.value = repository.setShortLiked(videoKey, true)
    }

    fun toggleShortSave(videoKey: String) {
        _savedShortKeys.value = repository.toggleShortSave(videoKey)
    }

    // 365-Level Progression & Feature Vault
    fun getClaimedVaultLevels(): Set<Int> = repository.getClaimedVaultLevels()
    fun isVaultLevelClaimed(level: Int): Boolean = repository.getClaimedVaultLevels().contains(level)

    fun claimVaultLevelReward(level: Int, shieldsToAward: Int): Boolean {
        val success = repository.claimVaultLevelReward(level, shieldsToAward)
        if (success) {
            val updated = repository.loadInitialState()
            _uiState.update {
                it.copy(
                    shieldsCount = updated.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
        return success
    }

    fun claimAllUnlockedVaultRewards(levelsWithRewards: List<Pair<Int, Int>>): Int {
        val count = repository.claimAllUnlockedVaultRewards(levelsWithRewards)
        if (count > 0) {
            val updated = repository.loadInitialState()
            _uiState.update {
                it.copy(
                    shieldsCount = updated.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
        return count
    }

    // --- The Forge Actions ---
    fun startForge(tier: ForgeKeyTier, isArabic: Boolean = true): Boolean {
        val started = repository.startForge(tier)
        if (started) {
            val updatedStreak = repository.loadInitialState()
            _forgeState.value = repository.loadForgeState()
            _uiState.update {
                it.copy(
                    shieldsCount = updatedStreak.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
        return started
    }

    fun claimForgeKey(tier: ForgeKeyTier, isArabic: Boolean = true): Boolean {
        val claimed = repository.claimForgeKey(tier)
        if (claimed) {
            _forgeState.value = repository.loadForgeState()
        }
        return claimed
    }

    fun cancelForge(tier: ForgeKeyTier, isArabic: Boolean = true): Boolean {
        val cancelled = repository.cancelForge(tier)
        if (cancelled) {
            val updatedStreak = repository.loadInitialState()
            _forgeState.value = repository.loadForgeState()
            _uiState.update {
                it.copy(
                    shieldsCount = updatedStreak.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
        return cancelled
    }

    fun craftFlameShield(
        option: com.example.data.model.FlameShieldCraftOption,
        slotIndex: Int = 0,
        isArabic: Boolean = true
    ): Boolean {
        val started = repository.craftFlameShield(option, slotIndex = slotIndex)
        if (started) {
            val updatedStreak = repository.loadInitialState()
            val updatedForge = repository.loadForgeState()
            _forgeState.value = updatedForge
            _uiState.update {
                it.copy(
                    shieldsCount = updatedStreak.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
        return started
    }

    fun claimFlameShieldCraft(
        slotIndex: Int = 0,
        isArabic: Boolean = true
    ): Boolean {
        val claimed = repository.claimFlameShieldCraft(slotIndex = slotIndex)
        if (claimed) {
            _forgeState.value = repository.loadForgeState()
        }
        return claimed
    }

    fun cancelFlameShieldCraft(
        slotIndex: Int = 0,
        isArabic: Boolean = true
    ): Boolean {
        val cancelled = repository.cancelFlameShieldCraft(slotIndex = slotIndex)
        if (cancelled) {
            val updatedStreak = repository.loadInitialState()
            _forgeState.value = repository.loadForgeState()
            _uiState.update {
                it.copy(
                    shieldsCount = updatedStreak.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
        return cancelled
    }

    // --- Store Theme Economy & Free 1-Minute Trial ---
    fun equipOwnedStoreTheme(theme: StoreTheme, isArabic: Boolean = true): Boolean {
        val equipped = repository.equipOwnedStoreTheme(theme)
        if (equipped) {
            _activeStoreTheme.value = theme
            _activeThemeTrial.value = null
        }
        return equipped
    }

    fun purchaseStoreThemeWithKeys(theme: StoreTheme, isArabic: Boolean = true): Boolean {
        val purchased = repository.purchaseStoreTheme(theme)
        if (purchased) {
            val updatedStreak = repository.loadInitialState()
            _forgeState.value = repository.loadForgeState()
            _purchasedThemeIds.value = repository.getPurchasedStoreThemeIds()
            _activeStoreTheme.value = theme
            _activeThemeTrial.value = null
            _uiState.update {
                it.copy(
                    shieldsCount = updatedStreak.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
        return purchased
    }

    fun startThemeTrialWithShield(theme: StoreTheme, isArabic: Boolean = true): Boolean {
        val expiresAt = repository.activateTrialTheme(theme)
        if (expiresAt != null) {
            _activeStoreTheme.value = theme
            _activeThemeTrial.value = ThemeTrialState(theme = theme, remainingSeconds = 60)
            return true
        } else if (repository.isStoreThemeUnlocked(theme)) {
            return equipOwnedStoreTheme(theme, isArabic)
        } else {
            return false
        }
    }

    fun tickThemeTrialTimer(isArabic: Boolean = true) {
        val reverted = repository.expireTrialIfNeeded()
        if (reverted != null) {
            _activeThemeTrial.value = null
            _activeStoreTheme.value = reverted
        } else {
            val activeInfo = repository.getActiveTrialInfo()
            _activeThemeTrial.value = activeInfo?.let { (t, sec) -> ThemeTrialState(t, sec) }
        }
    }

    // --- Store Seekbar Purchase & Activation ---
    fun purchaseSeekbarStyle(style: PlayerSeekbarStyle, isArabic: Boolean = true): Boolean {
        val purchased = repository.purchaseSeekbarStyle(style)
        if (purchased) {
            val updatedStreak = repository.loadInitialState()
            _forgeState.value = repository.loadForgeState()
            _isWaveformSeekbarPurchased.value = repository.isWaveformSeekbarPurchased()
            _isShootingStarSeekbarPurchased.value = repository.isShootingStarSeekbarPurchased()
            _isAngrySnakeSeekbarPurchased.value = repository.isAngrySnakeSeekbarPurchased()
            _purchasedSeekbarIds.value = repository.getPurchasedSeekbarIds()
            _activeSeekbarStyle.value = style
            _isWaveformSeekbarEnabled.value = (style == PlayerSeekbarStyle.WAVEFORM)
            _uiState.update {
                it.copy(
                    shieldsCount = updatedStreak.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
        return purchased
    }

    fun selectSeekbarStyle(style: PlayerSeekbarStyle, isArabic: Boolean = true) {
        if (repository.setActiveSeekbarStyle(style)) {
            _activeSeekbarStyle.value = style
            _isWaveformSeekbarEnabled.value = (style == PlayerSeekbarStyle.WAVEFORM)
        }
    }

    fun purchaseWaveformSeekbar(isArabic: Boolean = true): Boolean =
        purchaseSeekbarStyle(PlayerSeekbarStyle.WAVEFORM, isArabic = isArabic)

    fun toggleWaveformSeekbarEnabled(enabled: Boolean, isArabic: Boolean = true) {
        selectSeekbarStyle(
            if (enabled) PlayerSeekbarStyle.WAVEFORM else PlayerSeekbarStyle.CLASSIC,
            isArabic = isArabic
        )
    }

    // --- Room-Backed Modular Home Mimic Equip, Purchase, Switch & Disable ---
    fun purchaseHomeMimic(item: HomeMimicItem, isArabic: Boolean = true): Boolean {
        if (item == HomeMimicItem.DNA_CHROMOSOME) {
            return purchaseMimicDna(isArabic = isArabic)
        }
        val purchased = repository.purchaseMimicItem(item)
        if (purchased) {
            val updatedStreak = repository.loadInitialState()
            _forgeState.value = repository.loadForgeState()
            _purchasedMimicIds.value = repository.getPurchasedMimicIds()
            _isMimicFlameEnabled.value = (item == HomeMimicItem.STREAK_FLAME)
            _isMimicDnaEnabled.value = (item == HomeMimicItem.DNA_CHROMOSOME)
            viewModelScope.launch {
                homeMimicRepository.equipMimic(item)
            }
            _uiState.update {
                it.copy(
                    shieldsCount = updatedStreak.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
        return purchased
    }

    fun equipHomeMimic(item: HomeMimicItem, isArabic: Boolean = true): Boolean {
        if (!repository.isMimicUnlocked(item)) {
            return purchaseHomeMimic(item = item, isArabic = isArabic)
        }
        when (item) {
            HomeMimicItem.STREAK_FLAME -> {
                repository.setMimicFlameEnabled(true)
            }
            HomeMimicItem.DNA_CHROMOSOME -> {
                repository.setMimicDnaEnabled(true)
            }
            else -> {
                repository.setMimicFlameEnabled(false)
                repository.setMimicDnaEnabled(false)
            }
        }
        _isMimicFlameEnabled.value = (item == HomeMimicItem.STREAK_FLAME)
        _isMimicDnaEnabled.value = (item == HomeMimicItem.DNA_CHROMOSOME)
        viewModelScope.launch {
            homeMimicRepository.equipMimic(item)
        }
        return true
    }

    fun setHomeMimicEnabled(enabled: Boolean, isArabic: Boolean = true) {
        val active = homeMimicConfig.value.equippedMimic
        if (!enabled) {
            repository.setMimicFlameEnabled(false)
            repository.setMimicDnaEnabled(false)
            _isMimicFlameEnabled.value = false
            _isMimicDnaEnabled.value = false
        } else {
            when (active) {
                HomeMimicItem.STREAK_FLAME -> repository.setMimicFlameEnabled(true)
                HomeMimicItem.DNA_CHROMOSOME -> repository.setMimicDnaEnabled(true)
                else -> Unit
            }
            _isMimicFlameEnabled.value = (active == HomeMimicItem.STREAK_FLAME)
            _isMimicDnaEnabled.value = (active == HomeMimicItem.DNA_CHROMOSOME)
        }
        viewModelScope.launch {
            homeMimicRepository.setMimicEnabled(enabled, active)
        }
    }

    // --- Store Mimic Purchase, Equip & Disable (Single Home Centerpiece Slot) ---
    fun toggleMimicFlameEnabled(enabled: Boolean, isArabic: Boolean = true) {
        if (repository.setMimicFlameEnabled(enabled)) {
            _isMimicFlameEnabled.value = repository.isMimicFlameEnabled()
            _isMimicDnaEnabled.value = repository.isMimicDnaEnabled()
            viewModelScope.launch {
                if (enabled) {
                    homeMimicRepository.equipMimic(HomeMimicItem.STREAK_FLAME)
                } else if (homeMimicConfig.value.equippedMimic == HomeMimicItem.STREAK_FLAME) {
                    homeMimicRepository.setMimicEnabled(false, HomeMimicItem.STREAK_FLAME)
                }
            }
        }
    }

    fun purchaseMimicDna(isArabic: Boolean = true): Boolean {
        val purchased = repository.purchaseMimicDna()
        if (purchased) {
            val updatedStreak = repository.loadInitialState()
            _forgeState.value = repository.loadForgeState()
            _isMimicDnaPurchased.value = repository.isMimicDnaPurchased()
            _isMimicDnaEnabled.value = repository.isMimicDnaEnabled()
            _isMimicFlameEnabled.value = repository.isMimicFlameEnabled()
            _purchasedMimicIds.value = repository.getPurchasedMimicIds()
            viewModelScope.launch {
                homeMimicRepository.equipMimic(HomeMimicItem.DNA_CHROMOSOME)
            }
            _uiState.update {
                it.copy(
                    shieldsCount = updatedStreak.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
        return purchased
    }

    fun toggleMimicDnaEnabled(enabled: Boolean, isArabic: Boolean = true) {
        if (repository.setMimicDnaEnabled(enabled)) {
            _isMimicDnaEnabled.value = repository.isMimicDnaEnabled()
            _isMimicFlameEnabled.value = repository.isMimicFlameEnabled()
            viewModelScope.launch {
                if (enabled) {
                    homeMimicRepository.equipMimic(HomeMimicItem.DNA_CHROMOSOME)
                } else if (homeMimicConfig.value.equippedMimic == HomeMimicItem.DNA_CHROMOSOME) {
                    homeMimicRepository.setMimicEnabled(false, HomeMimicItem.DNA_CHROMOSOME)
                }
            }
        }
    }

    // --- Store Ambient Video Fill Modes Purchase ---
    fun purchaseAmbientMode(mode: AmbientBlackBlurMode, isArabic: Boolean = true): Boolean {
        val purchased = repository.purchaseAmbientMode(mode)
        if (purchased) {
            val updatedStreak = repository.loadInitialState()
            _forgeState.value = repository.loadForgeState()
            _purchasedAmbientModeIds.value = repository.getPurchasedAmbientModeIds()
            _uiState.update {
                it.copy(
                    shieldsCount = updatedStreak.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
        return purchased
    }

    // --- Room-Backed Cyberpunk Avatars & Holographic Aura Frames ---
    fun purchaseCyberAvatar(avatar: StoreAvatarItem, isArabic: Boolean = true): Boolean {
        val wasPurchased = repository.isCyberAvatarPurchased(avatar)
        val isPremiumFree = _isPremiumActivated.value && !wasPurchased
        val purchased = repository.purchaseCyberAvatar(avatar)
        if (purchased) {
            val updatedStreak = repository.loadInitialState()
            _forgeState.value = repository.loadForgeState()
            _purchasedAvatarIds.value = repository.getPurchasedCyberAvatarIds()
            repository.saveUserAvatarUri(null)
            _userAvatarUri.value = null
            viewModelScope.launch {
                if (!isPremiumFree) {
                    cyberAvatarRepository.unlockAvatar(avatar, autoEquip = true)
                } else {
                    cyberAvatarRepository.equipAvatar(avatar)
                }
            }
            _uiState.update {
                it.copy(
                    shieldsCount = updatedStreak.shieldsCount,
                    feedbackMessage = null
                )
            }
        }
        return purchased
    }

    fun equipCyberAvatar(avatar: StoreAvatarItem, isArabic: Boolean = true): Boolean {
        if (!repository.isCyberAvatarUnlocked(avatar)) {
            return purchaseCyberAvatar(avatar = avatar, isArabic = isArabic)
        }
        repository.saveUserAvatarUri(null)
        _userAvatarUri.value = null
        viewModelScope.launch {
            cyberAvatarRepository.equipAvatar(avatar)
        }
        return true
    }

    fun clearEquippedCyberAvatar(isArabic: Boolean = true, silent: Boolean = false) {
        viewModelScope.launch {
            cyberAvatarRepository.unequipAvatar()
        }
    }

    fun setCyberAvatarFrameEnabled(enabled: Boolean) {
        viewModelScope.launch {
            cyberAvatarRepository.setAuraFrameEnabled(enabled)
        }
    }
}
