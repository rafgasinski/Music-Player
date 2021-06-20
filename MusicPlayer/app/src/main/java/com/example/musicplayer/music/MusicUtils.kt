package com.example.musicplayer.music

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.text.format.DateUtils
import java.util.*
import java.util.concurrent.TimeUnit

object MusicUtils {

    fun Long.toURI(): Uri {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, this)
    }

    fun Long.toAlbumArtURI(): Uri {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), this)
    }

    fun formatSongDuration(duration: Long): String {
        return String.format(
            Locale.getDefault(), "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    duration
                )
            )
        )
    }

    fun Long.toDuration(): String {
        var duration = DateUtils.formatElapsedTime(this)

        if (duration[0] == '0') {
            duration = duration.slice(1 until duration.length)
        }

        return duration
    }
}