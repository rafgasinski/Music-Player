package com.example.musicplayer.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.musicplayer.db.entities.AlbumTable
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumTableDao {
    @Insert
    suspend fun insertAlbum(albumTable: AlbumTable)

    @Query("DELETE FROM albums WHERE albumId=:id")
    suspend fun deleteAlbum(id: Long)

    @Query("SELECT * FROM albums")
    fun selectAllFlow() : Flow<List<AlbumTable>>

    @Query("SELECT * FROM albums WHERE albumId=:id")
    suspend fun albumExist(id : Long) : AlbumTable
}