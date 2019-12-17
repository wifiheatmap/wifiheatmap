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
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
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
    private lateinit var viewModel: ViewModel

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
        mapsViewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)
        viewModel = ViewModelProviders.of(this).get(ViewModel::class.java)
        // set the mapsViewModel to the Singleton MapsViewModel
        recyclerAdapter.mapsViewModel = mapsViewModel
        // pass a reference to the drawerLayout so when
        // an item is selected within it, it closes the
        // drawer layout.
        recyclerAdapter.drawerLayout = drawerLayout

        recyclerDrawerView.adapter = recyclerAdapter

        val drawerRefreshButton = findViewById<Button>(R.id.refresh_drawer_network_list)

        val drawerSearchEditText = findViewById<EditText>(R.id.search_edit_text)

        drawerSearchEditText.doOnTextChanged { text, start, count, after ->
            recyclerAdapter.filter.filter(text)
        }

        // Refreshes list of networks in the drawer view
        val refreshNetworkList: (View) -> Unit = {

            var scanResults: List<ScanResult>? = null
            var databaseNetworks: List<Network>? = null

            val setNetworkList: () -> Unit = setNetworkList@ {
                val scanResults = scanResults ?: return@setNetworkList
                val databaseNetworks = databaseNetworks ?: return@setNetworkList

                recyclerAdapter.setNetworks(databaseNetworks, scanResults)
            }

            class ScanListener : MainActivity.ScanResultListener {
                override fun onScanResultsAvailable(results: List<ScanResult>) {
                    scanResults = results
                    setNetworkList()
                }
            }

            viewModel.getNetworks().observeForever {
                var nonBlacklistedNetworks = ArrayList<Network>()
                for(network in it) {
                    if(!network.blacklisted) {
                        nonBlacklistedNetworks.add(network)
                    }
                }
                databaseNetworks = nonBlacklistedNetworks
                setNetworkList()
            }

            val scanListener = ScanListener()
            scanWifi(scanListener)
        }

        // when the REFRESH Button is tapped
        drawerRefreshButton.setOnClickListener(refreshNetworkList)

        // Refresh network list when drawer is first opened without need of pressing refresh button
        drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerClosed(drawerView: View) {
                drawerSearchEditText.text = Editable.Factory.getInstance().newEditable("")
            }

            override fun onDrawerOpened(drawerView: View) {
                Toast.makeText(drawerView.context, "List is refreshing . . .", Toast.LENGTH_SHORT).show()
                refreshNetworkList(drawerLayout)
            }

        })

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

                // call that callback function passing the list of scan results
                scl.onScanResultsAvailable(nonDuplicatedResults2)
            }
        }

        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()

    }

    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }
}

