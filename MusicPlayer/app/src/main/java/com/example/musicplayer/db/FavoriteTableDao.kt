package com.example.musicplayer.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoriteTableDao {
    @Insert
    suspend fun InsertFavorite(favoriteTable : FavoriteTable)

    @Query(
        "DELETE FROM favorites WHERE musicID=:id"
    )
    suspend fun DeleteFavorite(id: Long)

    @Query(
        "SELECT * FROM favorites"
    )
    fun SelectAll() : LiveData<List<FavoriteTable>>

    @Query(
        "SELECT * FROM favorites WHERE musicID=:id"
    )
    suspend fun MusicExist(id : Long) : FavoriteTable

}