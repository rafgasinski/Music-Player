package com.example.musicplayer.player.service

import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioManager
import androidx.core.animation.addListener
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.example.musicplayer.player.state.PlayerStateManager
import com.example.musicplayer.utils.getSystemServiceSafe
import com.google.android.exoplayer2.SimpleExoPlayer

class Audio(
    context: Context,
    private val player: SimpleExoPlayer
) : AudioManager.OnAudioFocusChangeListener {
    private val audioManager = context.getSystemServiceSafe(AudioManager::class)

    private val playbackManager = PlayerStateManager.getInstance()

    private val request = AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
        .setWillPauseWhenDucked(true)
        .setOnAudioFocusChangeListener(this)
        .build()

    private var pauseWasTransient = false

    fun requestFocus() {
        AudioManagerCompat.requestAudioFocus(audioManager, request)
    }

    fun release() {
        AudioManagerCompat.abandonAudioFocusRequest(audioManager, request)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> onGain()
            AudioManager.AUDIOFOCUS_LOSS -> onLossPermanent()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> onLossTransient()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> onDuck()
        }
    }

    private fun onGain() {
        if (player.volume == VOLUME_DUCK) {
            unDuck()
        } else if (pauseWasTransient) {
            playbackManager.setPlaying(true)
            pauseWasTransient = false
        }
    }

    private fun onLossTransient() {
        if (playbackManager.isPlaying) {
            playbackManager.setPlaying(false)
            pauseWasTransient = true
        }
    }

    private fun onLossPermanent() {
        playbackManager.setPlaying(false)
    }

    private fun onDuck() {
        player.volume = VOLUME_DUCK
    }

    private fun unDuck() {
        player.volume = VOLUME_DUCK

        ValueAnimator().apply {
            setFloatValues(VOLUME_DUCK, VOLUME_FULL)
            duration = DUCK_DURATION
            addListener(
                onStart = { player.volume = VOLUME_DUCK },
                onCancel = { player.volume = VOLUME_FULL },
                onEnd = { player.volume = VOLUME_FULL }
            )
            addUpdateListener {
                player.volume = animatedValue as Float
            }
            start()
        }
    }

    companion object {
        private const val VOLUME_DUCK = 0.2f
        private const val DUCK_DURATION = 1500L
        private const val VOLUME_FULL = 1.0f
    }
}