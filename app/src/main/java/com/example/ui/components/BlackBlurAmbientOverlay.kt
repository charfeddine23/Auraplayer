package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.example.data.local.AmbientBlackBlurMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Strict Luxury-Minimalism Obsidian & Pure OLED Black Tokens
private val PureOledBlack = Color(0xFF000000)
private val Obsidian04 = Color(0xFF040406)
private val Obsidian07 = Color(0xFF07070A)
private val Charcoal0D = Color(0xFF0D0D11)
private val WarmCharcoal = Color(0xFF171311)
private val DeepSlateNoir = Color(0xFF12151C)
private val DeepVelvetMask = Color(0xFF09080C)

data class CinemaEdgeSample(
    val topEdge: Color = Color(0xFF1A1E24),
    val bottomEdge: Color = Color(0xFF16191F),
    val leftEdge: Color = Color(0xFF181B21),
    val rightEdge: Color = Color(0xFF171A20),
    val tenPercentReflection: Color = Color(0xFF181A1E),
    val warmMidnightEdge: Color = Color(0xFF1F1A17),
    val monochromeEdge: Color = Color(0xFF1C1D1F),
    val averageLuminance: Float = 0.22f
)

/**
 * Compresses video frame colors into low-saturation (10%–16%), cinema-safe dark tints
 * so Blur & Fade backdrops reflect subtle scene mood with zero bright neon or glare.
 */
fun desaturateToCinemaTint(
    argb: Int,
    saturationKeep: Float = 0.14f,
    maxValue: Float = 0.30f
): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(argb, hsl)
    hsl[1] = (hsl[1] * saturationKeep).coerceIn(0.01f, 0.16f)
    hsl[2] = (hsl[2] * 0.46f + 0.05f).coerceIn(0.05f, maxValue)
    return Color(ColorUtils.HSLToColor(hsl))
}

private fun dampChannelColor(previous: Color, next: Color, factor: Float, maxChannelDelta: Float = 0.045f): Color {
    fun stepChannel(prev: Float, target: Float): Float {
        val desired = (target - prev) * factor
        val clamped = desired.coerceIn(-maxChannelDelta, maxChannelDelta)
        return (prev + clamped).coerceIn(0f, 1f)
    }
    return Color(
        red = stepChannel(previous.red, next.red),
        green = stepChannel(previous.green, next.green),
        blue = stepChannel(previous.blue, next.blue),
        alpha = 1f
    )
}

/**
 * Heavily damped temporal smoothing + slew-rate flash limiter so rapid video cuts
 * or sudden bright frames never cause jarring flashes in the ambient backdrop.
 */
fun dampCinemaEdgeSample(
    previous: CinemaEdgeSample,
    next: CinemaEdgeSample,
    factor: Float = 0.16f
): CinemaEdgeSample {
    val t = factor.coerceIn(0.05f, 0.35f)
    val lumaDelta = ((next.averageLuminance - previous.averageLuminance) * t).coerceIn(-0.035f, 0.035f)
    return CinemaEdgeSample(
        topEdge = dampChannelColor(previous.topEdge, next.topEdge, t),
        bottomEdge = dampChannelColor(previous.bottomEdge, next.bottomEdge, t),
        leftEdge = dampChannelColor(previous.leftEdge, next.leftEdge, t),
        rightEdge = dampChannelColor(previous.rightEdge, next.rightEdge, t),
        tenPercentReflection = dampChannelColor(previous.tenPercentReflection, next.tenPercentReflection, t, 0.03f),
        warmMidnightEdge = dampChannelColor(previous.warmMidnightEdge, next.warmMidnightEdge, t, 0.035f),
        monochromeEdge = dampChannelColor(previous.monochromeEdge, next.monochromeEdge, t, 0.035f),
        averageLuminance = (previous.averageLuminance + lumaDelta).coerceIn(0.05f, 0.60f)
    )
}

