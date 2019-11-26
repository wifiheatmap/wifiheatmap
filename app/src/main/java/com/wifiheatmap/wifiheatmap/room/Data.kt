package com.wifiheatmap.wifiheatmap.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(foreignKeys = [ForeignKey(entity = Network::class, parentColumns = ["id"], childColumns = ["network_id"], onDelete = ForeignKey.CASCADE)])
data class Data(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "network_id") val networkID: Int,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "intensity") val intensity: Int,
    @ColumnInfo(name = "date") val date: Date
)