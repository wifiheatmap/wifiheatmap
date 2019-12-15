package com.wifiheatmap.wifiheatmap

import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.wifiheatmap.wifiheatmap.room.Network

abstract class NetworkListAdapter : RecyclerView.Adapter<NetworkListAdapter.NetworkHolder>() {

    class NetworkHolder(val networkView: View) : ViewHolder(networkView)

    protected data class NetworkData(val networkDB: Network? = null, val scanResult: ScanResult?)

    protected var networks: MutableList<NetworkData>? = null

    /**
     * Updates the Objects in our list to display in the RecyclerView
     * @param networksDB a [List] of [Network] from the database
     * @param scanResults a [List] of [ScanResult] which is all currently visible networks.
     */
    fun setNetworks(networksDB: List<Network>, scanResults: List<ScanResult>) {
        // clear the networks list and start from scratch
        if (this.networks == null) {
            this.networks = mutableListOf(NetworkData(null, null))
        }
        this.networks!!.clear()
        /*
            iterate through scanResults
            if an equivalent network exists in the networksDB list,
            merge them together into a NetworkData object.
            Else, create a NetworkData object anyways, except the DB
            object will be null.
            Then, take any remaining networks in the database and append
            them with null Network data.
            No NetworkData object can have both be null!!!
         */

        for (wifi: ScanResult in scanResults) {
            var matchedNetwork: Network? = null
            for (network: Network in networksDB) {
                if (network.ssid == wifi.SSID) {
                    matchedNetwork = network
                    break
                }
            }
            if (matchedNetwork != null) {
                // we found matching SSIDs, store them together in
                // the networks list.
                this.networks!!.add(NetworkData(matchedNetwork, wifi))
            } else {
                this.networks!!.add(NetworkData(null, wifi))
            }
        }

        // Now we have all matched networks and scan results without databases,
        // add all database entries that don't have matching scan results.
        for (db: Network in networksDB) {
            var matchFound = false
            for (scan: ScanResult in scanResults) {
                if (scan.SSID == db.ssid) {
                    matchFound = true
                    break
                }
            }
            // only add if there is no match, because
            // matches have already been added.
            if (!matchFound) {
                this.networks!!.add(NetworkData(db, null))
            }
        }

        notifyDataSetChanged()
    }

    /**
     * Returns the number of items that the adapter has up for display
     */
    override fun getItemCount(): Int {
        // using cached data
        // if networks exists, return its size, else return 0
        return networks?.size ?: 0
    }

    /**
     * Must be defined in subclass
     */
    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkHolder

    /**
     * Must be defined in subclass
     */
    abstract override fun onBindViewHolder(holder: NetworkHolder, position: Int)

    /**
     * Gets the location of the Wifi Icon appropriate for the Wifi strength.
     * @param strength a nullable [Int] indicating the strength of the Wifi network.
     * @return an [Int] which is the reference to the corresponding vector drawable.
     *         If the input is null, the default is an icon for the Wifi is off.
     */
    protected fun getWifiStrengthIcon(strength: Int?): Int {
        return if (strength != null) {
            when (WifiManager.calculateSignalLevel(strength, 5)) {
                0 -> R.drawable.ic_signal_wifi_0_bar_black_24dp
                1 -> R.drawable.ic_signal_wifi_1_bar_black_24dp
                2 -> R.drawable.ic_signal_wifi_2_bar_black_24dp
                3 -> R.drawable.ic_signal_wifi_3_bar_black_24dp
                4 -> R.drawable.ic_signal_wifi_4_bar_black_24dp
                else -> R.drawable.ic_signal_wifi_off_black_24dp
            }
        } else {
            R.drawable.ic_signal_wifi_off_black_24dp // default value
        }
    }
}