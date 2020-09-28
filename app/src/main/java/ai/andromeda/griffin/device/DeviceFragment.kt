package ai.andromeda.griffin.device

import android.os.Bundle
import androidx.fragment.app.Fragment
import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.LOG_TAG
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_device.view.*

class DeviceFragment : Fragment() {

    private lateinit var deviceViewModel: DeviceViewModel
    private lateinit var sensorAdapter: SensorAdapter
    private var deviceId: String? = null
    private var deviceName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(
            R.layout.fragment_device,
            container,
            false
        )

        deviceId = DeviceFragmentArgs.fromBundle(requireArguments()).deviceId
        deviceName = DeviceFragmentArgs.fromBundle(requireArguments())
            .deviceName ?: getString(R.string.unknown_device)

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
                sensorAdapter.sensorList = it
            }
        })

        sensorAdapter = SensorAdapter { position ->
            Log.i(LOG_TAG, "CLICK AT : $position")
            deviceViewModel.toggleStatusAt(position)
        }

        rootView.sensorList.adapter = sensorAdapter
        rootView.sensorList.layoutManager = GridLayoutManager(activity, 2)

        setHasOptionsMenu(true)
        (context as AppCompatActivity).supportActionBar?.title = deviceName

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.device_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.shareDevice -> {
                findNavController().navigate(
                    DeviceFragmentDirections.actionDeviceDetailsFragmentToShareFragment(
                        deviceId = deviceId,
                        deviceName = deviceName
                    )
                )
                true
            }
            else -> false
        }
//        return NavigationUI.
//        onNavDestinationSelected(item,requireView().findNavController())
//                || super.onOptionsItemSelected(item)
    }
}