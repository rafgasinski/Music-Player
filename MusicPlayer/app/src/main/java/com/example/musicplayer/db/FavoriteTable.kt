package com.example.musicplayer.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteTable(
    @PrimaryKey(autoGenerate = true)
    val ID: Int,
    val musicID: Long
)