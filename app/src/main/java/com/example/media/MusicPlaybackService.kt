package com.example.media

import android.app.PendingIntent
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.R

/**
 * Foreground Media3 MediaSessionService for background music playback.
 * Registers an official MediaSession with USAGE_MEDIA + AUDIO_CONTENT_TYPE_MUSIC
 * so Android / Samsung One UI classifies Aura Pulse as a real music player and
 * renders the native system media banner on lock screen, quick settings, and notification shade.
 */
@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_ALL
            }

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                attachAudioEffects(audioSessionId)
            }
        })

        val openPlayerIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_LISTEN_PLAYER
            putExtra(EXTRA_OPEN_LISTEN_PLAYER, true)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            1001,
            openPlayerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setChannelName(R.string.music_notification_channel_name)
            .build().apply {
                setSmallIcon(R.drawable.ic_stat_music_note)
            }
        setMediaNotificationProvider(notificationProvider)

        mediaSession = MediaSession.Builder(this, player)
            .setId("aura_music_session")
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        releaseAudioEffects()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        if (instance === this) {
            instance = null
        }
        super.onDestroy()
    }

    private fun attachAudioEffects(audioSessionId: Int) {
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET || audioSessionId == 0) return
        releaseAudioEffects()
        runCatching {
            equalizer = Equalizer(0, audioSessionId).apply { enabled = true }
        }
        runCatching {
            bassBoost = BassBoost(0, audioSessionId).apply { enabled = true }
        }
        runCatching {
            virtualizer = Virtualizer(0, audioSessionId).apply { enabled = pendingSpatialEnabled }
        }
        applyCurrentEffects()
    }

    fun applyCurrentEffects() {
        val eq = equalizer
        if (eq != null) {
            runCatching {
                val bandCount = eq.numberOfBands.toInt()
                val range = eq.bandLevelRange
                val minLevel = range[0].toFloat()
                val maxLevel = range[1].toFloat()
                val bands = pendingEqBands
                for (bandIdx in 0 until bandCount) {
                    val uiIdx = (bandIdx.toFloat() / bandCount.coerceAtLeast(1) * bands.size)
                        .toInt()
                        .coerceIn(0, bands.lastIndex)
                    val db = bands[uiIdx].coerceIn(-12f, 12f)
                    val mB = (db * 100f).coerceIn(minLevel, maxLevel).toInt().toShort()
                    eq.setBandLevel(bandIdx.toShort(), mB)
                }
            }
        }
        val bass = bassBoost
        if (bass != null) {
            runCatching {
                if (bass.strengthSupported) {
                    val targetStrength = (pendingBassBoostPercent.coerceIn(0, 100) * 10).toShort()
                    bass.setStrength(targetStrength)
                }
            }
        }
        val virt = virtualizer
        if (virt != null) {
            runCatching {
                virt.enabled = pendingSpatialEnabled
                if (virt.strengthSupported) {
                    val targetStrength = if (pendingSpatialEnabled) 750.toShort() else 0.toShort()
                    virt.setStrength(targetStrength)
                }
            }
        }
    }

    private fun releaseAudioEffects() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        equalizer = null
        bassBoost = null
        virtualizer = null
    }

    companion object {
        const val CHANNEL_ID = "aura_music_playback_channel"
        const val ACTION_OPEN_LISTEN_PLAYER = "com.example.action.OPEN_LISTEN_PLAYER"
        const val EXTRA_OPEN_LISTEN_PLAYER = "extra_open_listen_player"

        @Volatile
        private var instance: MusicPlaybackService? = null

        @Volatile
        private var pendingEqBands: List<Float> = listOf(6f, 3f, 0f, 2f, 5f)

        @Volatile
        private var pendingBassBoostPercent: Int = 75

        @Volatile
        private var pendingSpatialEnabled: Boolean = true

        fun updateAudioEffects(
            eqBands: List<Float>,
            bassBoostPercent: Int,
            spatialEnabled: Boolean = pendingSpatialEnabled
        ) {
            pendingEqBands = eqBands.toList()
            pendingBassBoostPercent = bassBoostPercent
            pendingSpatialEnabled = spatialEnabled
            instance?.applyCurrentEffects()
        }
    }
}
