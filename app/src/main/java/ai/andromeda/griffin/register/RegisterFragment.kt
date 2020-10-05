package ai.andromeda.griffin.register

import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceEntity
import ai.andromeda.griffin.util.generateDeviceId
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_register.view.*

@Suppress("SameParameterValue")
class RegisterFragment : Fragment() {

    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var rootView: View

    //------------------ ON CREATE VIEW() -------------------//
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_register, container, false)
        val application = requireActivity().application

        //--------------------- VIEW MODEL SETUP ---------------------//
        val registerViewModelFactory = RegisterViewModelFactory(application)
        registerViewModel = ViewModelProvider(this, registerViewModelFactory)
            .get(RegisterViewModel::class.java)

        //-------------------- CLICK LISTENERS --------------------//
        rootView.connectionRetryButton.setOnClickListener {
            registerViewModel.connectToBroker()
        }
        rootView.registerButton.setOnClickListener { registerDevice() }
        rootView.cancelButton.setOnClickListener { navigateToHome() }

        //------------------------ LIVE DATA OBSERVERS -----------------------//
        registerViewModel.connectionSuccessful.observe(viewLifecycleOwner, Observer {
            it?.let {
                onConnectionSuccessful()
                registerViewModel.doneShowingViews()
            }
        })
        registerViewModel.registrationSuccessful.observe(viewLifecycleOwner, Observer {
            it?.let {
                registerViewModel.saveData()
                hideProgress()
                navigateToHome()
                registerViewModel.doneNavigatingToHome()
            }
        })

        //-------------------- MENU -------------------//
        (context as AppCompatActivity).supportActionBar?.title =
            getString(R.string.register)

        return rootView
    }

    override fun onStart() {
        super.onStart()
        registerViewModel.connectToBroker()
    }

    //-------------------- CONNECTED ---------------------//
    private fun onConnectionSuccessful() {
        rootView.noConnectionView.visibility = View.GONE
        rootView.registrationForm.visibility = View.VISIBLE
    }

    //-------------------- PROGRESS DIALOG -----------------//
    private fun showProgress() {
        rootView.registrationForm.alpha = 0.1F
        rootView.progressDialog.visibility = View.VISIBLE
    }
    private fun hideProgress() {
        rootView.registrationForm.alpha = 1.0F
        rootView.progressDialog.visibility = View.GONE
    }

    //------------------------ REGISTER DEVICE --------------------//
    private fun registerDevice() {
        val isNameEmpty = rootView.deviceNameInput.text.toString().isEmpty()
        val isSSIDEmpty = rootView.ssidInput.text.toString().isEmpty()
        val isPasswordEmpty = rootView.passwordInput.text.toString().isEmpty()
        val isContactEmpty = rootView.contact1Input.text.toString().isEmpty()
        val isNumSensorEmpty = rootView.sensorNumberInput.text.toString().isEmpty()

        //------------------- EMPTY FIELD CHECK -----------------//
        when {
            isNameEmpty -> {
                rootView.deviceNameInputField.error = getString(R.string.empty_field_warning)
                Log.i(LOG_TAG, "REGISTER_F: NAME FIELD EMPTY")
            }
            isSSIDEmpty -> {
                rootView.ssidInputField.error = getString(R.string.empty_field_warning)
                Log.i(LOG_TAG, "REGISTER_F: NAME FIELD EMPTY")
            }
            isPasswordEmpty -> {
                rootView.passwordInputField.error = getString(R.string.empty_field_warning)
                Log.i(LOG_TAG, "REGISTER_F: PASSWORD FIELD EMPTY")
            }
            isContactEmpty -> {
                rootView.contact1InputField.error = getString(R.string.empty_field_warning)
                Log.i(LOG_TAG, "REGISTER_F: CONTACT FIELD EMPTY")
            }
            isNumSensorEmpty -> {
                rootView.sensorNumberInputField.error = getString(R.string.empty_field_warning)
                Log.i(LOG_TAG, "REGISTER_F: PASSWORD FIELD EMPTY")
            }

            //------------------- PUBLISH DATA -----------------//
            else -> {
                val deviceId = generateDeviceId()
                Log.i(LOG_TAG, "REGISTER_F: DEVICE ID : $deviceId")

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
                    Log.i(LOG_TAG, "REGISTER_F: PASSWORD FIELD EMPTY")
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
                    showProgress()
                    hideKeyboard()
                    registerViewModel.publish(data)
                }
            }
        }
    }

    //-------------------- NAVIGATE HOME -------------------//
    private fun navigateToHome() {
        findNavController().navigate(
            RegisterFragmentDirections.actionRegisterFragmentToHomeFragment()
        )
    }

    //---------------------- HIDE KEYBOARD -----------------//
    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}