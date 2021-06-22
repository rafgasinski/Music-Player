package com.example.musicplayer.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.musicplayer.db.entities.AlbumTable

@Dao
interface AlbumTableDao {
    @Insert
    suspend fun insertAlbum(albumTable: AlbumTable)

    @Query("DELETE FROM albums WHERE albumId=:id")
    suspend fun deleteAlbum(id: Long)

    @Query("SELECT * FROM albums")
    fun selectAll() : LiveData<List<AlbumTable>>

    @Query("SELECT * FROM albums WHERE albumId=:id")
    suspend fun albumExist(id : Long) : AlbumTable
}