fun extractCinemaEdgeSample(bitmap: Bitmap): CinemaEdgeSample {
    val w = bitmap.width.coerceAtLeast(1)
    val h = bitmap.height.coerceAtLeast(1)

    fun avgBand(xs: IntRange, ys: IntRange): Pair<Int, Float> {
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var count = 0
        for (y in ys) {
            val cy = y.coerceIn(0, h - 1)
            for (x in xs) {
                val cx = x.coerceIn(0, w - 1)
                val px = bitmap.getPixel(cx, cy)
                rSum += android.graphics.Color.red(px)
                gSum += android.graphics.Color.green(px)
                bSum += android.graphics.Color.blue(px)
                count++
            }
        }
        if (count == 0) return android.graphics.Color.rgb(20, 22, 26) to 0.2f
        val r = (rSum / count).toInt()
        val g = (gSum / count).toInt()
        val b = (bSum / count).toInt()
        val luma = (0.2126f * r + 0.7152f * g + 0.0722f * b) / 255f
        return android.graphics.Color.rgb(r, g, b) to luma
    }

    val (topRgb, topLuma) = avgBand(2 until (w - 2).coerceAtLeast(3), 0..2)
    val (botRgb, botLuma) = avgBand(2 until (w - 2).coerceAtLeast(3), (h - 3).coerceAtLeast(0) until h)
    val (leftRgb, leftLuma) = avgBand(0..2, 2 until (h - 2).coerceAtLeast(3))
    val (rightRgb, rightLuma) = avgBand((w - 3).coerceAtLeast(0) until w, 2 until (h - 2).coerceAtLeast(3))
    val (centerRgb, centerLuma) = avgBand((w / 4)..(w * 3 / 4), (h / 4)..(h * 3 / 4))

    val topTint = desaturateToCinemaTint(topRgb)
    val bottomTint = desaturateToCinemaTint(botRgb)
    val leftTint = desaturateToCinemaTint(leftRgb)
    val rightTint = desaturateToCinemaTint(rightRgb)

    // Strict 10% desaturated color reflection for Smoked Crystal Diffusion
    val avgRgb = ColorUtils.blendARGB(
        ColorUtils.blendARGB(topRgb, botRgb, 0.5f),
        ColorUtils.blendARGB(centerRgb, ColorUtils.blendARGB(leftRgb, rightRgb, 0.5f), 0.5f),
        0.45f
    )
    val tenPercent = desaturateToCinemaTint(avgRgb, saturationKeep = 0.10f, maxValue = 0.24f)

    // Warm muted charcoal halo for late-night Dimmed Charcoal Halo viewing
    val warmArgb = ColorUtils.blendARGB(avgRgb, android.graphics.Color.rgb(44, 33, 25), 0.74f)
    val warmMidnight = desaturateToCinemaTint(warmArgb, saturationKeep = 0.14f, maxValue = 0.27f)

    val meanLuma = ((topLuma + botLuma + leftLuma + rightLuma + centerLuma) / 5f).coerceIn(0.06f, 0.60f)
    val monoChannel = (0.07f + meanLuma * 0.21f).coerceIn(0.07f, 0.26f)
    val monoTint = Color(monoChannel, monoChannel, monoChannel, 1f)

    return CinemaEdgeSample(
        topEdge = topTint,
        bottomEdge = bottomTint,
        leftEdge = leftTint,
        rightEdge = rightTint,
        tenPercentReflection = tenPercent,
        warmMidnightEdge = warmMidnight,
        monochromeEdge = monoTint,
        averageLuminance = meanLuma
    )
}

/**
 * Hardware-accelerated 60 FPS Luxury "Blur & Fade Ambient Glow" Engine.
 * Combines GPU Modifier.blur (RenderEffect on API 31+) with multi-layered radial/linear
 * Brush fade masks blending invisibly into pure OLED #000000 with heavy temporal damping.
 */
