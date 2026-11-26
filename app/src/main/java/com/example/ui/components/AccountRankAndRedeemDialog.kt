package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.VaultProgressionRegistry
import com.example.ui.theme.aura
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val PREMIUM_REDEEM_CODE = "charfeddine23a"

enum class MemberStreakRank(
    val title: String,
    val rarityAr: String,
    val rarityEn: String,
    val minDays: Int,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    BASIC(
        title = "Basic",
        rarityAr = "عادي",
        rarityEn = "Common",
        minDays = 0,
        primaryColor = Color(0xFF94A3B8),
        secondaryColor = Color(0xFF38BDF8)
    ),
    ELITE(
        title = "Elite",
        rarityAr = "نادر",
        rarityEn = "Rare",
        minDays = 20,
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF00F2FE)
    ),
    VIP_I(
        title = "Vip I",
        rarityAr = "ملحمي",
        rarityEn = "Epic",
        minDays = 100,
        primaryColor = Color(0xFFA855F7),
        secondaryColor = Color(0xFFEC4899)
    ),
    VIP_II(
        title = "Vip II",
        rarityAr = "أسطوري",
        rarityEn = "Legendary",
        minDays = 365,
        primaryColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFFF8A00)
    );

    companion object {
        fun forStreak(streakDays: Int): MemberStreakRank = when {
            streakDays >= 365 -> VIP_II
            streakDays >= 100 -> VIP_I
            streakDays >= 20 -> ELITE
            else -> BASIC
        }
    }
}

data class EarnedTitleInfo(
    val titleAr: String,
    val titleEn: String,
    val accentColor: Color,
    val unlockedMedalsCount: Int
)

fun resolveEarnedTitle(streakDays: Int): EarnedTitleInfo {
    val unlockedBadges = listOf(5, 10, 15, 20, 25, 30, 50, 75, 100, 150, 200, 365)
        .filter { streakDays >= it }
    val count = unlockedBadges.size
    return when {
        streakDays >= 365 -> EarnedTitleInfo("الأسطورة السنوية 👑", VaultProgressionRegistry.getMilestone(365).badgeName ?: "Yearly Aura Legend 👑", Color(0xFFFFD700), count)
        streakDays >= 200 -> EarnedTitleInfo("خبير النظام المحلي", VaultProgressionRegistry.getMilestone(200).badgeName ?: "Local System Master", Color(0xFFF59E0B), count)
        streakDays >= 150 -> EarnedTitleInfo("حارس التشفير", VaultProgressionRegistry.getMilestone(150).badgeName ?: "Cipher Sentinel", Color(0xFFEC4899), count)
        streakDays >= 100 -> EarnedTitleInfo("وسام Aura Pro", VaultProgressionRegistry.getMilestone(100).badgeName ?: "Aura Pro", Color(0xFFA855F7), count)
        streakDays >= 75 -> EarnedTitleInfo("مستكشف المسار", VaultProgressionRegistry.getMilestone(75).badgeName ?: "Timeline Navigator", Color(0xFF38BDF8), count)
        streakDays >= 50 -> EarnedTitleInfo("سيد الصوتيات", VaultProgressionRegistry.getMilestone(50).badgeName ?: "Acoustic Master", Color(0xFF06B6D4), count)
        streakDays >= 30 -> EarnedTitleInfo("حارس الشهر الأول", VaultProgressionRegistry.getMilestone(30).badgeName ?: "Month-1 Guardian", Color(0xFF10B981), count)
        streakDays >= 25 -> EarnedTitleInfo("الهالة المشعّة", VaultProgressionRegistry.getMilestone(25).badgeName ?: "Radiant Aura", Color(0xFF22D3EE), count)
        streakDays >= 20 -> EarnedTitleInfo("الحارس الذهبي", VaultProgressionRegistry.getMilestone(20).badgeName ?: "Golden Guardian", Color(0xFFFACC15), count)
        streakDays >= 15 -> EarnedTitleInfo("طليعة النصف شهر", VaultProgressionRegistry.getMilestone(15).badgeName ?: "Half-Month Vanguard", Color(0xFF34D399), count)
        streakDays >= 10 -> EarnedTitleInfo("الحارس الفضي", VaultProgressionRegistry.getMilestone(10).badgeName ?: "Silver Guardian", Color(0xFFCBD5E1), count)
        streakDays >= 5 -> EarnedTitleInfo("شعلة البداية", VaultProgressionRegistry.getMilestone(5).badgeName ?: "Starter Flame", Color(0xFFFF7043), count)
        streakDays >= 1 -> EarnedTitleInfo("مبتدئ الشعلة", "Flame Initiate", Color(0xFF94A3B8), count)
        else -> EarnedTitleInfo("باحث الشرارة", "Spark Seeker", Color(0xFF64748B), 0)
    }
}

