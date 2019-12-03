package com.wifiheatmap.wifiheatmap

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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

private const val LOCATION_PERMISSION_REQUEST_CODE = 1

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var roomViewModel: ViewModel
    private lateinit var binding: MapsFragmentBinding
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var heatmapTileProvider: HeatmapTileProvider? = null
    private var tileOverlay: TileOverlay? = null

    private var locationUpdateState: Boolean = false
    private val heatmapData = ArrayList<WeightedLatLng>()

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
                locationUpdateState = true
                startLocationUpdates()
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

        context?.let {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(it)
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                lastLocation = locationResult.lastLocation
                heatmapData.add(
                    WeightedLatLng(
                        LatLng(
                            lastLocation.latitude,
                            lastLocation.longitude
                        )
                    )
                )
                updateHeatMap()
            }
        }

        createLocationRequest()

        return binding.root
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

        updateHeatMap()
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
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
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

    private fun updateHeatMap() {
        if (heatmapTileProvider == null && heatmapData.isNotEmpty()) {
            heatmapTileProvider =
                HeatmapTileProvider.Builder().radius(10).weightedData(heatmapData).build()
            tileOverlay =
                map?.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))
        }
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
}