package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.VideoBookmarkGroup
import com.example.data.model.LocalDeviceVideo
import com.example.data.model.MediaItem
import com.example.data.preferences.VideoProgressInfo
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.aura

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySpaceWatchlistSheet(
    savedItems: List<MediaItem>,
    thumbMap: Map<String, Bitmap?>,
    isArabic: Boolean,
    onPlayMedia: (MediaItem) -> Unit,
    onRemoveSaved: (MediaItem) -> Unit,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = aura.dialogBg,
        contentColor = aura.textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "قائمتي المحفوظة" else "Saved Watchlist",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = bodyFont,
                            fontWeight = FontWeight.Black,
                            color = aura.textPrimary
                        )
                    )
                    Text(
                        text = if (isArabic) "${savedItems.size} عنصر" else "${savedItems.size} saved titles",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = bodyFont,
                            color = aura.textSecondary
                        )
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isArabic) "إغلاق" else "Close",
                        tint = aura.textSecondary
                    )
                }
            }

            if (savedItems.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = aura.surfaceSubtle,
                    border = BorderStroke(1.dp, aura.borderMuted),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = aura.accentCyan,
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = if (isArabic) "قائمتك فارغة حالياً" else "Your Watchlist is empty",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = bodyFont,
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (isArabic) {
                                "اضغط على زر الحفظ في المشغل لإضافة عناوينك المفضلة."
                            } else {
                                "Tap Save on any title to keep your favorites ready here."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = bodyFont,
                                color = aura.textSecondary
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(savedItems, key = { it.id }) { item ->
                        val bmp = thumbMap[item.id]
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = aura.surfaceCard,
                            border = BorderStroke(1.dp, aura.borderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    onDismiss()
                                    onPlayMedia(item)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(88.dp)
                                        .aspectRatio(16f / 10f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(aura.surfaceSubtle)
                                ) {
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            filterQuality = FilterQuality.High,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(
                                                id = if (item.posterRes != 0) item.posterRes else R.drawable.img_hero_poster
                                            ),
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) item.titleArabic ?: item.title else item.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = bodyFont,
                                            color = aura.textPrimary,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.qualityTag} • ${item.seasonsOrDuration}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = bodyFont,
                                            color = aura.textSecondary,
                                            fontSize = 11.5.sp
                                        ),
                                        maxLines = 1
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveSaved(item) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = if (isArabic) "إزالة من القائمة" else "Remove from Watchlist",
                                        tint = aura.accentCyan
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySpaceHistorySheet(
    history: List<VideoProgressInfo>,
    isArabic: Boolean,
    onResumeEntry: (VideoProgressInfo) -> Unit,
    onRemoveEntry: (VideoProgressInfo) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = aura.dialogBg,
        contentColor = aura.textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "سجل المشاهدة" else "Watch History",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = bodyFont,
                            fontWeight = FontWeight.Black,
                            color = aura.textPrimary
                        )
                    )
                    Text(
                        text = if (isArabic) "${history.size} سجل حديث" else "${history.size} recent entries",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = bodyFont,
                            color = aura.textSecondary
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (history.isNotEmpty()) {
                        TextButton(
                            onClick = onClearAll,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text(
                                text = if (isArabic) "مسح الكل" else "Clear all",
                                fontFamily = bodyFont,
                                color = aura.accentFlame,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isArabic) "إغلاق" else "Close",
                            tint = aura.textSecondary
                        )
                    }
                }
            }

            if (history.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = aura.surfaceSubtle,
                    border = BorderStroke(1.dp, aura.borderMuted),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = aura.accentViolet,
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = if (isArabic) "لا يوجد سجل مشاهدة بعد" else "No playback history yet",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = bodyFont,
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(history, key = { "${it.id}_${it.uriString ?: ""}" }) { entry ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = aura.surfaceCard,
                            border = BorderStroke(1.dp, aura.borderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    onDismiss()
                                    onResumeEntry(entry)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(aura.accentViolet.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = if (isArabic) "استئناف" else "Resume",
                                        tint = aura.accentViolet
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = formatCleanTitle(entry.title, true),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = bodyFont,
                                            color = aura.textPrimary,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    LinearProgressIndicator(
                                        progress = { entry.progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = aura.accentCyan,
                                        trackColor = aura.progressTrack,
                                        strokeCap = StrokeCap.Round
                                    )
                                    Text(
                                        text = "${entry.progressPercent}% • ${formatTimeMs(entry.positionMs)} / ${formatTimeMs(entry.durationMs)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            color = aura.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveEntry(entry) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = if (isArabic) "حذف السجل" else "Remove entry",
                                        tint = aura.textTertiary
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySpaceOfflineMediaSheet(
    localVideos: List<LocalDeviceVideo>,
    bookmarkGroups: List<VideoBookmarkGroup>,
    isArabic: Boolean,
    onPlayLocalVideo: (LocalDeviceVideo) -> Unit,
    onPickLocalVideo: () -> Unit,
    onOpenWatchHub: () -> Unit,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = aura.dialogBg,
        contentColor = aura.textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "الوسائط المحلية والمجموعات" else "Offline Media & Collections",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = bodyFont,
                            fontWeight = FontWeight.Black,
                            color = aura.textPrimary
                        )
                    )
                    Text(
                        text = if (isArabic) {
                            "${localVideos.size} فيديو • ${bookmarkGroups.size} مجموعات علامات"
                        } else {
                            "${localVideos.size} device videos • ${bookmarkGroups.size} bookmarked sets"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = bodyFont,
                            color = aura.textSecondary
                        )
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isArabic) "إغلاق" else "Close",
                        tint = aura.textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onOpenWatchHub()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = aura.primaryCtaBg,
                        contentColor = aura.primaryCtaContent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "فتح مركز المشاهدة" else "Open Watch Hub",
                        fontFamily = bodyFont,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onPickLocalVideo,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, aura.borderSubtle),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (isArabic) "اختيار ملف" else "Pick File",
                        fontFamily = bodyFont,
                        color = aura.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (localVideos.isEmpty() && bookmarkGroups.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = aura.surfaceSubtle,
                    border = BorderStroke(1.dp, aura.borderMuted),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isArabic) {
                            "استخدم زر اختيار ملف لإضافة فيديوهاتك أو افتح مركز المشاهدة لمنح الإذن."
                        } else {
                            "Tap Pick File to open a local video directly or visit Watch Hub to scan folders."
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = bodyFont,
                            color = aura.textSecondary
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(localVideos.take(12), key = { it.id }) { video ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = aura.surfaceCard,
                            border = BorderStroke(1.dp, aura.borderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onDismiss()
                                    onPlayLocalVideo(video)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .aspectRatio(16f / 10f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(aura.surfaceSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bmp = video.bitmap
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = video.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = aura.accentEmerald
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = formatCleanTitle(video.title, true),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = bodyFont,
                                            color = aura.textPrimary,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${video.folderName} • ${video.durationText} • ${video.sizeText}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            color = aura.textSecondary,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1
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
