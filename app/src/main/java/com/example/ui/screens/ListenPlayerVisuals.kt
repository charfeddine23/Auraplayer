package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.LocalAudioTrack
import com.example.data.util.LocalMediaScanner
import com.example.ui.theme.AuraPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import android.graphics.Color as AndroidColor

@Immutable
internal data class AlbumDynamicPalette(
    val dominantAmbient: Color,
    val vibrantPrimary: Color,
    val vibrantSecondary: Color,
    val glowHighlight: Color,
    val deepShadowTint: Color,
    val onPrimaryAccent: Color
)

@Composable
internal fun rememberAnimatedAlbumPalette(
    context: Context,
    track: LocalAudioTrack,
    coverBitmap: Bitmap?,
    aura: AuraPalette
): AlbumDynamicPalette {
    val fallback = remember(aura.isDark, aura.accentCyan, aura.accentViolet, aura.accentEmerald) {
        buildFallbackPalette(
            isDarkTheme = aura.isDark,
            fallbackPrimary = aura.accentCyan,
            fallbackSecondary = aura.accentViolet,
            fallbackTertiary = aura.accentEmerald
        )
    }

    var targetPalette by remember(aura.isDark) { mutableStateOf(fallback) }

    LaunchedEffect(track.id, track.albumCoverRes, track.albumArtUri, coverBitmap, aura.isDark) {
        targetPalette = loadAndExtractAlbumPalette(
            context = context,
            track = track,
            coverBitmap = coverBitmap,
            isDarkTheme = aura.isDark,
            fallbackPrimary = aura.accentCyan,
            fallbackSecondary = aura.accentViolet,
            fallbackTertiary = aura.accentEmerald
        )
    }

    val colorAnimSpec = remember {
        tween<Color>(durationMillis = 850, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))
    }

    val dominantAmbient by animateColorAsState(
        targetValue = targetPalette.dominantAmbient,
        animationSpec = colorAnimSpec,
        label = "album_dominant_ambient"
    )
    val vibrantPrimary by animateColorAsState(
        targetValue = targetPalette.vibrantPrimary,
        animationSpec = colorAnimSpec,
        label = "album_vibrant_primary"
    )
    val vibrantSecondary by animateColorAsState(
        targetValue = targetPalette.vibrantSecondary,
        animationSpec = colorAnimSpec,
        label = "album_vibrant_secondary"
    )
    val glowHighlight by animateColorAsState(
        targetValue = targetPalette.glowHighlight,
        animationSpec = colorAnimSpec,
        label = "album_glow_highlight"
    )
    val deepShadowTint by animateColorAsState(
        targetValue = targetPalette.deepShadowTint,
        animationSpec = colorAnimSpec,
        label = "album_deep_shadow"
    )
    val onPrimaryAccent by animateColorAsState(
        targetValue = targetPalette.onPrimaryAccent,
        animationSpec = colorAnimSpec,
        label = "album_on_primary"
    )

    return AlbumDynamicPalette(
        dominantAmbient = dominantAmbient,
        vibrantPrimary = vibrantPrimary,
        vibrantSecondary = vibrantSecondary,
        glowHighlight = glowHighlight,
        deepShadowTint = deepShadowTint,
        onPrimaryAccent = onPrimaryAccent
    )
}

private fun buildFallbackPalette(
    isDarkTheme: Boolean,
    fallbackPrimary: Color,
    fallbackSecondary: Color,
    fallbackTertiary: Color
): AlbumDynamicPalette {
    val tunedPrimary = tuneAccentColor(fallbackPrimary, isDarkTheme)
    val tunedSecondary = tuneAccentColor(fallbackSecondary, isDarkTheme)
    val ambient = tuneAmbientColor(fallbackPrimary, isDarkTheme)
    val glow = lerp(tunedPrimary, Color.White, if (isDarkTheme) 0.42f else 0.24f)
    val shadow = tuneShadowColor(fallbackTertiary.takeIf { it != Color.Transparent } ?: fallbackPrimary)
    val onPrimary = if (tunedPrimary.luminance() > 0.46f) Color(0xFF0A0D14) else Color.White
    return AlbumDynamicPalette(
        dominantAmbient = ambient,
        vibrantPrimary = tunedPrimary,
        vibrantSecondary = tunedSecondary,
        glowHighlight = glow,
        deepShadowTint = shadow,
        onPrimaryAccent = onPrimary
    )
}

