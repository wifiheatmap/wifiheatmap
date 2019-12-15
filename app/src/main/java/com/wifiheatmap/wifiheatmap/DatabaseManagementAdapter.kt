package com.wifiheatmap.wifiheatmap

import android.app.Application
import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import com.wifiheatmap.wifiheatmap.room.Network

class DatabaseManagementAdapter : NetworkListAdapter() {

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
            ssidLabel.text = network.scanResult?.SSID ?: network.networkDB!!.ssid

            // Indicate to the user if the network has been blacklisted.
            if (network.networkDB != null && network.networkDB.blacklisted) {
                // strike through the the text
                ssidLabel.paint.isStrikeThruText = true
                // give it a grey background
                holder.networkView.setBackgroundColor(holder.networkView.context.getColor(R.color.blacklistGrey))
            } else {
                // explicitly don't strike-through
                ssidLabel.paint.isStrikeThruText = false
                // give it a transparent background
                holder.networkView.setBackgroundColor(0x00000000)
            }

            // display the strength of the current network as an icon.
            val strength = holder.networkView.findViewById<ImageView>(R.id.wifi_strength)
            strength.visibility = ImageView.VISIBLE
            strength.setBackgroundResource(getWifiStrengthIcon(network.scanResult?.level))

            // display if the network has a record in the database with data
            val checkMark =
                holder.networkView.findViewById<ImageView>(R.id.network_record_indicator)
            val viewModel = ViewModel(checkMark.context.applicationContext as Application)
            val liveData =
                viewModel.getRecordExists(network.scanResult?.SSID ?: network.networkDB!!.ssid)
            liveData.observeForever {
                if (it) {
                    checkMark.visibility = ImageView.VISIBLE
                } else {
                    // making it invisible will still take up space and apply margins so that we don't have issues that way.
                    checkMark.visibility = ImageView.INVISIBLE
                }
            }

            // setup the more button with an onClickListener
            val moreButton = holder.networkView.findViewById<ImageView>(R.id.more_button)
            moreButton.visibility = ImageView.VISIBLE
            moreButton.isClickable = true
            moreButton.setOnClickListener {
                showPopup(moreButton, moreButton.context, network)
            }
        } else {
            // Make nothing appear (although it will still take up space on the screen).
            holder.networkView.findViewById<TextView>(R.id.ssid_label).text = ""
            holder.networkView.findViewById<ImageView>(R.id.wifi_strength).visibility =
                ImageView.INVISIBLE
            holder.networkView.findViewById<ImageView>(R.id.network_record_indicator).visibility =
                ImageView.INVISIBLE
            val moreButton = holder.networkView.findViewById<ImageView>(R.id.more_button)
            moreButton.visibility = ImageView.INVISIBLE
            moreButton.isClickable = false
        }
    }

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
        val blackList: MenuItem = popup.menu.findItem(R.id.blacklistOption)
        if (n.networkDB != null) {
            if (n.networkDB.blacklisted) {
                blackList.title = c.getString(R.string.unblacklist_prompt)
                blackList.setOnMenuItemClickListener {
                    if (c.applicationContext is Application) {
                        // modify the network to update the database
                        // so that the blacklisted condition is removed.
                        val newNetwork = Network(n.networkDB.ssid, false)
                        ViewModel(c.applicationContext as Application).insertNetwork(newNetwork)
                        // refresh the recyclerView to reflect changes.
                        notifyDataSetChanged()
                        return@setOnMenuItemClickListener true
                    }
                    // We didn't have the application context, so we couldn't use the database.
                    false
                }
            } else {
                blackList.title = c.getString(R.string.blacklist_prompt)
                blackList.setOnMenuItemClickListener {
                    if (c.applicationContext is Application) {
                        // modify the network to update the database
                        // so that
                        val newNetwork = Network(n.networkDB.ssid, true)
                        ViewModel(c.applicationContext as Application).insertNetwork(newNetwork)
                        // refresh the recyclerView to reflect changes.
                        notifyDataSetChanged()
                        return@setOnMenuItemClickListener true
                    }
                    // We didn't have the application context, so we couldn't use the database.
                    false
                }
            }
        } else {
            // let the user blacklist BEFORE ever collecting data
            blackList.title = c.getString(R.string.blacklist_prompt)
            blackList.setOnMenuItemClickListener {
                if (c.applicationContext is Application) {
                    // modify the network to update the database
                    // so that the network is blacklisted.
                    val newNetwork = Network(n.scanResult!!.SSID, true)
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
        if (c.applicationContext is Application && n.networkDB != null) {
            val viewModel = ViewModel(c.applicationContext as Application)
            val liveData = viewModel.getRecordExists(n.scanResult?.SSID ?: n.networkDB!!.ssid)
            liveData.observeForever {
                if (it) {
                    val deleteOption: MenuItem = popup.menu.findItem(R.id.delete_option)
                    deleteOption.setOnMenuItemClickListener {
                        // add a network without any other data with blacklisted set to true.
                        val newNetwork = Network(n.networkDB.ssid, true)
                        viewModel.deleteNetwork(n.networkDB)
                        viewModel.insertNetwork(newNetwork)
                        notifyDataSetChanged()
                        return@setOnMenuItemClickListener true
                    }
                } else {
                    popup.menu.removeItem(R.id.delete_option)
                }
            }
        } else {
            // we can't delete stuff, so don't give them the option to delete when we can't.
            popup.menu.removeItem(R.id.delete_option)
        }

        popup.show()
    }
}