package com.example.musicplayer.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicplayer.db.dao.FavoriteTableDao
import com.example.musicplayer.db.entities.FavoriteTable

@Database(entities = [FavoriteTable::class], version = 1, exportSchema = false)
abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun favoriteTableDao() : FavoriteTableDao

    companion object {
        @Volatile
        private var INSTANCE : FavoriteDatabase? = null

        fun getDatabase(context: Context) : FavoriteDatabase {
            val tempInstance = INSTANCE

            if(tempInstance != null){
                return tempInstance
            }
            else{
                synchronized(this) {
                    return Room.databaseBuilder(
                        context.applicationContext,
                        FavoriteDatabase::class.java,
                        "mainDatabase"
                    ).fallbackToDestructiveMigration()
                        .build()
                }
            }
        }
    }
}