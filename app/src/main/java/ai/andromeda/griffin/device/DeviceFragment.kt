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
import android.view.inputmethod.InputMethodManager
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
    private lateinit var rootView: View
    private var deviceId: String? = null
    private var deviceName: String? = null
    private var currentPosition = 0

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
        rootView = inflater.inflate(R.layout.fragment_device, container, false)
        val application = requireNotNull(this.activity).application

        //-------------------- ARGUMENTS -------------------//
        deviceId = DeviceFragmentArgs.fromBundle(requireArguments()).deviceId
        deviceName = DeviceFragmentArgs.fromBundle(requireArguments())
            .deviceName ?: getString(R.string.unknown_device)

        // ------------------------- VIEW MODEL SETUP ---------------------//
        val deviceViewModelFactory = DeviceViewModelFactory(application, deviceId!!)
        deviceViewModel = ViewModelProvider(this, deviceViewModelFactory)
            .get(DeviceViewModel::class.java)

        //---------------- LIVE DATA OBSERVERS ---------------//
        deviceViewModel.deviceList.observe(viewLifecycleOwner, Observer {
            it?.let {
                deviceViewModel.refreshData()
            }
        })

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
                1 -> {
                    currentPosition = position
                    showEditDialog()
                }
            }
        }
        rootView.sensorList.adapter = sensorAdapter
        rootView.sensorList.layoutManager = GridLayoutManager(activity, 2)

        //----------------- CLICK LISTENERS ------------------//
        rootView.saveNameButton.setOnClickListener { onSaveButtonClick() }
        rootView.cancelButton.setOnClickListener { onCancelButtonClick() }
        rootView.cancelDeleteButton.setOnClickListener { hideDeleteDialog() }
        rootView.deleteDialogButton.setOnClickListener {
                deviceViewModel.removeDevice()
                navigateToHome()
        }

        //-------------------- MENU ------------------//
        setHasOptionsMenu(true)
        (context as AppCompatActivity).supportActionBar?.title = deviceName

        return rootView
    }

    //----------------------- ON_START() ------------------//
    override fun onStart() {
        super.onStart()
        Log.i(LOG_TAG, "DEVICE_F: ON START")
        activity?.let {
            Intent(activity, MqttConnectionManagerService::class.java).also { intent ->
                activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    //----------------- SAVE BUTTON CLICK --------------//
    private fun onSaveButtonClick() {
        val name = rootView.nameTextInput.text.toString()
        if (name.isNotEmpty()) {
            deviceId?.let {
                deviceViewModel.changeSensorName(deviceId!!, currentPosition, name)
                deviceViewModel.refreshData()
                hideEditDialog()
            }
        } else {
            rootView.nameTextInputField.error = getString(R.string.empty_field_warning)
        }
    }

    //---------------- CANCEL BUTTON CLICK ----------------//
    private fun onCancelButtonClick() {
        hideEditDialog()
    }

    //------------------- SHOW EDIT DIALOG ------------------//
    private fun showEditDialog() {
        rootView.nameTextInput.setText("")
        rootView.nameTextInput.requestFocus()
        rootView.editDialog.visibility = View.VISIBLE
        rootView.sensorList.alpha = 0.2F
        showKeyboard()
    }

    //------------------- HIDE EDIT DIALOG ------------------//
    private fun hideEditDialog() {
        rootView.nameTextInput.setText("")
        rootView.editDialog.visibility = View.GONE
        rootView.sensorList.alpha = 1.0F
        hideKeyboard()
    }

    //------------------- SHOW DELETE DIALOG ------------------//
    private fun showDeleteDialog() {
        rootView.deleteDialog.visibility = View.VISIBLE
        rootView.sensorList.alpha = 0.1F
    }

    //------------------- HIDE DELETE DIALOG ------------------//
    private fun hideDeleteDialog() {
        rootView.deleteDialog.visibility = View.GONE
        rootView.sensorList.alpha = 1.0F
    }

    //-------------------- SHOW KEYBOARD ---------------//
    private fun showKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        imm.showSoftInput(rootView.nameTextInput, 0)
    }

    //---------------------- HIDE KEYBOARD -----------------//
    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    //----------------------- NAVIGATE TO HOME FRAGMENT ------------------//
    private fun navigateToHome() {
        findNavController().navigate(
            DeviceFragmentDirections.actionDeviceDetailsFragmentToHomeFragment()
        )
    }

    //------------------ CREATE MENU -------------------//
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
                showDeleteDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //---------- ON_STOP() -----------//
    override fun onStop() {
        super.onStop()
        activity?.unbindService(connection)
        deviceViewModel.mBound = false
        Log.i(LOG_TAG, "DEVICE_F: SERVICE UNBOUND")
    }
}