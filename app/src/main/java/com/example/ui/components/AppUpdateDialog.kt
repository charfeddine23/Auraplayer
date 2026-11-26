package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.data.update.AppUpdateChecker
import com.example.data.update.AppUpdateInfo
import com.example.ui.theme.aura

private enum class UpdateModalPhase {
    PROMPT,
    ERROR
}

/**
 * Floating, compact modal App Update Dialog that notifies the user of new releases
 * and safely opens the official release page in the system browser (Google Play Protect compliant).
 */
@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    isArabic: Boolean = true,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val aura = MaterialTheme.aura

    var phase by remember { mutableStateOf(UpdateModalPhase.PROMPT) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        AppUpdateChecker.cleanupLegacyUpdateCache(context)
    }

    val openOfficialReleasePage: () -> Unit = {
        val opened = AppUpdateChecker.openReleasePage(context, updateInfo.releasePageUrl)
        if (opened) {
            onDismiss()
        } else {
            errorMessage = if (isArabic) {
                "تعذر فتح المتصفح لعرض صفحة التحديث."
            } else {
                "Unable to open browser for the release page."
            }
            phase = UpdateModalPhase.ERROR
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "update_rocket_anim")
    val rocketFloatY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rocket_float_y"
    )
    val ringGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_glow_alpha"
    )
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    val crimsonPrimary = Color(0xFFE50914)
    val crimsonBright = Color(0xFFFF2438)
    val crimsonDeep = Color(0xFFB70710)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 370.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    crimsonBright.copy(alpha = 0.22f * ringGlowAlpha),
                                    Color.Transparent
                                ),
                                radius = size.width * 0.7f
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.22f)
                        )
                    }
                    .border(
                        width = 1.1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                crimsonBright.copy(alpha = 0.5f),
                                aura.borderSubtle,
                                aura.borderMuted
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .testTag("app_update_dialog_card"),
                shape = RoundedCornerShape(28.dp),
                color = aura.dialogBg,
                shadowElevation = 24.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(aura.iconButtonBg)
                            .testTag("app_update_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isArabic) "إغلاق" else "Close",
                            tint = aura.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(116.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val outerR = size.minDimension / 2f

                                drawCircle(
                                    color = crimsonPrimary.copy(alpha = 0.14f * ringGlowAlpha),
                                    radius = outerR,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = Color(0xFF1F0C10),
                                    radius = outerR * 0.85f,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = crimsonBright.copy(alpha = 0.88f),
                                    radius = outerR * 0.74f,
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 2.2.dp.toPx())
                                )
                                drawCircle(
                                    color = Color(0xFF160B0E),
                                    radius = outerR * 0.71f,
                                    center = Offset(cx, cy)
                                )

                                drawRect(
                                    color = crimsonBright.copy(alpha = 0.85f),
                                    topLeft = Offset(cx - outerR * 0.42f, cy - outerR * 0.25f),
                                    size = androidx.compose.ui.geometry.Size(3.5.dp.toPx(), 3.5.dp.toPx())
                                )
                                drawRect(
                                    color = Color(0xFFFFFFFF),
                                    topLeft = Offset(cx + outerR * 0.42f, cy - outerR * 0.24f),
                                    size = androidx.compose.ui.geometry.Size(3.5.dp.toPx(), 3.5.dp.toPx())
                                )
                                drawRect(
                                    color = Color(0xFFFFC107),
                                    topLeft = Offset(cx - outerR * 0.36f, cy + outerR * 0.24f),
                                    size = androidx.compose.ui.geometry.Size(3.5.dp.toPx(), 3.5.dp.toPx())
                                )
                                drawRect(
                                    color = crimsonBright.copy(alpha = 0.75f),
                                    topLeft = Offset(cx + outerR * 0.36f, cy + outerR * 0.22f),
                                    size = androidx.compose.ui.geometry.Size(3.dp.toPx(), 3.dp.toPx())
                                )
                            }

                            when (phase) {
                                UpdateModalPhase.ERROR -> {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = crimsonBright,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                                UpdateModalPhase.PROMPT -> {
                                    Canvas(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .offset(y = rocketFloatY.dp)
                                    ) {
                                        val w = size.width
                                        val h = size.height
                                        val cx = w / 2f

                                        val flameHeight = h * 0.22f * flameScale
                                        val outerFlame = Path().apply {
                                            moveTo(cx - w * 0.08f, h * 0.67f)
                                            lineTo(cx, h * 0.67f + flameHeight)
                                            lineTo(cx + w * 0.08f, h * 0.67f)
                                            close()
                                        }
                                        drawPath(outerFlame, color = Color(0xFFFF6D00))

                                        val innerFlame = Path().apply {
                                            moveTo(cx - w * 0.045f, h * 0.67f)
                                            lineTo(cx, h * 0.67f + flameHeight * 0.68f)
                                            lineTo(cx + w * 0.045f, h * 0.67f)
                                            close()
                                        }
                                        drawPath(innerFlame, color = Color(0xFFFFD54F))

                                        val leftFin = Path().apply {
                                            moveTo(cx - w * 0.13f, h * 0.44f)
                                            lineTo(cx - w * 0.29f, h * 0.62f)
                                            lineTo(cx - w * 0.12f, h * 0.60f)
                                            close()
                                        }
                                        drawPath(leftFin, color = Color(0xFFD32F2F))

                                        val rightFin = Path().apply {
                                            moveTo(cx + w * 0.13f, h * 0.44f)
                                            lineTo(cx + w * 0.29f, h * 0.62f)
                                            lineTo(cx + w * 0.12f, h * 0.60f)
                                            close()
                                        }
                                        drawPath(rightFin, color = Color(0xFFD32F2F))

                                        drawRect(
                                            color = Color(0xFF546E7A),
                                            topLeft = Offset(cx - w * 0.085f, h * 0.62f),
                                            size = androidx.compose.ui.geometry.Size(w * 0.17f, h * 0.06f)
                                        )

                                        val bodyPath = Path().apply {
                                            moveTo(cx, h * 0.08f)
                                            cubicTo(
                                                cx + w * 0.18f, h * 0.25f,
                                                cx + w * 0.17f, h * 0.50f,
                                                cx + w * 0.11f, h * 0.63f
                                            )
                                            lineTo(cx - w * 0.11f, h * 0.63f)
                                            cubicTo(
                                                cx - w * 0.17f, h * 0.50f,
                                                cx - w * 0.18f, h * 0.25f,
                                                cx, h * 0.08f
                                            )
                                            close()
                                        }
                                        drawPath(bodyPath, color = Color(0xFFF8FAFC))

                                        val noseCone = Path().apply {
                                            moveTo(cx, h * 0.08f)
                                            cubicTo(
                                                cx + w * 0.10f, h * 0.17f,
                                                cx + w * 0.12f, h * 0.24f,
                                                cx + w * 0.12f, h * 0.26f
                                            )
                                            lineTo(cx - w * 0.12f, h * 0.26f)
                                            cubicTo(
                                                cx - w * 0.12f, h * 0.24f,
                                                cx - w * 0.10f, h * 0.17f,
                                                cx, h * 0.08f
                                            )
                                            close()
                                        }
                                        drawPath(noseCone, color = Color(0xFFD32F2F))

                                        drawCircle(
                                            color = Color(0xFF37474F),
                                            radius = w * 0.10f,
                                            center = Offset(cx, h * 0.40f)
                                        )
                                        drawCircle(
                                            color = Color(0xFF29E3FF),
                                            radius = w * 0.072f,
                                            center = Offset(cx, h * 0.40f)
                                        )
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.65f),
                                            radius = w * 0.022f,
                                            center = Offset(cx - w * 0.022f, h * 0.375f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = when (phase) {
                                UpdateModalPhase.PROMPT -> if (isArabic) "تحديث جديد متاح" else "New Update Available"
                                UpdateModalPhase.ERROR -> if (isArabic) "تعذر فتح الصفحة" else "Unable to Open Page"
                            },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = aura.textPrimary,
                                fontSize = 21.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isArabic) "إصدار رسمي جديد" else "New Official Release",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = crimsonBright,
                                fontSize = 14.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = aura.surfaceSubtle,
                            border = BorderStroke(0.8.dp, aura.borderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = aura.textSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = "→",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = crimsonBright,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = "v${updateInfo.versionName}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.5.sp
                                    )
                                )
                                val sizeStr = AppUpdateChecker.formatBytes(updateInfo.releaseSizeBytes)
                                if (sizeStr.isNotEmpty()) {
                                    Text(
                                        text = "• $sizeStr",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = aura.textSecondary,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        when (phase) {
                            UpdateModalPhase.PROMPT -> {
                                val notesText = updateInfo.releaseNotes
                                    .takeIf { it.isNotBlank() }
                                    ?: if (isArabic) {
                                        "تحديث جديد متاح يتضمن تحسينات في الأداء وإصلاحات للمشغل."
                                    } else {
                                        "A new version is available with performance and playback improvements."
                                    }

                                Text(
                                    text = notesText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = aura.textSecondary,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )

                                Spacer(modifier = Modifier.height(22.dp))

                                PrimaryCrimsonPillButton(
                                    label = if (isArabic) "تحديث" else "Update",
                                    crimsonBright = crimsonBright,
                                    crimsonPrimary = crimsonPrimary,
                                    crimsonDeep = crimsonDeep,
                                    testTag = "app_update_confirm_button",
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        openOfficialReleasePage()
                                    }
                                )
                            }

                            UpdateModalPhase.ERROR -> {
                                Text(
                                    text = errorMessage ?: (if (isArabic) "تعذر فتح رابط التحديث." else "Could not open update URL."),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = aura.accentDanger,
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                PrimaryCrimsonPillButton(
                                    label = if (isArabic) "إعادة المحاولة" else "Retry",
                                    crimsonBright = crimsonBright,
                                    crimsonPrimary = crimsonPrimary,
                                    crimsonDeep = crimsonDeep,
                                    testTag = "app_update_retry_button",
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        openOfficialReleasePage()
                                    }
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
private fun PrimaryCrimsonPillButton(
    label: String,
    crimsonBright: Color,
    crimsonPrimary: Color,
    crimsonDeep: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        shadowElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    listOf(crimsonBright, crimsonPrimary, crimsonDeep)
                )
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.5.sp
                )
            )
        }
    }
}
