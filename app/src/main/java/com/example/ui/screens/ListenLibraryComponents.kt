package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.LocalAudioTrack
import com.example.data.preferences.UserAudioPlaylist
import com.example.data.util.LocalMediaScanner
import com.example.ui.theme.aura
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

@Composable
internal fun LibraryTopUtilityBar(
    isArabic: Boolean,
    onBack: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = aura.surfaceCard,
            border = BorderStroke(0.8.dp, aura.borderSubtle)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("listen_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (isArabic) "رجوع" else "Back",
                    tint = aura.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = aura.surfaceCard,
            border = BorderStroke(0.8.dp, aura.borderSubtle)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onHistoryClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("listen_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = if (isArabic) "سجل الاستماع" else "Listening History",
                        tint = aura.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("listen_profile_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(aura.surfaceSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = if (isArabic) "ملف المستمع" else "Listener Profile",
                            tint = aura.textSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SoundwaveArcsEmblem(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = (h * 0.14f).coerceAtLeast(2.4f)

        val arcs = listOf(
            Triple(0.06f, 0.24f, 0.16f),
            Triple(0.14f, 0.50f, 0.13f),
            Triple(0.24f, 0.76f, 0.10f)
        )

        arcs.forEach { (insetFactor, yFactor, curveDepth) ->
            val startX = w * insetFactor
            val endX = w * (1f - insetFactor)
            val yBase = h * yFactor
            val path = Path().apply {
                moveTo(startX, yBase + h * curveDepth * 0.45f)
                quadraticBezierTo(
                    w * 0.5f,
                    yBase - h * curveDepth,
                    endX,
                    yBase + h * curveDepth * 0.45f
                )
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
internal fun ListeningExperienceCarousel(
    isArabic: Boolean,
    minutesListened: Long,
    totalPlays: Int,
    memberSince: String,
    topTrack: LocalAudioTrack,
    topTrackPlays: Int,
    libraryTrackCount: Int,
    onTopSongClick: () -> Unit,
    onStatsCardClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    val numberFmt = NumberFormat.getIntegerInstance(Locale.US)
    val minutesFormatted = numberFmt.format(minutesListened.coerceAtLeast(0L))
    val playsFormatted = numberFmt.format(totalPlays.coerceAtLeast(0))

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Card 1: Minutes Listened & Listener Badge
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = aura.surfaceCard,
                border = BorderStroke(0.9.dp, aura.borderSubtle),
                modifier = Modifier
                    .width(296.dp)
                    .height(198.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(onClick = onStatsCardClick)
                    .testTag("listen_stats_minutes_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    aura.ambientPrimary.copy(alpha = if (aura.isDark) 0.24f else 0.16f),
                                    aura.surfaceElevated,
                                    aura.surfaceCard
                                ),
                                start = Offset(120f, 0f),
                                end = Offset(720f, 520f)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = if (isArabic) "تجربة الاستماع الخاصة بك" else "YOUR LISTENING EXPERIENCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = aura.textSecondary,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 1.1.sp,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SoundwaveArcsEmblem(
                                color = aura.accentCyan,
                                modifier = Modifier.size(width = 34.dp, height = 26.dp)
                            )
                        }

                        Column {
                            Text(
                                text = minutesFormatted,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isArabic) "دقيقة استماع" else "MINUTES LISTENED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = aura.textSecondary,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 1.1.sp,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "مستمع أورا بالس" else "AURAPULSE LISTENER",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp,
                                        fontSize = 12.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isArabic) "$playsFormatted تشغيل · 2026" else "$playsFormatted plays · 2026",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = aura.textSecondary,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isArabic) "عضو منذ" else "MEMBER SINCE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = aura.textTertiary,
                                        letterSpacing = 0.8.sp,
                                        fontSize = 9.sp
                                    )
                                )
                                Text(
                                    text = memberSince,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card 2: Top Song (peeking card from right)
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = aura.surfaceCard,
                border = BorderStroke(0.9.dp, aura.borderSubtle),
                modifier = Modifier
                    .width(280.dp)
                    .height(198.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(onClick = onTopSongClick)
                    .testTag("listen_stats_top_song_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    aura.ambientSecondary.copy(alpha = if (aura.isDark) 0.24f else 0.15f),
                                    aura.surfaceElevated,
                                    aura.surfaceCard
                                ),
                                start = Offset(80f, 0f),
                                end = Offset(720f, 520f)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "تجربة الاستماع الخاصة بك" else "YOUR LISTENING EXPERIENCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.textSecondary,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.1.sp,
                                fontSize = 11.sp
                            )
                        )

                        Column {
                            Text(
                                text = topTrack.title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 23.sp,
                                    lineHeight = 27.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic) "الأغنية الأكثر استماعاً" else "TOP SONG",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = aura.textSecondary,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 1.1.sp,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Column {
                            Text(
                                text = if (isArabic) "مستمع أورا بالس" else "AURAPULSE LISTENER",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 12.5.sp
                                )
                            )
                            Text(
                                text = if (isArabic) {
                                    "${topTrack.artist} · $topTrackPlays تشغيل"
                                } else {
                                    "${topTrack.artist} · $topTrackPlays plays"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Card 3: Library Summary Card
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = aura.surfaceCard,
                border = BorderStroke(0.9.dp, aura.borderSubtle),
                modifier = Modifier
                    .width(248.dp)
                    .height(198.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(onClick = onStatsCardClick)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    aura.accentCyanSoft,
                                    aura.surfaceElevated,
                                    aura.surfaceCard
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "مكتبة الصوت" else "YOUR AUDIO VAULT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.textSecondary,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.1.sp,
                                fontSize = 11.sp
                            )
                        )

                        Column {
                            Text(
                                text = libraryTrackCount.toString(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                )
                            )
                            Text(
                                text = if (isArabic) "مقطوعات متوفرة" else "TRACKS IN LIBRARY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = aura.textSecondary,
                                    letterSpacing = 1.1.sp,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Text(
                            text = if (isArabic) "معادل الصوت والمؤقت متاحان" else "Spatial Audio & EQ Studio",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = aura.textSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun LibraryMenuSection(
    isArabic: Boolean,
    localMusicCount: Int,
    cachedCount: Int,
    onLocalMusicClick: () -> Unit,
    onCachedClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    Column(modifier = Modifier.fillMaxWidth()) {
        LibraryMenuRow(
            icon = Icons.Default.Folder,
            title = if (isArabic) "الموسيقى المحلية" else "Local Music",
            count = localMusicCount,
            onClick = onLocalMusicClick,
            testTag = "listen_menu_local_music"
        )
        HorizontalDivider(
            modifier = Modifier.padding(start = 62.dp, end = 20.dp),
            thickness = 0.6.dp,
            color = aura.borderMuted
        )
        LibraryMenuRow(
            icon = Icons.AutoMirrored.Filled.FormatListBulleted,
            title = if (isArabic) "الأغاني المخزنة مؤقتاً" else "Cached songs",
            count = cachedCount,
            onClick = onCachedClick,
            testTag = "listen_menu_cached_songs"
        )
    }
}

@Composable
private fun LibraryMenuRow(
    icon: ImageVector,
    title: String,
    count: Int,
    onClick: () -> Unit,
    testTag: String
) {
    val aura = MaterialTheme.aura
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = aura.textPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = aura.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            ),
            modifier = Modifier.weight(1f)
        )
        if (count > 0) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = aura.textTertiary,
                    fontSize = 13.sp
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = aura.textTertiary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
internal fun LibraryPlaylistsSection(
    isArabic: Boolean,
    playlists: List<UserAudioPlaylist>,
    favoritesCount: Int,
    onNewPlaylistClick: () -> Unit,
    onImportOrToolsClick: () -> Unit,
    onOpenFavoritesPlaylist: () -> Unit,
    onOpenUserPlaylist: (UserAudioPlaylist) -> Unit
) {
    val aura = MaterialTheme.aura
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = if (isArabic) "قوائم التشغيل" else "Playlists",
            style = MaterialTheme.typography.titleLarge.copy(
                color = aura.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Card: '+' New Playlist
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = aura.surfaceCard,
                    border = BorderStroke(0.7.dp, aura.borderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.96f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onNewPlaylistClick)
                        .testTag("listen_new_playlist_card")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (isArabic) "إنشاء قائمة تشغيل" else "New playlist",
                            tint = aura.textPrimary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isArabic) "قائمة جديدة" else "New playlist",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = if (isArabic) "إنشاء قائمة مخصصة" else "Create a custom mix",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.textTertiary,
                        fontSize = 11.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Right Card: Wave Emblem Badge (Import Audio / Audio Studio Hub)
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = aura.surfaceCard,
                    border = BorderStroke(0.7.dp, aura.borderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.96f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onImportOrToolsClick)
                        .testTag("listen_import_playlist_card")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(aura.textPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            SoundwaveArcsEmblem(
                                color = aura.background,
                                modifier = Modifier.size(width = 28.dp, height = 22.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isArabic) "استيراد ملفات الصوت" else "Import audio",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = if (isArabic) "إضافة من الهاتف واستوديو الصوت" else "Files, EQ & Sleep Timer",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.textTertiary,
                        fontSize = 11.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // Smart Favorites Row + User Created Playlists
        if (favoritesCount > 0 || playlists.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
        }

        if (favoritesCount > 0) {
            PlaylistSummaryRow(
                icon = Icons.Default.Favorite,
                iconTint = aura.accentFlame,
                title = if (isArabic) "المقاطع المفضلة" else "Favorites",
                subtitle = if (isArabic) "$favoritesCount مقطع" else "$favoritesCount tracks",
                onClick = onOpenFavoritesPlaylist
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        playlists.forEach { userPlaylist ->
            PlaylistSummaryRow(
                icon = Icons.Default.QueueMusic,
                iconTint = aura.accentCyan,
                title = userPlaylist.name,
                subtitle = if (isArabic) {
                    "${userPlaylist.trackIds.size} مقطع"
                } else {
                    "${userPlaylist.trackIds.size} tracks"
                },
                onClick = { onOpenUserPlaylist(userPlaylist) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PlaylistSummaryRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = aura.surfaceCard,
        border = BorderStroke(0.7.dp, aura.borderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(aura.surfaceSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.textSecondary,
                        fontSize = 12.sp
                    )
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = aura.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun FloatingMiniPlayerCircleButton(
    track: LocalAudioTrack,
    isPlaying: Boolean,
    isArabic: Boolean,
    onOpenFullPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val context = LocalContext.current
    var extractedBitmap by remember(track.id, track.audioUri, track.albumArtUri) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(track.id, track.audioUri, track.albumArtUri) {
        extractedBitmap = if (track.isFromDevice && (track.audioUri != null || track.albumArtUri != null)) {
            withContext(Dispatchers.IO) {
                LocalMediaScanner.extractAudioCoverArt(
                    context = context,
                    audioUriStr = track.audioUri,
                    albumArtUriStr = track.albumArtUri
                )
            }
        } else {
            null
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mini_player_disc")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_rotation"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "disc_pulse"
    )

    val actionLabel = if (isArabic) {
        "فتح مشغل الموسيقى الرئيسي: ${track.title}"
    } else {
        "Open full music player: ${track.title}"
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                aura.accentCyan.copy(alpha = 0.34f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Surface(
            shape = CircleShape,
            color = aura.surfaceElevated,
            border = BorderStroke(
                width = if (isPlaying) 2.dp else 1.2.dp,
                color = if (isPlaying) aura.accentCyan else aura.borderSubtle
            ),
            shadowElevation = 14.dp,
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .semantics { contentDescription = actionLabel }
                .clickable(
                    role = Role.Button,
                    onClickLabel = actionLabel,
                    onClick = onOpenFullPlayer
                )
                .testTag("listen_mini_player")
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp)
                        .clip(CircleShape)
                        .graphicsLayer {
                            rotationZ = if (isPlaying) discRotation else 0f
                        }
                ) {
                    when {
                        extractedBitmap != null && !extractedBitmap!!.isRecycled -> {
                            Image(
                                bitmap = extractedBitmap!!.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        track.albumArtUri != null -> {
                            AsyncImage(
                                model = track.albumArtUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        !track.isFromDevice -> {
                            Image(
                                painter = painterResource(id = track.albumCoverRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(aura.surfaceCard)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.56f),
                                    Color.Black.copy(alpha = 0.24f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isPlaying) aura.accentCyan else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

