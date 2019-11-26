package com.wifiheatmap.wifiheatmap

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.wifiheatmap.wifiheatmap.room.Network

class DatabaseManagementAdapter : RecyclerView.Adapter<DatabaseManagementAdapter.NetworkHolder>() {

    class NetworkHolder(val networkView: View) : ViewHolder(networkView)

    private data class NetworkData(val networkDB: Network? = null, val scanResult: ScanResult?)

    private var networks: MutableList<NetworkData>? = null

    /**
     * Updates the Objects in our list to display in the RecyclerView
     * @param networksDB a [List] of [Network] from the database
     * @param scanResults a [List] of [ScanResult] which is all currently visible networks.
     */
    fun setNetworks(networksDB: List<Network>, scanResults: List<ScanResult>) {
        // clear the networks list and start from scratch
        if (this.networks == null)
        {
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

        for (wifi : ScanResult in scanResults) {
            var matchedNetwork : Network? = null
            for (network : Network in networksDB) {
                if (network.ssid == wifi.SSID) {
                    matchedNetwork = network
                    break
                }
            }
            if (matchedNetwork != null) {
                // we found matching SSIDs, store them together in
                // the networks list.
                this.networks!!.add(NetworkData(matchedNetwork, wifi))
            }
            else {
                this.networks!!.add(NetworkData(null, wifi))
            }
        }

        // Now we have all matched networks and scan results without databases,
        // add all database entries that don't have matching scan results.
        for (db : Network in networksDB) {
            var matchFound = false
            for (scan : ScanResult in scanResults) {
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
        if (networks != null && position >= 0 && position < this.itemCount) {
            // Get the network
            val network = networks!![position]
            // Display the name of the network from whichever source is non-null
            val ssidLabel = holder.networkView.findViewById<TextView>(R.id.ssid_label)
            ssidLabel.text = if (network.scanResult != null) {
                network.scanResult.SSID
            }
            else {
                network.networkDB!!.ssid
            }

            // put a strike through the text if it has been blacklisted
            ssidLabel.paint.isStrikeThruText = network.networkDB != null && network.networkDB.blacklisted

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
    /**
     * Displays a smart popup menu that gives the user choices on
     * how to contextually interact with the database (without
     * explicitly telling them about the database).
     * @param v a [View] object to anchor the popup to.
     * @param c the [Context] of the view we anchor the popup to.
     * @param n the [NetworkData] which determines how we interact
     *          with the database.
     */
    private fun showPopup(v: View, c: Context, n: NetworkData) {
        val popup = PopupMenu(c, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.network_options, popup.menu)
        // MODIFY THE MENU TO REFLECT THE DATA PRESENT
        // set the text based on if the network is blacklisted.
        val blackList : MenuItem = popup.menu.findItem(R.id.blacklistOption)
        if (n.networkDB != null) {
            if (n.networkDB.blacklisted) {
                blackList.title = c.resources.getString(R.string.unblacklist_prompt)
                blackList.setOnMenuItemClickListener {
                    if (c.applicationContext is Application) {
                        // modify the network to update the database
                        // so that the blacklisted condition is removed.
                        val newNetwork = Network(n.networkDB.id, n.networkDB.ssid, false)
                        ViewModel(c.applicationContext as Application).insertNetwork(newNetwork)
                        // refresh the recyclerView to reflect changes.
                        notifyDataSetChanged()
                        return@setOnMenuItemClickListener true
                    }
                    // We didn't have the application context, so we couldn't use the database.
                    false
                }
            }
            else {
                blackList.title = c.resources.getString(R.string.blacklist_prompt)
                blackList.setOnMenuItemClickListener {
                    if (c.applicationContext is Application) {
                        // modify the network to update the database
                        // so that
                        val newNetwork = Network(n.networkDB.id, n.networkDB.ssid, true)
                        ViewModel(c.applicationContext as Application).insertNetwork(newNetwork)
                        // refresh the recyclerView to reflect changes.
                        notifyDataSetChanged()
                        return@setOnMenuItemClickListener true
                    }
                    // We didn't have the application context, so we couldn't use the database.
                    false
                }
            }
        }
        else {
            // let the user blacklist BEFORE ever collecting data
            blackList.title = c.resources.getString(R.string.blacklist_prompt)
            blackList.setOnMenuItemClickListener {
                if (c.applicationContext is Application) {
                    // modify the network to update the database
                    // so that the network is blacklisted.
                    val newNetwork = Network(0, n.scanResult!!.SSID, true)
                    ViewModel(c.applicationContext as Application).insertNetwork(newNetwork)
                    // refresh the recyclerView to reflect changes.
                    notifyDataSetChanged()
                    return@setOnMenuItemClickListener true
                }
                // We didn't have the application context, so we couldn't use the database.
                false
            }
        }
        // setup the delete database option
        if (n.networkDB == null) {
            // Don't have the menu option to delete the data if the data is not there.
            popup.menu.removeItem(R.id.delete_option)
        }
        else {
            val deleteOption : MenuItem = popup.menu.findItem(R.id.delete_option)
            deleteOption.setOnMenuItemClickListener {
                if (c.applicationContext is Application) {
                    // Get a reference to the ViewModel
                    val viewModel = ViewModel(c.applicationContext as Application)
                    // Cascading delete on the old version of the network
                    viewModel.deleteNetwork(n.networkDB)
                    // add a network without any other data with blacklisted set to true.
                    // so that we don't add new data.
                    val newNetwork = Network(0, n.networkDB.ssid, true)
                    viewModel.insertNetwork(newNetwork)
                    notifyDataSetChanged()
                    return@setOnMenuItemClickListener true
                }
                false
            }
        }
        popup.show()
    }
}