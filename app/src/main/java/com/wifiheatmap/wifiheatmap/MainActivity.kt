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

    /**
     * Interface allows us to make a simple callback function
     * when scanning for Wi-Fi Networks.
     */
    interface scanResultListener {
        /**
         * Notifies the listener when scan results are available.
         * @param results a [List] of [ScanResult] objects.
         */
        fun onScanResultsAvailable(results : List<ScanResult>)
    }

    lateinit var wifiManager: WifiManager
    lateinit var results: List<ScanResult>
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

    /**
     * Creates an asynchronous call to scan for nearby Wifi networks
     * which is passed through the callback function in the
     * ScanResultListener passed in as a parameter.
     */
    fun scanWifi(scl: scanResultListener) {

        val wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                results = wifiManager.scanResults
                unregisterReceiver(this)


                val nonDuplicatedResults2 : List<ScanResult> = scanResultManager
                    .removeDuplicatesFromScanResults(results)

                // call that callback function passing the list of scan results
                scl.onScanResultsAvailable(nonDuplicatedResults2)
            }
        }

        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
        Toast.makeText(this, "Scanning WiFi ... ", Toast.LENGTH_SHORT).show()

    }

}
