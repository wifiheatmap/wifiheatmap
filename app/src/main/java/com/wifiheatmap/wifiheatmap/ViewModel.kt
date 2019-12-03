package com.wifiheatmap.wifiheatmap

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.wifiheatmap.wifiheatmap.room.Data
import com.wifiheatmap.wifiheatmap.room.DataAccessObject
import com.wifiheatmap.wifiheatmap.room.Network
import com.wifiheatmap.wifiheatmap.room.WifiRepository

class ViewModel(app: Application): AndroidViewModel(app) {

    val repo: WifiRepository

    init {
        repo = WifiRepository(app)
    }

    fun getNetworks(): LiveData<List<Network>> {
        return repo.getNetworks()
    }

    fun getData(networkId: Int): LiveData<List<Data>> {
        return repo.getData(networkId)
    }

    fun getData(networkId: Int, minLatitude: Double, minLongitude: Double, maxLatitude: Double, maxLongitude: Double): LiveData<List<Data>> {
        return repo.getData(networkId, minLatitude, minLongitude, maxLatitude, maxLongitude)
    }

    fun insertNetwork(network: Network) {
        repo.insertNetwork(network)
    }

    fun insertData(data: Data) {
        repo.insertData(data)
    }

    fun deleteNetwork(network: Network) {
        repo.deleteNetwork(network)
    }

    fun deleteData(data: Data) {
        repo.deleteData(data)
    }

}
