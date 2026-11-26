package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MediaPlaybackActivityBus
import com.example.data.model.HomeMimicItem
import com.example.ui.theme.aura
import kotlinx.coroutines.launch

/**
 * Modular Home Screen Mimic Centerpiece Rendering Engine.
 * Routes seamlessly between all 5 Liquid Glass Dark Mode interactive centerpieces
 * as well as Streak Flame and DNA & Chromosomes Helix.
 */
@Composable
fun HomeMimicCenterpieceEngine(
    equippedMimic: HomeMimicItem,
    isMimicEnabled: Boolean,
    streakDays: Int,
    dailyProgress: Float,
    isPlayerActive: Boolean,
    onLongPressToVault: () -> Unit,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isMimicEnabled) return

    val easing = remember { CubicBezierEasing(0.22f, 1f, 0.36f, 1f) }

    AnimatedContent(
        targetState = equippedMimic,
        transitionSpec = {
            (fadeIn(tween(300, easing = easing)) + scaleIn(initialScale = 0.90f, animationSpec = tween(320, easing = easing))) togetherWith
                (fadeOut(tween(200)) + scaleOut(targetScale = 0.92f, animationSpec = tween(220)))
        },
        label = "home_mimic_switch",
        modifier = modifier
    ) { target ->
        when (target) {
            HomeMimicItem.STREAK_FLAME -> {
                FlameParticleComponent(
                    streakDays = streakDays,
                    onLongPressToRoadmap = onLongPressToVault,
                    isArabic = isArabic,
                    modifier = Modifier.fillMaxSize()
                )
            }

            HomeMimicItem.DNA_CHROMOSOME -> {
                DnaChromosomeComponent(
                    streakDays = streakDays,
                    onLongPressToRoadmap = onLongPressToVault,
                    isArabic = isArabic,
                    modifier = Modifier.fillMaxSize()
                )
            }

            HomeMimicItem.QUANTUM_REACTOR,
            HomeMimicItem.ACOUSTIC_SINGULARITY,
            HomeMimicItem.HOLOGRAPHIC_CYBER_VINYL,
            HomeMimicItem.LIQUID_TIME_CRYSTAL,
            HomeMimicItem.CYBERNETIC_NEURAL_WEB,
            HomeMimicItem.SUPERNOVA_NEBULA_CORE,
            HomeMimicItem.QUANTUM_ORACLE_SPHERE,
            HomeMimicItem.SUPERCONDUCTING_PLASMA_LATTICE,
            HomeMimicItem.FERROFLUIDIC_MAGNETIC_VORTEX,
            HomeMimicItem.DIMENSIONAL_STARGATE_MONOLITH -> {
                LiquidGlassInteractiveMimicSlot(
                    item = target,
                    streakDays = streakDays,
                    dailyProgress = dailyProgress,
                    isPlayerActive = isPlayerActive,
                    onLongPressToVault = onLongPressToVault,
                    isArabic = isArabic,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassInteractiveMimicSlot(
    item: HomeMimicItem,
    streakDays: Int,
    dailyProgress: Float,
    isPlayerActive: Boolean,
    onLongPressToVault: () -> Unit,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val thawState = rememberMimicThawState(streakDays)
    val basePrimary = Color(item.primaryAccentHex)
    val baseSecondary = Color(item.secondaryAccentHex)
    val primaryColor = androidx.compose.ui.graphics.lerp(MimicGlacialCyan, basePrimary, thawState.thawProgress)
    val secondaryColor = androidx.compose.ui.graphics.lerp(MimicGlacialIce, baseSecondary, thawState.thawProgress)

    val chargeProgress = remember(item) { Animatable(0f) }
    val tapImpulse = remember(item) { Animatable(0f) }
    var tapNormalizedPoint by remember(item) { mutableStateOf(Offset.Zero) }
    var tapBurstActive by remember(item) { mutableStateOf(false) }

    val effectivePlayerActive = isPlayerActive || tapBurstActive

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(210.dp)
                .testTag("mimic_centerpiece_${item.id}")
                .pointerInput(item) {
                    var triggeredDuringPress = false
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val nx = ((tapOffset.x / size.width.coerceAtLeast(1)) * 2f - 1f).coerceIn(-0.85f, 0.85f)
                            val ny = ((tapOffset.y / size.height.coerceAtLeast(1)) * 2f - 1f).coerceIn(-0.85f, 0.85f)
                            tapNormalizedPoint = Offset(nx, ny)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            MediaPlaybackActivityBus.pulseActivity()
                            scope.launch {
                                tapBurstActive = true
                                tapImpulse.snapTo(1f)
                                tapImpulse.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 1200, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
                                )
                                tapBurstActive = false
                            }
                        },
                        onPress = {
                            triggeredDuringPress = false
                            val job = scope.launch {
                                chargeProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                                )
                            }
                            tryAwaitRelease()
                            if (!triggeredDuringPress && chargeProgress.value >= 0.98f) {
                                triggeredDuringPress = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPressToVault()
                            }
                            job.cancel()
                            chargeProgress.snapTo(0f)
                        },
                        onLongPress = {
                            if (!triggeredDuringPress) {
                                triggeredDuringPress = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPressToVault()
                            }
                        }
                    )
                }
        ) {
            // Pure OLED #000000 + Frosted Liquid Glass Circular Cradle
            Box(
                modifier = Modifier
                    .size(172.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                primaryColor.copy(alpha = if (effectivePlayerActive) 0.16f else 0.08f),
                                Color(0xFF040508),
                                Color(0xFF000000)
                            )
                        )
                    )
                    .border(
                        width = 1.25.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.28f),
                                primaryColor.copy(alpha = 0.65f),
                                secondaryColor.copy(alpha = 0.55f),
                                Color.White.copy(alpha = 0.14f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // 1-second Long-Press Charge-Up Vault Ring
            Canvas(modifier = Modifier.size(182.dp)) {
                if (chargeProgress.value > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(primaryColor, Color.White, secondaryColor, primaryColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * chargeProgress.value,
                        useCenter = false,
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Live 60 FPS Mimic Canvas Centerpiece
            HomeMimicLiveCanvasPreview(
                mimicItem = item,
                streakDays = streakDays,
                dailyProgress = dailyProgress,
                isPlayerActive = effectivePlayerActive,
                tapImpulse = tapImpulse.value,
                tapNormalizedPoint = tapNormalizedPoint,
                isCompact = false,
                modifier = Modifier.size(188.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        val statusLabel = when (item) {
            HomeMimicItem.QUANTUM_REACTOR -> if (isArabic) {
                if (effectivePlayerActive) "المفاعل الكمي في حالة نبض نشط ⚡" else "المفاعل الكمي مستقر • اضغط للتفاعل"
            } else {
                if (effectivePlayerActive) "Quantum Core Active Surge ⚡" else "Quantum Core Idle • Tap to Pulse"
            }
            HomeMimicItem.ACOUSTIC_SINGULARITY -> if (isArabic) {
                if (effectivePlayerActive) "جاذبية صوتية متفاعلة مع التشغيل 🕳️" else "أفق الحدث مستقر • اضغط للتموّج"
            } else {
                if (effectivePlayerActive) "Singularity Wave Distortion Active 🕳️" else "Event Horizon Calm • Tap to Ripple"
            }
            HomeMimicItem.HOLOGRAPHIC_CYBER_VINYL -> if (isArabic) {
                if (effectivePlayerActive) "الفينيل الهولوغرافي يدور بسرعة التشغيل 💿" else "دوران هادئ • يتسارع عند التشغيل أو الضغط"
            } else {
                if (effectivePlayerActive) "Cyber-Vinyl Spinning at Playback RPM 💿" else "Idle Glide • Accelerates on Playback/Tap"
            }
            HomeMimicItem.LIQUID_TIME_CRYSTAL -> {
                val pct = (dailyProgress.coerceIn(0f, 1f) * 100).toInt()
                if (isArabic) {
                    "تدفق السائل البلوري: $pct% يومي • $streakDays يوم ⏳"
                } else {
                    "Crystal Flow: $pct% Daily • $streakDays Days ⏳"
                }
            }
            HomeMimicItem.CYBERNETIC_NEURAL_WEB -> if (isArabic) {
                if (effectivePlayerActive) "نبضات عصبية نشطة عبر الشبكة 🧠" else "المصفوفة العصبية جاهزة • المس لإرسال إشارة"
            } else {
                if (effectivePlayerActive) "Synaptic Matrix Firing Signals 🧠" else "Neural Matrix Online • Tap to Fire Impulse"
            }
            HomeMimicItem.SUPERNOVA_NEBULA_CORE -> if (isArabic) {
                if (effectivePlayerActive) "السديم النجمي ينبض بطاقة السوبرنوفا ✨" else "نواة السديم هادئة • اضغط لإشعال التوهج"
            } else {
                if (effectivePlayerActive) "Supernova Nebula Radiating Active Light ✨" else "Nebula Core Breathing • Tap to Ignite"
            }
            HomeMimicItem.QUANTUM_ORACLE_SPHERE -> if (isArabic) {
                if (effectivePlayerActive) "مصفوفة العرّاف الرونية في دوران متزامن 🔮" else "الكرة الزجاجية مستقرة • المس لتحريك الرموز"
            } else {
                if (effectivePlayerActive) "Oracle Rune-Matrix Shifting in Sync 🔮" else "Oracle Sphere Suspended • Tap to Shift Runes"
            }
            HomeMimicItem.SUPERCONDUCTING_PLASMA_LATTICE -> if (isArabic) {
                if (effectivePlayerActive) "قوس البلازما السيبراني فائق التوصيل ⚡" else "حلقات الأوبسيديان متزنة • المس لشحن العقد"
            } else {
                if (effectivePlayerActive) "Superconducting Plasma Arcs Active ⚡" else "Obsidian Lattice Humming • Tap to Surge"
            }
            HomeMimicItem.FERROFLUIDIC_MAGNETIC_VORTEX -> if (isArabic) {
                if (effectivePlayerActive) "المعدن السائل يتفاعل مع إيقاع الوسائط 🧲" else "الدوّامة المغناطيسية مستعدة • المس للتموّج"
            } else {
                if (effectivePlayerActive) "Ferrofluid Vortex Morphing to Rhythm 🧲" else "Magnetic Ferrofluid Calm • Tap to Spike"
            }
            HomeMimicItem.DIMENSIONAL_STARGATE_MONOLITH -> {
                val roadmapPct = ((streakDays / 365f).coerceIn(0f, 1f) * 100).toInt()
                if (isArabic) {
                    "بوابة الأبعاد مفتوحة بنسبة $roadmapPct% • $streakDays/365 يوم 🌌"
                } else {
                    "Stargate Aperture: $roadmapPct% Roadmap • $streakDays/365 Days 🌌"
                }
            }
            else -> if (isArabic) item.displayNameAr else item.displayNameEn
        }

        val thawBadge = formatMimicThawBadge(streakDays, thawState.thawProgress, isArabic)
        Text(
            text = thawBadge,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (thawState.freezeFactor > 0.35f) MimicGlacialIce else primaryColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                color = primaryColor.copy(alpha = 0.92f),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = if (isArabic) "اضغط للتفاعل • اضغط مطولاً (1ث) لفتح Vault" else "Tap to interact • Hold 1s to open Vault",
            style = MaterialTheme.typography.labelSmall.copy(
                color = aura.textSecondary,
                fontSize = 10.sp,
                letterSpacing = 0.25.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
