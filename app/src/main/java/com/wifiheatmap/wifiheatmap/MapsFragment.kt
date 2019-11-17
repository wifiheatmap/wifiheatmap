package com.wifiheatmap.wifiheatmap

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import com.wifiheatmap.wifiheatmap.databinding.MapsFragmentBinding
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList

private const val LOCATION_PERMISSION_REQUEST_CODE = 1

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel: MapsViewModel
    private lateinit var binding: MapsFragmentBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var heatmapTileProvider: HeatmapTileProvider
    private lateinit var tileOverlay: TileOverlay

    private var locationUpdateState: Boolean = false
    private val heatmapData = ArrayList<WeightedLatLng>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.maps_fragment, container, false)

        binding.darkModeSwitch.setOnClickListener {
            if (binding.darkModeSwitch.isChecked) {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.night_map))
            } else {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.default_map))
            }
        }

        binding.fab.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
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
        map.uiSettings.isZoomControlsEnabled = true
        getPermission()
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                lastLocation = it
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 20.0f))
            }
        }
        addHeatMap()
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

            task.addOnSuccessListener {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    private fun addHeatMap() {
        val data: List<WeightedLatLng> = readWeightedLatLngFromJson(R.raw.test_weightedlatlng_data)
        for (item in data) {
            heatmapData.add(item)
        }
        heatmapTileProvider = HeatmapTileProvider.Builder().weightedData(heatmapData).build()
        tileOverlay = map.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))
    }

    private fun updateHeatMap() {
        heatmapTileProvider.setWeightedData(heatmapData)
        tileOverlay.clearTileCache()
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

    private fun readWeightedLatLngFromJson(resource: Int): ArrayList<WeightedLatLng> {
        val list = ArrayList<WeightedLatLng>()
        val iStream = resources.openRawResource(resource)
        val json = Scanner(iStream).useDelimiter("\\A").next()
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val lat = jsonObject.getDouble("lat")
            val lng = jsonObject.getDouble("lng")
            val weight = jsonObject.getDouble("weight")
            val weightedLatLng = WeightedLatLng(LatLng(lat, lng), weight)
            list.add(weightedLatLng)
        }
        return list
    }
}
