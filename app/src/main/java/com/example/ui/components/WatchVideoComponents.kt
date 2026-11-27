package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocalDeviceVideo
import com.example.data.util.EpisodeParser
import com.example.data.util.LocalMediaScanner
import com.example.data.util.ParsedEpisodeInfo
import com.example.ui.theme.TextDark
import com.example.ui.theme.aura
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

/**
 * Smooth back-and-forth (ping-pong) marquee for long single-line video titles.
 * When text fits inside its available width, renders statically with zero animation overhead.
 * When text overflows, gently pauses at both ends and glides smoothly left and right so viewers
 * can effortlessly read the entire title and episode number.
 */
@Composable
fun SmoothMarqueeTitleText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    var containerWidthPx by remember { mutableIntStateOf(0) }
    var textWidthPx by remember(text) { mutableIntStateOf(0) }
    val overflowPx = (textWidthPx - containerWidthPx).coerceAtLeast(0)
    val shouldAnimate = containerWidthPx > 0 && overflowPx > 2
    val scrollOffset = remember(text) { Animatable(0f) }

    val smoothGlideEasing = remember { CubicBezierEasing(0.37f, 0f, 0.63f, 1f) }
    val speedPxPerSec = remember(density) { with(density) { 42.dp.toPx() }.coerceAtLeast(30f) }

    LaunchedEffect(text, shouldAnimate, overflowPx) {
        scrollOffset.snapTo(0f)
        if (shouldAnimate) {
            val target = overflowPx.toFloat()
            val durationMs = ((target / speedPxPerSec) * 1000f)
                .roundToInt()
                .coerceIn(1400, 6800)
            while (isActive) {
                delay(1100L)
                scrollOffset.animateTo(
                    targetValue = target,
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = durationMs,
                        easing = smoothGlideEasing
                    )
                )
                delay(1050L)
                scrollOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = durationMs,
                        easing = smoothGlideEasing
                    )
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .onSizeChanged { size ->
                if (containerWidthPx != size.width) {
                    containerWidthPx = size.width
                }
            }
    ) {
        Text(
            text = text,
            style = style,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            onTextLayout = { result ->
                val measuredW = result.size.width
                if (textWidthPx != measuredW) {
                    textWidthPx = measuredW
                }
            },
            modifier = Modifier
                .wrapContentWidth(align = Alignment.Start, unbounded = true)
                .graphicsLayer {
                    if (shouldAnimate) {
                        val shift = scrollOffset.value.coerceIn(0f, overflowPx.toFloat())
                        translationX = if (isRtl) shift else -shift
                    }
                }
        )
    }
}

/**
 * Luxury obsidian-glass episode pill overlaid cleanly on video thumbnails.
 */
