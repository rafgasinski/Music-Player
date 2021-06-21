package com.example.musicplayer.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumTable(
    @PrimaryKey(autoGenerate = true)
    val ID: Int,
    val albumID: Long
)