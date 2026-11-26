package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MediaItem
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.aura
import com.example.ui.theme.stableStatusBarsPadding
import java.util.Locale

enum class HomeFilterCategory(val labelEn: String, val labelAr: String) {
    ALL("All", "الكل"),
    SERIES("Series", "مسلسلات"),
    MOVIES("Movies", "أفلام"),
    SPORTS("Sports & Live", "رياضة ومباشر"),
    MY_LIST("My List", "قائمتي")
}

data class HomeContinueItem(
    val mediaItem: MediaItem,
    val title: String,
    val subtitle: String,
    val progress: Float,
    val bitmap: Bitmap? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeTopBar(
    streakDays: Int,
    dailyProgress: Float,
    scrollFraction: Float,
    isArabic: Boolean,
    onSearchClick: () -> Unit,
    onAuraClick: () -> Unit,
    onVaultLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val isFrozen = streakDays == 0

    val infiniteTransition = rememberInfiniteTransition(label = "home_brand_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brand_dot_alpha"
    )

    val topBgColor = aura.background.copy(alpha = (scrollFraction * 0.94f).coerceIn(0f, 0.94f))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        aura.background.copy(alpha = (0.72f + scrollFraction * 0.24f).coerceIn(0f, 0.96f)),
                        topBgColor,
                        topBgColor.copy(alpha = scrollFraction * 0.85f)
                    )
                )
            )
            .stableStatusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("aura_pulse_brand_header")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Minimal Editorial Brand Wordmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        aura.accentCyan.copy(alpha = 0.55f * dotAlpha),
                                        Color.Transparent
                                    ),
                                    radius = size.minDimension * 1.9f
                                )
                            )
                        }
                        .clip(CircleShape)
                        .background(aura.accentCyan)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "VIDEO PULSE",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 2.2.sp,
                    color = aura.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right Action Cluster: Aura/Streak Pill + Search
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val pillAccent = if (isFrozen) aura.accentCyan else aura.accentFlame
                Surface(
                    shape = CircleShape,
                    color = aura.surfaceCard.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, pillAccent.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .height(40.dp)
                        .clip(CircleShape)
                        .combinedClickable(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onAuraClick()
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onVaultLongPress()
                            }
                        )
                        .testTag("home_aura_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(22.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = aura.progressTrack,
                                strokeWidth = 2.dp,
                                strokeCap = StrokeCap.Round
                            )
                            CircularProgressIndicator(
                                progress = { dailyProgress.coerceIn(0.05f, 1f) },
                                modifier = Modifier.fillMaxSize(),
                                color = pillAccent,
                                strokeWidth = 2.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Icon(
                                imageVector = if (isFrozen) Icons.Default.AcUnit else Icons.Default.LocalFireDepartment,
                                contentDescription = if (isArabic) "حالة الهالة" else "Aura Streak",
                                tint = pillAccent,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = "$streakDays",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = aura.textPrimary,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(aura.surfaceCard.copy(alpha = 0.85f))
                        .border(1.dp, aura.borderSubtle, CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSearchClick()
                        }
                        .testTag("header_search_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = if (isArabic) "بحث" else "Search",
                        tint = aura.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeCategoryPills(
    selectedCategory: HomeFilterCategory,
    myListCount: Int,
    isArabic: Boolean,
    onCategorySelected: (HomeFilterCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(HomeFilterCategory.entries) { category ->
            val isSelected = category == selectedCategory
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) aura.primaryCtaBg else aura.surfaceCard.copy(alpha = 0.72f),
                animationSpec = spring(),
                label = "cat_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) aura.primaryCtaContent else aura.textPrimary,
                animationSpec = spring(),
                label = "cat_text"
            )
            val borderColor = if (isSelected) aura.primaryCtaBg else aura.borderSubtle

            val baseLabel = if (isArabic) category.labelAr else category.labelEn
            val displayLabel = if (category == HomeFilterCategory.MY_LIST && myListCount > 0) {
                "$baseLabel ($myListCount)"
            } else {
                baseLabel
            }

            Surface(
                shape = CircleShape,
                color = bgColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .height(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCategorySelected(category)
                    }
                    .testTag("home_category_${category.name.lowercase()}")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = displayLabel,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeroCarousel(
    items: List<MediaItem>,
    savedIds: Set<String>,
    isArabic: Boolean,
    onWatchNow: (MediaItem) -> Unit,
    onToggleMyList: (MediaItem) -> Unit,
    onDetailsClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val pagerState = rememberPagerState(pageCount = { items.size })

    val infiniteTransition = rememberInfiniteTransition(label = "hero_live_dot")
    val liveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot_alpha"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
        ) { pageIndex ->
            val mediaItem = items[pageIndex]
            val isSaved = savedIds.contains(mediaItem.id)
            val primaryTitle = if (isArabic && !mediaItem.titleArabic.isNullOrBlank()) {
                mediaItem.titleArabic
            } else {
                mediaItem.title
            }
            val badgeText = if (isArabic && !mediaItem.subtitleBadgeArabic.isNullOrBlank()) {
                mediaItem.subtitleBadgeArabic
            } else {
                mediaItem.subtitleBadge
            }
            val durationText = if (isArabic && !mediaItem.seasonsOrDurationArabic.isNullOrBlank()) {
                mediaItem.seasonsOrDurationArabic
            } else {
                mediaItem.seasonsOrDuration
            }
            val genreText = if (isArabic && !mediaItem.genresTextArabic.isNullOrBlank()) {
                mediaItem.genresTextArabic
            } else {
                mediaItem.genresText
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.78f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.22f),
                                aura.borderSubtle,
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDetailsClick(mediaItem)
                    }
                    .testTag(if (pageIndex == 0) "hero_spotlight_card" else "hero_spotlight_card_$pageIndex"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0E17)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = mediaItem.posterRes),
                        contentDescription = primaryTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Netflix/TOD-style seamless cinematic vignette
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color(0x55000000),
                                        0.28f to Color.Transparent,
                                        0.56f to Color(0x80070A12),
                                        0.82f to Color(0xE6070A12),
                                        1.0f to Color(0xFF070A12)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Minimal Top Editorial Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (mediaItem.isLive) Color(0xE6DC2626) else Color(0xCC0D1320),
                                border = BorderStroke(
                                    1.dp,
                                    if (mediaItem.isLive) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.16f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    if (mediaItem.isLive) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = liveAlpha))
                                        )
                                    }
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xB30D1320),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
                            ) {
                                Text(
                                    text = mediaItem.qualityTag,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF00F2FE),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // Bottom Typography + Netflix/TOD Clean Action Bar
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = primaryTitle,
                                fontFamily = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = if (isArabic) 30.sp else 28.sp,
                                lineHeight = 36.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${mediaItem.year}  •  $durationText  •  $genreText  •  ${mediaItem.maturityRating}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.78f),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Clean 3-Button Action Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Primary Watch Now CTA
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1.35f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onWatchNow(mediaItem)
                                        }
                                        .testTag("hero_watch_now_button")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color(0xFF0A0D14),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isArabic) "شاهد الآن" else "Watch Now",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                color = Color(0xFF0A0D14),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Secondary My List Toggle
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0x33FFFFFF),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSaved) Color(0xFF00F2FE).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onToggleMyList(mediaItem)
                                        }
                                        .testTag("hero_add_list_button")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
                                            contentDescription = null,
                                            tint = if (isSaved) Color(0xFF00F2FE) else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (isSaved) {
                                                if (isArabic) "في قائمتي" else "Saved"
                                            } else {
                                                if (isArabic) "قائمتي" else "My List"
                                            },
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                color = if (isSaved) Color(0xFF00F2FE) else Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Circular Info / Details Button
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0x2BFFFFFF))
                                        .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onDetailsClick(mediaItem)
                                        }
                                        .testTag("hero_more_info_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = if (isArabic) "التفاصيل" else "Details",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Minimalist Pager Dots
        if (items.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(items.size) { index ->
                    val isActive = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isActive) 20.dp else 6.dp,
                        animationSpec = spring(),
                        label = "pager_dot_w"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isActive) aura.accentCyan else aura.textTertiary.copy(alpha = 0.45f)
                            )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePulseCompactBar(
    streakDays: Int,
    dailyWatchSeconds: Int,
    dailyProgress: Float,
    isGoalCompletedToday: Boolean,
    unlockedLevel: Int,
    isArabic: Boolean,
    onOpenAuraStudio: () -> Unit,
    onLongPressToVault: () -> Unit,
    onOpenRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val dailyAccent = if (isGoalCompletedToday) aura.accentEmerald else aura.accentFlame
    val curMin = dailyWatchSeconds / 60
    val curSec = dailyWatchSeconds % 60
    val timeText = String.format(Locale.US, "%02d:%02d / 05:00", curMin, curSec)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 620.dp)
            .padding(horizontal = 16.dp)
            .testTag("streak_status_card"),
        shape = RoundedCornerShape(18.dp),
        color = aura.surfaceCard.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, aura.borderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Daily Watch Ring + Streak Summary (Tap -> Mimic Studio, Hold -> Vault)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .combinedClickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenAuraStudio()
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPressToVault()
                        }
                    )
                    .padding(end = 8.dp)
                    .testTag("daily_watch_progress_card")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(38.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = aura.progressTrack,
                        strokeWidth = 3.dp,
                        strokeCap = StrokeCap.Round
                    )
                    CircularProgressIndicator(
                        progress = { dailyProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxSize(),
                        color = dailyAccent,
                        strokeWidth = 3.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Icon(
                        imageVector = if (isGoalCompletedToday) Icons.Default.CheckCircle else Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = dailyAccent,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isArabic) "الهدف اليومي • $timeText" else "Daily Pulse • $timeText",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { dailyProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.5.dp)
                            .clip(CircleShape),
                        color = dailyAccent,
                        trackColor = aura.progressTrack
                    )
                }
            }

            // Right: Roadmap Level Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = aura.accentCyanSoft,
                border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.4f)),
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenRoadmap()
                    }
                    .testTag("roadmap_shortcut")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "LVL $unlockedLevel • المسار" else "LVL $unlockedLevel",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = aura.accentCyan,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = if (isArabic) "فتح المسار" else "Open Roadmap",
                        tint = aura.accentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeSectionHeader(
    title: String,
    isArabic: Boolean,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = aura.textPrimary,
                fontSize = 18.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (onActionClick != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onActionClick()
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = actionLabel ?: if (isArabic) "عرض الكل" else "See All",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = aura.accentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = aura.accentCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ContinueWatchingRail(
    title: String,
    items: List<HomeContinueItem>,
    isArabic: Boolean,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        HomeSectionHeader(title = title, isArabic = isArabic)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.mediaItem.id }) { item ->
                Column(
                    modifier = Modifier
                        .width(214.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onItemClick(item.mediaItem)
                        }
                        .testTag("continue_card_${item.mediaItem.id}")
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .border(1.dp, aura.borderSubtle, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = aura.surfaceCard)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (item.bitmap != null) {
                                Image(
                                    bitmap = item.bitmap.asImageBitmap(),
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    filterQuality = FilterQuality.High
                                )
                            } else {
                                Image(
                                    painter = painterResource(
                                        id = if (item.mediaItem.backdropRes != 0) item.mediaItem.backdropRes else R.drawable.img_hero_poster
                                    ),
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color(0xAA070A12))
                                        )
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .background(Color(0x99000000))
                                    .border(1.dp, Color.White.copy(alpha = 0.65f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Thin sleek progress bar at the very bottom
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.5.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(Color(0x4DFFFFFF))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(item.progress.coerceIn(0.05f, 1f))
                                        .fillMaxHeight()
                                        .background(aura.accentCyan)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.subtitle,
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
    }
}

@Composable
fun TopTenRankedRail(
    title: String,
    items: List<MediaItem>,
    isArabic: Boolean,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        HomeSectionHeader(title = title, isArabic = isArabic)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(items, key = { _, item -> "top_${item.id}" }) { index, item ->
                val displayTitle = if (isArabic && !item.titleArabic.isNullOrBlank()) item.titleArabic else item.title
                Column(
                    modifier = Modifier
                        .width(154.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onItemClick(item)
                        }
                        .testTag("top_ranked_card_${index + 1}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp)
                    ) {
                        // Bold Rank Numeral on start edge (Netflix signature)
                        Text(
                            text = "${index + 1}",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 74.sp,
                            lineHeight = 74.sp,
                            color = aura.accentCyan.copy(alpha = 0.28f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(bottom = 2.dp)
                        )

                        Card(
                            modifier = Modifier
                                .width(118.dp)
                                .aspectRatio(0.68f)
                                .align(Alignment.CenterEnd)
                                .border(1.dp, aura.borderSubtle, RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = aura.surfaceCard)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = painterResource(id = item.posterRes),
                                    contentDescription = displayTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xB3070A12),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = item.qualityTag.substringBefore(" "),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 34.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LiveSportsBroadcastRail(
    title: String,
    items: List<MediaItem>,
    isArabic: Boolean,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "live_badge_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_badge_alpha"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        HomeSectionHeader(title = title, isArabic = isArabic)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { "live_${it.id}" }) { item ->
                val displayTitle = if (isArabic && !item.titleArabic.isNullOrBlank()) item.titleArabic else item.title
                val subtitle = if (isArabic && !item.genresTextArabic.isNullOrBlank()) item.genresTextArabic else item.genresText

                Card(
                    modifier = Modifier
                        .width(248.dp)
                        .aspectRatio(16f / 9.2f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, aura.borderSubtle, RoundedCornerShape(16.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onItemClick(item)
                        }
                        .testTag("live_sport_card_${item.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0E17))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = item.backdropRes),
                            contentDescription = displayTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x22000000), Color(0x77000000), Color(0xEE070A12))
                                    )
                                )
                        )

                        // Top-start LIVE badge
                        Surface(
                            shape = CircleShape,
                            color = Color(0xE6DC2626),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = dotAlpha))
                                )
                                Text(
                                    text = if (isArabic) "مباشر" else "LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.5.sp
                                    )
                                )
                            }
                        }

                        // Quality tag
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xB3070A12),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = item.qualityTag,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF00F2FE),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = displayTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.78f),
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StandardPosterRail(
    title: String,
    items: List<MediaItem>,
    isArabic: Boolean,
    onItemClick: (MediaItem) -> Unit,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        HomeSectionHeader(
            title = title,
            isArabic = isArabic,
            onActionClick = onSeeAllClick
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { "${title}_${it.id}" }) { item ->
                val displayTitle = if (isArabic && !item.titleArabic.isNullOrBlank()) item.titleArabic else item.title
                val duration = if (isArabic && !item.seasonsOrDurationArabic.isNullOrBlank()) {
                    item.seasonsOrDurationArabic
                } else {
                    item.seasonsOrDuration
                }

                Column(
                    modifier = Modifier
                        .width(128.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onItemClick(item)
                        }
                        .testTag("poster_card_${item.id}")
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.68f)
                            .border(1.dp, aura.borderSubtle, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = aura.surfaceCard)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = item.posterRes),
                                contentDescription = displayTitle,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Transparent, Color(0xB3070A12))
                                        )
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.year} • $duration",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            fontSize = 10.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun LocalDeviceVideosHomeBanner(
    videoCount: Int,
    isArabic: Boolean,
    onOpenWatchTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 620.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onOpenWatchTab()
            }
            .testTag("home_local_library_banner"),
        shape = RoundedCornerShape(18.dp),
        color = aura.surfaceCard.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, aura.borderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(aura.accentCyanSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = aura.accentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isArabic) "مكتبة فيديوهات جهازك" else "Your Device Video Library",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    )
                    Text(
                        text = if (videoCount > 0) {
                            if (isArabic) "$videoCount فيديو جاهز للتشغيل بلا إنترنت" else "$videoCount offline videos ready to play"
                        } else {
                            if (isArabic) "افتح تبويب Watch لتشغيل ملفات هاتفك" else "Open Watch tab to browse offline videos"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = aura.accentCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
