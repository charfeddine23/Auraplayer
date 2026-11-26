package com.example.data.update

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class PlayUpdateCheckOutcome {
    data object StartedInAppFlow : PlayUpdateCheckOutcome()
    data object ReadyToInstall : PlayUpdateCheckOutcome()
    data object UpToDate : PlayUpdateCheckOutcome()
    data class UnavailableOnPlay(val reason: String) : PlayUpdateCheckOutcome()
}

data class PlayInAppUpdateUiState(
    val isDownloading: Boolean = false,
    val bytesDownloaded: Long = 0L,
    val totalBytesToDownload: Long = 0L,
    val isDownloadedReadyToInstall: Boolean = false,
    val isInstalling: Boolean = false,
    val lastErrorMessage: String? = null
) {
    val downloadProgress: Float
        get() = if (totalBytesToDownload > 0L) {
            (bytesDownloaded.toFloat() / totalBytesToDownload.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val progressPercent: Int
        get() = (downloadProgress * 100f).toInt().coerceIn(0, 100)
}

/**
 * Google Play Official In-App Updates controller supporting both Flexible and Immediate
 * update flows via Play Core (`app-update-ktx`), strictly compliant with Google Play Policy.
 */
class PlayInAppUpdateController(
    context: Context,
    injectedManager: AppUpdateManager? = null
) {
    private val appUpdateManager: AppUpdateManager? =
        injectedManager ?: runCatching {
            AppUpdateManagerFactory.create(context.applicationContext)
        }.getOrNull()

    private val _uiState = MutableStateFlow(PlayInAppUpdateUiState())
    val uiState: StateFlow<PlayInAppUpdateUiState> = _uiState.asStateFlow()

    private var activeUpdateType: Int? = null
    private var isListenerRegistered = false

    private val installStateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.PENDING,
            InstallStatus.DOWNLOADING -> {
                _uiState.update {
                    it.copy(
                        isDownloading = true,
                        bytesDownloaded = state.bytesDownloaded(),
                        totalBytesToDownload = state.totalBytesToDownload(),
                        isDownloadedReadyToInstall = false,
                        isInstalling = false,
                        lastErrorMessage = null
                    )
                }
            }

            InstallStatus.DOWNLOADED -> {
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        isDownloadedReadyToInstall = true,
                        isInstalling = false,
                        lastErrorMessage = null
                    )
                }
            }

            InstallStatus.INSTALLING -> {
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        isDownloadedReadyToInstall = false,
                        isInstalling = true,
                        lastErrorMessage = null
                    )
                }
            }

            InstallStatus.INSTALLED -> {
                activeUpdateType = null
                _uiState.value = PlayInAppUpdateUiState()
            }

            InstallStatus.CANCELED -> {
                activeUpdateType = null
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        isInstalling = false
                    )
                }
            }

            InstallStatus.FAILED -> {
                activeUpdateType = null
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        isInstalling = false,
                        lastErrorMessage = "Install error (${state.installErrorCode()})"
                    )
                }
            }

            else -> Unit
        }
    }

    fun registerListener() {
        val manager = appUpdateManager ?: return
        if (!isListenerRegistered) {
            runCatching {
                manager.registerListener(installStateListener)
                isListenerRegistered = true
            }
        }
    }

    fun unregisterListener() {
        val manager = appUpdateManager ?: return
        if (isListenerRegistered) {
            runCatching {
                manager.unregisterListener(installStateListener)
            }
            isListenerRegistered = false
        }
    }

    suspend fun checkAndStartIfAvailable(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        isManualCheck: Boolean = false
    ): PlayUpdateCheckOutcome {
        val manager = appUpdateManager
            ?: return PlayUpdateCheckOutcome.UnavailableOnPlay("Play Core manager unavailable")

        val info = fetchAppUpdateInfo(manager)
            ?: return PlayUpdateCheckOutcome.UnavailableOnPlay("Play Store update info unavailable")

        if (info.installStatus() == InstallStatus.DOWNLOADED) {
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    isDownloadedReadyToInstall = true,
                    isInstalling = false,
                    lastErrorMessage = null
                )
            }
            return PlayUpdateCheckOutcome.ReadyToInstall
        }

        if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS &&
            info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
        ) {
            val started = startUpdateFlow(manager, info, AppUpdateType.IMMEDIATE, launcher)
            return if (started) {
                PlayUpdateCheckOutcome.StartedInAppFlow
            } else {
                PlayUpdateCheckOutcome.UnavailableOnPlay("Could not resume immediate update")
            }
        }

        if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            val selectedType = chooseUpdateType(info, isManualCheck)
                ?: return PlayUpdateCheckOutcome.UnavailableOnPlay("Update type not allowed")

            val started = startUpdateFlow(manager, info, selectedType, launcher)
            return if (started) {
                PlayUpdateCheckOutcome.StartedInAppFlow
            } else {
                PlayUpdateCheckOutcome.UnavailableOnPlay("Failed to launch Play update flow")
            }
        }

        return PlayUpdateCheckOutcome.UpToDate
    }

    fun resumeIfInProgressOrDownloaded(
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val manager = appUpdateManager ?: return
        runCatching {
            manager.appUpdateInfo.addOnSuccessListener { info ->
                when {
                    info.installStatus() == InstallStatus.DOWNLOADED -> {
                        _uiState.update {
                            it.copy(
                                isDownloading = false,
                                isDownloadedReadyToInstall = true,
                                isInstalling = false
                            )
                        }
                    }

                    info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS &&
                        info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                        startUpdateFlow(manager, info, AppUpdateType.IMMEDIATE, launcher)
                    }
                }
            }
        }
    }

    fun completeFlexibleUpdate() {
        val manager = appUpdateManager ?: return
        runCatching {
            manager.completeUpdate()
        }.onFailure { err ->
            _uiState.update {
                it.copy(lastErrorMessage = err.localizedMessage ?: "Failed to complete update")
            }
        }
    }

    fun handleActivityResult(resultCode: Int) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                _uiState.update { it.copy(lastErrorMessage = null) }
            }

            Activity.RESULT_CANCELED -> {
                activeUpdateType = null
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        isInstalling = false
                    )
                }
            }

            ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                activeUpdateType = null
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        isInstalling = false,
                        lastErrorMessage = "In-app update failed ($resultCode)"
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(lastErrorMessage = null) }
    }

    internal fun chooseUpdateType(
        info: AppUpdateInfo,
        isManualCheck: Boolean
    ): Int? {
        val isHighPriorityOrStale =
            info.updatePriority() >= HIGH_PRIORITY_THRESHOLD ||
                (info.clientVersionStalenessDays() ?: 0) >= STALE_DAYS_FOR_IMMEDIATE

        return when {
            isHighPriorityOrStale && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) ->
                AppUpdateType.IMMEDIATE
            !isManualCheck && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) ->
                AppUpdateType.FLEXIBLE
            info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) ->
                AppUpdateType.FLEXIBLE
            info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) ->
                AppUpdateType.IMMEDIATE
            else -> null
        }
    }

    private fun startUpdateFlow(
        manager: AppUpdateManager,
        info: AppUpdateInfo,
        updateType: Int,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ): Boolean {
        return runCatching {
            activeUpdateType = updateType
            val options = AppUpdateOptions.newBuilder(updateType).build()
            manager.startUpdateFlowForResult(info, launcher, options)
        }.getOrDefault(false)
    }

    private suspend fun fetchAppUpdateInfo(manager: AppUpdateManager): AppUpdateInfo? =
        suspendCancellableCoroutine { cont ->
            runCatching {
                manager.appUpdateInfo
                    .addOnSuccessListener { info ->
                        if (cont.isActive) cont.resume(info)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
            }.onFailure {
                if (cont.isActive) cont.resume(null)
            }
        }

    companion object {
        private const val HIGH_PRIORITY_THRESHOLD = 4
        private const val STALE_DAYS_FOR_IMMEDIATE = 7
    }
}
