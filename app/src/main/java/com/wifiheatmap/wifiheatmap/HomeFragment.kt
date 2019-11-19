package com.wifiheatmap.wifiheatmap

import android.net.wifi.ScanResult
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.wifiheatmap.wifiheatmap.databinding.FragmentHomeBinding
import com.wifiheatmap.wifiheatmap.room.Network
import timber.log.Timber

/**
 * The home fragment for the Wifi heat mapping application
 * displays information about the current networks that are
 * visible to this device, and what history the device has
 * recorded about those networks.
 */
class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    private lateinit var recyclerAdapter: HomeViewAdapter
    private var networks : List<Network>? = null
    private var scanResults : List<ScanResult>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        // setup the recyclerView
        val recyclerLayout = LinearLayoutManager(this.context)
        recyclerAdapter = HomeViewAdapter()

        binding.networksRecyclerView.apply{
            setHasFixedSize(true)
            layoutManager = recyclerLayout
            adapter = recyclerAdapter
        }

        // link the ViewModel and the HomeViewAdapter
        val viewModel = ViewModelProviders.of(this).get(ViewModel::class.java)

        // listen to the viewModel's data and attach it to the adapter
        viewModel.getNetworks().observeForever{
            networks = it
            if (scanResults != null) {
                // I don't have to worry about networks being null,
                // because I set it above explicitly.
                recyclerAdapter.setNetworks(networks!!, scanResults!!)
            }
        }

        // Make a first call to the updateScanResults so that the view
        // will pull that in right away and not wait for the user to
        // press a button.
        this.updateScanResults()

        setHasOptionsMenu(true)
        return this.binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val menuItem = menu.add(Menu.NONE, R.id.refresh_network_list, Menu.NONE,
            R.string.refresh)
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.refresh_network_list) {
            this.updateScanResults()
            true
        } else {
            Timber.e("Unrecognized options item: %s", item.itemId)
            false
        }
    }

    private fun updateScanResults()
    {
        fun passResults(l : List<ScanResult>)
        {
            Toast.makeText(this.context, "Size of ScanResults: " + l.size, Toast.LENGTH_SHORT).show()
            this.scanResults = l
            if (networks != null) {
                // I don't have to worry about scanResults being null,
                // because I set it above explicitly.
                recyclerAdapter.setNetworks(networks!!, scanResults!!)
            }
        }
        val mainActivity = this.activity as MainActivity
        mainActivity.scanWifi(::passResults)
    }
}
