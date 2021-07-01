package com.example.musicplayer.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.musicplayer.db.entities.FavoriteTable
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTableDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFavorite(favoriteTable : FavoriteTable)

    @Query("DELETE FROM favorites WHERE musicId=:id AND isTrack=:isTrack")
    suspend fun deleteFavoriteTrack(id: Long, isTrack: Boolean = true)

    @Query("DELETE FROM favorites WHERE musicId=:id AND isTrack=:isTrack")
    suspend fun deleteFavoriteAlbum(id: Long, isTrack: Boolean = false)

    @Query("SELECT * FROM favorites")
    fun selectAllFlow() : Flow<List<FavoriteTable>>

    @Query("SELECT * FROM favorites WHERE isTrack==:isTrack")
    fun selectAllTypeFlow(isTrack: Boolean) : Flow<List<FavoriteTable>>

}