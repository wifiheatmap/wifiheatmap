package com.wifiheatmap.wifiheatmap.room

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface DataAccessObject {
    @Query("SELECT * FROM network")
    fun getNetworks(): LiveData<List<Network>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNetwork(vararg networks: Network?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertData(vararg data: Data?)

    @Delete
    fun deleteNetworks(vararg networks: Network?)

    @Delete
    fun deleteData(vararg data: Data?)

    @Query("SELECT * FROM data WHERE data.network_ssid = :networkSsid")
    fun getData(networkSsid : String): LiveData<List<Data>>

    @Query("SELECT * FROM data WHERE data.network_ssid = :networkSsid AND data.latitude > :minLatitude AND data.longitude > :minLongitude AND data.latitude < :maxLatitude AND data.longitude < :maxLongitude")
    fun getData(networkSsid: String, minLatitude: Double, minLongitude: Double, maxLatitude: Double, maxLongitude: Double): LiveData<List<Data>>

    @Query("SELECT * FROM data WHERE data.network_ssid = :networkSsid AND data.latitude > :minLatitude AND data.longitude > :minLongitude AND data.latitude < :maxLatitude AND data.longitude < :maxLongitude AND data.date > :minDate AND data.date < :maxDate")
    fun getData(networkSsid: String, minLatitude: Double, minLongitude: Double, maxLatitude: Double, maxLongitude: Double, minDate: Date, maxDate: Date): LiveData<List<Data>>

    @Query("SELECT CASE WHEN EXISTS (SELECT * FROM network, data WHERE network.ssid = data.network_ssid AND network.ssid = :ssid) THEN 1 ELSE 0 END")
    fun getRecordExists(ssid: String): LiveData<Boolean>
}