@Composable
fun BlackBlurAmbientBackdrop(
    isEnabled: Boolean,
    mode: AmbientBlackBlurMode,
    edgeSample: CinemaEdgeSample,
    isCroppedFullScreen: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isEnabled || isCroppedFullScreen) {
        Box(modifier = modifier.fillMaxSize().background(PureOledBlack))
        return
    }

    val driftTransition = rememberInfiniteTransition(label = "luxury_blur_drift")
    val driftPhaseState = driftTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_phase"
    )

    val temporalEasing = remember { CubicBezierEasing(0.22f, 1f, 0.36f, 1f) }
    val temporalColorDuration = when (mode) {
        AmbientBlackBlurMode.RADIAL_VIGNETTE_DISSOLVE -> 4200 // 07 Dark Ink Ripple Blur (ultra-slow)
        AmbientBlackBlurMode.OLED_MIDNIGHT_DIFFUSION -> 3800 // 04 Still Arctic Mist
        AmbientBlackBlurMode.VELVET_SHADOW_MELT -> 3600      // 09 Anamorphic Bokeh Fade
        else -> 3400
    }
    val topAnimState = animateColorAsState(
        targetValue = edgeSample.topEdge,
        animationSpec = tween(durationMillis = temporalColorDuration, easing = temporalEasing),
        label = "bb_top_anim"
    )
    val bottomAnimState = animateColorAsState(
        targetValue = edgeSample.bottomEdge,
        animationSpec = tween(durationMillis = temporalColorDuration, easing = temporalEasing),
        label = "bb_bottom_anim"
    )
    val leftAnimState = animateColorAsState(
        targetValue = edgeSample.leftEdge,
        animationSpec = tween(durationMillis = temporalColorDuration, easing = temporalEasing),
        label = "bb_left_anim"
    )
    val rightAnimState = animateColorAsState(
        targetValue = edgeSample.rightEdge,
        animationSpec = tween(durationMillis = temporalColorDuration, easing = temporalEasing),
        label = "bb_right_anim"
    )
    val reflectionAnimState = animateColorAsState(
        targetValue = edgeSample.tenPercentReflection,
        animationSpec = tween(durationMillis = 3600, easing = temporalEasing),
        label = "bb_reflection_anim"
    )
    val warmAnimState = animateColorAsState(
        targetValue = edgeSample.warmMidnightEdge,
        animationSpec = tween(durationMillis = 3800, easing = temporalEasing),
        label = "bb_warm_anim"
    )
    val monoAnimState = animateColorAsState(
        targetValue = edgeSample.monochromeEdge,
        animationSpec = tween(durationMillis = 3800, easing = temporalEasing),
        label = "bb_mono_anim"
    )
    val lumaAnimState = animateFloatAsState(
        targetValue = edgeSample.averageLuminance,
        animationSpec = tween(durationMillis = 4000, easing = temporalEasing),
        label = "bb_luma_anim"
    )

    val baseBackground = when (mode) {
        AmbientBlackBlurMode.DEEP_OBSIDIAN_BLUR -> PureOledBlack         // 01 Silent Obsidian Eclipse
        AmbientBlackBlurMode.CINEMA_MATTE_VIGNETTE -> Obsidian04         // 02 Smoked Crystal Diffusion
        AmbientBlackBlurMode.SMOKED_LIQUID_GLASS -> PureOledBlack        // 03 Matte Theater Curtain Fade
        AmbientBlackBlurMode.OLED_MIDNIGHT_DIFFUSION -> PureOledBlack    // 04 Still Arctic Mist
        AmbientBlackBlurMode.VELVET_SHADOW_MIST -> PureOledBlack         // 05 Deep Velvet Melt
        AmbientBlackBlurMode.EDGE_FADE_GAUSSIAN_BLUR -> PureOledBlack    // 06 Midnight Horizon Gradient
        AmbientBlackBlurMode.RADIAL_VIGNETTE_DISSOLVE -> Obsidian04      // 07 Dark Ink Ripple Blur
        AmbientBlackBlurMode.CINEMATIC_SMOKED_BLUR -> DeepSlateNoir      // 08 Frosted Noir Marble
        AmbientBlackBlurMode.VELVET_SHADOW_MELT -> PureOledBlack         // 09 Anamorphic Bokeh Fade
        AmbientBlackBlurMode.MULTI_LAYER_LAYERED_FADE -> PureOledBlack   // 10 Dimmed Charcoal Halo
    }

    val blurRadius = when (mode) {
        AmbientBlackBlurMode.DEEP_OBSIDIAN_BLUR -> 104.dp       // 01 Silent Obsidian Eclipse
        AmbientBlackBlurMode.CINEMA_MATTE_VIGNETTE -> 116.dp    // 02 Smoked Crystal Diffusion
        AmbientBlackBlurMode.SMOKED_LIQUID_GLASS -> 98.dp       // 03 Matte Theater Curtain Fade
        AmbientBlackBlurMode.OLED_MIDNIGHT_DIFFUSION -> 108.dp  // 04 Still Arctic Mist
        AmbientBlackBlurMode.VELVET_SHADOW_MIST -> 120.dp       // 05 Deep Velvet Melt
        AmbientBlackBlurMode.EDGE_FADE_GAUSSIAN_BLUR -> 102.dp  // 06 Midnight Horizon Gradient
        AmbientBlackBlurMode.RADIAL_VIGNETTE_DISSOLVE -> 114.dp // 07 Dark Ink Ripple Blur
        AmbientBlackBlurMode.CINEMATIC_SMOKED_BLUR -> 110.dp    // 08 Frosted Noir Marble
        AmbientBlackBlurMode.VELVET_SHADOW_MELT -> 126.dp       // 09 Anamorphic Bokeh Fade
        AmbientBlackBlurMode.MULTI_LAYER_LAYERED_FADE -> 114.dp // 10 Dimmed Charcoal Halo
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBackground)
    ) {
        // Stage 1 GPU Hardware-Accelerated Unbounded RenderEffect Gaussian Blur (0 recompositions)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        ) {
            val w = size.width
            val h = size.height
            val maxDim = size.maxDimension
            val minDim = size.minDimension
            val topAnim = topAnimState.value
            val bottomAnim = bottomAnimState.value
            val leftAnim = leftAnimState.value
            val rightAnim = rightAnimState.value
            val reflectionAnim = reflectionAnimState.value
            val warmAnim = warmAnimState.value
            val monoAnim = monoAnimState.value
            val lumaAnim = lumaAnimState.value
            val driftPhase = driftPhaseState.value

            when (mode) {
                AmbientBlackBlurMode.DEEP_OBSIDIAN_BLUR -> {
                    // 01 Silent Obsidian Eclipse: Circular dark eclipse halo absorbing bright edges into OLED black
                    val avgHalo = lerp(lerp(topAnim, bottomAnim, 0.5f), lerp(leftAnim, rightAnim, 0.5f), 0.5f)
                    val ringAlpha = (0.14f + lumaAnim * 0.10f).coerceIn(0.14f, 0.22f)
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to PureOledBlack,
                                0.24f to Obsidian04,
                                0.42f to avgHalo.copy(alpha = ringAlpha),
                                0.70f to Obsidian07.copy(alpha = 0.80f),
                                1.0f to PureOledBlack
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = maxDim * 0.58f
                        )
                    )
                }

                AmbientBlackBlurMode.CINEMA_MATTE_VIGNETTE -> {
                    // 02 Smoked Crystal Diffusion: Ultra-wide frosted smoked-glass blur with 10% desaturated reflection
                    val dx = cos(driftPhase * 0.5f) * w * 0.035f
                    drawRect(
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                0.0f to reflectionAnim.copy(alpha = 0.10f),
                                0.48f to Charcoal0D.copy(alpha = 0.72f),
                                1.0f to reflectionAnim.copy(alpha = 0.10f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                reflectionAnim.copy(alpha = 0.10f),
                                Charcoal0D.copy(alpha = 0.36f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f + dx, h * 0.5f),
                            radius = maxDim * 0.72f
                        )
                    )
                }

                AmbientBlackBlurMode.SMOKED_LIQUID_GLASS -> {
                    // 03 Matte Theater Curtain Fade: Horizontal-focused shadow blur mimicking velvet theater curtains on left/right borders
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.0f to PureOledBlack,
                                0.12f to leftAnim.copy(alpha = 0.17f),
                                0.30f to DeepVelvetMask.copy(alpha = 0.78f),
                                0.50f to PureOledBlack.copy(alpha = 0.92f),
                                0.70f to DeepVelvetMask.copy(alpha = 0.78f),
                                0.88f to rightAnim.copy(alpha = 0.17f),
                                1.0f to PureOledBlack
                            )
                        )
                    )
                }

                AmbientBlackBlurMode.OLED_MIDNIGHT_DIFFUSION -> {
                    // 04 Still Arctic Mist: Luminance-only (monochrome shadow/light) cloud-like soft mist diffusion
                    val mistAlpha = (0.10f + lumaAnim * 0.09f).coerceIn(0.10f, 0.19f)
                    val breatheY = sin(driftPhase * 0.5f) * h * 0.035f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to monoAnim.copy(alpha = mistAlpha),
                                0.46f to monoAnim.copy(alpha = mistAlpha * 0.50f),
                                0.84f to Obsidian04.copy(alpha = 0.86f),
                                1.0f to PureOledBlack
                            ),
                            center = Offset(w * 0.40f, h * 0.44f + breatheY),
                            radius = minDim * 0.94f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to monoAnim.copy(alpha = mistAlpha * 0.82f),
                                0.56f to Obsidian07.copy(alpha = 0.52f),
                                1.0f to PureOledBlack
                            ),
                            center = Offset(w * 0.62f, h * 0.58f - breatheY),
                            radius = minDim * 0.90f
                        )
                    )
                }

                AmbientBlackBlurMode.VELVET_SHADOW_MIST -> {
                    // 05 Deep Velvet Melt: High-density edge blur dissolving borders completely behind dark velvet mask
                    val velvetBlend = lerp(lerp(topAnim, bottomAnim, 0.5f), lerp(leftAnim, rightAnim, 0.5f), 0.5f)
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to velvetBlend.copy(alpha = 0.15f),
                                0.34f to DeepVelvetMask.copy(alpha = 0.68f),
                                0.70f to Obsidian04.copy(alpha = 0.92f),
                                1.0f to PureOledBlack
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = maxDim * 0.72f
                        )
                    )
                }

                AmbientBlackBlurMode.EDGE_FADE_GAUSSIAN_BLUR -> {
                    // 06 Midnight Horizon Gradient: Vertical-weighted soft blur fading smoothly outward from top and bottom edges
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to PureOledBlack,
                                0.15f to topAnim.copy(alpha = 0.18f),
                                0.42f to Obsidian04.copy(alpha = 0.64f),
                                0.58f to Obsidian04.copy(alpha = 0.64f),
                                0.85f to bottomAnim.copy(alpha = 0.18f),
                                1.0f to PureOledBlack
                            )
                        )
                    )
                }

                AmbientBlackBlurMode.RADIAL_VIGNETTE_DISSOLVE -> {
                    // 07 Dark Ink Ripple Blur: Ultra-slow fluid diffusion resembling dark ink dispersing smoothly in water
                    val inkDx = cos(driftPhase) * w * 0.065f
                    val inkDy = sin(driftPhase) * h * 0.055f
                    val inkCore = lerp(topAnim, leftAnim, 0.5f)
                    val inkSecondary = lerp(bottomAnim, rightAnim, 0.5f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to inkCore.copy(alpha = 0.16f),
                                0.42f to Charcoal0D.copy(alpha = 0.58f),
                                0.82f to Obsidian04.copy(alpha = 0.88f),
                                1.0f to PureOledBlack
                            ),
                            center = Offset(w * 0.42f + inkDx, h * 0.44f + inkDy),
                            radius = minDim * 0.96f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to inkSecondary.copy(alpha = 0.14f),
                                0.48f to Obsidian07.copy(alpha = 0.54f),
                                1.0f to Color.Transparent
                            ),
                            center = Offset(w * 0.58f - inkDx, h * 0.56f - inkDy),
                            radius = minDim * 0.90f
                        )
                    )
                }

                AmbientBlackBlurMode.CINEMATIC_SMOKED_BLUR -> {
                    // 08 Frosted Noir Marble: Multi-toned charcoal and deep slate frosted blur with zero eye strain
                    drawRect(
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                0.0f to Charcoal0D,
                                0.28f to lerp(topAnim, DeepSlateNoir, 0.70f).copy(alpha = 0.42f),
                                0.52f to lerp(leftAnim, Charcoal0D, 0.76f).copy(alpha = 0.36f),
                                0.78f to lerp(bottomAnim, DeepSlateNoir, 0.70f).copy(alpha = 0.40f),
                                1.0f to PureOledBlack
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        )
                    )
                }

                AmbientBlackBlurMode.VELVET_SHADOW_MELT -> {
                    // 09 Anamorphic Bokeh Fade: Soft out-of-focus cinematic lens blur turning edge highlights into faint fading orbs
                    val swayX = sin(driftPhase * 0.5f) * w * 0.035f
                    val swayY = cos(driftPhase * 0.5f) * h * 0.025f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(topAnim.copy(alpha = 0.14f), Obsidian07.copy(alpha = 0.28f), Color.Transparent),
                            center = Offset(w * 0.22f + swayX, h * 0.26f + swayY),
                            radius = minDim * 0.46f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(rightAnim.copy(alpha = 0.12f), Obsidian07.copy(alpha = 0.24f), Color.Transparent),
                            center = Offset(w * 0.78f - swayX, h * 0.32f - swayY),
                            radius = minDim * 0.42f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(leftAnim.copy(alpha = 0.11f), Color.Transparent),
                            center = Offset(w * 0.28f - swayX, h * 0.74f - swayY),
                            radius = minDim * 0.44f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(bottomAnim.copy(alpha = 0.13f), Color.Transparent),
                            center = Offset(w * 0.74f + swayX, h * 0.76f + swayY),
                            radius = minDim * 0.48f
                        )
                    )
                }

                AmbientBlackBlurMode.MULTI_LAYER_LAYERED_FADE -> {
                    // 10 Dimmed Charcoal Halo (Stage 1): Warm-tinted muted charcoal base
                    val nightAlpha = (0.11f + lumaAnim * 0.08f).coerceIn(0.11f, 0.19f)
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to warmAnim.copy(alpha = nightAlpha),
                                0.44f to WarmCharcoal.copy(alpha = 0.34f),
                                0.80f to Obsidian04.copy(alpha = 0.88f),
                                1.0f to PureOledBlack
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = maxDim * 0.64f
                        )
                    )
                }
            }
        }

        // Stage 2 Secondary Unbounded Diffusion Blur Layer for Dimmed Charcoal Halo (wide warm night fade)
        if (mode == AmbientBlackBlurMode.MULTI_LAYER_LAYERED_FADE) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 62.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            ) {
                val w = size.width
                val h = size.height
                val warmAnim = warmAnimState.value
                drawRect(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to warmAnim.copy(alpha = 0.12f),
                            0.55f to WarmCharcoal.copy(alpha = 0.22f),
                            1.0f to PureOledBlack
                        ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = size.maxDimension * 0.56f
                    )
                )
            }
        }

        // Smooth Alpha Fade Mask blending borders seamlessly into Pure OLED #000000 (zero sharp seams)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            when (mode) {
                AmbientBlackBlurMode.DEEP_OBSIDIAN_BLUR -> {
                    // Silent Obsidian Eclipse radial mask
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to PureOledBlack.copy(alpha = 0.50f),
                                0.46f to Color(0x55040406),
                                0.82f to Obsidian04.copy(alpha = 0.94f),
                                1.0f to PureOledBlack
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = size.maxDimension * 0.64f
                        )
                    )
                }

                AmbientBlackBlurMode.CINEMA_MATTE_VIGNETTE -> {
                    // Smoked Crystal Diffusion frosted edge fade
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to PureOledBlack,
                                0.22f to Charcoal0D.copy(alpha = 0.45f),
                                0.50f to Color(0x330B0C10),
                                0.78f to Charcoal0D.copy(alpha = 0.45f),
                                1.0f to PureOledBlack
                            )
                        )
                    )
                }

                AmbientBlackBlurMode.SMOKED_LIQUID_GLASS -> {
                    // Matte Theater Curtain left/right velvet fade
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.0f to PureOledBlack,
                                0.14f to DeepVelvetMask.copy(alpha = 0.78f),
                                0.50f to Color.Transparent,
                                0.86f to DeepVelvetMask.copy(alpha = 0.78f),
                                1.0f to PureOledBlack
                            )
                        )
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to PureOledBlack,
                                0.15f to Color.Transparent,
                                0.85f to Color.Transparent,
                                1.0f to PureOledBlack
                            )
                        )
                    )
                }

                AmbientBlackBlurMode.OLED_MIDNIGHT_DIFFUSION -> {
                    // Still Arctic Mist soft outer OLED black shroud
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.52f to Obsidian04.copy(alpha = 0.55f),
                                0.86f to PureOledBlack.copy(alpha = 0.94f),
                                1.0f to PureOledBlack
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = size.maxDimension * 0.68f
                        )
                    )
                }

                AmbientBlackBlurMode.VELVET_SHADOW_MIST -> {
                    // Deep Velvet Melt total border dissolution mask
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0x3D050408),
                                0.48f to DeepVelvetMask.copy(alpha = 0.72f),
                                0.82f to PureOledBlack.copy(alpha = 0.95f),
                                1.0f to PureOledBlack
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = size.maxDimension * 0.66f
                        )
                    )
                }

                AmbientBlackBlurMode.EDGE_FADE_GAUSSIAN_BLUR -> {
                    // Midnight Horizon Gradient vertical fade
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to PureOledBlack,
                                0.18f to Color(0x52040507),
                                0.50f to Color.Transparent,
                                0.82f to Color(0x52040507),
                                1.0f to PureOledBlack
                            )
                        )
                    )
                }

                AmbientBlackBlurMode.RADIAL_VIGNETTE_DISSOLVE -> {
                    // Dark Ink Ripple soft perimeter dissolve
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.54f to Obsidian04.copy(alpha = 0.60f),
                                0.88f to PureOledBlack.copy(alpha = 0.94f),
                                1.0f to PureOledBlack
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = size.maxDimension * 0.68f
                        )
                    )
                }

                AmbientBlackBlurMode.CINEMATIC_SMOKED_BLUR -> {
                    // Frosted Noir Marble multi-toned frosted veil
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to PureOledBlack,
                                0.24f to DeepSlateNoir.copy(alpha = 0.44f),
                                0.50f to Charcoal0D.copy(alpha = 0.34f),
                                0.76f to DeepSlateNoir.copy(alpha = 0.44f),
                                1.0f to PureOledBlack
                            )
                        )
                    )
                }

                AmbientBlackBlurMode.VELVET_SHADOW_MELT -> {
                    // Anamorphic Bokeh Fade soft outer lens falloff
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.58f to Obsidian04.copy(alpha = 0.62f),
                                0.90f to PureOledBlack.copy(alpha = 0.95f),
                                1.0f to PureOledBlack
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = size.maxDimension * 0.70f
                        )
                    )
                }

                AmbientBlackBlurMode.MULTI_LAYER_LAYERED_FADE -> {
                    // Dimmed Charcoal Halo wide late-night radial alpha fade
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.48f to WarmCharcoal.copy(alpha = 0.38f),
                                0.82f to PureOledBlack.copy(alpha = 0.92f),
                                1.0f to PureOledBlack
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = size.maxDimension * 0.66f
                        )
                    )
                }
            }
        }
    }
}

