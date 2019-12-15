package com.wifiheatmap.wifiheatmap

import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.android.synthetic.main.network_selection_item_drawer.view.*

class MainDrawerAdapter : NetworkListAdapter() {

    lateinit var drawerLayout: DrawerLayout

    // this is being set from the MainActivity when this MainDrawerAdapter is created.
    lateinit var mapsViewModel: MapsViewModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkHolder {
        val view = (LayoutInflater.from(parent.context).inflate(
            R.layout.network_selection_item_drawer,
            parent,
            false
        ))
        return NetworkHolder(view)
    }

    override fun onBindViewHolder(holder: NetworkHolder, position: Int) {

        // here is where we would set a function call to this fragment
        // use the holder.itemVIew.ssid_label_drawer.text to get the SSID
        holder.itemView.setOnClickListener {
            // set the mapsViewModel viewNetwork (String) to the SSID of the tapped wifi item.
            mapsViewModel.viewNetwork.value = holder.itemView.ssid_label_drawer.text.toString()
            this.drawerLayout.closeDrawers()
        }


        if (networks != null && position >= 0 && position < this.itemCount) {
            // Get the network
            val network = networks!![position]
            // Display the name of the network from whichever source is non-null
            val ssidLabel = holder.networkView.findViewById<TextView>(R.id.ssid_label_drawer)

            ssidLabel.text = network.scanResult?.SSID ?: network.networkDB!!.ssid

            // display the strength of the current network as an icon.
            val strength = holder.networkView.findViewById<ImageView>(R.id.wifi_strength_drawer)
            strength.visibility = ImageView.VISIBLE
            strength.setBackgroundResource(getWifiStrengthIcon(network.scanResult?.level))

        }
    }

}