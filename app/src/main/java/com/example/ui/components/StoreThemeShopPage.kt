package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AmbientBlackBlurMode
import com.example.data.local.CyberAvatarConfig
import com.example.data.model.ForgeKeyTier
import com.example.data.model.ForgeUiState
import com.example.data.model.HomeMimicItem
import com.example.data.model.PlayerSeekbarStyle
import com.example.data.model.StoreAvatarItem
import com.example.data.model.StoreItemRarity
import com.example.data.preferences.StreakPreferencesRepository
import com.example.ui.theme.AuraPalette
import com.example.ui.theme.StoreTheme
import com.example.ui.theme.StoreThemeCategory
import com.example.ui.theme.aura
import com.example.ui.theme.auraCardBorderBrush
import com.example.ui.theme.auraPaletteFor
import com.example.ui.viewmodel.ThemeTrialState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private enum class ShopCategoryDestination {
    HUB,
    THEMES,
    AVATARS,
    SEEKBARE,
    MIMIC,
    AMBIENT_VIDEO_FILL
}

@Composable
fun StoreThemeShopPage(
    shieldsCount: Int,
    forgeState: ForgeUiState,
    currentStoreTheme: StoreTheme,
    purchasedThemeIds: Set<String>,
    activeThemeTrial: ThemeTrialState?,
    onEquipOwnedTheme: (StoreTheme) -> Unit,
    onPurchaseTheme: (StoreTheme) -> Unit,
    onStartTrialTheme: (StoreTheme) -> Unit,
    isArabic: Boolean,
    onBack: () -> Unit = {},
    showHubBackButton: Boolean = true,
    isWaveformSeekbarPurchased: Boolean = false,
    isWaveformSeekbarEnabled: Boolean = false,
    isShootingStarSeekbarPurchased: Boolean = false,
    isAngrySnakeSeekbarPurchased: Boolean = false,
    purchasedSeekbarIds: Set<String> = emptySet(),
    activeSeekbarStyle: PlayerSeekbarStyle = PlayerSeekbarStyle.CLASSIC,
    onPurchaseWaveformSeekbar: () -> Unit = {},
    onToggleWaveformSeekbar: (Boolean) -> Unit = {},
    onPurchaseSeekbarStyle: (PlayerSeekbarStyle) -> Unit = {},
    onSelectSeekbarStyle: (PlayerSeekbarStyle) -> Unit = {},
    streakDays: Int = 0,
    isMimicFlameEnabled: Boolean = true,
    isMimicDnaPurchased: Boolean = false,
    isMimicDnaEnabled: Boolean = false,
    equippedHomeMimic: HomeMimicItem = HomeMimicItem.STREAK_FLAME,
    isHomeMimicVisible: Boolean = true,
    dailyWatchProgress: Float = 0f,
    isPlayerActive: Boolean = false,
    purchasedMimicIds: Set<String> = emptySet(),
    onPurchaseHomeMimic: (HomeMimicItem) -> Unit = {},
    onEquipHomeMimic: (HomeMimicItem) -> Unit = {},
    onToggleHomeMimicVisible: (Boolean) -> Unit = {},
    onToggleMimicFlame: (Boolean) -> Unit = {},
    onPurchaseMimicDna: () -> Unit = {},
    onToggleMimicDna: (Boolean) -> Unit = {},
    purchasedAmbientModeIds: Set<String> = emptySet(),
    activeAmbientMode: AmbientBlackBlurMode = AmbientBlackBlurMode.DEEP_OBSIDIAN_BLUR,
    isAmbientGlowEnabled: Boolean = true,
    onPurchaseAmbientMode: (AmbientBlackBlurMode) -> Unit = {},
    onSelectAmbientMode: (AmbientBlackBlurMode) -> Unit = {},
    onToggleAmbientGlow: (Boolean) -> Unit = {},
    cyberAvatarConfig: CyberAvatarConfig = CyberAvatarConfig(),
    purchasedAvatarIds: Set<String> = emptySet(),
    onPurchaseAvatar: (StoreAvatarItem) -> Unit = {},
    onEquipAvatar: (StoreAvatarItem) -> Unit = {},
    onUnequipAvatar: () -> Unit = {},
    onToggleAvatarAuraFrame: (Boolean) -> Unit = {},
    isPremiumActivated: Boolean = false,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val totalKeys = forgeState.bronzeKeys + forgeState.silverKeys + forgeState.goldenKeys + forgeState.diamondKeys
    val effectiveActiveSeekbar = if (activeSeekbarStyle != PlayerSeekbarStyle.CLASSIC) {
        activeSeekbarStyle
    } else if (isWaveformSeekbarEnabled) {
        PlayerSeekbarStyle.WAVEFORM
    } else {
        PlayerSeekbarStyle.CLASSIC
    }
    val availableSeekbarsCount = PlayerSeekbarStyle.entries.count { style ->
        style != PlayerSeekbarStyle.CLASSIC && !when (style) {
            PlayerSeekbarStyle.WAVEFORM -> isWaveformSeekbarPurchased || purchasedSeekbarIds.contains(style.id)
            PlayerSeekbarStyle.SHOOTING_STAR -> isShootingStarSeekbarPurchased || purchasedSeekbarIds.contains(style.id)
            PlayerSeekbarStyle.ANGRY_SNAKE -> isAngrySnakeSeekbarPurchased || purchasedSeekbarIds.contains(style.id)
            else -> purchasedSeekbarIds.contains(style.id)
        }
    }
    val activeMimicsCount = if (isHomeMimicVisible || isMimicFlameEnabled || isMimicDnaEnabled) 1 else 0
    val unlockedAmbientCount = AmbientBlackBlurMode.entries.count {
        it.isUnlockedByDefault || purchasedAmbientModeIds.contains(it.name) || isPremiumActivated
    }
    val unlockedAvatarSet = remember(purchasedAvatarIds, cyberAvatarConfig.unlockedAvatarIds) {
        purchasedAvatarIds + cyberAvatarConfig.unlockedAvatarIds
    }
    val unlockedAvatarsCount = StoreAvatarItem.entries.count {
        unlockedAvatarSet.contains(it.id) || isPremiumActivated
    }

    var shopDestination by remember { mutableStateOf(ShopCategoryDestination.HUB) }
    var selectedCategory by remember { mutableStateOf<StoreThemeCategory?>(null) }
    var inspectedTheme by remember { mutableStateOf<StoreTheme?>(null) }

    BackHandler(enabled = shopDestination != ShopCategoryDestination.HUB) {
        shopDestination = ShopCategoryDestination.HUB
    }

    // Purchased themes automatically move to Multi-Theme Engine and vanish from the Store!
    val unownedThemes = remember(purchasedThemeIds) {
        StoreTheme.storeCatalog.filter { !it.isUnlockedByDefault && !purchasedThemeIds.contains(it.id) }
    }
    val displayedThemes = remember(unownedThemes, selectedCategory) {
        if (selectedCategory == null) {
            unownedThemes
        } else {
            unownedThemes.filter { it.category == selectedCategory }
        }
    }
    val themeRows = remember(displayedThemes) { displayedThemes.chunked(2) }

    val fxTransition = rememberInfiniteTransition(label = "shop_category_live_fx")
    val animPhase by fxTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shop_fx_phase"
    )
    val pulsePhase by fxTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shop_pulse_phase"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("store_themes_shop_screen")
    ) {
        // Top Navigation Bar (adapts dynamically to Hub vs Themes vs Avatar/Seekbare/Mimic)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = aura.surfaceElevated,
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(aura.accentGold.copy(alpha = 0.55f), aura.accentCyan.copy(alpha = 0.55f))
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (shopDestination == ShopCategoryDestination.HUB && !showHubBackButton) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(aura.accentGoldBg)
                                .border(
                                    1.dp,
                                    Brush.linearGradient(listOf(aura.accentGold, aura.accentCyan)),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = if (isArabic) "المتجر" else "The Shop",
                                tint = aura.accentGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (shopDestination == ShopCategoryDestination.HUB) {
                                    onBack()
                                } else {
                                    shopDestination = ShopCategoryDestination.HUB
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(aura.iconButtonBg)
                                .testTag("shop_exit_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (isArabic) "رجوع" else "Back",
                                tint = aura.textPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when (shopDestination) {
                                ShopCategoryDestination.HUB -> "Shop"
                                ShopCategoryDestination.THEMES -> "Themes"
                                ShopCategoryDestination.AVATARS -> "Avatars"
                                ShopCategoryDestination.SEEKBARE -> "Seekbar"
                                ShopCategoryDestination.MIMIC -> "Mimic"
                                ShopCategoryDestination.AMBIENT_VIDEO_FILL -> "Ambient Video Fill"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = aura.accentGold,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = when (shopDestination) {
                                ShopCategoryDestination.HUB -> if (isArabic) "أقسام المتجر والتخصيص" else "Store Categories & Upgrades"
                                ShopCategoryDestination.THEMES -> if (isArabic) "متجر الثيمات والمفاتيح" else "Themes & Keys Store"
                                ShopCategoryDestination.AVATARS -> if (isArabic) "10 أفاتار سيبرانية وإطارات هولوغرافية (60 FPS)" else "10 Cyberpunk Avatars & Holographic Aura Frames"
                                ShopCategoryDestination.SEEKBARE -> if (isArabic) "8 أشرطة تمرير (زجاجية، موجات، ليزر، أفعى)" else "8 Seekbars (Liquid Glass, Waveform, Laser)"
                                ShopCategoryDestination.MIMIC -> if (isArabic) "مجسمات الصفحة الرئيسية" else "Home Mimic Centerpieces"
                                ShopCategoryDestination.AMBIENT_VIDEO_FILL -> if (isArabic) "10 مؤثرات تمويه سينمائي" else "10 Cinema Black Blur Modes"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.textSecondary,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = aura.accentGoldBg,
                    border = BorderStroke(1.dp, aura.accentGold.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (shopDestination) {
                                ShopCategoryDestination.HUB -> Icons.Default.AutoAwesome
                                ShopCategoryDestination.THEMES -> Icons.Default.Palette
                                ShopCategoryDestination.AVATARS -> Icons.Default.Person
                                ShopCategoryDestination.SEEKBARE -> Icons.Default.Tune
                                ShopCategoryDestination.MIMIC -> Icons.Default.AutoFixHigh
                                ShopCategoryDestination.AMBIENT_VIDEO_FILL -> Icons.Default.BlurOn
                            },
                            contentDescription = null,
                            tint = aura.accentGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (shopDestination) {
                                ShopCategoryDestination.HUB -> if (isArabic) "5 أقسام" else "5 Sections"
                                ShopCategoryDestination.THEMES -> if (isPremiumActivated) (if (isArabic) "مجاني 👑" else "FREE 👑") else (if (isArabic) "${unownedThemes.size} متاح" else "${unownedThemes.size} Left")
                                ShopCategoryDestination.AVATARS -> when {
                                    cyberAvatarConfig.equippedAvatar != null -> if (isArabic) "مُجهّز ✓" else "Equipped ✓"
                                    isPremiumActivated -> if (isArabic) "مجاني 👑" else "FREE 👑"
                                    else -> "$unlockedAvatarsCount/10"
                                }
                                ShopCategoryDestination.SEEKBARE -> when {
                                    effectiveActiveSeekbar != PlayerSeekbarStyle.CLASSIC -> if (isArabic) "مُفعّل ✓" else "Active ✓"
                                    availableSeekbarsCount == 0 -> if (isArabic) "مملوك ✓" else "Owned ✓"
                                    isPremiumActivated -> if (isArabic) "مجاني 👑" else "FREE 👑"
                                    else -> if (isArabic) "$availableSeekbarsCount متاح" else "$availableSeekbarsCount Avail"
                                }
                                ShopCategoryDestination.MIMIC -> when {
                                    activeMimicsCount > 0 -> if (isArabic) "$activeMimicsCount مُجهّز ✓" else "$activeMimicsCount Equipped ✓"
                                    isPremiumActivated -> if (isArabic) "مجاني 👑" else "FREE 👑"
                                    else -> if (isArabic) "معطّل" else "Disabled"
                                }
                                ShopCategoryDestination.AMBIENT_VIDEO_FILL -> when {
                                    isPremiumActivated -> if (isArabic) "مجاني 👑" else "FREE 👑"
                                    else -> "$unlockedAmbientCount/10"
                                }
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.accentGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }
            }
        }

        val shopDir = if (androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl) -1 else 1
        val shopEnterEasing = remember { CubicBezierEasing(0.22f, 1f, 0.36f, 1f) }
        val shopExitEasing = remember { CubicBezierEasing(0.4f, 0f, 1f, 1f) }

        AnimatedContent(
            targetState = shopDestination,
            transitionSpec = {
                val enteringCategory = initialState == ShopCategoryDestination.HUB && targetState != ShopCategoryDestination.HUB
                if (enteringCategory) {
                    ContentTransform(
                        targetContentEnter = slideInHorizontally(
                            animationSpec = tween(290, easing = shopEnterEasing),
                            initialOffsetX = { it * shopDir }
                        ),
                        initialContentExit = slideOutHorizontally(
                            animationSpec = tween(290, easing = shopEnterEasing),
                            targetOffsetX = { -(it * 0.22f * shopDir).toInt() }
                        ),
                        targetContentZIndex = 1f,
                        sizeTransform = SizeTransform(clip = true)
                    )
                } else {
                    ContentTransform(
                        targetContentEnter = slideInHorizontally(
                            animationSpec = tween(260, easing = shopEnterEasing),
                            initialOffsetX = { -(it * 0.22f * shopDir).toInt() }
                        ),
                        initialContentExit = slideOutHorizontally(
                            animationSpec = tween(250, easing = shopExitEasing),
                            targetOffsetX = { it * shopDir }
                        ),
                        targetContentZIndex = -1f,
                        sizeTransform = SizeTransform(clip = true)
                    )
                }
            },
            label = "shop_section_transition",
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) { dest ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(aura.background)
            ) {
                when (dest) {
                ShopCategoryDestination.HUB -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("shop_categories_hub"),
                        contentPadding = PaddingValues(bottom = 24.dp + LocalFloatingBottomBarPadding.current),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Keys & Shields Balance Card
                        item(key = "hub_keys_balance") {
                            ShopBalanceSummaryCard(
                                totalKeys = totalKeys,
                                shieldsCount = shieldsCount,
                                forgeState = forgeState,
                                isArabic = isArabic
                            )
                        }

                        // Live 1-Minute Trial Countdown Banner if running
                        if (activeThemeTrial != null) {
                            item(key = "hub_trial_banner") {
                                ActiveTrialBannerCard(activeThemeTrial = activeThemeTrial, isArabic = isArabic)
                            }
                        }

                        // Row 1: Themes + Avatar (2 per Row with distinct live visual FX)
                        item(key = "shop_cat_row_1") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ShopCategoryEffectTile(
                                    title = "Themes",
                                    subtitle = if (isArabic) "ثيمات الواجهة والألوان" else "UI Themes & Glass",
                                    badgeText = if (isPremiumActivated) (if (isArabic) "مجاني 👑" else "FREE 👑") else (if (isArabic) "${unownedThemes.size} متاح" else "${unownedThemes.size} Avail"),
                                    badgeColor = if (isPremiumActivated) aura.accentGold else aura.accentCyan,
                                    accentPrimary = aura.accentCyan,
                                    accentSecondary = aura.accentViolet,
                                    effectType = ShopTileEffectType.THEMES,
                                    animPhase = animPhase,
                                    pulsePhase = pulsePhase,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        shopDestination = ShopCategoryDestination.THEMES
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("shop_category_themes")
                                )

                                ShopCategoryEffectTile(
                                    title = "Avatars",
                                    subtitle = if (isArabic) "10 أفاتار سيبرانية وإطارات هالة" else "10 Cyberpunk Avatars & Frames",
                                    badgeText = when {
                                        cyberAvatarConfig.equippedAvatar != null -> if (isArabic) "مُجهّز ✓" else "Equipped ✓"
                                        isPremiumActivated -> if (isArabic) "مجاني 👑 • 10" else "FREE 👑 • 10"
                                        else -> "$unlockedAvatarsCount/10"
                                    },
                                    badgeColor = when {
                                        cyberAvatarConfig.equippedAvatar != null -> aura.accentEmerald
                                        isPremiumActivated -> aura.accentGold
                                        else -> aura.accentViolet
                                    },
                                    accentPrimary = aura.accentViolet,
                                    accentSecondary = aura.accentCyan,
                                    effectType = ShopTileEffectType.AVATAR,
                                    animPhase = animPhase,
                                    pulsePhase = pulsePhase,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        shopDestination = ShopCategoryDestination.AVATARS
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("shop_category_avatar")
                                )
                            }
                        }

                        // Row 2: Seekbare + Mimic (2 per Row with distinct live visual FX)
                        item(key = "shop_cat_row_2") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ShopCategoryEffectTile(
                                    title = "Seekbar",
                                    subtitle = if (isArabic) "5 زجاج سائل + موجة وشهاب وأفعى" else "5 Liquid Glass + Wave & Star",
                                    badgeText = when {
                                        effectiveActiveSeekbar != PlayerSeekbarStyle.CLASSIC -> if (isArabic) "مُفعّل ✓" else "Active ✓"
                                        availableSeekbarsCount == 0 -> if (isArabic) "مملوك ✓" else "Owned ✓"
                                        isPremiumActivated -> if (isArabic) "مجاني 👑" else "FREE 👑"
                                        else -> if (isArabic) "$availableSeekbarsCount متاح" else "$availableSeekbarsCount Avail"
                                    },
                                    badgeColor = if (isPremiumActivated && effectiveActiveSeekbar == PlayerSeekbarStyle.CLASSIC) aura.accentGold else aura.accentEmerald,
                                    accentPrimary = aura.accentEmerald,
                                    accentSecondary = aura.accentCyan,
                                    effectType = ShopTileEffectType.SEEKBARE,
                                    animPhase = animPhase,
                                    pulsePhase = pulsePhase,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        shopDestination = ShopCategoryDestination.SEEKBARE
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("shop_category_seekbare")
                                )

                                ShopCategoryEffectTile(
                                    title = "Mimic",
                                    subtitle = if (isArabic) "الشعلة، DNA و 10 مجسمات نيون" else "Flame, DNA & 10 Neon Cores",
                                    badgeText = when {
                                        activeMimicsCount > 0 -> if (isArabic) "مُجهّز ✓" else "Active ✓"
                                        isPremiumActivated && !isMimicDnaPurchased -> if (isArabic) "مجاني 👑" else "FREE 👑"
                                        else -> if (isArabic) "12 عنصر" else "12 Items"
                                    },
                                    badgeColor = if (activeMimicsCount > 0) aura.accentEmerald else if (isPremiumActivated) aura.accentGold else aura.accentFlame,
                                    accentPrimary = aura.accentFlame,
                                    accentSecondary = aura.accentCyan,
                                    effectType = ShopTileEffectType.MIMIC,
                                    animPhase = animPhase,
                                    pulsePhase = pulsePhase,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        shopDestination = ShopCategoryDestination.MIMIC
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("shop_category_mimic")
                                )
                            }
                        }

                        // Row 3: Ambient Video Fill (10 Cinema Blur & Fade Effects)
                        item(key = "shop_cat_row_3") {
                            ShopCategoryEffectTile(
                                title = "Ambient Video Fill",
                                subtitle = if (isArabic) "10 تأثيرات تمويه + أشرطة التمرير" else "10 Cinema Blurs + Seekbars",
                                badgeText = when {
                                    isPremiumActivated -> if (isArabic) "مجاني 👑 • 10" else "FREE 👑 • 10"
                                    else -> "$unlockedAmbientCount/10"
                                },
                                badgeColor = if (isPremiumActivated) aura.accentGold else aura.accentCyan,
                                accentPrimary = aura.accentGold,
                                accentSecondary = aura.accentViolet,
                                effectType = ShopTileEffectType.AMBIENT_FILL,
                                animPhase = animPhase,
                                pulsePhase = pulsePhase,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    shopDestination = ShopCategoryDestination.AMBIENT_VIDEO_FILL
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("shop_category_ambient_fill")
                            )
                        }
                    }
                }

                ShopCategoryDestination.THEMES -> {
                    // Scrollable Unowned Themes Catalog
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("store_themes_list"),
                        contentPadding = PaddingValues(bottom = 24.dp + LocalFloatingBottomBarPadding.current),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
            // Top Keys & Shields Balance Card
            item(key = "shop_keys_balance") {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = aura.surfaceCard,
                    border = BorderStroke(
                        1.2.dp,
                        Brush.linearGradient(
                            listOf(aura.accentGold.copy(alpha = 0.55f), aura.accentCyan.copy(alpha = 0.45f))
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_keys_balance_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = aura.accentGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "رصيدي من الدروع والمفاتيح" else "My Shields & Keys",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = aura.accentGoldBg,
                                border = BorderStroke(1.dp, aura.accentGold.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = if (isArabic) "المفاتيح: $totalKeys" else "Keys: $totalKeys",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = aura.accentGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StoreKeyBalanceChip(
                                label = if (isArabic) "دروع" else "Shields",
                                count = shieldsCount,
                                accent = aura.accentEmerald,
                                icon = Icons.Default.Shield,
                                modifier = Modifier.weight(1f)
                            )
                            StoreKeyBalanceChip(
                                label = if (isArabic) "برونزي" else "Bronze",
                                count = forgeState.bronzeKeys,
                                accent = if (aura.isDark) Color(0xFFE59A54) else Color(0xFFB45309),
                                icon = Icons.Default.VpnKey,
                                modifier = Modifier.weight(1f)
                            )
                            StoreKeyBalanceChip(
                                label = if (isArabic) "فضي" else "Silver",
                                count = forgeState.silverKeys,
                                accent = if (aura.isDark) Color(0xFFE2E8F0) else Color(0xFF475569),
                                icon = Icons.Default.VpnKey,
                                modifier = Modifier.weight(1f)
                            )
                            StoreKeyBalanceChip(
                                label = if (isArabic) "ذهبي" else "Gold",
                                count = forgeState.goldenKeys,
                                accent = aura.accentGold,
                                icon = Icons.Default.VpnKey,
                                modifier = Modifier.weight(1f)
                            )
                            StoreKeyBalanceChip(
                                label = if (isArabic) "ماسي" else "Diamond",
                                count = forgeState.diamondKeys,
                                accent = if (aura.isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                icon = Icons.Default.Diamond,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Live 1-Minute Trial Countdown Banner (shown when trial is running)
            if (activeThemeTrial != null) {
                item(key = "active_trial_banner") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = aura.accentFlameSoft,
                        border = BorderStroke(
                            1.2.dp,
                            Brush.horizontalGradient(listOf(aura.accentFlame, aura.accentGold))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_theme_trial_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = aura.accentFlame,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isArabic) {
                                            "تجربة مجانية نشطة: ${activeThemeTrial.theme.displayName}"
                                        } else {
                                            "1-Min Trial Active: ${activeThemeTrial.theme.displayName}"
                                        },
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = aura.textPrimary,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isArabic) {
                                            "يعود الثيم تلقائياً بعد انتهاء الدقيقة"
                                        } else {
                                            "Auto-reverts when timer reaches 00:00"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = aura.textSecondary,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = aura.surfaceElevated,
                                border = BorderStroke(1.dp, aura.accentFlame.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = String.format("00:%02d", activeThemeTrial.remainingSeconds.coerceIn(0, 60)),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = aura.accentFlame,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Currently Applied Theme Banner + Reset
            item(key = "active_theme_banner") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = aura.surfaceElevated,
                    border = BorderStroke(1.dp, auraCardBorderBrush(aura)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_store_theme_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(aura.accentCyanSoft)
                                    .border(1.dp, aura.accentCyan.copy(alpha = 0.55f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = themeIconFor(currentStoreTheme),
                                    contentDescription = null,
                                    tint = aura.accentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "الثيم المطبّق حالياً" else "Currently Active Theme",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = aura.textTertiary,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = currentStoreTheme.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (currentStoreTheme != StoreTheme.DEFAULT) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = aura.surfaceSubtle,
                                border = BorderStroke(1.dp, aura.borderSubtle),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onEquipOwnedTheme(StoreTheme.DEFAULT)
                                    }
                                    .testTag("store_reset_default_theme")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = aura.textSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "الافتراضي" else "Reset",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = aura.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = aura.accentEmeraldSoft,
                                border = BorderStroke(1.dp, aura.accentEmerald.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = if (isArabic) "الأساسي" else "Default",
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = aura.accentEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Chips (counting only remaining unowned Store themes)
            item(key = "theme_category_filters") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StoreCategoryChip(
                        label = if (isArabic) "الكل (${unownedThemes.size})" else "All (${unownedThemes.size})",
                        isSelected = selectedCategory == null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedCategory = null
                        }
                    )
                    StoreThemeCategory.entries.forEach { category ->
                        val count = unownedThemes.count { it.category == category }
                        StoreCategoryChip(
                            label = if (isArabic) "${category.labelAr} ($count)" else "${category.labelEn} ($count)",
                            isSelected = selectedCategory == category,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedCategory = category
                            }
                        )
                    }
                }
            }

            if (displayedThemes.isEmpty()) {
                item(key = "all_themes_owned_state") {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = aura.surfaceCard,
                        border = BorderStroke(1.2.dp, aura.accentEmerald.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = aura.accentEmerald,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = if (isArabic) "تمتلك جميع ثيمات هذا القسم!" else "All themes in this section owned!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.Black
                                ),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (isArabic) {
                                    "الثيمات المشتراة نُقلت تلقائياً إلى الإعدادات ← Multi-Theme Engine لتفعيلها في أي وقت."
                                } else {
                                    "Purchased themes moved automatically to Settings → Multi-Theme Engine."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    lineHeight = 18.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Catalog Theme Showcase Grid (2 Themes per Row)
                items(
                    items = themeRows,
                    key = { row -> row.joinToString("_") { it.id } }
                ) { rowThemes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowThemes.forEach { theme ->
                            val isPermanentlyOwned = theme.isUnlockedByDefault || purchasedThemeIds.contains(theme.id)
                            val isUnlocked = isPermanentlyOwned || isPremiumActivated
                            val isApplied = currentStoreTheme == theme
                            val isOnTrial = activeThemeTrial?.theme == theme
                            val trialSec = if (isOnTrial) activeThemeTrial?.remainingSeconds ?: 0 else 0

                            StoreThemeShowcaseCard(
                                theme = theme,
                                isUnlocked = isUnlocked,
                                isUnlockedByPremium = isPremiumActivated && !isPermanentlyOwned,
                                isApplied = isApplied,
                                isOnTrial = isOnTrial,
                                trialRemainingSeconds = trialSec,
                                parentIsDark = aura.isDark,
                                isArabic = isArabic,
                                onInspect = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    inspectedTheme = theme
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowThemes.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
                    }
                }

                ShopCategoryDestination.AVATARS -> {
                    AvatarStoreSection(
                        totalKeys = totalKeys,
                        shieldsCount = shieldsCount,
                        forgeState = forgeState,
                        cyberAvatarConfig = cyberAvatarConfig,
                        purchasedAvatarIds = unlockedAvatarSet,
                        isPremiumActivated = isPremiumActivated,
                        isArabic = isArabic,
                        onPurchaseAvatar = { avatar ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPurchaseAvatar(avatar)
                        },
                        onEquipAvatar = { avatar ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEquipAvatar(avatar)
                        },
                        onUnequipAvatar = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUnequipAvatar()
                        },
                        onToggleAuraFrame = { enabled ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleAvatarAuraFrame(enabled)
                        }
                    )
                }

                ShopCategoryDestination.SEEKBARE -> {
                    SeekbarStoreSection(
                        totalKeys = totalKeys,
                        shieldsCount = shieldsCount,
                        forgeState = forgeState,
                        isWaveformPurchased = isWaveformSeekbarPurchased,
                        isShootingStarPurchased = isShootingStarSeekbarPurchased,
                        isAngrySnakePurchased = isAngrySnakeSeekbarPurchased,
                        purchasedSeekbarIds = purchasedSeekbarIds,
                        isPremiumActivated = isPremiumActivated,
                        activeSeekbarStyle = effectiveActiveSeekbar,
                        animPhase = animPhase,
                        pulsePhase = pulsePhase,
                        isArabic = isArabic,
                        onPurchaseStyle = { style ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (style == PlayerSeekbarStyle.WAVEFORM) {
                                onPurchaseWaveformSeekbar()
                            } else {
                                onPurchaseSeekbarStyle(style)
                            }
                        },
                        onSelectStyle = { style ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (style == PlayerSeekbarStyle.WAVEFORM && effectiveActiveSeekbar == PlayerSeekbarStyle.CLASSIC) {
                                onToggleWaveformSeekbar(true)
                            } else {
                                onSelectSeekbarStyle(style)
                            }
                        }
                    )
                }

                ShopCategoryDestination.MIMIC -> {
                    MimicStoreSection(
                        totalKeys = totalKeys,
                        shieldsCount = shieldsCount,
                        forgeState = forgeState,
                        streakDays = streakDays,
                        dailyWatchProgress = dailyWatchProgress,
                        isPlayerActive = isPlayerActive,
                        equippedHomeMimic = equippedHomeMimic,
                        isHomeMimicVisible = isHomeMimicVisible,
                        isFlameEnabled = isMimicFlameEnabled || (isHomeMimicVisible && equippedHomeMimic == HomeMimicItem.STREAK_FLAME),
                        isDnaPurchased = isMimicDnaPurchased,
                        isDnaEnabled = isMimicDnaEnabled || (isHomeMimicVisible && equippedHomeMimic == HomeMimicItem.DNA_CHROMOSOME),
                        purchasedMimicIds = purchasedMimicIds,
                        isPremiumActivated = isPremiumActivated,
                        animPhase = animPhase,
                        pulsePhase = pulsePhase,
                        isArabic = isArabic,
                        onPurchaseHomeMimic = { item ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPurchaseHomeMimic(item)
                        },
                        onEquipHomeMimic = { item ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEquipHomeMimic(item)
                        },
                        onToggleHomeMimicVisible = { enabled ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleHomeMimicVisible(enabled)
                        },
                        onToggleFlame = { enabled ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleMimicFlame(enabled)
                        },
                        onPurchaseDna = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPurchaseMimicDna()
                        },
                        onToggleDna = { enabled ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleMimicDna(enabled)
                        }
                    )
                }

                ShopCategoryDestination.AMBIENT_VIDEO_FILL -> {
                    AmbientVideoFillStoreSection(
                        totalKeys = totalKeys,
                        shieldsCount = shieldsCount,
                        forgeState = forgeState,
                        purchasedModeIds = purchasedAmbientModeIds,
                        activeMode = activeAmbientMode,
                        isGlowEnabled = isAmbientGlowEnabled,
                        isPremiumActivated = isPremiumActivated,
                        animPhase = animPhase,
                        pulsePhase = pulsePhase,
                        isArabic = isArabic,
                        onToggleGlow = { enabled ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleAmbientGlow(enabled)
                        },
                        onPurchaseMode = { mode ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPurchaseAmbientMode(mode)
                        },
                        onSelectMode = { mode ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectAmbientMode(mode)
                        }
                    )
                }
            }
            }
        }
    }

    // Detailed Popup Page on Theme Tap (Full Description, Live Preview, Colors, Buy & 1-Min Trial)
    val activeInspection = inspectedTheme
    if (activeInspection != null) {
        val previewPalette = remember(activeInspection, aura.isDark) {
            val useDark = when (activeInspection) {
                StoreTheme.NEUMORPHISM -> false
                StoreTheme.LIQUID_FROSTED_GLASS, StoreTheme.GLASS_MORPHISM_PSD_MOCKUP -> aura.isDark
                else -> true
            }
            auraPaletteFor(activeInspection, useDark)
        }
        val isPermanentlyOwned = activeInspection.isUnlockedByDefault || purchasedThemeIds.contains(activeInspection.id)
        val isUnlockedByPremium = isPremiumActivated && !isPermanentlyOwned
        val isUnlocked = isPermanentlyOwned || isPremiumActivated
        val isAlreadyApplied = currentStoreTheme == activeInspection
        val isOnTrial = activeThemeTrial?.theme == activeInspection
        val availableTierKeys = activeInspection.costTier?.let { forgeState.getKeyCount(it) } ?: Int.MAX_VALUE
        val canAffordShields = shieldsCount >= activeInspection.shieldsCost
        val canAffordKeys = availableTierKeys >= activeInspection.costAmount
        val canAffordPurchase = isUnlocked || (canAffordShields && canAffordKeys)
        val tierAccent = keyTierColor(activeInspection.costTier, aura.isDark)

        Dialog(
            onDismissRequest = { inspectedTheme = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = aura.dialogBg,
                border = BorderStroke(
                    1.4.dp,
                    Brush.linearGradient(listOf(previewPalette.accentCyan, previewPalette.accentViolet))
                ),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("store_theme_preview_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Popup Header: Icon, Name, Badges & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                previewPalette.accentCyan.copy(alpha = 0.24f),
                                                previewPalette.accentViolet.copy(alpha = 0.24f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.1.dp,
                                        Brush.linearGradient(listOf(previewPalette.accentCyan, previewPalette.accentViolet)),
                                        RoundedCornerShape(13.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = themeIconFor(activeInspection),
                                    contentDescription = null,
                                    tint = previewPalette.accentCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = activeInspection.displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    RarityBadgePill(
                                        rarity = activeInspection.rarity,
                                        isArabic = isArabic
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(7.dp),
                                        color = previewPalette.accentViolet.copy(alpha = 0.16f),
                                        border = BorderStroke(0.8.dp, previewPalette.accentViolet.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = activeInspection.badgeLabel,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (aura.isDark) previewPalette.accentViolet else aura.textPrimary,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.5.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = { inspectedTheme = null },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(aura.iconButtonBg)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isArabic) "إغلاق" else "Close",
                                tint = aura.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Full Interactive Theme Live Preview
                    ThemeLiveMiniPreview(
                        theme = activeInspection,
                        previewAura = previewPalette,
                        isArabic = isArabic,
                        isCompact = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(136.dp)
                    )

                    // Theme Description / Explanation Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = aura.surfaceSubtle,
                        border = BorderStroke(0.9.dp, aura.borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isArabic) "وصف الثيم والتفاصيل" else "Theme Details",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = previewPalette.accentCyan,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.5.sp
                                )
                            )
                            Text(
                                text = if (isArabic) activeInspection.descriptionAr else activeInspection.descriptionEn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textPrimary,
                                    lineHeight = 19.sp,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    // 5-Color Theme Palette Swatches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            previewPalette.background,
                            previewPalette.surfaceElevated,
                            previewPalette.accentCyan,
                            previewPalette.accentViolet,
                            previewPalette.accentGold
                        ).forEach { swatch ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(22.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(swatch)
                                    .border(0.8.dp, aura.borderSubtle, RoundedCornerShape(7.dp))
                            )
                        }
                    }

                    // Dual-Currency (Shields + Keys) Cost vs Owned Balance Summary when Locked
                    if (!isUnlocked && activeInspection.costTier != null) {
                        val tierName = when (activeInspection.costTier) {
                            ForgeKeyTier.BRONZE -> if (isArabic) "برونزي" else "Bronze"
                            ForgeKeyTier.SILVER -> if (isArabic) "فضي" else "Silver"
                            ForgeKeyTier.GOLD -> if (isArabic) "ذهبي" else "Gold"
                            ForgeKeyTier.DIAMOND -> if (isArabic) "ماسي" else "Diamond"
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tierAccent.copy(alpha = 0.10f),
                            border = BorderStroke(0.9.dp, tierAccent.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isArabic) {
                                            "🛡️ الدروع: ${activeInspection.shieldsCost} درع"
                                        } else {
                                            "🛡️ Shields: ${activeInspection.shieldsCost}"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = aura.accentEmerald,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                    Text(
                                        text = if (isArabic) "رصيدك: $shieldsCount" else "Owned: $shieldsCount",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (canAffordShields) aura.accentEmerald else aura.accentFlame,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isArabic) {
                                            "🗝️ المفاتيح: ${activeInspection.costAmount} $tierName"
                                        } else {
                                            "🗝️ Keys: ${activeInspection.costAmount} $tierName"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = tierAccent,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                    Text(
                                        text = if (isArabic) "رصيدك: $availableTierKeys" else "Owned: $availableTierKeys",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (canAffordKeys) aura.accentEmerald else aura.accentFlame,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Popup Action Buttons: Purchase / Apply + Free 1-Minute Trial
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isUnlocked) {
                                    onEquipOwnedTheme(activeInspection)
                                } else {
                                    onPurchaseTheme(activeInspection)
                                }
                                inspectedTheme = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isAlreadyApplied && isUnlocked -> aura.accentEmerald
                                    isUnlocked -> previewPalette.accentCyan
                                    canAffordPurchase -> aura.accentGold
                                    else -> aura.surfaceSubtle
                                },
                                contentColor = when {
                                    isAlreadyApplied && isUnlocked -> Color.White
                                    isUnlocked || canAffordPurchase -> Color(0xFF07090D)
                                    else -> aura.textSecondary
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("dialog_apply_theme_button")
                        ) {
                            Icon(
                                imageVector = when {
                                    isUnlocked && isAlreadyApplied -> Icons.Default.CheckCircle
                                    isUnlocked -> Icons.Default.Palette
                                    canAffordPurchase -> Icons.Default.VpnKey
                                    else -> Icons.Default.Lock
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isUnlocked && isAlreadyApplied -> if (isArabic) "مُطبّق حالياً ✓" else "Applied ✓"
                                    isUnlockedByPremium -> if (isArabic) "تطبيق مجاناً (Premium 👑)" else "Apply Free (Premium 👑)"
                                    isUnlocked -> if (isArabic) "تطبيق على التطبيق" else "Apply Theme"
                                    else -> {
                                        val costLabel = formatThemeCostText(activeInspection, isArabic)
                                        if (isArabic) "شراء وتفعيل ($costLabel)" else "Buy & Apply ($costLabel)"
                                    }
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (!isUnlocked) {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onStartTrialTheme(activeInspection)
                                    inspectedTheme = null
                                },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    1.1.dp,
                                    if (isOnTrial) aura.accentFlame else aura.accentEmerald.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("dialog_trial_theme_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = if (isOnTrial) aura.accentFlame else aura.accentEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOnTrial) {
                                        val sec = String.format("%02d", activeThemeTrial?.remainingSeconds ?: 0)
                                        if (isArabic) "تجربة مجانية جارية (00:$sec)" else "Free Trial Running (00:$sec)"
                                    } else {
                                        if (isArabic) "تجربة مجانية لمدة 1 دقيقة" else "Free 1-Minute Trial"
                                    },
                                    color = if (isOnTrial) aura.accentFlame else aura.accentEmerald,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreThemeShowcaseCard(
    theme: StoreTheme,
    isUnlocked: Boolean,
    isUnlockedByPremium: Boolean = false,
    isApplied: Boolean,
    isOnTrial: Boolean,
    trialRemainingSeconds: Int,
    parentIsDark: Boolean,
    isArabic: Boolean,
    onInspect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val previewAura = remember(theme, parentIsDark) {
        val useDark = when (theme) {
            StoreTheme.NEUMORPHISM -> false
            StoreTheme.LIQUID_FROSTED_GLASS, StoreTheme.GLASS_MORPHISM_PSD_MOCKUP -> parentIsDark
            else -> true
        }
        auraPaletteFor(theme, useDark)
    }

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            isOnTrial -> aura.accentFlame
            isApplied -> previewAura.accentCyan
            else -> aura.borderSubtle
        },
        animationSpec = tween(250),
        label = "theme_card_border"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = aura.surfaceCard,
        border = BorderStroke(
            width = if (isApplied || isOnTrial) 1.8.dp else 1.dp,
            brush = if (isApplied || isOnTrial) {
                Brush.linearGradient(
                    if (isOnTrial) listOf(aura.accentFlame, aura.accentGold)
                    else listOf(previewAura.accentCyan, previewAura.accentViolet)
                )
            } else {
                Brush.linearGradient(listOf(cardBorderColor, aura.borderMuted))
            }
        ),
        shadowElevation = if (isApplied) 8.dp else 2.dp,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onInspect() }
            .testTag("store_theme_card_${theme.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Compact Theme Preview with Active / Trial indicator
            Box(modifier = Modifier.fillMaxWidth()) {
                ThemeLiveMiniPreview(
                    theme = theme,
                    previewAura = previewAura,
                    isArabic = isArabic,
                    isCompact = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(78.dp)
                )

                if (isApplied || isOnTrial) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isOnTrial) aura.accentFlame else aura.accentEmerald,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = if (isOnTrial) {
                                String.format("00:%02d", trialRemainingSeconds.coerceIn(0, 60))
                            } else {
                                if (isArabic) "مُطبّق" else "Active"
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF07090D),
                                fontWeight = FontWeight.Black,
                                fontSize = 8.5.sp
                            )
                        )
                    }
                }
            }

            // Clean Theme Name Only (No Description Clutter on Storefront)
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = aura.textPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp
                ),
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Rarity + Free Trial Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RarityBadgePill(
                    rarity = theme.rarity,
                    isArabic = isArabic
                )
                if (!isUnlocked) {
                    Text(
                        text = if (isArabic) "تجربة 1 د مجاناً" else "Free 1m",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = aura.accentEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            // Dual-Currency Price / Ownership Pill
            PricePillBadge(
                theme = theme,
                isUnlocked = isUnlocked,
                isUnlockedByPremium = isUnlockedByPremium,
                isArabic = isArabic
            )
        }
    }
}

@Composable
private fun RarityBadgePill(
    rarity: StoreItemRarity,
    isArabic: Boolean
) {
    val aura = MaterialTheme.aura
    val color = rarityBadgeColor(rarity, aura.isDark)
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.55f))
    ) {
        Text(
            text = if (isArabic) rarity.labelAr else rarity.labelEn,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 8.5.sp
            ),
            maxLines = 1
        )
    }
}

private fun rarityBadgeColor(rarity: StoreItemRarity, isDark: Boolean): Color = when (rarity) {
    StoreItemRarity.FREE -> if (isDark) Color(0xFF34D399) else Color(0xFF059669)
    StoreItemRarity.COMMON -> if (isDark) Color(0xFFE59A54) else Color(0xFFB45309)
    StoreItemRarity.RARE -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    StoreItemRarity.EPIC -> if (isDark) Color(0xFFC084FC) else Color(0xFF7E22CE)
    StoreItemRarity.MYTHIC -> if (isDark) Color(0xFFFF4FD8) else Color(0xFFBE185D)
}

@Composable
private fun PricePillBadge(
    theme: StoreTheme,
    isUnlocked: Boolean,
    isUnlockedByPremium: Boolean = false,
    isArabic: Boolean
) {
    val aura = MaterialTheme.aura
    val tierColor = keyTierColor(theme.costTier, aura.isDark)

    when {
        theme.isUnlockedByDefault -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = aura.accentEmeraldSoft,
                border = BorderStroke(0.9.dp, aura.accentEmerald.copy(alpha = 0.6f))
            ) {
                Text(
                    text = if (isArabic) "مجاني • افتراضي" else "FREE • DEFAULT",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = aura.accentEmerald,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.5.sp
                    )
                )
            }
        }
        isUnlockedByPremium -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = aura.accentGoldBg,
                border = BorderStroke(0.9.dp, aura.accentGold.copy(alpha = 0.7f))
            ) {
                Text(
                    text = if (isArabic) "مجاني 👑" else "FREE 👑",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = aura.accentGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.5.sp
                    )
                )
            }
        }
        isUnlocked -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = aura.accentEmeraldSoft,
                border = BorderStroke(0.9.dp, aura.accentEmerald.copy(alpha = 0.6f))
            ) {
                Text(
                    text = if (isArabic) "مملوك ✓" else "OWNED ✓",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = aura.accentEmerald,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.5.sp
                    )
                )
            }
        }
        else -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tierColor.copy(alpha = 0.14f),
                border = BorderStroke(0.9.dp, tierColor.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = aura.accentEmerald,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${theme.shieldsCost}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = aura.accentEmerald,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.5.sp
                        )
                    )
                    Text(
                        text = " + ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = aura.textSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                    Icon(
                        imageVector = if (theme.costTier == ForgeKeyTier.DIAMOND) Icons.Default.Diamond else Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = tierColor,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = formatThemeKeyCostOnly(theme, isArabic),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = tierColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun formatThemeKeyCostOnly(theme: StoreTheme, isArabic: Boolean): String {
    val tier = theme.costTier ?: return if (isArabic) "مجاني" else "Free"
    val tierLabel = when (tier) {
        ForgeKeyTier.BRONZE -> if (isArabic) "برونزي" else "Bronze"
        ForgeKeyTier.SILVER -> if (isArabic) "فضي" else "Silver"
        ForgeKeyTier.GOLD -> if (isArabic) "ذهبي" else "Gold"
        ForgeKeyTier.DIAMOND -> if (isArabic) "ماسي" else "Diamond"
    }
    return "${theme.costAmount} $tierLabel"
}

private fun formatThemeCostText(theme: StoreTheme, isArabic: Boolean): String {
    val tier = theme.costTier ?: return if (isArabic) "مجاني" else "Free"
    val keyPart = formatThemeKeyCostOnly(theme, isArabic)
    return if (theme.shieldsCost > 0) {
        "${theme.shieldsCost} 🛡️ + $keyPart"
    } else {
        keyPart
    }
}

private fun keyTierColor(tier: ForgeKeyTier?, isDark: Boolean): Color = when (tier) {
    ForgeKeyTier.BRONZE -> if (isDark) Color(0xFFE59A54) else Color(0xFFB45309)
    ForgeKeyTier.SILVER -> if (isDark) Color(0xFFE2E8F0) else Color(0xFF475569)
    ForgeKeyTier.GOLD -> if (isDark) Color(0xFFFFD700) else Color(0xFFA16207)
    ForgeKeyTier.DIAMOND -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    null -> if (isDark) Color(0xFF34D399) else Color(0xFF059669)
}

@Composable
internal fun ThemeLiveMiniPreview(
    theme: StoreTheme,
    previewAura: AuraPalette,
    isArabic: Boolean,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(if (isCompact) 14.dp else 16.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(previewAura.background)
            .border(
                width = if (previewAura.isGlassStyle) 1.3.dp else 1.dp,
                brush = auraCardBorderBrush(previewAura),
                shape = shape
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (previewAura.isNeumorphic) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(previewAura.ambientPrimary.copy(alpha = 0.55f), Color.Transparent),
                        radius = w * 0.65f
                    ),
                    center = Offset(w * 0.15f, h * 0.15f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(previewAura.ambientSecondary.copy(alpha = 0.55f), Color.Transparent),
                        radius = w * 0.65f
                    ),
                    center = Offset(w * 0.85f, h * 0.88f)
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(previewAura.ambientPrimary.copy(alpha = 0.42f), Color.Transparent),
                        radius = w * 0.5f
                    ),
                    center = Offset(w * 0.18f, h * 0.25f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(previewAura.ambientSecondary.copy(alpha = 0.36f), Color.Transparent),
                        radius = w * 0.48f
                    ),
                    center = Offset(w * 0.82f, h * 0.72f)
                )
            }

            if (theme == StoreTheme.SYNTHWAVE) {
                val horizonY = h * 0.62f
                drawLine(
                    color = previewAura.accentViolet.copy(alpha = 0.65f),
                    start = Offset(0f, horizonY),
                    end = Offset(w, horizonY),
                    strokeWidth = 1.2.dp.toPx()
                )
                for (i in -3..3) {
                    drawLine(
                        color = previewAura.accentCyan.copy(alpha = 0.32f),
                        start = Offset(w * 0.5f, horizonY),
                        end = Offset(w * 0.5f + i * (w * 0.24f), h),
                        strokeWidth = 0.8.dp.toPx()
                    )
                }
            } else if (theme == StoreTheme.GLASS_MORPHISM_PSD_MOCKUP) {
                val step = 18.dp.toPx()
                var x = step
                while (x < w) {
                    drawLine(previewAura.textTertiary.copy(alpha = 0.18f), Offset(x, 0f), Offset(x, h), 0.5.dp.toPx())
                    x += step
                }
                var y = step
                while (y < h) {
                    drawLine(previewAura.textTertiary.copy(alpha = 0.18f), Offset(0f, y), Offset(w, y), 0.5.dp.toPx())
                    y += step
                }
            }
        }

        if (isCompact) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = themeIconFor(theme),
                        contentDescription = null,
                        tint = previewAura.accentCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = theme.badgeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = previewAura.accentCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = previewAura.surfaceCard,
                    border = BorderStroke(0.9.dp, auraCardBorderBrush(previewAura)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 7.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(previewAura.progressTrack)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.68f)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(previewAura.accentCyan, previewAura.accentViolet)
                                        )
                                    )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(previewAura.primaryCtaBg)
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(previewAura.accentCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AURA PULSE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = previewAura.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.5.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = previewAura.accentCyanSoft,
                        border = BorderStroke(0.6.dp, previewAura.accentCyan.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = theme.badgeLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = previewAura.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(11.dp),
                    color = previewAura.surfaceCard,
                    border = BorderStroke(1.dp, auraCardBorderBrush(previewAura)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "معاينة الواجهة والبطاقات" else "Live UI & Card Contrast",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = previewAura.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(previewAura.progressTrack)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.68f)
                                        .height(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(previewAura.accentCyan, previewAura.accentViolet)
                                            )
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = previewAura.primaryCtaBg
                        ) {
                            Text(
                                text = if (isArabic) "تشغيل" else "Play",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = previewAura.primaryCtaContent,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreCategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) aura.accentCyanSoft else aura.unselectedChipBg,
        border = BorderStroke(
            width = if (isSelected) 1.2.dp else 0.9.dp,
            color = if (isSelected) aura.accentCyan else aura.borderSubtle
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (isSelected) aura.accentCyan else aura.textSecondary,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = 11.5.sp
            )
        )
    }
}