@Composable
fun LuxuryEpisodeThumbnailBadge(
    episodeInfo: ParsedEpisodeInfo,
    isArabic: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val aura = MaterialTheme.aura
    val label = remember(episodeInfo, isArabic) {
        episodeInfo.formatCompactBadge(isArabic = isArabic)
    }

    Surface(
        shape = RoundedCornerShape(if (compact) 6.dp else 8.dp),
        color = Color(0xD9070B14),
        border = BorderStroke(
            width = if (compact) 0.75.dp else 0.9.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    aura.accentGold.copy(alpha = 0.85f),
                    aura.accentCyan.copy(alpha = 0.8f)
                )
            )
        ),
        shadowElevation = if (compact) 2.dp else 4.dp,
        modifier = modifier.testTag("video_episode_badge_${episodeInfo.episodeNumber}")
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 5.dp else 7.dp,
                vertical = if (compact) 1.5.dp else 3.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 4.5.dp else 5.5.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(aura.accentGold, aura.accentCyan))
                    )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.45.sp,
                    fontSize = if (compact) 8.sp else 9.5.sp,
                    fontFeatureSettings = "tnum"
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun PinnedBadgePill(
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    Surface(
        shape = RoundedCornerShape(if (compact) 5.dp else 7.dp),
        color = Color(0xD90A101D),
        border = BorderStroke(0.9.dp, aura.accentGold.copy(alpha = 0.78f)),
        modifier = modifier.testTag("pinned_badge_pill")
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 4.dp else 6.dp,
                vertical = if (compact) 2.dp else 3.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = aura.accentGold,
                modifier = Modifier.size(if (compact) 10.dp else 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLongPressActionSheet(
    video: LocalDeviceVideo,
    isArabic: Boolean,
    hideFileExtensions: Boolean,
    isPinned: Boolean = false,
    onTogglePin: () -> Unit = {},
    onPlay: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onInfo: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val episodeInfo = remember(video.id, video.title, video.folderName, video.relativePath, video.uriString) {
        EpisodeParser.parseForVideo(video)
    }
    val cleanTitle = remember(video.title, hideFileExtensions) {
        formatCleanTitle(video.title, hideFileExtensions)
    }

    val sheetContainerColor = aura.surfaceCard.compositeOver(aura.background).copy(alpha = 0.95f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor,
        contentColor = aura.textPrimary,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(aura.borderSubtle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 18.dp)
                .testTag("video_long_press_sheet")
        ) {
            // Luxury Video Preview Header
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = aura.surfaceSubtle,
                border = BorderStroke(1.dp, aura.borderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(84.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0E1525))
                    ) {
                        if (video.bitmap != null) {
                            Image(
                                bitmap = video.bitmap.asImageBitmap(),
                                contentDescription = video.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                filterQuality = FilterQuality.High
                            )
                        } else if (video.thumbnailRes != 0) {
                            Image(
                                painter = painterResource(video.thumbnailRes),
                                contentDescription = video.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            SmartFallbackCover(
                                title = video.title,
                                aspectRatio = 1.55f,
                                showText = false,
                                gradientVariant = video.id.hashCode()
                            )
                        }
                        if (episodeInfo != null) {
                            LuxuryEpisodeThumbnailBadge(
                                episodeInfo = episodeInfo,
                                isArabic = isArabic,
                                compact = true,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        SmoothMarqueeTitleText(
                            text = cleanTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = aura.textPrimary,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildString {
                                if (episodeInfo != null) {
                                    append(episodeInfo.formatReadableLabel(isArabic))
                                    append(" • ")
                                }
                                append(video.durationText)
                                append(" • ")
                                append(video.sizeText)
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPlay()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("sheet_play_video_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(aura.primaryCtaBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = if (isArabic) "تشغيل" else "Play",
                                tint = aura.primaryCtaContent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            VideoActionRow(
                icon = Icons.Default.PushPin,
                title = if (isPinned) {
                    if (isArabic) "إلغاء التثبيت من الأعلى" else "Unpin from Top"
                } else {
                    if (isArabic) "تثبيت في الأعلى" else "Pin to Top"
                },
                subtitle = if (isPinned) {
                    if (isArabic) "إعادة الفيديو للترتيب العادي" else "Return video to normal order"
                } else {
                    if (isArabic) "إبقاء الفيديو دائماً في أعلى القائمة" else "Keep this video pinned at the top"
                },
                accent = aura.accentGold,
                testTag = "video_action_pin_to_top",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTogglePin()
                }
            )

            VideoActionRow(
                icon = Icons.Default.DriveFileRenameOutline,
                title = if (isArabic) "تعديل الاسم" else "Rename",
                subtitle = if (isArabic) "تغيير اسم ملف الفيديو" else "Change video file name",
                accent = aura.accentCyan,
                testTag = "video_action_rename",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onRename()
                }
            )

            VideoActionRow(
                icon = Icons.Default.DriveFileMove,
                title = if (isArabic) "نقل إلى مجلد" else "Move to Folder",
                subtitle = if (isArabic) "نقل الفيديو إلى مجلد آخر" else "Move video to another folder",
                accent = aura.accentGold,
                testTag = "video_action_move",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onMove()
                }
            )

            VideoActionRow(
                icon = Icons.Default.Share,
                title = if (isArabic) "مشاركة" else "Share",
                subtitle = if (isArabic) "إرسال الفيديو عبر التطبيقات" else "Send video via other apps",
                accent = aura.accentViolet,
                testTag = "video_action_share",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onShare()
                }
            )

            VideoActionRow(
                icon = Icons.Default.Info,
                title = if (isArabic) "معلومات حول الفيديو" else "Video Info",
                subtitle = if (isArabic) "الدقة، الحجم، المسار، والتفاصيل" else "Resolution, size, path & details",
                accent = aura.textPrimary,
                testTag = "video_action_info",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInfo()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = aura.borderSubtle
            )

            VideoActionRow(
                icon = Icons.Default.DeleteOutline,
                title = if (isArabic) "حذف الفيديو" else "Delete Video",
                subtitle = if (isArabic) "إزالة الملف من ذاكرة الجهاز" else "Remove file from device storage",
                accent = Color(0xFFFF5C77),
                isDestructive = true,
                testTag = "video_action_delete",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun VideoActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    testTag: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDestructive) Color(0x26FF5C77) else accent.copy(alpha = 0.14f)
                )
                .border(
                    0.8.dp,
                    if (isDestructive) Color(0x55FF5C77) else accent.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = if (isDestructive) Color(0xFFFF6E86) else aura.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = aura.textSecondary,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

data class WatchInlineActionItem(
    val icon: ImageVector,
    val label: String,
    val accent: Color,
    val testTag: String,
    val isDestructive: Boolean = false,
    val isHighlighted: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun WatchInlineSelectionActionBar(
    isArabic: Boolean,
    actions: List<WatchInlineActionItem>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = aura.surfaceCard,
        border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.55f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("watch_inline_selection_action_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClose()
                    }
                    .testTag("watch_inline_action_close"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = aura.surfaceSubtle,
                    border = BorderStroke(0.8.dp, aura.borderSubtle),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isArabic) "إلغاء التحديد" else "Clear selection",
                            tint = aura.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            actions.forEach { action ->
                WatchInlineActionButton(action = action)
            }
        }
    }
}

@Composable
private fun WatchInlineActionButton(
    action: WatchInlineActionItem
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val containerColor = when {
        action.isDestructive -> Color(0x26FF5C77)
        action.isHighlighted -> action.accent.copy(alpha = 0.20f)
        else -> aura.surfaceSubtle
    }
    val borderColor = when {
        action.isDestructive -> Color(0x66FF5C77)
        action.isHighlighted -> action.accent.copy(alpha = 0.65f)
        else -> action.accent.copy(alpha = 0.30f)
    }
    val textColor = when {
        action.isDestructive -> Color(0xFFFF6E86)
        action.isHighlighted -> action.accent
        else -> aura.textPrimary
    }

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                haptic.performHapticFeedback(
                    if (action.isDestructive) HapticFeedbackType.LongPress
                    else HapticFeedbackType.TextHandleMove
                )
                action.onClick()
            }
            .testTag(action.testTag),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = containerColor,
            border = BorderStroke(0.9.dp, borderColor)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.label,
                    tint = action.accent,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = action.label,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun RenameVideoDialog(
    video: LocalDeviceVideo,
    isArabic: Boolean,
    onConfirmRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    val dotIdx = remember(video.title) { video.title.lastIndexOf('.') }
    val baseName = remember(video.title) {
        if (dotIdx > 0) video.title.substring(0, dotIdx) else video.title
    }
    val ext = remember(video.title) {
        if (dotIdx > 0 && dotIdx < video.title.length - 1) video.title.substring(dotIdx) else ""
    }

    var inputName by remember(video.id) { mutableStateOf(baseName) }
    val sanitized = inputName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "")
    val isValid = sanitized.isNotEmpty()

    val dialogContainerColor = aura.surfaceCard.compositeOver(aura.background).copy(alpha = 0.95f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogContainerColor,
        titleContentColor = aura.textPrimary,
        textContentColor = aura.textSecondary,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DriveFileRenameOutline,
                    contentDescription = null,
                    tint = aura.accentCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isArabic) "تعديل اسم الفيديو" else "Rename Video",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    singleLine = true,
                    label = {
                        Text(if (isArabic) "الاسم الجديد" else "New name")
                    },
                    suffix = if (ext.isNotEmpty()) {
                        {
                            Text(
                                text = ext,
                                color = aura.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else null,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = aura.accentCyan,
                        unfocusedBorderColor = aura.borderSubtle,
                        focusedTextColor = aura.textPrimary,
                        unfocusedTextColor = aura.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_video_input")
                )
                if (!isValid) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) "يرجى إدخال اسم صالح بدون رموز ممنوعة." else "Enter a valid name without illegal characters.",
                        color = Color(0xFFFF6E86),
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmRename(sanitized + ext) },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = aura.accentCyan,
                    contentColor = if (aura.isDark) TextDark else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_rename_video_button")
            ) {
                Text(
                    text = if (isArabic) "حفظ" else "Save",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isArabic) "إلغاء" else "Cancel",
                    color = aura.textSecondary
                )
            }
        }
    )
}

@Composable
fun MoveVideoFolderDialog(
    video: LocalDeviceVideo,
    existingFolders: List<Pair<String, String?>>,
    isArabic: Boolean,
    onSelectFolder: (folderName: String, relativePath: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    var customFolderName by remember { mutableStateOf("") }
    var showCreateField by remember { mutableStateOf(false) }

    val dialogContainerColor = aura.surfaceCard.compositeOver(aura.background).copy(alpha = 0.95f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogContainerColor,
        titleContentColor = aura.textPrimary,
        textContentColor = aura.textSecondary,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DriveFileMove,
                        contentDescription = null,
                        tint = aura.accentGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "نقل إلى مجلد" else "Move to Folder",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                IconButton(
                    onClick = { showCreateField = !showCreateField },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = if (isArabic) "مجلد جديد" else "New folder",
                        tint = aura.accentCyan
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (showCreateField || existingFolders.isEmpty()) {
                    OutlinedTextField(
                        value = customFolderName,
                        onValueChange = { customFolderName = it },
                        singleLine = true,
                        label = {
                            Text(if (isArabic) "اسم المجلد الجديد (داخل Movies)" else "New folder name (in Movies)")
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = aura.accentCyan,
                            unfocusedBorderColor = aura.borderSubtle,
                            focusedTextColor = aura.textPrimary,
                            unfocusedTextColor = aura.textPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("move_new_folder_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val cleaned = customFolderName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "")
                    Button(
                        onClick = { onSelectFolder(cleaned, null) },
                        enabled = cleaned.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = aura.accentCyan,
                            contentColor = if (aura.isDark) TextDark else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_create_move_folder_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "نقل إلى المجلد الجديد" else "Move to New Folder",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (existingFolders.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = aura.borderSubtle)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (existingFolders.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(existingFolders) { (folderName, relPath) ->
                            val isCurrent = folderName.equals(video.folderName, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrent) aura.accentCyanSoft else aura.surfaceSubtle,
                                border = BorderStroke(
                                    1.dp,
                                    if (isCurrent) aura.accentCyan else aura.borderSubtle
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isCurrent) {
                                        onSelectFolder(folderName, relPath)
                                    }
                                    .testTag("move_target_folder_$folderName")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = if (isCurrent) aura.accentCyan else aura.accentGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = folderName,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                color = aura.textPrimary,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!relPath.isNullOrBlank()) {
                                            Text(
                                                text = relPath.trimEnd('/'),
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = aura.textSecondary,
                                                    fontSize = 10.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    if (isCurrent) {
                                        Text(
                                            text = if (isArabic) "الحالي" else "Current",
                                            color = aura.accentCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isArabic) "إغلاق" else "Close",
                    color = aura.textSecondary
                )
            }
        }
    )
}

@Composable
fun DeleteVideoConfirmationDialog(
    video: LocalDeviceVideo,
    isArabic: Boolean,
    hideFileExtensions: Boolean,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    val cleanTitle = formatCleanTitle(video.title, hideFileExtensions)
    val dialogContainerColor = aura.surfaceCard.compositeOver(aura.background).copy(alpha = 0.95f)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogContainerColor,
        titleContentColor = aura.textPrimary,
        textContentColor = aura.textSecondary,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = Color(0xFFFF5C77),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isArabic) "حذف الفيديو؟" else "Delete Video?",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Text(
                text = if (isArabic) {
                    "سيتم حذف \"$cleanTitle\" نهائياً من جهازك (${video.sizeText})."
                } else {
                    "\"$cleanTitle\" (${video.sizeText}) will be permanently removed from your device."
                },
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5C77),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_delete_video_button")
            ) {
                Text(
                    text = if (isArabic) "حذف" else "Delete",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, aura.borderSubtle),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isArabic) "إلغاء" else "Cancel",
                    color = aura.textPrimary
                )
            }
        }
    )
}

@Composable
fun VideoInfoDialog(
    details: LocalMediaScanner.VideoDetailsInfo,
    episodeInfo: ParsedEpisodeInfo?,
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    val dialogContainerColor = aura.surfaceCard.compositeOver(aura.background).copy(alpha = 0.95f)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogContainerColor,
        titleContentColor = aura.textPrimary,
        textContentColor = aura.textSecondary,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = aura.accentCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "معلومات الفيديو" else "Video Details",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isArabic) "إغلاق" else "Close",
                        tint = aura.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VideoInfoRow(
                    label = if (isArabic) "اسم الملف" else "File Name",
                    value = details.fileName
                )
                if (episodeInfo != null) {
                    VideoInfoRow(
                        label = if (isArabic) "رقم الحلقة" else "Episode",
                        value = episodeInfo.formatReadableLabel(isArabic),
                        highlight = aura.accentGold
                    )
                }
                VideoInfoRow(
                    label = if (isArabic) "المدة" else "Duration",
                    value = details.duration
                )
                VideoInfoRow(
                    label = if (isArabic) "الدقة" else "Resolution",
                    value = if (details.exactDimensions != null) {
                        "${details.resolution} (${details.exactDimensions})"
                    } else {
                        details.resolution
                    }
                )
                VideoInfoRow(
                    label = if (isArabic) "الحجم" else "File Size",
                    value = details.size
                )
                if (details.bitrateText != null) {
                    VideoInfoRow(
                        label = if (isArabic) "معدل البت" else "Bitrate",
                        value = details.bitrateText
                    )
                }
                VideoInfoRow(
                    label = if (isArabic) "الصيغة" else "Format",
                    value = details.mimeType
                )
                VideoInfoRow(
                    label = if (isArabic) "المجلد" else "Folder",
                    value = details.folderName
                )
                VideoInfoRow(
                    label = if (isArabic) "تاريخ الإضافة" else "Date Added",
                    value = details.dateAdded
                )
                VideoInfoRow(
                    label = if (isArabic) "المسار" else "Storage Path",
                    value = details.storagePath
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = aura.accentCyan,
                    contentColor = if (aura.isDark) TextDark else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("close_video_info_button")
            ) {
                Text(
                    text = if (isArabic) "حسناً" else "Done",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun VideoInfoRow(
    label: String,
    value: String,
    highlight: Color? = null
) {
    val aura = MaterialTheme.aura
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = aura.surfaceSubtle,
        border = BorderStroke(0.7.dp, aura.borderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = aura.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = highlight ?: aura.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderLongPressActionSheet(
    folderName: String,
    videoCount: Int,
    totalSizeText: String,
    leadVideo: LocalDeviceVideo?,
    leadThumbnail: androidx.compose.ui.graphics.ImageBitmap?,
    isArabic: Boolean,
    isPinned: Boolean,
    onOpen: () -> Unit,
    onTogglePin: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onInfo: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetContainerColor = aura.surfaceCard.compositeOver(aura.background).copy(alpha = 0.95f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor,
        contentColor = aura.textPrimary,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(aura.borderSubtle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 18.dp)
                .testTag("folder_long_press_sheet")
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = aura.surfaceSubtle,
                border = BorderStroke(1.dp, aura.borderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(84.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0E1525)),
                        contentAlignment = Alignment.Center
                    ) {
                        val leadBmp = leadThumbnail ?: leadVideo?.bitmap?.asImageBitmap()
                        if (leadBmp != null) {
                            Image(
                                bitmap = leadBmp,
                                contentDescription = folderName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                filterQuality = FilterQuality.High
                            )
                        } else {
                            SmartFallbackCover(
                                title = "",
                                subtitle = null,
                                icon = Icons.Default.Folder,
                                aspectRatio = 1.55f,
                                showText = false,
                                gradientVariant = folderName.hashCode()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        SmoothMarqueeTitleText(
                            text = folderName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = aura.textPrimary,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildString {
                                append(if (isArabic) "$videoCount فيديو" else "$videoCount videos")
                                if (totalSizeText.isNotBlank()) {
                                    append(" • ")
                                    append(totalSizeText)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpen()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("sheet_open_folder_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(aura.primaryCtaBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = if (isArabic) "فتح" else "Open",
                                tint = aura.primaryCtaContent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            VideoActionRow(
                icon = Icons.Default.PushPin,
                title = if (isPinned) {
                    if (isArabic) "إلغاء التثبيت من الأعلى" else "Unpin from Top"
                } else {
                    if (isArabic) "تثبيت في الأعلى" else "Pin to Top"
                },
                subtitle = if (isPinned) {
                    if (isArabic) "إعادة المجلد للترتيب العادي" else "Return folder to normal order"
                } else {
                    if (isArabic) "إبقاء المجلد دائماً في أعلى القائمة" else "Keep this folder pinned at the top"
                },
                accent = aura.accentGold,
                testTag = "folder_action_pin_to_top",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTogglePin()
                }
            )

            VideoActionRow(
                icon = Icons.Default.DriveFileRenameOutline,
                title = if (isArabic) "تعديل الاسم" else "Rename",
                subtitle = if (isArabic) "تغيير اسم المجلد" else "Change folder name",
                accent = aura.accentCyan,
                testTag = "folder_action_rename",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onRename()
                }
            )

            VideoActionRow(
                icon = Icons.Default.Share,
                title = if (isArabic) "مشاركة" else "Share",
                subtitle = if (isArabic) "مشاركة فيديوهات هذا المجلد" else "Share videos in this folder",
                accent = aura.accentViolet,
                testTag = "folder_action_share",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onShare()
                }
            )

            VideoActionRow(
                icon = Icons.Default.Info,
                title = if (isArabic) "معلومات حول المجلد" else "Folder Info",
                subtitle = if (isArabic) "العدد، الحجم الإجمالي، والمسار" else "Count, total size & path",
                accent = aura.textPrimary,
                testTag = "folder_action_info",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInfo()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = aura.borderSubtle
            )

            VideoActionRow(
                icon = Icons.Default.DeleteOutline,
                title = if (isArabic) "حذف المجلد" else "Delete Folder",
                subtitle = if (isArabic) "حذف جميع فيديوهات المجلد من الجهاز" else "Remove folder videos from storage",
                accent = Color(0xFFFF5C77),
                isDestructive = true,
                testTag = "folder_action_delete",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                }
            )
        }
    }
}

@Composable
fun RenameFolderDialog(
    currentFolderName: String,
    isArabic: Boolean,
    onConfirmRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    var inputName by remember(currentFolderName) { mutableStateOf(currentFolderName) }
    val sanitized = inputName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "")
    val isValid = sanitized.isNotEmpty()
    val dialogContainerColor = aura.surfaceCard.compositeOver(aura.background).copy(alpha = 0.95f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogContainerColor,
        titleContentColor = aura.textPrimary,
        textContentColor = aura.textSecondary,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DriveFileRenameOutline,
                    contentDescription = null,
                    tint = aura.accentCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isArabic) "تعديل اسم المجلد" else "Rename Folder",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    singleLine = true,
                    label = {
                        Text(if (isArabic) "الاسم الجديد للمجلد" else "New folder name")
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = aura.accentCyan,
                        unfocusedBorderColor = aura.borderSubtle,
                        focusedTextColor = aura.textPrimary,
                        unfocusedTextColor = aura.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_folder_input")
                )
                if (!isValid) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) "يرجى إدخال اسم صالح بدون رموز ممنوعة." else "Enter a valid name without illegal characters.",
                        color = Color(0xFFFF6E86),
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmRename(sanitized) },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = aura.accentCyan,
                    contentColor = if (aura.isDark) TextDark else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_rename_folder_button")
            ) {
                Text(
                    text = if (isArabic) "حفظ" else "Save",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isArabic) "إلغاء" else "Cancel",
                    color = aura.textSecondary
                )
            }
        }
    )
}

@Composable
fun DeleteFolderConfirmationDialog(
    folderName: String,
    videoCount: Int,
    isArabic: Boolean,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    val dialogContainerColor = aura.surfaceCard.compositeOver(aura.background).copy(alpha = 0.95f)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogContainerColor,
        titleContentColor = aura.textPrimary,
        textContentColor = aura.textSecondary,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = Color(0xFFFF5C77),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isArabic) "حذف المجلد؟" else "Delete Folder?",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Text(
                text = if (isArabic) {
                    "سيتم حذف مجلد \"$folderName\" وجميع الفيديوهات بداخله ($videoCount فيديو) نهائياً من جهازك."
                } else {
                    "Folder \"$folderName\" and all $videoCount video(s) inside it will be permanently removed from your device."
                },
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5C77),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_delete_folder_button")
            ) {
                Text(
                    text = if (isArabic) "حذف" else "Delete",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, aura.borderSubtle),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isArabic) "إلغاء" else "Cancel",
                    color = aura.textPrimary
                )
            }
        }
    )
}

