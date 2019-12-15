package com.wifiheatmap.wifiheatmap

import android.graphics.Color
import android.net.wifi.WifiManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.heatmaps.Gradient
import com.wifiheatmap.wifiheatmap.room.Data
import kotlin.math.sign

class TileHeatMap(latitudeDivisions: Int) {
    // Number of times the globe should be divided up into from the north to the south pole
    private val latitudeDivisions = latitudeDivisions

    private var weakSignalColor = Color.rgb(102, 225, 0)
    private var strongSignalColor = Color.rgb(255, 0, 0)

    private var tiles = HashMap<ULong, Tile>()

    private class Tile(polygon: Polygon, initialPoint: Data) {
        var signalStrengthSum: Int
        var totalNumberOfPoints: Int
        var polygon: Polygon

        init {
            signalStrengthSum = getCroppedWifiStrength(initialPoint.intensity)
            totalNumberOfPoints = 1
            this.polygon = polygon
        }

        private fun interpolate(a: Float, b: Float, proportion: Float): Float {
            return a + (b - a) * proportion
        }

        private fun interpolateColor(a: Int, b: Int, proportion: Float): Int {
            val hsva = FloatArray(3)
            val hsvb = FloatArray(3)
            Color.colorToHSV(a, hsva)
            Color.colorToHSV(b, hsvb)
            for (i in 0..2) {
                hsvb[i] = interpolate(hsva[i], hsvb[i], proportion)
            }
            return Color.HSVToColor(hsvb)
        }

        fun refreshPolygonColor(weakSignalColor: Int, strongSignalColor: Int) {
            val normalizedCellStrength = (signalStrengthSum.toDouble()/totalNumberOfPoints.toDouble()) / 10.0
            polygon.fillColor = interpolateColor(weakSignalColor, strongSignalColor, normalizedCellStrength.toFloat())
        }

        private fun getCroppedWifiStrength(level: Int): Int {
            return WifiManager.calculateSignalLevel(level, 10)
        }

        fun addPoint(data: Data) {
            signalStrengthSum += getCroppedWifiStrength(data.intensity)
            totalNumberOfPoints++
        }
    }

    private fun getTileId(latitude: Double, longitude: Double): ULong {
        return (2*latitudeDivisions).toULong()*Math.floor(latitude/(180.0/latitudeDivisions)).toUInt() + Math.floor(longitude/(360.0/(2*latitudeDivisions))).toUInt()
    }

    private fun createPolygon(map: GoogleMap, latitude: Double, longitude: Double): Polygon {
        val cellDiameter: Double = 180.0/latitudeDivisions.toDouble()
        val minLatitude = latitude - latitude.rem(cellDiameter)
        val minLongitude = longitude - longitude.rem(cellDiameter)
        val maxLatitude = minLatitude + cellDiameter
        val maxLongitude = minLongitude + cellDiameter
        return map.addPolygon(PolygonOptions()
            .add(
                LatLng(minLatitude, minLongitude),
                LatLng(maxLatitude, minLongitude),
                LatLng(maxLatitude, maxLongitude),
                LatLng(minLatitude, maxLongitude)
            )
        )
    }

    fun setHeatmapColor(weakSignalColor: Int, strongSignalColor: Int) {
        this.weakSignalColor = weakSignalColor
        this.strongSignalColor = strongSignalColor

        for(tile in tiles) {
            tile.value.refreshPolygonColor(weakSignalColor, strongSignalColor)
        }
    }

    fun createHeatmap(map: GoogleMap, data: List<Data>) {
        tiles.clear()
        map.clear()
        for(datum in data) {
            val id = getTileId(datum.latitude, datum.longitude)
            val tile = tiles[id]
            if(tile == null) {
                tiles[id] = Tile(createPolygon(map, datum.latitude, datum.longitude), datum)
                tiles[id]!!.refreshPolygonColor(weakSignalColor, strongSignalColor)
            } else {
                tile.addPoint(datum)
                tile.refreshPolygonColor(weakSignalColor, strongSignalColor)
            }
        }
    }

    fun addDataPoint(map: GoogleMap, data: Data) {
        val id = getTileId(data.latitude, data.longitude)
        val tile = tiles[id]
        if(tile == null) {
            tiles[id] = Tile(createPolygon(map, data.latitude, data.longitude), data)
            tiles[id]!!.refreshPolygonColor(weakSignalColor, strongSignalColor)
        } else {
            tile.addPoint(data)
            tile.refreshPolygonColor(weakSignalColor, strongSignalColor)
        }
    }
}