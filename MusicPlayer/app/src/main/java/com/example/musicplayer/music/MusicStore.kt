package com.example.musicplayer.music

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class MusicStore private constructor() {
    private var mArtists = listOf<Artist>()
    val artists: List<Artist> get() = mArtists

    private var mAlbums = listOf<Album>()
    val albums: List<Album> get() = mAlbums

    private var mTracks = listOf<Track>()
    val tracks: List<Track> get() = mTracks

    var musicAlreadyLoaded = false
        private set

    suspend fun load(context: Context): Response {
        return withContext(Dispatchers.IO) {
            loadMusicInternal(context)
        }
    }

    private fun loadMusicInternal(context: Context): Response {
        try {
            val loader = MusicFilesLoader(context)
            loader.load()

            if (loader.tracks.isEmpty()) {
                return Response.NO_MUSIC
            }

            mTracks = loader.tracks
            mAlbums = loader.albums
            mArtists = loader.artists
        } catch (e: Exception) {
            return Response.FAILED
        }

        musicAlreadyLoaded = true

        return Response.SUCCESS
    }

    fun findTrackForUri(uri: Uri, resolver: ContentResolver): Track? {
        val cur = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)

        cur?.use { cursor ->
            cursor.moveToFirst()
            val fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))

            return tracks.find { it.fileName == fileName }
        }

        return null
    }

    enum class Response {
        NO_MUSIC, NO_PERMS, FAILED, SUCCESS
    }

    companion object {
        @Volatile
        private var INSTANCE: MusicStore? = null

        fun getInstance(): MusicStore {
            val currentInstance = INSTANCE

            if (currentInstance != null) {
                return currentInstance
            }

            synchronized(this) {
                val newInstance = MusicStore()
                INSTANCE = newInstance
                return newInstance
            }
        }
    }
}