package com.example.musicplayer.music

import android.net.Uri

sealed class BaseModel {
    abstract val id: Long
    abstract val name: String
}

sealed class Parent : BaseModel() {
    abstract val hash: Int

    val displayName: String get() = name
}

data class Track(
    override val id: Long,
    override val name: String,
    val fileName: String,
    val albumId: Long,
    val positionInAlbum: Int,
    val artistName: String,
    val duration: Long
) : BaseModel() {
    private var mAlbum: Album? = null
    private var mArtist: Artist? = null

    val album: Album get() = requireNotNull(mAlbum)
    val artist: Artist get() = requireNotNull(mArtist)

    val seconds = duration / 1000
    val formattedDuration = MusicUtils.formatSongDuration(duration)

    val hash = trackHash()

    fun linkAlbum(album: Album) {
        if (mAlbum == null) {
            mAlbum = album
        }
    }

    fun linkArtist(artist: Artist) {
        if (mArtist == null) {
            mArtist = artist
        }
    }

    private fun trackHash(): Int {
        var result = name.hashCode()
        result = 31 * result + positionInAlbum
        result = 31 * result + duration.hashCode()
        return result
    }
}

data class Artist(
    override val id: Long,
    override val name: String,
    val tracks: List<Track>
) : Parent() {
    val tracksCount = tracks.count()

    fun linkTracks(tracks: List<Track>) {
        for (track in tracks) {
            track.linkArtist(this)
        }
    }

    override val hash = name.hashCode()
}

data class Album(
    override val id: Long,
    override val name: String,
    var artistName: String,
    val coverUri: Uri,
) : Parent() {
    private var mArtist: Artist? = null
    val artist: Artist get() = requireNotNull(mArtist)

    private val mTracks = mutableListOf<Track>()
    val tracks: List<Track> get() = mTracks

    override val hash = albumHash()

    fun linkArtist(artist: Artist) {
        mArtist = artist
    }

    fun linkTracks(tracks: List<Track>) {
        for (track in tracks) {
            track.linkAlbum(this)
            mTracks.add(track)
        }

        mTracks.sortBy { it.positionInAlbum }
        // Until further notice:
        artistName = mTracks[0].artistName
    }

    private fun albumHash(): Int {
        var result = name.hashCode()
        result = 31 * result + artistName.hashCode()
        return result
    }
}