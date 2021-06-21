package com.example.musicplayer.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlbumTableDao {
    @Insert
    suspend fun InsertAlbum(albumTable: AlbumTable)

    @Query(
        "DELETE FROM albums WHERE albumID=:id"
    )
    suspend fun DeleteAlbum(id: Long)

    @Query(
        "SELECT * FROM albums"
    )
    fun SelectAll() : LiveData<List<AlbumTable>>

    @Query(
        "SELECT * FROM albums WHERE albumID=:id"
    )
    suspend fun AlbumExist(id : Long) : AlbumTable
}