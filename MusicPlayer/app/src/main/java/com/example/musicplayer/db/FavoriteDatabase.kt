package com.example.musicplayer.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteTable::class, AlbumTable::class], version = 7, exportSchema = false)
abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun favoriteTableDao() : FavoriteTableDao
    abstract fun albumTableDao() : AlbumTableDao

    companion object {
        @Volatile
        private var INSTANCE : FavoriteDatabase? = null

        fun getDatabase(context: Context) : FavoriteDatabase {
            val tempInstance = INSTANCE

            if(tempInstance != null){
                return tempInstance
            }
            else{
                synchronized(this){
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        FavoriteDatabase::class.java,
                        "mainDatabase"
                    ).fallbackToDestructiveMigration()
                        .build()

                    return instance
                }
            }
        }
    }
}