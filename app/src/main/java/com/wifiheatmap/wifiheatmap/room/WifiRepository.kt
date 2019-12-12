package com.wifiheatmap.wifiheatmap.room

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import java.util.*

class WifiRepository(app: Application) {
    private var dataAccessObject: DataAccessObject

    init {
        val database: WifiDatabase = WifiDatabase.getInstance(app)
        dataAccessObject = database.dataAccessObject()
    }

    fun getNetworks(): LiveData<List<Network>> {
        return dataAccessObject.getNetworks();
    }

    fun getData(networkSsid: String): LiveData<List<Data>> {
        return dataAccessObject.getData(networkSsid)
    }

    fun getData(networkSsid: String, minLatitude: Double, minLongitude: Double, maxLatitude: Double, maxLongitude: Double): LiveData<List<Data>> {
        return dataAccessObject.getData(networkSsid, minLatitude, minLongitude, maxLatitude, maxLongitude)
    }

    fun getData(networkSsid: String, minLatitude: Double, minLongitude: Double, maxLatitude: Double, maxLongitude: Double, minDate: Date, maxDate: Date): LiveData<List<Data>> {
        return dataAccessObject.getData(networkSsid, minLatitude, minLongitude, maxLatitude, maxLongitude, minDate, maxDate)
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