/**
 * Minimalist Blur & Fade preview swatch representing each of the 10 Luxury modes.
 */
@Composable
private fun BlackBlurModeSwatch(
    mode: AmbientBlackBlurMode,
    isSelected: Boolean
) {
    val swatchBorder = if (isSelected) Color(0xFFE2E8F0) else Color(0x2AFFFFFF)
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(PureOledBlack)
            .border(1.dp, swatchBorder, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (mode) {
                AmbientBlackBlurMode.DEEP_OBSIDIAN_BLUR -> {
                    // 01 Silent Obsidian Eclipse
                    drawRect(PureOledBlack)
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(PureOledBlack, Color(0xFF282E3A), PureOledBlack)
                        ),
                        radius = size.minDimension * 0.48f
                    )
                }
                AmbientBlackBlurMode.CINEMA_MATTE_VIGNETTE -> {
                    // 02 Smoked Crystal Diffusion
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF15171D), Color(0xFF2B313D), Color(0xFF0A0B0E))
                        )
                    )
                }
                AmbientBlackBlurMode.SMOKED_LIQUID_GLASS -> {
                    // 03 Matte Theater Curtain Fade
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF24212B), PureOledBlack, Color(0xFF24212B))
                        )
                    )
                }
                AmbientBlackBlurMode.OLED_MIDNIGHT_DIFFUSION -> {
                    // 04 Still Arctic Mist (Monochrome)
                    drawRect(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF333538), Color(0xFF151618), PureOledBlack)
                        )
                    )
                }
                AmbientBlackBlurMode.VELVET_SHADOW_MIST -> {
                    // 05 Deep Velvet Melt
                    drawRect(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF282330), Obsidian04, PureOledBlack)
                        )
                    )
                }
                AmbientBlackBlurMode.EDGE_FADE_GAUSSIAN_BLUR -> {
                    // 06 Midnight Horizon Gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF293242), PureOledBlack, Color(0xFF232B38))
                        )
                    )
                }
                AmbientBlackBlurMode.RADIAL_VIGNETTE_DISSOLVE -> {
                    // 07 Dark Ink Ripple Blur
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF2D3442), Color(0xFF12151C), PureOledBlack)
                        ),
                        radius = size.minDimension * 0.48f
                    )
                }
                AmbientBlackBlurMode.CINEMATIC_SMOKED_BLUR -> {
                    // 08 Frosted Noir Marble
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF1C2029), Color(0xFF2C313D), Color(0xFF0B0C10))
                        )
                    )
                }
                AmbientBlackBlurMode.VELVET_SHADOW_MELT -> {
                    // 09 Anamorphic Bokeh Fade
                    drawRect(PureOledBlack)
                    drawCircle(
                        color = Color(0x44394254),
                        radius = size.minDimension * 0.22f,
                        center = Offset(size.width * 0.32f, size.height * 0.34f)
                    )
                    drawCircle(
                        color = Color(0x44394254),
                        radius = size.minDimension * 0.20f,
                        center = Offset(size.width * 0.70f, size.height * 0.66f)
                    )
                }
                AmbientBlackBlurMode.MULTI_LAYER_LAYERED_FADE -> {
                    // 10 Dimmed Charcoal Halo
                    drawRect(PureOledBlack)
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF332924), Color(0xFF120F0E), PureOledBlack)
                        ),
                        radius = size.minDimension * 0.48f
                    )
                }
            }
            // Tiny cinema frame silhouette inside the swatch
            drawRect(
                color = PureOledBlack.copy(alpha = 0.86f),
                topLeft = Offset(size.width * 0.24f, size.height * 0.34f),
                size = Size(size.width * 0.52f, size.height * 0.32f)
            )
        }
    }
}

