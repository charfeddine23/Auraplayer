package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ForgeKeyTier
import com.example.data.model.ForgeSlotState
import com.example.data.model.ForgeUiState
import com.example.ui.components.ForgeHammerExplanationDialog
import com.example.ui.components.ForgeRecipeCard
import com.example.ui.components.formatForgeCountdown
import com.example.ui.theme.AuraAtmosphericLayer
import com.example.ui.theme.aura
import com.example.ui.theme.stableStatusBarsPadding
import com.example.ui.viewmodel.StreakViewModel

@Composable
fun KeyWorkshopsScreen(
    streakViewModel: StreakViewModel,
    onBack: () -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val streakUiState by streakViewModel.uiState.collectAsState()
    val forgeState by streakViewModel.forgeState.collectAsState()
    val shieldsCount = streakUiState.shieldsCount
    var hammerInfoTier by remember { mutableStateOf<ForgeKeyTier?>(null) }

    val activeWorkshopsCount = forgeState.slots.values.count { it.isActive && !it.isReadyToClaim }
    val readyWorkshopsCount = forgeState.slots.values.count { it.isReadyToClaim }
    val totalWorkshops = ForgeKeyTier.entries.size

    val infiniteTransition = rememberInfiniteTransition(label = "key_workshops_ambient")
    val emberPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "key_workshops_ember_phase"
    )
    val furnacePulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "key_workshops_furnace_pulse"
    )
    val gearPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "key_workshops_gear_rotation"
    )

    BackHandler {
        if (hammerInfoTier != null) {
            hammerInfoTier = null
        } else {
            onBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(aura.background)
            .testTag("key_workshops_screen")
    ) {
        AuraAtmosphericLayer()

        // Atmospheric Molten Gold & Furnace Backdrop Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val activeIntensity = when {
                activeWorkshopsCount >= 2 -> 1.0f
                activeWorkshopsCount == 1 -> 0.72f
                readyWorkshopsCount > 0 -> 0.62f
                else -> 0.28f
            }
            val topGlowColor = when {
                readyWorkshopsCount > 0 -> Color(0xFF10B981)
                activeWorkshopsCount > 0 -> Color(0xFFFF7A1A)
                else -> Color(0xFFFFD700)
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        topGlowColor.copy(alpha = 0.22f * activeIntensity * furnacePulse),
                        Color(0xFFFFD700).copy(alpha = 0.11f * activeIntensity),
                        Color.Transparent
                    ),
                    radius = size.width * 0.92f
                ),
                center = Offset(size.width * 0.5f, size.height * 0.14f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .stableStatusBarsPadding()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Top Foundry Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBack()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(aura.iconButtonBg)
                            .border(1.dp, aura.borderSubtle, CircleShape)
                            .testTag("key_workshops_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isArabic) "رجوع" else "Back",
                            tint = aura.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Key Workshops",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = aura.textPrimary,
                                    fontSize = 18.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Hardware,
                                contentDescription = null,
                                tint = aura.accentGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = if (isArabic) {
                                "4 ورشات لصهر المفاتيح • تتقدم بالاستخدام النشط"
                            } else {
                                "4 Key Smelting Workshops • Advances on active usage"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Active Workshops Telemetry Pill
                val headerPillColor = when {
                    readyWorkshopsCount > 0 -> aura.accentEmerald
                    activeWorkshopsCount > 0 -> aura.accentFlame
                    else -> aura.accentGold
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = headerPillColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.1.dp, headerPillColor.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = headerPillColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = when {
                                readyWorkshopsCount > 0 -> "$readyWorkshopsCount ✨"
                                else -> "$activeWorkshopsCount/$totalWorkshops"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.5.sp,
                                fontFeatureSettings = "tnum"
                            )
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "key_workshops_overview") {
                    KeyWorkshopsTelemetryHeader(
                        shieldsCount = shieldsCount,
                        forgeState = forgeState,
                        activeWorkshopsCount = activeWorkshopsCount,
                        readyWorkshopsCount = readyWorkshopsCount,
                        furnacePulse = furnacePulse,
                        isArabic = isArabic
                    )
                }

                items(ForgeKeyTier.entries, key = { it.id }) { tier ->
                    val slot = forgeState.getSlot(tier)
                    ForgeRecipeCard(
                        tier = tier,
                        slot = slot,
                        shieldsCount = shieldsCount,
                        forgeState = forgeState,
                        emberPhase = emberPhase,
                        furnacePulse = furnacePulse,
                        gearPhase = gearPhase,
                        isArabic = isArabic,
                        onStart = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            streakViewModel.startForge(tier, isArabic = isArabic)
                        },
                        onClaim = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            streakViewModel.claimForgeKey(tier, isArabic = isArabic)
                        },
                        onHammerClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            hammerInfoTier = tier
                        }
                    )
                }
            }
        }
    }

    val infoTier = hammerInfoTier
    if (infoTier != null) {
        ForgeHammerExplanationDialog(
            infoTier = infoTier,
            isArabic = isArabic,
            onDismiss = { hammerInfoTier = null }
        )
    }
}

