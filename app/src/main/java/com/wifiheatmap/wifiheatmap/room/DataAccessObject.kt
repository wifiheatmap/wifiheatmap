package com.wifiheatmap.wifiheatmap.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DataAccessObject {
    @Query("SELECT * FROM network")
    fun getNetworks(): LiveData<List<Network>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Query("SELECT CASE WHEN EXISTS (SELECT * FROM network, data WHERE network.id = data.id AND network.ssid = :ssid) THEN 1 ELSE 0 END")
    fun getRecordExists(ssid: String): LiveData<Boolean>
}