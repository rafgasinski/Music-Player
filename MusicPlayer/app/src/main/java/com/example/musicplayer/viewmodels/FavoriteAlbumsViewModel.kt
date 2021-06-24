package com.example.musicplayer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.db.*
import com.example.musicplayer.db.entities.AlbumTable
import com.example.musicplayer.db.repositories.AlbumTableRepository
import com.example.musicplayer.db.repositories.FavoriteTableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FavoriteAlbumsViewModel(application: Application): AndroidViewModel(application) {

    val allFavoriteAlbumsId = MutableLiveData<List<AlbumTable>>()

    private val repository : AlbumTableRepository
    private val albumDao = FavoriteDatabase.getDatabase(application).albumTableDao()

    init {
        repository = AlbumTableRepository(albumDao)

        viewModelScope.launch {
            repository.selectAllFlow().collect {
                allFavoriteAlbumsId.postValue(it)
            }
        }
    }

    fun addAlbum(albumID: Long){
        val album = AlbumTable(id = 0, albumId = albumID)
        viewModelScope.launch(Dispatchers.IO) { repository.addItem(album) }
    }

    fun deleteAlbum(musicID: Long){
        viewModelScope.launch { repository.deleteItem(musicID) }
    }

    fun albumExist(id : Long) : LiveData<AlbumTable> {
        val albumObject = MutableLiveData<AlbumTable>()

        viewModelScope.launch {
            val arrivedData = repository.albumExist(id)
            albumObject.postValue(arrivedData)
        }
        return albumObject
    }
}