private suspend fun loadAndExtractAlbumPalette(
    context: Context,
    track: LocalAudioTrack,
    coverBitmap: Bitmap?,
    isDarkTheme: Boolean,
    fallbackPrimary: Color,
    fallbackSecondary: Color,
    fallbackTertiary: Color
): AlbumDynamicPalette {
    val fallback = buildFallbackPalette(
        isDarkTheme = isDarkTheme,
        fallbackPrimary = fallbackPrimary,
        fallbackSecondary = fallbackSecondary,
        fallbackTertiary = fallbackTertiary
    )

    var decodedResourceBitmap: Bitmap? = null
    val sourceBitmap: Bitmap? = when {
        coverBitmap != null && !coverBitmap.isRecycled -> coverBitmap
        track.albumArtUri != null || (track.isFromDevice && !track.audioUri.isNullOrBlank()) -> withContext(Dispatchers.IO) {
            LocalMediaScanner.extractAudioCoverArt(
                context = context,
                audioUriStr = track.audioUri,
                albumArtUriStr = track.albumArtUri
            )
        }
        else -> withContext(Dispatchers.IO) {
            runCatching {
                val opts = BitmapFactory.Options().apply {
                    inSampleSize = 4
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeResource(context.resources, track.albumCoverRes, opts)
            }.getOrNull().also { decodedResourceBitmap = it }
        }
    }

    if (sourceBitmap == null || sourceBitmap.isRecycled) {
        return fallback
    }

    return try {
        extractAlbumPaletteFromBitmap(
            bitmap = sourceBitmap,
            isDarkTheme = isDarkTheme,
            fallback = fallback
        )
    } finally {
        val toRecycle = decodedResourceBitmap
        if (toRecycle != null && toRecycle !== coverBitmap && !toRecycle.isRecycled) {
            toRecycle.recycle()
        }
    }
}

private suspend fun extractAlbumPaletteFromBitmap(
    bitmap: Bitmap,
    isDarkTheme: Boolean,
    fallback: AlbumDynamicPalette
): AlbumDynamicPalette = withContext(Dispatchers.Default) {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0) return@withContext fallback

    val grid = 32
    val stepX = (width.toFloat() / grid).coerceAtLeast(1f)
    val stepY = (height.toFloat() / grid).coerceAtLeast(1f)

    val bucketCount = 24
    val bucketWeight = FloatArray(bucketCount)
    val bucketR = FloatArray(bucketCount)
    val bucketG = FloatArray(bucketCount)
    val bucketB = FloatArray(bucketCount)
    val hsv = FloatArray(3)

    var totalWeight = 0f
    var sumR = 0f
    var sumG = 0f
    var sumB = 0f

    for (gy in 0 until grid) {
        val py = min(height - 1, (gy * stepY).toInt())
        val normY = (gy.toFloat() / (grid - 1).coerceAtLeast(1)) - 0.5f
        for (gx in 0 until grid) {
            val px = min(width - 1, (gx * stepX).toInt())
            val pixel = bitmap.getPixel(px, py)
            val alpha = AndroidColor.alpha(pixel)
            if (alpha < 128) continue

            AndroidColor.colorToHSV(pixel, hsv)
            val sat = hsv[1]
            val valBright = hsv[2]

            val normX = (gx.toFloat() / (grid - 1).coerceAtLeast(1)) - 0.5f
            val centerDistSq = normX * normX + normY * normY
            val centerBias = 1.25f - centerDistSq

            val r = AndroidColor.red(pixel).toFloat()
            val g = AndroidColor.green(pixel).toFloat()
            val b = AndroidColor.blue(pixel).toFloat()

            val baseWeight = (0.35f + centerBias * 0.45f).coerceAtLeast(0.1f)
            totalWeight += baseWeight
            sumR += r * baseWeight
            sumG += g * baseWeight
            sumB += b * baseWeight

            // Filter out near-black and near-white desaturated pixels for vivid palette selection
            if (valBright < 0.12f || (sat < 0.10f && valBright > 0.90f)) continue

            val chromaWeight = (sat * sat * 2.4f + 0.15f) *
                (1f - abs(valBright - 0.58f) * 0.65f).coerceIn(0.35f, 1.15f) *
                centerBias

            val bucketIdx = ((hsv[0] / 360f) * bucketCount).toInt().coerceIn(0, bucketCount - 1)
            bucketWeight[bucketIdx] += chromaWeight
            bucketR[bucketIdx] += r * chromaWeight
            bucketG[bucketIdx] += g * chromaWeight
            bucketB[bucketIdx] += b * chromaWeight
        }
    }

    val avgColor = if (totalWeight > 0f) {
        Color(
            red = (sumR / totalWeight).toInt().coerceIn(0, 255),
            green = (sumG / totalWeight).toInt().coerceIn(0, 255),
            blue = (sumB / totalWeight).toInt().coerceIn(0, 255)
        )
    } else {
        fallback.dominantAmbient
    }

    var primaryBucket = -1
    var bestPrimaryScore = 0f
    for (i in 0 until bucketCount) {
        if (bucketWeight[i] > bestPrimaryScore) {
            bestPrimaryScore = bucketWeight[i]
            primaryBucket = i
        }
    }

    if (primaryBucket == -1 || bestPrimaryScore <= 0.05f) {
        return@withContext fallback.copy(
            dominantAmbient = tuneAmbientColor(avgColor, isDarkTheme),
            deepShadowTint = tuneShadowColor(avgColor)
        )
    }

    val rawPrimary = Color(
        red = (bucketR[primaryBucket] / bucketWeight[primaryBucket]).toInt().coerceIn(0, 255),
        green = (bucketG[primaryBucket] / bucketWeight[primaryBucket]).toInt().coerceIn(0, 255),
        blue = (bucketB[primaryBucket] / bucketWeight[primaryBucket]).toInt().coerceIn(0, 255)
    )

    // Look for a distinct complementary / secondary hue at least ~45 degrees apart
    var secondaryBucket = -1
    var bestSecondaryScore = 0f
    for (i in 0 until bucketCount) {
        val bucketDist = min(abs(i - primaryBucket), bucketCount - abs(i - primaryBucket))
        if (bucketDist >= 3 && bucketWeight[i] > bestSecondaryScore) {
            bestSecondaryScore = bucketWeight[i]
            secondaryBucket = i
        }
    }

    val rawSecondary = if (secondaryBucket != -1 && bestSecondaryScore > bestPrimaryScore * 0.14f) {
        Color(
            red = (bucketR[secondaryBucket] / bucketWeight[secondaryBucket]).toInt().coerceIn(0, 255),
            green = (bucketG[secondaryBucket] / bucketWeight[secondaryBucket]).toInt().coerceIn(0, 255),
            blue = (bucketB[secondaryBucket] / bucketWeight[secondaryBucket]).toInt().coerceIn(0, 255)
        )
    } else {
        shiftHue(rawPrimary, 46f)
    }

    val dominantAmbient = tuneAmbientColor(lerp(avgColor, rawPrimary, 0.58f), isDarkTheme)
    val vibrantPrimary = tuneAccentColor(rawPrimary, isDarkTheme)
    val vibrantSecondary = tuneAccentColor(rawSecondary, isDarkTheme)
    val glowHighlight = lerp(vibrantPrimary, Color.White, if (isDarkTheme) 0.45f else 0.22f)
    val deepShadowTint = tuneShadowColor(lerp(rawPrimary, rawSecondary, 0.25f))
    val onPrimaryAccent = if (vibrantPrimary.luminance() > 0.46f) Color(0xFF0A0D14) else Color.White

    AlbumDynamicPalette(
        dominantAmbient = dominantAmbient,
        vibrantPrimary = vibrantPrimary,
        vibrantSecondary = vibrantSecondary,
        glowHighlight = glowHighlight,
        deepShadowTint = deepShadowTint,
        onPrimaryAccent = onPrimaryAccent
    )
}

