package ai.andromeda.griffin.reconfigure

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.andromeda.griffin.R
import ai.andromeda.griffin.background.MqttConnectionManagerService
import ai.andromeda.griffin.config.Config
import ai.andromeda.griffin.database.DeviceEntity
import ai.andromeda.griffin.device.DeviceFragmentArgs
import ai.andromeda.griffin.util.generateDeviceId
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_reconfigure.view.*
import kotlinx.android.synthetic.main.fragment_reconfigure.view.contact1Input
import kotlinx.android.synthetic.main.fragment_reconfigure.view.contact1InputField
import kotlinx.android.synthetic.main.fragment_reconfigure.view.contact2Input
import kotlinx.android.synthetic.main.fragment_reconfigure.view.contact3Input
import kotlinx.android.synthetic.main.fragment_reconfigure.view.customMessageText
import kotlinx.android.synthetic.main.fragment_reconfigure.view.deviceNameInput
import kotlinx.android.synthetic.main.fragment_reconfigure.view.deviceNameInputField
import kotlinx.android.synthetic.main.fragment_reconfigure.view.passwordInput
import kotlinx.android.synthetic.main.fragment_reconfigure.view.passwordInputField
import kotlinx.android.synthetic.main.fragment_reconfigure.view.sensorNumberInput
import kotlinx.android.synthetic.main.fragment_reconfigure.view.sensorNumberInputField
import kotlinx.android.synthetic.main.fragment_reconfigure.view.ssidInput
import kotlinx.android.synthetic.main.fragment_reconfigure.view.ssidInputField

class ReconfigureFragment : Fragment() {

    private lateinit var mqttService: MqttConnectionManagerService
    private lateinit var reconfigureViewModel: ReconfigureViewModel
    private lateinit var rootView: View
    private lateinit var deviceId: String

    //------------------- SERVICE BINDING CALLBACK ------------------//
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MqttConnectionManagerService.LocalBinder
            mqttService = binder.getInstance()
            reconfigureViewModel.initializeClient(mqttService)
            reconfigureViewModel.mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            reconfigureViewModel.mBound = false
            Log.i(Config.LOG_TAG, "RECONFIGURE_F: SERVICE UNBOUNDED")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_reconfigure, container, false)
        val application = requireActivity().application

        deviceId = DeviceFragmentArgs.fromBundle(requireArguments()).deviceId ?: ""

        val reconfigureViewModelFactory = ReconfigureViewModelFactory(application, deviceId)
        reconfigureViewModel = ViewModelProvider(this, reconfigureViewModelFactory)
            .get(ReconfigureViewModel::class.java)


        reconfigureViewModel.device.observe(viewLifecycleOwner, Observer { device ->
            device?.let {
                updateUI(device)
            }
        })

        rootView.saveChangesButton.setOnClickListener { saveChanges() }

        return rootView
    }

    //----------------------- ON_START() ------------------//
    override fun onStart() {
        super.onStart()
        Log.i(Config.LOG_TAG, "RECONFIGURE_F: ON START")
        activity?.let {
            Intent(activity, MqttConnectionManagerService::class.java).also { intent ->
                activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private fun updateUI(device: DeviceEntity) {
        rootView.deviceNameInput.setText(device.deviceName.toString())
        rootView.ssidInput.setText(device.ssid.toString())
        rootView.passwordInput.setText(device.password.toString())
        rootView.contact1Input.setText(device.contact1.toString())
        rootView.contact2Input.setText(device.contact2)
        rootView.contact3Input.setText(device.contact3)
        rootView.sensorNumberInput.setText(device.numSensors.toString())
        rootView.customMessageText.setText(device.customMessage)
    }

    //------------------------ REGISTER DEVICE --------------------//
    private fun saveChanges() {
        val isNameEmpty = rootView.deviceNameInput.text.toString().isEmpty()
        val isSSIDEmpty = rootView.ssidInput.text.toString().isEmpty()
        val isPasswordEmpty = rootView.passwordInput.text.toString().isEmpty()
        val isContactEmpty = rootView.contact1Input.text.toString().isEmpty()
        val isNumSensorEmpty = rootView.sensorNumberInput.text.toString().isEmpty()

        //------------------- EMPTY FIELD CHECK -----------------//
        when {
            isNameEmpty -> {
                rootView.deviceNameInputField.error = getString(R.string.empty_field_warning)
                Log.i(Config.LOG_TAG, "REGISTER_F: NAME FIELD EMPTY")
            }
            isSSIDEmpty -> {
                rootView.ssidInputField.error = getString(R.string.empty_field_warning)
                Log.i(Config.LOG_TAG, "REGISTER_F: NAME FIELD EMPTY")
            }
            isPasswordEmpty -> {
                rootView.passwordInputField.error = getString(R.string.empty_field_warning)
                Log.i(Config.LOG_TAG, "REGISTER_F: PASSWORD FIELD EMPTY")
            }
            isContactEmpty -> {
                rootView.contact1InputField.error = getString(R.string.empty_field_warning)
                Log.i(Config.LOG_TAG, "REGISTER_F: CONTACT FIELD EMPTY")
            }
            isNumSensorEmpty -> {
                rootView.sensorNumberInputField.error = getString(R.string.empty_field_warning)
                Log.i(Config.LOG_TAG, "REGISTER_F: PASSWORD FIELD EMPTY")
            }

            //------------------- PUBLISH DATA -----------------//
            else -> {
                val deviceName = rootView.deviceNameInput.text.toString()
                val ssid = rootView.ssidInput.text.toString()
                val password = rootView.passwordInput.text.toString()
                val contact1 = rootView.contact1Input.text.toString()
                val contact2 = rootView.contact2Input.text.toString()
                val contact3 = rootView.contact3Input.text.toString()
                val numSensors = rootView.sensorNumberInput.text.toString().toInt()
                val customMessage = rootView.customMessageText.text.toString()

                //--------------- SENSOR NUMBER CHECK ---------------//
                if (numSensors > 999) {
                    rootView.sensorNumberInputField.error = getString(R.string.too_many_sensors)
                    Log.i(Config.LOG_TAG, "REGISTER_F: PASSWORD FIELD EMPTY")
                }
                else {
                    val data = DeviceEntity(
                        deviceId = deviceId,
                        deviceName = deviceName,
                        ssid = ssid,
                        password = password,
                        contact1 = contact1,
                        contact2 = contact2,
                        contact3 = contact3,
                        numSensors = numSensors,
                        lockedSensors = numSensors,
                        customMessage = customMessage
                    )
                    reconfigureViewModel.updateDevice(data)
                    reconfigureViewModel.publishData(data)
                    // navigateToHome()
                }
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(
            ReconfigureFragmentDirections.actionReconfigureFragmentToHomeFragment()
        )
    }
}