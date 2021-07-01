package com.example.musicplayer.db.repositories

import com.example.musicplayer.db.dao.FavoriteTableDao
import com.example.musicplayer.db.entities.FavoriteTable
import kotlinx.coroutines.flow.Flow

class FavoriteTableRepository(private val favoriteTableDao: FavoriteTableDao) {

    fun selectAllFlow() = favoriteTableDao.selectAllFlow()

    fun selectAllTypeFlow(isTrack: Boolean) : Flow<List<FavoriteTable>> {
        return favoriteTableDao.selectAllTypeFlow(isTrack)
    }

    suspend fun addItem(favoriteTable: FavoriteTable){
        favoriteTableDao.insertFavorite(favoriteTable)
    }

    suspend fun deleteTrack(id: Long){
        favoriteTableDao.deleteFavoriteTrack(id)
    }

    suspend fun deleteAlbum(id: Long){
        favoriteTableDao.deleteFavoriteAlbum(id)
    }

}