private fun tuneAccentColor(color: Color, isDarkTheme: Boolean): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color.toArgb(), hsv)
    hsv[1] = hsv[1].coerceIn(0.56f, 0.94f)
    hsv[2] = if (isDarkTheme) hsv[2].coerceIn(0.76f, 0.98f) else hsv[2].coerceIn(0.42f, 0.68f)
    return Color(AndroidColor.HSVToColor(hsv))
}

private fun tuneAmbientColor(color: Color, isDarkTheme: Boolean): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color.toArgb(), hsv)
    hsv[1] = hsv[1].coerceIn(0.36f, 0.80f)
    hsv[2] = if (isDarkTheme) hsv[2].coerceIn(0.26f, 0.56f) else hsv[2].coerceIn(0.55f, 0.84f)
    return Color(AndroidColor.HSVToColor(hsv))
}

private fun tuneShadowColor(color: Color): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color.toArgb(), hsv)
    hsv[1] = hsv[1].coerceIn(0.55f, 0.95f)
    hsv[2] = hsv[2].coerceIn(0.24f, 0.50f)
    return Color(AndroidColor.HSVToColor(hsv))
}

private fun shiftHue(color: Color, degrees: Float): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color.toArgb(), hsv)
    hsv[0] = (hsv[0] + degrees + 360f) % 360f
    return Color(AndroidColor.HSVToColor(hsv))
}

