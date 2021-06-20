package com.example.musicplayer.player.service

import android.content.Context
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.musicplayer.music.Track
import com.example.musicplayer.player.state.LoopMode
import com.example.musicplayer.player.state.PlayerStateManager
import com.example.musicplayer.utils.loadBitmap
import com.google.android.exoplayer2.Player

class PlayerSessionConnection(
    private val context: Context,
    private val player: Player,
    private val mediaSession: MediaSessionCompat
) : PlayerStateManager.Callback, Player.Listener, MediaSessionCompat.Callback() {

    private val playbackManager = PlayerStateManager.getInstance()
    private val emptyMetadata = MediaMetadataCompat.Builder().build()

    init {
        mediaSession.setCallback(this)
        playbackManager.addCallback(this)
        player.addListener(this)

        onTrackUpdate(playbackManager.track)
        onPlayingUpdate(playbackManager.isPlaying)
    }

    fun release() {
        playbackManager.removeCallback(this)
        player.removeListener(this)
    }

    override fun onPlay() {
        playbackManager.setPlaying(true)
    }

    override fun onPause() {
        playbackManager.setPlaying(false)
    }

    override fun onSkipToNext() {
        playbackManager.next()
    }

    override fun onSkipToPrevious() {
        playbackManager.prev(false)
    }

    override fun onSeekTo(position: Long) {
        player.seekTo(position)
    }

    override fun onRewind() {
        playbackManager.rewind()
    }

    override fun onSetRepeatMode(repeatMode: Int) {
        val mode = when (repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_ALL -> LoopMode.ALL
            PlaybackStateCompat.REPEAT_MODE_GROUP -> LoopMode.ALL
            PlaybackStateCompat.REPEAT_MODE_ONE -> LoopMode.TRACK
            else -> LoopMode.NONE
        }

        playbackManager.setLoopMode(mode)
    }

    override fun onSetShuffleMode(shuffleMode: Int) {
        playbackManager.setShuffling(
            shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL ||
                    shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_GROUP,
            true
        )
    }

    override fun onTrackUpdate(track: Track?) {
        if (track == null) {
            mediaSession.setMetadata(emptyMetadata)
            return
        }

        val builder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.name)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, track.name)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artistName)
            .putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, track.artistName)
            .putString(MediaMetadataCompat.METADATA_KEY_COMPOSER, track.artistName)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, track.artistName)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.album.name)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.duration)

        loadBitmap(context, track) { bitmap ->
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            mediaSession.setMetadata(builder.build())
        }
    }

    override fun onPlayingUpdate(isPlaying: Boolean) {
        invalidateSessionState()
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.containsAny(
                Player.EVENT_POSITION_DISCONTINUITY,
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_IS_PLAYING_CHANGED,
                Player.EVENT_REPEAT_MODE_CHANGED,
                Player.EVENT_PLAYBACK_PARAMETERS_CHANGED
            )
        ) {
            invalidateSessionState()
        }
    }

    private fun invalidateSessionState() {
        val state = PlaybackStateCompat.Builder()
            .setActions(ACTIONS)
            .setBufferedPosition(player.bufferedPosition)
            .setState(
                PlaybackStateCompat.STATE_PAUSED,
                player.currentPosition,
                1.0f,
                SystemClock.elapsedRealtime()
            )

        mediaSession.setPlaybackState(state.build())

        state.setState(
            getPlayerState(),
            player.currentPosition,
            1.0f,
            SystemClock.elapsedRealtime()
        )

        mediaSession.setPlaybackState(state.build())
    }

    private fun getPlayerState(): Int {
        if (playbackManager.track == null) {
            return PlaybackStateCompat.STATE_STOPPED
        }

        return if (playbackManager.isPlaying) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }
    }

    companion object {
        const val ACTIONS = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SET_REPEAT_MODE or
                PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_STOP
    }
}