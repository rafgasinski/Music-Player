package com.example.musicplayer.music

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.musicplayer.R
import com.example.musicplayer.music.MusicUtils.extractInt
import com.example.musicplayer.music.MusicUtils.removePrefixes
import com.example.musicplayer.music.MusicUtils.toAlbumArtURI
import java.util.*

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
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST
            ),
            null, null, null
        )

        val albumPlaceholder = context.getString(R.string.placeholder_album)
        val artistPlaceholder = context.getString(R.string.placeholder_artist)

        albumCursor?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                var name = cursor.getString(nameIndex)
                var artistName = cursor.getString(artistNameIndex)
                val coverUri = id.toAlbumArtURI()

                if (name == context.getString(R.string.music_placeholder)) {
                    name = albumPlaceholder
                }

                if (artistName == MediaStore.UNKNOWN_STRING) {
                    artistName = artistPlaceholder
                }

                albums.add(Album(id, name, artistName, coverUri))
            }
        }

        albums = albums.distinctBy {
            it.name to it.artistName
        }.toMutableList()

        albums.sortWith { album1, album2 ->
            if (album1.name.contains("\\d".toRegex()) || album2.name.contains("\\d".toRegex())) {
                extractInt(album1.name) - extractInt(album2.name)
            } else {
                album1.name.compareTo(album2.name)
            }
        }
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
            selector, null, null
        )

        val artistPlaceholder = context.getString(R.string.placeholder_artist)

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
                var artist = cursor.getString(artistIndex)
                val duration = cursor.getLong(durationIndex)

                if (artist == MediaStore.UNKNOWN_STRING) {
                    artist = artistPlaceholder
                }

                tracks.add(Track(id, title, fileName, albumId, track, artist, duration))
            }
        }

        tracks = tracks.distinctBy {
            it.name to it.albumId to it.positionInAlbum to it.duration
        }.toMutableList()

        tracks.sortWith { track1, track2 ->
            val name1 = track1.name.replace("[^\\p{L}\\p{N}\\s]+".toRegex(), "")
            val name2 = track2.name.replace("[^\\p{L}\\p{N}\\s]+".toRegex(), "")

            return@sortWith name1.removePrefixes.compareTo(name2.removePrefixes, true)
        }

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

        artists.sortBy { it.name }
    }

}