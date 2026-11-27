package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.model.LocalDeviceVideo
import com.example.data.model.MediaItem
import com.example.data.preferences.StreakPreferencesRepository
import com.example.data.util.LocalMediaScanner
import com.example.ui.components.AccountRankAndRedeemDialog
import com.example.ui.components.LocalFloatingBottomBarPadding
import com.example.ui.components.MemberStreakRank
import com.example.ui.components.MySpaceHistorySheet
import com.example.ui.components.MySpaceLikedVideosSheet
import com.example.ui.components.MySpaceOfflineMediaSheet
import com.example.ui.components.MySpaceWatchlistSheet
import com.example.ui.components.allCatalogMediaItems
import com.example.ui.components.asHomeMediaItem
import com.example.ui.components.formatCleanTitle
import com.example.ui.components.resolveEarnedTitle
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.AuraAtmosphericLayer
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.aura
import com.example.ui.theme.stableStatusBarsPadding
import com.example.ui.viewmodel.StreakViewModel
import com.example.ui.viewmodel.WatchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MySpaceScreen(
    streakViewModel: StreakViewModel,
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onOpenRoadmap: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onOpenShieldFarming: () -> Unit = {},
    onOpenStatistics: () -> Unit = {},
    onOpenSystemSettings: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    isCheckingForUpdates: Boolean = false,
    onPlayMedia: (MediaItem) -> Unit = {},
    onNavigateToWatch: () -> Unit = {},
    onNavigateToListen: () -> Unit = {},
    watchViewModel: WatchViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val uiState by streakViewModel.uiState.collectAsState()
    val xpState by streakViewModel.xpState.collectAsState()
    val userName by streakViewModel.userName.collectAsState()
    val cyberAvatarConfig by streakViewModel.cyberAvatarConfig.collectAsState()
    val isPremiumActivated by streakViewModel.isPremiumActivated.collectAsState()
    val savedMediaIds by streakViewModel.savedMediaIds.collectAsState()
    val watchHistory by streakViewModel.watchHistory.collectAsState()
    val bookmarkGroups by watchViewModel.bookmarkGroups.collectAsState()

    val isArabic = currentLanguage == AppLanguage.ARABIC
    val streakDays = uiState.streakDays
    val totalWatchHours = streakViewModel.getTotalWatchHours()
    val currentRank = remember(streakDays) { MemberStreakRank.forStreak(streakDays) }
    val earnedTitle = remember(xpState.unlockedLevel) { resolveEarnedTitle(xpState.unlockedLevel) }

    val prefsRepo = remember(context) { StreakPreferencesRepository.getInstance(context) }

    var showAccountRankDialog by remember { mutableStateOf(false) }
    var showAccountsHubSheet by remember { mutableStateOf(false) }
    var showWatchlistSheet by remember { mutableStateOf(false) }
    var showLikedVideosSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showOfflineSheet by remember { mutableStateOf(false) }
    var showPlaylistsSheet by remember { mutableStateOf(false) }
    var statusBanner by remember { mutableStateOf<String?>(null) }

    val localVideos = remember { mutableStateListOf<LocalDeviceVideo>() }
    var likedVideoKeys by remember { mutableStateOf(prefsRepo.getShortLikedVideoKeys()) }

    LaunchedEffect(Unit) {
        likedVideoKeys = prefsRepo.getShortLikedVideoKeys()
        if (LocalMediaScanner.hasStorageOrVideoPermission(context)) {
            val scanned = withContext(Dispatchers.IO) {
                LocalMediaScanner.scanDeviceVideos(context)
            }
            localVideos.clear()
            localVideos.addAll(scanned)
        }
    }

    LaunchedEffect(statusBanner) {
        if (statusBanner != null) {
            delay(2200)
            statusBanner = null
        }
    }

    val pickLocalVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val resolved = withContext(Dispatchers.IO) {
                    uris.map { uri -> LocalMediaScanner.parseUriToLocalVideo(context, uri) }
                }
                val merged = (resolved + localVideos).distinctBy { it.uriString ?: it.id }
                localVideos.clear()
                localVideos.addAll(merged)
                resolved.firstOrNull()?.let { first ->
                    onPlayMedia(first.asHomeMediaItem())
                }
            }
        }
    }

    val allCatalog = remember { allCatalogMediaItems() }
    val localThumbMap = remember(localVideos.toList()) {
        localVideos.associate { it.id to it.bitmap }
    }
    val savedMediaItems = remember(savedMediaIds, allCatalog, localVideos.toList()) {
        val localAsMedia = localVideos.map { it.asHomeMediaItem() }
        val pool = allCatalog + localAsMedia
        pool.filter { it.id in savedMediaIds }.distinctBy { it.id }
    }
    val likedLocalVideos = remember(likedVideoKeys, localVideos.toList()) {
        localVideos.filter { video ->
            val key = prefsRepo.canonicalVideoKey(video.id, video.uriString)
            likedVideoKeys.contains(key) || likedVideoKeys.contains(video.id)
        }
    }

    val displayName = userName.trim().ifBlank {
        if (isArabic) "تسجيل الدخول" else "Sign in"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(aura.background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            aura.ambientPrimary.copy(alpha = if (aura.isDark) 0.24f else 0.14f),
                            aura.ambientSecondary.copy(alpha = if (aura.isDark) 0.12f else 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.42f, size.height * 0.10f),
                        radius = size.width * 0.92f
                    )
                )
            }
            .testTag("my_space_screen")
    ) {
        AuraAtmosphericLayer()
        val bottomBarPad = LocalFloatingBottomBarPadding.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .stableStatusBarsPadding(),
            contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp + bottomBarPad),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "myspace_profile_row") {
                MeeTubeProfileRow(
                    displayName = displayName,
                    cyberAvatarConfig = cyberAvatarConfig,
                    isArabic = isArabic,
                    onAvatarClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToEditProfile()
                    },
                    onCastClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val castActions = listOf(
                            Settings.ACTION_CAST_SETTINGS,
                            "android.settings.WIFI_DISPLAY_SETTINGS",
                            Settings.ACTION_BLUETOOTH_SETTINGS
                        )
                        val opened = castActions.any { action ->
                            val intent = Intent(action).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            runCatching {
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                    true
                                } else {
                                    false
                                }
                            }.getOrDefault(false)
                        }
                        if (!opened) {
                            statusBanner = if (isArabic) {
                                "إعدادات البث اللاسلكي غير متاحة على هذا الجهاز"
                            } else {
                                "Wireless Cast settings unavailable on this device"
                            }
                        }
                    }
                )
            }

            item(key = "myspace_account_settings_pills") {
                MeeTubeAccountSettingsPills(
                    isArabic = isArabic,
                    onAccountsClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showAccountsHubSheet = true
                    },
                    onSettingsClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenSystemSettings()
                    }
                )
            }

            item(key = "myspace_history_row") {
                MeeTubeHistoryHeaderRow(
                    isArabic = isArabic,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showHistorySheet = true
                    }
                )
            }

            item(key = "myspace_library_section") {
                MeeTubeLibrarySection(
                    isArabic = isArabic,
                    onDownloadsClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showOfflineSheet = true
                    },
                    onWatchLaterClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showWatchlistSheet = true
                    },
                    onLikedVideosClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        likedVideoKeys = prefsRepo.getShortLikedVideoKeys()
                        showLikedVideosSheet = true
                    },
                    onPlaylistsClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showPlaylistsSheet = true
                    },
                    onMyVideosClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToWatch()
                    }
                )
            }

            item(key = "myspace_explore_section") {
                MeeTubeExploreSection(
                    isArabic = isArabic,
                    onWatchClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToWatch()
                    },
                    onListenClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToListen()
                    }
                )
            }

            item(key = "myspace_bottom_spacer") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (statusBanner != null) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = aura.dialogBg,
                border = BorderStroke(1.dp, aura.borderSubtle),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .stableStatusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = statusBanner!!,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }
        }

        if (showAccountsHubSheet) {
            MeeTubeAccountsHubSheet(
                userName = displayName,
                streakDays = streakDays,
                shieldsCount = uiState.shieldsCount,
                totalWatchHours = totalWatchHours,
                xpState = xpState,
                currentRank = currentRank,
                currentThemeMode = currentThemeMode,
                currentLanguage = currentLanguage,
                isCheckingForUpdates = isCheckingForUpdates,
                isArabic = isArabic,
                onDismiss = { showAccountsHubSheet = false },
                onEditProfile = onNavigateToEditProfile,
                onOpenRankDialog = { showAccountRankDialog = true },
                onOpenRoadmap = onOpenRoadmap,
                onOpenShieldFarming = onOpenShieldFarming,
                onOpenStatistics = onOpenStatistics,
                onCycleThemeMode = {
                    val next = when (currentThemeMode) {
                        ThemeMode.DARK -> ThemeMode.LIGHT
                        ThemeMode.LIGHT -> ThemeMode.SYSTEM
                        ThemeMode.SYSTEM -> ThemeMode.DARK
                    }
                    onThemeModeChange(next)
                },
                onToggleLanguage = {
                    val next = if (currentLanguage == AppLanguage.ARABIC) {
                        AppLanguage.ENGLISH
                    } else {
                        AppLanguage.ARABIC
                    }
                    onLanguageChange(next)
                },
                onCheckForUpdates = onCheckForUpdates
            )
        }

        if (showPlaylistsSheet) {
            MeeTubePlaylistsSheet(
                bookmarkGroups = bookmarkGroups,
                isArabic = isArabic,
                onDismiss = { showPlaylistsSheet = false },
                onOpenWatch = onNavigateToWatch,
                onOpenListen = onNavigateToListen
            )
        }

        if (showAccountRankDialog) {
            AccountRankAndRedeemDialog(
                streakDays = streakDays,
                currentRank = currentRank,
                earnedTitle = earnedTitle,
                isPremiumActivated = isPremiumActivated,
                isArabic = isArabic,
                onActivatePremiumWithCode = {
                    streakViewModel.setPremiumActivated(true, isArabic = isArabic)
                },
                onDeactivatePremium = {
                    streakViewModel.setPremiumActivated(false, isArabic = isArabic)
                },
                onDismiss = { showAccountRankDialog = false }
            )
        }

        if (showWatchlistSheet) {
            MySpaceWatchlistSheet(
                savedItems = savedMediaItems,
                thumbMap = localThumbMap,
                isArabic = isArabic,
                onPlayMedia = onPlayMedia,
                onRemoveSaved = { item ->
                    streakViewModel.toggleSavedMedia(
                        mediaId = item.id,
                        title = if (isArabic) item.titleArabic ?: item.title else item.title,
                        isArabic = isArabic
                    )
                },
                onDismiss = { showWatchlistSheet = false }
            )
        }

        if (showLikedVideosSheet) {
            MySpaceLikedVideosSheet(
                likedVideos = likedLocalVideos,
                isArabic = isArabic,
                onPlayLocalVideo = { video ->
                    onPlayMedia(video.asHomeMediaItem())
                },
                onUnlikeVideo = { video ->
                    val updated = prefsRepo.setShortLiked(video.id, video.uriString, liked = false)
                    likedVideoKeys = updated
                },
                onDismiss = { showLikedVideosSheet = false }
            )
        }

        if (showHistorySheet) {
            MySpaceHistorySheet(
                history = watchHistory,
                isArabic = isArabic,
                onResumeEntry = { entry ->
                    val matchedCatalog = allCatalog.firstOrNull { it.id == entry.id }
                    val media = matchedCatalog?.copy(initialSeekPositionMs = entry.positionMs) ?: MediaItem(
                        id = entry.id,
                        title = formatCleanTitle(entry.title, true),
                        subtitleBadge = if (entry.uriString != null) "OFFLINE • LOCAL" else "RESUME",
                        posterRes = if (entry.thumbnailRes != 0) entry.thumbnailRes else R.drawable.img_hero_poster,
                        backdropRes = if (entry.thumbnailRes != 0) entry.thumbnailRes else R.drawable.img_hero_poster,
                        uriString = entry.uriString,
                        initialSeekPositionMs = entry.positionMs,
                        progress = entry.progress
                    )
                    onPlayMedia(media)
                },
                onRemoveEntry = { entry ->
                    streakViewModel.removeWatchHistoryItem(entry.id, entry.uriString)
                },
                onClearAll = {
                    streakViewModel.clearWatchHistory(isArabic = isArabic)
                },
                onDismiss = { showHistorySheet = false }
            )
        }

        if (showOfflineSheet) {
            MySpaceOfflineMediaSheet(
                localVideos = localVideos.toList(),
                bookmarkGroups = bookmarkGroups,
                isArabic = isArabic,
                onPlayLocalVideo = { video ->
                    onPlayMedia(video.asHomeMediaItem())
                },
                onPickLocalVideo = {
                    pickLocalVideoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                onOpenWatchHub = onNavigateToWatch,
                onDismiss = { showOfflineSheet = false }
            )
        }
    }
}
