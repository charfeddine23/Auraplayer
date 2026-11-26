package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.local.CyberAvatarConfig
import com.example.data.model.RoadmapXpState
import com.example.data.model.VaultProgressionRegistry
import com.example.ui.screens.defaultAvatarPresets
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TextDark
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.aura
import com.example.ui.theme.auraCardBorderBrush
import java.util.Locale

@Composable
fun MySpaceTopBar(
    isArabic: Boolean,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(aura.accentCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "مساحتي" else "MY SPACE",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = bodyFont,
                        fontWeight = FontWeight.Black,
                        fontSize = if (isArabic) 20.sp else 18.sp,
                        letterSpacing = if (isArabic) 0.sp else 2.4.sp,
                        color = aura.textPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isArabic) "الهوية والمكتبة والتحكم السريع" else "Profile, library & studio controls",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = bodyFont,
                    color = aura.textSecondary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = aura.surfaceCard,
                border = BorderStroke(1.dp, aura.borderSubtle),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .height(42.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onEditProfile()
                    }
                    .testTag("edit_profile_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = if (isArabic) "تعديل الملف" else "Edit profile",
                        tint = aura.accentCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (isArabic) "تعديل" else "Edit",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = bodyFont,
                            color = aura.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = aura.surfaceCard,
                border = BorderStroke(1.dp, aura.borderSubtle),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenSettings()
                    }
                    .testTag("my_space_settings_icon")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = if (isArabic) "إعدادات النظام" else "System Settings",
                        tint = aura.textPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MySpaceProfileHeader(
    userName: String,
    userAvatarPreset: Int,
    cyberAvatarConfig: CyberAvatarConfig,
    isPremiumActivated: Boolean,
    streakDays: Int,
    shieldsCount: Int,
    maxShields: Int,
    totalWatchHours: Float,
    xpState: RoadmapXpState,
    currentRank: MemberStreakRank,
    earnedTitle: EarnedTitleInfo,
    shimmerPhase: Float,
    glowPulse: Float,
    isArabic: Boolean,
    onEditProfile: () -> Unit,
    onOpenRankDialog: () -> Unit,
    onOpenRoadmap: () -> Unit,
    onOpenShields: () -> Unit,
    onOpenStatistics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily
    val heroPrimary = if (isPremiumActivated) aura.accentGold else currentRank.primaryColor
    val heroSecondary = if (isPremiumActivated) aura.accentCyan else currentRank.secondaryColor
    val preset = defaultAvatarPresets.find { it.id == userAvatarPreset } ?: defaultAvatarPresets[0]
    val equippedCyberAvatar = cyberAvatarConfig.equippedAvatar

    CinemaSurfaceCard(
        accentColor = heroPrimary,
        secondaryColor = heroSecondary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Identity Row (Avatar + Name + Rank + Badges)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(82.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (equippedCyberAvatar != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(aura.backgroundAlt)
                                    .border(1.5.dp, heroPrimary.copy(alpha = 0.7f), CircleShape)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onEditProfile()
                                    }
                                    .testTag("profile_avatar_hero"),
                                contentAlignment = Alignment.Center
                            ) {
                                CyberAvatarPortraitCanvas(
                                    avatar = equippedCyberAvatar,
                                    modifier = Modifier.fillMaxSize(),
                                    isCompact = true,
                                    showAuraFrame = cyberAvatarConfig.isAuraFrameEnabled
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(heroPrimary, heroSecondary, aura.accentCyan, heroPrimary)
                                        )
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onEditProfile()
                                    }
                                    .padding(2.5.dp)
                                    .testTag("profile_avatar_hero"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(aura.background)
                                        .padding(2.5.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(preset.gradient)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = preset.icon,
                                        contentDescription = userName,
                                        tint = TextDark,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                        }

                        // Subtle floating edit indicator badge
                        Surface(
                            shape = CircleShape,
                            color = aura.surfaceElevated,
                            border = BorderStroke(1.dp, heroPrimary.copy(alpha = 0.65f)),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = aura.textPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = userName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Black,
                                    color = aura.textPrimary,
                                    fontSize = 20.sp,
                                    letterSpacing = (-0.2).sp
                                )
                            )

                            AccountRankLuxuryBadge(
                                rank = currentRank,
                                isPremiumActivated = isPremiumActivated,
                                shimmerPhase = shimmerPhase,
                                glowPulse = glowPulse,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onOpenRankDialog()
                                }
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = aura.accentCyanSoft,
                                border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onOpenRoadmap()
                                    }
                                    .testTag("profile_level_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = aura.accentCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isArabic) "المستوى ${xpState.unlockedLevel}" else "Lvl ${xpState.unlockedLevel}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = bodyFont,
                                            color = aura.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = earnedTitle.accentColor.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, earnedTitle.accentColor.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onOpenRoadmap()
                                    }
                                    .testTag("profile_earned_title_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (xpState.unlockedLevel >= 20) Icons.Default.EmojiEvents else Icons.Default.MilitaryTech,
                                        contentDescription = null,
                                        tint = earnedTitle.accentColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isArabic) earnedTitle.titleAr else earnedTitle.titleEn,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = bodyFont,
                                            color = earnedTitle.accentColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sleek Cinema XP Ribbon
            val xpCur = VaultProgressionRegistry.formatXpNumber(xpState.currentLevelXp)
            val xpReq = VaultProgressionRegistry.formatXpNumber(xpState.requiredLevelXp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenRoadmap()
                    }
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (xpState.isMaxLevelReached) {
                            if (isArabic) "المستوى الأسطوري مكتمل" else "MAX LEVEL REACHED"
                        } else {
                            if (isArabic) "التقدم نحو المستوى ${xpState.targetLevel}" else "PROGRESS TO LVL ${xpState.targetLevel}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = bodyFont,
                            color = aura.textTertiary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = if (isArabic) 0.sp else 0.8.sp,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = if (xpState.isMaxLevelReached) "365/365" else "$xpCur / $xpReq XP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            color = aura.accentCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                LinearProgressIndicator(
                    progress = { xpState.levelProgressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = heroPrimary,
                    trackColor = aura.progressTrack,
                    strokeCap = StrokeCap.Round
                )
            }

            // Clean 3-column Cinema Metric Bento Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeaderHighlightCell(
                    icon = Icons.Default.LocalFireDepartment,
                    tint = aura.accentFlame,
                    value = if (isArabic) "${streakDays} ي" else "${streakDays}d",
                    label = if (isArabic) "السترايك" else "Streak",
                    isArabic = isArabic,
                    onClick = onOpenRoadmap,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                HeaderHighlightCell(
                    icon = Icons.Default.Shield,
                    tint = aura.accentCyan,
                    value = "$shieldsCount/$maxShields",
                    label = if (isArabic) "الدروع" else "Shields",
                    isArabic = isArabic,
                    onClick = onOpenShields,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("profile_shields_tile")
                )
                HeaderHighlightCell(
                    icon = Icons.Default.BarChart,
                    tint = aura.accentViolet,
                    value = String.format(Locale.US, "%.1fh", totalWatchHours),
                    label = if (isArabic) "المشاهدة" else "Watched",
                    isArabic = isArabic,
                    onClick = onOpenStatistics,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun HeaderHighlightCell(
    icon: ImageVector,
    tint: Color,
    value: String,
    label: String,
    isArabic: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        color = aura.surfaceSubtle,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(tint.copy(alpha = if (aura.isDark) 0.14f else 0.08f), Color.Transparent),
                            radius = size.width * 0.7f
                        ),
                        center = Offset(size.width * 0.5f, 0f)
                    )
                }
                .padding(horizontal = 8.dp, vertical = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Black,
                        color = aura.textPrimary,
                        fontSize = 15.5.sp
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = bodyFont,
                    color = aura.textSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MySpaceQuickAccessGrid(
    savedCount: Int,
    historyCount: Int,
    offlineVideosCount: Int,
    collectionsCount: Int,
    unlockedLevel: Int,
    badgesCount: Int,
    isArabic: Boolean,
    onOpenWatchlist: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenOfflineMedia: () -> Unit,
    onOpenRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionEditorialHeader(
            title = if (isArabic) "مكتبتك السريعة" else "YOUR LIBRARY",
            isArabic = isArabic
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAccessTile(
                icon = Icons.Default.Bookmark,
                accent = MaterialTheme.aura.accentCyan,
                countPill = "$savedCount",
                title = if (isArabic) "قائمتي" else "Watchlist",
                subtitle = if (isArabic) {
                    if (savedCount == 0) "أضف عناوينك المفضلة" else "$savedCount عناصر محفوظة"
                } else {
                    if (savedCount == 0) "Save titles for later" else "$savedCount saved titles"
                },
                isArabic = isArabic,
                onClick = onOpenWatchlist,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("myspace_watchlist_card")
            )
            QuickAccessTile(
                icon = Icons.Default.History,
                accent = MaterialTheme.aura.accentViolet,
                countPill = "$historyCount",
                title = if (isArabic) "سجل المشاهدة" else "History",
                subtitle = if (isArabic) {
                    if (historyCount == 0) "لا يوجد سجل بعد" else "$historyCount مقاطع حديثة"
                } else {
                    if (historyCount == 0) "No recent plays" else "$historyCount recent plays"
                },
                isArabic = isArabic,
                onClick = onOpenHistory,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("myspace_history_card")
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAccessTile(
                icon = Icons.Default.FolderOpen,
                accent = MaterialTheme.aura.accentEmerald,
                countPill = "$offlineVideosCount",
                title = if (isArabic) "الوسائط المحلية" else "Offline Media",
                subtitle = if (isArabic) {
                    "$offlineVideosCount فيديو • $collectionsCount مجلد"
                } else {
                    "$offlineVideosCount videos • $collectionsCount sets"
                },
                isArabic = isArabic,
                onClick = onOpenOfflineMedia,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("myspace_offline_card")
            )
            QuickAccessTile(
                icon = Icons.Default.EmojiEvents,
                accent = MaterialTheme.aura.accentGold,
                countPill = "L$unlockedLevel",
                title = if (isArabic) "المسار والمكافآت" else "Rewards",
                subtitle = if (isArabic) {
                    "المستوى $unlockedLevel • $badgesCount/12 وسام"
                } else {
                    "Lvl $unlockedLevel • $badgesCount/12 badges"
                },
                isArabic = isArabic,
                onClick = onOpenRoadmap,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("myspace_rewards_card")
            )
        }
    }
}

@Composable
private fun QuickAccessTile(
    icon: ImageVector,
    accent: Color,
    countPill: String,
    title: String,
    subtitle: String,
    isArabic: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(22.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        color = aura.surfaceCard,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    accent.copy(alpha = if (aura.isDark) 0.42f else 0.30f),
                    aura.borderMuted
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = if (aura.isDark) 0.14f else 0.08f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.72f
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.12f)
                    )
                }
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.16f))
                        .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accent,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = accent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = countPill,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            color = accent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = bodyFont,
                        fontWeight = FontWeight.ExtraBold,
                        color = aura.textPrimary,
                        fontSize = 14.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = bodyFont,
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

