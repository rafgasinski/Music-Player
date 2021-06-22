package com.example.musicplayer.db.repositories

import androidx.lifecycle.LiveData
import com.example.musicplayer.db.dao.FavoriteTableDao
import com.example.musicplayer.db.entities.FavoriteTable

class FavoriteTableRepository(private val favoriteTableDao: FavoriteTableDao) {

    val selectAll : LiveData<List<FavoriteTable>> = favoriteTableDao.selectAll()

    suspend fun addItem(favoriteTable : FavoriteTable){
        favoriteTableDao.insertFavorite(favoriteTable)
    }

    suspend fun deleteItem(id : Long){
        favoriteTableDao.deleteFavorite(id)
    }

    suspend fun musicExist(id : Long) : FavoriteTable {
        return favoriteTableDao.musicExist(id)
    }
}