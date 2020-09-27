package ai.andromeda.griffin.device

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs

class DeviceFragment : Fragment() {

    private lateinit var deviceViewModel: DeviceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(
            R.layout.fragment_device_details,
            container,
            false
        )

        val deviceId = DeviceFragmentArgs.fromBundle(requireArguments()).deviceId

        val application = requireNotNull(this.activity).application

        val deviceViewModelFactory = DeviceViewModelFactory(
            application,
            deviceId
        )
        deviceViewModel = ViewModelProvider(this, deviceViewModelFactory)
            .get(DeviceViewModel::class.java)

        deviceViewModel.sensorList.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.i(LOG_TAG, "SENSOR SIZE : ${it.size}")
            }
        })

        return rootView
    }
}