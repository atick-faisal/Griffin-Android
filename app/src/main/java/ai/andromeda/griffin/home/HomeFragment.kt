package ai.andromeda.griffin.home

import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var deviceDatabase: DeviceDatabase
    private lateinit var deviceList: List<DeviceEntity>
    private lateinit var deviceListAdapter: DeviceListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        rootView.fab.setOnClickListener { navigateToRegister() }

        deviceListAdapter = DeviceListAdapter()

        rootView.deviceList.apply {
            layoutManager = GridLayoutManager(activity, 2)
            adapter = deviceListAdapter
            setHasFixedSize(true)
        }

        deviceDatabase = DeviceDatabase.getInstance(
            requireNotNull(this.activity).application
        )

        CoroutineScope(Dispatchers.IO).launch {
            fetchDevices()
        }

        return rootView
    }

    private fun navigateToRegister() {
        findNavController().navigate(R.id.action_homeFragment_to_registerFragment)
    }

    private suspend fun fetchDevices() {
        deviceList = deviceDatabase.deviceDao.getAll()
        deviceListAdapter.submitList(deviceList)
        Log.i(LOG_TAG, "FETCHED ${deviceList.size}")
    }
}