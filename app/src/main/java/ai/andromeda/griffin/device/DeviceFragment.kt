package ai.andromeda.griffin.device

import ai.andromeda.griffin.R
import ai.andromeda.griffin.background.MqttConnectionManagerService
import ai.andromeda.griffin.config.Config.LOG_TAG
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_device.view.*

class DeviceFragment : Fragment() {

    private lateinit var mqttService: MqttConnectionManagerService
    private lateinit var deviceViewModel: DeviceViewModel
    private lateinit var sensorAdapter: SensorAdapter
    private var deviceId: String? = null
    private var deviceName: String? = null

    //------------------- SERVICE BINDING CALLBACK ------------------//
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MqttConnectionManagerService.LocalBinder
            mqttService = binder.getInstance()
            deviceViewModel.initializeClient(mqttService)
            deviceViewModel.mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            deviceViewModel.mBound = false
            Log.i(LOG_TAG, "DEVICE_F: SERVICE UNBOUNDED")
        }
    }

    //------------- ON_CREATE_VIEW() -----------------//
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_device, container, false)
        val application = requireNotNull(this.activity).application

        //------------------ ARGUMENTS -------------------//
        deviceId = DeviceFragmentArgs.fromBundle(requireArguments()).deviceId
        deviceName = DeviceFragmentArgs.fromBundle(requireArguments())
            .deviceName ?: getString(R.string.unknown_device)

        // ------------------------- VIEW MODEL SETUP ---------------------//
        val deviceViewModelFactory = DeviceViewModelFactory(application, deviceId!!)
        deviceViewModel = ViewModelProvider(this, deviceViewModelFactory)
            .get(DeviceViewModel::class.java)

        //---------------- LIVE DATA OBSERVERS ---------------//
        deviceViewModel.sensorList.observe(viewLifecycleOwner, Observer {
            it?.let {
                // Update recyclerView
                sensorAdapter.sensorList = it
            }
        })

        //--------------- RECYCLER VIEW SETUP -----------------//
        sensorAdapter = SensorAdapter { view, position ->
            Log.i(LOG_TAG, "CLICK AT : $position")
            when (view) {
                // CLICKED ON THE IMAGE BUTTON
                0 -> deviceViewModel.toggleStatusAt(position)
                // CLICKED ON EDIT
                1 -> navigateToEdit(position)
            }
        }
        rootView.sensorList.adapter = sensorAdapter
        rootView.sensorList.layoutManager = GridLayoutManager(activity, 2)

        //-------------------- MENU ------------------//
        setHasOptionsMenu(true)
        (context as AppCompatActivity).supportActionBar?.title = deviceName

        return rootView
    }

    //----------------------- ON_START() ------------------//
    override fun onStart() {
        super.onStart()
        activity?.let {
            Intent(activity, MqttConnectionManagerService::class.java).also { intent ->
                activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    //----------------- NAVIGATE TO EDIT FRAGMENT -----------------//
    private fun navigateToEdit(position: Int) {
        findNavController().navigate(
            DeviceFragmentDirections.actionDeviceDetailsFragmentToEditNameFragment(
                position = position, deviceId = deviceId
            )
        )
    }

    //----------------------- NAVIGATE TO HOME FRAGMENT ------------------//
    private fun navigateToHome() {
        findNavController().navigate(
            DeviceFragmentDirections.actionDeviceDetailsFragmentToHomeFragment()
        )
    }

    //---------------- CREATE MENU -------------------//
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.device_menu, menu)
    }

    //------------------- MENU LISTENER ----------------------//
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.shareDevice -> {
                findNavController().navigate(
                    DeviceFragmentDirections.actionDeviceDetailsFragmentToShareFragment(
                        deviceId = deviceId, deviceName = deviceName
                    )
                )
                true
            }
            R.id.deleteDevice -> {
                deviceViewModel.removeDevice()
                navigateToHome()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.unbindService(connection)
        deviceViewModel.mBound = false
        Log.i(LOG_TAG, "DEVICE_F: SERVICE UNBOUND")
    }
}