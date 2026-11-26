package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.HomeMimicItem
import com.example.data.model.LocalDeviceVideo
import com.example.data.model.MediaItem
import com.example.data.model.MediaRepository
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.aura
import kotlin.math.absoluteValue

enum class HomeCategoryFilter(val labelEn: String, val labelAr: String) {
    ALL("All", "الكل"),
    LOCAL("Offline", "محلي"),
    SERIES("Series", "مسلسلات"),
    MOVIES("Movies", "أفلام"),
    SPORTS("Sports", "رياضة"),
    MUSIC("Music", "موسيقى")
}

data class HomeSpotlightEntry(
    val mediaItem: MediaItem,
    val bitmap: Bitmap? = null,
    val progress: Float = 0f,
    val isResume: Boolean = false,
    val eyebrowEn: String,
    val eyebrowAr: String
)

data class HomeContinueEntry(
    val mediaItem: MediaItem,
    val title: String,
    val progress: Float,
    val remainingLabel: String,
    val bitmap: Bitmap? = null,
    val isLocal: Boolean = false
)

fun allCatalogMediaItems(): List<MediaItem> {
    val combined = listOf(MediaRepository.heroItem) +
        MediaRepository.pulseOriginals +
        MediaRepository.trendingNow +
        MediaRepository.continueWatchingList +
        MediaRepository.sportsAndLiveList
    return combined.distinctBy { it.id }
}

fun LocalDeviceVideo.asHomeMediaItem(): MediaItem {
    return MediaItem(
        id = id,
        title = formatCleanTitle(title, true),
        titleArabic = formatCleanTitle(title, true),
        subtitleBadge = "OFFLINE • LOCAL",
        episodesFreeBadge = resolutionText,
        year = dateAddedText,
        seasonsOrDuration = durationText,
        maturityRating = "All",
        qualityTag = resolutionText,
        genresText = "$folderName • $sizeText",
        category = "Offline",
        matchScore = 100,
        description = "Offline media ready for instant hardware-accelerated playback.",
        posterRes = if (thumbnailRes != 0) thumbnailRes else R.drawable.img_hero_poster,
        backdropRes = if (thumbnailRes != 0) thumbnailRes else R.drawable.img_hero_poster,
        uriString = uriString
    )
}

