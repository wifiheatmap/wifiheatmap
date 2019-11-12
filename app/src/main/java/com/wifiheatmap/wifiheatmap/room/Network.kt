package com.wifiheatmap.wifiheatmap.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Network (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "ssid")
    val ssid: String,
    @ColumnInfo(name = "blacklisted")
    val blacklisted: Boolean
)