@Composable
internal fun LiquidGlassSurface(
    palette: AlbumDynamicPalette,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    tintStrength: Float = 1f,
    shadowElevation: Dp = 10.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val baseGlassStart = if (isDark) {
        lerp(Color(0x8A101624), palette.dominantAmbient.copy(alpha = 0.42f), 0.36f * tintStrength)
    } else {
        lerp(Color(0xCCF7FAFF), palette.dominantAmbient.copy(alpha = 0.24f), 0.26f * tintStrength)
    }
    val baseGlassEnd = if (isDark) {
        lerp(Color(0x6B090D16), palette.vibrantSecondary.copy(alpha = 0.22f), 0.28f * tintStrength)
    } else {
        lerp(Color(0xB8EDF3FC), palette.vibrantPrimary.copy(alpha = 0.16f), 0.20f * tintStrength)
    }

    val specularBorder = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.42f),
                palette.glowHighlight.copy(alpha = 0.34f * tintStrength),
                Color.White.copy(alpha = 0.08f),
                palette.vibrantSecondary.copy(alpha = 0.28f * tintStrength)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.92f),
                palette.vibrantPrimary.copy(alpha = 0.34f * tintStrength),
                Color.White.copy(alpha = 0.45f),
                palette.vibrantSecondary.copy(alpha = 0.28f * tintStrength)
            )
        }
    )

    val sheenBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.13f),
                Color.White.copy(alpha = 0.03f),
                Color.Transparent
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.55f),
                Color.White.copy(alpha = 0.14f),
                Color.Transparent
            )
        }
    )

    Box(
        modifier = modifier
            .then(
                if (shadowElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = shadowElevation,
                        shape = shape,
                        ambientColor = palette.deepShadowTint.copy(alpha = 0.65f),
                        spotColor = palette.vibrantPrimary.copy(alpha = 0.55f)
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(Brush.linearGradient(listOf(baseGlassStart, baseGlassEnd)))
            .background(sheenBrush)
            .border(BorderStroke(borderWidth, specularBorder), shape),
        content = content
    )
}

@Composable
internal fun LiquidGlassAtmosphericBackdrop(
    currentTrack: LocalAudioTrack,
    coverBitmap: Bitmap?,
    palette: AlbumDynamicPalette,
    isPlaying: Boolean,
    aura: AuraPalette
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_backdrop_orbs")
    val orbitProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_orbit"
    )
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse"
    )

    val playEnergy by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.55f,
        animationSpec = tween(700),
        label = "play_energy"
    )

    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(56.dp)
    } else {
        Modifier
    }

    val artAlpha = if (aura.isDark) 0.44f else 0.22f
    val backdropModifier = Modifier
        .fillMaxSize()
        .then(blurModifier)

    when {
        coverBitmap != null && !coverBitmap.isRecycled -> {
            Image(
                bitmap = coverBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = backdropModifier,
                contentScale = ContentScale.Crop,
                alpha = artAlpha
            )
        }
        currentTrack.albumArtUri != null -> {
            AsyncImage(
                model = currentTrack.albumArtUri,
                contentDescription = null,
                modifier = backdropModifier,
                contentScale = ContentScale.Crop,
                alpha = artAlpha
            )
        }
        else -> {
            Image(
                painter = painterResource(id = currentTrack.albumCoverRes),
                contentDescription = null,
                modifier = backdropModifier,
                contentScale = ContentScale.Crop,
                alpha = artAlpha
            )
        }
    }

    // Dynamic ambient color veil derived from the album cover
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (aura.isDark) {
                        listOf(
                            lerp(aura.background, palette.dominantAmbient, 0.52f).copy(alpha = 0.72f),
                            lerp(aura.background, palette.deepShadowTint, 0.46f).copy(alpha = 0.84f),
                            lerp(aura.background, palette.dominantAmbient, 0.28f).copy(alpha = 0.93f),
                            aura.background.copy(alpha = 0.98f)
                        )
                    } else {
                        listOf(
                            lerp(aura.background, palette.dominantAmbient, 0.30f).copy(alpha = 0.78f),
                            lerp(aura.background, palette.vibrantSecondary, 0.18f).copy(alpha = 0.86f),
                            lerp(aura.background, palette.vibrantPrimary, 0.14f).copy(alpha = 0.94f),
                            aura.background.copy(alpha = 0.98f)
                        )
                    }
                )
            )
    )

    // Drifting & breathing liquid aurora orbs
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val driftX = cos(orbitProgress) * w * 0.11f
        val driftY = sin(orbitProgress) * h * 0.06f

        val primaryAlpha = (if (aura.isDark) 0.42f else 0.24f) * playEnergy
        val secondaryAlpha = (if (aura.isDark) 0.34f else 0.20f) * playEnergy
        val highlightAlpha = (if (aura.isDark) 0.22f else 0.14f) * playEnergy

        // Top-left primary orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.vibrantPrimary.copy(alpha = primaryAlpha),
                    palette.dominantAmbient.copy(alpha = primaryAlpha * 0.45f),
                    Color.Transparent
                ),
                radius = w * 0.85f * pulseProgress
            ),
            center = Offset(w * 0.22f + driftX, h * 0.20f + driftY)
        )

        // Mid-right secondary orb behind artwork
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.vibrantSecondary.copy(alpha = secondaryAlpha),
                    palette.dominantAmbient.copy(alpha = secondaryAlpha * 0.40f),
                    Color.Transparent
                ),
                radius = w * 0.92f
            ),
            center = Offset(w * 0.84f - driftX * 0.8f, h * 0.44f - driftY)
        )

        // Lower glow orb behind controls
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.glowHighlight.copy(alpha = highlightAlpha),
                    palette.vibrantPrimary.copy(alpha = highlightAlpha * 0.35f),
                    Color.Transparent
                ),
                radius = w * 0.78f * (2f - pulseProgress * 0.85f)
            ),
            center = Offset(w * 0.48f + driftX * 0.5f, h * 0.84f - driftY * 0.6f)
        )
    }
}

