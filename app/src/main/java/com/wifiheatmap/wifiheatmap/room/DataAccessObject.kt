package com.wifiheatmap.wifiheatmap.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DataAccessObject {
    @Query("SELECT * FROM network")
    fun getNetworks(): LiveData<List<Network>>

    @Insert
    fun insertNetwork(vararg networks: Network?)

    @Insert
    fun insertData(vararg data: Data?)

    @Delete
    fun deleteNetworks(vararg networks: Network?)

    @Delete
    fun deleteData(vararg data: Data?)

    @Query("SELECT * FROM data WHERE data.network_id = :networkId")
    fun getData(networkId : Int): LiveData<List<Data>>

    @Query("SELECT * FROM data WHERE data.network_id = :networkId AND data.latitude > :minLatitude AND data.longitude > :minLongitude AND data.latitude < :maxLatitude AND data.longitude < :maxLongitude")
    fun getData(networkId: Int, minLatitude: Double, minLongitude: Double, maxLatitude: Double, maxLongitude: Double): LiveData<List<Data>>
}