@Composable
fun HomeTopBar(
    streakDays: Int,
    dailyProgress: Float,
    isSearchOpen: Boolean,
    onToggleSearch: () -> Unit,
    onOpenRoadmap: () -> Unit,
    onLongPressVault: () -> Unit,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val isFrozen = streakDays == 0

    val pulseTransition = rememberInfiniteTransition(label = "home_status_dot")
    val dotAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("aura_pulse_brand_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Minimal Wordmark + Living Aura Status Dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        (if (isFrozen) aura.accentCyan else aura.accentFlame).copy(alpha = dotAlpha)
                    )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "AURA PULSE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 2.4.sp,
                        color = aura.textPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isArabic) "السينما والمكتبة المحلية" else "CINEMA & OFFLINE HUB",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.2.sp,
                        color = aura.textTertiary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Compact Streak & Flame Pill (tap -> Roadmap, 1s long-press -> Vault)
            Surface(
                shape = RoundedCornerShape(50),
                color = aura.surfaceCard.copy(alpha = 0.9f),
                border = BorderStroke(
                    1.dp,
                    if (isFrozen) aura.accentCyan.copy(alpha = 0.4f)
                    else aura.accentFlame.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(50))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenRoadmap()
                            },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPressVault()
                            }
                        )
                    }
                    .testTag("home_streak_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { dailyProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = if (isFrozen) aura.accentCyan else aura.accentFlame,
                            trackColor = aura.progressTrack,
                            strokeWidth = 2.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Icon(
                            imageVector = if (isFrozen) Icons.Default.AcUnit else Icons.Default.LocalFireDepartment,
                            contentDescription = if (isArabic) "الشعلة" else "Streak",
                            tint = if (isFrozen) aura.accentCyan else aura.accentFlame,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "$streakDays ي" else "${streakDays}d",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Search Toggle Button (48dp touch target)
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleSearch()
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isSearchOpen) aura.accentCyanSoft else aura.surfaceCard.copy(alpha = 0.9f))
                    .border(
                        1.dp,
                        if (isSearchOpen) aura.accentCyan.copy(alpha = 0.6f) else aura.borderSubtle,
                        CircleShape
                    )
                    .testTag("home_search_toggle")
            ) {
                Icon(
                    imageVector = if (isSearchOpen) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isArabic) "بحث" else "Search",
                    tint = if (isSearchOpen) aura.accentCyan else aura.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun HomeSearchField(
    visible: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + expandVertically(tween(240, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = {
                Text(
                    text = if (isArabic) "ابحث في الأفلام، المسلسلات أو ملفاتك..." else "Search titles or offline videos...",
                    color = aura.textTertiary,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = aura.accentCyan,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isArabic) "مسح" else "Clear",
                            tint = aura.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = aura.accentCyan,
                unfocusedBorderColor = aura.borderSubtle,
                focusedContainerColor = aura.surfaceCard,
                unfocusedContainerColor = aura.surfaceSubtle,
                focusedTextColor = aura.textPrimary,
                unfocusedTextColor = aura.textPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .testTag("home_search_input")
        )
    }
}

@Composable
fun HomeHeroSpotlightPager(
    spotlights: List<HomeSpotlightEntry>,
    savedMediaIds: Set<String>,
    scrollParallaxProvider: () -> Float,
    onPlayClick: (MediaItem) -> Unit,
    onToggleSave: (MediaItem) -> Unit,
    onDetailsClick: (MediaItem) -> Unit,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    if (spotlights.isEmpty()) return

    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val pagerState = rememberPagerState(pageCount = { spotlights.size })

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_spotlight_pager")
        ) { page ->
            val entry = spotlights[page]
            val item = entry.mediaItem
            val isSaved = savedMediaIds.contains(item.id)
            val displayTitle = if (isArabic && !item.titleArabic.isNullOrBlank()) item.titleArabic else item.title
            val eyebrow = if (isArabic) entry.eyebrowAr else entry.eyebrowEn

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 580.dp)
                    .aspectRatio(0.84f)
                    .graphicsLayer {
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                        val scale = 1f - (pageOffset * 0.045f).coerceIn(0f, 0.06f)
                        scaleX = scale
                        scaleY = scale
                        alpha = 1f - (pageOffset * 0.25f).coerceIn(0f, 0.35f)
                    }
                    .clip(RoundedCornerShape(26.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                aura.accentCyan.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.10f),
                                aura.borderSubtle
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDetailsClick(item)
                    }
                    .testTag(if (page == 0) "hero_spotlight_card" else "hero_spotlight_card_$page"),
                shape = RoundedCornerShape(26.dp),
                color = aura.surfaceCard,
                shadowElevation = 14.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Artwork with subtle GPU parallax shift on scroll
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val scrollShift = scrollParallaxProvider()
                                translationY = scrollShift * 0.22f
                                val zoom = 1.04f + (scrollShift / 2400f).coerceIn(0f, 0.06f)
                                scaleX = zoom
                                scaleY = zoom
                            }
                    ) {
                        if (entry.bitmap != null) {
                            Image(
                                bitmap = entry.bitmap.asImageBitmap(),
                                contentDescription = displayTitle,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                filterQuality = FilterQuality.High
                            )
                        } else {
                            Image(
                                painter = painterResource(id = item.posterRes),
                                contentDescription = displayTitle,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Clean Netflix/TOD cinematic vignette scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.0f to Color(0x4005070C),
                                    0.42f to Color.Transparent,
                                    0.70f to Color(0xCC070A12),
                                    1.0f to Color(0xF7060910)
                                )
                            )
                    )

                    // Top subtle eyebrow pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xA60B0F19),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                        ) {
                            Text(
                                text = eyebrow,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = aura.accentCyan,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp
                                )
                            )
                        }

                        if (item.qualityTag.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x880B0F19)
                            ) {
                                Text(
                                    text = item.qualityTag,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.92f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp
                                    )
                                )
                            }
                        }
                    }

                    // Bottom Title, Metadata & Clean Action Bar
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = displayTitle,
                                fontFamily = if (isArabic && !item.titleArabic.isNullOrBlank()) CairoFontFamily else MaterialTheme.typography.headlineLarge.fontFamily,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 26.sp,
                                    lineHeight = 32.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${item.year}  •  ${item.seasonsOrDuration}  •  ${item.genresText}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.78f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (entry.isResume && entry.progress > 0f) {
                            LinearProgressIndicator(
                                progress = { entry.progress.coerceIn(0.02f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.5.dp)
                                    .clip(CircleShape),
                                color = aura.accentCyan,
                                trackColor = Color.White.copy(alpha = 0.24f)
                            )
                        }

                        // Primary Play + Save + Info Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onPlayClick(item)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF0A0C12)
                                ),
                                contentPadding = PaddingValues(horizontal = 18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag(if (page == 0) "hero_watch_now_button" else "hero_watch_now_button_$page")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        entry.isResume && isArabic -> "استئناف الآن"
                                        entry.isResume -> "Resume"
                                        isArabic -> "شاهد الآن"
                                        else -> "Play Now"
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                )
                            }

                            // + My List / Saved button (48dp touch target)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSaved) aura.accentCyan.copy(alpha = 0.22f) else Color(0x66161C28),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSaved) aura.accentCyan else Color.White.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onToggleSave(item)
                                    }
                                    .testTag(if (page == 0) "hero_add_list_button" else "hero_add_list_button_$page")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
                                        contentDescription = if (isArabic) "حفظ في قائمتي" else "Save to Watchlist",
                                        tint = if (isSaved) aura.accentCyan else Color.White,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                            }

                            // Details / Info button (48dp touch target)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0x66161C28),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onDetailsClick(item)
                                    }
                                    .testTag(if (page == 0) "hero_more_info_button" else "hero_more_info_button_$page")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
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

        if (spotlights.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(spotlights.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 20.dp else 6.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "hero_dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) aura.accentCyan else aura.textTertiary.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun HomeFilterChipsRow(
    selectedFilter: HomeCategoryFilter,
    onSelectFilter: (HomeCategoryFilter) -> Unit,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_filter_chips"),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(HomeCategoryFilter.entries, key = { it.name }) { filter ->
            val isSelected = filter == selectedFilter
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) aura.accentCyanSoft else aura.unselectedChipBg,
                animationSpec = tween(180),
                label = "chip_bg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) aura.accentCyan.copy(alpha = 0.8f) else aura.borderSubtle,
                animationSpec = tween(180),
                label = "chip_border"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) aura.accentCyan else aura.textSecondary,
                animationSpec = tween(180),
                label = "chip_text"
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = bgColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectFilter(filter)
                    }
                    .testTag("home_chip_${filter.name.lowercase()}")
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isArabic) filter.labelAr else filter.labelEn,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun HomeSectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                color = aura.textPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                letterSpacing = (-0.2).sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (actionLabel != null && onActionClick != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onActionClick()
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = aura.accentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = actionLabel,
                    tint = aura.accentCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun HomeContinueWatchingRail(
    entries: List<HomeContinueEntry>,
    onPlayClick: (MediaItem) -> Unit,
    onRemoveClick: ((HomeContinueEntry) -> Unit)? = null,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) return

    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_continue_watching_rail"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeSectionHeader(
            title = if (isArabic) "متابعة المشاهدة" else "Continue Watching"
        )

        LazyRow(
            state = listState,
            flingBehavior = snapBehavior,
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(entries, key = { it.mediaItem.id + (it.mediaItem.uriString ?: "") }) { entry ->
                Column(
                    modifier = Modifier
                        .width(214.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPlayClick(entry.mediaItem)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(aura.surfaceCard)
                            .border(1.dp, aura.borderSubtle, RoundedCornerShape(16.dp))
                    ) {
                        when {
                            entry.bitmap != null -> {
                                Image(
                                    bitmap = entry.bitmap.asImageBitmap(),
                                    contentDescription = entry.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    filterQuality = FilterQuality.High
                                )
                            }
                            entry.mediaItem.posterRes != 0 -> {
                                Image(
                                    painter = painterResource(id = entry.mediaItem.posterRes),
                                    contentDescription = entry.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                SmartFallbackCover(
                                    title = entry.title,
                                    showText = false,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0x99060910))
                                    )
                                )
                        )

                        // Center glass play button
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                                .border(1.dp, Color.White.copy(alpha = 0.65f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = if (isArabic) "تشغيل" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (onRemoveClick != null && entry.isLocal) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x99000000))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onRemoveClick(entry)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = if (isArabic) "إزالة" else "Remove",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // Sleek bottom progress bar
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .background(Color.White.copy(alpha = 0.25f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(entry.progress.coerceIn(0.03f, 1f))
                                    .background(aura.accentCyan)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = entry.remainingLabel,
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
fun HomePosterRail(
    title: String,
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    onSeeAllClick: (() -> Unit)? = null,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeSectionHeader(
            title = title,
            actionLabel = if (onSeeAllClick != null) (if (isArabic) "عرض الكل" else "See All") else null,
            onActionClick = onSeeAllClick
        )

        LazyRow(
            state = listState,
            flingBehavior = snapBehavior,
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            items(items, key = { it.id }) { item ->
                val displayTitle = if (isArabic && !item.titleArabic.isNullOrBlank()) item.titleArabic else item.title
                Column(
                    modifier = Modifier
                        .width(134.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onItemClick(item)
                        }
                        .testTag("media_card_${item.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.68f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(aura.surfaceCard)
                            .border(1.dp, aura.borderSubtle, RoundedCornerShape(16.dp))
                    ) {
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
                                        0.0f to Color.Transparent,
                                        0.68f to Color.Transparent,
                                        1.0f to Color(0xBF05070C)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${item.year} • ${item.seasonsOrDuration}",
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
fun HomeOfflineLibraryRail(
    localVideos: List<LocalDeviceVideo>,
    hasStoragePermission: Boolean,
    isArabic: Boolean,
    onPlayVideo: (LocalDeviceVideo) -> Unit,
    onGrantPermission: () -> Unit,
    onPickVideos: () -> Unit,
    onOpenWatchTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_offline_library_rail"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeSectionHeader(
            title = if (isArabic) "على جهازك (أوفلاين)" else "On This Device",
            actionLabel = if (isArabic) "المكتبة" else "Library",
            onActionClick = onOpenWatchTab
        )

        if (localVideos.isNotEmpty()) {
            LazyRow(
                state = listState,
                flingBehavior = snapBehavior,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                items(localVideos.take(15), key = { it.id }) { video ->
                    val cleanTitle = formatCleanTitle(video.title, true)
                    Column(
                        modifier = Modifier
                            .width(178.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPlayVideo(video)
                            }
                            .testTag("home_local_video_${video.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 10f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(aura.surfaceCard)
                                .border(1.dp, aura.borderSubtle, RoundedCornerShape(16.dp))
                        ) {
                            if (video.bitmap != null) {
                                Image(
                                    bitmap = video.bitmap.asImageBitmap(),
                                    contentDescription = cleanTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    filterQuality = FilterQuality.High
                                )
                            } else {
                                SmartFallbackCover(
                                    title = cleanTitle,
                                    showText = false,
                                    gradientVariant = video.id.hashCode(),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color(0x99060910))
                                        )
                                    )
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xCC060910),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = video.durationText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        Text(
                            text = cleanTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = aura.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${video.folderName} • ${video.sizeText}",
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
        } else {
            // Calm, minimal offline access card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = aura.surfaceCard.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, aura.borderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(aura.accentCyanSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasStoragePermission) Icons.Default.VideoLibrary else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = aura.accentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (!hasStoragePermission) {
                                    if (isArabic) "فيديوهات جهازك جاهزة للتشغيل" else "Access your offline videos"
                                } else {
                                    if (isArabic) "لا توجد فيديوهات مكتشفة بعد" else "Pick a video from your device"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (!hasStoragePermission) {
                                    if (isArabic) "اسمح بالوصول لعرض ملفات الفيديو المحلية تلقائياً" else "Grant media access or pick files directly for 100% offline playback"
                                } else {
                                    if (isArabic) "اختر أي ملف فيديو من الهاتف أو استعرض المجلدات" else "Open any local video file directly with instant resume"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!hasStoragePermission) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onGrantPermission()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = aura.accentCyan,
                                    contentColor = Color(0xFF070A12)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "منح الإذن" else "Grant Access",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPickVideos()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, aura.borderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = aura.accentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "اختيار فيديو" else "Pick Video",
                                color = aura.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeDailyAuraBar(
    equippedMimic: HomeMimicItem,
    streakDays: Int,
    dailyWatchSeconds: Int,
    dailyProgress: Float,
    unlockedLevel: Int,
    isGoalCompleted: Boolean,
    isArabic: Boolean,
    onOpenRoadmap: () -> Unit,
    onLongPressVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val curMin = dailyWatchSeconds / 60
    val curSec = dailyWatchSeconds % 60
    val accent = if (isGoalCompleted) aura.accentEmerald else aura.accentFlame

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = aura.surfaceCard.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, aura.borderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenRoadmap()
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPressVault()
                    }
                )
            }
            .testTag("roadmap_shortcut")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { dailyProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = accent,
                        trackColor = aura.progressTrack,
                        strokeWidth = 3.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isArabic) {
                            "هدف اليوم: ${String.format("%02d:%02d", curMin, curSec)} / 05:00"
                        } else {
                            "Daily Aura Goal • ${String.format("%02d:%02d", curMin, curSec)} / 05:00"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isArabic) {
                            "المستوى $unlockedLevel • ${equippedMimic.displayNameAr} • اضغط مطولاً للخزنة"
                        } else {
                            "Lvl $unlockedLevel • ${equippedMimic.displayNameEn} • Hold for Vault"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (isArabic) "فتح المسار" else "Open Roadmap",
                tint = aura.accentCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMediaDetailSheet(
    mediaItem: MediaItem?,
    isSaved: Boolean,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onPlayMedia: (MediaItem) -> Unit,
    onToggleSave: (MediaItem) -> Unit
) {
    if (mediaItem == null) return

    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val displayTitle = if (isArabic && !mediaItem.titleArabic.isNullOrBlank()) mediaItem.titleArabic else mediaItem.title

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = aura.dialogBg,
        dragHandle = null,
        modifier = Modifier.testTag("home_media_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
            ) {
                Image(
                    painter = painterResource(id = mediaItem.backdropRes),
                    contentDescription = displayTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x44000000), Color.Transparent, aura.dialogBg)
                            )
                        )
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isArabic) "إغلاق" else "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = displayTitle,
                    fontFamily = if (isArabic && !mediaItem.titleArabic.isNullOrBlank()) CairoFontFamily else MaterialTheme.typography.headlineSmall.fontFamily,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.Black
                    )
                )

                Text(
                    text = "${mediaItem.year}  •  ${mediaItem.seasonsOrDuration}  •  ${mediaItem.qualityTag}  •  ${mediaItem.maturityRating}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.accentCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                if (mediaItem.description.isNotBlank()) {
                    Text(
                        text = mediaItem.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = aura.textSecondary,
                            lineHeight = 20.sp
                        )
                    )
                }

                if (mediaItem.cast.isNotBlank()) {
                    Text(
                        text = if (isArabic) "البطولة: ${mediaItem.cast}" else "Cast: ${mediaItem.cast}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textTertiary,
                            fontSize = 11.5.sp
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                            onPlayMedia(mediaItem)
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
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "تشغيل الآن" else "Play Now",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleSave(mediaItem)
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isSaved) aura.accentCyan else aura.borderSubtle),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = if (isSaved) aura.accentCyan else aura.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSaved) {
                                if (isArabic) "محفوظ" else "Saved"
                            } else {
                                if (isArabic) "حفظ" else "Watchlist"
                            },
                            color = if (isSaved) aura.accentCyan else aura.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (mediaItem.episodes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isArabic) "الحلقات" else "Episodes",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    mediaItem.episodes.forEach { episode ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = aura.surfaceSubtle,
                            border = BorderStroke(1.dp, aura.borderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDismiss()
                                    onPlayMedia(
                                        mediaItem.copy(
                                            id = "${mediaItem.id}_ep_${episode.episodeNumber}",
                                            title = "${mediaItem.title} • ${episode.title}"
                                        )
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(aura.accentCyanSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = aura.accentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = episode.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = aura.textPrimary,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${episode.duration} • ${episode.description}",
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
            }
        }
    }
}
