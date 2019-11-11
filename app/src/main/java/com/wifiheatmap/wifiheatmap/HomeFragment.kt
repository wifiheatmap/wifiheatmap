package com.wifiheatmap.wifiheatmap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.wifiheatmap.wifiheatmap.databinding.FragmentHomeBinding

/**
 * The home fragment for the Wifi heat mapping application
 * displays information about the current networks that are
 * visible to this device, and what history the device has
 * recorded about those networks.
 */
class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        // setup the recyclerView
        val recyclerLayout = LinearLayoutManager(this.context)
        val recyclerAdapter = HomeViewAdapter()

        binding.networksRecyclerView.apply{
            setHasFixedSize(true)
            layoutManager = recyclerLayout
            adapter = recyclerAdapter
        }

        // link the ViewModel and the SandwichAdapter
//        val viewModel = ViewModelProviders.of(this)
//            .get(outViewModelClass::class.java)

//        // listen to the viewModel's data and attach it to the adapter
//        viewModel.getDataMethod().observe(this, object: Observer<List<networkObject>> {
//            // store a reference to the adapter in this anonymous class
//            private val adapter = recyclerAdapter
//
//            override fun onChanged(t: List<networkObject>?) {
//                if (t != null) {
//                    adapter.setNetworks(t, List of visible networks)
//                }
//            }
//        }


        return this.binding.root
    }


}