@Composable
private fun StoreKeyBalanceChip(
    label: String,
    count: Int,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = aura.surfaceElevated,
        border = BorderStroke(0.9.dp, accent.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 3.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accent,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = aura.textPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.5.sp
                ),
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = accent.copy(alpha = 0.9f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

internal fun themeIconFor(theme: StoreTheme): ImageVector = when (theme) {
    StoreTheme.DEFAULT -> Icons.Default.AutoAwesome
    StoreTheme.LIQUID_FROSTED_GLASS -> Icons.Default.WaterDrop
    StoreTheme.TOKYO_NIGHT -> Icons.Default.ColorLens
    StoreTheme.CATPPUCCIN_MOCHA -> Icons.Default.Palette
    StoreTheme.NORD_ARCTIC -> Icons.Default.BlurOn
    StoreTheme.FROSTED_GLASSMORPHISM -> Icons.Default.BlurOn
    StoreTheme.NEUMORPHISM -> Icons.Default.TouchApp
    StoreTheme.SYNTHWAVE -> Icons.Default.AutoAwesome
    StoreTheme.RETRO_GLOW -> Icons.Default.WbTwilight
    StoreTheme.DRACULA_PRO -> Icons.Default.Diamond
    StoreTheme.HOLOGRAPHIC_LIQUID_GLASS -> Icons.Default.AutoAwesome
    StoreTheme.GLASS_MORPHISM_PSD_MOCKUP -> Icons.Default.Layers
}

private enum class ShopTileEffectType {
    THEMES,
    AVATAR,
    SEEKBARE,
    MIMIC,
    AMBIENT_FILL
}

@Composable
private fun ShopBalanceSummaryCard(
    totalKeys: Int,
    shieldsCount: Int,
    forgeState: ForgeUiState,
    isArabic: Boolean
) {
    val aura = MaterialTheme.aura
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = aura.surfaceCard,
        border = BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                listOf(aura.accentGold.copy(alpha = 0.55f), aura.accentCyan.copy(alpha = 0.45f))
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shop_keys_balance_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = aura.accentGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "رصيدي من الدروع والمفاتيح" else "My Shields & Keys",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = aura.accentGoldBg,
                    border = BorderStroke(1.dp, aura.accentGold.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = if (isArabic) "المفاتيح: $totalKeys" else "Keys: $totalKeys",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = aura.accentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StoreKeyBalanceChip(
                    label = if (isArabic) "دروع" else "Shields",
                    count = shieldsCount,
                    accent = aura.accentEmerald,
                    icon = Icons.Default.Shield,
                    modifier = Modifier.weight(1f)
                )
                StoreKeyBalanceChip(
                    label = if (isArabic) "برونزي" else "Bronze",
                    count = forgeState.bronzeKeys,
                    accent = if (aura.isDark) Color(0xFFE59A54) else Color(0xFFB45309),
                    icon = Icons.Default.VpnKey,
                    modifier = Modifier.weight(1f)
                )
                StoreKeyBalanceChip(
                    label = if (isArabic) "فضي" else "Silver",
                    count = forgeState.silverKeys,
                    accent = if (aura.isDark) Color(0xFFE2E8F0) else Color(0xFF475569),
                    icon = Icons.Default.VpnKey,
                    modifier = Modifier.weight(1f)
                )
                StoreKeyBalanceChip(
                    label = if (isArabic) "ذهبي" else "Gold",
                    count = forgeState.goldenKeys,
                    accent = aura.accentGold,
                    icon = Icons.Default.VpnKey,
                    modifier = Modifier.weight(1f)
                )
                StoreKeyBalanceChip(
                    label = if (isArabic) "ماسي" else "Diamond",
                    count = forgeState.diamondKeys,
                    accent = if (aura.isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                    icon = Icons.Default.Diamond,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActiveTrialBannerCard(
    activeThemeTrial: ThemeTrialState,
    isArabic: Boolean
) {
    val aura = MaterialTheme.aura
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = aura.accentFlameSoft,
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(listOf(aura.accentFlame, aura.accentGold))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_theme_trial_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = aura.accentFlame,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isArabic) {
                            "تجربة مجانية نشطة: ${activeThemeTrial.theme.displayName}"
                        } else {
                            "1-Min Trial Active: ${activeThemeTrial.theme.displayName}"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isArabic) {
                            "يعود الثيم تلقائياً بعد انتهاء الدقيقة"
                        } else {
                            "Auto-reverts when timer reaches 00:00"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = aura.textSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = aura.surfaceElevated,
                border = BorderStroke(1.dp, aura.accentFlame.copy(alpha = 0.6f))
            ) {
                Text(
                    text = String.format("00:%02d", activeThemeTrial.remainingSeconds.coerceIn(0, 60)),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = aura.accentFlame,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ShopCategoryEffectTile(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    accentPrimary: Color,
    accentSecondary: Color,
    effectType: ShopTileEffectType,
    animPhase: Float,
    pulsePhase: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = aura.surfaceCard,
        border = BorderStroke(
            1.3.dp,
            Brush.linearGradient(
                listOf(accentPrimary.copy(alpha = 0.72f), accentSecondary.copy(alpha = 0.55f))
            )
        ),
        shadowElevation = 6.dp,
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            // Realistic Category-Specific Animated Effect Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(106.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (aura.isDark) Color(0xFF0A101E) else aura.surfaceSubtle)
                    .border(0.9.dp, accentPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            ) {
                ShopCategoryEffectCanvas(
                    effectType = effectType,
                    accentPrimary = accentPrimary,
                    accentSecondary = accentSecondary,
                    accentGold = aura.accentGold,
                    animPhase = animPhase,
                    pulsePhase = pulsePhase,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.22f),
                    border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(7.dp)
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.5.sp
                        )
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.textSecondary,
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ShopCategoryEffectCanvas(
    effectType: ShopTileEffectType,
    accentPrimary: Color,
    accentSecondary: Color,
    accentGold: Color,
    animPhase: Float,
    pulsePhase: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val rad = animPhase * 2f * PI.toFloat()

        when (effectType) {
            ShopTileEffectType.THEMES -> {
                // Chromatic drifting aura orbs + layered frosted-glass theme cards
                val orbX1 = w * (0.28f + 0.14f * cos(rad))
                val orbY1 = h * (0.34f + 0.12f * sin(rad))
                val orbX2 = w * (0.72f - 0.14f * cos(rad))
                val orbY2 = h * (0.68f - 0.12f * sin(rad))

                drawCircle(
                    brush = Brush.radialGradient(listOf(accentPrimary.copy(alpha = 0.52f), Color.Transparent), radius = w * 0.45f),
                    center = Offset(orbX1, orbY1)
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(accentSecondary.copy(alpha = 0.48f), Color.Transparent), radius = w * 0.42f),
                    center = Offset(orbX2, orbY2)
                )
                // Layered glass theme swatch cards
                val cardW = w * 0.48f
                val cardH = h * 0.48f
                val shift = 4.dp.toPx() * pulsePhase
                drawRoundRect(
                    color = accentSecondary.copy(alpha = 0.25f),
                    topLeft = Offset(cx - cardW * 0.65f - shift, cy - cardH * 0.58f),
                    size = Size(cardW, cardH),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
                drawRoundRect(
                    color = accentPrimary.copy(alpha = 0.22f),
                    topLeft = Offset(cx - cardW * 0.28f + shift, cy - cardH * 0.34f),
                    size = Size(cardW, cardH),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.75f), accentPrimary)),
                    topLeft = Offset(cx - cardW * 0.28f + shift, cy - cardH * 0.34f),
                    size = Size(cardW, cardH),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(width = 1.4.dp.toPx())
                )
                // Mini spectrum dots
                val dotColors = listOf(accentPrimary, accentSecondary, accentGold, Color(0xFF34D399))
                dotColors.forEachIndexed { idx, col ->
                    drawCircle(
                        color = col,
                        radius = 4.2.dp.toPx(),
                        center = Offset(
                            x = (cx - cardW * 0.14f + shift) + idx * 12.dp.toPx(),
                            y = cy + cardH * 0.02f
                        )
                    )
                }
            }

            ShopTileEffectType.AVATAR -> {
                // Rotating cosmic avatar frame halo + biometric silhouette + orbiting stars
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(accentPrimary.copy(alpha = 0.34f + 0.16f * pulsePhase), Color.Transparent),
                        radius = h * 0.56f
                    ),
                    center = Offset(cx, cy)
                )
                val ringRadius = minOf(w, h) * 0.33f
                rotate(degrees = animPhase * 360f, pivot = Offset(cx, cy)) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(accentPrimary, accentGold, Color.Transparent, accentSecondary, accentPrimary),
                            center = Offset(cx, cy)
                        ),
                        radius = ringRadius,
                        center = Offset(cx, cy),
                        style = Stroke(
                            width = 2.6.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f)
                        )
                    )
                }
                // Inner glowing portrait badge
                drawCircle(
                    color = accentPrimary.copy(alpha = 0.24f),
                    radius = ringRadius * 0.76f,
                    center = Offset(cx, cy)
                )
                // Head circle & shoulders arc
                drawCircle(
                    color = Color.White.copy(alpha = 0.92f),
                    radius = ringRadius * 0.24f,
                    center = Offset(cx, cy - ringRadius * 0.16f)
                )
                drawArc(
                    color = accentGold.copy(alpha = 0.95f),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = true,
                    topLeft = Offset(cx - ringRadius * 0.44f, cy + ringRadius * 0.10f),
                    size = Size(ringRadius * 0.88f, ringRadius * 0.75f)
                )
                // Orbiting sparkle nodes
                for (i in 0 until 3) {
                    val angle = rad + i * (2f * PI.toFloat() / 3f)
                    val px = cx + cos(angle) * (ringRadius + 7.dp.toPx())
                    val py = cy + sin(angle) * (ringRadius + 7.dp.toPx())
                    drawCircle(color = accentGold, radius = 2.6.dp.toPx(), center = Offset(px, py))
                }
            }

            ShopTileEffectType.SEEKBARE -> {
                // Pulsing audio/video spectrum bars + glowing animated neon seekbar & thumb
                val padX = w * 0.12f
                val trackW = w - 2f * padX
                val trackY = h * 0.70f
                val progress = 0.20f + 0.65f * pulsePhase
                val thumbX = padX + trackW * progress

                val barCount = 12
                val barSpacing = trackW / barCount
                for (i in 0 until barCount) {
                    val barFrac = (i + 0.5f) / barCount
                    val wave = 0.35f + 0.65f * ((sin(rad * 1.5f + i * 0.7f) + 1f) / 2f)
                    val barH = (h * 0.34f) * wave
                    val barActive = barFrac <= progress
                    drawRoundRect(
                        color = if (barActive) accentPrimary.copy(alpha = 0.9f) else accentSecondary.copy(alpha = 0.26f),
                        topLeft = Offset(padX + i * barSpacing + 2.dp.toPx(), trackY - 12.dp.toPx() - barH),
                        size = Size((barSpacing - 4.dp.toPx()).coerceAtLeast(2.dp.toPx()), barH),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }

                // Track base
                drawLine(
                    color = Color.White.copy(alpha = 0.18f),
                    start = Offset(padX, trackY),
                    end = Offset(padX + trackW, trackY),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Active glowing progress track
                drawLine(
                    brush = Brush.horizontalGradient(listOf(accentSecondary, accentPrimary)),
                    start = Offset(padX, trackY),
                    end = Offset(thumbX, trackY),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Comet thumb ripple + bright core
                drawCircle(
                    color = accentPrimary.copy(alpha = 0.30f + 0.25f * pulsePhase),
                    radius = 12.dp.toPx(),
                    center = Offset(thumbX, trackY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = Offset(thumbX, trackY)
                )
            }

            ShopTileEffectType.MIMIC -> {
                // Synchronized holographic kinetic mirror echoes radiating from core diamond
                for (echo in 2 downTo 0) {
                    val echoShift = echo * 15.dp.toPx() * (0.4f + 0.6f * pulsePhase)
                    val alpha = when (echo) {
                        2 -> 0.22f
                        1 -> 0.45f
                        else -> 0.92f
                    }
                    val color = if (echo % 2 == 0) accentPrimary else accentSecondary
                    val half = 16.dp.toPx()

                    // Left & Right mirrored kinetic echo brackets
                    drawRoundRect(
                        color = color.copy(alpha = alpha * 0.45f),
                        topLeft = Offset(cx - half - echoShift, cy - half),
                        size = Size(half * 2f, half * 2f),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                    if (echo > 0) {
                        drawRoundRect(
                            color = accentGold.copy(alpha = alpha * 0.5f),
                            topLeft = Offset(cx - half + echoShift, cy - half),
                            size = Size(half * 2f, half * 2f),
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                            style = Stroke(width = 1.6.dp.toPx())
                        )
                    }
                }
                // Central radiant kinetic pulse
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.White, accentPrimary, Color.Transparent), radius = 16.dp.toPx()),
                    center = Offset(cx, cy),
                    radius = 12.dp.toPx()
                )
            }

            ShopTileEffectType.AMBIENT_FILL -> {
                // Cinema screen floating over deep obsidian radial blur & layered vignette diffusion
                val glowRadius = h * (0.46f + 0.10f * pulsePhase)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentPrimary.copy(alpha = 0.28f),
                            accentSecondary.copy(alpha = 0.16f),
                            Color(0xFF050508),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = glowRadius * 1.45f
                    ),
                    center = Offset(cx, cy),
                    radius = glowRadius * 1.45f
                )
                val driftX = cos(rad) * 8.dp.toPx()
                val driftY = sin(rad) * 4.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentGold.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(cx + driftX, cy + driftY),
                        radius = glowRadius
                    ),
                    center = Offset(cx + driftX, cy + driftY),
                    radius = glowRadius
                )
                // Inner cinema viewport frame
                val frameW = w * 0.44f
                val frameH = h * 0.50f
                drawRoundRect(
                    color = Color(0xFF020204),
                    topLeft = Offset(cx - frameW / 2f, cy - frameH / 2f),
                    size = Size(frameW, frameH),
                    cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(accentPrimary.copy(alpha = 0.72f), accentSecondary.copy(alpha = 0.55f))),
                    topLeft = Offset(cx - frameW / 2f, cy - frameH / 2f),
                    size = Size(frameW, frameH),
                    cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun AvatarStoreSection(
    totalKeys: Int,
    shieldsCount: Int,
    forgeState: ForgeUiState,
    cyberAvatarConfig: CyberAvatarConfig,
    purchasedAvatarIds: Set<String>,
    isPremiumActivated: Boolean,
    isArabic: Boolean,
    onPurchaseAvatar: (StoreAvatarItem) -> Unit,
    onEquipAvatar: (StoreAvatarItem) -> Unit,
    onUnequipAvatar: () -> Unit,
    onToggleAuraFrame: (Boolean) -> Unit
) {
    val aura = MaterialTheme.aura
    val equippedAvatar = cyberAvatarConfig.equippedAvatar
    var previewAvatar by remember(equippedAvatar) {
        mutableStateOf(equippedAvatar ?: StoreAvatarItem.CYBER_SAMURAI)
    }
    val unlockedCount = StoreAvatarItem.entries.count {
        purchasedAvatarIds.contains(it.id) || isPremiumActivated
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("avatar_store_list"),
        contentPadding = PaddingValues(bottom = 24.dp + LocalFloatingBottomBarPadding.current),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "avatar_store_balance") {
            ShopBalanceSummaryCard(
                totalKeys = totalKeys,
                shieldsCount = shieldsCount,
                forgeState = forgeState,
                isArabic = isArabic
            )
        }

        // Interactive Spotlight Preview Stage + Holographic Frame Toggle
        item(key = "avatar_spotlight_preview_card") {
            val primary = Color(previewAvatar.primaryAccentHex)
            val secondary = Color(previewAvatar.secondaryAccentHex)
            val isPreviewPurchased = purchasedAvatarIds.contains(previewAvatar.id)
            val isPreviewUnlocked = isPreviewPurchased || isPremiumActivated
            val isPreviewEquipped = equippedAvatar == previewAvatar
            val tierColor = keyTierColor(previewAvatar.costTier, aura.isDark)
            val availableKeys = forgeState.getKeyCount(previewAvatar.costTier)
            val canAfford = isPreviewUnlocked || (shieldsCount >= previewAvatar.shieldsCost && availableKeys >= previewAvatar.costAmount)
            val tierWord = when (previewAvatar.costTier) {
                ForgeKeyTier.BRONZE -> if (isArabic) "برونزي" else "Bronze"
                ForgeKeyTier.SILVER -> if (isArabic) "فضي" else "Silver"
                ForgeKeyTier.GOLD -> if (isArabic) "ذهبي" else "Gold"
                ForgeKeyTier.DIAMOND -> if (isArabic) "ماسي" else "Diamond"
            }

            Surface(
                shape = RoundedCornerShape(26.dp),
                color = aura.surfaceCard,
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(primary, secondary))
                ),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("avatar_spotlight_preview_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "معاينة حية 60 FPS" else "Live 60 FPS Preview",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = primary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = "• $unlockedCount/10",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = aura.textSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            Text(
                                text = if (isArabic) previewAvatar.displayNameAr else previewAvatar.displayNameEn,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isArabic) previewAvatar.frameNameAr else previewAvatar.frameNameEn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = secondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        RarityBadgePill(
                            rarity = previewAvatar.rarity,
                            isArabic = isArabic
                        )
                    }

                    // OLED #000000 Holographic Portrait Showcase Stage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(186.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF000000))
                            .border(
                                1.2.dp,
                                Brush.linearGradient(listOf(primary.copy(alpha = 0.65f), secondary.copy(alpha = 0.55f))),
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CyberAvatarPortraitCanvas(
                            avatar = previewAvatar,
                            isCompact = false,
                            showAuraFrame = cyberAvatarConfig.isAuraFrameEnabled,
                            modifier = Modifier.size(164.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xCC060912),
                            border = BorderStroke(0.8.dp, primary.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (isPreviewEquipped) {
                                    if (isArabic) "مُجهّز بالملف الشخصي ✓" else "Active on Profile ✓"
                                } else {
                                    if (isArabic) "معاينة تفاعلية" else "Interactive Stage"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isPreviewEquipped) aura.accentEmerald else primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp
                                )
                            )
                        }
                    }

                    // Holographic Aura Frame Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(aura.surfaceSubtle)
                            .border(0.9.dp, aura.borderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "إطار الهالة الهولوغرافي" else "Holographic Aura Frame",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = if (isArabic) "حلقات وجسيمات دوّارة 60 FPS حول الأفاتار" else "Rotating 60 FPS neon rings & particles",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                        Switch(
                            checked = cyberAvatarConfig.isAuraFrameEnabled,
                            onCheckedChange = onToggleAuraFrame,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = primary,
                                checkedThumbColor = Color(0xFF07090D),
                                uncheckedTrackColor = aura.progressTrack,
                                uncheckedThumbColor = aura.textSecondary
                            ),
                            modifier = Modifier.testTag("avatar_aura_frame_switch")
                        )
                    }

                    // Quick Action inside Spotlight Stage
                    if (!isPreviewUnlocked) {
                        Button(
                            onClick = { onPurchaseAvatar(previewAvatar) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAfford) aura.accentGold else aura.surfaceSubtle,
                                contentColor = if (canAfford) Color(0xFF07090D) else aura.textSecondary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("spotlight_buy_avatar_button")
                        ) {
                            Icon(
                                imageVector = if (canAfford) Icons.Default.VpnKey else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) {
                                    "شراء وتجهيز (${previewAvatar.shieldsCost} 🛡️ + ${previewAvatar.costAmount} $tierWord)"
                                } else {
                                    "Buy & Equip (${previewAvatar.shieldsCost} 🛡️ + ${previewAvatar.costAmount} $tierWord)"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (isPreviewEquipped) onUnequipAvatar() else onEquipAvatar(previewAvatar)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPreviewEquipped) aura.surfaceSubtle else primary,
                                contentColor = if (isPreviewEquipped) primary else Color(0xFF07090D)
                            ),
                            border = if (isPreviewEquipped) BorderStroke(1.dp, primary.copy(alpha = 0.6f)) else null,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("spotlight_equip_avatar_button")
                        ) {
                            Icon(
                                imageVector = if (isPreviewEquipped) Icons.Default.VisibilityOff else Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPreviewEquipped) {
                                    if (isArabic) "إزالة من الملف الشخصي" else "Unequip from Profile"
                                } else {
                                    if (isArabic) "تجهيز كأفاتار الملف الشخصي" else "Equip on Profile"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // All 10 Futuristic Cyberpunk Avatars
        items(StoreAvatarItem.entries, key = { "shop_avatar_${it.id}" }) { avatar ->
            val primary = Color(avatar.primaryAccentHex)
            val secondary = Color(avatar.secondaryAccentHex)
            val isPurchased = purchasedAvatarIds.contains(avatar.id)
            val isUnlocked = isPurchased || isPremiumActivated
            val isEquipped = equippedAvatar == avatar
            val isSelectedPreview = previewAvatar == avatar
            val tierColor = keyTierColor(avatar.costTier, aura.isDark)
            val availableKeys = forgeState.getKeyCount(avatar.costTier)
            val canAffordShields = shieldsCount >= avatar.shieldsCost
            val canAffordKeys = availableKeys >= avatar.costAmount
            val canAfford = isUnlocked || (canAffordShields && canAffordKeys)
            val tierWord = when (avatar.costTier) {
                ForgeKeyTier.BRONZE -> if (isArabic) "برونزي" else "Bronze"
                ForgeKeyTier.SILVER -> if (isArabic) "فضي" else "Silver"
                ForgeKeyTier.GOLD -> if (isArabic) "ذهبي" else "Gold"
                ForgeKeyTier.DIAMOND -> if (isArabic) "ماسي" else "Diamond"
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = aura.surfaceCard,
                border = BorderStroke(
                    width = when {
                        isEquipped -> 1.8.dp
                        isSelectedPreview -> 1.5.dp
                        else -> 1.15.dp
                    },
                    brush = Brush.linearGradient(
                        if (isEquipped) listOf(aura.accentEmerald, primary)
                        else listOf(primary.copy(alpha = 0.65f), secondary.copy(alpha = 0.45f))
                    )
                ),
                shadowElevation = if (isEquipped || isSelectedPreview) 10.dp else 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { previewAvatar = avatar }
                    .testTag("shop_avatar_card_${avatar.id}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Live OLED Medallion Preview
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF000000))
                                .border(
                                    1.2.dp,
                                    Brush.linearGradient(listOf(primary.copy(alpha = 0.7f), secondary.copy(alpha = 0.6f))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CyberAvatarPortraitCanvas(
                                avatar = avatar,
                                isCompact = true,
                                showAuraFrame = cyberAvatarConfig.isAuraFrameEnabled,
                                modifier = Modifier.size(88.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) avatar.displayNameAr else avatar.displayNameEn,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    ),
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                RarityBadgePill(
                                    rarity = avatar.rarity,
                                    isArabic = isArabic
                                )
                            }

                            Text(
                                text = if (isArabic) avatar.subtitleAr else avatar.subtitleEn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SeekbarStatusPill(
                                    isActive = isEquipped,
                                    isPurchased = isPurchased,
                                    isPremiumActivated = isPremiumActivated,
                                    costLabel = "${avatar.shieldsCost}🛡️ + ${avatar.costAmount} $tierWord",
                                    tierColor = tierColor,
                                    isArabic = isArabic
                                )
                            }
                        }
                    }

                    SeekbarPriceOrFreeRow(
                        isPurchased = isPurchased,
                        isPremiumActivated = isPremiumActivated,
                        shieldsCost = avatar.shieldsCost,
                        availableShields = shieldsCount,
                        canAffordShields = canAffordShields,
                        keyCostText = if (isArabic) "🗝️ ${avatar.costAmount} مفتاح $tierWord" else "🗝️ ${avatar.costAmount} $tierWord Keys",
                        availableKeys = availableKeys,
                        canAffordKeys = canAffordKeys,
                        tierColor = tierColor,
                        isArabic = isArabic
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { previewAvatar = avatar },
                            border = BorderStroke(
                                1.1.dp,
                                if (isSelectedPreview) primary else aura.borderSubtle
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("preview_avatar_${avatar.id}_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = if (isArabic) "معاينة" else "Preview",
                                tint = if (isSelectedPreview) primary else aura.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "معاينة" else "Preview",
                                color = if (isSelectedPreview) primary else aura.textSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (!isUnlocked) {
                            Button(
                                onClick = {
                                    previewAvatar = avatar
                                    onPurchaseAvatar(avatar)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canAfford) aura.accentGold else aura.surfaceSubtle,
                                    contentColor = if (canAfford) Color(0xFF07090D) else aura.textSecondary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("buy_avatar_${avatar.id}_button")
                            ) {
                                Icon(
                                    imageVector = if (canAfford) Icons.Default.VpnKey else Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "شراء وتجهيز" else "Buy & Equip",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.5.sp
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    previewAvatar = avatar
                                    if (isEquipped) onUnequipAvatar() else onEquipAvatar(avatar)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEquipped) aura.surfaceSubtle else if (isPremiumActivated && !isPurchased) aura.accentGold else primary,
                                    contentColor = if (isEquipped) primary else Color(0xFF07090D)
                                ),
                                border = if (isEquipped) BorderStroke(1.dp, primary.copy(alpha = 0.6f)) else null,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("equip_avatar_${avatar.id}_button")
                            ) {
                                Icon(
                                    imageVector = if (isEquipped) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        isEquipped -> if (isArabic) "مُجهّز ✓" else "Equipped ✓"
                                        isPremiumActivated && !isPurchased -> if (isArabic) "تجهيز مجاناً 👑" else "Equip Free 👑"
                                        else -> if (isArabic) "تجهيز بالملف الشخصي" else "Equip"
                                    },
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeekbarStoreSection(
    totalKeys: Int,
    shieldsCount: Int,
    forgeState: ForgeUiState,
    isWaveformPurchased: Boolean,
    isShootingStarPurchased: Boolean,
    isAngrySnakePurchased: Boolean,
    purchasedSeekbarIds: Set<String> = emptySet(),
    isPremiumActivated: Boolean = false,
    activeSeekbarStyle: PlayerSeekbarStyle,
    animPhase: Float,
    pulsePhase: Float,
    isArabic: Boolean,
    onPurchaseStyle: (PlayerSeekbarStyle) -> Unit,
    onSelectStyle: (PlayerSeekbarStyle) -> Unit
) {
    val aura = MaterialTheme.aura
    val waveCostTier = StreakPreferencesRepository.WAVEFORM_SEEKBAR_COST_TIER
    val waveCostAmount = StreakPreferencesRepository.WAVEFORM_SEEKBAR_COST_AMOUNT
    val waveShieldsCost = StreakPreferencesRepository.WAVEFORM_SEEKBAR_SHIELDS_COST
    val waveAvailableKeys = forgeState.getKeyCount(waveCostTier)
    val isWaveformUnlocked = isWaveformPurchased || isPremiumActivated
    val canAffordWaveShields = shieldsCount >= waveShieldsCost
    val canAffordWaveKeys = waveAvailableKeys >= waveCostAmount
    val canAffordWave = isWaveformUnlocked || (canAffordWaveShields && canAffordWaveKeys)
    val waveTierColor = keyTierColor(waveCostTier, aura.isDark)
    val isWaveformActive = activeSeekbarStyle == PlayerSeekbarStyle.WAVEFORM

    val starCostTier = StreakPreferencesRepository.SHOOTING_STAR_SEEKBAR_COST_TIER
    val starCostAmount = StreakPreferencesRepository.SHOOTING_STAR_SEEKBAR_COST_AMOUNT
    val starShieldsCost = StreakPreferencesRepository.SHOOTING_STAR_SEEKBAR_SHIELDS_COST
    val starAvailableKeys = forgeState.getKeyCount(starCostTier)
    val isStarUnlocked = isShootingStarPurchased || isPremiumActivated
    val canAffordStarShields = shieldsCount >= starShieldsCost
    val canAffordStarKeys = starAvailableKeys >= starCostAmount
    val canAffordStar = isStarUnlocked || (canAffordStarShields && canAffordStarKeys)
    val starTierColor = keyTierColor(starCostTier, aura.isDark)
    val isStarActive = activeSeekbarStyle == PlayerSeekbarStyle.SHOOTING_STAR

    val snakeCostTier = StreakPreferencesRepository.ANGRY_SNAKE_SEEKBAR_COST_TIER
    val snakeCostAmount = StreakPreferencesRepository.ANGRY_SNAKE_SEEKBAR_COST_AMOUNT
    val snakeShieldsCost = StreakPreferencesRepository.ANGRY_SNAKE_SEEKBAR_SHIELDS_COST
    val snakeAvailableKeys = forgeState.getKeyCount(snakeCostTier)
    val isSnakeUnlocked = isAngrySnakePurchased || isPremiumActivated
    val canAffordSnakeShields = shieldsCount >= snakeShieldsCost
    val canAffordSnakeKeys = snakeAvailableKeys >= snakeCostAmount
    val canAffordSnake = isSnakeUnlocked || (canAffordSnakeShields && canAffordSnakeKeys)
    val snakeTierColor = keyTierColor(snakeCostTier, aura.isDark)
    val isSnakeActive = activeSeekbarStyle == PlayerSeekbarStyle.ANGRY_SNAKE

    val baseProfile = remember {
        FloatArray(60) { i ->
            val t = i / 59f
            val env = sin(t * PI).toFloat().coerceAtLeast(0.18f)
            val ripple = 0.55f + 0.45f * kotlin.math.abs(sin(i * 0.48f))
            (0.24f + 0.76f * env * ripple).coerceIn(0.22f, 1f)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("seekbar_store_list"),
        contentPadding = PaddingValues(bottom = 24.dp + LocalFloatingBottomBarPadding.current),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "seekbar_store_balance") {
            ShopBalanceSummaryCard(
                totalKeys = totalKeys,
                shieldsCount = shieldsCount,
                forgeState = forgeState,
                isArabic = isArabic
            )
        }

        // Next-Gen Liquid Glass Dark Mode Seekbars (5 Interactive 60 FPS Canvas Skins)
        items(PlayerSeekbarStyle.nextGenLiquidGlassStyles, key = { "shop_liquid_seekbar_${it.id}" }) { style ->
            LiquidGlassSeekbarStoreCard(
                style = style,
                shieldsCount = shieldsCount,
                forgeState = forgeState,
                isPurchased = purchasedSeekbarIds.contains(style.id),
                isPremiumActivated = isPremiumActivated,
                isActive = activeSeekbarStyle == style,
                isArabic = isArabic,
                onPurchase = { onPurchaseStyle(style) },
                onToggle = {
                    onSelectStyle(if (activeSeekbarStyle == style) PlayerSeekbarStyle.CLASSIC else style)
                }
            )
        }

        // 1. Waveform Audio Seekbar
        item(key = "waveform_seekbar_item_card") {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = aura.surfaceCard,
                border = BorderStroke(
                    width = if (isWaveformActive) 1.6.dp else 1.2.dp,
                    brush = Brush.linearGradient(
                        if (isWaveformActive) listOf(aura.accentEmerald, aura.accentCyan)
                        else listOf(aura.accentCyan.copy(alpha = 0.65f), aura.accentGold.copy(alpha = 0.45f))
                    )
                ),
                shadowElevation = if (isWaveformActive) 10.dp else 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_waveform_seekbar_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Waveform Audio Seekbar",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.5.sp
                                    ),
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                RarityBadgePill(
                                    rarity = PlayerSeekbarStyle.WAVEFORM.rarity,
                                    isArabic = isArabic
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isArabic) {
                                    "شريط تمرير موجي متحرك (60 عموداً) بسلاسة فائقة 60 FPS"
                                } else {
                                    "60-bar animated sine-wave player seekbar • Ultra-smooth 60 FPS"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        SeekbarStatusPill(
                            isActive = isWaveformActive,
                            isPurchased = isWaveformPurchased,
                            isPremiumActivated = isPremiumActivated,
                            costLabel = if (isArabic) "$waveShieldsCost🛡️ + $waveCostAmount برونزي" else "$waveShieldsCost🛡️ + $waveCostAmount Bronze",
                            tierColor = waveTierColor,
                            isArabic = isArabic
                        )
                    }

                    // Live Animated 60-Bar Waveform Preview Stage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(104.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0A101E))
                            .border(1.dp, aura.accentCyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val previewActiveBrush = remember(aura.accentCyan, aura.accentGold) {
                            Brush.horizontalGradient(listOf(aura.accentCyan, aura.accentGold))
                        }
                        val previewInactiveColor = Color(0x3DFFFFFF)
                        val phaseRad = animPhase * 2f * PI.toFloat()
                        val previewProgress = 0.32f + 0.42f * pulsePhase

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val cy = h / 2f
                            val barCount = 60
                            val slotW = w / barCount
                            val barW = (slotW * 0.54f).coerceAtLeast(1.8.dp.toPx())
                            val maxBarH = h * 0.78f
                            val thumbX = w * previewProgress

                            for (i in 0 until barCount) {
                                val frac = (i + 0.5f) / barCount
                                val wave = 0.62f + 0.38f * sin(phaseRad * 2f + i * 0.34f)
                                val barH = (maxBarH * baseProfile[i] * wave).coerceAtLeast(4.dp.toPx())
                                val x = i * slotW + (slotW - barW) / 2f
                                val top = cy - barH / 2f
                                val corner = barW / 2f
                                val isActive = frac <= previewProgress
                                if (isActive) {
                                    drawRoundRect(
                                        brush = previewActiveBrush,
                                        topLeft = Offset(x, top),
                                        size = Size(barW, barH),
                                        cornerRadius = CornerRadius(corner, corner)
                                    )
                                } else {
                                    drawRoundRect(
                                        color = previewInactiveColor,
                                        topLeft = Offset(x, top),
                                        size = Size(barW, barH),
                                        cornerRadius = CornerRadius(corner, corner)
                                    )
                                }
                            }

                            drawCircle(
                                color = aura.accentCyan.copy(alpha = 0.35f),
                                radius = 11.dp.toPx(),
                                center = Offset(thumbX, cy)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 5.5.dp.toPx(),
                                center = Offset(thumbX, cy)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = aura.surfaceSubtle,
                        border = BorderStroke(0.8.dp, aura.borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isArabic) {
                                "يستبدل شريط التمرير الخطي العادي داخل مشغّل الفيديو بموجة صوتية متفاعلة من ~60 عموداً مضيئاً تنبض مع التشغيل وتستجيب للمس والسحب الفوري."
                            } else {
                                "Replaces the standard linear video player slider with ~60 glowing sine-wave vertical bars supporting instant touch-down and smooth horizontal drag seeking."
                            },
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                lineHeight = 18.sp,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    SeekbarPriceOrFreeRow(
                        isPurchased = isWaveformPurchased,
                        isPremiumActivated = isPremiumActivated,
                        shieldsCost = waveShieldsCost,
                        availableShields = shieldsCount,
                        canAffordShields = canAffordWaveShields,
                        keyCostText = if (isArabic) "🗝️ $waveCostAmount مفتاح برونزي" else "🗝️ $waveCostAmount Bronze Keys",
                        availableKeys = waveAvailableKeys,
                        canAffordKeys = canAffordWaveKeys,
                        tierColor = waveTierColor,
                        isArabic = isArabic
                    )

                    SeekbarActionButton(
                        isUnlocked = isWaveformUnlocked,
                        isPurchased = isWaveformPurchased,
                        isActive = isWaveformActive,
                        isPremiumActivated = isPremiumActivated,
                        canAfford = canAffordWave,
                        buyLabel = if (isArabic) "شراء وتفعيل ($waveShieldsCost 🛡️ + $waveCostAmount برونزي)" else "Buy & Activate ($waveShieldsCost 🛡️ + $waveCostAmount Bronze)",
                        activateLabel = if (isArabic) "تفعيل شريط الموجة في المشغّل" else "Activate Waveform Seekbar",
                        buyTestTag = "buy_waveform_seekbar_button",
                        toggleTestTag = "toggle_waveform_seekbar_button",
                        onPurchase = { onPurchaseStyle(PlayerSeekbarStyle.WAVEFORM) },
                        onToggle = {
                            onSelectStyle(if (isWaveformActive) PlayerSeekbarStyle.CLASSIC else PlayerSeekbarStyle.WAVEFORM)
                        },
                        isArabic = isArabic
                    )
                }
            }
        }

        // 2. Shooting Star Seekbar (Laser Projectiles Colliding with Current Video Position)
        item(key = "shooting_star_seekbar_item_card") {
            val laserCyan = Color(0xFF00F0FF)
            val laserMagenta = Color(0xFFFF4FD8)
            val starGold = Color(0xFFFFD700)

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = aura.surfaceCard,
                border = BorderStroke(
                    width = if (isStarActive) 1.7.dp else 1.2.dp,
                    brush = Brush.linearGradient(
                        if (isStarActive) listOf(aura.accentEmerald, laserCyan, laserMagenta)
                        else listOf(laserCyan.copy(alpha = 0.72f), laserMagenta.copy(alpha = 0.62f), starGold.copy(alpha = 0.55f))
                    )
                ),
                shadowElevation = if (isStarActive) 12.dp else 5.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_shooting_star_seekbar_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Shooting Star Seekbar",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.5.sp
                                    ),
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                RarityBadgePill(
                                    rarity = PlayerSeekbarStyle.SHOOTING_STAR.rarity,
                                    isArabic = isArabic
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isArabic) {
                                    "أضواء ليزرية تقذف وتصطدم بنقطة تقدم الفيديو بانفجار نجمي"
                                } else {
                                    "Laser bolts fire & collide into current playback position"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        SeekbarStatusPill(
                            isActive = isStarActive,
                            isPurchased = isShootingStarPurchased,
                            isPremiumActivated = isPremiumActivated,
                            costLabel = if (isArabic) "$starShieldsCost🛡️ + $starCostAmount فضي" else "$starShieldsCost🛡️ + $starCostAmount Silver",
                            tierColor = starTierColor,
                            isArabic = isArabic
                        )
                    }

                    // Live Animated Shooting Star Laser Collision Preview Stage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF050814), Color(0xFF0B1126), Color(0xFF050814))
                                )
                            )
                            .border(
                                1.1.dp,
                                Brush.horizontalGradient(listOf(laserCyan.copy(alpha = 0.45f), laserMagenta.copy(alpha = 0.45f))),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val laserTrackBrush = remember {
                            Brush.horizontalGradient(listOf(laserCyan, Color(0xFF6366F1), laserMagenta))
                        }

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val pad = 8.dp.toPx()
                            val trackW = (size.width - pad * 2f).coerceAtLeast(1f)
                            val cy = size.height / 2f
                            val progress = 0.36f + 0.40f * pulsePhase
                            val thumbX = pad + trackW * progress

                            // Subtle cosmic starfield specks
                            for (s in 0 until 14) {
                                val sx = pad + ((s * 0.173f) % 1f) * trackW
                                val sy = (0.14f + ((s * 0.291f) % 0.72f)) * size.height
                                val twinkle = 0.25f + 0.65f * kotlin.math.abs(sin(animPhase * 2f * PI.toFloat() + s * 1.1f))
                                drawCircle(
                                    color = Color.White.copy(alpha = twinkle * 0.65f),
                                    radius = if (s % 3 == 0) 1.6.dp.toPx() else 1.0.dp.toPx(),
                                    center = Offset(sx, sy)
                                )
                            }

                            // Unplayed dark star rail
                            drawLine(
                                color = Color(0x2BFFFFFF),
                                start = Offset(pad, cy),
                                end = Offset(pad + trackW, cy),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Charged laser track aura + core
                            drawLine(
                                color = laserCyan.copy(alpha = 0.25f),
                                start = Offset(pad, cy),
                                end = Offset(thumbX, cy),
                                strokeWidth = 10.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                brush = laserTrackBrush,
                                start = Offset(pad, cy),
                                end = Offset(thumbX, cy),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.85f),
                                start = Offset(pad, cy),
                                end = Offset(thumbX, cy),
                                strokeWidth = 1.4.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Laser bolts shooting along the beam + diagonal cosmic lasers impacting at thumbX
                            var peakImpact = 0f
                            val boltCount = 4
                            for (b in 0 until boltCount) {
                                val t = ((animPhase * 2f) + b * (1f / boltCount)) % 1f
                                val eased = t * (0.35f + 0.65f * t)
                                val headX = pad + (thumbX - pad) * eased
                                val tailLen = ((thumbX - pad) * 0.24f).coerceIn(14.dp.toPx(), 44.dp.toPx())
                                val tailX = (headX - tailLen).coerceAtLeast(pad)
                                val boltColor = if (b % 2 == 0) laserCyan else laserMagenta

                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color.Transparent, boltColor.copy(alpha = 0.85f), Color.White),
                                        startX = tailX,
                                        endX = headX
                                    ),
                                    start = Offset(tailX, cy),
                                    end = Offset(headX, cy),
                                    strokeWidth = 5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.6.dp.toPx(),
                                    center = Offset(headX, cy)
                                )

                                if (t > 0.84f) {
                                    val imp = ((t - 0.84f) / 0.16f).coerceIn(0f, 1f)
                                    if (imp > peakImpact) peakImpact = imp
                                }
                            }

                            // Two converging diagonal laser streaks striking thumbX from cosmic space
                            for (d in 0 until 2) {
                                val dt = ((animPhase * 1.5f) + d * 0.5f) % 1f
                                val signY = if (d == 0) -1f else 1f
                                val startOrigin = Offset((thumbX - 64.dp.toPx()).coerceAtLeast(pad), cy + signY * 26.dp.toPx())
                                val curX = startOrigin.x + (thumbX - startOrigin.x) * dt
                                val curY = startOrigin.y + (cy - startOrigin.y) * dt
                                val tailT = (dt - 0.28f).coerceAtLeast(0f)
                                val tX = startOrigin.x + (thumbX - startOrigin.x) * tailT
                                val tY = startOrigin.y + (cy - startOrigin.y) * tailT
                                val diagColor = if (d == 0) starGold else laserMagenta
                                drawLine(
                                    color = diagColor.copy(alpha = 0.85f),
                                    start = Offset(tX, tY),
                                    end = Offset(curX, curY),
                                    strokeWidth = 2.4.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.2.dp.toPx(),
                                    center = Offset(curX, curY)
                                )
                                if (dt > 0.85f) {
                                    val imp = ((dt - 0.85f) / 0.15f).coerceIn(0f, 1f)
                                    if (imp > peakImpact) peakImpact = imp
                                }
                            }

                            // Collision shockwave & radial laser sparks at thumbX
                            if (peakImpact > 0.01f) {
                                val shockRadius = 7.dp.toPx() + 18.dp.toPx() * peakImpact
                                val shockAlpha = (1f - peakImpact).coerceIn(0f, 1f)
                                drawCircle(
                                    color = laserCyan.copy(alpha = shockAlpha * 0.85f),
                                    radius = shockRadius,
                                    center = Offset(thumbX, cy),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                for (sp in 0 until 8) {
                                    val angle = (sp * (PI / 4.0) + animPhase * 0.6).toFloat()
                                    val innerR = 5.dp.toPx() + 6.dp.toPx() * peakImpact
                                    val outerR = innerR + 10.dp.toPx() * (1f - peakImpact * 0.35f)
                                    drawLine(
                                        color = (if (sp % 2 == 0) starGold else laserMagenta).copy(alpha = shockAlpha),
                                        start = Offset(thumbX + cos(angle) * innerR, cy + sin(angle) * innerR),
                                        end = Offset(thumbX + cos(angle) * outerR, cy + sin(angle) * outerR),
                                        strokeWidth = 1.8.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            // Target collision star core at current position thumbX
                            drawCircle(
                                color = laserMagenta.copy(alpha = 0.36f + 0.25f * peakImpact),
                                radius = 14.dp.toPx() + 4.dp.toPx() * peakImpact,
                                center = Offset(thumbX, cy)
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.9f),
                                start = Offset(thumbX - 11.dp.toPx(), cy),
                                end = Offset(thumbX + 11.dp.toPx(), cy),
                                strokeWidth = 1.8.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.9f),
                                start = Offset(thumbX, cy - 11.dp.toPx()),
                                end = Offset(thumbX, cy + 11.dp.toPx()),
                                strokeWidth = 1.8.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = laserCyan,
                                radius = 7.dp.toPx(),
                                center = Offset(thumbX, cy)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = Offset(thumbX, cy)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = aura.surfaceSubtle,
                        border = BorderStroke(0.8.dp, aura.borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isArabic) {
                                "شريط تمرير سينمائي يطلق حزم ليزر ضوئية وشُهب سماوية تنطلق باستمرار وتصطدم بنقطة تقدم الفيديو الحالية مع موجات صدمة وشرر متوهج بسلاسة 60 FPS."
                            } else {
                                "Cinematic plasma-laser seekbar firing streaking light bolts & shooting stars that collide directly into your current video position with shockwaves and sparks at 60 FPS."
                            },
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                lineHeight = 18.sp,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    SeekbarPriceOrFreeRow(
                        isPurchased = isShootingStarPurchased,
                        isPremiumActivated = isPremiumActivated,
                        shieldsCost = starShieldsCost,
                        availableShields = shieldsCount,
                        canAffordShields = canAffordStarShields,
                        keyCostText = if (isArabic) "🗝️ $starCostAmount مفتاح فضي" else "🗝️ $starCostAmount Silver Keys",
                        availableKeys = starAvailableKeys,
                        canAffordKeys = canAffordStarKeys,
                        tierColor = starTierColor,
                        isArabic = isArabic
                    )

                    SeekbarActionButton(
                        isUnlocked = isStarUnlocked,
                        isPurchased = isShootingStarPurchased,
                        isActive = isStarActive,
                        isPremiumActivated = isPremiumActivated,
                        canAfford = canAffordStar,
                        buyLabel = if (isArabic) "شراء وتفعيل ($starShieldsCost 🛡️ + $starCostAmount فضي)" else "Buy & Activate ($starShieldsCost 🛡️ + $starCostAmount Silver)",
                        activateLabel = if (isArabic) "تفعيل Shooting Star Seekbar" else "Activate Shooting Star Seekbar",
                        buyTestTag = "buy_shooting_star_seekbar_button",
                        toggleTestTag = "toggle_shooting_star_seekbar_button",
                        onPurchase = { onPurchaseStyle(PlayerSeekbarStyle.SHOOTING_STAR) },
                        onToggle = {
                            onSelectStyle(if (isStarActive) PlayerSeekbarStyle.CLASSIC else PlayerSeekbarStyle.SHOOTING_STAR)
                        },
                        isArabic = isArabic
                    )
                }
            }
        }

        // 3. Angry Snake Seekbar (Slithering Viper Hunting Orbs with Fangs & Rage Mode)
        item(key = "angry_snake_seekbar_item_card") {
            val venomGreen = Color(0xFF22C55E)
            val acidLime = Color(0xFFA3E635)
            val rageCrimson = Color(0xFFFF2E4C)
            val rageEmber = Color(0xFFFF8A00)
            val darkScale = Color(0xFF0A2414)
            val previewViperHead = remember { Path() }
            val previewTonguePath = remember { Path() }
            val previewScalePath = remember { Path() }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = aura.surfaceCard,
                border = BorderStroke(
                    width = if (isSnakeActive) 1.7.dp else 1.2.dp,
                    brush = Brush.linearGradient(
                        if (isSnakeActive) listOf(aura.accentEmerald, venomGreen, rageCrimson)
                        else listOf(venomGreen.copy(alpha = 0.72f), rageEmber.copy(alpha = 0.60f), rageCrimson.copy(alpha = 0.65f))
                    )
                ),
                shadowElevation = if (isSnakeActive) 12.dp else 5.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_angry_snake_seekbar_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Angry Snake Seekbar",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.5.sp
                                    ),
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                RarityBadgePill(
                                    rarity = PlayerSeekbarStyle.ANGRY_SNAKE.rarity,
                                    isArabic = isArabic
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isArabic) {
                                    "أفعى غاضبة تزحف وتنقض على كرات الطاقة مع أنياب ولسان متشعب"
                                } else {
                                    "Slithering viper that hunts energy orbs with fangs & rage bursts"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        SeekbarStatusPill(
                            isActive = isSnakeActive,
                            isPurchased = isAngrySnakePurchased,
                            isPremiumActivated = isPremiumActivated,
                            costLabel = if (isArabic) "$snakeShieldsCost🛡️ + $snakeCostAmount ذهبي" else "$snakeShieldsCost🛡️ + $snakeCostAmount Gold",
                            tierColor = snakeTierColor,
                            isArabic = isArabic
                        )
                    }

                    // Live Animated Angry Snake Preview Stage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF06110B), Color(0xFF0C1D13), Color(0xFF070F0A))
                                )
                            )
                            .border(
                                1.1.dp,
                                Brush.horizontalGradient(listOf(venomGreen.copy(alpha = 0.50f), rageCrimson.copy(alpha = 0.50f))),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val pad = 12.dp.toPx()
                            val trackW = (size.width - pad * 2f).coerceAtLeast(1f)
                            val cy = size.height / 2f
                            val progress = 0.34f + 0.42f * pulsePhase
                            val headX = pad + trackW * progress
                            val rage = 0.28f + 0.72f * max(0f, sin(animPhase * 2f * PI.toFloat()))

                            // Unplayed venom rail
                            drawLine(
                                color = Color(0x2E22C55E),
                                start = Offset(pad, cy),
                                end = Offset(pad + trackW, cy),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Prey Orbs along unplayed rail
                            val orbs = floatArrayOf(0.45f, 0.62f, 0.78f, 0.92f)
                            for (o in orbs) {
                                val ox = pad + trackW * o
                                if (ox > headX + 4.dp.toPx()) {
                                    val dist = ox - headX
                                    val danger = if (dist < 26.dp.toPx()) 1f - dist / 26.dp.toPx() else 0f
                                    drawCircle(
                                        color = rageCrimson.copy(alpha = 0.25f + 0.30f * danger),
                                        radius = 5.dp.toPx() + 2.dp.toPx() * danger,
                                        center = Offset(ox, cy)
                                    )
                                    drawCircle(
                                        color = lerp(rageEmber, rageCrimson, danger),
                                        radius = 2.6.dp.toPx(),
                                        center = Offset(ox, cy)
                                    )
                                }
                            }

                            // 22-segment slithering snake body
                            val segs = 22
                            val span = (headX - pad).coerceAtLeast(1f)
                            val maxAmp = 4.8.dp.toPx() + 2.6.dp.toPx() * rage
                            val pts = Array(segs + 1) { idx ->
                                val t = idx.toFloat() / segs.toFloat()
                                val x = pad + span * t
                                val env = sin(t * PI.toFloat()).coerceAtLeast(0.15f) * (0.35f + 0.65f * t)
                                val y = cy + sin(animPhase * 4f * PI.toFloat() - (1f - t) * 8.5f) * maxAmp * env
                                Offset(x, y)
                            }
                            val neck = pts[segs - 1]
                            val head = pts[segs]
                            val headDeg = (atan2((head.y - neck.y).toDouble(), (head.x - neck.x).toDouble()).toFloat() * 180f / PI.toFloat())
                                .coerceIn(-32f, 32f)

                            for (i in 0 until segs) {
                                val p0 = pts[i]
                                val p1 = pts[i + 1]
                                val t = (i + 0.5f) / segs.toFloat()
                                val sw = 2.4.dp.toPx() + 4.2.dp.toPx() * sin(t * PI.toFloat() * 0.72f + 0.25f)
                                val c = lerp(venomGreen, rageCrimson, (rage * (0.4f + 0.6f * t)).coerceIn(0f, 1f))
                                drawLine(
                                    color = c.copy(alpha = 0.22f + 0.20f * rage),
                                    start = p0,
                                    end = p1,
                                    strokeWidth = sw + 5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = darkScale,
                                    start = p0,
                                    end = p1,
                                    strokeWidth = sw + 1.4.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = c,
                                    start = p0,
                                    end = p1,
                                    strokeWidth = sw,
                                    cap = StrokeCap.Round
                                )
                                if (i % 2 == 0 && t > 0.12f) {
                                    val mid = Offset((p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f)
                                    val r = sw * 0.34f
                                    previewScalePath.reset()
                                    previewScalePath.moveTo(mid.x - r, mid.y)
                                    previewScalePath.lineTo(mid.x, mid.y - r * 0.75f)
                                    previewScalePath.lineTo(mid.x + r, mid.y)
                                    previewScalePath.lineTo(mid.x, mid.y + r * 0.75f)
                                    previewScalePath.close()
                                    drawPath(previewScalePath, color = lerp(acidLime, rageEmber, rage))
                                }
                            }

                            // Viper head + flickering forked tongue
                            rotate(degrees = headDeg, pivot = head) {
                                val hLen = 10.5.dp.toPx()
                                val hBack = 7.5.dp.toPx()
                                val hWidth = 6.8.dp.toPx()

                                val tExt = 5.dp.toPx() + 6.5.dp.toPx() * abs(sin(animPhase * 6f * PI.toFloat()))
                                val sX = head.x + hLen * 0.78f
                                val fX = sX + tExt * 0.62f
                                val eX = sX + tExt
                                previewTonguePath.reset()
                                previewTonguePath.moveTo(sX, head.y)
                                previewTonguePath.lineTo(fX, head.y)
                                previewTonguePath.moveTo(fX, head.y)
                                previewTonguePath.lineTo(eX, head.y - 2.6.dp.toPx())
                                previewTonguePath.moveTo(fX, head.y)
                                previewTonguePath.lineTo(eX, head.y + 2.6.dp.toPx())
                                drawPath(
                                    path = previewTonguePath,
                                    color = rageCrimson,
                                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )

                                previewViperHead.reset()
                                previewViperHead.moveTo(head.x + hLen, head.y)
                                previewViperHead.lineTo(head.x + hLen * 0.15f, head.y - hWidth * 0.88f)
                                previewViperHead.lineTo(head.x - hBack, head.y - hWidth)
                                previewViperHead.lineTo(head.x - hBack * 0.55f, head.y)
                                previewViperHead.lineTo(head.x - hBack, head.y + hWidth)
                                previewViperHead.lineTo(head.x + hLen * 0.15f, head.y + hWidth * 0.88f)
                                previewViperHead.close()

                                drawPath(previewViperHead, color = lerp(venomGreen, rageCrimson, rage * 0.75f))
                                drawPath(previewViperHead, color = darkScale, style = Stroke(width = 1.5.dp.toPx(), join = StrokeJoin.Round))

                                val eyeX = head.x + hLen * 0.08f
                                val eyeOff = hWidth * 0.44f
                                for (side in listOf(-1f, 1f)) {
                                    val ey = head.y + side * eyeOff
                                    drawCircle(
                                        color = lerp(Color(0xFFFACC15), Color(0xFFFF1744), rage),
                                        radius = 1.9.dp.toPx(),
                                        center = Offset(eyeX, ey)
                                    )
                                    drawLine(
                                        color = Color.Black,
                                        start = Offset(eyeX - 0.9.dp.toPx(), ey),
                                        end = Offset(eyeX + 0.9.dp.toPx(), ey),
                                        strokeWidth = 1.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = darkScale,
                                        start = Offset(eyeX - 2.3.dp.toPx(), ey + side * 2.1.dp.toPx()),
                                        end = Offset(eyeX + 2.1.dp.toPx(), ey + side * 0.6.dp.toPx()),
                                        strokeWidth = 1.5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = aura.surfaceSubtle,
                        border = BorderStroke(0.8.dp, aura.borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isArabic) {
                                "أفعى حية بـ 26 مفصلاً تتلوى على مسار الفيديو بأنياب سامة ولسان متشعب؛ تطارد كرات الطاقة وتتحول إلى اللون القرمزي الغاضب مع اهتزاز الذيل عند السحب، وتهدأ تماماً عند إيقاف الفيديو."
                            } else {
                                "A 26-segment articulated viper slithering across your timeline with venom fangs & a flickering forked tongue; lunges at energy orbs, ignites into crimson rage with tail rattle when scrubbed, and calms smoothly on pause."
                            },
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                lineHeight = 18.sp,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    SeekbarPriceOrFreeRow(
                        isPurchased = isAngrySnakePurchased,
                        isPremiumActivated = isPremiumActivated,
                        shieldsCost = snakeShieldsCost,
                        availableShields = shieldsCount,
                        canAffordShields = canAffordSnakeShields,
                        keyCostText = if (isArabic) "🗝️ $snakeCostAmount مفتاح ذهبي" else "🗝️ $snakeCostAmount Gold Keys",
                        availableKeys = snakeAvailableKeys,
                        canAffordKeys = canAffordSnakeKeys,
                        tierColor = snakeTierColor,
                        isArabic = isArabic
                    )

                    SeekbarActionButton(
                        isUnlocked = isSnakeUnlocked,
                        isPurchased = isAngrySnakePurchased,
                        isActive = isSnakeActive,
                        isPremiumActivated = isPremiumActivated,
                        canAfford = canAffordSnake,
                        buyLabel = if (isArabic) "شراء وتفعيل ($snakeShieldsCost 🛡️ + $snakeCostAmount ذهبي)" else "Buy & Activate ($snakeShieldsCost 🛡️ + $snakeCostAmount Gold)",
                        activateLabel = if (isArabic) "تفعيل Angry Snake Seekbar" else "Activate Angry Snake Seekbar",
                        buyTestTag = "buy_angry_snake_seekbar_button",
                        toggleTestTag = "toggle_angry_snake_seekbar_button",
                        onPurchase = { onPurchaseStyle(PlayerSeekbarStyle.ANGRY_SNAKE) },
                        onToggle = {
                            onSelectStyle(if (isSnakeActive) PlayerSeekbarStyle.CLASSIC else PlayerSeekbarStyle.ANGRY_SNAKE)
                        },
                        isArabic = isArabic
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidGlassSeekbarStoreCard(
    style: PlayerSeekbarStyle,
    shieldsCount: Int,
    forgeState: ForgeUiState,
    isPurchased: Boolean,
    isPremiumActivated: Boolean,
    isActive: Boolean,
    isArabic: Boolean,
    onPurchase: () -> Unit,
    onToggle: () -> Unit
) {
    val aura = MaterialTheme.aura
    val primary = Color(style.primaryAccentHex)
    val secondary = Color(style.secondaryAccentHex)
    val costTier = style.costTier ?: ForgeKeyTier.BRONZE
    val tierColor = keyTierColor(costTier, aura.isDark)
    val availableKeys = forgeState.getKeyCount(costTier)
    val isUnlocked = isPurchased || isPremiumActivated
    val canAffordShields = shieldsCount >= style.shieldsCost
    val canAffordKeys = availableKeys >= style.costAmount
    val canAfford = isUnlocked || (canAffordShields && canAffordKeys)

    var previewProgress by remember(style.id) { mutableFloatStateOf(0.64f) }
    var isPreviewDragging by remember(style.id) { mutableStateOf(false) }

    val tierWord = when (costTier) {
        ForgeKeyTier.BRONZE -> if (isArabic) "برونزي" else "Bronze"
        ForgeKeyTier.SILVER -> if (isArabic) "فضي" else "Silver"
        ForgeKeyTier.GOLD -> if (isArabic) "ذهبي" else "Gold"
        ForgeKeyTier.DIAMOND -> if (isArabic) "ماسي" else "Diamond"
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = aura.surfaceCard,
        border = BorderStroke(
            width = if (isActive) 1.7.dp else 1.2.dp,
            brush = Brush.linearGradient(
                if (isActive) listOf(aura.accentEmerald, primary)
                else listOf(primary.copy(alpha = 0.65f), secondary.copy(alpha = 0.50f))
            )
        ),
        shadowElevation = if (isActive) 10.dp else 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shop_${style.id}_seekbar_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = style.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.5.sp
                            ),
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        RarityBadgePill(
                            rarity = style.rarity,
                            isArabic = isArabic
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isArabic) style.subtitleAr else style.subtitleEn,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            fontSize = 11.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                SeekbarStatusPill(
                    isActive = isActive,
                    isPurchased = isPurchased,
                    isPremiumActivated = isPremiumActivated,
                    costLabel = "${style.shieldsCost}🛡️ + ${style.costAmount} $tierWord",
                    tierColor = tierColor,
                    isArabic = isArabic
                )
            }

            // OLED #000000 Liquid Glass Interactive Preview Stage (Drag to test 60 FPS scrubbing!)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF000000))
                    .border(1.dp, primary.copy(alpha = 0.42f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LiquidGlassSeekbarPreview(
                    style = style,
                    progressRatio = previewProgress,
                    isPlaying = true,
                    isDragging = isPreviewDragging,
                    isLocked = false,
                    onProgressChange = { previewProgress = it },
                    onDraggingChange = { isPreviewDragging = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
                Text(
                    text = if (isArabic) "اسحب الشريط لتجربة التفاعل الحي 60 FPS" else "Drag the timeline to test interactive 60 FPS scrubbing",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 9.5.sp
                    )
                )
            }

            SeekbarPriceOrFreeRow(
                isPurchased = isPurchased,
                isPremiumActivated = isPremiumActivated,
                shieldsCost = style.shieldsCost,
                availableShields = shieldsCount,
                canAffordShields = canAffordShields,
                keyCostText = if (isArabic) "🗝️ ${style.costAmount} مفتاح $tierWord" else "🗝️ ${style.costAmount} $tierWord Keys",
                availableKeys = availableKeys,
                canAffordKeys = canAffordKeys,
                tierColor = tierColor,
                isArabic = isArabic
            )

            SeekbarActionButton(
                isUnlocked = isUnlocked,
                isPurchased = isPurchased,
                isActive = isActive,
                isPremiumActivated = isPremiumActivated,
                canAfford = canAfford,
                buyLabel = if (isArabic) {
                    "شراء وتفعيل (${style.shieldsCost} 🛡️ + ${style.costAmount} $tierWord)"
                } else {
                    "Buy & Activate (${style.shieldsCost} 🛡️ + ${style.costAmount} $tierWord)"
                },
                activateLabel = if (isArabic) "تفعيل ${style.displayNameAr}" else "Activate ${style.displayName}",
                buyTestTag = "buy_${style.id}_seekbar_button",
                toggleTestTag = "toggle_${style.id}_seekbar_button",
                onPurchase = onPurchase,
                onToggle = onToggle,
                isArabic = isArabic
            )
        }
    }
}

@Composable
private fun SeekbarStatusPill(
    isActive: Boolean,
    isPurchased: Boolean,
    isPremiumActivated: Boolean,
    costLabel: String,
    tierColor: Color,
    isArabic: Boolean
) {
    val aura = MaterialTheme.aura
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = when {
            isActive -> aura.accentEmeraldSoft
            isPurchased -> aura.accentCyanSoft
            isPremiumActivated -> aura.accentGoldBg
            else -> tierColor.copy(alpha = 0.16f)
        },
        border = BorderStroke(
            1.dp,
            when {
                isActive -> aura.accentEmerald
                isPurchased -> aura.accentCyan
                isPremiumActivated -> aura.accentGold
                else -> tierColor.copy(alpha = 0.65f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    isPurchased || isActive -> Icons.Default.CheckCircle
                    isPremiumActivated -> Icons.Default.AutoAwesome
                    else -> Icons.Default.VpnKey
                },
                contentDescription = null,
                tint = when {
                    isActive -> aura.accentEmerald
                    isPurchased -> aura.accentCyan
                    isPremiumActivated -> aura.accentGold
                    else -> tierColor
                },
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when {
                    isActive -> if (isArabic) "مُفعّل ✓" else "Active ✓"
                    isPurchased -> if (isArabic) "مملوك ✓" else "Owned ✓"
                    isPremiumActivated -> if (isArabic) "مجاني 👑" else "FREE 👑"
                    else -> costLabel
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = when {
                        isActive -> aura.accentEmerald
                        isPurchased -> aura.accentCyan
                        isPremiumActivated -> aura.accentGold
                        else -> tierColor
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 10.5.sp
                )
            )
        }
    }
}

@Composable
private fun SeekbarPriceOrFreeRow(
    isPurchased: Boolean,
    isPremiumActivated: Boolean,
    shieldsCost: Int,
    availableShields: Int,
    canAffordShields: Boolean,
    keyCostText: String,
    availableKeys: Int,
    canAffordKeys: Boolean,
    tierColor: Color,
    isArabic: Boolean
) {
    val aura = MaterialTheme.aura
    if (!isPurchased && !isPremiumActivated) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = tierColor.copy(alpha = 0.10f),
            border = BorderStroke(0.9.dp, tierColor.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isArabic) "🛡️ الدروع: $shieldsCost درع" else "🛡️ Shields: $shieldsCost",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = aura.accentEmerald,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = if (isArabic) "رصيدك: $availableShields" else "Balance: $availableShields",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (canAffordShields) aura.accentEmerald else aura.accentFlame,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = keyCostText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = tierColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = if (isArabic) "رصيدك: $availableKeys" else "Balance: $availableKeys",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (canAffordKeys) aura.accentEmerald else aura.accentFlame,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    } else if (isPremiumActivated && !isPurchased) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = aura.accentGoldBg,
            border = BorderStroke(0.9.dp, aura.accentGold.copy(alpha = 0.55f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isArabic) "مفتوح مجاناً عبر Premium 👑" else "Unlocked FREE via Premium 👑",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = aura.accentGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = if (isArabic) "0 درع • 0 مفاتيح" else "0 Shields • 0 Keys",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = aura.accentEmerald,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun SeekbarActionButton(
    isUnlocked: Boolean,
    isPurchased: Boolean,
    isActive: Boolean,
    isPremiumActivated: Boolean,
    canAfford: Boolean,
    buyLabel: String,
    activateLabel: String,
    buyTestTag: String,
    toggleTestTag: String,
    onPurchase: () -> Unit,
    onToggle: () -> Unit,
    isArabic: Boolean
) {
    val aura = MaterialTheme.aura
    if (!isUnlocked) {
        Button(
            onClick = onPurchase,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canAfford) aura.accentGold else aura.surfaceSubtle,
                contentColor = if (canAfford) Color(0xFF07090D) else aura.textSecondary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag(buyTestTag)
        ) {
            Icon(
                imageVector = if (canAfford) Icons.Default.VpnKey else Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = buyLabel,
                fontWeight = FontWeight.Black,
                fontSize = 13.5.sp
            )
        }
    } else {
        Button(
            onClick = onToggle,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) aura.accentEmerald else if (isPremiumActivated) aura.accentGold else aura.accentCyan,
                contentColor = Color(0xFF07090D)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag(toggleTestTag)
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Tune,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isActive) {
                    if (isArabic) "مُفعّل في المشغّل ✓ — إيقاف مؤقت" else "Active in Player ✓ — Turn Off"
                } else if (isPremiumActivated && !isPurchased) {
                    if (isArabic) "تفعيل مجاناً الآن 👑" else "Activate Free (Premium 👑)"
                } else {
                    activateLabel
                },
                fontWeight = FontWeight.Black,
                fontSize = 13.5.sp
            )
        }
    }
}

@Composable
private fun MimicStoreSection(
    totalKeys: Int,
    shieldsCount: Int,
    forgeState: ForgeUiState,
    streakDays: Int,
    dailyWatchProgress: Float,
    isPlayerActive: Boolean,
    equippedHomeMimic: HomeMimicItem,
    isHomeMimicVisible: Boolean,
    isFlameEnabled: Boolean,
    isDnaPurchased: Boolean,
    isDnaEnabled: Boolean,
    purchasedMimicIds: Set<String>,
    isPremiumActivated: Boolean,
    animPhase: Float,
    pulsePhase: Float,
    isArabic: Boolean,
    onPurchaseHomeMimic: (HomeMimicItem) -> Unit,
    onEquipHomeMimic: (HomeMimicItem) -> Unit,
    onToggleHomeMimicVisible: (Boolean) -> Unit,
    onToggleFlame: (Boolean) -> Unit,
    onPurchaseDna: () -> Unit,
    onToggleDna: (Boolean) -> Unit
) {
    val aura = MaterialTheme.aura
    val clampedDays = streakDays.coerceIn(0, MIMIC_MAX_THAW_STREAK_DAYS)
    val streakProgress = clampedDays / MIMIC_MAX_THAW_STREAK_DAYS.toFloat()
    val isFrozen = clampedDays == 0

    val dnaCostTier = StreakPreferencesRepository.DNA_MIMIC_COST_TIER
    val dnaCostAmount = StreakPreferencesRepository.DNA_MIMIC_COST_AMOUNT
    val dnaShieldsCost = StreakPreferencesRepository.DNA_MIMIC_SHIELDS_COST
    val dnaAvailableKeys = forgeState.getKeyCount(dnaCostTier)
    val isDnaUnlocked = isDnaPurchased || isPremiumActivated
    val canAffordDnaShields = shieldsCount >= dnaShieldsCost
    val canAffordDnaKeys = dnaAvailableKeys >= dnaCostAmount
    val canAffordDna = isDnaUnlocked || (canAffordDnaShields && canAffordDnaKeys)
    val dnaTierColor = keyTierColor(dnaCostTier, aura.isDark)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mimic_store_list"),
        contentPadding = PaddingValues(bottom = 24.dp + LocalFloatingBottomBarPadding.current),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "mimic_store_balance") {
            ShopBalanceSummaryCard(
                totalKeys = totalKeys,
                shieldsCount = shieldsCount,
                forgeState = forgeState,
                isArabic = isArabic
            )
        }

        // Quick Switcher & Home Visibility Control (Filtered to owned/unlocked Mimics)
        item(key = "mimic_quick_switcher_panel") {
            val availableMimics = buildList {
                add(HomeMimicItem.STREAK_FLAME)
                HomeMimicItem.nextGenMimics.forEach { mimic ->
                    if (purchasedMimicIds.contains(mimic.id) || isPremiumActivated) {
                        add(mimic)
                    }
                }
                if (isDnaPurchased || isPremiumActivated) {
                    add(HomeMimicItem.DNA_CHROMOSOME)
                }
            }
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = aura.surfaceCard,
                border = BorderStroke(
                    1.1.dp,
                    Brush.horizontalGradient(
                        listOf(
                            Color(equippedHomeMimic.primaryAccentHex).copy(alpha = 0.65f),
                            Color(equippedHomeMimic.secondaryAccentHex).copy(alpha = 0.55f)
                        )
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mimic_quick_switcher_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "التبديل السريع لمجسمات Home" else "Quick Home Switcher",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isArabic) {
                                    "تجهيز فوري أو إخفاء المجسم من الشاشة الرئيسية"
                                } else {
                                    "Tap to equip instantly or toggle Home visibility"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isHomeMimicVisible) aura.accentEmeraldSoft else aura.surfaceSubtle,
                            border = BorderStroke(
                                1.dp,
                                if (isHomeMimicVisible) aura.accentEmerald.copy(alpha = 0.65f) else aura.borderSubtle
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onToggleHomeMimicVisible(!isHomeMimicVisible) }
                                .testTag("mimic_visibility_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isHomeMimicVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (isHomeMimicVisible) aura.accentEmerald else aura.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isHomeMimicVisible) {
                                        if (isArabic) "مفعّل" else "Visible"
                                    } else {
                                        if (isArabic) "مخفي" else "Hidden"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isHomeMimicVisible) aura.accentEmerald else aura.textSecondary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableMimics, key = { "quick_${it.id}" }) { item ->
                            val isQuickEquipped = isHomeMimicVisible && equippedHomeMimic == item
                            val primary = Color(item.primaryAccentHex)
                            val secondary = Color(item.secondaryAccentHex)

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isQuickEquipped) Color(0xFF050811) else Color(0xFF03050A),
                                border = BorderStroke(
                                    width = if (isQuickEquipped) 1.7.dp else 1.dp,
                                    brush = Brush.linearGradient(
                                        if (isQuickEquipped) listOf(primary, secondary)
                                        else listOf(Color.White.copy(alpha = 0.16f), primary.copy(alpha = 0.3f))
                                    )
                                ),
                                modifier = Modifier
                                    .width(138.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { onEquipHomeMimic(item) }
                                    .testTag("equip_mimic_deck_${item.id}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(86.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF000000))
                                            .border(
                                                0.9.dp,
                                                primary.copy(alpha = if (isQuickEquipped) 0.7f else 0.28f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        HomeMimicLiveCanvasPreview(
                                            mimicItem = item,
                                            streakDays = MIMIC_MAX_THAW_STREAK_DAYS,
                                            dailyProgress = dailyWatchProgress,
                                            isPlayerActive = isPlayerActive,
                                            isCompact = true,
                                            modifier = Modifier.size(82.dp)
                                        )
                                    }

                                    Text(
                                        text = if (isArabic) item.displayNameAr else item.displayNameEn,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isQuickEquipped) primary.copy(alpha = 0.20f) else Color(0xFF101624),
                                        border = BorderStroke(
                                            0.8.dp,
                                            if (isQuickEquipped) primary else Color.White.copy(alpha = 0.14f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (isQuickEquipped) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Text(
                                                text = if (isQuickEquipped) {
                                                    if (isArabic) "مُجهّز ✓" else "Equipped"
                                                } else {
                                                    if (isArabic) "تجهيز" else "Equip"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isQuickEquipped) primary else Color.White.copy(alpha = 0.85f),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5 Liquid Glass Dark Mode Interactive Mimic Centerpieces (All Priced & Purchase-Gated)
        items(HomeMimicItem.nextGenMimics, key = { "store_mimic_${it.id}" }) { mimic ->
            val isEquipped = isHomeMimicVisible && equippedHomeMimic == mimic
            val isPurchased = mimic.isUnlockedByDefault || purchasedMimicIds.contains(mimic.id)
            val isUnlocked = isPurchased || isPremiumActivated
            val primaryColor = Color(mimic.primaryAccentHex)
            val secondaryColor = Color(mimic.secondaryAccentHex)
            val costTier = mimic.costTier ?: ForgeKeyTier.SILVER
            val tierColor = keyTierColor(costTier, aura.isDark)
            val availableKeys = forgeState.getKeyCount(costTier)
            val canAffordShields = shieldsCount >= mimic.shieldsCost
            val canAffordKeys = availableKeys >= mimic.costAmount
            val canAfford = isUnlocked || (canAffordShields && canAffordKeys)
            val tierWord = when (costTier) {
                ForgeKeyTier.BRONZE -> if (isArabic) "برونزي" else "Bronze"
                ForgeKeyTier.SILVER -> if (isArabic) "فضي" else "Silver"
                ForgeKeyTier.GOLD -> if (isArabic) "ذهبي" else "Gold"
                ForgeKeyTier.DIAMOND -> if (isArabic) "ماسي" else "Diamond"
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = aura.surfaceCard,
                border = BorderStroke(
                    width = if (isEquipped) 1.7.dp else 1.15.dp,
                    brush = Brush.linearGradient(
                        if (isEquipped) listOf(primaryColor, secondaryColor)
                        else listOf(primaryColor.copy(alpha = 0.45f), secondaryColor.copy(alpha = 0.35f))
                    )
                ),
                shadowElevation = if (isEquipped) 12.dp else 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_mimic_card_${mimic.id}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isArabic) mimic.displayNameAr else mimic.displayNameEn,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    ),
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                RarityBadgePill(
                                    rarity = mimic.rarity,
                                    isArabic = isArabic
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isArabic) mimic.subtitleAr else mimic.subtitleEn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        SeekbarStatusPill(
                            isActive = isEquipped,
                            isPurchased = isPurchased,
                            isPremiumActivated = isPremiumActivated,
                            costLabel = "${mimic.shieldsCost}🛡️ + ${mimic.costAmount} $tierWord",
                            tierColor = tierColor,
                            isArabic = isArabic
                        )
                    }

                    // Live 60 FPS Liquid Glass Dark Mode Preview Stage (#000000 OLED)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(166.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF000000))
                            .border(
                                1.1.dp,
                                Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.55f), secondaryColor.copy(alpha = 0.50f))),
                                RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        HomeMimicLiveCanvasPreview(
                            mimicItem = mimic,
                            streakDays = MIMIC_MAX_THAW_STREAK_DAYS,
                            dailyProgress = dailyWatchProgress,
                            isPlayerActive = isPlayerActive,
                            isCompact = false,
                            modifier = Modifier.size(152.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xB8080D1B),
                            border = BorderStroke(
                                0.8.dp,
                                primaryColor.copy(alpha = 0.55f)
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (isArabic) "✨ عرض بدون جليد • 60 FPS" else "✨ Pure Preview • No Ice",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp
                                )
                            )
                        }
                    }

                    // Smooth 0 -> 60 Consecutive Freeze/Thaw Progress Bar
                    LinearProgressIndicator(
                        progress = { streakProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = primaryColor,
                        trackColor = aura.progressTrack
                    )

                    SeekbarPriceOrFreeRow(
                        isPurchased = isPurchased,
                        isPremiumActivated = isPremiumActivated,
                        shieldsCost = mimic.shieldsCost,
                        availableShields = shieldsCount,
                        canAffordShields = canAffordShields,
                        keyCostText = if (isArabic) "🗝️ ${mimic.costAmount} مفتاح $tierWord" else "🗝️ ${mimic.costAmount} $tierWord Keys",
                        availableKeys = availableKeys,
                        canAffordKeys = canAffordKeys,
                        tierColor = tierColor,
                        isArabic = isArabic
                    )

                    if (!isUnlocked) {
                        Button(
                            onClick = { onPurchaseHomeMimic(mimic) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAfford) aura.accentGold else aura.surfaceSubtle,
                                contentColor = if (canAfford) Color(0xFF07090D) else aura.textSecondary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("buy_mimic_${mimic.id}_button")
                        ) {
                            Icon(
                                imageVector = if (canAfford) Icons.Default.VpnKey else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) {
                                    "شراء وتجهيز (${mimic.shieldsCost} 🛡️ + ${mimic.costAmount} $tierWord)"
                                } else {
                                    "Buy & Equip (${mimic.shieldsCost} 🛡️ + ${mimic.costAmount} $tierWord)"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 13.5.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (isEquipped) {
                                    onToggleHomeMimicVisible(false)
                                } else {
                                    onEquipHomeMimic(mimic)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isEquipped -> aura.surfaceSubtle
                                    isPremiumActivated && !isPurchased -> aura.accentGold
                                    else -> primaryColor
                                },
                                contentColor = if (isEquipped) primaryColor else Color(0xFF07090D)
                            ),
                            border = if (isEquipped) BorderStroke(1.dp, primaryColor.copy(alpha = 0.6f)) else null,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("toggle_mimic_${mimic.id}_button")
                        ) {
                            Icon(
                                imageVector = if (isEquipped) Icons.Default.VisibilityOff else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isEquipped -> if (isArabic) "تعطيل وإخفاء من صفحة Home" else "Disable (Hide from Home)"
                                    isPremiumActivated && !isPurchased -> if (isArabic) "تجهيز مجاناً الآن 👑" else "Equip Free (Premium 👑)"
                                    else -> if (isArabic) "تجهيز في صفحة Home" else "Equip on Home"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Core Item: Home Streak Flame (Equip / Disable — hides from Home when disabled)
        item(key = "mimic_home_flame_card") {
            val flameColor = when {
                clampedDays >= 60 -> Color(0xFFFF1744)
                clampedDays >= 10 -> Color(0xFFFF6D00)
                else -> Color(0xFFFF8F00)
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = aura.surfaceCard,
                border = BorderStroke(
                    width = if (isFlameEnabled) 1.6.dp else 1.1.dp,
                    brush = Brush.linearGradient(
                        if (isFlameEnabled) listOf(aura.accentFlame, aura.accentGold)
                        else listOf(aura.borderSubtle, aura.textTertiary.copy(alpha = 0.4f))
                    )
                ),
                shadowElevation = if (isFlameEnabled) 10.dp else 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_mimic_flame_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "شعلة صفحة الـ Home" else "Home Streak Flame",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.5.sp
                                    ),
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                RarityBadgePill(
                                    rarity = HomeMimicItem.STREAK_FLAME.rarity,
                                    isArabic = isArabic
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isArabic) {
                                    "العنصر الأساسي في الصفحة الرئيسية • يذوب جليده مع السترايك (0 - 60 يوماً متتالياً)"
                                } else {
                                    "Core Home centerpiece • Thaws with Streak (0 - 60 consecutive days)"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isFlameEnabled) aura.accentEmeraldSoft else aura.surfaceSubtle,
                            border = BorderStroke(
                                1.dp,
                                if (isFlameEnabled) aura.accentEmerald else aura.borderSubtle
                            )
                        ) {
                            Text(
                                text = if (isFlameEnabled) {
                                    if (isArabic) "مُجهّز ✓" else "Equipped ✓"
                                } else {
                                    if (isArabic) "معطّل (مخفي)" else "Disabled"
                                },
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isFlameEnabled) aura.accentEmerald else aura.textSecondary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }

                    // Live Flame Preview Stage synchronized with current streakDays
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(126.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0A101E))
                            .border(1.dp, flameColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val glowR = 46.dp.toPx() * (0.9f + 0.2f * pulsePhase)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(
                                        flameColor.copy(alpha = if (isFlameEnabled) 0.55f else 0.22f),
                                        aura.accentGold.copy(alpha = if (isFlameEnabled) 0.24f else 0.08f),
                                        Color.Transparent
                                    ),
                                    center = Offset(cx, cy),
                                    radius = glowR * 1.4f
                                ),
                                center = Offset(cx, cy),
                                radius = glowR * 1.4f
                            )
                            // Rising Ember Sparks in preview (ice-free Store showcase)
                            for (i in 0 until 10) {
                                val t = (animPhase + i * 0.1f) % 1f
                                val xOff = sin(i * 1.9f + animPhase * 6.28f) * 34.dp.toPx()
                                val yOff = (1f - t) * 44.dp.toPx() - 22.dp.toPx()
                                drawCircle(
                                    color = flameColor.copy(alpha = (1f - t) * 0.85f),
                                    radius = (2.2f + (i % 3) * 0.9f).dp.toPx() * (1f - t * 0.4f),
                                    center = Offset(cx + xOff, cy + yOff)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            flameColor.copy(alpha = if (isFlameEnabled) 0.30f else 0.14f),
                                            Color(0xCC090E1A)
                                        )
                                    )
                                )
                                .border(1.2.dp, flameColor.copy(alpha = 0.7f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = if (isArabic) "معاينة الشعلة" else "Flame Preview",
                                tint = flameColor,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xAA0B1222),
                            border = BorderStroke(0.8.dp, flameColor.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (isArabic) "🔥 عرض بدون جليد" else "🔥 Pure Preview • No Ice",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = flameColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = if (isArabic) {
                            "يمكنك تجهيز الشعلة لعرضها في صفحة Home أو تعطيلها لإخفائها بالكامل دون فقدان تقدم السترايك."
                        } else {
                            "Equip the Streak Flame on your Home screen or disable it to hide it cleanly without affecting your streak."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            lineHeight = 18.sp,
                            fontSize = 11.5.sp
                        )
                    )

                    Button(
                        onClick = { onToggleFlame(!isFlameEnabled) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFlameEnabled) aura.surfaceSubtle else aura.accentEmerald,
                            contentColor = if (isFlameEnabled) aura.accentFlame else Color(0xFF07090D)
                        ),
                        border = if (isFlameEnabled) BorderStroke(1.dp, aura.accentFlame.copy(alpha = 0.55f)) else null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("toggle_mimic_flame_button")
                    ) {
                        Icon(
                            imageVector = if (isFlameEnabled) Icons.Default.VisibilityOff else Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFlameEnabled) {
                                if (isArabic) "تعطيل الشعلة (إخفاء من Home)" else "Disable Flame (Hide from Home)"
                            } else {
                                if (isArabic) "تجهيز الشعلة في صفحة Home" else "Equip Flame on Home"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }

        // 2. Item 2: Rotating DNA & Chromosomes (Bound to 0..60 Consecutive Streak Ice-Melt & Speed Curve)
        item(key = "mimic_dna_chromosome_card") {
            val dnaCyan = Color(0xFF00F2FE)
            val dnaViolet = Color(0xFFA855F7)

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = aura.surfaceCard,
                border = BorderStroke(
                    width = if (isDnaEnabled) 1.7.dp else 1.2.dp,
                    brush = Brush.linearGradient(
                        if (isDnaEnabled) listOf(aura.accentEmerald, dnaCyan, dnaViolet)
                        else listOf(dnaCyan.copy(alpha = 0.7f), dnaViolet.copy(alpha = 0.65f), aura.accentGold.copy(alpha = 0.55f))
                    )
                ),
                shadowElevation = if (isDnaEnabled) 12.dp else 5.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_mimic_dna_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "حمض الـ DNA والكروموزومات" else "DNA & Chromosomes Helix",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    ),
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                RarityBadgePill(
                                    rarity = HomeMimicItem.DNA_CHROMOSOME.rarity,
                                    isArabic = isArabic
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isArabic) {
                                    "يدور ثلاثي الأبعاد حول نفسه ويرتبط بالسترايك (0 → 60 يوماً متتالياً)"
                                } else {
                                    "3D self-rotating chromosome helix synced to Streak (0 → 60 consecutive days)"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        SeekbarStatusPill(
                            isActive = isDnaEnabled,
                            isPurchased = isDnaPurchased,
                            isPremiumActivated = isPremiumActivated,
                            costLabel = if (isArabic) "$dnaShieldsCost🛡️ + $dnaCostAmount ذهبي" else "$dnaShieldsCost🛡️ + $dnaCostAmount Gold",
                            tierColor = dnaTierColor,
                            isArabic = isArabic
                        )
                    }

                    // Live 60FPS 3D Rotating DNA & Chromosomes Preview Stage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF0E172E), Color(0xFF060913))
                                )
                            )
                            .border(
                                1.1.dp,
                                Brush.horizontalGradient(listOf(dnaCyan.copy(alpha = 0.5f), dnaViolet.copy(alpha = 0.5f))),
                                RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        DnaChromosomeCanvas(
                            streakDays = MIMIC_MAX_THAW_STREAK_DAYS,
                            isCompact = true,
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xB8080D1B),
                            border = BorderStroke(
                                0.8.dp,
                                dnaCyan.copy(alpha = 0.55f)
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (isArabic) "🧬 عرض بدون جليد • 60 FPS" else "🧬 Pure Preview • No Ice",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = dnaCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp
                                )
                            )
                        }
                    }

                    // Streak Velocity & Ice-Melt Progression Indicator Bar
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "تقدّم الذوبان وسرعة الدوران" else "Ice Melt & Spin Velocity",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 10.5.sp
                                )
                            )
                            Text(
                                text = if (isFrozen) {
                                    if (isArabic) "0% (متجمد)" else "0% (Frozen)"
                                } else {
                                    "${(streakProgress * 100).toInt().coerceAtLeast(1)}% • $clampedDays / $MIMIC_MAX_THAW_STREAK_DAYS"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isFrozen) Color(0xFF80DEEA) else dnaCyan,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                        LinearProgressIndicator(
                            progress = { streakProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isFrozen) Color(0xFF80DEEA) else dnaCyan,
                            trackColor = aura.progressTrack
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = aura.surfaceSubtle,
                        border = BorderStroke(0.8.dp, aura.borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isArabic) {
                                "مجسم ثلاثي الأبعاد لكروموزومات وحمض الـ DNA يدور على نفسه بسلاسة فائقة؛ في اليوم 0 يكون متجمداً داخل بلورات الجليد ومتوقفاً، ومع مواصلة السترايك يذوب الجليد وتتصاعد سرعته وتوهجه تدريجياً حتى الذوبان الكامل في 60 سترايك متتالياً، وفي حالة فقدان المستوى يعود الجليد."
                            } else {
                                "3D self-rotating DNA & Chromosome centerpiece bound to your consecutive streak: frozen solid & stationary at Day 0, progressively melting ice and accelerating in smooth bioluminescent glow up to 100% thaw at 60 consecutive days; losing your streak restores the ice."
                            },
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                lineHeight = 18.sp,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    SeekbarPriceOrFreeRow(
                        isPurchased = isDnaPurchased,
                        isPremiumActivated = isPremiumActivated,
                        shieldsCost = dnaShieldsCost,
                        availableShields = shieldsCount,
                        canAffordShields = canAffordDnaShields,
                        keyCostText = if (isArabic) "🗝️ $dnaCostAmount مفتاح ذهبي" else "🗝️ $dnaCostAmount Gold Keys",
                        availableKeys = dnaAvailableKeys,
                        canAffordKeys = canAffordDnaKeys,
                        tierColor = dnaTierColor,
                        isArabic = isArabic
                    )

                    if (!isDnaUnlocked) {
                        Button(
                            onClick = onPurchaseDna,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAffordDna) aura.accentGold else aura.surfaceSubtle,
                                contentColor = if (canAffordDna) Color(0xFF07090D) else aura.textSecondary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("buy_mimic_dna_button")
                        ) {
                            Icon(
                                imageVector = if (canAffordDna) Icons.Default.VpnKey else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "شراء وتجهيز ($dnaShieldsCost 🛡️ + $dnaCostAmount ذهبي)" else "Buy & Equip ($dnaShieldsCost 🛡️ + $dnaCostAmount Gold)",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.5.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = { onToggleDna(!isDnaEnabled) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isDnaEnabled -> aura.surfaceSubtle
                                    isPremiumActivated && !isDnaPurchased -> aura.accentGold
                                    else -> dnaCyan
                                },
                                contentColor = if (isDnaEnabled) aura.accentCyan else Color(0xFF07090D)
                            ),
                            border = if (isDnaEnabled) BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.55f)) else null,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("toggle_mimic_dna_button")
                        ) {
                            Icon(
                                imageVector = if (isDnaEnabled) Icons.Default.VisibilityOff else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isDnaEnabled -> if (isArabic) "تعطيل الـ DNA (إخفاء من Home)" else "Disable DNA (Hide from Home)"
                                    isPremiumActivated && !isDnaPurchased -> if (isArabic) "تجهيز مجاناً الآن 👑" else "Equip Free (Premium 👑)"
                                    else -> if (isArabic) "تجهيز الـ DNA في صفحة Home" else "Equip DNA on Home"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AmbientVideoFillStoreSection(
    totalKeys: Int,
    shieldsCount: Int,
    forgeState: ForgeUiState,
    purchasedModeIds: Set<String>,
    activeMode: AmbientBlackBlurMode,
    isGlowEnabled: Boolean,
    isPremiumActivated: Boolean,
    animPhase: Float,
    pulsePhase: Float,
    isArabic: Boolean,
    onToggleGlow: (Boolean) -> Unit,
    onPurchaseMode: (AmbientBlackBlurMode) -> Unit,
    onSelectMode: (AmbientBlackBlurMode) -> Unit
) {
    val aura = MaterialTheme.aura
    val unlockedCount = AmbientBlackBlurMode.entries.count {
        it.isUnlockedByDefault || purchasedModeIds.contains(it.name) || isPremiumActivated
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ambient_video_fill_store_list"),
        contentPadding = PaddingValues(bottom = 24.dp + LocalFloatingBottomBarPadding.current),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "ambient_store_balance") {
            ShopBalanceSummaryCard(
                totalKeys = totalKeys,
                shieldsCount = shieldsCount,
                forgeState = forgeState,
                isArabic = isArabic
            )
        }

        // Master Status Banner
        item(key = "ambient_store_master_card") {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = aura.surfaceCard,
                border = BorderStroke(
                    1.2.dp,
                    if (isGlowEnabled) aura.accentGold.copy(alpha = 0.65f) else aura.borderSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(aura.accentGoldBg)
                                .border(1.dp, aura.accentGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BlurOn,
                                contentDescription = null,
                                tint = aura.accentGold,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ambient Video Fill ($unlockedCount/10)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = if (isGlowEnabled) {
                                    if (isArabic) "المفعّل: ${activeMode.titleAr}" else "Active: ${activeMode.titleEn}"
                                } else {
                                    if (isArabic) "معطل • خلفية OLED سوداء نقية" else "Disabled • Pure #000000 OLED"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isGlowEnabled) aura.accentGold else aura.textSecondary,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Switch(
                        checked = isGlowEnabled,
                        onCheckedChange = onToggleGlow,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = aura.accentGold,
                            checkedThumbColor = Color(0xFF07090D),
                            uncheckedTrackColor = aura.progressTrack,
                            uncheckedThumbColor = aura.textSecondary
                        ),
                        modifier = Modifier.testTag("store_ambient_master_switch")
                    )
                }
            }
        }

        items(
            items = AmbientBlackBlurMode.entries,
            key = { it.name }
        ) { mode ->
            val isOwned = mode.isUnlockedByDefault || purchasedModeIds.contains(mode.name)
            val isUnlocked = isOwned || isPremiumActivated
            val isActiveMode = isGlowEnabled && activeMode == mode
            val tier = mode.costTier
            val tierColor = keyTierColor(tier, aura.isDark)
            val availableKeys = tier?.let { forgeState.getKeyCount(it) } ?: Int.MAX_VALUE
            val canAffordShields = shieldsCount >= mode.shieldsCost
            val canAffordKeys = availableKeys >= mode.costAmount
            val canAfford = isUnlocked || (canAffordShields && canAffordKeys)

            val tierNameAr = when (tier) {
                ForgeKeyTier.BRONZE -> "برونزي"
                ForgeKeyTier.SILVER -> "فضي"
                ForgeKeyTier.GOLD -> "ذهبي"
                ForgeKeyTier.DIAMOND -> "ماسي"
                null -> ""
            }
            val tierNameEn = when (tier) {
                ForgeKeyTier.BRONZE -> "Bronze"
                ForgeKeyTier.SILVER -> "Silver"
                ForgeKeyTier.GOLD -> "Gold"
                ForgeKeyTier.DIAMOND -> "Diamond"
                null -> ""
            }
            val keyCostText = if (tier == null || mode.costAmount <= 0) {
                if (isArabic) "🗝️ مجاني افتراضياً" else "🗝️ Free Default"
            } else {
                if (isArabic) "🗝️ ${mode.costAmount} مفتاح $tierNameAr" else "🗝️ ${mode.costAmount} $tierNameEn Keys"
            }
            val pillCostLabel = if (mode.isUnlockedByDefault) {
                if (isArabic) "مجاني" else "FREE"
            } else {
                if (isArabic) "${mode.shieldsCost}🛡️ + ${mode.costAmount} $tierNameAr"
                else "${mode.shieldsCost}🛡️ + ${mode.costAmount} $tierNameEn"
            }
            val buyLabel = if (isArabic) {
                "شراء وتفعيل (${mode.shieldsCost} 🛡️ + ${mode.costAmount} $tierNameAr)"
            } else {
                "Buy & Activate (${mode.shieldsCost} 🛡️ + ${mode.costAmount} $tierNameEn)"
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = aura.surfaceCard,
                border = BorderStroke(
                    width = if (isActiveMode) 1.6.dp else 1.1.dp,
                    brush = Brush.linearGradient(
                        if (isActiveMode) listOf(aura.accentEmerald, aura.accentGold)
                        else listOf(tierColor.copy(alpha = 0.55f), aura.borderSubtle)
                    )
                ),
                shadowElevation = if (isActiveMode) 10.dp else 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_ambient_mode_card_${mode.name}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = tierColor.copy(alpha = 0.18f),
                                    border = BorderStroke(0.8.dp, tierColor.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = mode.shortBadge,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = tierColor,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                                Text(
                                    text = if (isArabic) mode.titleAr else mode.titleEn,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.5.sp
                                    ),
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                RarityBadgePill(
                                    rarity = mode.rarity,
                                    isArabic = isArabic
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isArabic) mode.subtitleAr else mode.subtitleEn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        SeekbarStatusPill(
                            isActive = isActiveMode,
                            isPurchased = isOwned,
                            isPremiumActivated = isPremiumActivated,
                            costLabel = pillCostLabel,
                            tierColor = tierColor,
                            isArabic = isArabic
                        )
                    }

                    // Live Cinema Black Blur & Fade Stage Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF000000))
                            .border(
                                1.dp,
                                if (isActiveMode) aura.accentGold.copy(alpha = 0.55f) else Color(0x24FFFFFF),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AmbientModeStorePreviewCanvas(
                            mode = mode,
                            animPhase = animPhase,
                            pulsePhase = pulsePhase,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    SeekbarPriceOrFreeRow(
                        isPurchased = isOwned,
                        isPremiumActivated = isPremiumActivated,
                        shieldsCost = mode.shieldsCost,
                        availableShields = shieldsCount,
                        canAffordShields = canAffordShields,
                        keyCostText = keyCostText,
                        availableKeys = availableKeys,
                        canAffordKeys = canAffordKeys,
                        tierColor = tierColor,
                        isArabic = isArabic
                    )

                    SeekbarActionButton(
                        isUnlocked = isUnlocked,
                        isPurchased = isOwned,
                        isActive = isActiveMode,
                        isPremiumActivated = isPremiumActivated,
                        canAfford = canAfford,
                        buyLabel = buyLabel,
                        activateLabel = if (isArabic) "تفعيل في مشغّل الفيديو" else "Activate in Player",
                        buyTestTag = "buy_ambient_mode_${mode.name}",
                        toggleTestTag = "toggle_ambient_mode_${mode.name}",
                        onPurchase = { onPurchaseMode(mode) },
                        onToggle = {
                            if (isActiveMode) {
                                onToggleGlow(false)
                            } else {
                                onSelectMode(mode)
                            }
                        },
                        isArabic = isArabic
                    )
                }
            }
        }
    }
}

@Composable
private fun AmbientModeStorePreviewCanvas(
    mode: AmbientBlackBlurMode,
    animPhase: Float,
    pulsePhase: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val rad = animPhase * 2f * PI.toFloat()
        val pulseAlpha = 0.65f + 0.35f * pulsePhase

        val tintPrimary = when (mode) {
            AmbientBlackBlurMode.DEEP_OBSIDIAN_BLUR -> Color(0xFF252B38)       // 01 Silent Obsidian Eclipse
            AmbientBlackBlurMode.CINEMA_MATTE_VIGNETTE -> Color(0xFF2B313D)    // 02 Smoked Crystal Diffusion
            AmbientBlackBlurMode.SMOKED_LIQUID_GLASS -> Color(0xFF282233)      // 03 Matte Theater Curtain Fade
            AmbientBlackBlurMode.OLED_MIDNIGHT_DIFFUSION -> Color(0xFF323438)  // 04 Still Arctic Mist (Mono)
            AmbientBlackBlurMode.VELVET_SHADOW_MIST -> Color(0xFF2B2438)       // 05 Deep Velvet Melt
            AmbientBlackBlurMode.EDGE_FADE_GAUSSIAN_BLUR -> Color(0xFF273245)  // 06 Midnight Horizon Gradient
            AmbientBlackBlurMode.RADIAL_VIGNETTE_DISSOLVE -> Color(0xFF2A3444) // 07 Dark Ink Ripple Blur
            AmbientBlackBlurMode.CINEMATIC_SMOKED_BLUR -> Color(0xFF2C323E)    // 08 Frosted Noir Marble
            AmbientBlackBlurMode.VELVET_SHADOW_MELT -> Color(0xFF353E52)       // 09 Anamorphic Bokeh Fade
            AmbientBlackBlurMode.MULTI_LAYER_LAYERED_FADE -> Color(0xFF3A2D26) // 10 Dimmed Charcoal Halo
        }
        val tintSecondary = when (mode) {
            AmbientBlackBlurMode.OLED_MIDNIGHT_DIFFUSION -> Color(0xFF1A1B1D)
            AmbientBlackBlurMode.MULTI_LAYER_LAYERED_FADE -> Color(0xFF221A16)
            AmbientBlackBlurMode.SMOKED_LIQUID_GLASS,
            AmbientBlackBlurMode.VELVET_SHADOW_MIST -> Color(0xFF16131D)
            else -> Color(0xFF13161E)
        }

        when (mode) {
            AmbientBlackBlurMode.SMOKED_LIQUID_GLASS -> {
                // 03 Matte Theater Curtain Fade (left/right horizontal curtains)
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(tintPrimary.copy(alpha = 0.62f * pulseAlpha), Color.Black, Color.Black, tintPrimary.copy(alpha = 0.62f * pulseAlpha))
                    )
                )
            }
            AmbientBlackBlurMode.EDGE_FADE_GAUSSIAN_BLUR -> {
                // 06 Midnight Horizon Gradient (vertical top/bottom blur)
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(tintPrimary.copy(alpha = 0.65f * pulseAlpha), Color.Black, tintPrimary.copy(alpha = 0.65f * pulseAlpha))
                    )
                )
            }
            AmbientBlackBlurMode.RADIAL_VIGNETTE_DISSOLVE,
            AmbientBlackBlurMode.CINEMA_MATTE_VIGNETTE -> {
                // Smooth fluid/frosted drift
                val dx = cos(rad) * w * 0.12f
                val dy = sin(rad) * h * 0.10f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(tintPrimary.copy(alpha = 0.62f * pulseAlpha), tintSecondary.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(cx + dx, cy + dy),
                        radius = w * 0.44f
                    ),
                    center = Offset(cx + dx, cy + dy),
                    radius = w * 0.44f
                )
            }
            AmbientBlackBlurMode.VELVET_SHADOW_MELT -> {
                // 09 Anamorphic Bokeh Fade (faint blurred orbs)
                val sway = sin(rad) * w * 0.05f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(tintPrimary.copy(alpha = 0.50f * pulseAlpha), Color.Transparent),
                        center = Offset(w * 0.22f + sway, h * 0.34f),
                        radius = w * 0.22f
                    ),
                    center = Offset(w * 0.22f + sway, h * 0.34f),
                    radius = w * 0.22f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(tintPrimary.copy(alpha = 0.46f * pulseAlpha), Color.Transparent),
                        center = Offset(w * 0.78f - sway, h * 0.66f),
                        radius = w * 0.24f
                    ),
                    center = Offset(w * 0.78f - sway, h * 0.66f),
                    radius = w * 0.24f
                )
            }
            else -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(tintPrimary.copy(alpha = 0.62f * pulseAlpha), tintSecondary.copy(alpha = 0.28f), Color.Black),
                        center = Offset(cx, cy),
                        radius = w * 0.46f
                    ),
                    center = Offset(cx, cy),
                    radius = w * 0.46f
                )
            }
        }

        // Cinema 16:9 Mini Frame in Center
        val frameW = w * 0.40f
        val frameH = h * 0.62f
        drawRoundRect(
            color = Color(0xFF040406),
            topLeft = Offset(cx - frameW / 2f, cy - frameH / 2f),
            size = Size(frameW, frameH),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.18f),
            topLeft = Offset(cx - frameW / 2f, cy - frameH / 2f),
            size = Size(frameW, frameH),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}



