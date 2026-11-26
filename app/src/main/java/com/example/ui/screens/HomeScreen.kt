package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.data.model.VaultProgressionRegistry
import com.example.ui.components.AuraPulseBrandHeader
import com.example.ui.components.LocalFloatingBottomBarPadding
import com.example.ui.components.HomeMimicCenterpieceEngine
import com.example.ui.components.HomeSnowfallLayer
import com.example.ui.theme.AuraAtmosphericLayer
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.aura
import com.example.ui.theme.auraCardBorderBrush
import com.example.ui.theme.stableStatusBarsPadding
import com.example.ui.viewmodel.StreakViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    streakViewModel: StreakViewModel,
    onNavigateToRoadmap: () -> Unit,
    onNavigateToVault: () -> Unit = {},
    onPlayMedia: (MediaItem) -> Unit = {},
    onNavigateToWatch: () -> Unit = {},
    onOpenFlameShieldLabs: () -> Unit = {},
    isArabic: Boolean = false,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    val uiState by streakViewModel.uiState.collectAsState()
    val xpState by streakViewModel.xpState.collectAsState()
    val forgeState by streakViewModel.forgeState.collectAsState()
    val homeMimicConfig by streakViewModel.homeMimicConfig.collectAsState()
    val isAnyMediaPlaying by streakViewModel.isAnyMediaPlaying.collectAsState()

    val heroSpotlightTint = if (uiState.streakDays > 0) aura.accentFlame else aura.accentCyan
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaSansFontFamily

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(aura.background)
            .testTag("home_screen_minimal")
    ) {
        // Active Store Theme Atmospheric Backdrop
        AuraAtmosphericLayer()

        // Cinema-grade vertical spotlight vignette behind the upper Hero stage
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                heroSpotlightTint.copy(alpha = if (aura.isDark) 0.14f else 0.08f),
                                aura.accentViolet.copy(alpha = if (aura.isDark) 0.06f else 0.03f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.32f),
                            radius = size.width * 0.85f
                        )
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                aura.background.copy(alpha = 0.35f),
                                aura.background
                            ),
                            startY = size.height * 0.35f,
                            endY = size.height * 0.72f
                        )
                    )
                }
        )

        // Realistic VSync-locked multi-depth snowfall across the Home screen when frozen (streak == 0)
        HomeSnowfallLayer(
            isActive = uiState.streakDays == 0,
            isForegroundBokeh = false
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .stableStatusBarsPadding()
        ) {
            val bottomBarPad = LocalFloatingBottomBarPadding.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 4.dp, bottom = 28.dp + bottomBarPad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Minimal Editorial Header + Immersive Hero Centerpiece Stage
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AuraPulseBrandHeader(
                        streakDays = uiState.streakDays,
                        isArabic = isArabic
                    )

                    if (homeMimicConfig.isMimicEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 640.dp)
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            HomeMimicCenterpieceEngine(
                                equippedMimic = homeMimicConfig.equippedMimic,
                                isMimicEnabled = homeMimicConfig.isMimicEnabled,
                                streakDays = uiState.streakDays,
                                dailyProgress = uiState.dailyProgress,
                                isPlayerActive = isAnyMediaPlaying || uiState.isGenuinePlaybackActive,
                                onLongPressToVault = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onNavigateToVault()
                                },
                                isArabic = isArabic,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Clean 2-Tier Cinema Bento Deck (Zero clutter, instant scannability)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tier 1: Balanced 2-Column Metric Bento (Streak & Shields | Daily 5-Min Goal)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tile A: Active Streak & Shield Inventory
                        CinemaBentoCard(
                            accentColor = aura.accentFlame,
                            fillContentHeight = true,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("streak_status_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top Label Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(aura.accentFlameSoft)
                                            .border(
                                                1.dp,
                                                aura.accentFlame.copy(alpha = 0.4f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = aura.accentFlame,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Text(
                                        text = if (isArabic) "سلسلة الحماسة" else "Active Streak",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = bodyFont,
                                            color = aura.textSecondary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Hero Metric Value
                                val streakDaysCount = uiState.streakDays
                                val streakUnit = if (isArabic) {
                                    when {
                                        streakDaysCount == 0 -> "يوم"
                                        streakDaysCount == 1 -> "يوم واحد"
                                        streakDaysCount == 2 -> "يومان"
                                        streakDaysCount in 3..10 -> "أيام"
                                        else -> "يوماً"
                                    }
                                } else {
                                    if (streakDaysCount == 1) "Day" else "Days"
                                }

                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "$streakDaysCount",
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.Black,
                                            color = aura.textPrimary,
                                            fontSize = 30.sp,
                                            lineHeight = 32.sp
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = streakUnit,
                                        modifier = Modifier.padding(bottom = 3.dp),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = bodyFont,
                                            color = aura.textSecondary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Compact Shield Inventory Pills
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val hasFlameShield = forgeState.flameShieldsCount > 0
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (hasFlameShield) aura.accentFlameSoft else aura.surfaceSubtle,
                                        border = BorderStroke(
                                            1.dp,
                                            if (hasFlameShield) aura.accentFlame.copy(alpha = 0.55f) else aura.borderMuted
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onOpenFlameShieldLabs()
                                            }
                                            .testTag("home_flame_shield_pill")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalFireDepartment,
                                                contentDescription = "The Flame Shield",
                                                tint = if (hasFlameShield) aura.accentFlame else aura.textTertiary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${forgeState.flameShieldsCount}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontFamily = PlusJakartaSansFontFamily,
                                                    color = if (hasFlameShield) aura.accentFlame else aura.textSecondary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp
                                                )
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = aura.accentCyanSoft,
                                        border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.35f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = if (isArabic) "الدروع" else "Shields",
                                                tint = aura.accentCyan,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${uiState.shieldsCount}/${uiState.maxShields}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontFamily = PlusJakartaSansFontFamily,
                                                    color = aura.accentCyan,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Tile B: Daily 5-Minute Watch Goal
                        val dailyAccent = if (uiState.isGoalCompletedToday) aura.accentEmerald else aura.accentCyan
                        val dailySoftBg = if (uiState.isGoalCompletedToday) aura.accentEmeraldSoft else aura.accentCyanSoft
                        val curMin = uiState.dailyWatchSeconds / 60
                        val curSec = uiState.dailyWatchSeconds % 60
                        val progressPct = (uiState.dailyProgress * 100).toInt()

                        CinemaBentoCard(
                            accentColor = dailyAccent,
                            fillContentHeight = true,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("daily_watch_progress_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top Label + Status Pill
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isArabic) "الهدف اليومي" else "Daily Goal",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = bodyFont,
                                            color = aura.textSecondary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = dailySoftBg,
                                        border = BorderStroke(1.dp, dailyAccent.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = if (uiState.isGoalCompletedToday) {
                                                if (isArabic) "مكتمل ✓" else "DONE ✓"
                                            } else {
                                                "$progressPct%"
                                            },
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = bodyFont,
                                                color = dailyAccent,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Hero Watch Timer + Compact Progress Ring
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = String.format(Locale.US, "%02d:%02d", curMin, curSec),
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontWeight = FontWeight.Black,
                                                color = aura.textPrimary,
                                                fontSize = 24.sp,
                                                lineHeight = 26.sp
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "/ 05:00",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PlusJakartaSansFontFamily,
                                                color = aura.textTertiary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            progress = { 1f },
                                            modifier = Modifier.fillMaxSize(),
                                            color = aura.progressTrack,
                                            strokeWidth = 3.5.dp,
                                            strokeCap = StrokeCap.Round
                                        )
                                        CircularProgressIndicator(
                                            progress = { uiState.dailyProgress },
                                            modifier = Modifier.fillMaxSize(),
                                            color = dailyAccent,
                                            strokeWidth = 3.5.dp,
                                            strokeCap = StrokeCap.Round
                                        )
                                        Icon(
                                            imageVector = if (uiState.isGoalCompletedToday) {
                                                Icons.Default.CheckCircle
                                            } else {
                                                Icons.Default.LocalFireDepartment
                                            },
                                            contentDescription = null,
                                            tint = dailyAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Sleek Linear Progress Bar + Concise Caption
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    LinearProgressIndicator(
                                        progress = { uiState.dailyProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = dailyAccent,
                                        trackColor = aura.progressTrack
                                    )
                                    Text(
                                        text = if (isArabic) "شاهد 5 دقائق لتغذية الشعلة" else "Watch 5 mins to feed flame",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = bodyFont,
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

                    // Tier 2: Standout Full-Width Roadmap & Rewards Action Banner
                    CinemaBentoCard(
                        accentColor = aura.accentViolet,
                        secondaryAccent = aura.accentCyan,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToRoadmap()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("roadmap_shortcut")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(aura.accentCyanSoft)
                                            .border(
                                                1.dp,
                                                aura.accentCyan.copy(alpha = 0.4f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = aura.accentCyan,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = if (isArabic) {
                                                "الإنجازات، الدروع والمكافآت"
                                            } else {
                                                "Achievements, Shields & Rewards"
                                            },
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = bodyFont,
                                                color = aura.textPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        val subtitleText = if (xpState.isMaxLevelReached) {
                                            if (isArabic) {
                                                "👑 وصلت للمستوى الأسطوري 365 (50M XP)!"
                                            } else {
                                                "👑 Conquered Level 365 (50M XP)!"
                                            }
                                        } else {
                                            val curXp = VaultProgressionRegistry.formatXpCompact(xpState.currentLevelXp)
                                            val reqXp = VaultProgressionRegistry.formatXpCompact(xpState.requiredLevelXp)
                                            if (isArabic) {
                                                "المستوى ${xpState.targetLevel}: $curXp / $reqXp XP"
                                            } else {
                                                "Level ${xpState.targetLevel}: $curXp / $reqXp XP"
                                            }
                                        }

                                        Text(
                                            text = subtitleText,
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

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = aura.surfaceSubtle,
                                        border = BorderStroke(1.dp, aura.borderSubtle)
                                    ) {
                                        Text(
                                            text = "LVL ${xpState.unlockedLevel}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PlusJakartaSansFontFamily,
                                                color = aura.textPrimary,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.5.sp,
                                                letterSpacing = 0.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = aura.primaryCtaBg,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = if (isArabic) "عرض المسار" else "Open Roadmap",
                                                tint = aura.primaryCtaContent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            LinearProgressIndicator(
                                progress = { xpState.levelProgressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = aura.accentCyan,
                                trackColor = aura.progressTrack
                            )
                        }
                    }
                }
            }
        }

        // Subtle out-of-focus foreground bokeh snowflakes drifting softly in the camera plane
        HomeSnowfallLayer(
            isActive = uiState.streakDays == 0,
            isForegroundBokeh = true
        )
    }
}

@Composable
private fun CinemaBentoCard(
    accentColor: Color,
    modifier: Modifier = Modifier,
    secondaryAccent: Color = accentColor,
    fillContentHeight: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val aura = MaterialTheme.aura
    val shape = RoundedCornerShape(20.dp)
    val borderBrush = if (aura.isGlassStyle || aura.isNeumorphic) {
        auraCardBorderBrush(aura)
    } else {
        Brush.linearGradient(
            listOf(
                accentColor.copy(alpha = if (aura.isDark) 0.36f else 0.28f),
                aura.borderSubtle,
                secondaryAccent.copy(alpha = if (aura.isDark) 0.22f else 0.18f)
            )
        )
    }

    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .then(clickModifier),
        shape = shape,
        color = aura.surfaceCard.copy(alpha = if (aura.isDark) 0.88f else 0.96f),
        border = BorderStroke(1.dp, borderBrush),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (fillContentHeight) Modifier.fillMaxHeight() else Modifier)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = if (aura.isDark) 0.09f else 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.15f, size.height * 0.15f),
                            radius = size.maxDimension * 0.85f
                        )
                    )
                },
            content = content
        )
    }
}
