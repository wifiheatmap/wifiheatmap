package com.wifiheatmap.wifiheatmap.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Network::class, Data::class], version = 1)
abstract class WifiDatabase: RoomDatabase() {
    abstract fun dataAccessObject(): DataAccessObject
    companion object{
        private var instance: WifiDatabase? = null
        fun getInstance(context: Context): WifiDatabase {
            if(instance == null) {
                instance = Room.databaseBuilder(context, WifiDatabase::class.java, "wifi_db").build()
            }
            return instance as WifiDatabase
        }
    }
}