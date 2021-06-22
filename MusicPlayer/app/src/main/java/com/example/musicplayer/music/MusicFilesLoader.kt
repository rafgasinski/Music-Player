package com.example.musicplayer.music

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.musicplayer.R
import com.example.musicplayer.music.MusicUtils.toAlbumArtURI

class MusicFilesLoader(private val context: Context) {
    var albums = mutableListOf<Album>()
    var artists = mutableListOf<Artist>()
    var tracks = mutableListOf<Track>()

    private val resolver = context.contentResolver

    private var selector = "${MediaStore.Audio.Media.IS_MUSIC}=1"

    fun load() {
        loadAlbums()
        loadTracks()

        linkAlbums()
        buildArtists()
    }

    private fun loadAlbums() {
        val albumCursor = resolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Albums._ID, // 0
                MediaStore.Audio.Albums.ALBUM, // 1
                MediaStore.Audio.Albums.ARTIST, // 2
                MediaStore.Audio.Albums.FIRST_YEAR, // 4
            ),
            null, null,
            MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
        )

        val albumPlaceholder = context.getString(R.string.placeholder_album)
        val artistPlaceholder = context.getString(R.string.placeholder_artist)

        albumCursor?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex) ?: albumPlaceholder
                var artistName = cursor.getString(artistNameIndex) ?: artistPlaceholder
                val coverUri = id.toAlbumArtURI()

                if (artistName == MediaStore.UNKNOWN_STRING) {
                    artistName = artistPlaceholder
                }

                albums.add(Album(id, name, artistName, coverUri))
            }
        }

        albums = albums.distinctBy {
            it.name to it.artistName
        }.toMutableList()

        albums.sortBy { it.name }
    }

    @SuppressLint("InlinedApi")
    private fun loadTracks() {
        val cursor = resolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
            ),
            selector, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        )

        cursor?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val fileIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val fileName = cursor.getString(fileIndex)
                val title = cursor.getString(titleIndex) ?: fileName
                val albumId = cursor.getLong(albumIndex)
                val track = cursor.getInt(trackIndex)
                val artist = cursor.getString(artistIndex)
                val duration = cursor.getLong(durationIndex)

                tracks.add(Track(id, title, fileName, albumId, track, artist, duration))
            }
        }

        tracks = tracks.distinctBy {
            it.name to it.albumId to it.positionInAlbum to it.duration
        }.toMutableList()
    }

    private fun linkAlbums() {
        val tracksByAlbum = tracks.groupBy { it.albumId }
        val unknownAlbum = Album(
            id = -1,
            name = context.getString(R.string.placeholder_album),
            artistName = context.getString(R.string.placeholder_artist),
            coverUri = Uri.EMPTY,
        )

        tracksByAlbum.forEach { entry ->
            (albums.find { it.id == entry.key } ?: unknownAlbum).linkTracks(entry.value)
        }

        albums.removeAll { it.tracks.isEmpty() }

        if (unknownAlbum.tracks.isNotEmpty()) {
            albums.add(unknownAlbum)
        }
    }

    private fun buildArtists() {
        val tracksByArtist = tracks.groupBy { it.artistName }
        tracksByArtist.forEach { entry ->
            artists.add(
                Artist(
                    id = (Int.MIN_VALUE + artists.size).toLong(),
                    name = entry.key,
                    tracks = entry.value
                )
            )
        }

        tracksByArtist.forEach { entry ->
            (artists.find { it.name == entry.key })?.linkTracks(entry.value)
        }
    }

}