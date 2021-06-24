package com.example.musicplayer.db.repositories

import com.example.musicplayer.db.dao.FavoriteTableDao
import com.example.musicplayer.db.entities.FavoriteTable

class FavoriteTableRepository(private val favoriteTableDao: FavoriteTableDao) {

    fun selectAllFlow() = favoriteTableDao.selectAllFlow()

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