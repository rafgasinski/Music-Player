package com.example.musicplayer.player.state

import com.example.musicplayer.music.*

class PlayerStateManager private constructor() {
    private var mTrack: Track? = null
        set(value) {
            field = value
            callbacks.forEach { it.onTrackUpdate(value) }
        }

    private var mPosition: Long = 0
        set(value) {
            field = value
            callbacks.forEach { it.onPositionUpdate(value) }
        }

    private
    var mParent: Parent? = null
        set(value) {
            field = value
            callbacks.forEach { it.onParentUpdate(value) }
        }

    private var mQueue = mutableListOf<Track>()
        set(value) {
            field = value
            callbacks.forEach { it.onQueueUpdate(value) }
        }

    private var mIndex = 0
        set(value) {
            field = value
            callbacks.forEach { it.onIndexUpdate(value) }
        }

    private var mMode = QueueConstructor.ALL_TRACKS
        set(value) {
            field = value
            callbacks.forEach { it.onModeUpdate(value) }
        }

    private var mIsPlaying = false
        set(value) {
            field = value
            callbacks.forEach { it.onPlayingUpdate(value) }
        }

    private var mIsShuffling = false
        set(value) {
            field = value
            callbacks.forEach { it.onShuffleUpdate(value) }
        }

    private var mLoopMode = LoopMode.NONE
        set(value) {
            field = value
            callbacks.forEach { it.onLoopUpdate(value) }
        }

    private var mHasPlayed = false

    val track: Track? get() = mTrack
    val parent: Parent? get() = mParent
    val position: Long get() = mPosition
    val isPlaying: Boolean get() = mIsPlaying
    val isShuffling: Boolean get() = mIsShuffling
    val loopMode: LoopMode get() = mLoopMode
    val hasPlayed: Boolean get() = mHasPlayed

    private val musicStore = MusicStore.getInstance()

    private val callbacks = mutableListOf<Callback>()

    fun addCallback(callback: Callback) {
        callbacks.add(callback)
    }

    fun removeCallback(callback: Callback) {
        callbacks.remove(callback)
    }

    fun playTrack(track: Track, mode: QueueConstructor) {
        when (mode) {
            QueueConstructor.ALL_TRACKS -> {
                mParent = null
                mQueue = musicStore.tracks.toMutableList()
            }

            QueueConstructor.IN_ARTIST -> {
                mParent = track.artist
                mQueue = track.artist.tracks.toMutableList()
            }

            QueueConstructor.IN_ALBUM -> {
                mParent = track.album
                mQueue = track.album.tracks.toMutableList()
            }

            QueueConstructor.FAVORITE_TRACKS -> {
                mParent = null
                mQueue = musicStore.tracks.toMutableList()
            }
        }

        mMode = mode

        updatePlayback(track)
        setShuffling(mIsShuffling, keepSong = true)
    }

    fun playParent(parent: Parent, shuffled: Boolean) {

        mParent = parent
        mIndex = 0

        when (parent) {
            is Album -> {
                mQueue = parent.tracks.toMutableList()
                mMode = QueueConstructor.IN_ALBUM
            }

            is Artist -> {
                mQueue = parent.tracks.toMutableList()
                mMode = QueueConstructor.IN_ARTIST
            }

        }

        setShuffling(shuffled, keepSong = false)
        updatePlayback(mQueue[0])
    }

    fun shuffleAll() {
        mMode = QueueConstructor.ALL_TRACKS
        mQueue = musicStore.tracks.toMutableList()
        mParent = null

        setShuffling(true, keepSong = false)
        updatePlayback(mQueue[0])
    }

    private fun updatePlayback(track: Track, shouldPlay: Boolean = true) {
        mTrack = track
        mPosition = 0

        setPlaying(shouldPlay)
    }

