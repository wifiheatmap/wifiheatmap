package com.wifiheatmap.wifiheatmap

import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.wifiheatmap.wifiheatmap.room.Network
import kotlinx.android.synthetic.main.network_selection_item.view.*
import kotlinx.android.synthetic.main.network_selection_item_drawer.view.*

class MainDrawerAdapter : RecyclerView.Adapter<MainDrawerAdapter.NetworkHolder>() {

    public lateinit var drawerLayout: DrawerLayout

    // this is being set from the MainActivity when this MainDrawerAdapter is created.
    public lateinit var mapsViewModel: MapsViewModel

    class NetworkHolder(val networkView: View) : RecyclerView.ViewHolder(networkView)

    private data class NetworkData(val networkDB: Network? = null, val scanResult: ScanResult?)

    private var networks: MutableList<NetworkData>? = null

    fun setNetworks(scanResults: List<ScanResult>) {
        if (this.networks == null) {
            this.networks = mutableListOf(NetworkData(null, null))
        }
        this.networks!!.clear()
        for (wifi: ScanResult in scanResults) {
            var matchedNetwork: Network? = null
            if (matchedNetwork != null) {
                this.networks!!.add(NetworkData(matchedNetwork, wifi))
            } else {
                this.networks!!.add(NetworkData(null, wifi))
            }
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkHolder {
        val view = (LayoutInflater.from(parent.context).inflate(
            R.layout.network_selection_item_drawer,
            parent,
            false
        ))
        return NetworkHolder(view)
    }

    override fun getItemCount(): Int {
        return networks?.size ?: 0
    }


    override fun onBindViewHolder(holder: NetworkHolder, position: Int) {

        // here is where we would set a function call to this fragment
        // use the holder.itemVIew.ssid_label_drawer.text to get the SSID
        holder.itemView.setOnClickListener {
            // set the mapsViewModel viewNetwork (String) to the SSID of the tapped wifi item.
            mapsViewModel.viewNetwork = holder.itemView.ssid_label_drawer.text.toString()
            this.drawerLayout.closeDrawers()
        }


        if (networks != null && position >= 0 && position < this.itemCount) {
            // Get the network
            val network = networks!![position]
            // Display the name of the network from whichever source is non-null
            val ssidLabel = holder.networkView.findViewById<TextView>(R.id.ssid_label_drawer)

            if (network.scanResult != null) {
                ssidLabel.text = network.scanResult.SSID
            }

            // might want to check the current networks in this recycler view and
            // strikethrough all the ones that are blacklisted within the database.



            // display the strength of the current network as an icon.
            val strength = holder.networkView.findViewById<ImageView>(R.id.wifi_strength_drawer)
            strength.visibility = ImageView.VISIBLE
            strength.setBackgroundResource(getWifiStrengthIcon(network.scanResult?.level))

        }
    }

    private fun getWifiStrengthIcon(strength: Int?) : Int {
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
        return 0;
    }

}