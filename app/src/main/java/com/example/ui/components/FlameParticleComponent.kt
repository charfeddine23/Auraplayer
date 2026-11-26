package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.aura
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class FlameParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var alpha: Float,
    val color: Color,
    val maxLife: Float,
    var life: Float = 0f
)

@Composable
fun FlameParticleComponent(
    streakDays: Int,
    onLongPressToRoadmap: () -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val thawState = rememberMimicThawState(streakDays)
    // Smooth normalized progression ratio: 0.0 at Day 0, 1.0 at Day 60
    val progression = thawState.thawProgress
    val isFrozen = thawState.isFullyFrozen

    // Dynamic fire intensity variables scaling smoothly across 0..60 consecutive strikes
    val maxParticles = when {
        isFrozen -> 24
        streakDays < 10 -> 45
        streakDays < 30 -> 75
        streakDays < MIMIC_MAX_THAW_STREAK_DAYS -> 100
        else -> 125 // Raging inferno at 60+ consecutive strikes
    }

    val flickerPhase = rememberThawScaledLoopPhase(
        kineticFactor = thawState.kineticFactor,
        isPlayerActive = false,
        tapImpulse = 0f,
        baseCyclesPerSecond = 1.15f
    )
    val flickerWave = 0.5f + 0.5f * kotlin.math.sin(flickerPhase * (2f * kotlin.math.PI.toFloat()))
    val flameScale = if (isFrozen) {
        0.96f
    } else {
        val minScale = 0.95f + progression * 0.05f
        val maxScale = 1.08f + progression * 0.22f
        minScale + (maxScale - minScale) * flickerWave
    }
    val glowAlpha = if (isFrozen) {
        0.55f
    } else {
        val minGlow = 0.55f + progression * 0.25f
        minGlow + (1.0f - minGlow) * flickerWave
    }

    // 1-second long-press charge up ring animation
    val chargeProgress = remember { Animatable(0f) }
    var isPressing by remember { mutableStateOf(false) }

    // Rising particle sparks
    val particles = remember { mutableStateListOf<FlameParticle>() }

    // Particle color palettes transitioning from frozen blue/cyan -> orange/red -> ultra hot blue/violet core & solar gold
    val activeParticleColors = remember(streakDays) {
        when {
            streakDays == 0 -> listOf(
                Color(0xFF80DEEA), // Ice Cyan
                Color(0xFFE0F7FA), // Glacial White
                Color(0xFF00E5FF), // Electric Frost
                Color(0xFFB2EBF2), // Pale Frost
                Color(0xFF4DD0E1)  // Crystalline Cyan
            )
            streakDays < 10 -> listOf(
                Color(0xFFFFB74D), // Gentle Amber
                Color(0xFFFF9800), // Soft Orange
                Color(0xFFFF7043), // Ember Orange
                Color(0xFF80DEEA), // Lingering thawed frost spark
                Color(0xFFFFEB3B)  // Gold Spark
            )
            streakDays < 30 -> listOf(
                Color(0xFFFF3D00), // Hot Orange/Red
                Color(0xFFFF9100), // Vivid Orange
                Color(0xFFFF2D73), // Hot Neon Pink
                Color(0xFFFFEA00), // Luminous Gold
                Color(0xFFFF6D00)  // Deep Amber
            )
            streakDays < MIMIC_MAX_THAW_STREAK_DAYS -> listOf(
                Color(0xFFFF1744), // Scorching Crimson
                Color(0xFFFF5252), // Blaze Red
                Color(0xFFFF9100), // Electric Orange
                Color(0xFFFFEA00), // Solar Gold
                Color(0xFFFF007F)  // Ultra Neon Magenta
            )
            else -> listOf( // Raging Cosmic Inferno (60+ consecutive strikes)
                Color(0xFFFF0055), // Supernova Crimson
                Color(0xFFFF7700), // Blazing Solar Fire
                Color(0xFFFFEE00), // White-Hot Flare
                Color(0xFF7928CA), // Ultra Hot Core Violet
                Color(0xFF00F2FE), // Plasma Cyan Edge
                Color(0xFFFFFFFF)  // Pure White Hot Sparks
            )
        }
    }

    val kineticState by rememberUpdatedState(thawState.kineticFactor)

    // Particle update simulation loop whose speed is controlled smoothly by freeze/thaw kineticFactor
    LaunchedEffect(streakDays) {
        val random = Random(System.currentTimeMillis())
        while (isActive) {
            delay(16) // ~60fps particle updates
            val k = kineticState

            if (k > 0.01f) {
                // Emit sparks from the base of the flame with higher velocity & spread as fire thaws
                val emitCount = if (progression > 0.6f) 3 else if (progression > 0.15f) 2 else 1
                if (particles.size < maxParticles) {
                    repeat(emitCount) {
                        val spread = 80f + progression * 70f
                        val upwardSpeed = (1.6f + progression * 5.2f)
                        particles.add(
                            FlameParticle(
                                x = random.nextFloat() * spread - (spread / 2f),
                                y = 20f + random.nextFloat() * 20f,
                                vx = (random.nextFloat() - 0.5f) * (1.4f + progression * 2.4f),
                                vy = -upwardSpeed - random.nextFloat() * (2.5f + progression * 4.0f),
                                radius = (3.5f + random.nextFloat() * 4.5f) * (1f + progression * 0.65f),
                                alpha = 0.95f,
                                color = activeParticleColors.random(random),
                                maxLife = 35f + random.nextFloat() * (25f + progression * 20f)
                            )
                        )
                    }
                }

                // Update existing particles scaled smoothly by k (0 when frozen -> 1 at Day 365)
                val speedMul = 0.18f + 0.82f * k
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.x += p.vx * speedMul
                    p.y += p.vy * speedMul
                    p.life += speedMul
                    p.alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
                    p.radius = (p.radius * (1f - 0.03f * speedMul)).coerceAtLeast(1.2f)

                    if (p.life >= p.maxLife || p.alpha <= 0.05f) {
                        iterator.remove()
                    }
                }
            } else if (particles.isNotEmpty()) {
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.alpha = (p.alpha - 0.04f).coerceAtLeast(0f)
                    if (p.alpha <= 0.04f) {
                        iterator.remove()
                    }
                }
            }
        }
    }

    // Central Flame Tint & Aura Gradient Interpolation
    val auraRadiusScale = 0.75f + (progression * 0.55f)
    val flameIconSize = (76.dp + (progression * 24).dp)

    val targetWarmTint = when {
        progression < 0.25f -> androidx.compose.ui.graphics.lerp(
            if (aura.isDark) Color(0xFFFFB74D) else Color(0xFFD97706),
            if (aura.isDark) Color(0xFFFF6D00) else Color(0xFFEA580C),
            progression / 0.25f
        )
        progression < 0.65f -> androidx.compose.ui.graphics.lerp(
            if (aura.isDark) Color(0xFFFF6D00) else Color(0xFFEA580C),
            if (aura.isDark) Color(0xFFFF3D00) else Color(0xFFDC2626),
            (progression - 0.25f) / 0.40f
        )
        else -> androidx.compose.ui.graphics.lerp(
            if (aura.isDark) Color(0xFFFF3D00) else Color(0xFFDC2626),
            if (aura.isDark) Color(0xFFFF1744) else Color(0xFFB91C1C),
            (progression - 0.65f) / 0.35f
        )
    }
    val flamePrimaryTint = androidx.compose.ui.graphics.lerp(
        if (aura.isDark) MimicGlacialIce else Color(0xFF0284C7),
        targetWarmTint,
        progression
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .testTag("flame_interactive_component")
                .pointerInput(Unit) {
                    var triggeredDuringPress = false
                    detectTapGestures(
                        onPress = {
                            isPressing = true
                            triggeredDuringPress = false
                            val job = scope.launch {
                                chargeProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                                )
                            }
                            tryAwaitRelease()
                            isPressing = false
                            if (!triggeredDuringPress && chargeProgress.value >= 0.98f) {
                                triggeredDuringPress = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPressToRoadmap()
                            }
                            job.cancel()
                            chargeProgress.snapTo(0f)
                        },
                        onLongPress = {
                            if (!triggeredDuringPress) {
                                triggeredDuringPress = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPressToRoadmap()
                            }
                        }
                    )
                }
        ) {
            // Particle Canvas Layer (Rising Glowing Sparks)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                particles.forEach { particle ->
                    drawCircle(
                        color = particle.color.copy(alpha = particle.alpha),
                        radius = particle.radius,
                        center = Offset(centerX + particle.x, centerY + particle.y)
                    )
                }
            }

            // Outer Ambient Glowing Pulsing Aura (Scales smoothly with level)
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(flameScale)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = when {
                                    isFrozen -> listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.45f * glowAlpha),
                                        Color(0xFF80DEEA).copy(alpha = 0.25f * glowAlpha),
                                        Color.Transparent
                                    )
                                    streakDays < 10 -> listOf(
                                        Color(0xFFFFB74D).copy(alpha = 0.45f * glowAlpha),
                                        Color(0xFF80DEEA).copy(alpha = 0.20f * (1f - progression * 3f).coerceAtLeast(0f)), // Thawing ice edge
                                        Color(0xFFFF9800).copy(alpha = 0.25f * glowAlpha),
                                        Color.Transparent
                                    )
                                    streakDays < 30 -> listOf(
                                        Color(0xFFFF5722).copy(alpha = 0.55f * glowAlpha),
                                        Color(0xFFFF2D73).copy(alpha = 0.40f * glowAlpha),
                                        Color(0xFFFF9100).copy(alpha = 0.25f * glowAlpha),
                                        Color.Transparent
                                    )
                                    streakDays < MIMIC_MAX_THAW_STREAK_DAYS -> listOf(
                                        Color(0xFFFF1744).copy(alpha = 0.65f * glowAlpha),
                                        Color(0xFFFF9100).copy(alpha = 0.45f * glowAlpha),
                                        Color(0xFFFF2D73).copy(alpha = 0.35f * glowAlpha),
                                        Color.Transparent
                                    )
                                    else -> listOf( // Max Raging Fire at 60+ consecutive strikes
                                        Color(0xFFFF0055).copy(alpha = 0.75f * glowAlpha),
                                        Color(0xFFFF9100).copy(alpha = 0.55f * glowAlpha),
                                        Color(0xFF7928CA).copy(alpha = 0.45f * glowAlpha),
                                        Color(0xFFFFEA00).copy(alpha = 0.30f * glowAlpha),
                                        Color.Transparent
                                    )
                                },
                                radius = size.width * auraRadiusScale
                            )
                        )
                    }
            )

            // 1-Second Long-Press Charge-Up Ring
            Canvas(modifier = Modifier.size(160.dp)) {
                if (chargeProgress.value > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            when {
                                isFrozen -> listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF80DEEA),
                                    Color(0xFFE0F7FA),
                                    Color(0xFF00E5FF)
                                )
                                streakDays < 30 -> listOf(
                                    Color(0xFFFF9800),
                                    Color(0xFFFFB74D),
                                    Color(0xFF00F2FE),
                                    Color(0xFFFF9800)
                                )
                                else -> listOf(
                                    Color(0xFFFF1744),
                                    Color(0xFFFF9100),
                                    Color(0xFFFFEA00),
                                    Color(0xFFFF007F),
                                    Color(0xFFFF1744)
                                )
                            }
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * chargeProgress.value,
                        useCenter = false,
                        style = Stroke(width = (4.dp + (progression * 2).dp).toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Liquid Glass Inner Card Base for the Flame
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            if (aura.isDark) {
                                when {
                                    isFrozen -> listOf(Color(0x3300E5FF), Color(0x1A081324), Color(0x800B0F19))
                                    streakDays < 20 -> listOf(Color(0x33FF9800), Color(0x1A121826), Color(0x800B0F19))
                                    else -> listOf(Color(0x4DFF3D00), Color(0x2B1E1026), Color(0x900B0F19))
                                }
                            } else {
                                when {
                                    isFrozen -> listOf(Color(0x3838BDF8), Color(0xFFE0F2FE), Color(0xFFFFFFFF))
                                    streakDays < 20 -> listOf(Color(0x38F59E0B), Color(0xFFFFFBEB), Color(0xFFFFFFFF))
                                    else -> listOf(Color(0x38EA580C), Color(0xFFFFF7ED), Color(0xFFFFFFFF))
                                }
                            }
                        )
                    )
                    .border(
                        width = (1.dp + (progression * 1.5).dp),
                        brush = Brush.linearGradient(
                            if (isFrozen) {
                                listOf(aura.accentCyan.copy(alpha = 0.55f), Color(0x3380DEEA))
                            } else {
                                listOf(flamePrimaryTint.copy(alpha = 0.8f), Color(0x44FFA000))
                            }
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Central Glowing Animated Flame Icon
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak Flame - Long press 1s for Roadmap",
                    modifier = Modifier
                        .size(flameIconSize)
                        .scale(flameScale)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    when {
                                        isFrozen -> listOf(
                                            Color(0xFFE0F7FA).copy(alpha = 0.85f),
                                            Color(0xFF00E5FF).copy(alpha = 0.5f),
                                            Color.Transparent
                                        )
                                        streakDays < 10 -> listOf(
                                            Color(0xFFFFECB3).copy(alpha = 0.85f),
                                            Color(0xFFFF9800).copy(alpha = 0.5f),
                                            Color.Transparent
                                        )
                                        streakDays < 60 -> listOf(
                                            Color(0xFFFFD54F).copy(alpha = 0.85f),
                                            Color(0xFFFF3D00).copy(alpha = 0.55f),
                                            Color.Transparent
                                        )
                                        else -> listOf(
                                            Color(0xFFFFFFFF).copy(alpha = 0.95f),
                                            Color(0xFFFFEA00).copy(alpha = 0.80f),
                                            Color(0xFFFF1744).copy(alpha = 0.65f),
                                            Color.Transparent
                                        )
                                    },
                                    radius = size.width * (0.6f + progression * 0.25f)
                                )
                            )
                        },
                    tint = flamePrimaryTint
                )

                // Smooth 0 -> 60 crystalline ice encasement melting away day by day
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawSmoothMimicIceOverlay(
                        freezeFactor = thawState.freezeFactor,
                        shimmerPhase = glowAlpha,
                        isCompact = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Instructional hint label reflecting smooth 0..60 freeze/thaw progress
        val thawBadge = formatMimicThawBadge(streakDays, progression, isArabic)
        val statusTextLabel = if (isArabic) {
            when {
                streakDays <= 0 -> "السلسلة مجمدة 0% • شاهد لإذابة الجليد"
                streakDays < MIMIC_MAX_THAW_STREAK_DAYS -> "$thawBadge • الشعلة تتوهج 🔥"
                else -> "ذوبان كامل 100% 🔥 • $MIMIC_MAX_THAW_STREAK_DAYS / $MIMIC_MAX_THAW_STREAK_DAYS"
            }
        } else {
            when {
                streakDays <= 0 -> "Streak Frozen 0% • Watch to melt the ice"
                streakDays < MIMIC_MAX_THAW_STREAK_DAYS -> "$thawBadge • Flame awakening 🔥"
                else -> "Mythic Inferno 100% 🔥 • $MIMIC_MAX_THAW_STREAK_DAYS / $MIMIC_MAX_THAW_STREAK_DAYS"
            }
        }

        Text(
            text = statusTextLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                color = flamePrimaryTint,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = if (isArabic) "اضغط مطولاً (1 ثانية) لفتح صفحة Vault" else "Hold 1s on flame to open Vault",
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isFrozen && aura.isDark) Color(0xFFB2EBF2) else aura.accentCyan,
                fontSize = 10.sp,
                letterSpacing = 0.3.sp
            )
        )
    }
}
