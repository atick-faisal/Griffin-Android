package ai.andromeda.griffin.register

import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
import ai.andromeda.griffin.util.generateDeviceId
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_register.view.*

@Suppress("SameParameterValue")
class RegisterFragment : Fragment() {

    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var deviceDatabase: DeviceDatabase
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_register, container, false)

        val application = requireNotNull(this.activity).application
        deviceDatabase = DeviceDatabase.getInstance(application)

        val registerViewModelFactory = RegisterViewModelFactory(
            deviceDatabase, application
        )
        registerViewModel = ViewModelProvider(this, registerViewModelFactory)
            .get(RegisterViewModel::class.java)

        rootView.connectionRetryButton.setOnClickListener {
            registerViewModel.connectToBroker()
        }
        rootView.registerButton.setOnClickListener { registerDevice() }

        registerViewModel.connectionSuccessful.observe(viewLifecycleOwner, Observer {
            it?.let {
                onConnectionSuccessful()
                registerViewModel.doneShowingViews()
            }
        })

        return rootView
    }

    private fun onConnectionSuccessful() {
        rootView.noConnectionView.visibility = View.GONE
        rootView.registrationForm.visibility = View.VISIBLE
    }

    private fun registerDevice() {
        val isNameEmpty = rootView.deviceNameInput.text.isEmpty()
        val isSSIDEmpty = rootView.ssidInput.text.isEmpty()
        val isPasswordEmpty = rootView.passwordInput.text.isEmpty()
        val isContactEmpty = rootView.contact1Input.text.isEmpty()
        val isNumSensorEmpty = rootView.sensorNumberInput.text.isEmpty()

        Log.i(LOG_TAG, "TEXT : ${rootView.deviceNameInput.text.toString().isEmpty()}")

        if (
            isNameEmpty ||
            isSSIDEmpty ||
            isPasswordEmpty ||
            isContactEmpty ||
            isNumSensorEmpty
        ) {
            Log.i(LOG_TAG, "REQUIRED FIELD EMPTY")
        } else {
            val deviceId = generateDeviceId()
            Log.i(LOG_TAG, "DEVICE ID : $deviceId")

            val deviceName = rootView.deviceNameInput.text.toString()
            val ssid = rootView.ssidInput.text.toString()
            val password = rootView.passwordInput.text.toString()
            val contact1 = rootView.contact1Input.text.toString()
            val contact2 = rootView.contact2Input.text.toString()
            val contact3 = rootView.contact3Input.text.toString()
            val numSensors = rootView.sensorNumberInput.text.toString()
            val additionalInfo = rootView.additionalInfoText.text.toString()

            val data = DeviceEntity(
                deviceId = deviceId,
                deviceName = deviceName,
                ssid = ssid,
                password = password,
                contact1 = contact1,
                contact2 = contact2,
                contact3 = contact3,
                numSensors = numSensors.toInt(),
                additionalInfo = additionalInfo
            )

            val payload = deviceId +
                    deviceName + "," +
                    ssid + "," +
                    password + "," +
                    contact1 + "," +
                    contact2 + "," +
                    contact3 + "," +
                    numSensors + "," +
                    additionalInfo

            registerViewModel.publish(payload)
            registerViewModel.saveData(data)
        }
    }
}