@Composable
internal fun ReactiveAudioWaveBars(
    isPlaying: Boolean,
    palette: AlbumDynamicPalette,
    modifier: Modifier = Modifier,
    barCount: Int = 14
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reactive_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    val energy by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.16f,
        animationSpec = tween(420),
        label = "wave_energy"
    )

    Canvas(
        modifier = modifier
            .width(56.dp)
            .height(14.dp)
    ) {
        val count = barCount.coerceAtLeast(4)
        val gap = size.width / (count * 2f - 1f)
        val barWidth = gap.coerceAtLeast(1.8.dp.toPx())
        val step = size.width / count
        val barBrush = Brush.horizontalGradient(
            listOf(palette.vibrantPrimary, palette.glowHighlight, palette.vibrantSecondary)
        )

        for (i in 0 until count) {
            val wave1 = (sin(phase + i * 0.65f) + 1f) * 0.5f
            val wave2 = (cos(phase * 1.6f - i * 0.9f) + 1f) * 0.5f
            val amp = ((wave1 * 0.6f + wave2 * 0.4f) * energy + 0.14f).coerceIn(0.14f, 1f)
            val barH = size.height * amp
            val x = i * step + (step - barWidth) * 0.5f
            val y = (size.height - barH) * 0.5f
            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth * 0.5f, barWidth * 0.5f)
            )
        }
    }
}