@Composable
fun AccountRankLuxuryBadge(
    rank: MemberStreakRank,
    isPremiumActivated: Boolean,
    shimmerPhase: Float,
    glowPulse: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val labelText = if (isPremiumActivated) "Premium" else rank.title
    val primaryColor = if (isPremiumActivated) Color(0xFFFFD700) else rank.primaryColor
    val secondaryColor = if (isPremiumActivated) Color(0xFF00F2FE) else rank.secondaryColor
    val rarityTier = when {
        isPremiumActivated -> 4
        rank == MemberStreakRank.VIP_II -> 3
        rank == MemberStreakRank.VIP_I -> 2
        rank == MemberStreakRank.ELITE -> 1
        else -> 0
    }

    val badgeIcon = when {
        isPremiumActivated -> Icons.Default.WorkspacePremium
        rank == MemberStreakRank.VIP_II -> Icons.Default.WorkspacePremium
        rank == MemberStreakRank.VIP_I -> Icons.Default.Diamond
        rank == MemberStreakRank.ELITE -> Icons.Default.Star
        else -> Icons.Default.Verified
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = primaryColor.copy(alpha = 0.14f + 0.06f * rarityTier),
        border = BorderStroke(
            width = if (rarityTier >= 2) 1.3.dp else 1.dp,
            brush = Brush.horizontalGradient(
                colors = if (rarityTier >= 2) {
                    listOf(primaryColor, Color.White.copy(alpha = 0.85f * glowPulse + 0.15f), secondaryColor)
                } else {
                    listOf(primaryColor, secondaryColor.copy(alpha = 0.65f))
                }
            )
        ),
        shadowElevation = if (rarityTier >= 2) 6.dp else 1.dp,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .drawBehind {
                if (rarityTier >= 1) {
                    val sweepX = (-0.3f + 1.6f * shimmerPhase) * size.width
                    val sweepW = size.width * 0.45f
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.10f + 0.07f * rarityTier),
                                Color.Transparent
                            ),
                            startX = sweepX - sweepW,
                            endX = sweepX + sweepW
                        ),
                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                    )
                }
                if (rarityTier >= 2) {
                    for (s in 0 until rarityTier + 1) {
                        val angle = shimmerPhase * 2f * PI.toFloat() + s * 1.6f
                        val px = size.width * (0.2f + 0.6f * ((s * 0.37f) % 1f)) + cos(angle) * 2.dp.toPx()
                        val py = size.height * 0.5f + sin(angle) * (size.height * 0.34f)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.35f + 0.45f * glowPulse),
                            radius = 1.1.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }
            }
            .clickable(onClick = onClick)
            .testTag("account_type_rank_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = badgeIcon,
                contentDescription = labelText,
                tint = primaryColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (rarityTier >= 3 && aura.isDark) Color(0xFFFFF3B0) else primaryColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 0.4.sp
                )
            )
        }
    }
}

