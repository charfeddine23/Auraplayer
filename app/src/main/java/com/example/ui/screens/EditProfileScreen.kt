package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.example.ui.theme.stableStatusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StoreAvatarItem
import com.example.ui.components.CyberAvatarPortraitCanvas
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.PulseViolet
import com.example.ui.theme.TextDark
import com.example.ui.theme.aura
import com.example.ui.viewmodel.StreakViewModel

data class AvatarPreset(
    val id: Int,
    val nameEn: String,
    val nameAr: String,
    val icon: ImageVector,
    val gradient: List<Color>
)

val defaultAvatarPresets = listOf(
    AvatarPreset(
        id = 0,
        nameEn = "Neon Pulse",
        nameAr = "نبض النيون",
        icon = Icons.Default.Person,
        gradient = listOf(PulseCyan, PulseViolet)
    ),
    AvatarPreset(
        id = 1,
        nameEn = "Liquid Flame",
        nameAr = "اللهب المتوهج",
        icon = Icons.Default.LocalFireDepartment,
        gradient = listOf(Color(0xFFFF5722), Color(0xFFFF9800))
    ),
    AvatarPreset(
        id = 2,
        nameEn = "Cosmic Star",
        nameAr = "النجم الكوني",
        icon = Icons.Default.AutoAwesome,
        gradient = listOf(Color(0xFF7928CA), Color(0xFFFF0080))
    ),
    AvatarPreset(
        id = 3,
        nameEn = "Shield Guardian",
        nameAr = "حارس الدرع",
        icon = Icons.Default.Security,
        gradient = listOf(Color(0xFF00E5FF), Color(0xFF10B981))
    ),
    AvatarPreset(
        id = 4,
        nameEn = "VIP Prestige",
        nameAr = "عضوية VIP",
        icon = Icons.Default.WorkspacePremium,
        gradient = listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
    )
)

