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

class ScannerViewModel(application: Application, deviceDatabase: DeviceDatabase) :
    AndroidViewModel(application) {

    val database = deviceDatabase.deviceDao
    private var device: DeviceEntity? = null

    private val _deviceName = MutableLiveData<String>()
    val deviceName: LiveData<String>
        get() = _deviceName

    init {
        _deviceName.value = null
    }

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
            val additionalInfo = json.getString("additionalInfo")
            val lockedSensors = json.getInt("lockedSensors")
            device = DeviceEntity(
                deviceId = deviceId,
                deviceName = name,
                ssid = ssid,
                password = password,
                contact1 = contact1,
                contact2 = contact2,
                contact3 = contact3,
                numSensors =  numSensors,
                additionalInfo = additionalInfo,
                lockedSensors = lockedSensors
            )
            _deviceName.value = name
        } catch (e: JSONException) {
            Log.i(LOG_TAG, "QR CODE NOT CORRECT")
            _deviceName.value = "Device Not Recognized"
            e.printStackTrace()
        }
    }

    fun onTryAgain() {
        _deviceName.value = null
        device = null
    }

    fun saveData() {
        if (device != null) {
            writeToSharedPreferences()
            viewModelScope.launch {
                insert()
            }
            Log.i(LOG_TAG, "DEVICE ADDED")
        } else {
            Log.i(LOG_TAG, "DEVICE NOT INITIALIZED")
        }
    }

    private fun writeToSharedPreferences() {
        device?.let {
            val nameKey = device!!.deviceId + "/name"
            val valueKey = device!!.deviceId + "/value"
            val names = StringBuilder()
            val values = StringBuilder()

            val n = device!!.numSensors
            for (i in 0 until n) {
                names.append("SENSOR $i,")
                values.append("0,")
            }
            SharedPreferencesManager.putString(getApplication(), nameKey, names.toString())
            SharedPreferencesManager.putString(getApplication(), valueKey, values.toString())
        }
    }

    private suspend fun insert() {
        device?.let { database.insert(it) }
    }
}