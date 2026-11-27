package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.LocalAudioTrack
import com.example.data.preferences.UserAudioPlaylist
import com.example.ui.theme.aura
import java.util.Locale

internal enum class LibraryCollectionSheetType {
    HISTORY,
    FAVORITES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryTrackCollectionSheet(
    type: LibraryCollectionSheetType,
    isArabic: Boolean,
    tracks: List<LocalAudioTrack>,
    currentTrackId: String,
    isPlaying: Boolean,
    downloadedIds: Set<String>,
    onDismiss: () -> Unit,
    onPlayTrack: (LocalAudioTrack) -> Unit,
    onToggleDownload: (LocalAudioTrack) -> Unit
) {
    val aura = MaterialTheme.aura
    val sheetBg = aura.dialogBg
    val title = when (type) {
        LibraryCollectionSheetType.HISTORY -> if (isArabic) "سجل الاستماع" else "Listening History"
        LibraryCollectionSheetType.FAVORITES -> if (isArabic) "الأغاني المعجب بها" else "Liked Songs"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetBg,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (isArabic) "${tracks.size} مقاطع" else "${tracks.size} tracks",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textSecondary
                        )
                    )
                }

            }

            Spacer(modifier = Modifier.height(14.dp))

            if (tracks.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = aura.surfaceSubtle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = aura.textSecondary,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = when (type) {
                                LibraryCollectionSheetType.HISTORY -> if (isArabic) "لم تستمع لأي مقطع بعد" else "Your listening history will appear here"
                                LibraryCollectionSheetType.FAVORITES -> if (isArabic) "لم تعجب بأي أغنية بعد" else "No liked songs yet"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = aura.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(tracks, key = { idx, t -> "${t.id}_$idx" }) { _, track ->
                        val isCurrent = track.id == currentTrackId
                        val isSaved = downloadedIds.contains(track.id)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCurrent) aura.accentCyanSoft else aura.surfaceCard,
                            border = BorderStroke(
                                0.8.dp,
                                if (isCurrent) aura.accentCyan else aura.borderSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onPlayTrack(track) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(aura.surfaceSubtle)
                                ) {
                                    if (track.albumArtUri != null) {
                                        AsyncImage(
                                            model = track.albumArtUri,
                                            contentDescription = track.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = track.albumCoverRes),
                                            contentDescription = track.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isCurrent) aura.accentCyan else aura.textPrimary,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${track.artist} · ${track.durationSeconds / 60}:${String.format(Locale.US, "%02d", track.durationSeconds % 60)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = aura.textSecondary,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { onToggleDownload(track) }) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Default.DownloadDone else Icons.Default.PlaylistAdd,
                                        contentDescription = if (isSaved) "Saved" else "Save",
                                        tint = if (isSaved) aura.accentEmerald else aura.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (isCurrent && isPlaying) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Playing",
                                        tint = aura.accentCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryProfileAndStudioSheet(
    isArabic: Boolean,
    minutesListened: Long,
    totalPlays: Int,
    memberSince: String,
    playlistsCount: Int,
    onDismiss: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenFiles: () -> Unit,
    onScanDeviceMusic: () -> Unit
) {
    val aura = MaterialTheme.aura
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = aura.dialogBg,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = if (isArabic) "استوديو الصوت وإحصائياتك" else "Listener Profile & Audio Studio",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = aura.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isArabic) {
                    "$minutesListened دقيقة استماع · $totalPlays تشغيل · $playlistsCount قائمة"
                } else {
                    "$minutesListened min listened · $totalPlays plays · $playlistsCount playlists · Member $memberSince"
                },
                style = MaterialTheme.typography.bodySmall.copy(color = aura.textSecondary)
            )
            Spacer(modifier = Modifier.height(16.dp))

            StudioActionTile(
                icon = Icons.Default.Tune,
                title = if (isArabic) "معادل الصوت والصوت المحيطي" else "Equalizer & Spatial Audio",
                subtitle = if (isArabic) "ضبط الترددات وتضخيم البيس" else "5-band EQ, Bass Boost & 3D Surround",
                onClick = {
                    onDismiss()
                    onOpenEqualizer()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            StudioActionTile(
                icon = Icons.Default.Timer,
                title = if (isArabic) "مؤقت النوم الذكي" else "Smart Sleep Timer",
                subtitle = if (isArabic) "إيقاف تلقائي مع خفض الصوت تدريجياً" else "Auto fade-out playback timer",
                onClick = {
                    onDismiss()
                    onOpenSleepTimer()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            StudioActionTile(
                icon = Icons.Default.FolderOpen,
                title = if (isArabic) "استيراد ملفات صوتية من الهاتف" else "Import Audio Files from Storage",
                subtitle = if (isArabic) "اختر MP3 / M4A / FLAC مباشرة" else "Pick MP3, M4A, FLAC or WAV tracks",
                onClick = {
                    onDismiss()
                    onOpenFiles()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            StudioActionTile(
                icon = Icons.Default.Refresh,
                title = if (isArabic) "فحص تخزين الجهاز" else "Scan Device Music Library",
                subtitle = if (isArabic) "تحديث قائمة الملفات الصوتية المحلية" else "Refresh MediaStore audio tracks",
                onClick = {
                    onDismiss()
                    onScanDeviceMusic()
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StudioActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = aura.surfaceCard,
        border = BorderStroke(0.7.dp, aura.borderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = aura.accentCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = aura.textSecondary,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
internal fun CreateAudioPlaylistDialog(
    isArabic: Boolean,
    availableTracks: List<LocalAudioTrack>,
    onDismiss: () -> Unit,
    onCreate: (name: String, selectedTrackIds: List<String>) -> Unit
) {
    val aura = MaterialTheme.aura
    var name by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = aura.dialogBg,
        title = {
            Text(
                text = if (isArabic) "إنشاء قائمة تشغيل جديدة" else "New Playlist",
                color = aura.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = if (isArabic) "اسم القائمة (مثلاً: سهرة)" else "Playlist name",
                            color = aura.textTertiary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = aura.textPrimary,
                        unfocusedTextColor = aura.textPrimary,
                        focusedBorderColor = aura.accentCyan,
                        unfocusedBorderColor = aura.borderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isArabic) "اختر المقاطع:" else "Select tracks:",
                    style = MaterialTheme.typography.labelMedium.copy(color = aura.textSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                    itemsIndexed(availableTracks) { _, track ->
                        val checked = selectedIds.contains(track.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (checked) selectedIds.remove(track.id) else selectedIds.add(track.id)
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = if (checked) aura.accentCyan else aura.textTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = track.title,
                                color = aura.textPrimary,
                                fontSize = 13.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = name.trim().ifBlank {
                        if (isArabic) "قائمتي" else "My Playlist"
                    }
                    onCreate(trimmed, selectedIds.toList())
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = aura.primaryCtaBg,
                    contentColor = aura.primaryCtaContent
                )
            ) {
                Text(
                    text = if (isArabic) "إنشاء" else "Create",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserPlaylistDetailSheet(
    playlist: UserAudioPlaylist,
    tracks: List<LocalAudioTrack>,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onPlayTrack: (LocalAudioTrack) -> Unit,
    onDeletePlaylist: () -> Unit
) {
    val aura = MaterialTheme.aura
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = aura.dialogBg,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (isArabic) "${tracks.size} مقاطع" else "${tracks.size} tracks",
                        style = MaterialTheme.typography.bodySmall.copy(color = aura.textSecondary)
                    )
                }
                IconButton(onClick = onDeletePlaylist) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = if (isArabic) "حذف القائمة" else "Delete playlist",
                        tint = aura.accentDanger
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (tracks.isEmpty()) {
                Text(
                    text = if (isArabic) "القائمة فارغة حالياً." else "No tracks in this playlist yet.",
                    color = aura.textSecondary,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(tracks) { _, track ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = aura.surfaceCard,
                            border = BorderStroke(0.6.dp, aura.borderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPlayTrack(track) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = aura.accentCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        color = aura.textPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = track.artist,
                                        color = aura.textSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayQueueBottomSheet(
    isArabic: Boolean,
    playlist: List<LocalAudioTrack>,
    currentTrackIndex: Int,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    onSelectTrackIndex: (Int) -> Unit
) {
    val aura = MaterialTheme.aura
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = aura.dialogBg,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = if (isArabic) "قائمة الانتظار (${playlist.size} أغانٍ)" else "Play Queue (${playlist.size} tracks)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = aura.textPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                itemsIndexed(playlist) { index, track ->
                    val isCurrent = index == currentTrackIndex
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrent) aura.accentCyanSoft else aura.surfaceSubtle,
                        border = BorderStroke(1.dp, if (isCurrent) aura.accentCyan else aura.borderMuted),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectTrackIndex(index) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isCurrent && isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (isCurrent) aura.accentCyan else aura.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isCurrent) aura.accentCyan else aura.textPrimary,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${track.artist} • ${track.durationSeconds / 60}:${String.format(Locale.US, "%02d", track.durationSeconds % 60)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
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
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LyricsBottomSheet(
    track: LocalAudioTrack,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = aura.dialogBg,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = aura.textPrimary
                )
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall.copy(color = aura.accentCyan)
            )
            Spacer(modifier = Modifier.height(20.dp))
            track.lyricsLines.forEachIndexed { index, line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (index == 0) aura.textPrimary else aura.textSecondary,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SongOptionsBottomSheet(
    isArabic: Boolean,
    track: LocalAudioTrack,
    isDownloaded: Boolean,
    onDismiss: () -> Unit,
    onScanDeviceMusic: () -> Unit,
    onToggleDownload: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onPlayNext: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val aura = MaterialTheme.aura
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = aura.dialogBg,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = aura.surfaceSubtle
                ) {
                    if (track.albumArtUri != null) {
                        AsyncImage(
                            model = track.albumArtUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = track.albumCoverRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = aura.textPrimary,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.accentCyan,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(aura.borderSubtle)
            )
            Spacer(modifier = Modifier.height(8.dp))

            MusicOptionItem(
                icon = Icons.Default.FolderOpen,
                title = if (isArabic) "مسح والبحث عن كل موسيقى الهاتف" else "Scan all device music",
                onClick = {
                    onDismiss()
                    onScanDeviceMusic()
                }
            )
            MusicOptionItem(
                icon = Icons.Default.PlaylistAdd,
                title = if (isArabic) "إضافة إلى قائمة التشغيل" else "Add to playlist",
                onClick = {
                    onDismiss()
                    onAddToPlaylistClick()
                }
            )
            MusicOptionItem(
                icon = Icons.Default.PlayArrow,
                title = if (isArabic) "تشغيل التالي" else "Play next",
                onClick = {
                    onDismiss()
                    onPlayNext()
                }
            )
            MusicOptionItem(
                icon = Icons.Default.DownloadDone,
                title = if (isDownloaded) {
                    if (isArabic) "محفوظة في التنزيلات" else "Saved to Downloads"
                } else {
                    if (isArabic) "حفظ في التنزيلات" else "Save to Downloads"
                },
                onClick = {
                    onDismiss()
                    onToggleDownload()
                }
            )
            MusicOptionItem(
                icon = Icons.Default.LibraryMusic,
                title = if (isArabic) "الألبوم: ${track.album}" else "Album: ${track.album}",
                onClick = {
                    onDismiss()
                    onShowMessage(if (isArabic) "الألبوم: ${track.album}" else "Album: ${track.album}")
                }
            )
            MusicOptionItem(
                icon = Icons.Default.Info,
                title = if (isArabic) "معلومات الأغنية" else "Song info",
                onClick = {
                    onDismiss()
                    onShowMessage("Duration: ${track.durationSeconds}s • 320 kbps")
                }
            )
            MusicOptionItem(
                icon = Icons.Default.Timer,
                title = if (isArabic) "مؤقت النوم" else "Sleep timer",
                onClick = {
                    onDismiss()
                    onOpenSleepTimer()
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SleepTimerBottomSheet(
    isArabic: Boolean,
    sleepTimerMinutes: Int,
    sleepTimerRemainingSeconds: Int,
    onDismiss: () -> Unit,
    onSelectMinutes: (Int, String) -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = aura.dialogBg,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isArabic) "مؤقت النوم الذكي" else "Smart Sleep Timer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = aura.textPrimary
                    )
                )
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = aura.accentGold,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isArabic) "يتلاشى الصوت تدريجياً ليتوقف التشغيل بسلاسة أثناء النوم" else "Audio gently fades out and stops playback automatically",
                style = MaterialTheme.typography.bodySmall.copy(color = aura.textSecondary, fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.height(18.dp))

            val timerOptions = listOf(
                Pair(15, if (isArabic) "15 دقيقة" else "15 minutes"),
                Pair(30, if (isArabic) "30 دقيقة" else "30 minutes"),
                Pair(60, if (isArabic) "60 دقيقة (ساعة واحدة)" else "60 minutes (1 hour)"),
                Pair(-1, if (isArabic) "نهاية المقطع الحالي" else "End of current track"),
                Pair(0, if (isArabic) "إلغاء المؤقت (إيقاف)" else "Turn off timer")
            )

            timerOptions.forEach { (minutes, label) ->
                val isSelected = (minutes == sleepTimerMinutes && (minutes != 0 || sleepTimerRemainingSeconds == 0))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) aura.accentGoldBg else aura.surfaceSubtle,
                    border = BorderStroke(1.dp, if (isSelected) aura.accentGold else aura.borderMuted),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectMinutes(minutes, label)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isSelected) aura.accentGold else aura.textPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = aura.accentGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EqualizerBottomSheet(
    isArabic: Boolean,
    selectedPreset: String,
    eqBands: MutableList<Float>,
    isSpatialAudioEnabled: Boolean,
    bassBoostPercent: Int,
    onDismiss: () -> Unit,
    onSelectPreset: (String) -> Unit,
    onToggleSpatialAudio: (Boolean) -> Unit,
    onBassBoostChange: (Int) -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = aura.dialogBg,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isArabic) "معادل الصوت والصوت المحيطي" else "Equalizer & Spatial Audio",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = aura.textPrimary
                    )
                )
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = aura.accentCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isArabic) "الملفات الصوتية الجاهزة" else "AUDIO PROFILES",
                style = MaterialTheme.typography.labelSmall.copy(color = aura.accentCyan, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf("Bass Boost", "Vocal", "Flat", "Electronic")
                presets.forEach { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) aura.accentCyanSoft else aura.surfaceSubtle,
                        border = BorderStroke(1.dp, if (isSelected) aura.accentCyan else aura.borderMuted),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectPreset(preset)
                            }
                    ) {
                        Text(
                            text = preset,
                            modifier = Modifier.padding(vertical = 7.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) aura.accentCyan else aura.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
            Text(
                text = if (isArabic) "نطاقات التردد (5 Bands)" else "FREQUENCY BANDS (5 BANDS)",
                style = MaterialTheme.typography.labelSmall.copy(color = aura.accentCyan, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(10.dp))

            bandFrequencies.forEachIndexed { index, freq ->
                val value = eqBands.getOrElse(index) { 0f }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = freq,
                        modifier = Modifier.width(62.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    )
                    Slider(
                        value = value,
                        valueRange = -12f..12f,
                        onValueChange = { newVal ->
                            eqBands[index] = newVal
                            onSelectPreset("Custom")
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = aura.accentCyan,
                            activeTrackColor = aura.accentCyan,
                            inactiveTrackColor = aura.progressTrack
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${if (value >= 0) "+" else ""}${value.toInt()}dB",
                        modifier = Modifier.width(44.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (value != 0f) aura.accentCyan else aura.textSecondary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "الصوت المحيطي المكاني (Spatial 3D Audio)" else "Spatial 3D Audio & Surround",
                        style = MaterialTheme.typography.bodyMedium.copy(color = aura.textPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isArabic) "محاكاة مسرح صوتي سينمائي 360° غامر" else "Virtual 360° immersive binaural acoustic room",
                        style = MaterialTheme.typography.bodySmall.copy(color = aura.textSecondary, fontSize = 11.sp)
                    )
                }
                Switch(
                    checked = isSpatialAudioEnabled,
                    onCheckedChange = onToggleSpatialAudio,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = aura.accentCyan
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isArabic) "مضخم البيس (Bass Boost)" else "Bass Boost Strength",
                    style = MaterialTheme.typography.bodyMedium.copy(color = aura.textPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$bassBoostPercent%",
                    style = MaterialTheme.typography.labelSmall.copy(color = aura.accentCyan, fontWeight = FontWeight.Bold)
                )
            }
            Slider(
                value = bassBoostPercent.toFloat(),
                valueRange = 0f..100f,
                onValueChange = { onBassBoostChange(it.toInt()) },
                colors = SliderDefaults.colors(
                    thumbColor = aura.accentCyan,
                    activeTrackColor = aura.accentCyan,
                    inactiveTrackColor = aura.progressTrack
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MusicOptionItem(
    icon: ImageVector,
    title: String,
    trailingAction: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = aura.accentCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = aura.textPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp
                )
            )
        }
        if (trailingAction != null) {
            trailingAction()
        }
    }
}
