package com.wifiheatmap.wifiheatmap

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wifiheatmap.wifiheatmap.databinding.ActivityMainBinding
import com.wifiheatmap.wifiheatmap.room.Network
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    /**
     * Interface allows us to make a simple callback function
     * when scanning for Wi-Fi Networks.
     */
    interface ScanResultListener {
        /**
         * Notifies the listener when scan results are available.
         * @param results a [List] of [ScanResult] objects.
         */
        fun onScanResultsAvailable(results : List<ScanResult>)
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerAdapter: MainDrawerAdapter
    private lateinit var recyclerDrawerView: RecyclerView
    private lateinit var mapsViewModel: MapsViewModel

    lateinit var wifiManager: WifiManager
    lateinit var results: List<ScanResult>
    var scanResultManager: ScanResultManager = ScanResultManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawerLayout)
        recyclerDrawerView = findViewById(R.id.drawerRecyclerView)


        var layoutManager1 = LinearLayoutManager(this)
        recyclerDrawerView.layoutManager = layoutManager1
        recyclerAdapter = MainDrawerAdapter()
        // create the Singleton MapsViewModel
        mapsViewModel = MapsViewModel(this.application)
        // set the mapsViewModel to the Singleton MapsViewModel
        recyclerAdapter.mapsViewModel = mapsViewModel

        recyclerDrawerView.adapter = recyclerAdapter

        var drawerRefreshButton = findViewById<Button>(R.id.refresh_drawer_network_list)

        // when the REFRESH Button is tapped
        // scan the wifi and then update the RecyclerAdapter.
        drawerRefreshButton.setOnClickListener {
            class ScanListener : MainActivity.ScanResultListener {
                override fun onScanResultsAvailable(results: List<ScanResult>) {
                    recyclerAdapter.setNetworks(results)
                }
            }

            val scanListener = ScanListener()
            scanWifi(scanListener)
        }

        navController = this.findNavController(R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)

        // Lock drawer to only welcome screen
        navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, _ ->
            if (nd.id == nc.graph.startDestination) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

        // request permissions for the wifi scanning
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)
        }

        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }



    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    /**
     * Creates an asynchronous call to scan for nearby Wifi networks
     * which is passed through the callback function in the
     * ScanResultListener passed in as a parameter.
     */
    fun scanWifi(scl: ScanResultListener) {

        val wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                results = wifiManager.scanResults
                unregisterReceiver(this)


                val nonDuplicatedResults2 : List<ScanResult> = scanResultManager
                    .removeDuplicatesFromScanResults(results)
                // var networks : List<Network>? = null
                // recyclerAdapter.setNetworks(networks, nonDuplicatedResults2)
                Toast.makeText(context, "in WifiReceiver!", Toast.LENGTH_SHORT).show()
                // SET THE DRAWER LIST HERE
                recyclerAdapter.setNetworks(nonDuplicatedResults2)
                // call that callback function passing the list of scan results
                scl.onScanResultsAvailable(nonDuplicatedResults2)
            }
        }

        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
        Toast.makeText(this, "Scanning WiFi ... ", Toast.LENGTH_SHORT).show()

    }
}

