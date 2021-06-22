package com.example.musicplayer.player.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.example.musicplayer.BuildConfig
import com.example.musicplayer.R
import com.example.musicplayer.music.Parent
import com.example.musicplayer.music.Track
import com.example.musicplayer.player.state.LoopMode
import com.example.musicplayer.utils.loadBitmap


@SuppressLint("RestrictedApi")
class PlayerNotification private constructor(
    context: Context,
    mediaToken: MediaSessionCompat.Token
) : NotificationCompat.Builder(context, CHANNEL_ID){
    init {
        setSmallIcon(R.drawable.ic_logo_notification)
        setCategory(NotificationCompat.CATEGORY_SERVICE)
        setShowWhen(false)
        setSilent(true)
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        addAction(buildShuffleAction(context, false))
        addAction(buildAction(context, ACTION_SKIP_PREV, R.drawable.ic_previous_notification))
        addAction(buildPlayPauseAction(context, true))
        addAction(buildAction(context, ACTION_SKIP_NEXT, R.drawable.ic_next_notification))

        setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaToken)
        )
    }

    fun setMetadata(context: Context, track: Track, useAlbumArt: Boolean, onDone: () -> Unit) {
        setContentTitle(track.name)
        setContentText(track.artistName)

        if (useAlbumArt) {
            loadBitmap(context, track) { bitmap ->
                setLargeIcon(bitmap)
                onDone()
            }
        }
    }

    fun setShuffle(context: Context, isShuffling: Boolean) {
        mActions[0] = buildShuffleAction(context, isShuffling)
    }

    fun setPlaying(context: Context, isPlaying: Boolean) {
        mActions[2] = buildPlayPauseAction(context, isPlaying)
    }

    fun setParent(context: Context, parent: Parent?) {
        setSubText(parent?.displayName ?: context.getString(R.string.all_tracks))
    }

    private fun buildPlayPauseAction(
        context: Context,
        isPlaying: Boolean
    ): NotificationCompat.Action {
        val drawableRes = if (isPlaying) R.drawable.ic_pause_notification else R.drawable.ic_play_notification

        return buildAction(context, ACTION_PLAY_PAUSE, drawableRes)
    }

    private fun buildShuffleAction(
        context: Context,
        isShuffled: Boolean
    ): NotificationCompat.Action {
        val drawableRes = if (isShuffled) R.drawable.ic_shuffle_active_notification else R.drawable.ic_shuffle_inactive_notification

        return buildAction(context, ACTION_SHUFFLE, drawableRes)
    }

    private fun buildAction(
        context: Context,
        actionName: String,
        @DrawableRes iconRes: Int
    ): NotificationCompat.Action {
        val action = NotificationCompat.Action.Builder(
            iconRes, actionName,
            PendingIntent.getBroadcast(
                context, REQUEST_CODE,
                Intent(actionName), PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

        return action.build()
    }

    companion object {
        const val CHANNEL_ID = "CHANNEL_AUDIO_PLAYER"
        const val NOTIFICATION_ID = 0xA001
        const val REQUEST_CODE = 0xB001

        const val ACTION_SHUFFLE = BuildConfig.APPLICATION_ID + ".action.SHUFFLE"
        const val ACTION_SKIP_PREV = BuildConfig.APPLICATION_ID + ".action.PREV"
        const val ACTION_PLAY_PAUSE = BuildConfig.APPLICATION_ID + ".action.PLAY_PAUSE"
        const val ACTION_SKIP_NEXT = BuildConfig.APPLICATION_ID + ".action.NEXT"

        fun create(
            context: Context,
            notificationManager: NotificationManager,
            mediaSession: MediaSessionCompat
        ): PlayerNotification {
            val channel = NotificationChannel(
                CHANNEL_ID, context.getString(R.string.channel_id),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(channel)

            return PlayerNotification(context, mediaSession.sessionToken)
        }
    }
}