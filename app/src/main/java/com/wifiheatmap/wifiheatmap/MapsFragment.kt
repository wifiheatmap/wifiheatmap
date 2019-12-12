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

    private var viewNetwork: String = ""

    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var viewModel: ViewModel
    private lateinit var binding: MapsFragmentBinding
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest? = null
    private var heatmapTileProvider: HeatmapTileProvider? = null
    private var tileOverlay: TileOverlay? = null

    private var locationUpdateState: Boolean = false
    private val heatmapData = ArrayList<WeightedLatLng>()

    private var wifiLiveData: LiveData<List<Data>>? = null
    private var currentNetwork: Network? = null

    private var networkList: List<Network>? = null

    private val settingsDialog = SettingsDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                heatmapTileProvider?.setGradient(
                    Gradient(
                        intArrayOf(
                            Color.rgb(0, 0, 255),
                            Color.rgb(255, 0, 0)
                        ), floatArrayOf(
                            0.2f, 1f
                        )
                    )
                )
            } else {
                heatmapTileProvider?.setGradient(
                    Gradient(
                        intArrayOf(
                            Color.rgb(102, 225, 0),
                            Color.rgb(255, 0, 0)
                        ), floatArrayOf(
                            0.2f, 1f
                        )
                    )
                )
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

                // For testing, will need changed for production
                if(networkList?.size ?: 0 > 0) {
                    setNetwork(networkList!![0].ssid)
                }

                locationUpdateState = true
                startLocationUpdates()
                updateWifi()
                scheduleHeatMapRefresh()
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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                lastLocation = locationResult.lastLocation
                Toast.makeText(activity, "${lastLocation.latitude}, ${lastLocation.longitude}", Toast.LENGTH_LONG).show()
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
                updateHeatMap()
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

    private fun setNetwork(ssid: String) {
        val network = getNetworkIfExists(ssid) ?: return
        currentNetwork = network
        if(wifiLiveData != null) {
            wifiLiveData!!.removeObserver(this)
        }
        wifiLiveData = viewModel.getData(network.ssid)
        wifiLiveData!!.observeForever(this)

        updateHeatMap()
    }

    private fun scheduleHeatMapRefresh() {
        val delay: Double = (mapsViewModel.refreshRate.value ?: 10.0) * 1000.0
        android.os.Handler().postDelayed(
            {
                updateHeatMap()
                if(locationUpdateState) {
                    scheduleHeatMapRefresh()
                }
            },
            delay.toLong()
        )
    }

    private fun updateWifi()
    {
        class ScanListener : MainActivity.ScanResultListener {
            override fun onScanResultsAvailable(results: List<ScanResult>) {
                for(result in results) {
                    val network = getNetworkIfExists(result.SSID)
                    //Problem: what if the network doesn't exist yet? Temp solution: insert network and disregard data until it exists
                    if(network == null) {
                        if(networkList != null) {
                            val newNetwork = Network(result.SSID, false)
                            viewModel.insertNetwork(newNetwork)
                        }
                        continue
                    }
                    val data = Data(0, network.ssid, lastLocation.latitude, lastLocation.longitude, result.level, Date())
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

    }

    private fun updateHeatMap() {

        val data = wifiLiveData?.value
        if(data != null) {
            heatmapData.clear()
            for(datum in data) {
                val point = WeightedLatLng(LatLng(datum.latitude, datum.longitude), datum.intensity.toDouble())
                heatmapData.add(point)
            }
        }

        if (heatmapTileProvider == null && heatmapData.isNotEmpty()) {
            heatmapTileProvider =
                HeatmapTileProvider.Builder().radius(10).weightedData(heatmapData).build()
            tileOverlay =
                map?.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))
        }
        // calculate a radius based on the zoom level
        var zoomLevel : Int? = null
        if (map != null) {
            val zoomPercentage = map!!.cameraPosition.zoom / map!!.maxZoomLevel
            // the magic number is the radius of each point when we are zoomed in as much as possible.
            zoomLevel = (40.0 * zoomPercentage.pow(map!!.maxZoomLevel - map!!.cameraPosition.zoom)).toInt()
            // enforce a minimum to prevent divide by zero errors
            if (zoomLevel < 1) {
                zoomLevel = 1
            }
            // If needed, show the radius of our points as a toast for debugging.
            // Toast.makeText(this.context, zoomLevel.toString(), Toast.LENGTH_SHORT).show()
            Timber.d("Radius of each point: %s", zoomLevel)
        }
        heatmapTileProvider?.setRadius(zoomLevel ?: 10)
        heatmapTileProvider?.setWeightedData(heatmapData)
        tileOverlay?.clearTileCache()
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

    fun updateViewNetwork(networkSSID: String) {
        viewNetwork = networkSSID
    }
}
