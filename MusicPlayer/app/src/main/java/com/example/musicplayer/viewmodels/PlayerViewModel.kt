package com.example.musicplayer.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.example.musicplayer.music.*
import com.example.musicplayer.music.MusicUtils.toDuration
import com.example.musicplayer.player.state.LoopMode
import com.example.musicplayer.player.state.PlayerStateManager
import com.example.musicplayer.player.state.QueueConstructor
import com.example.musicplayer.utils.PreferencesManager

class PlayerViewModel : ViewModel(), PlayerStateManager.Callback {

    private val mTrack = MutableLiveData<Track?>()
    private val mParent = MutableLiveData<Parent?>()
    private val mPosition = MutableLiveData(0L)

    private val mQueue = MutableLiveData(listOf<Track>())
    private val mIndex = MutableLiveData(0)
    private val mMode = MutableLiveData(QueueConstructor.ALL_TRACKS)

    private val mIsPlaying = MutableLiveData(false)
    private val mIsShuffling = MutableLiveData(false)
    private val mLoopMode = MutableLiveData(LoopMode.NONE)

    private val mIsSeeking = MutableLiveData(false)
    private var mIntentUri: Uri? = null

    private val mCurrentPlayingAlbum = MutableLiveData<Album?>()

    val track: LiveData<Track?> get() = mTrack
    val parent: LiveData<Parent?> get() = mParent
    val position: LiveData<Long> get() = mPosition

    val currentPlayingAlbum: LiveData<Album?> get() = mCurrentPlayingAlbum
    val isPlaying: LiveData<Boolean> get() = mIsPlaying
    val isShuffling: LiveData<Boolean> get() = mIsShuffling
    val loopMode: LiveData<LoopMode> get() = mLoopMode

    val isSeeking: LiveData<Boolean> get() = mIsSeeking

    val formattedPosition = Transformations.map(mPosition) {
        it.toDuration()
    }

    val positionAsProgress = Transformations.map(mPosition) {
        if (mTrack.value != null) it.toInt() else 0
    }

    private val playbackManager = PlayerStateManager.getInstance()
    private val musicStore = MusicStore.getInstance()
    private val preferencesManager = PreferencesManager.getInstance()

    init {
        playbackManager.addCallback(this)
    }

    fun playTrack(track: Track, mode: Int) {
        preferencesManager.queueConstructorMode = mode
        playbackManager.playTrack(track, QueueConstructor.fromInt(mode)!!)
    }


    fun playAlbum(album: Album, shuffled: Boolean) {
        if (album.tracks.isEmpty()) {
            return
        }

        preferencesManager.queueConstructorMode = QueueConstructor.IN_ALBUM.toInt()
        playbackManager.playParent(album, shuffled)
    }

    fun playArtist(artist: Artist, shuffled: Boolean) {
        if (artist.tracks.isEmpty()) {
            return
        }

        playbackManager.playParent(artist, shuffled)
    }

    fun playWithUri(uri: Uri, context: Context) {
        if (musicStore.musicAlreadyLoaded) {
            playWithUriInternal(uri, context)
        } else {
            mIntentUri = uri
        }
    }

    private fun playWithUriInternal(uri: Uri, context: Context) {
        musicStore.findTrackForUri(uri, context.contentResolver)?.let { track ->
            playTrack(track, preferencesManager.queueConstructorMode)
        }
    }

    fun shuffleAll() {
        playbackManager.shuffleAll()
    }

    fun setPosition(progress: Int) {
        playbackManager.seekTo((progress * 1000).toLong())
    }

    fun updatePositionDisplay(progress: Int) {
        mPosition.value = progress.toLong()
    }

    fun skipNext() {
        playbackManager.next()
    }

    fun skipPrev(doRewind : Boolean) {
        playbackManager.prev(doRewind)
    }

    fun invertPlayingStatus() {
        playbackManager.setPlaying(!playbackManager.isPlaying)
    }

    fun invertShuffleStatus() {
        playbackManager.setShuffling(!playbackManager.isShuffling, keepSong = true)
    }

    fun incrementLoopStatus() {
        playbackManager.setLoopMode(playbackManager.loopMode.increment())
    }

    fun setSeekingStatus(isSeeking: Boolean) {
        mIsSeeking.value = isSeeking
    }

    override fun onCleared() {
        playbackManager.removeCallback(this)
    }

    override fun onTrackUpdate(track: Track?) {
        mTrack.value = track
        track?.let {
            mCurrentPlayingAlbum.value = track.album
        }
    }

    override fun onParentUpdate(parent: Parent?) {
        mParent.value = parent
    }

    override fun onPositionUpdate(position: Long) {
        if (!mIsSeeking.value!!) {
            mPosition.value = position / 1000
        }
    }

    override fun onQueueUpdate(queue: List<Track>) {
        mQueue.value = queue
    }

    override fun onIndexUpdate(index: Int) {
        mIndex.value = index
    }

    override fun onModeUpdate(mode: QueueConstructor) {
        mMode.value = mode
    }

    override fun onPlayingUpdate(isPlaying: Boolean) {
        mIsPlaying.value = isPlaying
    }

    override fun onShuffleUpdate(isShuffling: Boolean) {
        mIsShuffling.value = isShuffling
    }

    override fun onLoopUpdate(loopMode: LoopMode) {
        mLoopMode.value = loopMode
    }
}