@Composable
fun FolderInfoDialog(
    details: LocalMediaScanner.FolderDetailsInfo,
    isPinned: Boolean,
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    val dialogContainerColor = aura.surfaceCard.compositeOver(aura.background).copy(alpha = 0.95f)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogContainerColor,
        titleContentColor = aura.textPrimary,
        textContentColor = aura.textSecondary,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = aura.accentCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "معلومات المجلد" else "Folder Details",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isArabic) "إغلاق" else "Close",
                        tint = aura.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VideoInfoRow(
                    label = if (isArabic) "اسم المجلد" else "Folder Name",
                    value = details.folderName
                )
                VideoInfoRow(
                    label = if (isArabic) "عدد الفيديوهات" else "Videos Count",
                    value = if (isArabic) "${details.videoCount} فيديو" else "${details.videoCount} Videos"
                )
                VideoInfoRow(
                    label = if (isArabic) "الحجم الإجمالي" else "Total Size",
                    value = details.totalSizeText
                )
                VideoInfoRow(
                    label = if (isArabic) "التثبيت في الأعلى" else "Pin Status",
                    value = if (isPinned) {
                        if (isArabic) "مثبت في الأعلى" else "Pinned to Top"
                    } else {
                        if (isArabic) "غير مثبت" else "Not Pinned"
                    },
                    highlight = if (isPinned) aura.accentGold else null
                )
                VideoInfoRow(
                    label = if (isArabic) "المسار" else "Storage Path",
                    value = details.storagePath
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = aura.accentCyan,
                    contentColor = if (aura.isDark) TextDark else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("close_folder_info_button")
            ) {
                Text(
                    text = if (isArabic) "حسناً" else "Done",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
