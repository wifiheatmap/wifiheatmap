package com.wifiheatmap.wifiheatmap

import android.app.SearchManager
import android.content.Context
import android.net.wifi.ScanResult
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.wifiheatmap.wifiheatmap.databinding.FragmentDbManagementBinding
import com.wifiheatmap.wifiheatmap.room.Network
import timber.log.Timber
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.getSystemService





/**
 * The home fragment for the Wifi heat mapping application
 * displays information about the current networks that are
 * visible to this device, and what history the device has
 * recorded about those networks.
 */
class DatabaseManagementFragment : Fragment() {

    private lateinit var binding : FragmentDbManagementBinding
    private lateinit var recyclerAdapter: DatabaseManagementAdapter
    private var networks : List<Network>? = null
    private var scanResults : List<ScanResult>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_db_management, container, false)

        // setup the recyclerView
        val recyclerLayout = LinearLayoutManager(this.context)
        recyclerAdapter = DatabaseManagementAdapter()

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

    private fun showInputMethod(view: View) {
        val imm = getSystemService(context!!, InputMethodManager::class.java)
        if (imm != null) {
//            imm!!.showSoftInput(view, InputMethodManager.SHOW_FORCED)
            imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.db_management_menu, menu)
        val searchManager = getSystemService(context!!, SearchManager::class.java)
        val searchView = menu.findItem(R.id.search_network_list).actionView as SearchView
        searchView.setSearchableInfo(searchManager!!.getSearchableInfo(activity!!.componentName))
        searchView.isIconifiedByDefault = false
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                recyclerAdapter.filter.filter(newText)
                return true
            }

        })
        searchView.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                showInputMethod(v.findFocus())
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.refresh_network_list) {
            this.updateScanResults()
            true
        } else if (item.itemId == R.id.search_network_list) {
            val searchView = item.actionView as SearchView
            searchView.requestFocus()
            true
        } else {
            Timber.e("Unrecognized options item: %s", item.itemId)
            false
        }
    }

    private fun updateScanResults()
    {
        class ScanListener : MainActivity.ScanResultListener {
            override fun onScanResultsAvailable(results: List<ScanResult>) {
                // As an anonymous class I have access to DatabaseManagementFragment variables.
                scanResults = results
                if (networks != null) {
                    // I don't have to worry about scanResults being null,
                    // because I set it above explicitly.
                    recyclerAdapter.setNetworks(networks!!, scanResults!!)
                }
            }
        }
        val mainActivity = this.activity as MainActivity
        val scanListener = ScanListener()
        mainActivity.scanWifi(scanListener)
    }
}