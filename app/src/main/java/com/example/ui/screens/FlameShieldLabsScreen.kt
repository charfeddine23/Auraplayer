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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FLAME_SHIELD_LABS_COUNT
import com.example.data.model.FlameShieldCraftOption
import com.example.data.model.FlameShieldForgeSlotState
import com.example.data.model.ForgeUiState
import com.example.ui.theme.AuraAtmosphericLayer
import com.example.ui.theme.aura
import com.example.ui.theme.stableStatusBarsPadding
import com.example.ui.viewmodel.StreakViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FlameShieldLabsScreen(
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

    var nowEpochMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val hasAnyActiveLab = forgeState.flameShieldSlots.any { it.isActive && !it.isReadyToClaim(nowEpochMs) }

    LaunchedEffect(forgeState.flameShieldSlots) {
        nowEpochMs = System.currentTimeMillis()
        while (forgeState.flameShieldSlots.any { it.isActive && !it.isReadyToClaim(nowEpochMs) }) {
            delay(1000L)
            nowEpochMs = System.currentTimeMillis()
        }
    }

    val activeLabsCount = forgeState.flameShieldSlots.count { it.isActive && !it.isReadyToClaim(nowEpochMs) }
    val readyLabsCount = forgeState.flameShieldSlots.count { it.isReadyToClaim(nowEpochMs) }

    val ambientTransition = rememberInfiniteTransition(label = "flame_labs_ambient")
    val ambientPulse by ambientTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_labs_pulse"
    )
    val ambientWave by ambientTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flame_labs_wave"
    )

    BackHandler {
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(aura.background)
            .testTag("flame_shield_labs_screen")
    ) {
        AuraAtmosphericLayer()

        // Imperial Obsidian & Molten Thermal Backdrop Aura when labs are working
        Canvas(modifier = Modifier.fillMaxSize()) {
            val activeIntensity = when {
                activeLabsCount >= 2 -> 1.0f
                activeLabsCount == 1 -> 0.72f
                readyLabsCount > 0 -> 0.62f
                else -> 0.25f
            }
            val topGlowColor = when {
                activeLabsCount > 0 -> Color(0xFFFF4D00)
                readyLabsCount > 0 -> Color(0xFF10B981)
                else -> Color(0xFFFF7A1A)
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        topGlowColor.copy(alpha = 0.24f * activeIntensity * ambientPulse),
                        Color(0xFFFFD700).copy(alpha = 0.11f * activeIntensity),
                        Color.Transparent
                    ),
                    radius = size.width * 0.92f
                ),
                center = Offset(size.width * 0.5f, size.height * 0.14f)
            )
            if (activeLabsCount > 0) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF6D00).copy(alpha = 0.14f * activeIntensity * ambientPulse),
                            Color(0xFFD50000).copy(alpha = 0.08f * activeIntensity),
                            Color.Transparent
                        ),
                        radius = size.width * 0.85f
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.65f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .stableStatusBarsPadding()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Top Sanctuary Header Bar
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
                            .testTag("flame_labs_back_button")
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
                                text = "The Flame Shield",
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
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = aura.accentFlame,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = if (isArabic) {
                                "معملان متوازيان للصهر • 24 ساعة وقت فعلي"
                            } else {
                                "2 Parallel Crucible Labs • 24h Real-Time"
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

                // Active Labs Summary Chip + Vault Count
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (activeLabsCount > 0) aura.accentFlameSoft else aura.surfaceCard,
                    border = BorderStroke(
                        1.1.dp,
                        if (activeLabsCount > 0) aura.accentFlame.copy(alpha = 0.7f) else aura.accentGold.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Flame Shields Vault",
                            tint = if (activeLabsCount > 0) aura.accentFlame else aura.accentGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${forgeState.flameShieldsCount}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Imperial Twin-Reactor Status & Resources Overview
                item(key = "flame_labs_overview") {
                    FlameLabsTelemetryHeader(
                        shieldsCount = shieldsCount,
                        forgeState = forgeState,
                        nowEpochMs = nowEpochMs,
                        activeLabsCount = activeLabsCount,
                        readyLabsCount = readyLabsCount,
                        ambientPulse = ambientPulse,
                        isArabic = isArabic
                    )
                }

                // Render the 2 Independent Flame Shield Crafting Labs
                items(
                    count = FLAME_SHIELD_LABS_COUNT,
                    key = { idx -> "flame_shield_lab_$idx" }
                ) { slotIndex ->
                    val slot = forgeState.getFlameShieldSlot(slotIndex)
                    FlameShieldLabCard(
                        slotIndex = slotIndex,
                        slot = slot,
                        shieldsCount = shieldsCount,
                        forgeState = forgeState,
                        nowEpochMs = nowEpochMs,
                        ambientPulse = ambientPulse,
                        ambientWave = ambientWave,
                        isArabic = isArabic,
                        onCraft = { option ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            streakViewModel.craftFlameShield(
                                option = option,
                                slotIndex = slotIndex,
                                isArabic = isArabic
                            )
                        },
                        onClaim = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            streakViewModel.claimFlameShieldCraft(
                                slotIndex = slotIndex,
                                isArabic = isArabic
                            )
                        },
                        onCancel = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            streakViewModel.cancelFlameShieldCraft(
                                slotIndex = slotIndex,
                                isArabic = isArabic
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FlameLabsTelemetryHeader(
    shieldsCount: Int,
    forgeState: ForgeUiState,
    nowEpochMs: Long,
    activeLabsCount: Int,
    readyLabsCount: Int,
    ambientPulse: Float,
    isArabic: Boolean
) {
    val aura = MaterialTheme.aura
    val slot0 = forgeState.getFlameShieldSlot(0)
    val slot1 = forgeState.getFlameShieldSlot(1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (aura.isDark) {
                        listOf(Color(0xFF1F110E), Color(0xFF121522), Color(0xFF0A0D16))
                    } else {
                        listOf(Color(0xFFFFF5EC), Color(0xFFFFFBF7), Color(0xFFF6F9FE))
                    }
                )
            )
            .border(
                width = 1.3.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        aura.accentFlame.copy(alpha = if (activeLabsCount > 0) ambientPulse else 0.55f),
                        aura.accentGold.copy(alpha = 0.78f),
                        aura.accentFlame.copy(alpha = if (activeLabsCount > 0) ambientPulse else 0.55f)
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
                        text = if (isArabic) "غرفة المعامل الملكية (2 معامل)" else "Imperial Crucible Chamber (2 Labs)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isArabic) {
                            "يمكنك تشغيل المعملين معاً لصهر درعين بالتوازي"
                        } else {
                            "Run both labs simultaneously to forge 2 Flame Shields in parallel"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                val statusLabel = when {
                    readyLabsCount > 0 -> if (isArabic) "$readyLabsCount جاهز ✨" else "$readyLabsCount Ready ✨"
                    activeLabsCount > 0 -> if (isArabic) "$activeLabsCount / 2 نشط 🔥" else "$activeLabsCount / 2 Active 🔥"
                    else -> if (isArabic) "0 / 2 نشط" else "0 / 2 Active"
                }
                val statusColor = when {
                    readyLabsCount > 0 -> aura.accentEmerald
                    activeLabsCount > 0 -> aura.accentFlame
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

            // Twin Lab Mini Telemetry Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LabMiniTelemetryBadge(
                    labNumber = 1,
                    slot = slot0,
                    nowEpochMs = nowEpochMs,
                    ambientPulse = ambientPulse,
                    isArabic = isArabic,
                    modifier = Modifier.weight(1f)
                )
                LabMiniTelemetryBadge(
                    labNumber = 2,
                    slot = slot1,
                    nowEpochMs = nowEpochMs,
                    ambientPulse = ambientPulse,
                    isArabic = isArabic,
                    modifier = Modifier.weight(1f)
                )
            }

            // Craft Materials Strip (Shields + Bronze + Silver + Diamond)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LabMaterialChip(
                    label = if (isArabic) "دروع" else "Shields",
                    count = shieldsCount,
                    accent = aura.accentCyan,
                    icon = Icons.Default.Shield,
                    modifier = Modifier.weight(1f)
                )
                LabMaterialChip(
                    label = if (isArabic) "برونزي" else "Bronze",
                    count = forgeState.bronzeKeys,
                    accent = if (aura.isDark) Color(0xFFE59A54) else Color(0xFFB45309),
                    icon = Icons.Default.VpnKey,
                    modifier = Modifier.weight(1f)
                )
                LabMaterialChip(
                    label = if (isArabic) "فضي" else "Silver",
                    count = forgeState.silverKeys,
                    accent = if (aura.isDark) Color(0xFFE2E8F0) else Color(0xFF475569),
                    icon = Icons.Default.VpnKey,
                    modifier = Modifier.weight(1f)
                )
                LabMaterialChip(
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
private fun LabMiniTelemetryBadge(
    labNumber: Int,
    slot: FlameShieldForgeSlotState,
    nowEpochMs: Long,
    ambientPulse: Float,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val isReady = slot.isReadyToClaim(nowEpochMs)
    val isWorking = slot.isActive && !isReady
    val accent = when {
        isReady -> aura.accentEmerald
        isWorking -> aura.accentFlame
        else -> aura.textTertiary
    }
    val statusText = when {
        isReady -> if (isArabic) "مكتمل 100%" else "Ready 100%"
        isWorking -> formatLabHms(slot.remainingMs(nowEpochMs))
        else -> if (isArabic) "جاهز للإشعال" else "Standby"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isWorking || isReady) accent.copy(alpha = 0.12f) else aura.surfaceSubtle,
        border = BorderStroke(
            width = 1.dp,
            color = if (isWorking) accent.copy(alpha = 0.55f * ambientPulse) else if (isReady) accent else aura.borderSubtle
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            accent.copy(alpha = if (isWorking) ambientPulse else if (isReady) 1f else 0.45f)
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "معمل 0$labNumber" else "Lab 0$labNumber",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.5.sp
                    )
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
private fun LabMaterialChip(
    label: String,
    count: Int,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = accent.copy(alpha = 0.9f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FlameShieldLabCard(
    slotIndex: Int,
    slot: FlameShieldForgeSlotState,
    shieldsCount: Int,
    forgeState: ForgeUiState,
    nowEpochMs: Long,
    ambientPulse: Float,
    ambientWave: Float,
    isArabic: Boolean,
    onCraft: (FlameShieldCraftOption) -> Unit,
    onClaim: () -> Unit,
    onCancel: () -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val labNumber = slotIndex + 1

    val isActive = slot.isActive
    val isReady = slot.isReadyToClaim(nowEpochMs)
    val isForging = isActive && !isReady
    val remainingMs = slot.remainingMs(nowEpochMs)
    val elapsedMs = slot.elapsedMs(nowEpochMs)
    val progressFraction = slot.progress(nowEpochMs)

    val firstAffordable = FlameShieldCraftOption.entries.firstOrNull { it.canAfford(shieldsCount, forgeState) }
    var selectedOption by remember(slotIndex) {
        mutableStateOf(firstAffordable ?: FlameShieldCraftOption.SHIELDS_100)
    }
    val canCraftSelected = selectedOption.canAfford(shieldsCount, forgeState)

    val flamePrimary = aura.accentFlame
    val goldAccent = aura.accentGold
    val emeraldAccent = aura.accentEmerald

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (aura.isDark) {
                        when {
                            isReady -> listOf(Color(0xFF0E261E), Color(0xFF101722), Color(0xFF070A12))
                            isForging -> listOf(Color(0xFF2B100A), Color(0xFF170E18), Color(0xFF080A12))
                            else -> listOf(Color(0xFF17131B), Color(0xFF10131E), Color(0xFF080B12))
                        }
                    } else {
                        when {
                            isReady -> listOf(Color(0xFFECFDF5), Color(0xFFF6FEF9), Color(0xFFFFFFFF))
                            isForging -> listOf(Color(0xFFFFF1E6), Color(0xFFFFF8F1), Color(0xFFFFFFFF))
                            else -> listOf(Color(0xFFF8FAFC), Color(0xFFFFFFFF), Color(0xFFF3F6FB))
                        }
                    }
                )
            )
            .border(
                width = if (isForging || isReady) 2.dp else 1.2.dp,
                brush = Brush.linearGradient(
                    when {
                        isReady -> listOf(emeraldAccent, goldAccent, emeraldAccent)
                        isForging -> listOf(
                            Color(0xFFFF3D00).copy(alpha = ambientPulse),
                            goldAccent,
                            Color(0xFFFF9100).copy(alpha = ambientPulse)
                        )
                        else -> listOf(
                            goldAccent.copy(alpha = 0.42f),
                            aura.borderSubtle,
                            flamePrimary.copy(alpha = 0.35f)
                        )
                    }
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .testTag("flame_shield_lab_card_$slotIndex")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Lab Header Plaque
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isReady -> aura.accentEmeraldSoft
                            isForging -> aura.accentFlameSoft
                            else -> goldAccent.copy(alpha = 0.14f)
                        },
                        border = BorderStroke(
                            1.2.dp,
                            when {
                                isReady -> emeraldAccent
                                isForging -> flamePrimary
                                else -> goldAccent.copy(alpha = 0.55f)
                            }
                        )
                    ) {
                        Text(
                            text = "#0$labNumber",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = when {
                                    isReady -> emeraldAccent
                                    isForging -> flamePrimary
                                    else -> goldAccent
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isArabic) {
                                if (labNumber == 1) "المعمل الأول • البوتقة الملكية" else "المعمل الثاني • المفاعل الإمبراطوري"
                            } else {
                                if (labNumber == 1) "Lab 01 • Royal Crucible" else "Lab 02 • Imperial Reactor"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isArabic) {
                                "صناعة درع الشعلة • 24 ساعة وقت فعلي"
                            } else {
                                "Flame Shield Synthesis • 24h Real-Time"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.textSecondary,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }

                val badgeColor = when {
                    isReady -> emeraldAccent
                    isForging -> flamePrimary
                    else -> goldAccent
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.65f))
                ) {
                    Text(
                        text = when {
                            isReady -> if (isArabic) "جاهز للاستلام ✨" else "READY ✨"
                            isForging -> if (isArabic) "قيد الصهر 🔥" else "FORGING 🔥"
                            else -> if (isArabic) "خامل" else "IDLE"
                        },
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // LUXURY ANIMATED CRUCIBLE VISUALIZATION CHAMBER
            LuxuryFlameShieldCrucibleStage(
                isForging = isForging,
                isReady = isReady,
                progressFraction = progressFraction,
                ambientPulse = ambientPulse,
                ambientWave = ambientWave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isForging || isReady) 216.dp else 132.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            when {
                                isReady -> listOf(emeraldAccent.copy(alpha = 0.75f), goldAccent.copy(alpha = 0.5f))
                                isForging -> listOf(
                                    Color(0xFFFFD700).copy(alpha = 0.75f),
                                    Color(0xFFFF4D00).copy(alpha = 0.55f)
                                )
                                else -> listOf(aura.borderSubtle, aura.borderMuted)
                            }
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("flame_lab_crucible_stage_$slotIndex")
            )

            if (isActive) {
                val activeRecipeLabel = slot.selectedOption?.let { opt ->
                    if (isArabic) opt.labelAr else opt.labelEn
                } ?: if (isArabic) "وصفة درع الشعلة" else "Flame Shield Recipe"
                val progressPercent = (progressFraction * 100f).toInt().coerceIn(0, 100)

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (aura.isDark) Color(0xCC090D16) else aura.surfaceElevated,
                    border = BorderStroke(
                        1.1.dp,
                        (if (isReady) emeraldAccent else goldAccent).copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flame_lab_timer_panel_$slotIndex")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isReady) {
                                        if (isArabic) "اكتمل تصنيع درع الشعلة!" else "Flame Shield Tempering Complete!"
                                    } else {
                                        if (isArabic) "الوقت الفعلي المتبقي" else "Real-Time Remaining"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isReady) emeraldAccent else aura.textSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isReady) "00:00:00" else formatLabHms(remainingMs),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        color = if (isReady) emeraldAccent else aura.textPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    modifier = Modifier.testTag("flame_lab_countdown_$slotIndex")
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = (if (isReady) emeraldAccent else goldAccent).copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "$progressPercent%",
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isReady) emeraldAccent else goldAccent,
                                            fontWeight = FontWeight.Black,
                                            fontFeatureSettings = "tnum"
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeRecipeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = aura.textTertiary,
                                        fontSize = 10.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Custom Luxury Molten Temper Progress Bar
                        LuxuryMoltenTemperProgressBar(
                            progress = progressFraction,
                            isReady = isReady,
                            pulse = ambientPulse,
                            wave = ambientWave,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) {
                                    "المُنجز: ${formatLabHms(elapsedMs)} / 24:00:00"
                                } else {
                                    "Elapsed: ${formatLabHms(elapsedMs)} / 24:00:00"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 10.5.sp,
                                    fontFeatureSettings = "tnum"
                                )
                            )
                            Text(
                                text = if (isArabic) "مستمر بالوقت الفعلي 24/7" else "Active 24/7 real clock",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = aura.textTertiary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                if (isReady) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(emeraldAccent, Color(0xFF00E5FF), goldAccent)
                                )
                            )
                            .clickable(onClick = onClaim)
                            .testTag("claim_flame_shield_lab_$slotIndex"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF07090D),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "استلام The Flame Shield الآن" else "Claim The Flame Shield Now",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color(0xFF07090D),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = aura.surfaceSubtle,
                        border = BorderStroke(1.dp, Color(0x66FF5C77)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onCancel)
                            .testTag("cancel_flame_shield_lab_$slotIndex")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) "إلغاء الصهر واسترداد الموارد" else "Cancel & Refund Resources",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFFF6E86),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            } else {
                // 4 Recipe Choices in a 2x2 Grid
                val options = FlameShieldCraftOption.entries
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { option ->
                                val isSelected = selectedOption == option
                                val affordable = option.canAfford(shieldsCount, forgeState)
                                val optionAccent = when (option) {
                                    FlameShieldCraftOption.SHIELDS_100 -> aura.accentCyan
                                    FlameShieldCraftOption.BRONZE_3 -> if (aura.isDark) Color(0xFFE59A54) else Color(0xFFB45309)
                                    FlameShieldCraftOption.SILVER_2 -> if (aura.isDark) Color(0xFFE2E8F0) else Color(0xFF475569)
                                    FlameShieldCraftOption.DIAMOND_1 -> if (aura.isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                }
                                val optionIcon = when (option) {
                                    FlameShieldCraftOption.SHIELDS_100 -> Icons.Default.Shield
                                    FlameShieldCraftOption.DIAMOND_1 -> Icons.Default.Diamond
                                    else -> Icons.Default.VpnKey
                                }
                                val stockText = when (option) {
                                    FlameShieldCraftOption.SHIELDS_100 -> "$shieldsCount / 100"
                                    FlameShieldCraftOption.BRONZE_3 -> "${forgeState.bronzeKeys} / 3"
                                    FlameShieldCraftOption.SILVER_2 -> "${forgeState.silverKeys} / 2"
                                    FlameShieldCraftOption.DIAMOND_1 -> "${forgeState.diamondKeys} / 1"
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = when {
                                        isSelected -> optionAccent.copy(alpha = 0.17f)
                                        affordable -> aura.surfaceElevated
                                        else -> aura.surfaceSubtle
                                    },
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = when {
                                            isSelected -> optionAccent
                                            affordable -> optionAccent.copy(alpha = 0.45f)
                                            else -> aura.borderSubtle
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedOption = option
                                        }
                                        .testTag("flame_lab_${slotIndex}_option_${option.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(optionAccent.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = optionIcon,
                                                contentDescription = null,
                                                tint = optionAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isArabic) option.labelAr else option.labelEn,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = aura.textPrimary,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 11.5.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = stockText,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (affordable) emeraldAccent else aura.textTertiary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = optionAccent,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val selectedLabel = if (isArabic) selectedOption.labelAr else selectedOption.labelEn
                if (canCraftSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFF4D00), Color(0xFFFF8C00), Color(0xFFFFD700))
                                )
                            )
                            .clickable { onCraft(selectedOption) }
                            .testTag("craft_flame_shield_lab_$slotIndex"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFF07090D),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) {
                                    "إشعال المعمل 0$labNumber • 24 ساعة ($selectedLabel)"
                                } else {
                                    "Ignite Lab 0$labNumber • 24h ($selectedLabel)"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color(0xFF07090D),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        color = aura.surfaceSubtle,
                        border = BorderStroke(1.dp, aura.borderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .clickable { onCraft(selectedOption) }
                            .testTag("craft_flame_shield_lab_locked_$slotIndex")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = aura.textTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) {
                                    "تحتاج إلى $selectedLabel لتشغيل المعمل (24 ساعة)"
                                } else {
                                    "Requires $selectedLabel to ignite Lab (24h)"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = aura.textSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

private class LuxuryParticle(
    var x: Float = 0.5f,
    var y: Float = 0.5f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var angle: Float = 0f,
    var radius: Float = 0.4f,
    var angularSpeed: Float = 1f,
    var radialSpeed: Float = 0.1f,
    var life: Float = 0f,
    var maxLife: Float = 1f,
    var sizePx: Float = 2f,
    var isOrbitalInfuser: Boolean = false
)

private class FlameCrucibleVfxEngine {
    val particles = MutableList(64) { LuxuryParticle() }
    var timeSec: Float = 0f
    var pulseTimerSec: Float = 0f
    var shockwaveProgress: Float = 0f // 0..1 active wave, <0 inactive
    private var nextSlot = 0
    private var emberSpawnTimer = 0f
    private var infuserSpawnTimer = 0f

    init {
        repeat(24) {
            spawnPhoenixEmber(randomY = true)
        }
        repeat(14) {
            spawnOrbitalInfuser(randomProgress = true)
        }
    }

    fun step(dtSec: Float, isForging: Boolean, isReady: Boolean) {
        val dt = dtSec.coerceIn(0.001f, 0.05f)
        timeSec += dt

        if (isForging || isReady) {
            pulseTimerSec += dt
            val interval = if (isForging) 2.0f else 2.8f
            if (pulseTimerSec >= interval) {
                pulseTimerSec -= interval
                shockwaveProgress = 0.01f
            }
            if (shockwaveProgress > 0f) {
                shockwaveProgress += dt * (if (isForging) 0.72f else 0.52f)
                if (shockwaveProgress >= 1f) {
                    shockwaveProgress = -1f
                }
            }
        } else {
            shockwaveProgress = -1f
        }

        val emberInterval = when {
            isForging -> 0.045f
            isReady -> 0.08f
            else -> 0.25f
        }
        emberSpawnTimer += dt
        if (emberSpawnTimer >= emberInterval) {
            emberSpawnTimer = 0f
            spawnPhoenixEmber(randomY = false)
        }

        if (isForging) {
            infuserSpawnTimer += dt
            if (infuserSpawnTimer >= 0.085f) {
                infuserSpawnTimer = 0f
                spawnOrbitalInfuser(randomProgress = false)
            }
        }

        for (p in particles) {
            if (p.life <= 0f) continue
            p.life -= dt
            if (p.isOrbitalInfuser) {
                p.angle += p.angularSpeed * dt
                p.radius = (p.radius - p.radialSpeed * dt).coerceAtLeast(0.04f)
            } else {
                val sway = sin(timeSec * 3.4f + p.angle) * 0.045f
                p.x += (p.vx + sway) * dt
                p.y += p.vy * dt
            }
        }
    }

    private fun allocate(): LuxuryParticle {
        val p = particles[nextSlot]
        nextSlot = (nextSlot + 1) % particles.size
        return p
    }

    private fun spawnPhoenixEmber(randomY: Boolean) {
        val p = allocate()
        p.isOrbitalInfuser = false
        p.x = 0.14f + Random.nextFloat() * 0.72f
        p.y = if (randomY) Random.nextFloat() * 0.9f else 0.88f + Random.nextFloat() * 0.10f
        p.vx = (Random.nextFloat() - 0.5f) * 0.05f
        p.vy = -(0.16f + Random.nextFloat() * 0.26f)
        p.angle = Random.nextFloat() * 6.28f
        p.maxLife = 1.1f + Random.nextFloat() * 1.1f
        p.life = if (randomY) Random.nextFloat() * p.maxLife else p.maxLife
        p.sizePx = 1.6f + Random.nextFloat() * 2.4f
    }

    private fun spawnOrbitalInfuser(randomProgress: Boolean) {
        val p = allocate()
        p.isOrbitalInfuser = true
        p.angle = Random.nextFloat() * 6.283f
        val startR = 0.40f + Random.nextFloat() * 0.10f
        p.radius = if (randomProgress) 0.12f + Random.nextFloat() * 0.30f else startR
        p.angularSpeed = 1.8f + Random.nextFloat() * 1.4f
        p.radialSpeed = 0.20f + Random.nextFloat() * 0.12f
        p.maxLife = 1.25f + Random.nextFloat() * 0.5f
        p.life = if (randomProgress) Random.nextFloat() * p.maxLife else p.maxLife
        p.sizePx = 1.8f + Random.nextFloat() * 2.0f
    }
}

@Composable
private fun LuxuryFlameShieldCrucibleStage(
    isForging: Boolean,
    isReady: Boolean,
    progressFraction: Float,
    ambientPulse: Float,
    ambientWave: Float,
    modifier: Modifier = Modifier
) {
    val engine = remember { FlameCrucibleVfxEngine() }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(engine, isForging, isReady) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = (now - lastNanos) / 1_000_000_000f
                    engine.step(dt, isForging = isForging, isReady = isReady)
                }
                lastNanos = now
                frameTick = now
            }
        }
    }

    Canvas(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = when {
                    isReady -> listOf(Color(0xFF071B15), Color(0xFF09121B), Color(0xFF05080F))
                    isForging -> listOf(Color(0xFF1B0907), Color(0xFF100A14), Color(0xFF060810))
                    else -> listOf(Color(0xFF0D111A), Color(0xFF090C14), Color(0xFF06080E))
                }
            )
        )
    ) {
        // Read frameTick strictly inside draw phase to prevent Compose recompositions at 60fps
        @Suppress("UNUSED_VARIABLE")
        val tick = frameTick
        val t = engine.timeSec
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val minDim = size.minDimension

        // 1. Volumetric Multi-Stop Thermal Bloom
        if (isForging) {
            val breath = 0.85f + 0.15f * sin(t * 2.8f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = 0.28f * breath),
                        Color(0xFFFF5722).copy(alpha = 0.34f * breath),
                        Color(0xFF991B1B).copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    radius = minDim * 0.68f
                ),
                center = center
            )
        } else if (isReady) {
            val breath = 0.88f + 0.12f * sin(t * 2.2f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF34D399).copy(alpha = 0.36f * breath),
                        Color(0xFFFFD700).copy(alpha = 0.22f * breath),
                        Color.Transparent
                    ),
                    radius = minDim * 0.70f
                ),
                center = center
            )
        } else {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x28FF7A1A),
                        Color(0x14334155),
                        Color.Transparent
                    ),
                    radius = minDim * 0.58f
                ),
                center = center
            )
        }

        // 2. Celebratory Radiant Light Beams (When Ready) or Subtle Forge Flare Rays (When Forging)
        if (isForging || isReady) {
            val rayCount = if (isReady) 12 else 8
            val rayRotation = t * (if (isReady) 14f else 10f)
            rotate(rayRotation, pivot = center) {
                for (i in 0 until rayCount) {
                    val angleRad = (i * (360f / rayCount)) * (PI / 180.0)
                    val innerR = minDim * 0.19f
                    val outerR = minDim * (if (isReady) 0.47f else 0.44f)
                    val sx = center.x + (innerR * cos(angleRad)).toFloat()
                    val sy = center.y + (innerR * sin(angleRad)).toFloat()
                    val ex = center.x + (outerR * cos(angleRad)).toFloat()
                    val ey = center.y + (outerR * sin(angleRad)).toFloat()
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                (if (isReady) Color(0xFF6EE7B7) else Color(0xFFFFD54F)).copy(alpha = 0.24f),
                                Color.Transparent
                            ),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey)
                        ),
                        start = Offset(sx, sy),
                        end = Offset(ex, ey),
                        strokeWidth = (if (isReady) 3.2f else 2.4f).dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // 3. Sacred Imperial Astrolabe & Plasma Containment Rings
        val outerR = minDim * 0.41f
        val midR = minDim * 0.33f
        val innerR = minDim * 0.255f

        if (isForging) {
            // Outer Gold Astrolabe Ring rotating Clockwise
            rotate(t * 22f, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFF6D00),
                            Color(0x44FFD700),
                            Color(0xFFFFF59D),
                            Color(0xFFFFD700)
                        ),
                        center = center
                    ),
                    radius = outerR,
                    center = center,
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                )
                // 16 Engraved Imperial Filigree Ticks + 4 Cardinal Gem Anchors
                for (i in 0 until 16) {
                    val rad = i * (PI / 8.0)
                    val isCardinal = (i % 4 == 0)
                    val rStart = outerR - (if (isCardinal) 6.dp.toPx() else 3.dp.toPx())
                    val rEnd = outerR + (if (isCardinal) 6.dp.toPx() else 3.dp.toPx())
                    val p1 = Offset(center.x + (rStart * cos(rad)).toFloat(), center.y + (rStart * sin(rad)).toFloat())
                    val p2 = Offset(center.x + (rEnd * cos(rad)).toFloat(), center.y + (rEnd * sin(rad)).toFloat())
                    drawLine(
                        color = if (isCardinal) Color(0xFFFFF8DC) else Color(0xAAFFD700),
                        start = p1,
                        end = p2,
                        strokeWidth = (if (isCardinal) 2.2f else 1.1f).dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    if (isCardinal) {
                        val nodeCenter = Offset(
                            center.x + ((outerR + 7.dp.toPx()) * cos(rad)).toFloat(),
                            center.y + ((outerR + 7.dp.toPx()) * sin(rad)).toFloat()
                        )
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.9f),
                            radius = 3.dp.toPx(),
                            center = nodeCenter
                        )
                    }
                }
            }

            // Middle Molten Plasma Containment Ring rotating Counter-Clockwise
            rotate(-t * 36f, pivot = center) {
                val arcRectTopLeft = Offset(center.x - midR, center.y - midR)
                val arcSize = Size(midR * 2f, midR * 2f)
                for (seg in 0 until 3) {
                    val startAngle = seg * 120f + 12f
                    // Soft glow under-pass
                    drawArc(
                        color = Color(0xFFFF5722).copy(alpha = 0.35f),
                        startAngle = startAngle,
                        sweepAngle = 76f,
                        useCenter = false,
                        topLeft = arcRectTopLeft,
                        size = arcSize,
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Sharp molten arc core
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFFFF4D00), Color(0xFFFFB300), Color(0xFFFFF8E1), Color(0xFFFF4D00)),
                            center = center
                        ),
                        startAngle = startAngle,
                        sweepAngle = 76f,
                        useCenter = false,
                        topLeft = arcRectTopLeft,
                        size = arcSize,
                        style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Inner Real-Time 24h Tempering Progress Ring + Incandescent Comet Tip
            val progressRectTopLeft = Offset(center.x - innerR, center.y - innerR)
            val progressRectSize = Size(innerR * 2f, innerR * 2f)
            drawCircle(
                color = Color(0x33FFFFFF),
                radius = innerR,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
            val sweepDeg = (progressFraction.coerceIn(0.02f, 1f) * 360f)
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color(0xFFFF5722), Color(0xFFFFB300), Color(0xFFFFF59D), Color(0xFFFFD700)),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = sweepDeg,
                useCenter = false,
                topLeft = progressRectTopLeft,
                size = progressRectSize,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
            val tipRad = (-90f + sweepDeg) * (PI / 180.0)
            val tipOffset = Offset(
                center.x + (innerR * cos(tipRad)).toFloat(),
                center.y + (innerR * sin(tipRad)).toFloat()
            )
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = 0.45f * ambientPulse),
                radius = 8.dp.toPx(),
                center = tipOffset
            )
            drawCircle(
                color = Color.White,
                radius = 3.2.dp.toPx(),
                center = tipOffset
            )
        } else if (isReady) {
            rotate(t * 18f, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(Color(0xFF34D399), Color(0xFFFFD700), Color(0xFF00E5FF), Color(0xFF34D399)),
                        center = center
                    ),
                    radius = outerR,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.55f),
                radius = midR,
                center = center,
                style = Stroke(width = 1.6.dp.toPx())
            )
        } else {
            // Dormant Obsidian-Bronze Astrolabe Ring
            drawCircle(
                color = Color(0x4494A3B8),
                radius = outerR,
                center = center,
                style = Stroke(width = 1.6.dp.toPx())
            )
            drawCircle(
                color = Color(0x28CBD5E1),
                radius = innerR,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )
        }

        // 4. Rhythmic Tempering Shockwave Expanding Outward
        if (engine.shockwaveProgress > 0f && (isForging || isReady)) {
            val sw = engine.shockwaveProgress
            val swAlpha = ((1f - sw) * 0.65f).coerceIn(0f, 1f)
            val swColor = if (isReady) Color(0xFF6EE7B7) else Color(0xFFFFD54F)
            drawCircle(
                color = swColor.copy(alpha = swAlpha),
                radius = innerR * 0.7f + (outerR * 0.65f * sw),
                center = center,
                style = Stroke(width = ((1f - sw) * 3.2f + 0.8f).dp.toPx())
            )
        }

        // 5. Dual Physics Particle Render (Orbital Energy Infusers + Phoenix Embers)
        for (p in engine.particles) {
            if (p.life <= 0f) continue
            val heat = (p.life / p.maxLife).coerceIn(0f, 1f)
            if (p.isOrbitalInfuser) {
                val rPx = p.radius * minDim
                val px = center.x + (rPx * cos(p.angle.toDouble())).toFloat()
                val py = center.y + (rPx * sin(p.angle.toDouble())).toFloat()
                val color = if (p.sizePx > 2.8f) Color(0xFF38BDF8) else Color(0xFFFFD700)
                drawCircle(
                    color = color.copy(alpha = heat * 0.35f),
                    radius = p.sizePx * 2.1f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White.copy(alpha = heat * 0.9f),
                    radius = p.sizePx * 0.8f,
                    center = Offset(px, py)
                )
            } else {
                val px = p.x * w
                val py = p.y * h
                val emberColor = when {
                    isReady -> lerp(Color(0xFF10B981), Color(0xFFFFF59D), heat)
                    heat > 0.72f -> lerp(Color(0xFFFFD54F), Color(0xFFFFFDE7), (heat - 0.72f) / 0.28f)
                    heat > 0.35f -> lerp(Color(0xFFFF6D00), Color(0xFFFFD54F), (heat - 0.35f) / 0.37f)
                    else -> lerp(Color(0xFF991B1B), Color(0xFFFF5722), heat / 0.35f)
                }
                val alphaFactor = if (isForging || isReady) 0.9f else 0.28f
                drawCircle(
                    color = emberColor.copy(alpha = (heat * alphaFactor * 0.32f).coerceIn(0f, 1f)),
                    radius = p.sizePx * 2.2f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = emberColor.copy(alpha = (heat * alphaFactor).coerceIn(0f, 1f)),
                    radius = p.sizePx,
                    center = Offset(px, py)
                )
            }
        }

        // 6. Central Heraldic Flame Shield Crest (Custom Vector Path)
        val crestScale = if (isForging) {
            minDim * (0.19f + 0.008f * sin(t * 3.1f))
        } else if (isReady) {
            minDim * (0.205f + 0.008f * sin(t * 2.4f))
        } else {
            minDim * 0.24f
        }
        drawImperialFlameShieldCrest(
            center = center,
            radius = crestScale,
            isForging = isForging,
            isReady = isReady,
            progressFraction = progressFraction,
            timeSec = t,
            waveProgress = ambientWave
        )
    }
}

