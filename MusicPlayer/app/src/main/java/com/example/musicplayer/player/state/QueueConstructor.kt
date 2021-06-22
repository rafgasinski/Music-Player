package com.example.musicplayer.player.state

enum class QueueConstructor {
    IN_ARTIST,
    IN_ALBUM,
    ALL_TRACKS,
    FAVORITE_TRACKS;

    fun toInt(): Int {
        return when (this) {
            IN_ARTIST -> CONST_IN_ARTIST
            IN_ALBUM -> CONST_IN_ALBUM
            ALL_TRACKS -> CONST_ALL_TRACKS
            FAVORITE_TRACKS -> CONST_FAVORITE_TRACKS
        }
    }

    companion object {
        const val CONST_IN_ARTIST = 0xA103
        const val CONST_IN_ALBUM = 0xA104
        const val CONST_ALL_TRACKS = 0xA105
        const val CONST_FAVORITE_TRACKS = 0xA106

        fun fromInt(constant: Int): QueueConstructor? {
            return when (constant) {
                CONST_IN_ARTIST -> IN_ARTIST
                CONST_IN_ALBUM -> IN_ALBUM
                CONST_ALL_TRACKS -> ALL_TRACKS
                CONST_FAVORITE_TRACKS -> FAVORITE_TRACKS

                else -> null
            }
        }
    }
}