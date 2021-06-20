package com.example.musicplayer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.db.FavoriteDatabase
import com.example.musicplayer.db.FavoriteTable
import com.example.musicplayer.db.FavoriteTableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application): AndroidViewModel(application) {
    private val repository : FavoriteTableRepository
    var allFavoriteTracksID : LiveData<List<FavoriteTable>>

    init {
        val favoriteDao = FavoriteDatabase.getDatabase(application).favoriteTableDao()

        repository = FavoriteTableRepository(favoriteDao)
        allFavoriteTracksID = repository.SelectAll
    }

    fun AddMusic(musicID: Long){
        var favorite = FavoriteTable(ID = 0, musicID = musicID)
        viewModelScope.launch(Dispatchers.IO) { repository.AddItem(favorite) }
    }

    fun DeleteMusic(musicID: Long){
        viewModelScope.launch { repository.DeleteItem(musicID) }
    }

    fun MusicExist(id : Long) : LiveData<FavoriteTable> {
        var musicObject = MutableLiveData<FavoriteTable>()
        viewModelScope.launch {
            val arrivedData = repository.MusicExist(id)
            musicObject.postValue(arrivedData)
        }
        return musicObject
    }
}