package com.example.musicplayer.db

import androidx.lifecycle.LiveData

class FavoriteTableRepository(private val favoriteTableDao: FavoriteTableDao) {
    val SelectAll : LiveData<List<FavoriteTable>> = favoriteTableDao.SelectAll()

    suspend fun AddItem(favoriteTable : FavoriteTable){
        favoriteTableDao.InsertFavorite(favoriteTable)
    }

    suspend fun DeleteItem(id : Long){
        favoriteTableDao.DeleteFavorite(id)
    }

    suspend fun MusicExist(id : Long) : FavoriteTable{
        return favoriteTableDao.MusicExist(id)
    }
}