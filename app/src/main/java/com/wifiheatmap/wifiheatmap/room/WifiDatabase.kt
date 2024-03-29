package com.wifiheatmap.wifiheatmap.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Network::class, Data::class], version = 2)
@TypeConverters(DateConverter::class)
abstract class WifiDatabase: RoomDatabase() {
    abstract fun dataAccessObject(): DataAccessObject
    companion object{
        private var instance: WifiDatabase? = null
        fun getInstance(context: Context): WifiDatabase {
            if(instance == null) {
                // TODO remove fallbackToDestructiveMigration
                instance = Room.databaseBuilder(context, WifiDatabase::class.java, "wifi_db").fallbackToDestructiveMigration().build()
            }
            return instance as WifiDatabase
        }
    }
}