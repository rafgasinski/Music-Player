package com.example.musicplayer.player.state

enum class LoopMode {
    NONE, ALL, TRACK;

    fun increment(): LoopMode {
        return when (this) {
            NONE -> ALL
            ALL -> TRACK
            TRACK -> NONE
        }
    }

    fun toInt(): Int {
        return when (this) {
            NONE -> CONST_NONE
            ALL -> CONST_ALL
            TRACK -> CONST_TRACK
        }
    }

    companion object {
        const val CONST_NONE = 0xA100
        const val CONST_ALL = 0xB101
        const val CONST_TRACK = 0xC102
    }
}