private fun DrawScope.drawImperialFlameShieldCrest(
    center: Offset,
    radius: Float,
    isForging: Boolean,
    isReady: Boolean,
    progressFraction: Float,
    timeSec: Float,
    waveProgress: Float
) {
    val shieldPath = Path().apply {
        val topY = center.y - radius * 0.98f
        val shoulderY = center.y - radius * 0.62f
        val midY = center.y + radius * 0.14f
        val bottomY = center.y + radius * 1.05f
        val halfW = radius * 0.86f

        moveTo(center.x, topY)
        cubicTo(
            center.x + halfW * 0.45f, topY + radius * 0.12f,
            center.x + halfW * 0.82f, shoulderY - radius * 0.08f,
            center.x + halfW, shoulderY
        )
        cubicTo(
            center.x + halfW * 0.96f, midY,
            center.x + halfW * 0.52f, bottomY - radius * 0.30f,
            center.x, bottomY
        )
        cubicTo(
            center.x - halfW * 0.52f, bottomY - radius * 0.30f,
            center.x - halfW * 0.96f, midY,
            center.x - halfW, shoulderY
        )
        cubicTo(
            center.x - halfW * 0.82f, shoulderY - radius * 0.08f,
            center.x - halfW * 0.45f, topY + radius * 0.12f,
            center.x, topY
        )
        close()
    }

    // Outer Halo Glow around Shield Silhouette
    if (isForging || isReady) {
        drawPath(
            path = shieldPath,
            color = (if (isReady) Color(0xFF34D399) else Color(0xFFFF8C00)).copy(alpha = 0.28f),
            style = Stroke(width = 8.dp.toPx(), join = StrokeJoin.Round)
        )
    }

    // Base Obsidian-Bronze Shield Body
    drawPath(
        path = shieldPath,
        brush = Brush.verticalGradient(
            colors = when {
                isReady -> listOf(Color(0xFF134E4A), Color(0xFF065F46), Color(0xFF042F2E))
                isForging -> listOf(Color(0xFF3B150E), Color(0xFF1E0D14), Color(0xFF0E0A12))
                else -> listOf(Color(0xFF283142), Color(0xFF171D28), Color(0xFF0E121B))
            }
        )
    )

    // Clipped Interior Tempering Fill + Specular Glint + Living Sacred Flame
    clipPath(shieldPath) {
        if (isForging) {
            val fillH = (progressFraction.coerceIn(0.14f, 1f)) * (radius * 2.05f)
            val fillTop = (center.y + radius * 1.05f) - fillH
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xAAFF9800),
                        Color(0xCCFF3D00),
                        Color(0xEEB91C1C)
                    ),
                    startY = fillTop,
                    endY = center.y + radius * 1.05f
                ),
                topLeft = Offset(center.x - radius, fillTop),
                size = Size(radius * 2f, fillH + 4f)
            )
        }

        // Diagonal Metallic Specular Sweep across Shield Face
        if (isForging || isReady) {
            val sheenX = center.x - radius * 1.8f + waveProgress * radius * 3.6f
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.28f), Color.Transparent)
                ),
                start = Offset(sheenX - radius * 0.4f, center.y - radius * 1.1f),
                end = Offset(sheenX + radius * 0.4f, center.y + radius * 1.1f),
                strokeWidth = radius * 0.42f
            )
        }

        // Sacred Flame Tongue Drawn Inside the Shield Core
        val sway = if (isForging) sin(timeSec * 5.2f) * radius * 0.08f else 0f
        val flameScale = if (isForging) 1.05f else if (isReady) 1.0f else 0.72f
        val fr = radius * 0.48f * flameScale
        val flamePath = Path().apply {
            val baseY = center.y + fr * 0.78f
            val tipY = center.y - fr * 0.92f
            moveTo(center.x, baseY)
            cubicTo(
                center.x + fr * 0.95f, baseY - fr * 0.15f,
                center.x + fr * 0.75f, center.y - fr * 0.35f,
                center.x + sway, tipY
            )
            cubicTo(
                center.x - fr * 0.75f, center.y - fr * 0.35f,
                center.x - fr * 0.95f, baseY - fr * 0.15f,
                center.x, baseY
            )
            close()
        }
        drawPath(
            path = flamePath,
            brush = Brush.verticalGradient(
                colors = when {
                    isReady -> listOf(Color(0xFFFFFDE7), Color(0xFF6EE7B7), Color(0xFF059669))
                    isForging -> listOf(Color(0xFFFFFDE7), Color(0xFFFFD54F), Color(0xFFFF5722))
                    else -> listOf(Color(0x8894A3B8), Color(0x4464748B))
                }
            )
        )
    }

    // Beveled Imperial Gold / Emerald Metallic Rim
    drawPath(
        path = shieldPath,
        brush = Brush.verticalGradient(
            colors = when {
                isReady -> listOf(Color(0xFFFFF59D), Color(0xFF34D399), Color(0xFFFFD700))
                isForging -> listOf(Color(0xFFFFF8DC), Color(0xFFFFD700), Color(0xFFFF6D00))
                else -> listOf(Color(0xFF94A3B8), Color(0xFF475569))
            }
        ),
        style = Stroke(width = (if (isForging || isReady) 2.5f else 1.8f).dp.toPx(), join = StrokeJoin.Round)
    )
}

