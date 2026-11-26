package com.example.ui.screens

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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CyberAvatarConfig
import com.example.data.local.VideoBookmarkGroup
import com.example.data.model.RoadmapXpState
import com.example.ui.components.CyberAvatarPortraitCanvas
import com.example.ui.components.MemberStreakRank
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.aura
import com.example.ui.theme.auraCardBorderBrush
import java.util.Locale

@Composable
internal fun MeeTubeTopBar(
    appBrandLabel: String,
    isArabic: Boolean,
    onBrandClick: () -> Unit,
    onCastClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    val borderWidth = if (aura.isGlassStyle || aura.isNeumorphic) 1.2.dp else 0.85.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = aura.surfaceCard,
            border = BorderStroke(borderWidth, auraCardBorderBrush(aura)),
            modifier = Modifier
                .height(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onBrandClick)
                .testTag("myspace_brand_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val badgeColor = aura.ambientPrimary
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val path = Path().apply {
                            moveTo(w * 0.5f, h * 0.88f)
                            cubicTo(w * 0.14f, h * 0.66f, w * 0.02f, h * 0.38f, w * 0.14f, h * 0.22f)
                            cubicTo(w * 0.24f, h * 0.08f, w * 0.44f, h * 0.12f, w * 0.5f, h * 0.26f)
                            cubicTo(w * 0.56f, h * 0.12f, w * 0.76f, h * 0.08f, w * 0.86f, h * 0.22f)
                            cubicTo(w * 0.98f, h * 0.38f, w * 0.86f, h * 0.66f, w * 0.5f, h * 0.88f)
                            close()
                        }
                        drawPath(path = path, color = badgeColor)
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = appBrandLabel,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = aura.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = aura.surfaceCard,
            border = BorderStroke(borderWidth, auraCardBorderBrush(aura))
        ) {
            IconButton(
                onClick = onCastClick,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("myspace_cast_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cast,
                    contentDescription = if (isArabic) "البث على الأجهزة" else "Cast",
                    tint = aura.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
internal fun MeeTubeProfileRow(
    displayName: String,
    cyberAvatarConfig: CyberAvatarConfig,
    onAvatarClick: () -> Unit
) {
    val aura = MaterialTheme.aura
    val equippedCyberAvatar = cyberAvatarConfig.equippedAvatar

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(aura.surfaceElevated)
                .border(1.2.dp, auraCardBorderBrush(aura), CircleShape)
                .clickable(onClick = onAvatarClick)
                .testTag("profile_avatar_hero"),
            contentAlignment = Alignment.Center
        ) {
            if (equippedCyberAvatar != null) {
                CyberAvatarPortraitCanvas(
                    avatar = equippedCyberAvatar,
                    modifier = Modifier.fillMaxSize(),
                    isCompact = true,
                    showAuraFrame = cyberAvatarConfig.isAuraFrameEnabled
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = displayName,
                    tint = aura.textPrimary,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(18.dp))

        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = aura.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onAvatarClick)
        )
    }
}

@Composable
internal fun MeeTubeAccountSettingsPills(
    isArabic: Boolean,
    onAccountsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val aura = MaterialTheme.aura

    Surface(
        shape = RoundedCornerShape(32.dp),
        color = aura.surfaceSubtle,
        border = BorderStroke(0.8.dp, aura.borderMuted),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = aura.surfaceCard,
                border = BorderStroke(0.8.dp, aura.borderSubtle),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAccountsClick)
                    .testTag("myspace_accounts_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isArabic) "الحسابات" else "Accounts",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.5.sp
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
                    .height(46.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onSettingsClick)
                    .testTag("myspace_settings_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isArabic) "الإعدادات" else "Settings",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
internal fun MeeTubeHistoryHeaderRow(
    isArabic: Boolean,
    onClick: () -> Unit
) {
    val aura = MaterialTheme.aura

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag("myspace_history_row"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isArabic) "السجل" else "History",
            style = MaterialTheme.typography.titleMedium.copy(
                color = aura.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = aura.accentCyan,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
internal fun MeeTubeLibrarySection(
    isArabic: Boolean,
    onDownloadsClick: () -> Unit,
    onWatchLaterClick: () -> Unit,
    onLikedVideosClick: () -> Unit,
    onPlaylistsClick: () -> Unit,
    onMyVideosClick: () -> Unit
) {
    val aura = MaterialTheme.aura

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = if (isArabic) "المكتبة" else "Library",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = aura.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = aura.surfaceSubtle,
            border = BorderStroke(0.9.dp, aura.borderMuted),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: 4 tiles (Downloads, Watch later, Liked videos, Playlists) — NO Subscriptions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MeeTubeSquareTile(
                        icon = Icons.Default.Download,
                        label = if (isArabic) "التنزيلات" else "Downloads",
                        onClick = onDownloadsClick,
                        testTag = "myspace_lib_downloads",
                        modifier = Modifier.weight(1f)
                    )
                    MeeTubeSquareTile(
                        icon = Icons.Default.Schedule,
                        label = if (isArabic) "المشاهدة لاحقاً" else "Watch later",
                        onClick = onWatchLaterClick,
                        testTag = "myspace_lib_watch_later",
                        modifier = Modifier.weight(1f)
                    )
                    MeeTubeSquareTile(
                        icon = Icons.Default.ThumbUp,
                        label = if (isArabic) "المعجب بها" else "Liked videos",
                        onClick = onLikedVideosClick,
                        testTag = "myspace_lib_liked",
                        modifier = Modifier.weight(1f)
                    )
                    MeeTubeSquareTile(
                        icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                        label = if (isArabic) "القوائم" else "Playlists",
                        onClick = onPlaylistsClick,
                        testTag = "myspace_lib_playlists",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: My videos (aligned cleanly to 1/4 width column)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MeeTubeSquareTile(
                        icon = Icons.Default.OndemandVideo,
                        label = if (isArabic) "فيديوهاتي" else "My videos",
                        onClick = onMyVideosClick,
                        testTag = "myspace_lib_my_videos",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun MeeTubeExploreSection(
    isArabic: Boolean,
    onWatchClick: () -> Unit,
    onListenClick: () -> Unit
) {
    val aura = MaterialTheme.aura

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = if (isArabic) "استكشاف" else "Explore",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = aura.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = aura.surfaceSubtle,
            border = BorderStroke(0.9.dp, aura.borderMuted),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MeeTubeSquareTile(
                    icon = Icons.Default.VideoLibrary,
                    label = if (isArabic) "مشاهدة" else "Watch",
                    onClick = onWatchClick,
                    testTag = "myspace_explore_watch",
                    useWideAspect = true,
                    iconTint = aura.accentCyan,
                    modifier = Modifier.weight(1f)
                )
                MeeTubeSquareTile(
                    icon = Icons.Default.Headphones,
                    label = if (isArabic) "استماع" else "Listen",
                    onClick = onListenClick,
                    testTag = "myspace_explore_listen",
                    useWideAspect = true,
                    iconTint = aura.accentViolet,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MeeTubeSquareTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String,
    useWideAspect: Boolean = false,
    iconTint: Color? = null,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val borderWidth = if (aura.isGlassStyle || aura.isNeumorphic) 1.dp else 0.75.dp

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = aura.surfaceCard,
        border = BorderStroke(borderWidth, auraCardBorderBrush(aura)),
        modifier = modifier
            .then(
                if (useWideAspect) Modifier.height(92.dp) else Modifier.aspectRatio(0.92f)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint ?: aura.textPrimary,
                modifier = Modifier.size(23.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = aura.textSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MeeTubeAccountsHubSheet(
    userName: String,
    streakDays: Int,
    shieldsCount: Int,
    totalWatchHours: Float,
    xpState: RoadmapXpState,
    currentRank: MemberStreakRank,
    currentThemeMode: ThemeMode,
    currentLanguage: AppLanguage,
    isCheckingForUpdates: Boolean,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenRankDialog: () -> Unit,
    onOpenRoadmap: () -> Unit,
    onOpenShieldFarming: () -> Unit,
    onOpenStatistics: () -> Unit,
    onCycleThemeMode: () -> Unit,
    onToggleLanguage: () -> Unit,
    onCheckForUpdates: () -> Unit
) {
    val aura = MaterialTheme.aura

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = aura.dialogBg,
        contentColor = aura.textPrimary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = userName.ifBlank { if (isArabic) "الحساب والملف" else "Account & Studio Hub" },
                style = MaterialTheme.typography.titleLarge.copy(
                    color = aura.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = String.format(
                    Locale.US,
                    if (isArabic) {
                        "المستوى %d · %d يوم متواصل · %d درع · %.1f ساعة"
                    } else {
                        "Lv.%d · %d day streak · %d shields · %.1fh watched"
                    },
                    xpState.unlockedLevel,
                    streakDays,
                    shieldsCount,
                    totalWatchHours
                ),
                style = MaterialTheme.typography.bodySmall.copy(color = aura.textSecondary)
            )
            Spacer(modifier = Modifier.height(6.dp))

            AccountsHubOptionRow(
                icon = Icons.Default.Edit,
                title = if (isArabic) "تعديل الاسم والصورة" else "Edit Profile & Avatar",
                onClick = { onDismiss(); onEditProfile() }
            )
            AccountsHubOptionRow(
                icon = Icons.Default.EmojiEvents,
                title = if (isArabic) "الرتبة ورمز البريميوم (${currentRank.title})" else "Rank & Premium Code (${currentRank.title})",
                onClick = { onDismiss(); onOpenRankDialog() }
            )
            AccountsHubOptionRow(
                icon = Icons.Default.AccountCircle,
                title = if (isArabic) "مسار المستويات والمكافآت" else "XP Roadmap & Milestones",
                onClick = { onDismiss(); onOpenRoadmap() }
            )
            AccountsHubOptionRow(
                icon = Icons.Default.Shield,
                title = if (isArabic) "مزرعة الدروع" else "Shield Farming & Vault",
                onClick = { onDismiss(); onOpenShieldFarming() }
            )
            AccountsHubOptionRow(
                icon = Icons.Default.BarChart,
                title = if (isArabic) "الإحصائيات التفصيلية" else "Detailed Analytics",
                onClick = { onDismiss(); onOpenStatistics() }
            )
            AccountsHubOptionRow(
                icon = Icons.Default.DarkMode,
                title = if (isArabic) "المظهر: ${currentThemeMode.labelAr}" else "Theme: ${currentThemeMode.labelEn}",
                onClick = onCycleThemeMode
            )
            AccountsHubOptionRow(
                icon = Icons.Default.Language,
                title = if (isArabic) "اللغة: العربية" else "Language: ${currentLanguage.labelEn}",
                onClick = onToggleLanguage
            )
            AccountsHubOptionRow(
                icon = Icons.Default.SystemUpdate,
                title = if (isCheckingForUpdates) {
                    if (isArabic) "جاري التحقق..." else "Checking for updates..."
                } else {
                    if (isArabic) "التحقق من التحديثات" else "Check for Updates"
                },
                onClick = { onDismiss(); onCheckForUpdates() }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun AccountsHubOptionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    val aura = MaterialTheme.aura

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = aura.surfaceCard,
        border = BorderStroke(0.8.dp, aura.borderSubtle),
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
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = aura.textPrimary,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = aura.textTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MeeTubePlaylistsSheet(
    bookmarkGroups: List<VideoBookmarkGroup>,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onOpenWatch: () -> Unit,
    onOpenListen: () -> Unit
) {
    val aura = MaterialTheme.aura

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = aura.dialogBg,
        contentColor = aura.textPrimary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = if (isArabic) "قوائم التشغيل والمجموعات" else "Playlists & Collections",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = aura.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (bookmarkGroups.isEmpty()) {
                Text(
                    text = if (isArabic) "لا توجد قوائم فيديو محفوظة بعد. يمكنك فتح قسم Watch أو Listen." else "No saved playlists yet. Jump into Watch or Listen to create collections.",
                    color = aura.textSecondary,
                    fontSize = 13.5.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountsHubOptionRow(
                        icon = Icons.Default.VideoLibrary,
                        title = if (isArabic) "الانتقال إلى Watch" else "Open Watch",
                        onClick = { onDismiss(); onOpenWatch() }
                    )
                    AccountsHubOptionRow(
                        icon = Icons.Default.Headphones,
                        title = if (isArabic) "الانتقال إلى Listen" else "Open Listen",
                        onClick = { onDismiss(); onOpenListen() }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bookmarkGroups) { group ->
                        AccountsHubOptionRow(
                            icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                            title = "${group.videoTitle} (${group.bookmarks.size})",
                            onClick = { onDismiss(); onOpenWatch() }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
