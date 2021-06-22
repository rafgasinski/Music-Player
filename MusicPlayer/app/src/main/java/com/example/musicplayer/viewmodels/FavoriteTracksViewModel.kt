package com.example.musicplayer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.db.FavoriteDatabase
import com.example.musicplayer.db.entities.FavoriteTable
import com.example.musicplayer.db.repositories.FavoriteTableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteTracksViewModel(application: Application): AndroidViewModel(application) {
    val repository : FavoriteTableRepository

    var allFavoriteTracksId: LiveData<List<FavoriteTable>>

    init {
        val favoriteDao = FavoriteDatabase.getDatabase(application).favoriteTableDao()
        repository = FavoriteTableRepository(favoriteDao)
        allFavoriteTracksId = repository.selectAll
    }

    fun addMusic(trackId: Long){
        val favorite = FavoriteTable(id = 0, trackId = trackId)
        viewModelScope.launch(Dispatchers.IO) { repository.addItem(favorite) }
    }

    fun deleteMusic(musicID: Long){
        viewModelScope.launch { repository.deleteItem(musicID) }
    }

    fun musicExist(id : Long) : LiveData<FavoriteTable> {
        val musicObject = MutableLiveData<FavoriteTable>()

        viewModelScope.launch {
            val arrivedData = repository.musicExist(id)
            musicObject.postValue(arrivedData)
        }
        return musicObject
    }
}