@Composable
private fun KeyWorkshopsTelemetryHeader(
    shieldsCount: Int,
    forgeState: ForgeUiState,
    activeWorkshopsCount: Int,
    readyWorkshopsCount: Int,
    furnacePulse: Float,
    isArabic: Boolean
) {
    val aura = MaterialTheme.aura
    val totalWorkshops = ForgeKeyTier.entries.size

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (aura.isDark) {
                        listOf(Color(0xFF211A10), Color(0xFF121623), Color(0xFF0A0D16))
                    } else {
                        listOf(Color(0xFFFEF9E7), Color(0xFFFFFCF5), Color(0xFFF6F9FE))
                    }
                )
            )
            .border(
                width = 1.3.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        aura.accentGold.copy(alpha = if (activeWorkshopsCount > 0) furnacePulse else 0.65f),
                        aura.accentFlame.copy(alpha = 0.78f),
                        aura.accentGold.copy(alpha = if (activeWorkshopsCount > 0) furnacePulse else 0.65f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "مجمع ورشات المفاتيح (4 ورشات)" else "Key Crucible Foundry (4 Workshops)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isArabic) {
                            "اصهر مفاتيح البرونز والفضة والذهب والألماس بالتوازي أثناء المشاهدة والاستخدام"
                        } else {
                            "Smelt Bronze, Silver, Gold & Diamond keys in parallel while using the app"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                val statusLabel = when {
                    readyWorkshopsCount > 0 -> if (isArabic) "$readyWorkshopsCount جاهز ✨" else "$readyWorkshopsCount Ready ✨"
                    activeWorkshopsCount > 0 -> if (isArabic) "$activeWorkshopsCount / $totalWorkshops نشط 🔥" else "$activeWorkshopsCount / $totalWorkshops Active 🔥"
                    else -> if (isArabic) "0 / $totalWorkshops نشط" else "0 / $totalWorkshops Active"
                }
                val statusColor = when {
                    readyWorkshopsCount > 0 -> aura.accentEmerald
                    activeWorkshopsCount > 0 -> aura.accentFlame
                    else -> aura.accentGold
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }

            // 2x2 Live Workshop Mini Telemetry Pills
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ForgeKeyTier.entries.chunked(2).forEach { rowTiers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTiers.forEach { tier ->
                            WorkshopMiniTelemetryBadge(
                                tier = tier,
                                slot = forgeState.getSlot(tier),
                                furnacePulse = furnacePulse,
                                isArabic = isArabic,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Craft Materials Strip (Shields + Bronze + Silver + Gold + Diamond)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                WorkshopMaterialChip(
                    label = if (isArabic) "دروع" else "Shields",
                    count = shieldsCount,
                    accent = aura.accentCyan,
                    icon = Icons.Default.Shield,
                    modifier = Modifier.weight(1f)
                )
                WorkshopMaterialChip(
                    label = if (isArabic) "برونز" else "Bronze",
                    count = forgeState.bronzeKeys,
                    accent = if (aura.isDark) Color(0xFFE59A54) else Color(0xFFB45309),
                    icon = Icons.Default.VpnKey,
                    modifier = Modifier.weight(1f)
                )
                WorkshopMaterialChip(
                    label = if (isArabic) "فضة" else "Silver",
                    count = forgeState.silverKeys,
                    accent = if (aura.isDark) Color(0xFFE2E8F0) else Color(0xFF475569),
                    icon = Icons.Default.VpnKey,
                    modifier = Modifier.weight(1f)
                )
                WorkshopMaterialChip(
                    label = if (isArabic) "ذهب" else "Gold",
                    count = forgeState.goldenKeys,
                    accent = if (aura.isDark) Color(0xFFFFD700) else Color(0xFFA16207),
                    icon = Icons.Default.VpnKey,
                    modifier = Modifier.weight(1f)
                )
                WorkshopMaterialChip(
                    label = if (isArabic) "ألماس" else "Diamond",
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
private fun WorkshopMiniTelemetryBadge(
    tier: ForgeKeyTier,
    slot: ForgeSlotState,
    furnacePulse: Float,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val isReady = slot.isReadyToClaim
    val isWorking = slot.isActive && !isReady
    val tierColor = when (tier) {
        ForgeKeyTier.BRONZE -> if (aura.isDark) Color(0xFFE59A54) else Color(0xFFB45309)
        ForgeKeyTier.SILVER -> if (aura.isDark) Color(0xFFE2E8F0) else Color(0xFF475569)
        ForgeKeyTier.GOLD -> if (aura.isDark) Color(0xFFFFD700) else Color(0xFFA16207)
        ForgeKeyTier.DIAMOND -> if (aura.isDark) Color(0xFF00F2FE) else Color(0xFF0284C7)
    }
    val accent = when {
        isReady -> aura.accentEmerald
        isWorking -> tierColor
        else -> aura.textTertiary
    }
    val label = when (tier) {
        ForgeKeyTier.BRONZE -> if (isArabic) "#01 برونز" else "#01 Bronze"
        ForgeKeyTier.SILVER -> if (isArabic) "#02 فضة" else "#02 Silver"
        ForgeKeyTier.GOLD -> if (isArabic) "#03 ذهب" else "#03 Gold"
        ForgeKeyTier.DIAMOND -> if (isArabic) "#04 ألماس" else "#04 Diamond"
    }
    val statusText = when {
        isReady -> if (isArabic) "جاهز 100%" else "Ready 100%"
        isWorking -> formatForgeCountdown(slot.remainingSeconds)
        else -> if (isArabic) "خامل" else "Standby"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isWorking || isReady) accent.copy(alpha = 0.12f) else aura.surfaceSubtle,
        border = BorderStroke(
            width = 1.dp,
            color = if (isWorking) accent.copy(alpha = 0.55f * furnacePulse) else if (isReady) accent else aura.borderSubtle
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isWorking) accent.copy(alpha = furnacePulse) else if (isReady) accent else tierColor.copy(alpha = 0.45f)
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = accent,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.5.sp,
                    fontFeatureSettings = "tnum"
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WorkshopMaterialChip(
    label: String,
    count: Int,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(11.dp),
        color = if (aura.isDark) Color(0xAA0A0E17) else aura.surfaceElevated,
        border = BorderStroke(0.9.dp, accent.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.5.sp
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
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
