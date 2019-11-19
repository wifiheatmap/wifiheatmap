package com.wifiheatmap.wifiheatmap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    lateinit var wifiManager: WifiManager
    lateinit var results: List<ScanResult>
    var arrayList: ArrayList<Any> = arrayListOf()
    var scanResultManager: ScanResultManager = ScanResultManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show()
            wifiManager.isWifiEnabled = true
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    // if you want to call this the syntax is *scanWifi(:: yourfunctionname)
    fun scanWifi(callbackFun: (List<ScanResult>) -> Unit): Unit {

        val wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                results = wifiManager.scanResults
                unregisterReceiver(this)


                var nonDuplicatedResults2 : List<ScanResult> = scanResultManager.removeDuplicatesFromScanResults(results)

                for (result in nonDuplicatedResults2) {
                    arrayList.add(result.SSID + " | Wi-Fi Strength: " + result.level)
                    // adapter.notifyDataSetChanged()
                }
                println("WITHIN THE RECEIVER!!!!!!")
                // call that callback function passing the list of scan results
                callbackFun(nonDuplicatedResults2)
            }
        }

        arrayList.clear()
        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
        Toast.makeText(this, "Scanning WiFi ... ", Toast.LENGTH_SHORT).show()

    }

}