@Composable
fun AccountRankAndRedeemDialog(
    streakDays: Int,
    currentRank: MemberStreakRank,
    earnedTitle: EarnedTitleInfo,
    isPremiumActivated: Boolean,
    isArabic: Boolean,
    onActivatePremiumWithCode: () -> Unit,
    onDeactivatePremium: () -> Unit,
    onDismiss: () -> Unit
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    var showRedeemInput by remember { mutableStateOf(true) }
    var redeemCodeInput by remember { mutableStateOf("") }
    var redeemError by remember { mutableStateOf<String?>(null) }

    val nextRank = MemberStreakRank.entries.firstOrNull { it.minDays > streakDays }
    val progressToNext = if (nextRank != null) {
        val span = (nextRank.minDays - currentRank.minDays).coerceAtLeast(1)
        ((streakDays - currentRank.minDays).toFloat() / span.toFloat()).coerceIn(0f, 1f)
    } else 1f

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = aura.dialogBg,
            border = BorderStroke(
                1.2.dp,
                Brush.linearGradient(
                    listOf(
                        if (isPremiumActivated) Color(0xFFFFD700) else currentRank.primaryColor,
                        currentRank.secondaryColor,
                        aura.accentCyan
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(horizontal = 20.dp)
                .testTag("account_rank_redeem_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            (if (isPremiumActivated) Color(0xFFFFD700) else currentRank.primaryColor).copy(alpha = 0.22f),
                                            currentRank.secondaryColor.copy(alpha = 0.16f)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    if (isPremiumActivated) Color(0xFFFFD700) else currentRank.primaryColor,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPremiumActivated || currentRank == MemberStreakRank.VIP_II) {
                                    Icons.Default.WorkspacePremium
                                } else {
                                    Icons.Default.Diamond
                                },
                                contentDescription = null,
                                tint = if (isPremiumActivated) Color(0xFFFFD700) else currentRank.primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isArabic) "نوع الحساب ورتب السترايك" else "Account Tier & Streak Ranks",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = aura.textPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.5.sp
                                )
                            )
                            Text(
                                text = if (isArabic) {
                                    "تقدمك: $streakDays يوم • اللقب: ${earnedTitle.titleAr}"
                                } else {
                                    "Streak: $streakDays days • Title: ${earnedTitle.titleEn}"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = aura.textSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isArabic) "إغلاق" else "Close",
                            tint = aura.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = aura.surfaceSubtle,
                    border = BorderStroke(0.9.dp, currentRank.primaryColor.copy(alpha = 0.42f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) "رتبتك الحالية:" else "Current Rank:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = aura.textSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${currentRank.title} (${if (isArabic) currentRank.rarityAr else currentRank.rarityEn})",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = currentRank.primaryColor,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progressToNext },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = currentRank.primaryColor,
                            trackColor = aura.progressTrack
                        )

                        Text(
                            text = if (nextRank != null) {
                                val remaining = (nextRank.minDays - streakDays).coerceAtLeast(0)
                                if (isArabic) {
                                    "متبقي $remaining يوم للترقية إلى رتبة ${nextRank.title}"
                                } else {
                                    "$remaining days left to reach ${nextRank.title}"
                                }
                            } else {
                                if (isArabic) "وصلت إلى أعلى رتبة أسطورية Vip II! 👑" else "Maximum Legendary rank Vip II reached! 👑"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = aura.textTertiary,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MemberStreakRank.entries.forEach { rank ->
                        val isCurrent = rank == currentRank
                        val isUnlocked = streakDays >= rank.minDays
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCurrent) rank.primaryColor.copy(alpha = 0.14f) else aura.surfaceCard,
                            border = BorderStroke(
                                width = if (isCurrent) 1.2.dp else 0.8.dp,
                                color = if (isCurrent) rank.primaryColor else aura.borderSubtle
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(rank.primaryColor)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = rank.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = if (isUnlocked) aura.textPrimary else aura.textTertiary,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${if (isArabic) rank.rarityAr else rank.rarityEn}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = rank.primaryColor,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (rank.minDays == 0) {
                                            if (isArabic) "0+ يوم" else "0+ Days"
                                        } else {
                                            if (isArabic) "${rank.minDays}+ يوم" else "${rank.minDays}+ Days"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = aura.textSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = rank.primaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = aura.borderMuted)

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isPremiumActivated) aura.accentGoldBg else aura.surfaceSubtle,
                    border = BorderStroke(
                        1.1.dp,
                        Brush.horizontalGradient(
                            if (isPremiumActivated) {
                                listOf(Color(0xFFFFD700), aura.accentEmerald)
                            } else {
                                listOf(Color(0xFFFFD700).copy(alpha = 0.65f), aura.accentCyan.copy(alpha = 0.55f))
                            }
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showRedeemInput = !showRedeemInput
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD700).copy(alpha = 0.18f))
                                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.65f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Redeem,
                                        contentDescription = "Redeem Premium",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isPremiumActivated) {
                                            if (isArabic) "عضوية Premium مفعّلة 👑" else "Premium Activated 👑"
                                        } else {
                                            if (isArabic) "تفعيل Premium بكود Redeem" else "Activate Premium with Redeem Code"
                                        },
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = if (isPremiumActivated) Color(0xFFFFD700) else aura.textPrimary,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
                                        )
                                    )
                                    Text(
                                        text = if (isPremiumActivated) {
                                            if (isArabic) "كل الميزات والثيمات وأشرطة التمرير مفتوحة مجاناً" else "All themes, seekbars & mimics unlocked free"
                                        } else {
                                            if (isArabic) "أدخل كود التفعيل السري لفتح Premium" else "Enter your redeem code to unlock Premium"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = aura.textSecondary,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }
                            }

                            if (isPremiumActivated) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = aura.accentEmeraldSoft,
                                    border = BorderStroke(1.dp, aura.accentEmerald)
                                ) {
                                    Text(
                                        text = if (isArabic) "مفعّل ✓" else "Active ✓",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = aura.accentEmerald,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }
                            }
                        }

                        if (isPremiumActivated) {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDeactivatePremium()
                                },
                                border = BorderStroke(1.dp, aura.accentFlame.copy(alpha = 0.65f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("deactivate_premium_button")
                            ) {
                                Text(
                                    text = if (isArabic) "إيقاف وضع Premium" else "Deactivate Premium",
                                    color = aura.accentFlame,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            AnimatedVisibility(visible = showRedeemInput) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = redeemCodeInput,
                                        onValueChange = {
                                            redeemCodeInput = it
                                            redeemError = null
                                        },
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.VpnKey,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                text = if (isArabic) "أدخل كود Redeem هنا..." else "Enter Redeem code...",
                                                fontSize = 12.sp,
                                                color = aura.textTertiary
                                            )
                                        },
                                        isError = redeemError != null,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFFD700),
                                            unfocusedBorderColor = aura.borderSubtle,
                                            focusedTextColor = aura.textPrimary,
                                            unfocusedTextColor = aura.textPrimary,
                                            cursorColor = Color(0xFFFFD700)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("premium_redeem_code_input")
                                    )

                                    if (redeemError != null) {
                                        Text(
                                            text = redeemError ?: "",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = aura.accentDanger,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (redeemCodeInput.trim() == PREMIUM_REDEEM_CODE) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                redeemError = null
                                                redeemCodeInput = ""
                                                onActivatePremiumWithCode()
                                            } else {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                redeemError = if (isArabic) {
                                                    "كود التفعيل غير صحيح، تأكد من كتابته بشكل صحيح"
                                                } else {
                                                    "Invalid redeem code. Please check and try again."
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFFD700),
                                            contentColor = Color(0xFF0F172A)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .testTag("premium_redeem_submit_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Redeem,
                                            contentDescription = null,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isArabic) "تفعيل Premium الآن" else "Redeem & Activate Premium",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
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
}