@Composable
internal fun Modifier.behindCoverAmbientShadows(
    palette: AlbumDynamicPalette,
    isPlaying: Boolean,
    isDark: Boolean,
    pulseRatio: Float
): Modifier {
    val playScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.68f,
        animationSpec = tween(600),
        label = "cover_shadow_play_scale"
    )

    return this.drawBehind {
        val center = Offset(size.width * 0.5f, size.height * 0.52f)
        val minDim = size.minDimension

        // Deep floating floor shadow underneath the album cover
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Black.copy(alpha = if (isDark) 0.72f else 0.34f),
                    palette.deepShadowTint.copy(alpha = if (isDark) 0.55f else 0.28f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.62f),
                radius = minDim * 0.78f
            ),
            center = Offset(size.width * 0.5f, size.height * 0.62f),
            radius = minDim * 0.78f
        )

        // Wide left-top primary vibrant bloom behind the artwork
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.vibrantPrimary.copy(alpha = (if (isDark) 0.56f else 0.36f) * playScale),
                    palette.dominantAmbient.copy(alpha = (if (isDark) 0.28f else 0.16f) * playScale),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.34f, size.height * 0.46f),
                radius = minDim * (0.76f + 0.08f * pulseRatio)
            ),
            center = Offset(size.width * 0.34f, size.height * 0.46f),
            radius = minDim * (0.76f + 0.08f * pulseRatio)
        )

        // Wide right-bottom secondary complementary bloom behind the artwork
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.vibrantSecondary.copy(alpha = (if (isDark) 0.48f else 0.30f) * playScale),
                    palette.glowHighlight.copy(alpha = (if (isDark) 0.22f else 0.14f) * playScale),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.68f, size.height * 0.58f),
                radius = minDim * (0.74f + 0.07f * (1f - pulseRatio))
            ),
            center = Offset(size.width * 0.68f, size.height * 0.58f),
            radius = minDim * (0.74f + 0.07f * (1f - pulseRatio))
        )

        // Soft centered aura halo peeking past cover edges
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.glowHighlight.copy(alpha = (if (isDark) 0.32f else 0.22f) * playScale),
                    Color.Transparent
                ),
                center = center,
                radius = minDim * 0.72f
            ),
            center = center,
            radius = minDim * 0.72f
        )
    }
}
