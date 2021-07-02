package com.example.musicplayer.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.musicplayer.db.FavoriteDatabase
import com.example.musicplayer.db.entities.FavoriteTable
import com.example.musicplayer.db.repositories.FavoriteTableRepository
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.player.state.PlayerStateManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class FavoritesViewModel(application: Application): AndroidViewModel(application), PlayerStateManager.Callback {

    val allFavoriteIds = MutableLiveData<List<FavoriteTable>>()
    val allFavoriteTracksIds = MutableLiveData<List<FavoriteTable>>()
    val allFavoriteAlbumsIds = MutableLiveData<List<FavoriteTable>>()

    private val repository : FavoriteTableRepository
    private val favoriteDao = FavoriteDatabase.getDatabase(application).favoriteTableDao()
    private val playerManager = PlayerStateManager.getInstance()
    private val musicStore = MusicStore.getInstance()

    init {
        playerManager.addCallback(this)
        repository = FavoriteTableRepository(favoriteDao)

        viewModelScope.launch {
            repository.selectAllFlow().collect {
                allFavoriteIds.postValue(it)
            }
        }

        viewModelScope.launch {
            repository.selectAllTypeFlow(isTrack = true).collect {
                allFavoriteTracksIds.postValue(it)
            }
        }

        viewModelScope.launch {
            repository.selectAllTypeFlow(isTrack = false).collect {
                allFavoriteAlbumsIds.postValue(it)
            }
        }
    }

    override fun onCleared() {
        playerManager.removeCallback(this)
    }

    override fun onFavoriteUpdate(isFavorite: Boolean) {
        if(isFavorite){
            playerManager.track?.let { deleteTrack(it.id) }
            playerManager.track?.let { addTrack(it.id) }
        } else {
            playerManager.track?.let { deleteTrack(it.id) }
        }
    }

    fun setFavorite(isFavorite: Boolean) {
        playerManager.setFavorite(isFavorite)
    }

    private fun addTrack(trackId: Long){
        val favorite = FavoriteTable(id = 0, musicId = trackId, isTrack = true)
        viewModelScope.launch { repository.addItem(favorite) }
    }

    fun deleteTrack(id: Long){
        viewModelScope.launch { repository.deleteTrack(id) }
    }

    fun addAlbum(albumId: Long){
        val favorite = FavoriteTable(id = 0, musicId = albumId, isTrack = false)
        viewModelScope.launch { repository.addItem(favorite) }
    }

    fun deleteAlbum(id: Long){
        viewModelScope.launch { repository.deleteAlbum(id) }
    }

}