package com.example.musicplayer.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.musicplayer.db.FavoriteDatabase
import com.example.musicplayer.db.entities.FavoriteTable
import com.example.musicplayer.db.repositories.FavoriteTableRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class FavoriteTracksViewModel(application: Application): AndroidViewModel(application) {

    val allFavoriteTracksId = MutableLiveData<List<FavoriteTable>>()

    private val repository : FavoriteTableRepository
    private val favoriteDao = FavoriteDatabase.getDatabase(application).favoriteTableDao()

    init {
        repository = FavoriteTableRepository(favoriteDao)

        viewModelScope.launch {
            repository.selectAllFlow().collect {
                allFavoriteTracksId.postValue(it)
            }
        }
    }

    fun addMusic(trackId: Long){
        val favorite = FavoriteTable(id = 0, trackId = trackId)
        viewModelScope.launch { repository.addItem(favorite) }
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