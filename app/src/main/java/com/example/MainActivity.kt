package com.example

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.isActive
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.MediaItem
import com.example.data.update.AppUpdateChecker
import com.example.data.update.AppUpdateInfo
import com.example.data.update.UpdateCheckResult
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.ExoVideoPlayer

import com.example.ui.components.BottomEdgeFadeInGlass
import com.example.ui.components.LocalFloatingBottomBarPadding
import com.example.ui.components.NavDestination
import com.example.ui.components.VideoPulseBottomBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MilestoneRoadmapScreen
import com.example.ui.screens.MySpaceScreen
import com.example.ui.screens.ListenScreen
import com.example.ui.screens.ShieldFarmingScreen
import com.example.ui.screens.ShortScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.SystemSettingsScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.screens.WatchScreen
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalStableStatusBarTop
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StoreTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.aura
import com.example.ui.theme.rememberStableStatusBarTopDp
import com.example.ui.viewmodel.StreakViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            val prefsRepo = remember { com.example.data.preferences.StreakPreferencesRepository.getInstance(applicationContext) }
            val streakViewModel: StreakViewModel = viewModel()
            var currentThemeMode by remember { mutableStateOf(prefsRepo.loadThemeMode()) }
            val activeStoreTheme by streakViewModel.activeStoreTheme.collectAsState()
            var currentLanguage by remember { mutableStateOf(prefsRepo.loadLanguage()) }

            MyApplicationTheme(
                themeMode = currentThemeMode,
                storeTheme = activeStoreTheme
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides (if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr)
                ) {
                    VideoPulseApp(
                        currentThemeMode = currentThemeMode,
                        onThemeModeChange = {
                            currentThemeMode = it
                            prefsRepo.saveThemeMode(it)
                        },
                        currentStoreTheme = activeStoreTheme,
                        onStoreThemeChange = {
                            streakViewModel.equipOwnedStoreTheme(it, currentLanguage == AppLanguage.ARABIC)
                        },
                        currentLanguage = currentLanguage,
                        onLanguageChange = {
                            currentLanguage = it
                            prefsRepo.saveLanguage(it)
                        },
                        streakViewModel = streakViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPulseApp(
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    currentStoreTheme: StoreTheme,
    onStoreThemeChange: (StoreTheme) -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    streakViewModel: StreakViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val streakState by streakViewModel.uiState.collectAsState()
    val isFullScreenMode by streakViewModel.isFullScreenMode.collectAsState()
    val activeThemeTrial by streakViewModel.activeThemeTrial.collectAsState()
    val view = LocalView.current

    val tabBackStack = remember { mutableStateListOf(NavDestination.HOME) }
    val currentDestination = tabBackStack.lastOrNull() ?: NavDestination.HOME
    var activePlayerMedia by remember { mutableStateOf<MediaItem?>(null) }
    var lastPlayerMedia by remember { mutableStateOf<MediaItem?>(null) }
    LaunchedEffect(activePlayerMedia) {
        if (activePlayerMedia != null) {
            lastPlayerMedia = activePlayerMedia
        }
    }
    var isEditingProfile by remember { mutableStateOf(false) }
    var isShieldFarmingOpen by remember { mutableStateOf(false) }
    var isStatisticsOpen by remember { mutableStateOf(false) }
    var isSystemSettingsOpen by remember { mutableStateOf(false) }
    var isVaultOpen by remember { mutableStateOf(false) }
    var isRoadmapOpen by remember { mutableStateOf(false) }
    var isFlameShieldLabsOpen by remember { mutableStateOf(false) }
    var isKeyWorkshopsOpen by remember { mutableStateOf(false) }

    var availableUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isCheckingForUpdates by remember { mutableStateOf(false) }

    val isArabic = currentLanguage == AppLanguage.ARABIC

    // Automatic in-app update check against GitHub releases on launch
    LaunchedEffect(Unit) {
        delay(1500L)
        val result = AppUpdateChecker.checkForUpdates()
        if (result is UpdateCheckResult.UpdateAvailable) {
            availableUpdate = result.updateInfo
            showUpdateDialog = true
        }
    }

    val onCheckForUpdates: () -> Unit = {
        if (!isCheckingForUpdates) {
            scope.launch {
                isCheckingForUpdates = true
                when (val result = AppUpdateChecker.checkForUpdates()) {
                    is UpdateCheckResult.UpdateAvailable -> {
                        availableUpdate = result.updateInfo
                        showUpdateDialog = true
                    }
                    is UpdateCheckResult.UpToDate -> {
                        snackbarHostState.showSnackbar(
                            if (isArabic) "أنت تستخدم أحدث إصدار بالفعل ✓"
                            else "You are already on the latest version ✓"
                        )
                    }
                    is UpdateCheckResult.Error -> {
                        snackbarHostState.showSnackbar(
                            if (isArabic) "تعذر التحقق من التحديثات، تحقق من الاتصال"
                            else "Unable to check for updates. Check connection."
                        )
                    }
                }
                isCheckingForUpdates = false
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var isAppInForeground by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAppInForeground = true
            } else if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                isAppInForeground = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val configuration = LocalConfiguration.current
    val aura = MaterialTheme.aura

    // Apply Full Screen mode (hide/show top phone status bar) without resizing/shrinking window
    // and ensure backgrounds & system bar icons adapt to active Light/Dark theme
    LaunchedEffect(isFullScreenMode, isAppInForeground, configuration.orientation, aura.isDark, activePlayerMedia, currentDestination, isRoadmapOpen) {
        val window = context.findActivity()?.window ?: return@LaunchedEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val controller = WindowCompat.getInsetsController(window, view)
        val useLightSystemBars = !aura.isDark && activePlayerMedia == null && (currentDestination != NavDestination.SHORT || isRoadmapOpen)
        controller.isAppearanceLightStatusBars = useLightSystemBars
        controller.isAppearanceLightNavigationBars = useLightSystemBars
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (isFullScreenMode) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val cachedStatusBarDp = remember { streakViewModel.getLastStatusBarHeightDp().dp }
    val stableStatusBarTop = rememberStableStatusBarTopDp(
        isFullScreenMode = isFullScreenMode,
        cachedPortraitHeightDp = cachedStatusBarDp
    )
    LaunchedEffect(stableStatusBarTop, isPortrait) {
        if (isPortrait && stableStatusBarTop > 0.dp) {
            streakViewModel.saveLastStatusBarHeightDp(stableStatusBarTop.value)
        }
    }

    // Active foreground app usage tracker + 1-minute free store theme trial countdown:
    // Core Rules: Background usage does NOT count. Awards 10 shields per 1 full hour.
    LaunchedEffect(isAppInForeground, isArabic) {
        if (isAppInForeground) {
            streakViewModel.tickThemeTrialTimer(isArabic = isArabic)
            while (isActive) {
                delay(1000L)
                if (isAppInForeground) {
                    streakViewModel.tickActiveForegroundUsage(1)
                    streakViewModel.tickThemeTrialTimer(isArabic = isArabic)
                }
            }
        }
    }

    // Android System Back Navigation Handling (always 1 step back across overlays and tab history)
    BackHandler(
        enabled = showUpdateDialog || activePlayerMedia != null || isFlameShieldLabsOpen || isKeyWorkshopsOpen || isRoadmapOpen || isVaultOpen || isSystemSettingsOpen || isStatisticsOpen || isShieldFarmingOpen || isEditingProfile || tabBackStack.size > 1
    ) {
        when {
            showUpdateDialog -> showUpdateDialog = false
            activePlayerMedia != null -> activePlayerMedia = null
            isFlameShieldLabsOpen -> isFlameShieldLabsOpen = false
            isKeyWorkshopsOpen -> isKeyWorkshopsOpen = false
            isRoadmapOpen -> isRoadmapOpen = false
            isVaultOpen -> isVaultOpen = false
            isSystemSettingsOpen -> isSystemSettingsOpen = false
            isStatisticsOpen -> isStatisticsOpen = false
            isShieldFarmingOpen -> isShieldFarmingOpen = false
            isEditingProfile -> isEditingProfile = false
            tabBackStack.size > 1 -> tabBackStack.removeAt(tabBackStack.lastIndex)
        }
    }

    var isWatchBottomBarHidden by remember { mutableStateOf(false) }
    var bottomBarHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val fallbackBottomBarDp = 80.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarHeightDp = if (bottomBarHeightPx > 0) {
        with(density) { bottomBarHeightPx.toDp() }
    } else {
        fallbackBottomBarDp
    }

    LaunchedEffect(currentDestination) {
        if (currentDestination != NavDestination.WATCH) {
            isWatchBottomBarHidden = false
        }
    }

    val shouldHideBottomBar = currentDestination == NavDestination.LISTEN ||
        (isWatchBottomBarHidden && currentDestination == NavDestination.WATCH)
    val bottomBarHideProgress by animateFloatAsState(
        targetValue = if (shouldHideBottomBar) 1f else 0f,
        animationSpec = tween(
            durationMillis = 360,
            easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
        ),
        label = "watch_bottom_bar_hide_progress"
    )

    CompositionLocalProvider(
        LocalStableStatusBarTop provides stableStatusBarTop,
        LocalFloatingBottomBarPadding provides bottomBarHeightDp
    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(aura.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            val tabStateHolder = rememberSaveableStateHolder()

            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    (fadeIn(
                        animationSpec = tween(
                            durationMillis = 190,
                            delayMillis = 70,
                            easing = FastOutSlowInEasing
                        )
                    ) + scaleIn(
                        initialScale = 0.98f,
                        animationSpec = tween(
                            durationMillis = 210,
                            delayMillis = 70,
                            easing = FastOutSlowInEasing
                        )
                    )) togetherWith (fadeOut(
                        animationSpec = tween(
                            durationMillis = 70,
                            easing = FastOutLinearInEasing
                        )
                    ) + scaleOut(
                        targetScale = 0.985f,
                        animationSpec = tween(
                            durationMillis = 70,
                            easing = FastOutLinearInEasing
                        )
                    )) using SizeTransform(clip = true)
                },
                modifier = Modifier.fillMaxSize(),
                label = "tab_navigation_transition"
            ) { destination ->
                tabStateHolder.SaveableStateProvider(destination) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(aura.background)
                    ) {
                        when (destination) {
                            NavDestination.HOME -> {
                                HomeScreen(
                                    streakViewModel = streakViewModel,
                                    onNavigateToRoadmap = {
                                        isRoadmapOpen = true
                                    },
                                    onNavigateToVault = {
                                        isVaultOpen = true
                                    },
                                    onPlayMedia = { media ->
                                        activePlayerMedia = media
                                    },
                                    onNavigateToWatch = {
                                        tabBackStack.remove(NavDestination.WATCH)
                                        tabBackStack.add(NavDestination.WATCH)
                                    },
                                    onOpenFlameShieldLabs = {
                                        isFlameShieldLabsOpen = true
                                    },
                                    isArabic = isArabic
                                )
                            }

                            NavDestination.WATCH -> {
                                WatchScreen(
                                    onPlayMedia = { media ->
                                        activePlayerMedia = media
                                    },
                                    isArabic = isArabic,
                                    streakViewModel = streakViewModel,
                                    bottomBarPadding = bottomBarHeightDp,
                                    onBottomBarHiddenChange = { hidden ->
                                        isWatchBottomBarHidden = hidden
                                    }
                                )
                            }

                            NavDestination.LISTEN -> {
                                // Tab 2: Listen Screen (Music & Audio Local Hub)
                                ListenScreen(
                                    isArabic = isArabic,
                                    onBack = {
                                        if (tabBackStack.size > 1) {
                                            tabBackStack.removeAt(tabBackStack.lastIndex)
                                        } else {
                                            tabBackStack.clear()
                                            tabBackStack.add(NavDestination.HOME)
                                        }
                                    }
                                )
                            }

                            NavDestination.SHORT -> {
                                val isShortActive = currentDestination == NavDestination.SHORT &&
                                    activePlayerMedia == null &&
                                    !isRoadmapOpen &&
                                    !isVaultOpen &&
                                    !isSystemSettingsOpen &&
                                    !isStatisticsOpen &&
                                    !isShieldFarmingOpen &&
                                    !isEditingProfile &&
                                    !isFlameShieldLabsOpen &&
                                    !isKeyWorkshopsOpen
                                ShortScreen(
                                    isActiveTab = isShortActive,
                                    streakViewModel = streakViewModel,
                                    isArabic = isArabic,
                                    bottomBarPadding = bottomBarHeightDp,
                                    onShowMessage = { msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                )
                            }

                            NavDestination.MY_SPACE -> {
                                // Tab 4: "My Space" Screen
                                MySpaceScreen(
                                    streakViewModel = streakViewModel,
                                    currentThemeMode = currentThemeMode,
                                    onThemeModeChange = onThemeModeChange,
                                    currentLanguage = currentLanguage,
                                    onLanguageChange = onLanguageChange,
                                    onOpenRoadmap = {
                                        isRoadmapOpen = true
                                    },
                                    onNavigateToEditProfile = {
                                        isEditingProfile = true
                                    },
                                    onOpenShieldFarming = {
                                        isShieldFarmingOpen = true
                                    },
                                    onOpenStatistics = {
                                        isStatisticsOpen = true
                                    },
                                    onOpenSystemSettings = {
                                        isSystemSettingsOpen = true
                                    },
                                    onCheckForUpdates = onCheckForUpdates,
                                    isCheckingForUpdates = isCheckingForUpdates,
                                    onPlayMedia = { media ->
                                        activePlayerMedia = media
                                    },
                                    onNavigateToWatch = {
                                        tabBackStack.remove(NavDestination.WATCH)
                                        tabBackStack.add(NavDestination.WATCH)
                                    },
                                    onNavigateToListen = {
                                        tabBackStack.remove(NavDestination.LISTEN)
                                        tabBackStack.add(NavDestination.LISTEN)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            val bottomFadeHeight = (bottomBarHeightDp - 18.dp).coerceAtLeast(62.dp)
            val bottomFadeGlassAlpha by animateFloatAsState(
                targetValue = if (currentDestination == NavDestination.WATCH) {
                    (1f - bottomBarHideProgress).coerceIn(0f, 1f)
                } else {
                    1f
                },
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "bottom_fade_glass_alpha"
            )
            if (currentDestination != NavDestination.LISTEN && bottomFadeGlassAlpha > 0.01f) {
                BottomEdgeFadeInGlass(
                    height = bottomFadeHeight,
                    baseBackgroundColor = if (currentDestination == NavDestination.SHORT) Color.Black else aura.background,
                    frostedSurfaceColor = if (currentDestination == NavDestination.SHORT) Color(0xFF141418) else aura.surfaceElevated,
                    isDark = if (currentDestination == NavDestination.SHORT) true else aura.isDark,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            alpha = bottomFadeGlassAlpha
                        }
                )
            }

            // Overlaid bottom navigation bar; smoothly slides away on Listen and during Watch scrolling
            if (bottomBarHideProgress < 0.99f) {
                VideoPulseBottomBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = { dest ->
                        if (dest != currentDestination) {
                            if (dest == NavDestination.HOME) {
                                tabBackStack.clear()
                                tabBackStack.add(NavDestination.HOME)
                            } else {
                                tabBackStack.remove(dest)
                                tabBackStack.add(dest)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged { size ->
                            if (size.height > 0 && bottomBarHeightPx != size.height) {
                                bottomBarHeightPx = size.height
                            }
                        }
                        .graphicsLayer {
                            translationY = bottomBarHideProgress * (size.height + 32f)
                            alpha = (1f - bottomBarHideProgress * 1.15f).coerceIn(0f, 1f)
                        }
                )
            }
        }

        val overlayDir = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
        val overlayEnterEasing = remember { CubicBezierEasing(0.22f, 1f, 0.36f, 1f) }
        val overlayExitEasing = remember { CubicBezierEasing(0.4f, 0f, 1f, 1f) }
        val overlayEnterSpec = remember(overlayDir) {
            slideInHorizontally(
                animationSpec = tween(290, easing = overlayEnterEasing),
                initialOffsetX = { it * overlayDir }
            )
        }
        val overlayExitSpec = remember(overlayDir) {
            slideOutHorizontally(
                animationSpec = tween(240, easing = overlayExitEasing),
                targetOffsetX = { it * overlayDir }
            )
        }

        // Edit Profile Screen Overlay
        AnimatedVisibility(
            visible = isEditingProfile,
            enter = overlayEnterSpec,
            exit = overlayExitSpec
        ) {
            com.example.ui.screens.EditProfileScreen(
                streakViewModel = streakViewModel,
                onNavigateBack = {
                    isEditingProfile = false
                },
                isArabic = isArabic
            )
        }

        // Shield Farming Screen Overlay (opened from Profile Shields tile)
        AnimatedVisibility(
            visible = isShieldFarmingOpen,
            enter = overlayEnterSpec,
            exit = overlayExitSpec
        ) {
            ShieldFarmingScreen(
                streakViewModel = streakViewModel,
                onBack = {
                    isShieldFarmingOpen = false
                },
                isArabic = isArabic
            )
        }

        // Statistics Screen Overlay (opened from Profile Statistics tile)
        AnimatedVisibility(
            visible = isStatisticsOpen,
            enter = overlayEnterSpec,
            exit = overlayExitSpec
        ) {
            StatisticsScreen(
                streakViewModel = streakViewModel,
                onBack = {
                    isStatisticsOpen = false
                },
                isArabic = isArabic
            )
        }

        // System Settings Screen Overlay (opened from My Space System Settings card)
        AnimatedVisibility(
            visible = isSystemSettingsOpen,
            enter = overlayEnterSpec,
            exit = overlayExitSpec
        ) {
            SystemSettingsScreen(
                streakViewModel = streakViewModel,
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange,
                onBack = {
                    isSystemSettingsOpen = false
                },
                isArabic = isArabic
            )
        }


        // Dedicated 2-Lab Flame Shield Chamber (opened from The Flame Shield card or Home pill)
        AnimatedVisibility(
            visible = isFlameShieldLabsOpen,
            enter = overlayEnterSpec,
            exit = overlayExitSpec
        ) {
            com.example.ui.screens.FlameShieldLabsScreen(
                streakViewModel = streakViewModel,
                onBack = {
                    isFlameShieldLabsOpen = false
                },
                isArabic = isArabic
            )
        }

        // Dedicated 4-Workshop Key Smelting Foundry (opened from Key Workshops card inside THE FORGE)
        AnimatedVisibility(
            visible = isKeyWorkshopsOpen,
            enter = overlayEnterSpec,
            exit = overlayExitSpec
        ) {
            com.example.ui.screens.KeyWorkshopsScreen(
                streakViewModel = streakViewModel,
                onBack = {
                    isKeyWorkshopsOpen = false
                },
                isArabic = isArabic
            )
        }

        // Vault Screen (opened via 1-second long press on the Home flame)
        AnimatedVisibility(
            visible = isVaultOpen,
            enter = overlayEnterSpec,
            exit = overlayExitSpec
        ) {
            VaultScreen(
                onBack = {
                    isVaultOpen = false
                },
                isArabic = isArabic
            )
        }

        // Milestone Roadmap Screen Overlay (opened from Home or My Space)
        AnimatedVisibility(
            visible = isRoadmapOpen,
            enter = overlayEnterSpec,
            exit = overlayExitSpec
        ) {
            MilestoneRoadmapScreen(
                streakViewModel = streakViewModel,
                streakDays = streakState.streakDays,
                shieldsCount = streakState.shieldsCount,
                maxShields = streakState.maxShields,
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
                currentStoreTheme = currentStoreTheme,
                onStoreThemeChange = onStoreThemeChange,
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange,
                onBack = {
                    isRoadmapOpen = false
                },
                onOpenFlameShieldLabs = {
                    isFlameShieldLabsOpen = true
                },
                onOpenKeyWorkshops = {
                    isKeyWorkshopsOpen = true
                },
                isArabic = isArabic
            )
        }

        // Full-screen ExoPlayer playback component
        AnimatedVisibility(
            visible = activePlayerMedia != null,
            enter = fadeIn(tween(240, easing = overlayEnterEasing)) +
                scaleIn(initialScale = 0.94f, animationSpec = tween(260, easing = overlayEnterEasing)),
            exit = fadeOut(tween(200, easing = overlayExitEasing)) +
                scaleOut(targetScale = 0.94f, animationSpec = tween(200, easing = overlayExitEasing))
        ) {
            val playerMedia = activePlayerMedia ?: lastPlayerMedia
            if (playerMedia != null) {
                ExoVideoPlayer(
                    mediaItem = playerMedia,
                    streakViewModel = streakViewModel,
                    onClose = {
                        activePlayerMedia = null
                    }
                )
            }
        }

        // Compact floating App Update Modal (never full-screen; downloads & installs fully in-app)
        if (showUpdateDialog && availableUpdate != null) {
            AppUpdateDialog(
                updateInfo = availableUpdate!!,
                isArabic = isArabic,
                onDismiss = {
                    showUpdateDialog = false
                }
            )
        }



        // Compact live indicator when a free 1-minute theme trial is running across the app
        val runningTrial = activeThemeTrial
        if (runningTrial != null && activePlayerMedia == null) {
            val secText = runningTrial.remainingSeconds.toString().padStart(2, '0')
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(50),
                color = aura.surfaceElevated.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.55f)),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) {
                            "⏱️ تجربة مجانية: ${runningTrial.theme.displayName} • 00:$secText"
                        } else {
                            "⏱️ Free Trial: ${runningTrial.theme.displayName} • 00:$secText"
                        },
                        color = aura.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Global Snackbar for feedback
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
