package com.example.musicplayer.db

import com.example.musicplayer.music.Album

class AlbumTableRepository(private val albumTableDao: AlbumTableDao) {
    suspend fun AddItem(albumTable : AlbumTable){
        albumTableDao.InsertAlbum(albumTable)
    }

    suspend fun DeleteItem(id : Long){
        albumTableDao.DeleteAlbum(id)
    }

    suspend fun AlbumExist(id : Long) : AlbumTable{
        return albumTableDao.AlbumExist(id)
    }
}