package com.wifiheatmap.wifiheatmap.room

import androidx.room.*
import java.util.*
@Entity(foreignKeys = [ForeignKey(entity = Network::class, parentColumns = ["ssid"], childColumns = ["network_ssid"], onDelete = ForeignKey.CASCADE)], indices = [Index(value = ["network_ssid", "latitude", "longitude", "intensity"], unique = true)])
data class Data(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "network_ssid") val networkSsid: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "intensity") val intensity: Int,
    @ColumnInfo(name = "date") val date: Date
)