package com.wifiheatmap.wifiheatmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class HomeViewAdapter : RecyclerView.Adapter<HomeViewAdapter.NetworkHolder>() {
    private var networks: List<Objects>>? = null

    /**
     * Updates the Objects in our list to display in the RecyclerView
     * @param networksDB a [List] of //Get name from model// from the database
     * @param networksVisible a [List] of //get name from Wifi code// which is all currently visible networks.
     */
    fun setNetworks(networksDB: List<Objects>, networksVisible: List<Objects>) {
        this.networks = networksDB
        notifyDataSetChanged()
    }

    /**
     * Returns the number of items that the adapter has up for display
     */
    override fun getItemCount(): Int {
        // using cached data
        // if sandwiches exists, return its size, else return 0
        return networks?.size ?: 0
    }

    class NetworkHolder(val networkView: View) : RecyclerView.Recycler.ViewHolder(networkView)

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
     * @param holder the [SandwichHolder] that the RecyclerView is about to display
     * @param position an [Int] for the position of the SandwichHolder in the RecyclerView,
     *          corresponds to a location in the data we will display.
     */
    override fun onBindViewHolder(holder: NetworkHolder, position: Int) {
        if (networks != null) {
//            val sw = sandwiches?.get(position) ?: SandwichEntity(0, "", "", "", 0.0, Date())
            return // placeholder code
        }
        else {
            // get all of the textViews and set their text attribute to ""
            return // placeholder code
        }
    }
}