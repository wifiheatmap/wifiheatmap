package com.wifiheatmap.wifiheatmap.room

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData

class WifiRepository(app: Application) {
    private var dataAccessObject: DataAccessObject

    init {
        val database: WifiDatabase = WifiDatabase.getInstance(app)
        dataAccessObject = database.dataAccessObject()
    }

    fun getNetworks(): LiveData<List<Network>> {
        return dataAccessObject.getNetworks();
    }

    fun getData(networkId: Int): LiveData<List<Data>> {
        return dataAccessObject.getData(networkId)
    }

    fun getData(networkId: Int, minLatitude: Double, minLongitude: Double, maxLatitude: Double, maxLongitude: Double): LiveData<List<Data>> {
        return dataAccessObject.getData(networkId, minLatitude, minLongitude, maxLatitude, maxLongitude)
    }

    class AsyncInsertNetwork(val dataAccessObject: DataAccessObject): AsyncTask<Network, Void, Unit>() {
        override fun doInBackground(vararg params: Network?) {
            dataAccessObject.insertNetwork(*params)
        }
    }

    fun insertNetwork(network: Network) {
        AsyncInsertNetwork(dataAccessObject).execute(network)
    }

    class AsyncInsertData(val dataAccessObject: DataAccessObject): AsyncTask<Data, Void, Unit>() {
        override fun doInBackground(vararg params: Data?) {
            dataAccessObject.insertData(*params)
        }
    }

    fun insertData(data: Data) {
        AsyncInsertData(dataAccessObject).execute(data)
    }

    class AsyncDeleteNetwork(val dataAccessObject: DataAccessObject): AsyncTask<Network, Void, Unit>() {
        override fun doInBackground(vararg params: Network?) {
            dataAccessObject.deleteNetworks(*params)
        }
    }

    fun deleteNetwork(network: Network) {
        AsyncDeleteNetwork(dataAccessObject).execute(network)
    }

    class AsyncDeleteData(val dataAccessObject: DataAccessObject): AsyncTask<Data, Void, Unit>() {
        override fun doInBackground(vararg params: Data?) {
            dataAccessObject.deleteData(*params)
        }
    }

    fun deleteData(data: Data) {
        AsyncDeleteData(dataAccessObject).execute(data)
    }

    fun getRecordExists(ssid: String): LiveData<Boolean> {
        return dataAccessObject.getRecordExists(ssid)
    }

}