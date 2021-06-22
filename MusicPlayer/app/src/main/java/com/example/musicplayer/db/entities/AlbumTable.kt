package com.example.musicplayer.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumTable(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val albumId: Long
)