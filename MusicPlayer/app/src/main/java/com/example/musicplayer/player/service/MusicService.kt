package com.example.musicplayer.player.service

import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import com.example.musicplayer.music.MusicUtils.toURI
import com.example.musicplayer.music.Parent
import com.example.musicplayer.music.Track
import com.example.musicplayer.player.state.LoopMode
import com.example.musicplayer.player.state.PlayerStateManager
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.utils.getSystemServiceSafe
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile

class MusicService : Service(), Player.Listener, PlayerStateManager.Callback, PreferencesManager.Callback {
    private lateinit var player: SimpleExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var connector: PlayerSessionConnection

    private lateinit var notification: PlayerNotification
    private lateinit var notificationManager: NotificationManager

    private lateinit var audioReactor: Audio
    private lateinit var wakeLock: PowerManager.WakeLock
    private val systemReceiver = SystemEventReceiver()

    private val playerManager = PlayerStateManager.getInstance()

    private var isForeground = false

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        player = newPlayer()
        player.addListener(this@MusicService)
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build(),
            false
        )

        audioReactor = Audio(this, player)
        wakeLock = getSystemServiceSafe(PowerManager::class).newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, this::class.simpleName
        )

        mediaSession = MediaSessionCompat(this, packageName).apply {
            isActive = true
        }

        connector = PlayerSessionConnection(this, player, mediaSession)

        IntentFilter().apply {
            addAction(PlayerNotification.ACTION_FAVORITE)
            addAction(PlayerNotification.ACTION_PLAY_PAUSE)
            addAction(PlayerNotification.ACTION_SKIP_PREV)
            addAction(PlayerNotification.ACTION_SKIP_NEXT)

            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction(Intent.ACTION_HEADSET_PLUG)

            registerReceiver(systemReceiver, this)
        }

        notificationManager = getSystemServiceSafe(NotificationManager::class)
        notification = PlayerNotification.create(this, notificationManager, mediaSession)

        playerManager.setHasPlayed(playerManager.isPlaying)
        playerManager.addCallback(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        stopForegroundAndNotification()
        unregisterReceiver(systemReceiver)

        player.release()
        connector.release()
        mediaSession.release()
        audioReactor.release()
        releaseWakelock()

        playerManager.removeCallback(this)

        playerManager.setPlaying(false)
    }

    override fun onPlaybackStateChanged(state: Int) {
        when (state) {
            Player.STATE_READY -> {
                startPollingPosition()
                releaseWakelock()
            }

            Player.STATE_ENDED -> playerManager.next()

            else -> {}
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        acquireWakeLock()
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        playerManager.next()
    }

    override fun onPositionDiscontinuity(reason: Int) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            playerManager.setPosition(player.currentPosition)
        }
    }

    override fun onTrackUpdate(track: Track?) {
        if (track != null) {
            player.setMediaItem(MediaItem.fromUri(track.id.toURI()))
            player.prepare()

            notification.setMetadata(
                this, track, true, ::startForegroundOrNotify
            )

            return
        }

        player.stop()
        stopForegroundAndNotification()
    }

    override fun onParentUpdate(parent: Parent?) {
        notification.setParent(this, parent)

        startForegroundOrNotify()
    }

    override fun onPlayingUpdate(isPlaying: Boolean) {
        notification.setPlaying(this, isPlaying)
        startForegroundOrNotify()

        if (isPlaying && !player.isPlaying) {
            player.play()
            audioReactor.requestFocus()
            startPollingPosition()
        } else {
            player.pause()
            stopForeground(false)
        }
    }

    override fun onLoopUpdate(loopMode: LoopMode) {
        player.repeatMode = if (loopMode == LoopMode.TRACK) {
            Player.REPEAT_MODE_ONE
        } else {
            Player.REPEAT_MODE_OFF
        }
    }

    override fun onSeek(position: Long) {
        player.seekTo(position)
    }

    private fun newPlayer(): SimpleExoPlayer {
        val audioRenderer = RenderersFactory { handler, _, audioListener, _, _ ->
            arrayOf(
                MediaCodecAudioRenderer(this, MediaCodecSelector.DEFAULT, handler, audioListener)
            )
        }

        val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)

        return SimpleExoPlayer.Builder(this, audioRenderer)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this, extractorsFactory))
            .build()
    }

    private fun startPollingPosition() {
        val pollFlow = flow {
            while (true) {
                emit(player.currentPosition)
                delay(POS_POLL_INTERVAL)
            }
        }.conflate()

        serviceScope.launch {
            pollFlow.takeWhile { player.isPlaying }
                .collect {
                    playerManager.setPosition(it)
                }
        }
    }

    private fun startForegroundOrNotify() {
        if (playerManager.hasPlayed && playerManager.track != null) {

            if (!isForeground) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        PlayerNotification.NOTIFICATION_ID, notification.build(),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(PlayerNotification.NOTIFICATION_ID, notification.build())
                }
            } else {
                notificationManager.notify(
                    PlayerNotification.NOTIFICATION_ID, notification.build()
                )
            }
        }
    }

    private fun stopForegroundAndNotification() {
        stopForeground(true)
        notificationManager.cancel(PlayerNotification.NOTIFICATION_ID)

        isForeground = false
    }

    private fun acquireWakeLock() {
        wakeLock.acquire(WAKELOCK_TIME)
    }

    private fun releaseWakelock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private inner class SystemEventReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {

                PlayerNotification.ACTION_PLAY_PAUSE -> {
                    playerManager.setPlaying(
                        !playerManager.isPlaying
                    )
                }

                PlayerNotification.ACTION_FAVORITE -> playerManager.setShuffling(
                    !playerManager.isShuffling, keepSong = true
                )

                PlayerNotification.ACTION_SKIP_PREV -> playerManager.prev(true)
                PlayerNotification.ACTION_SKIP_NEXT -> playerManager.next()

                BluetoothDevice.ACTION_ACL_CONNECTED -> resumeFromPlug()
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> pauseFromPlug()

                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                    when (intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)) {
                        AudioManager.SCO_AUDIO_STATE_CONNECTED -> resumeFromPlug()
                        AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> pauseFromPlug()
                    }
                }

                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> pauseFromPlug()

                Intent.ACTION_HEADSET_PLUG -> {
                    when (intent.getIntExtra("state", -1)) {
                        CONNECTED -> resumeFromPlug()
                        DISCONNECTED -> pauseFromPlug()
                    }
                }
            }
        }

        private fun resumeFromPlug() {
            if (playerManager.track != null) {
                playerManager.setPlaying(true)
            }
        }

        private fun pauseFromPlug() {
            if (playerManager.track != null) {
                playerManager.setPlaying(false)
            }
        }
    }

    companion object {
        private const val DISCONNECTED = 0
        private const val CONNECTED = 1
        private const val WAKELOCK_TIME = 25000L
        private const val POS_POLL_INTERVAL = 500L
    }

}