    fun next() {
        if (mIndex < mQueue.lastIndex) {
            mIndex = mIndex.inc()
            updatePlayback(mQueue[mIndex])
        } else {
            mIndex = 0
            updatePlayback(mQueue[mIndex], shouldPlay = mLoopMode == LoopMode.ALL)
        }

        forceQueueUpdate()
    }

    fun prev(doRewind : Boolean) {
        if (doRewind && mPosition >= REWIND_THRESHOLD) {
            rewind()
        } else {
            if (mIndex > 0) {
                mIndex = mIndex.dec()
            }

            updatePlayback(mQueue[mIndex])
            forceQueueUpdate()
        }
    }

    private fun moveQueueItems(from: Int, to: Int): Boolean {
        if (from > mQueue.size || from < 0 || to > mQueue.size || to < 0) {
            return false
        }

        val item = mQueue.removeAt(from)
        mQueue.add(to, item)

        forceQueueUpdate()

        return true
    }

    private fun forceQueueUpdate() {
        mQueue = mQueue
    }

    fun setShuffling(shuffled: Boolean, keepSong: Boolean) {
        mIsShuffling = shuffled

        if (mIsShuffling) {
            genShuffle(keepSong)
        } else {
            resetShuffle(keepSong)
        }
    }

    private fun genShuffle(keepSong: Boolean,) {
        mQueue.shuffle()
        mIndex = 0

        if (keepSong) {
            moveQueueItems(mQueue.indexOf(mTrack), 0)
        } else {
            mTrack = mQueue[0]
        }

        forceQueueUpdate()
    }

    private fun resetShuffle(keepSong: Boolean,) {
        mQueue = when (mMode) {
            QueueConstructor.IN_ARTIST -> (mParent as Artist).tracks.toMutableList()
            QueueConstructor.IN_ALBUM -> (mParent as Album).tracks.sortedBy { it.positionInAlbum }.toMutableList()
            QueueConstructor.ALL_TRACKS -> musicStore.tracks.toMutableList()
            QueueConstructor.FAVORITE_TRACKS -> musicStore.tracks.toMutableList()
        }

        if (keepSong) {
            mIndex = mQueue.indexOf(mTrack)
        }

        forceQueueUpdate()
    }


    fun setPlaying(playing: Boolean) {
        if (mIsPlaying != playing) {
            if (playing) {
                mHasPlayed = true
            }

            mIsPlaying = playing
        }
    }

    fun setPosition(position: Long) {
        mTrack?.let { song ->
            if (position <= song.duration) {
                mPosition = position
            }
        }
    }

    fun seekTo(position: Long) {
        mPosition = position

        callbacks.forEach { it.onSeek(position) }
    }

    fun rewind() {
        seekTo(0)
        setPlaying(true)
    }

    fun setLoopMode(mode: LoopMode) {
        mLoopMode = mode
    }

    fun setHasPlayed(hasPlayed: Boolean) {
        mHasPlayed = hasPlayed
    }

    interface Callback {
        fun onTrackUpdate(track: Track?) {}
        fun onParentUpdate(parent: Parent?) {}
        fun onPositionUpdate(position: Long) {}
        fun onQueueUpdate(queue: List<Track>) {}
        fun onModeUpdate(mode: QueueConstructor) {}
        fun onIndexUpdate(index: Int) {}
        fun onPlayingUpdate(isPlaying: Boolean) {}
        fun onShuffleUpdate(isShuffling: Boolean) {}
        fun onLoopUpdate(loopMode: LoopMode) {}
        fun onSeek(position: Long) {}
    }

    companion object {
        private const val REWIND_THRESHOLD = 3000L

        @Volatile
        private var INSTANCE: PlayerStateManager? = null

        fun getInstance(): PlayerStateManager {
            val currentInstance = INSTANCE

            if (currentInstance != null) {
                return currentInstance
            }

            synchronized(this) {
                val newInstance = PlayerStateManager()
                INSTANCE = newInstance
                return newInstance
            }
        }
    }
}