package com.wifiheatmap.wifiheatmap

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.wifi.ScanResult
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import com.wifiheatmap.wifiheatmap.databinding.MapsFragmentBinding
import com.wifiheatmap.wifiheatmap.room.Data
import com.wifiheatmap.wifiheatmap.room.Network
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

private const val LOCATION_PERMISSION_REQUEST_CODE = 1

class MapsFragment : Fragment(), OnMapReadyCallback, Observer<List<Data>> {

    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var viewModel: ViewModel
    private lateinit var binding: MapsFragmentBinding
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest? = null

    private val tileHeatMap = TileHeatMap(9999999)
    private var heatMapRefreshNeeded = false

    private var locationUpdateState: Boolean = false

    private var wifiLiveData: LiveData<List<Data>>? = null
    private var currentNetwork: Network? = null

    private var networkList: List<Network>? = null

    private val settingsDialog = SettingsDialog()

    private var previousViewNetwork = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getPermission()

        binding = DataBindingUtil.inflate(inflater, R.layout.maps_fragment, container, false)

        mapsViewModel = ViewModelProviders.of(requireActivity()).get(MapsViewModel::class.java)

        mapsViewModel.isDarkModeEnabled.observe(this, Observer { isEnabled ->
            if (isEnabled) {
                map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.night_map))
            } else {
                map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.default_map))
            }
        })

        viewModel = ViewModelProviders.of(requireActivity()).get(ViewModel::class.java)

        viewModel.getNetworks().observeForever {
            networkList = it
        }

        mapsViewModel.isColorBlindModeEnabled.observe(this, Observer { isEnabled ->
            if (isEnabled) {
                tileHeatMap.setHeatmapColor(Color.rgb(255, 225, 0), Color.rgb(100, 100, 255))
            } else {
                tileHeatMap.setHeatmapColor(Color.rgb(0, 0, 255), Color.rgb(255, 0, 0))
            }
        })

        mapsViewModel.viewNetwork.observe(this, Observer { network ->
            if(network != null && network != "") {
                setNetwork(network)
            }
        })

        binding.settingsFab.setOnClickListener {
            fragmentManager?.let {
                settingsDialog.show(it, null)
            }
            //binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.playPauseFab.setOnClickListener {
            if (locationUpdateState) {
                // on pause
                locationUpdateState = false
                fusedLocationClient.removeLocationUpdates(locationCallback)
                context?.let {
                    binding.playPauseFab.setImageDrawable(
                        ContextCompat.getDrawable(
                            it,
                            R.drawable.ic_play_arrow_white_24dp
                        )
                    )
                }
            } else {
                // on play

                if(mapsViewModel.viewNetwork.value ?: "" == "") {
                    Toast.makeText(this.context, "Please select a network", Toast.LENGTH_SHORT).show()
                    val mainActivity = this.activity as MainActivity
                    mainActivity.openDrawer()
                } else {
                    locationUpdateState = true
                    startLocationUpdates()
                    updateWifi()
                    context?.let {
                        binding.playPauseFab.setImageDrawable(
                            ContextCompat.getDrawable(
                                it,
                                R.drawable.ic_pause_white_24dp
                            )
                        )
                    }
                }

            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                lastLocation = locationResult.lastLocation
            }
        }

        // add the options menu which navigates to the database management fragment
        setHasOptionsMenu(true)

        return binding.root
    }

    /**
     * Initialize the contents of the fragment standard options menu.
     * This options menu will allow navigation to the database management fragment.
     *
     * @param menu The options menu of the fragment.
     * @param inflater The [MenuInflater] with which we will inflate the XML
     *
     * @see .setHasOptionsMenu
     * @see .onPrepareOptionsMenu
     * @see .onOptionsItemSelected
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val menuItem = menu.add(Menu.NONE, R.id.db_manager_nav_button, Menu.NONE, R.string.db_nav_prompt)
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    /**
     * Handles the event where the user presses a button in our standard options menu.
     * @param item The menu item that was selected.
     *
     * @return a [Boolean] indicating if the event was processed successfully.
     *
     * @see .onCreateOptionsMenu
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.db_manager_nav_button) {
            // navigate to the database management fragment
            findNavController().navigate(MapsFragmentDirections.actionMapsFragmentToDatabaseManagementFragment())
            return true
        } else {
            Timber.e("Unrecognized options item: %s", item.itemId)
            return false
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.uiSettings?.isZoomControlsEnabled = true
        getPermission()
        map?.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                lastLocation = it
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 20.0f))
            }
        }
    }

    private fun getPermission() {
        context?.let { context ->
            activity?.let { activity ->
                if (ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                    return
                }
            }
        }
    }

    private fun startLocationUpdates() {
        getPermission()

        if (locationRequest == null) {
            createLocationRequest()
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.let { locationRequest ->
            locationRequest.interval = 10000
            locationRequest.fastestInterval = 5000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            context?.let { context ->
                val client = LocationServices.getSettingsClient(context)
                val task = client.checkLocationSettings(builder.build())

                // need to remove right away because on resume enables it when the app launches
                task.addOnSuccessListener {
                    locationUpdateState = false
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
        }

    }

    private fun getNetworkIfExists(ssid: String): Network? {
        val networks = networkList ?: return null
        for(network in networks) {
            if(network.ssid == ssid) {
                return network
            }
        }
        return null
    }

    /**
     * Sets the network to be displayed as a heatmap on the map
     */
    private fun setNetwork(ssid: String) {
        if(previousViewNetwork == ssid) return
        previousViewNetwork = ssid
        val network = getNetworkIfExists(ssid) ?: return
        currentNetwork = network
        if(wifiLiveData != null) {
            wifiLiveData!!.removeObserver(this)
        }
        wifiLiveData = viewModel.getData(network.ssid)
        wifiLiveData!!.observeForever(this)

        heatMapRefreshNeeded = true
    }

    private fun updateWifi()
    {
        class ScanListener : MainActivity.ScanResultListener {
            override fun onScanResultsAvailable(results: List<ScanResult>) {
                for(result in results) {
                    val network = getNetworkIfExists(result.SSID)
                    // network will be null if the network list isn't ready yet or if the current network doesn't exist in the database
                    if(network == null) {
                        // if the networkList is not null we know that the reason network was null was because this was a network not in the database
                        if(networkList != null) {
                            val newNetwork = Network(result.SSID, false)
                            viewModel.insertNetwork(newNetwork)
                        }
                        continue
                    }
                    // Don't collect data for a network that is blacklisted
                    if(network.blacklisted) {
                        continue
                    }
                    val data = Data(0, network.ssid, lastLocation.latitude, lastLocation.longitude, result.level, Date())
                    if(network.ssid == currentNetwork?.ssid && map != null) {
                        tileHeatMap.addDataPoint(map!!, data)
                    }
                    viewModel.insertData(data)
                }
                if(locationUpdateState) {
                    updateWifi()
                }
            }
        }
        val mainActivity = this.activity as MainActivity
        val scanListener = ScanListener()
        mainActivity.scanWifi(scanListener)
    }

    override fun onChanged(t: List<Data>?) {
        if(heatMapRefreshNeeded && t != null && map != null) {
            tileHeatMap.createHeatmap(map!!, t)
            heatMapRefreshNeeded = false
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }
}