@Composable
private fun LuxuryMoltenTemperProgressBar(
    progress: Float,
    isReady: Boolean,
    pulse: Float,
    wave: Float,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val clamped = progress.coerceIn(0f, 1f)
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (aura.isDark) Color(0xFF070B12) else aura.progressTrack)
            .border(1.dp, aura.borderSubtle, RoundedCornerShape(8.dp))
    ) {
        val barWidth = (size.width * clamped).coerceAtLeast(if (clamped > 0f) 6.dp.toPx() else 0f)
        if (barWidth > 2f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = if (isReady) {
                        listOf(Color(0xFF10B981), Color(0xFF00E5FF), Color(0xFFFFD700))
                    } else {
                        listOf(Color(0xFFD50000), Color(0xFFFF6D00), Color(0xFFFFD700))
                    }
                ),
                size = Size(barWidth, size.height),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            // Flowing molten shimmer highlight along filled track
            val shimmerCenter = wave * barWidth
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent),
                    startX = shimmerCenter - 24.dp.toPx(),
                    endX = shimmerCenter + 24.dp.toPx()
                ),
                size = Size(barWidth, size.height)
            )
            // Incandescent tip orb
            drawCircle(
                color = Color.White.copy(alpha = 0.92f * pulse),
                radius = size.height * 0.42f,
                center = Offset(barWidth.coerceIn(size.height * 0.5f, size.width - 2f), size.height * 0.5f)
            )
        }
    }
}

private fun formatLabHms(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}