@Composable
fun MySpaceQuickControlsCard(
    currentThemeMode: ThemeMode,
    onCycleThemeMode: () -> Unit,
    currentLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    totalWatchHours: Float,
    totalTracks: Int,
    shieldsCount: Int,
    isArabic: Boolean,
    onOpenStatistics: () -> Unit,
    onOpenShieldFarming: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionEditorialHeader(
            title = if (isArabic) "التحكم والأدوات" else "PREFERENCES & TOOLS",
            isArabic = isArabic
        )

        // Instant One-Tap Quick Switchers (Appearance + Language)
        val themeIcon = when (currentThemeMode) {
            ThemeMode.DARK -> Icons.Default.DarkMode
            ThemeMode.LIGHT -> Icons.Default.LightMode
            ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickSwitcherChipCard(
                icon = themeIcon,
                accent = aura.accentCyan,
                label = if (isArabic) "المظهر" else "Appearance",
                value = if (isArabic) currentThemeMode.labelAr else currentThemeMode.labelEn,
                isArabic = isArabic,
                onClick = onCycleThemeMode,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("myspace_theme_toggle")
            )
            QuickSwitcherChipCard(
                icon = Icons.Default.Language,
                accent = aura.accentEmerald,
                label = if (isArabic) "اللغة" else "Language",
                value = if (isArabic) "العربية" else "English",
                isArabic = isArabic,
                onClick = onToggleLanguage,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("myspace_language_toggle")
            )
        }

        // Grouped Cinema Tools List
        CinemaSurfaceCard(
            accentColor = aura.accentViolet,
            secondaryColor = aura.accentCyan,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ControlRow(
                    icon = Icons.Default.BarChart,
                    accent = aura.accentViolet,
                    title = if (isArabic) "الإحصائيات" else "Statistics",
                    subtitle = if (isArabic) {
                        "${String.format(Locale.US, "%.1f", totalWatchHours)} س • $totalTracks مقطع صوتي"
                    } else {
                        "${String.format(Locale.US, "%.1f", totalWatchHours)} hrs watched • $totalTracks tracks"
                    },
                    isArabic = isArabic,
                    onClick = onOpenStatistics,
                    modifier = Modifier.testTag("profile_statistics_tile")
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = aura.borderMuted
                )

                ControlRow(
                    icon = Icons.Default.Shield,
                    accent = aura.accentFlame,
                    title = if (isArabic) "زراعة الدروع والحداد" else "Shield Farming & Forge",
                    subtitle = if (isArabic) "رصيدك الحالي: $shieldsCount درع" else "Available: $shieldsCount shields",
                    isArabic = isArabic,
                    onClick = onOpenShieldFarming,
                    modifier = Modifier.testTag("myspace_shield_farming_row")
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = aura.borderMuted
                )

                ControlRow(
                    icon = Icons.Default.Settings,
                    accent = aura.accentCyan,
                    title = if (isArabic) "إعدادات النظام" else "System Settings",
                    subtitle = if (isArabic) "الثيمات، الخزنة السرية، التشغيل والنسخ الاحتياطي" else "Themes, PIN vault, playback & backup",
                    isArabic = isArabic,
                    onClick = onOpenSystemSettings,
                    modifier = Modifier.testTag("system_settings_entry_card")
                )
            }
        }
    }
}

