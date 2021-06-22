package com.example.musicplayer.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteTable(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val trackId: Long
)