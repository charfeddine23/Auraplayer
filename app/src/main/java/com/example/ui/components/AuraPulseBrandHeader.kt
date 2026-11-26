package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.aura

@Composable
fun AuraPulseBrandHeader(
    streakDays: Int,
    isArabic: Boolean = false,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val isFrozen = streakDays == 0

    val infiniteTransition = rememberInfiniteTransition(label = "aura_editorial_header")
    val auraPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("aura_pulse_brand_header"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle atmospheric radial glow behind top header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .drawBehind {
                    val primaryGlow = if (isFrozen) {
                        if (aura.isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
                    } else {
                        aura.accentCyan
                    }
                    val secondaryGlow = if (isFrozen) {
                        Color(0xFF38BDF8)
                    } else {
                        aura.accentViolet
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryGlow.copy(alpha = if (aura.isDark) 0.14f else 0.08f),
                                secondaryGlow.copy(alpha = if (aura.isDark) 0.08f else 0.04f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.25f, size.height * 0.4f),
                            radius = size.width * 0.65f
                        )
                    )
                }
        )

        // Clean Netflix-style Edge-to-Edge Editorial Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Start: Brand Wordmark + Concise Tagline
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFrozen) {
                                    if (aura.isDark) Color(0xFF80DEEA) else Color(0xFF0284C7)
                                } else {
                                    aura.accentCyan.copy(alpha = auraPulseAlpha)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VIDEO PULSE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 2.6.sp,
                            color = aura.textPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isFrozen) {
                        if (isArabic) "توقفت الهالة • شاهد 5د لإذابة الجليد" else "Aura paused • Watch 5m to thaw"
                    } else {
                        if (isArabic) "مشغل وسائط الفاخر مدعوم بالذكاء الإصطناعي" else "AI-Powered Luxury Media Player"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily,
                        color = aura.textSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // End: Minimal High-Contrast Status Pill
            if (isFrozen) {
                val iceTint = if (aura.isDark) Color(0xFF80DEEA) else Color(0xFF0369A1)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (aura.isDark) Color(0x2800E5FF) else Color(0x160284C7),
                    border = BorderStroke(1.dp, iceTint.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AcUnit,
                            contentDescription = if (isArabic) "مجمّد" else "Frozen",
                            tint = iceTint,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isArabic) "مجمّد • 0 يوم" else "FROZEN • 0D",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily,
                                color = if (aura.isDark) Color(0xFFE0F7FA) else Color(0xFF0C4A6E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.6.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = aura.accentCyanSoft,
                    border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.38f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(aura.accentCyan.copy(alpha = auraPulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "الهالة نشطة" else "AURA LIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily,
                                color = aura.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.8.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
