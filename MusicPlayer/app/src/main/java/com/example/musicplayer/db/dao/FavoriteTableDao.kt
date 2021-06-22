package com.example.musicplayer.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.musicplayer.db.entities.FavoriteTable

@Dao
interface FavoriteTableDao {
    @Insert
    suspend fun insertFavorite(favoriteTable : FavoriteTable)

    @Query("DELETE FROM favorites WHERE trackId=:id")
    suspend fun deleteFavorite(id: Long)

    @Query("SELECT * FROM favorites")
    fun selectAll() : LiveData<List<FavoriteTable>>

    @Query("SELECT * FROM favorites WHERE trackId=:id")
    suspend fun musicExist(id : Long) : FavoriteTable

}