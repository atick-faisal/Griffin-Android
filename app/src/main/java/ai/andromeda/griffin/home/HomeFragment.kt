package ai.andromeda.griffin.home

import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    private lateinit var deviceDatabase: DeviceDatabase
    private lateinit var deviceListAdapter: DeviceListAdapter
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        val application = requireNotNull(this.activity).application
        deviceDatabase = DeviceDatabase.getInstance(application)

        deviceListAdapter = DeviceListAdapter(application, DeviceClickListener {
            Log.i(LOG_TAG, "ITEM CLICKED. ID : $it")
            navigateToDetails(it)
        })

        val viewModelFactory = HomeViewModelFactory(
            application = application,
            database = deviceDatabase
        )

        homeViewModel = ViewModelProvider(this, viewModelFactory)
            .get(HomeViewModel::class.java)

        rootView.deviceList.adapter = deviceListAdapter

        homeViewModel.deviceList.observe(viewLifecycleOwner, Observer {
            it?.let {
                deviceListAdapter.submitList(it)
            }
        })

        rootView.fab.setOnClickListener { navigateToRegister() }
        rootView.deviceList.layoutManager = GridLayoutManager(activity, 2)

        return rootView
    }

    private fun navigateToRegister() {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToRegisterFragment()
        )
    }

    private fun navigateToDetails(deviceId: String) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToDeviceDetailsFragment(
                deviceId = deviceId
            )
        )
    }
}