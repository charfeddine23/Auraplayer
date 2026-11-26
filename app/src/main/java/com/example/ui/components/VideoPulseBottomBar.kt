// Developed by Charfeddine Ka
package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.aura
import kotlin.math.roundToInt
import kotlin.random.Random

val LocalFloatingBottomBarPadding = staticCompositionLocalOf { 0.dp }

enum class NavDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    WATCH("Watch", Icons.Filled.SmartDisplay, Icons.Outlined.SmartDisplay),
    LISTEN("Listen", Icons.Filled.Headphones, Icons.Outlined.Headphones),
    SHORT("Short", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle),
    MY_SPACE("My space", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun VideoPulseBottomBar(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val destinations = remember { NavDestination.entries }
    val selectedIndex = destinations.indexOf(currentDestination).coerceAtLeast(0)
    val grainBitmap = remember { createFilmGrainBitmap(sizePx = 128) }

    // Enforce strictly left-to-right item ordering regardless of locale or RTL layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                .height(68.dp)
                .clip(CircleShape)
                .background(Color(0xFF0B0B0D).copy(alpha = 0.75f))
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = 0.55f),
                        0.45f to Color.White.copy(alpha = 0.08f),
                        1.0f to Color.White.copy(alpha = 0.28f)
                    ),
                    shape = CircleShape
                )
        ) {
            // Optical Glass Caustics, Backdrop Blur & Chromatic Edge Dispersion Layer (strictly clipped inside CircleShape)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(28.dp)
                    .clip(CircleShape)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Soft internal specular glass streaks matching dark liquid refraction
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.14f),
                                Color(0xFF38BDF8).copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.16f, size.height * 0.18f),
                            radius = size.height * 0.85f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color(0xFFFBBF24).copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.42f, size.height * 0.22f),
                            radius = size.height * 0.75f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.13f),
                                Color(0xFFC084FC).copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.68f, size.height * 0.20f),
                            radius = size.height * 0.85f
                        )
                    )
                }
            }

            // Subtle Chromatic Edge Refraction Tint along Curved Highlights (low-alpha dispersion)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = 1.2.dp,
                        brush = Brush.horizontalGradient(
                            0.0f to Color(0xFF38BDF8).copy(alpha = 0.14f),
                            0.18f to Color(0xFFFBBF24).copy(alpha = 0.11f),
                            0.50f to Color.White.copy(alpha = 0.06f),
                            0.78f to Color(0xFFA855F7).copy(alpha = 0.12f),
                            1.0f to Color(0xFF38BDF8).copy(alpha = 0.14f)
                        ),
                        shape = CircleShape
                    )
            )

            // Subtle Film Grain Texture (0.03f alpha)
            Canvas(modifier = Modifier.matchParentSize()) {
                val tileW = grainBitmap.width
                val tileH = grainBitmap.height
                var y = 0
                while (y < size.height.roundToInt()) {
                    var x = 0
                    while (x < size.width.roundToInt()) {
                        drawImage(
                            image = grainBitmap,
                            dstOffset = IntOffset(x, y),
                            dstSize = IntSize(tileW, tileH),
                            alpha = 0.03f
                        )
                        x += tileW
                    }
                    y += tileH
                }
            }

            // Interactive Tab Track with Smooth 60-90 FPS Sliding Selection Pill
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                val tabWidth = maxWidth / destinations.size
                val indicatorOffsetX by animateDpAsState(
                    targetValue = tabWidth * selectedIndex,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "liquid_glass_pill_offset"
                )

                // Moving Active Liquid Glass Capsule Pill
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffsetX)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.19f),
                                    Color.White.copy(alpha = 0.10f)
                                )
                            )
                        )
                        .border(
                            width = 0.8.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.34f),
                                    Color.White.copy(alpha = 0.06f)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    destinations.forEach { destination ->
                        val isSelected = destination == currentDestination
                        val interactionSource = remember { MutableInteractionSource() }

                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                            label = "tab_content_color"
                        )
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.06f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = 0.78f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "tab_icon_scale"
                        )
                        val glowAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 0.32f else 0f,
                            animationSpec = tween(durationMillis = 220),
                            label = "tab_glow_alpha"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    if (!isSelected) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onDestinationSelected(destination)
                                    }
                                }
                                .testTag("nav_item_${destination.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .scale(iconScale)
                                    .padding(horizontal = 2.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .drawBehind {
                                            if (glowAlpha > 0.01f) {
                                                drawCircle(
                                                    brush = Brush.radialGradient(
                                                        colors = listOf(
                                                            Color.White.copy(alpha = glowAlpha),
                                                            Color.Transparent
                                                        ),
                                                        radius = size.maxDimension * 1.15f
                                                    )
                                                )
                                            }
                                        }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.label,
                                        tint = contentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = destination.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = contentColor,
                                        letterSpacing = 0.1.sp
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun createFilmGrainBitmap(sizePx: Int): ImageBitmap {
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val rng = Random(20261124)
    val pixels = IntArray(sizePx * sizePx)
    for (i in pixels.indices) {
        val lum = rng.nextInt(140, 256)
        val alpha = rng.nextInt(90, 256)
        pixels[i] = (alpha shl 24) or (lum shl 16) or (lum shl 8) or lum
    }
    bmp.setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
    return bmp.asImageBitmap()
}

@Composable
fun BottomEdgeFadeInGlass(
    height: Dp = 68.dp,
    modifier: Modifier = Modifier,
    baseBackgroundColor: Color = MaterialTheme.aura.background,
    frostedSurfaceColor: Color = MaterialTheme.aura.surfaceElevated,
    isDark: Boolean = MaterialTheme.aura.isDark
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        // Compact frosted-diffusion glass layer rising gently along the bottom edge
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .blur(radius = 16.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.46f to frostedSurfaceColor.copy(alpha = if (isDark) 0.16f else 0.22f),
                            0.78f to baseBackgroundColor.copy(alpha = if (isDark) 0.44f else 0.52f),
                            1.0f to baseBackgroundColor.copy(alpha = if (isDark) 0.72f else 0.80f)
                        )
                    )
                )
        )

        // Tighter vertical fade veil at the bottom edge behind the floating pill
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.40f to baseBackgroundColor.copy(alpha = 0.10f),
                            0.70f to baseBackgroundColor.copy(alpha = 0.36f),
                            0.90f to baseBackgroundColor.copy(alpha = 0.62f),
                            1.0f to baseBackgroundColor.copy(alpha = 0.80f)
                        )
                    )
                )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun VideoPulseBottomBarPreview() {
    var selected by remember { mutableStateOf(NavDestination.MY_SPACE) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Color(0xFF060608)),
        contentAlignment = Alignment.BottomCenter
    ) {
        BottomEdgeFadeInGlass(
            height = 112.dp,
            baseBackgroundColor = Color(0xFF060608),
            frostedSurfaceColor = Color(0xFF141418),
            isDark = true
        )
        VideoPulseBottomBar(
            currentDestination = selected,
            onDestinationSelected = { selected = it }
        )
    }
}
