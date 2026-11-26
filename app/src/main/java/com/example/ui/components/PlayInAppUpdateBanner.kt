package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.update.PlayInAppUpdateUiState
import com.example.ui.theme.aura

/**
 * Floating status banner for Google Play Flexible In-App Updates:
 * displays real-time download progress and a one-tap restart button once ready to install.
 */
@Composable
fun PlayInAppUpdateBanner(
    state: PlayInAppUpdateUiState,
    isArabic: Boolean,
    onInstallNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aura = MaterialTheme.aura
    val isVisible = state.isDownloading || state.isDownloadedReadyToInstall || state.isInstalling

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .testTag("play_in_app_update_banner"),
            shape = RoundedCornerShape(18.dp),
            color = aura.surfaceElevated.copy(alpha = 0.96f),
            border = BorderStroke(
                1.dp,
                if (state.isDownloadedReadyToInstall) {
                    aura.accentCyan.copy(alpha = 0.7f)
                } else {
                    aura.borderSubtle
                }
            ),
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (state.isDownloadedReadyToInstall) {
                            Icons.Rounded.SystemUpdate
                        } else {
                            Icons.Rounded.DownloadForOffline
                        },
                        contentDescription = if (isArabic) "تحديث التطبيق" else "App Update",
                        tint = if (state.isDownloadedReadyToInstall) {
                            aura.accentCyan
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(22.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        val title = when {
                            state.isInstalling -> if (isArabic) {
                                "جارٍ تثبيت التحديث..."
                            } else {
                                "Installing update..."
                            }

                            state.isDownloadedReadyToInstall -> if (isArabic) {
                                "التحديث جاهز للتثبيت"
                            } else {
                                "Update ready to install"
                            }

                            else -> if (isArabic) {
                                "جارٍ تنزيل التحديث (${state.progressPercent}%)"
                            } else {
                                "Downloading update (${state.progressPercent}%)"
                            }
                        }
                        Text(
                            text = title,
                            color = aura.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val subtitle = when {
                            state.isDownloadedReadyToInstall -> if (isArabic) {
                                "أعد تشغيل التطبيق لإكمال التثبيت"
                            } else {
                                "Restart to complete installation"
                            }

                            state.totalBytesToDownload > 0L ->
                                "${AppUpdateCheckerText(state.bytesDownloaded)} / ${AppUpdateCheckerText(state.totalBytesToDownload)}"

                            else -> if (isArabic) {
                                "يتم التحميل عبر Google Play..."
                            } else {
                                "Downloading via Google Play..."
                            }
                        }
                        Text(
                            text = subtitle,
                            color = aura.textSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (state.isDownloadedReadyToInstall) {
                        Button(
                            onClick = onInstallNow,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = aura.accentCyan,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("play_in_app_update_install_button")
                        ) {
                            Text(
                                text = if (isArabic) "تثبيت الآن" else "Restart",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                if (state.isDownloading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (state.totalBytesToDownload > 0L) {
                        LinearProgressIndicator(
                            progress = { state.downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = aura.borderSubtle
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = aura.borderSubtle
                        )
                    }
                }
            }
        }
    }
}

private fun AppUpdateCheckerText(bytes: Long): String {
    return com.example.data.update.AppUpdateChecker.formatBytes(bytes).ifBlank { "0 KB" }
}
