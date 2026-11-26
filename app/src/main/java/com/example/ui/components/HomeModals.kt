package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HomeMimicConfig
import com.example.data.model.MediaItem
import com.example.data.model.RoadmapXpState
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.aura
import com.example.ui.viewmodel.StreakUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailsBottomSheet(
    mediaItem: MediaItem,
    isSavedInMyList: Boolean,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onPlayClick: (MediaItem) -> Unit,
    onToggleMyList: (MediaItem) -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val title = if (isArabic && !mediaItem.titleArabic.isNullOrBlank()) mediaItem.titleArabic else mediaItem.title
    val description = if (isArabic && !mediaItem.descriptionArabic.isNullOrBlank()) mediaItem.descriptionArabic else mediaItem.description
    val duration = if (isArabic && !mediaItem.seasonsOrDurationArabic.isNullOrBlank()) mediaItem.seasonsOrDurationArabic else mediaItem.seasonsOrDuration
    val genres = if (isArabic && !mediaItem.genresTextArabic.isNullOrBlank()) mediaItem.genresTextArabic else mediaItem.genresText

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = aura.dialogBg,
        dragHandle = null,
        modifier = Modifier.testTag("media_details_bottom_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            item {
                // Widescreen Backdrop Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9.2f)
                ) {
                    Image(
                        painter = painterResource(id = mediaItem.backdropRes),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x55000000),
                                        Color.Transparent,
                                        aura.dialogBg.copy(alpha = 0.72f),
                                        aura.dialogBg
                                    )
                                )
                            )
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x99000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isArabic) "إغلاق" else "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (mediaItem.isLive) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xE6DC2626),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = if (isArabic) "بث مباشر" else "LIVE",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                ) {
                    Text(
                        text = title,
                        fontFamily = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = aura.textPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isArabic) "ملاءمة ${mediaItem.matchScore}%" else "${mediaItem.matchScore}% Match",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = aura.accentEmerald,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = "${mediaItem.year} • $duration • ${mediaItem.maturityRating}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                fontSize = 12.sp
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = aura.surfaceSubtle,
                            border = BorderStroke(1.dp, aura.borderSubtle)
                        ) {
                            Text(
                                text = mediaItem.qualityTag,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = aura.accentCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Play + Secondary My List
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = aura.primaryCtaBg,
                            modifier = Modifier
                                .weight(1.4f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDismiss()
                                    onPlayClick(mediaItem)
                                }
                                .testTag("details_play_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = aura.primaryCtaContent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "تشغيل الآن" else "Play Now",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = aura.primaryCtaContent,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = aura.surfaceSubtle,
                            border = BorderStroke(
                                1.dp,
                                if (isSavedInMyList) aura.accentCyan.copy(alpha = 0.6f) else aura.borderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleMyList(mediaItem)
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSavedInMyList) Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (isSavedInMyList) aura.accentCyan else aura.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isSavedInMyList) {
                                        if (isArabic) "في قائمتي" else "Saved"
                                    } else {
                                        if (isArabic) "قائمتي" else "My List"
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (isSavedInMyList) aura.accentCyan else aura.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }

                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = aura.textPrimary.copy(alpha = 0.9f),
                                lineHeight = 20.sp,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = genres,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.accentCyan,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    )

                    if (mediaItem.cast.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "البطولة: ${mediaItem.cast}" else "Cast: ${mediaItem.cast}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }

            if (mediaItem.episodes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(
                        color = aura.borderSubtle,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isArabic) "الحلقات (${mediaItem.episodes.size})" else "Episodes (${mediaItem.episodes.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(mediaItem.episodes, key = { it.episodeNumber }) { ep ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onDismiss()
                                onPlayClick(
                                    mediaItem.copy(
                                        title = "${mediaItem.title} • ${ep.title}",
                                        titleArabic = "${mediaItem.titleArabic ?: mediaItem.title} • ${ep.title}"
                                    )
                                )
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = aura.surfaceCard,
                        border = BorderStroke(1.dp, aura.borderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(aura.accentCyanSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = aura.accentCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ep.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = aura.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = ep.duration,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = aura.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = ep.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = aura.textSecondary,
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp
                                    ),
                                    maxLines = 2,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseAuraMimicSheet(
    uiState: StreakUiState,
    xpState: RoadmapXpState,
    flameShieldsCount: Int,
    homeMimicConfig: HomeMimicConfig,
    isPlayerActive: Boolean,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onNavigateToRoadmap: () -> Unit,
    onNavigateToVault: () -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = aura.dialogBg,
        modifier = Modifier.testTag("pulse_aura_mimic_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "استوديو الهالة والشعلة" else "Aura & Streak Studio",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = if (isArabic) "اضغط للتفاعل • اضغط مطولاً لفتح الخزنة" else "Tap centerpiece to pulse • Hold 1s for Vault",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            fontSize = 11.5.sp
                        )
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(aura.surfaceSubtle)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isArabic) "إغلاق" else "Close",
                        tint = aura.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (homeMimicConfig.isMimicEnabled) {
                HomeMimicCenterpieceEngine(
                    equippedMimic = homeMimicConfig.equippedMimic,
                    isMimicEnabled = true,
                    streakDays = uiState.streakDays,
                    dailyProgress = uiState.dailyProgress,
                    isPlayerActive = isPlayerActive,
                    onLongPressToVault = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                        onNavigateToVault()
                    },
                    isArabic = isArabic,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                FlameParticleComponent(
                    streakDays = uiState.streakDays,
                    onLongPressToRoadmap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                        onNavigateToVault()
                    },
                    isArabic = isArabic,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Compact Stats Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = aura.surfaceCard,
                    border = BorderStroke(1.dp, aura.borderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = aura.accentFlame,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.streakDays}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Black
                            )
                        )
                        Text(
                            text = if (isArabic) "أيام متواصلة" else "Streak Days",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.textSecondary,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = aura.surfaceCard,
                    border = BorderStroke(1.dp, aura.borderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = aura.accentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.shieldsCount}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Black
                            )
                        )
                        Text(
                            text = if (isArabic) "الدروع" else "Shields",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.textSecondary,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = aura.surfaceCard,
                    border = BorderStroke(1.dp, aura.borderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = aura.accentGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$flameShieldsCount",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Black
                            )
                        )
                        Text(
                            text = if (isArabic) "درع الشعلة" else "Flame Shield",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.textSecondary,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daily 5-Minute Goal Progress
            val curMin = uiState.dailyWatchSeconds / 60
            val curSec = uiState.dailyWatchSeconds % 60
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = aura.surfaceCard,
                border = BorderStroke(1.dp, aura.borderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "هدف المشاهدة اليومي (5 دقائق)" else "Daily 5-Min Watch Goal",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = String.format(Locale.US, "%02d:%02d / 05:00", curMin, curSec),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = aura.accentCyan,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { uiState.dailyProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (uiState.isGoalCompletedToday) aura.accentEmerald else aura.accentFlame,
                        trackColor = aura.progressTrack
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Navigation Buttons: Roadmap + Vault
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = aura.accentCyanSoft,
                    border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                            onNavigateToRoadmap()
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isArabic) "المسار • LVL ${xpState.unlockedLevel}" else "Roadmap • LVL ${xpState.unlockedLevel}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = aura.accentCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = aura.accentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = aura.surfaceSubtle,
                    border = BorderStroke(1.dp, aura.borderSubtle),
                    modifier = Modifier
                        .weight(0.75f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                            onNavigateToVault()
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = aura.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "الخزنة" else "Vault",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
