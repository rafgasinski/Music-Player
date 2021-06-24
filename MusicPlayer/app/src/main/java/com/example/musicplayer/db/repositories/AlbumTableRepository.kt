package com.example.musicplayer.db.repositories

import androidx.lifecycle.LiveData
import com.example.musicplayer.db.dao.AlbumTableDao
import com.example.musicplayer.db.entities.AlbumTable

class AlbumTableRepository(private val albumTableDao: AlbumTableDao) {

    fun selectAllFlow() = albumTableDao.selectAllFlow()

    suspend fun addItem(albumTable : AlbumTable){
        albumTableDao.insertAlbum(albumTable)
    }

    suspend fun deleteItem(id : Long){
        albumTableDao.deleteAlbum(id)
    }

    suspend fun albumExist(id : Long) : AlbumTable {
        return albumTableDao.albumExist(id)
    }
}