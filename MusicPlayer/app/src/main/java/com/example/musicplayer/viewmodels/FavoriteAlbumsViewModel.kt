package com.example.musicplayer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.db.*
import com.example.musicplayer.db.entities.AlbumTable
import com.example.musicplayer.db.repositories.AlbumTableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteAlbumsViewModel(application: Application): AndroidViewModel(application) {

    private val repository : AlbumTableRepository
    var allFavoriteAlbumsId : LiveData<List<AlbumTable>>

    init {
        val albumDao = FavoriteDatabase.getDatabase(application).albumTableDao()
        repository = AlbumTableRepository(albumDao)
        allFavoriteAlbumsId = repository.selectAll
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