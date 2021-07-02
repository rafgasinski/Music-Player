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
    val currentIndex: MutableLiveData<Int> get() = mIndex

    val isSeeking: LiveData<Boolean> get() = mIsSeeking

    val formattedPosition = Transformations.map(mPosition) {
        it.toDuration()
    }

    val positionAsProgress = Transformations.map(mPosition) {
        if (mTrack.value != null) it.toInt() else 0
    }

    private val playerManager = PlayerStateManager.getInstance()
    private val musicStore = MusicStore.getInstance()
    private val preferencesManager = PreferencesManager.getInstance()

    init {
        playerManager.addCallback(this)
    }

    fun playTrack(track: Track, mode: Int) {
        preferencesManager.queueConstructorMode = mode
        playerManager.playTrack(track, QueueConstructor.fromInt(mode)!!)
    }


    fun playAlbum(album: Album, shuffled: Boolean) {
        if (album.tracks.isEmpty()) {
            return
        }

        preferencesManager.queueConstructorMode = QueueConstructor.IN_ALBUM.toInt()
        playerManager.playParent(album, shuffled)
    }

    fun playArtist(artist: Artist, shuffled: Boolean) {
        if (artist.tracks.isEmpty()) {
            return
        }

        playerManager.playParent(artist, shuffled)
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
        playerManager.shuffleAll()
    }

    fun playFavorites(shuffled: Boolean) {
        playerManager.playFavorites(shuffled)
    }

    fun setPosition(progress: Int) {
        playerManager.seekTo((progress * 1000).toLong())
    }

    fun updatePositionDisplay(progress: Int) {
        mPosition.value = progress.toLong()
    }

    fun skipNext() {
        playerManager.next()
    }

    fun skipPrev(doRewind : Boolean) {
        playerManager.prev(doRewind)
    }

    fun invertPlayingStatus() {
        playerManager.setPlaying(!playerManager.isPlaying)
    }

    fun invertShuffleStatus() {
        playerManager.setShuffling(!playerManager.isShuffling, keepSong = true)
    }

    fun incrementLoopStatus() {
        playerManager.setLoopMode(playerManager.loopMode.increment())
    }

    fun setSeekingStatus(isSeeking: Boolean) {
        mIsSeeking.value = isSeeking
    }

    override fun onCleared() {
        playerManager.removeCallback(this)
    }

    override fun onTrackUpdate(track: Track?) {
        mTrack.value = track
        track?.let {
            mCurrentPlayingAlbum.value = track.album
            setFavorite(it)
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

    private fun setFavorite(track: Track) {
        playerManager.setFavorite(musicStore.favoriteTracks.any { it.id == track.id })
    }
}