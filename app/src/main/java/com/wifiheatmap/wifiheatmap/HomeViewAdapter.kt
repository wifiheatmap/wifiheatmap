package com.wifiheatmap.wifiheatmap

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class HomeViewAdapter : RecyclerView.Adapter<HomeViewAdapter.NetworkHolder>() {

    class NetworkHolder(val networkView: View) : ViewHolder(networkView)

    private class NetworkData(val networkDB: Any? = null, val scanResult: ScanResult?)

    private var networks: List<NetworkData>? = null

    /**
     * Updates the Objects in our list to display in the RecyclerView
     * @param networksDB a [List] of //Get name from model// from the database
     * @param networksVisible a [List] of //get name from Wifi code// which is all currently visible networks.
     */
    fun setNetworks(networksDB: List<Any>, networksVisible: List<ScanResult>) {
        /*
            iterate through networksVisible
            if an equivalent network exists in the networksDB list,
            merge them together into a NetworkData object.
            Else, create a NetworkData object anyways, except the DB
            object will be null.
            Then, take any remaining networks in the database and append
            them with null Network data.

            No NetworkData object can have both be null!!!
         */
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
     * Attaches a view holder to a layout file to be what the recyclerView dynamically displays.
     * @param parent a [ViewGroup] that will serve as the returned view's parent
     * @param viewType an [Int] that I don't use.
     * @return a [NetworkHolder] which is the type of view the RecyclerView will display
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkHolder {
        val view = (LayoutInflater.from(parent.context)
            .inflate(R.layout.network_selection_item, parent, false))
        return NetworkHolder(view)
    }

    /**
     * puts data in the views displayed by the RecyclerView
     * @param holder the [NetworkHolder] that the RecyclerView is about to display
     * @param position an [Int] for the position of the NetworkHolder in the RecyclerView,
     *          corresponds to a location in the data we will display.
     */
    override fun onBindViewHolder(holder: NetworkHolder, position: Int) {
        if (networks != null) {
            // Get the network
            val network = networks?.get(position) ?: NetworkData(null, null)
            // Display the name of the network from whichever souce is non-null
            val ssidLabel = holder.networkView.findViewById<TextView>(R.id.ssid_label)
            ssidLabel.text = if (network.scanResult != null) {
                network.scanResult.SSID
            }
            else {
                network.networkDB?.toString() // TODO replace with SSID from database
            }
            // TODO strike-through the text if the network has been blacklisted

            // display the strength of the current network as an icon.
            val strength = holder.networkView.findViewById<ImageView>(R.id.wifi_strength)
            strength.visibility = ImageView.VISIBLE
            strength.setBackgroundResource(getWifiStrengthIcon(network.scanResult?.level))

            // display if the network has a record in the database
            if (network.networkDB != null) {
                holder.networkView.findViewById<ImageView>(R.id.network_record_indicator).visibility = ImageView.VISIBLE
            }
            else {
                // making it invisible will still take up space and apply margins so that we don't have issues that way.
                holder.networkView.findViewById<ImageView>(R.id.network_record_indicator).visibility = ImageView.INVISIBLE
            }

            // setup the more button with an onClickListener
            val moreButton = holder.networkView.findViewById<ImageView>(R.id.more_button)
            moreButton.visibility = ImageView.VISIBLE
            moreButton.isClickable = true
            moreButton.setOnClickListener{
                showPopup(moreButton, moreButton.context, network)
            }
        }
        else {
            // Make nothing appear (although it will still take up space on the screen).
            holder.networkView.findViewById<TextView>(R.id.ssid_label).text = ""
            holder.networkView.findViewById<ImageView>(R.id.wifi_strength).visibility = ImageView.INVISIBLE
            holder.networkView.findViewById<ImageView>(R.id.network_record_indicator).visibility = ImageView.INVISIBLE
            val moreButton = holder.networkView.findViewById<ImageView>(R.id.more_button)
            moreButton.visibility = ImageView.INVISIBLE
            moreButton.isClickable = false
        }
    }

    /**
     * Gets the location of the Wifi Icon appropriate for the Wifi strength.
     * @param strength a nullable [Int] indicating the strength of the Wifi network.
     * @return an [Int] which is the reference to the corresponding vector drawable.
     *         If the input is null, the default is an icon for the Wifi is off.
     */
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
    }

    // https://developer.android.com/guide/topics/ui/menus#PopupMenu
    // https://developer.android.com/reference/android/widget/PopupMenu.html
    private fun showPopup(v: View, c: Context, n: NetworkData) {
        val popup = PopupMenu(c, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.network_options, popup.menu)
        // TODO modify the menu to reflect the data.
        // set the text based on if the network is blacklisted.
//        popup.menu.findItem(R.id.blacklistOption)
        // Don't have the menu option to delete the data if the data is not there.
        if (n.networkDB == null)
            popup.menu.removeItem(R.id.delete_option)
        popup.setOnMenuItemClickListener {
            TODO("not implemented")
            // NEED DATABASE FOR ALL LISTENERS TO FUNCTION
        }
        popup.show()
    }
}