@Composable
fun EditProfileScreen(
    streakViewModel: StreakViewModel,
    onNavigateBack: () -> Unit,
    isArabic: Boolean = false,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val haptic = LocalHapticFeedback.current
    BackHandler {
        onNavigateBack()
    }
    val currentSavedName by streakViewModel.userName.collectAsState()
    val currentSavedAvatarUri by streakViewModel.userAvatarUri.collectAsState()
    val currentSavedPreset by streakViewModel.userAvatarPreset.collectAsState()
    val cyberAvatarConfig by streakViewModel.cyberAvatarConfig.collectAsStateWithLifecycle()
    val purchasedAvatarIds by streakViewModel.purchasedAvatarIds.collectAsStateWithLifecycle()
    val isPremiumActivated by streakViewModel.isPremiumActivated.collectAsStateWithLifecycle()
    val uiState by streakViewModel.uiState.collectAsState()

    var nameInput by remember(currentSavedName) { mutableStateOf(currentSavedName) }
    var selectedAvatarUri by remember(currentSavedAvatarUri) { mutableStateOf(currentSavedAvatarUri) }
    var selectedPresetId by remember(currentSavedPreset) { mutableIntStateOf(currentSavedPreset) }
    val unlockedCyberAvatars = remember(purchasedAvatarIds, cyberAvatarConfig.unlockedAvatarIds, isPremiumActivated) {
        val unlockedIds = purchasedAvatarIds + cyberAvatarConfig.unlockedAvatarIds
        StoreAvatarItem.entries.filter { isPremiumActivated || unlockedIds.contains(it.id) }
    }
    val activeCyberAvatar = if (selectedAvatarUri == null) cyberAvatarConfig.equippedAvatar else null

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val uriStr = uri.toString()
            selectedAvatarUri = uriStr
            streakViewModel.clearEquippedCyberAvatar(isArabic = isArabic, silent = true)
            streakViewModel.updateProfile(
                name = nameInput,
                avatarUri = uriStr,
                presetId = selectedPresetId
            )
        }
    }

    val xpState by streakViewModel.xpState.collectAsState()
    val currentTierBadge = streakViewModel.getUserTierBadge(xpState.unlockedLevel, isArabic)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(aura.background)
            .testTag("edit_profile_screen")
    ) {
        // Ambient glass background glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(aura.accentCyan.copy(alpha = 0.12f), aura.accentViolet.copy(alpha = 0.06f), Color.Transparent),
                            radius = size.width * 0.9f
                        ),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.15f)
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .stableStatusBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(aura.iconButtonBg)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = aura.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (isArabic) "تعديل الملف الشخصي" else "Edit Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = aura.textPrimary
                    )
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Main Avatar Preview Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, aura.borderSubtle, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = aura.surfaceCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val preset = defaultAvatarPresets.find { it.id == selectedPresetId }
                                ?: defaultAvatarPresets[0]

                            if (activeCyberAvatar != null) {
                                Box(
                                    modifier = Modifier
                                        .size(124.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF000000))
                                        .testTag("edit_profile_cyber_avatar_preview"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CyberAvatarPortraitCanvas(
                                        avatar = activeCyberAvatar,
                                        modifier = Modifier.fillMaxSize(),
                                        isCompact = false,
                                        showAuraFrame = cyberAvatarConfig.isAuraFrameEnabled
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(108.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(preset.gradient))
                                        .border(2.dp, aura.borderSubtle, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = preset.icon,
                                        contentDescription = "Profile Avatar",
                                        tint = TextDark,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = nameInput.ifBlank { "Charfeddine Kahlessenane" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = aura.textPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Tier Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = aura.accentCyanSoft,
                                border = BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = currentTierBadge,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = aura.accentCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }

                            if (activeCyberAvatar != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                val avatarAccent = Color(activeCyberAvatar.primaryAccentHex)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF070A12),
                                    border = BorderStroke(1.dp, avatarAccent.copy(alpha = 0.55f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isArabic) activeCyberAvatar.displayNameAr else activeCyberAvatar.displayNameEn,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = avatarAccent,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            )
                                            Text(
                                                text = if (isArabic) activeCyberAvatar.frameNameAr else activeCyberAvatar.frameNameEn,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = aura.textSecondary,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                        Switch(
                                            checked = cyberAvatarConfig.isAuraFrameEnabled,
                                            onCheckedChange = { enabled ->
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                streakViewModel.setCyberAvatarFrameEnabled(enabled)
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color(0xFF000000),
                                                checkedTrackColor = avatarAccent
                                            ),
                                            modifier = Modifier.testTag("edit_profile_aura_frame_switch")
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Pick Photo from Device Button
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = aura.accentCyanSoft,
                                    contentColor = aura.accentCyan
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, aura.accentCyan.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "إضافة صورة من الهاتف" else "Add Photo from Phone",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Choose Default Preset Avatar
                item {
                    Column {
                        Text(
                            text = if (isArabic) "أو اختر صورة رمزية افتراضية:" else "Or Select a Preset Avatar:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = aura.textSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(defaultAvatarPresets) { _, item ->
                                val isSelected = selectedPresetId == item.id && selectedAvatarUri == null && activeCyberAvatar == null
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(item.gradient))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) aura.accentCyan else aura.borderSubtle,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedPresetId = item.id
                                            selectedAvatarUri = null
                                            streakViewModel.clearEquippedCyberAvatar(isArabic = isArabic, silent = true)
                                            streakViewModel.updateProfile(
                                                name = nameInput,
                                                avatarUri = null,
                                                presetId = item.id
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.nameEn,
                                        tint = TextDark,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0x33000000), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
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

                // Cyberpunk Store Avatars (Unlocked from The Forge -> Shop -> Avatars & persisted via Room DB)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (isArabic) {
                                "أفاتارات السايبر المتحركة (المفتوحة من المتجر):"
                            } else {
                                "Unlocked Cyberpunk Store Avatars:"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = aura.textSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )

                        if (unlockedCyberAvatars.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = aura.surfaceCard,
                                border = BorderStroke(1.dp, aura.borderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isArabic) {
                                        "🔒 افتح 10 أفاتارات سايبر متحركة وإطارات هولوغرافية من The Forge ← المتجر ← Avatars"
                                    } else {
                                        "🔒 Unlock 10 animated Cyberpunk Avatars & Holographic Aura Frames in The Forge → Shop → Avatars"
                                    },
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = aura.textSecondary,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.testTag("edit_profile_cyber_avatars_row")
                            ) {
                                items(unlockedCyberAvatars, key = { it.id }) { avatar ->
                                    val isEquipped = activeCyberAvatar == avatar
                                    val accent = Color(avatar.primaryAccentHex)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .width(74.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                selectedAvatarUri = null
                                                streakViewModel.equipCyberAvatar(avatar, isArabic = isArabic)
                                            }
                                            .padding(4.dp)
                                            .testTag("edit_profile_cyber_avatar_${avatar.id}")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF000000))
                                                .border(
                                                    width = if (isEquipped) 2.2.dp else 1.dp,
                                                    color = if (isEquipped) accent else aura.borderSubtle,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CyberAvatarPortraitCanvas(
                                                avatar = avatar,
                                                modifier = Modifier.fillMaxSize(),
                                                isCompact = true,
                                                showAuraFrame = cyberAvatarConfig.isAuraFrameEnabled
                                            )
                                        }
                                        Text(
                                            text = if (isArabic) avatar.displayNameAr else avatar.displayNameEn,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isEquipped) accent else aura.textSecondary,
                                                fontWeight = if (isEquipped) FontWeight.ExtraBold else FontWeight.Medium,
                                                fontSize = 9.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // User Name Input Section
                item {
                    Column {
                        Text(
                            text = if (isArabic) "اسم المستخدم" else "User Name",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = aura.textSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { updatedName ->
                                nameInput = updatedName
                                streakViewModel.updateProfile(
                                    name = updatedName,
                                    avatarUri = selectedAvatarUri,
                                    presetId = selectedPresetId
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_username_input"),
                            shape = RoundedCornerShape(14.dp),
                            placeholder = {
                                Text("Charfeddine Kahlessenane", color = aura.textTertiary)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = aura.accentCyan,
                                unfocusedBorderColor = aura.borderSubtle,
                                focusedContainerColor = aura.surfaceCard,
                                unfocusedContainerColor = aura.surfaceCard,
                                focusedTextColor = aura.textPrimary,
                                unfocusedTextColor = aura.textPrimary
                            )
                        )
                    }
                }

                // Tier Levels Information Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, aura.borderSubtle, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = aura.surfaceCard)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isArabic) "نظام رتب الأعضاء والسترايك" else "Member Tier System",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = aura.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            TierRow(
                                title = if (isArabic) "مستخدم عادي" else "Standard User",
                                requirement = if (isArabic) "السترايك حتى 20 يوماً" else "Streak up to 20 days",
                                isActive = uiState.streakDays <= 20
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            TierRow(
                                title = if (isArabic) "مستخدم مميز" else "Special User",
                                requirement = if (isArabic) "السترايك من 21 حتى 60 يوماً" else "Streak 21 to 60 days",
                                isActive = uiState.streakDays in 21..60
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            TierRow(
                                title = if (isArabic) "مستخدم مميز (VIP)" else "VIP Member",
                                requirement = if (isArabic) "السترايك من 61 حتى 180 يوماً" else "Streak 61 to 180 days",
                                isActive = uiState.streakDays in 61..180
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            TierRow(
                                title = "Vip II Member",
                                requirement = if (isArabic) "أكثر من 180 يوماً متواصلاً" else "Streak over 180 days",
                                isActive = uiState.streakDays > 180,
                                isTopTier = true
                            )
                        }
                    }
                }

                // Auto-saved status indicator & Done button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            streakViewModel.updateProfile(
                                name = nameInput,
                                avatarUri = selectedAvatarUri,
                                presetId = selectedPresetId
                            )
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_profile_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = aura.accentCyan,
                            contentColor = if (aura.isDark) TextDark else Color.White
                        )
                    ) {
                        Text(
                            text = if (isArabic) "تم الحفظ تلقائياً • تم" else "Auto-Saved • Done",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TierRow(
    title: String,
    requirement: String,
    isActive: Boolean,
    isTopTier: Boolean = false
) {
    val aura = MaterialTheme.aura
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) (if (isTopTier) aura.accentGold else aura.accentCyan) else aura.textTertiary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) (if (isTopTier) aura.accentGold else aura.accentCyan) else aura.textSecondary,
                    fontSize = 12.sp
                )
            )
        }
        Text(
            text = requirement,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isActive) aura.textPrimary else aura.textTertiary,
                fontSize = 11.sp
            )
        )
    }
}