@Composable
private fun QuickSwitcherChipCard(
    icon: ImageVector,
    accent: Color,
    label: String,
    value: String,
    isArabic: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        color = aura.surfaceCard,
        border = BorderStroke(1.dp, accent.copy(alpha = if (aura.isDark) 0.34f else 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = bodyFont,
                        color = aura.textTertiary,
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = bodyFont,
                        color = aura.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ControlRow(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String,
    isArabic: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily

    Row(
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(accent.copy(alpha = 0.14f))
                .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(19.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = bodyFont,
                    color = aura.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = bodyFont,
                    color = aura.textSecondary,
                    fontSize = 11.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
fun MySpaceAppFooterCard(
    isArabic: Boolean,
    isCheckingForUpdates: Boolean,
    onCheckForUpdates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily

    CinemaSurfaceCard(
        accentColor = aura.accentCyan,
        secondaryColor = aura.accentViolet,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(aura.accentCyan)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "AURA PULSE",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                color = aura.textPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.6.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isArabic) {
                            "تطوير Charfeddine Kahlessenane • بناء ${BuildConfig.VERSION_CODE}"
                        } else {
                            "By Charfeddine Kahlessenane • Build ${BuildConfig.VERSION_CODE}"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = bodyFont,
                            color = aura.textTertiary,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = aura.accentCyanSoft,
                    border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            color = aura.accentCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = aura.primaryCtaBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !isCheckingForUpdates) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCheckForUpdates()
                    }
                    .testTag("check_for_updates_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isCheckingForUpdates) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = aura.primaryCtaContent,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isArabic) "جاري التحقق..." else "Checking for updates...",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = bodyFont,
                                color = aura.primaryCtaContent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = aura.primaryCtaContent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "التحقق من التحديثات" else "Check for Updates",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = bodyFont,
                                color = aura.primaryCtaContent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionEditorialHeader(
    title: String,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily

    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge.copy(
            fontFamily = bodyFont,
            fontWeight = FontWeight.ExtraBold,
            color = aura.textSecondary,
            letterSpacing = if (isArabic) 0.sp else 1.5.sp,
            fontSize = if (isArabic) 13.5.sp else 11.5.sp
        )
    )
}

@Composable
private fun CinemaSurfaceCard(
    accentColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val aura = MaterialTheme.aura
    val shape = RoundedCornerShape(24.dp)
    val borderBrush = if (aura.isGlassStyle || aura.isNeumorphic) {
        auraCardBorderBrush(aura)
    } else {
        Brush.linearGradient(
            listOf(
                accentColor.copy(alpha = if (aura.isDark) 0.42f else 0.30f),
                aura.borderSubtle,
                secondaryColor.copy(alpha = if (aura.isDark) 0.22f else 0.14f)
            )
        )
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = aura.surfaceCard.copy(alpha = if (aura.isDark) 0.90f else 0.98f),
        border = BorderStroke(width = 1.dp, brush = borderBrush),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = if (aura.isDark) 0.10f else 0.05f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.65f
                        ),
                        center = Offset(size.width * 0.12f, size.height * 0.12f)
                    )
                },
            content = content
        )
    }
}