/**
 * Ultra-clean floating selector opened when tapping the player's Ambient Video Glow Fill button.
 * All selections save immediately to Room DB.
 */
@Composable
fun BlackBlurAmbientPickerDialog(
    visible: Boolean,
    isEnabled: Boolean,
    selectedMode: AmbientBlackBlurMode,
    isArabic: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectMode: (AmbientBlackBlurMode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    unlockedModes: Set<AmbientBlackBlurMode> = AmbientBlackBlurMode.entries.toSet(),
    onLockedModeAttempt: ((AmbientBlackBlurMode) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var lockNotice by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(lockNotice, visible) {
        if (!visible) {
            lockNotice = null
        } else if (lockNotice != null) {
            kotlinx.coroutines.delay(2600L)
            lockNotice = null
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.94f, animationSpec = tween(220)),
        exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.96f, animationSpec = tween(180)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xF508080B),
                border = BorderStroke(1.dp, Color(0x28FFFFFF)),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .heightIn(max = 540.dp)
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .testTag("black_blur_ambient_picker_overlay")
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF141418))
                                    .border(1.dp, Color(0x2AFFFFFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = if (isEnabled) Color(0xFFE2E8F0) else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ambient Video Glow Fill",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color(0xFFF1F5F9),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                )
                                Text(
                                    text = if (isArabic) "أوضاع التمويه السينمائي الأسود (حفظ تلقائي)" else "Black Blur Cinema Modes • Auto-Saved",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleEnabled(checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureOledBlack,
                                    checkedTrackColor = Color(0xFFE2E8F0),
                                    uncheckedThumbColor = Color(0xFF64748B),
                                    uncheckedTrackColor = Color(0x22FFFFFF)
                                ),
                                modifier = Modifier.testTag("black_blur_master_switch")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedVisibility(visible = lockNotice != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF101217),
                            border = BorderStroke(1.dp, Color(0x44CBD5E1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = lockNotice.orEmpty(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFCBD5E1),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AmbientBlackBlurMode.entries.forEach { mode ->
                            val isUnlocked = mode.isUnlockedByDefault || (mode in unlockedModes)
                            val isSelected = isEnabled && selectedMode == mode
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = when {
                                    isSelected -> Color(0xFF141419)
                                    !isUnlocked -> Color(0xFF08080A)
                                    else -> Color(0xFF0B0B0E)
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 1.2.dp else 0.8.dp,
                                    color = if (isSelected) Color(0xAAE2E8F0) else Color(0x1EFFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (isUnlocked) {
                                            onSelectMode(mode)
                                        } else {
                                            val title = if (isArabic) mode.titleAr else mode.titleEn
                                            lockNotice = if (isArabic) {
                                                "🔒 افتح «$title» من المتجر ← Ambient Video Fill"
                                            } else {
                                                "🔒 Unlock \"$title\" in Shop → Ambient Video Fill"
                                            }
                                            onLockedModeAttempt?.invoke(mode)
                                        }
                                    }
                                    .testTag("black_blur_mode_${mode.name}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BlackBlurModeSwatch(mode = mode, isSelected = isSelected)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isArabic) mode.titleAr else mode.titleEn,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = when {
                                                    isSelected -> Color.White
                                                    !isUnlocked -> Color(0xFF8A94A6)
                                                    else -> Color(0xFFCBD5E1)
                                                },
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isArabic) mode.subtitleAr else mode.subtitleEn,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF7E8898),
                                                fontSize = 10.5.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE2E8F0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = PureOledBlack,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else if (!isUnlocked) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0x16CBD5E1),
                                            border = BorderStroke(0.7.dp, Color(0x33CBD5E1))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Text(
                                                    text = "${mode.shieldsCost}🛡️+${mode.costAmount}🗝️",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFF94A3B8),
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 9.5.sp
                                                    )
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = mode.shortBadge,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF475569),
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
}

/**
 * Clean toggle + 10-mode Black Blur selector embedded directly in the Video Player Settings Bottom Sheet.
 */
@Composable
fun AmbientBlackBlurSettingsSection(
    isEnabled: Boolean,
    selectedMode: AmbientBlackBlurMode,
    isArabic: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectMode: (AmbientBlackBlurMode) -> Unit,
    unlockedModes: Set<AmbientBlackBlurMode> = AmbientBlackBlurMode.entries.toSet(),
    onLockedModeAttempt: ((AmbientBlackBlurMode) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var lockNotice by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(lockNotice) {
        if (lockNotice != null) {
            kotlinx.coroutines.delay(2600L)
            lockNotice = null
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = if (isEnabled) Color(0xFFE2E8F0) else Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Ambient Video Glow Fill",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (isEnabled) {
                            if (isArabic) selectedMode.titleAr else selectedMode.titleEn
                        } else {
                            if (isArabic) "معطل • خلفية OLED سوداء نقية" else "Off • Pure #000000 OLED black"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { isChecked ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleEnabled(isChecked)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PureOledBlack,
                    checkedTrackColor = Color(0xFFE2E8F0),
                    uncheckedThumbColor = Color(0xFF64748B),
                    uncheckedTrackColor = Color(0x33FFFFFF)
                ),
                modifier = Modifier.testTag("ambient_video_glow_switch")
            )
        }

        AnimatedVisibility(
            visible = isEnabled,
            enter = fadeIn(tween(200)) + expandVertically(),
            exit = fadeOut(tween(150)) + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AnimatedVisibility(visible = lockNotice != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF101217),
                        border = BorderStroke(0.9.dp, Color(0x44CBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = lockNotice.orEmpty(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                AmbientBlackBlurMode.entries.forEach { mode ->
                    val isUnlocked = mode.isUnlockedByDefault || (mode in unlockedModes)
                    val isSelected = selectedMode == mode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isSelected -> Color(0xFF141419)
                            !isUnlocked -> Color(0xFF070709)
                            else -> Color(0xFF0A0A0D)
                        },
                        border = BorderStroke(
                            width = if (isSelected) 1.dp else 0.7.dp,
                            color = if (isSelected) Color(0x99E2E8F0) else Color(0x1EFFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (isUnlocked) {
                                    onSelectMode(mode)
                                } else {
                                    val title = if (isArabic) mode.titleAr else mode.titleEn
                                    lockNotice = if (isArabic) {
                                        "🔒 افتح «$title» من المتجر ← Ambient Video Fill"
                                    } else {
                                        "🔒 Unlock \"$title\" in Shop → Ambient Video Fill"
                                    }
                                    onLockedModeAttempt?.invoke(mode)
                                }
                            }
                            .testTag("settings_black_blur_mode_${mode.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BlackBlurModeSwatch(mode = mode, isSelected = isSelected)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) mode.titleAr else mode.titleEn,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = when {
                                            isSelected -> Color.White
                                            !isUnlocked -> Color(0xFF8A94A6)
                                            else -> Color(0xFFCBD5E1)
                                        },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isArabic) mode.subtitleAr else mode.subtitleEn,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF7E8898),
                                        fontSize = 10.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = Color(0xFFE2E8F0),
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (!isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
