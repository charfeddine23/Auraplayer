package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.LocalAudioTrack
import com.example.data.preferences.UserAudioPlaylist
import com.example.data.util.LocalMediaScanner
import com.example.data.util.OnlineFolderCoverFetcher
import com.example.ui.components.LocalFloatingBottomBarPadding
import com.example.ui.theme.AuraAtmosphericLayer
import com.example.ui.theme.aura
import com.example.ui.theme.stableStatusBarsPadding
import com.example.ui.viewmodel.WatchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

private enum class LocalMusicTab(val labelEn: String, val labelAr: String) {
    SONGS("Songs", "الأغاني"),
    ARTISTS("Artists", "الفنانون"),
    ALBUMS("Albums", "الألبومات"),
    PLAYLISTS("Playlists", "القوائم"),
    FOLDERS("Folders", "المجلدات")
}

private enum class LocalMusicSort(val labelEn: String, val labelAr: String) {
    TITLE("Title (A-Z)", "الاسم (أ-ي)"),
    ARTIST("Artist", "الفنان"),
    DURATION("Duration", "المدة"),
    RECENT("Recently Added", "المضافة حديثاً")
}

@Composable
internal fun LocalMusicScreen(
    isArabic: Boolean,
    tracks: List<LocalAudioTrack>,
    mostListenedTrack: LocalAudioTrack?,
    currentTrackId: String,
    userPlaylists: List<UserAudioPlaylist>,
    favoriteIds: Set<String>,
    onBack: () -> Unit,
    onPlayTrack: (LocalAudioTrack) -> Unit,
    onShufflePlay: (List<LocalAudioTrack>) -> Unit,
    onOpenProfileStudio: () -> Unit,
    onScanDeviceMusic: () -> Unit,
    onOpenAudioFilePicker: () -> Unit,
    onOpenFavoritesPlaylist: () -> Unit,
    onOpenUserPlaylist: (UserAudioPlaylist) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var selectedTab by remember { mutableStateOf(LocalMusicTab.SONGS) }
    var selectedSort by remember { mutableStateOf(LocalMusicSort.TITLE) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    var activeFilterLabel by remember { mutableStateOf<String?>(null) }
    var activeFilterTracks by remember { mutableStateOf<List<LocalAudioTrack>?>(null) }

    BackHandler {
        when {
            activeFilterTracks != null -> {
                activeFilterTracks = null
                activeFilterLabel = null
            }
            isSearchVisible -> {
                isSearchVisible = false
                searchQuery = ""
            }
            else -> onBack()
        }
    }

    val baseTracks = activeFilterTracks ?: tracks

    val filteredSortedTracks = remember(baseTracks, searchQuery, selectedSort) {
        val q = searchQuery.trim()
        val filtered = if (q.isEmpty()) {
            baseTracks
        } else {
            baseTracks.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.artist.contains(q, ignoreCase = true) ||
                    it.album.contains(q, ignoreCase = true) ||
                    it.folderName.contains(q, ignoreCase = true)
            }
        }
        when (selectedSort) {
            LocalMusicSort.TITLE -> filtered.sortedBy { it.title.lowercase(Locale.getDefault()) }
            LocalMusicSort.ARTIST -> filtered.sortedBy { it.artist.lowercase(Locale.getDefault()) }
            LocalMusicSort.DURATION -> filtered.sortedByDescending { it.durationSeconds }
            LocalMusicSort.RECENT -> filtered.sortedByDescending { it.dateAddedMs }
        }
    }

    val totalSeconds = remember(baseTracks) {
        baseTracks.sumOf { it.durationSeconds.toLong() }
    }
    val durationFormatted = remember(totalSeconds, isArabic) {
        val hours = totalSeconds / 3600L
        val mins = (totalSeconds % 3600L) / 60L
        if (hours > 0L) {
            if (isArabic) "${hours}س ${mins}د" else "${hours}h ${mins}m"
        } else {
            if (isArabic) "${mins.coerceAtLeast(1)} دقيقة" else "${mins.coerceAtLeast(1)}m"
        }
    }

    val aura = MaterialTheme.aura
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(aura.background)
            .testTag("local_music_screen")
    ) {
        AuraAtmosphericLayer()

        // Subtle vertical gradient backdrop tinted by active AuraTheme
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            aura.ambientPrimary.copy(alpha = if (aura.isDark) 0.16f else 0.10f),
                            aura.background.copy(alpha = 0.65f),
                            Color.Transparent
                        )
                    )
                )
        )

        val bottomDockPadding = LocalFloatingBottomBarPadding.current
        val watchViewModel: WatchViewModel = viewModel()
        val watchSettings by watchViewModel.watchSettings.collectAsStateWithLifecycle()
        val cachedFolderCovers by watchViewModel.cachedFolderCovers.collectAsStateWithLifecycle()

        val musicFolderNames = remember(filteredSortedTracks) {
            filteredSortedTracks.map { it.folderName }.filter { it.isNotBlank() }.distinct()
        }
        LaunchedEffect(selectedTab, musicFolderNames, watchSettings.autoFetchOnlineFolderCovers) {
            if (selectedTab == LocalMusicTab.FOLDERS &&
                watchSettings.autoFetchOnlineFolderCovers &&
                musicFolderNames.isNotEmpty()
            ) {
                watchViewModel.syncFolderCovers(
                    folderNames = musicFolderNames,
                    isMusicFolder = true
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(bottom = bottomDockPadding + 96.dp)
        ) {
            // Hero Square Card showcasing cover art of the most-listened song
            item {
                Spacer(modifier = Modifier.stableStatusBarsPadding())
                Spacer(modifier = Modifier.height(62.dp))

                val heroTrack = mostListenedTrack ?: tracks.firstOrNull()
                val context = LocalContext.current
                var heroCoverBitmap by remember(heroTrack?.id, heroTrack?.audioUri, heroTrack?.albumArtUri) {
                    mutableStateOf<Bitmap?>(null)
                }
                LaunchedEffect(heroTrack?.id, heroTrack?.audioUri, heroTrack?.albumArtUri) {
                    heroCoverBitmap = if (heroTrack != null && (heroTrack.audioUri != null || heroTrack.albumArtUri != null)) {
                        withContext(Dispatchers.IO) {
                            LocalMediaScanner.extractAudioCoverArt(
                                context = context,
                                audioUriStr = heroTrack.audioUri,
                                albumArtUriStr = heroTrack.albumArtUri
                            )
                        }
                    } else {
                        null
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = aura.surfaceCard,
                        border = BorderStroke(0.9.dp, aura.borderSubtle),
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.58f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(enabled = heroTrack != null) {
                                heroTrack?.let {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onPlayTrack(it)
                                }
                            }
                            .testTag("local_music_hero_card")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                heroCoverBitmap != null && !heroCoverBitmap!!.isRecycled -> {
                                    Image(
                                        bitmap = heroCoverBitmap!!.asImageBitmap(),
                                        contentDescription = heroTrack?.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                heroTrack?.albumArtUri != null -> {
                                    AsyncImage(
                                        model = heroTrack.albumArtUri,
                                        contentDescription = heroTrack.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                heroTrack != null -> {
                                    Image(
                                        painter = painterResource(id = heroTrack.albumCoverRes),
                                        contentDescription = heroTrack.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = aura.accentCyan.copy(alpha = 0.72f),
                                        modifier = Modifier.size(88.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Title + Subtitle + Tracks / Duration summary
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = activeFilterLabel ?: if (isArabic) "الموسيقى المحلية" else "Local Music",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isArabic) "ملفات الصوت على الجهاز" else "Audio files on device",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = aura.textSecondary,
                            fontSize = 14.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isArabic) {
                            "${baseTracks.size} مقطع · $durationFormatted"
                        } else {
                            "${baseTracks.size} Tracks · $durationFormatted"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = aura.textTertiary,
                            fontSize = 12.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Play & Shuffle Action Pill Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = aura.primaryCtaBg,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                filteredSortedTracks.firstOrNull()?.let { onPlayTrack(it) }
                            }
                            .testTag("local_music_play_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = aura.primaryCtaContent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "تشغيل" else "Play",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = aura.primaryCtaContent,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = aura.surfaceCard,
                        border = BorderStroke(0.8.dp, aura.borderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onShufflePlay(filteredSortedTracks)
                            }
                            .testTag("local_music_shuffle_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = null,
                                tint = aura.textPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "عشوائي" else "Shuffle",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Segmented Capsule Filter Tabs (Songs | Artists | Albums | Playlists | Folders)
            item {
                Surface(
                    shape = CircleShape,
                    color = aura.surfaceSubtle,
                    border = BorderStroke(0.7.dp, aura.borderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LocalMusicTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab && activeFilterTracks == null
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) aura.accentCyanSoft else Color.Transparent,
                                border = if (isSelected) BorderStroke(0.8.dp, aura.accentCyan.copy(alpha = 0.5f)) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedTab = tab
                                        activeFilterTracks = null
                                        activeFilterLabel = null
                                    }
                                    .testTag("local_music_tab_${tab.name.lowercase()}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isArabic) tab.labelAr else tab.labelEn,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isSelected) aura.accentCyan else aura.textSecondary,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Expandable Search Bar when user clicks Search icon
            item {
                AnimatedVisibility(visible = isSearchVisible) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = aura.textSecondary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = aura.textSecondary
                                    )
                                }
                            }
                        },
                        placeholder = {
                            Text(
                                text = if (isArabic) "ابحث في المقاطع أو الفنانين..." else "Search songs, artists, albums...",
                                color = aura.textTertiary,
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = aura.textPrimary,
                            unfocusedTextColor = aura.textPrimary,
                            focusedBorderColor = aura.accentCyan,
                            unfocusedBorderColor = aura.borderSubtle,
                            focusedContainerColor = aura.surfaceCard,
                            unfocusedContainerColor = aura.surfaceCard
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                    )
                }
            }

            // Quick scan/open banner when no device tracks have been scanned yet
            if (tracks.none { it.isFromDevice }) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = aura.surfaceCard,
                        border = BorderStroke(0.7.dp, aura.borderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "لم يتم فحص ملفات الهاتف بعد" else "Scan or import device audio",
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = if (isArabic) "اضغط لفحص التخزين أو اختيار ملفات" else "Load MP3, M4A & FLAC files from storage",
                                    color = aura.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = onScanDeviceMusic,
                                    border = BorderStroke(1.dp, aura.borderSubtle),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "فحص" else "Scan",
                                        color = aura.textPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                                Button(
                                    onClick = onOpenAudioFilePicker,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = aura.primaryCtaBg,
                                        contentColor = aura.primaryCtaContent
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "فتح" else "Open",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tab Views
            when {
                selectedTab == LocalMusicTab.SONGS || activeFilterTracks != null -> {
                    val rows = filteredSortedTracks.chunked(2)
                    if (rows.isEmpty()) {
                        item {
                            LocalMusicEmptyState(
                                isArabic = isArabic,
                                onScanDeviceMusic = onScanDeviceMusic,
                                onOpenAudioFilePicker = onOpenAudioFilePicker
                            )
                        }
                    } else {
                        items(rows) { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                pair.forEach { track ->
                                    LocalSongGridCard(
                                        track = track,
                                        isCurrent = track.id == currentTrackId,
                                        onClick = { onPlayTrack(track) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                selectedTab == LocalMusicTab.ARTISTS -> {
                    val groupedArtists = filteredSortedTracks
                        .groupBy { it.artist }
                        .entries
                        .sortedBy { it.key.lowercase(Locale.getDefault()) }

                    items(groupedArtists) { (artistName, artistTracks) ->
                        LocalGroupRow(
                            title = artistName,
                            subtitle = if (isArabic) "${artistTracks.size} مقطع" else "${artistTracks.size} tracks",
                            leadingTrack = artistTracks.firstOrNull(),
                            fallbackIcon = Icons.Default.Person,
                            onClick = {
                                activeFilterLabel = artistName
                                activeFilterTracks = artistTracks
                            }
                        )
                    }
                }

                selectedTab == LocalMusicTab.ALBUMS -> {
                    val groupedAlbums = filteredSortedTracks
                        .groupBy { it.album }
                        .entries
                        .sortedBy { it.key.lowercase(Locale.getDefault()) }
                        .chunked(2)

                    items(groupedAlbums) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            pair.forEach { (albumName, albumTracks) ->
                                val rep = albumTracks.first()
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            activeFilterLabel = albumName
                                            activeFilterTracks = albumTracks
                                        }
                                ) {
                                    LocalAudioArtworkBox(
                                        track = rep,
                                        isHighlighted = false,
                                        fallbackIcon = Icons.Default.Album
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = albumName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = aura.textPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isArabic) {
                                            "${rep.artist} · ${albumTracks.size} مقاطع"
                                        } else {
                                            "${rep.artist} · ${albumTracks.size} tracks"
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = aura.textSecondary,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                selectedTab == LocalMusicTab.PLAYLISTS -> {
                    item {
                        LocalGroupRow(
                            title = if (isArabic) "المقاطع المفضلة" else "Favorites",
                            subtitle = if (isArabic) "${favoriteIds.size} مقطع" else "${favoriteIds.size} tracks",
                            leadingTrack = null,
                            fallbackIcon = Icons.Default.Favorite,
                            iconTint = aura.accentFlame,
                            onClick = onOpenFavoritesPlaylist
                        )
                    }
                    items(userPlaylists) { pl ->
                        LocalGroupRow(
                            title = pl.name,
                            subtitle = if (isArabic) "${pl.trackIds.size} مقطع" else "${pl.trackIds.size} tracks",
                            leadingTrack = null,
                            fallbackIcon = Icons.Default.QueueMusic,
                            onClick = { onOpenUserPlaylist(pl) }
                        )
                    }
                }

                selectedTab == LocalMusicTab.FOLDERS -> {
                    val groupedFolders = filteredSortedTracks
                        .groupBy { it.folderName }
                        .entries
                        .sortedBy { it.key.lowercase(Locale.getDefault()) }

                    items(groupedFolders) { (folderName, folderTracks) ->
                        val coverPath = if (watchSettings.autoFetchOnlineFolderCovers) {
                            cachedFolderCovers[OnlineFolderCoverFetcher.buildFolderCacheKey(folderName, true)]?.localFilePath
                        } else null
                        LocalGroupRow(
                            title = folderName,
                            subtitle = if (isArabic) "${folderTracks.size} مقطع" else "${folderTracks.size} tracks",
                            leadingTrack = folderTracks.firstOrNull(),
                            coverFilePath = coverPath,
                            fallbackIcon = Icons.Default.Folder,
                            onClick = {
                                activeFilterLabel = folderName
                                activeFilterTracks = folderTracks
                            }
                        )
                    }
                }
            }
        }

        // Top Floating Overlay Controls (compact 38.dp visual pills inside 48.dp touch targets)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .stableStatusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = aura.surfaceCard,
                    border = BorderStroke(0.8.dp, aura.borderSubtle),
                    modifier = Modifier.size(38.dp)
                ) {}
                IconButton(
                    onClick = {
                        if (activeFilterTracks != null) {
                            activeFilterTracks = null
                            activeFilterLabel = null
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("local_music_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = if (isArabic) "رجوع" else "Back",
                        tint = aura.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Box {
                Box(
                    modifier = Modifier.height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = aura.surfaceCard,
                        border = BorderStroke(0.8.dp, aura.borderSubtle),
                        modifier = Modifier
                            .matchParentSize()
                            .padding(vertical = 5.dp)
                    ) {}
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isSearchVisible = !isSearchVisible
                                if (!isSearchVisible) searchQuery = ""
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("local_music_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = if (isArabic) "بحث" else "Search",
                                tint = aura.textPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("local_music_sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = if (isArabic) "ترتيب وخيارات" else "Sort & Options",
                                tint = aura.textPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenProfileStudio,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("local_music_profile_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(aura.surfaceSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = if (isArabic) "ملف المستمع" else "Listener Profile",
                                    tint = aura.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(aura.dialogBg)
                ) {
                    LocalMusicSort.entries.forEach { sort ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isArabic) sort.labelAr else sort.labelEn,
                                    color = aura.textPrimary
                                )
                            },
                            trailingIcon = {
                                if (selectedSort == sort) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = aura.accentCyan
                                    )
                                }
                            },
                            onClick = {
                                selectedSort = sort
                                showSortMenu = false
                            }
                        )
                    }
                    HorizontalDivider(color = aura.borderMuted)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isArabic) "فحص موسيقى الهاتف" else "Scan device storage",
                                color = aura.textPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = aura.textPrimary
                            )
                        },
                        onClick = {
                            showSortMenu = false
                            onScanDeviceMusic()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isArabic) "فتح ملفات صوتية" else "Open audio files",
                                color = aura.textPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = aura.textPrimary
                            )
                        },
                        onClick = {
                            showSortMenu = false
                            onOpenAudioFilePicker()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalSongGridCard(
    track: LocalAudioTrack,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("local_song_card_${track.id}")
    ) {
        LocalAudioArtworkBox(
            track = track,
            isHighlighted = isCurrent
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isCurrent) aura.accentCyan else aura.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = track.artist,
            style = MaterialTheme.typography.bodySmall.copy(
                color = aura.textSecondary,
                fontSize = 12.5.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LocalAudioArtworkBox(
    track: LocalAudioTrack,
    isHighlighted: Boolean,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.MusicNote
) {
    val aura = MaterialTheme.aura
    val context = LocalContext.current
    var extractedBitmap by remember(track.id, track.audioUri, track.albumArtUri) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(track.id, track.audioUri, track.albumArtUri) {
        if (track.isFromDevice && (track.audioUri != null || track.albumArtUri != null)) {
            extractedBitmap = withContext(Dispatchers.IO) {
                LocalMediaScanner.extractAudioCoverArt(
                    context = context,
                    audioUriStr = track.audioUri,
                    albumArtUriStr = track.albumArtUri
                )
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = aura.surfaceCard,
        border = if (isHighlighted) {
            BorderStroke(1.5.dp, aura.accentCyan)
        } else {
            BorderStroke(0.6.dp, aura.borderSubtle)
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        when {
            extractedBitmap != null && !extractedBitmap!!.isRecycled -> {
                Image(
                    bitmap = extractedBitmap!!.asImageBitmap(),
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            track.albumArtUri != null -> {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            !track.isFromDevice -> {
                Image(
                    painter = painterResource(id = track.albumCoverRes),
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = fallbackIcon,
                        contentDescription = track.title,
                        tint = aura.textTertiary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalGroupRow(
    title: String,
    subtitle: String,
    leadingTrack: LocalAudioTrack?,
    coverFilePath: String? = null,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = MaterialTheme.aura.textPrimary,
    onClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    val context = LocalContext.current
    val coverFile = remember(coverFilePath) {
        coverFilePath?.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.exists() }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(aura.surfaceCard),
            contentAlignment = Alignment.Center
        ) {
            if (coverFile != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(coverFile)
                        .crossfade(false)
                        .build(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (leadingTrack?.albumArtUri != null) {
                AsyncImage(
                    model = leadingTrack.albumArtUri,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
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
                    fontSize = 12.5.sp
                ),
                maxLines = 1
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

@Composable
private fun LocalMusicEmptyState(
    isArabic: Boolean,
    onScanDeviceMusic: () -> Unit,
    onOpenAudioFilePicker: () -> Unit
) {
    val aura = MaterialTheme.aura
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = aura.textTertiary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (isArabic) "لا توجد مقاطع مطابقة" else "No matching tracks found",
            color = aura.textSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onScanDeviceMusic,
                border = BorderStroke(1.dp, aura.borderSubtle)
            ) {
                Text(
                    text = if (isArabic) "فحص التخزين" else "Scan Storage",
                    color = aura.textPrimary
                )
            }
            Button(
                onClick = onOpenAudioFilePicker,
                colors = ButtonDefaults.buttonColors(
                    containerColor = aura.primaryCtaBg,
                    contentColor = aura.primaryCtaContent
                )
            ) {
                Text(
                    text = if (isArabic) "فتح ملفات" else "Open Files",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
