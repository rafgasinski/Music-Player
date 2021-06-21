package com.example.musicplayer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlbumViewModel(application: Application): AndroidViewModel(application) {
    private val repository : AlbumTableRepository

    init {
        val albumDao = FavoriteDatabase.getDatabase(application).albumTableDao()

        repository = AlbumTableRepository(albumDao)
    }

    fun AddAlbum(albumID: Long){
        var album = AlbumTable(ID = 0, albumID = albumID)
        viewModelScope.launch(Dispatchers.IO) { repository.AddItem(album) }
    }

    fun DeleteAlbum(musicID: Long){
        viewModelScope.launch { repository.DeleteItem(musicID) }
    }

    fun AlbumExist(id : Long) : LiveData<AlbumTable> {
        var albumObject = MutableLiveData<AlbumTable>()
        viewModelScope.launch {
            val arrivedData = repository.AlbumExist(id)
            albumObject.postValue(arrivedData)
        }
        return albumObject
    }
}