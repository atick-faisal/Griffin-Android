package ai.andromeda.griffin.scanner

import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
import ai.andromeda.griffin.util.SharedPreferencesManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class ScannerViewModel(application: Application) :
    AndroidViewModel(application) {

    val database = DeviceDatabase.getInstance(application).deviceDao
    private var device: DeviceEntity? = null
    private var sensorNames: String? = null
    private var sensorValues: String? = null

    //---------------- LIVE DATA ---------------//
    private val _deviceName = MutableLiveData<String>()
    val deviceName: LiveData<String>
        get() = _deviceName

    //------------ INIT ------------//
    init {
        _deviceName.value = null
    }

    //------------- DATA PARSING --------------//
    fun parseData(data: String) {
        try {
            val json = JSONObject(data)
            val deviceId = json.getString("deviceId")
            val name = json.getString("deviceName")
            val ssid = json.getString("ssid")
            val password = json.getString("password")
            val contact1 = json.getString("contact1")
            val contact2 = json.getString("contact2")
            val contact3 = json.getString("contact3")
            val numSensors = json.getInt("numSensors")
            val customMessage = json.getString("customMessage")
            val lockedSensors = json.getInt("lockedSensors")
            sensorNames = json.getString("sensorNames")
            sensorValues = json.getString("sensorValues")
            device = DeviceEntity(
                deviceId = deviceId,
                deviceName = name,
                ssid = ssid,
                password = password,
                contact1 = contact1,
                contact2 = contact2,
                contact3 = contact3,
                numSensors = numSensors,
                customMessage = customMessage,
                lockedSensors = lockedSensors
            )
            _deviceName.value = name
        } catch (e: JSONException) {
            Log.i(LOG_TAG, "QR CODE NOT CORRECT")
            _deviceName.value = "Device Not Recognized"
            e.printStackTrace()
        }
    }

    //------------ TRY AGAIN -----------//
    fun onTryAgain() {
        _deviceName.value = null
        device = null
    }

    //------------ SAVE DATA ------------//
    fun saveData() {
        if (device != null) {
            viewModelScope.launch {
                insert()
            }
            Log.i(LOG_TAG, "DEVICE ADDED")
        } else {
            Log.i(LOG_TAG, "DEVICE NOT INITIALIZED")
        }
        if (sensorNames != null && sensorValues != null) {
            writeToSharedPreferences()
        }
    }

    //------------- WRITE TO SP ---------------//
    private fun writeToSharedPreferences() {
        device?.let {
            val nameKey = device?.deviceId.toString() + "/name"
            val valueKey = device?.deviceId.toString() + "/value"
            SharedPreferencesManager.putString(getApplication(), nameKey, sensorNames!!)
            SharedPreferencesManager.putString(getApplication(), valueKey, sensorValues!!)
        }
    }

    //------------ DATABASE -----------//
    private suspend fun insert() {
        device?.let { database.insert(it) }
    }
}