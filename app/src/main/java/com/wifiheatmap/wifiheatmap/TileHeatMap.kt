package com.wifiheatmap.wifiheatmap

import android.graphics.Color
import android.net.wifi.WifiManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.heatmaps.Gradient
import com.wifiheatmap.wifiheatmap.room.Data
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Represents a tile based heatmap where the color of each tile represents the average signal strength within its bounds
 */
class TileHeatMap(latitudeDivisions: Int) {
    // Number of times the globe should be divided up into from the north to the south pole
    var latitudeDivisions = latitudeDivisions

    // Colors to be used as the strongest and weakest signals
    private var weakSignalColor = Color.rgb(102, 225, 0)
    private var strongSignalColor = Color.rgb(255, 0, 0)

    // Hash map of all tiles on the Google map. Tiles must be accessible by their ID
    private var tiles = HashMap<ULong, Tile>()

    // Represents a single tile on the map.
    private class Tile(polygon: Polygon, initialPoint: Data) {
        // The sum of all cropped wifi signal readings
        var signalStrengthSum: Int
        // The total number of points being represented by this tile
        var totalNumberOfPoints: Int
        // Reference to the polygon on the Google map
        var polygon: Polygon

        init {
            signalStrengthSum = getCroppedWifiStrength(initialPoint.intensity)
            totalNumberOfPoints = 1
            this.polygon = polygon
        }

        /**
         * Interpolates between two floats
         */
        private fun interpolate(a: Float, b: Float, proportion: Float): Float {
            return a + (b - a) * proportion
        }

        /**
         * Interpolates between two integer based colors
         */
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

        /**
         * Refreshes the color of a polygon on the Google map
         */
        fun refreshPolygonColor(weakSignalColor: Int, strongSignalColor: Int) {
            val normalizedCellStrength = (signalStrengthSum.toDouble()/totalNumberOfPoints.toDouble()) / 10.0
            polygon.fillColor = interpolateColor(weakSignalColor, strongSignalColor, normalizedCellStrength.toFloat())
        }

        /**
         * Converts a raw WiFi strength reading from dBm to 0-10
         */
        private fun getCroppedWifiStrength(level: Int): Int {
            return WifiManager.calculateSignalLevel(level, 10)
        }

        /**
         * Add a data point to this existing tile
         */
        fun addPoint(data: Data) {
            signalStrengthSum += getCroppedWifiStrength(data.intensity)
            totalNumberOfPoints++
        }
    }

    /**
     * Given a latitude and longitude, get the ID of the tile that contains the coordinate
     */
    private fun getTileId(latitude: Double, longitude: Double): ULong {
        return (2*latitudeDivisions).toULong()*Math.floor((latitude+90.0)/(180.0/latitudeDivisions)).toUInt() + Math.floor((longitude+180.0)/(360.0/(2*latitudeDivisions))).toUInt()
    }

    /**
     * Creates a polygon outlining the tile containing the given coordinate
     */
    private fun createPolygon(map: GoogleMap, latitude: Double, longitude: Double): Polygon {
        val cellDiameter: Double = 180.0/latitudeDivisions.toDouble()
        val lat = latitude + 90.0
        val lon = longitude + 180.0
        val minLatitude = (lat - lat.rem(cellDiameter).absoluteValue) - 90.0
        val minLongitude = (lon - lon.rem(cellDiameter).absoluteValue) - 180.0
        val maxLatitude = minLatitude + cellDiameter
        val maxLongitude = minLongitude + cellDiameter
        return map.addPolygon(PolygonOptions()
            .add(
                LatLng(minLatitude, minLongitude),
                LatLng(maxLatitude, minLongitude),
                LatLng(maxLatitude, maxLongitude),
                LatLng(minLatitude, maxLongitude)
            )
            .strokeWidth(0f)
        )
    }

    /**
     * Changes the colors used to represent tile strength. Refreshes all existing tiles.
     */
    fun setHeatmapColor(weakSignalColor: Int, strongSignalColor: Int) {
        this.weakSignalColor = weakSignalColor
        this.strongSignalColor = strongSignalColor

        for(tile in tiles) {
            tile.value.refreshPolygonColor(weakSignalColor, strongSignalColor)
        }
    }

    /**
     * Removes all existing tiles and redraws heatmap for given list of data points
     */
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

    /**
     * Adds a data point to an existing heatmap. Only redraws tiles that have changed.
     */
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