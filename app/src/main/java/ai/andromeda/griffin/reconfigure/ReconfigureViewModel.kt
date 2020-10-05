package ai.andromeda.griffin.reconfigure

import ai.andromeda.griffin.background.MqttConnectionManagerService
import ai.andromeda.griffin.config.Config
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.json.JSONException
import org.json.JSONObject

class ReconfigureViewModel(application: Application, val deviceId: String) :
    AndroidViewModel(application) {

    private val database = DeviceDatabase.getInstance(application).deviceDao
    private lateinit var mqttService: MqttConnectionManagerService
    private lateinit var client: MqttAndroidClient
    var mBound: Boolean = false

    private val _device = MutableLiveData<DeviceEntity>()
    val device: LiveData<DeviceEntity>
        get() = _device

    //--------------------- COUNTER -----------------------//
    private var count = SharedPreferencesManager.getLong(
        application, "REG_COUNT" // TODO FIX REG COUNT
    )

    init {
        _device.value = null
        initDevice(deviceId)
    }

    // --------------- INITIALIZE CLIENT ----------------//
    fun initializeClient(service: MqttConnectionManagerService) {
        mqttService = service
        client = service.client
    }

    private fun initDevice(deviceId: String) {
        viewModelScope.launch {
            _device.value = get(deviceId)
        }
    }

    private suspend fun get(deviceId: String): DeviceEntity? {
        return database.get(deviceId)
    }

    fun publishData(deviceEntity: DeviceEntity) {
        val payload = getPayload(deviceEntity)
        Log.i(LOG_TAG, "RECONFIGURE_VM: PAYLOAD -> $payload")
        if (mBound) {
            mqttService.publish("Sub/$deviceId", payload)
        }
    }

    //--------------------- CREATE JSON FORMATTED DATA -----------------//
    private fun getPayload(data: DeviceEntity): String {
        val payload = JSONObject()
        try {
            payload.put("Device_ID", data.deviceId)
            payload.put("Count", ++count)
            payload.put("Command", "Configuration")
            payload.put("SSID", data.ssid)
            payload.put("Password", data.password)
            payload.put("Number_of_Sensors", data.numSensors)
            payload.put("Number_of_Contacts", 3)
            payload.put("Contact_1", data.contact1)
            payload.put("Contact_2", data.contact2)
            payload.put("Contact_3", data.contact3)
            payload.put("Message", data.customMessage)
        } catch (e: JSONException) {
            Log.i(LOG_TAG, "REGISTER_VM: JSON ERROR")
            e.printStackTrace()
        }
        return payload.toString()
    }

    fun updateDevice(deviceEntity: DeviceEntity) {
        if (deviceEntity.numSensors != device.value?.numSensors) {
            writeToSharedPreferences(deviceEntity)
        }
        val updatedDevice = mapDeviceData(deviceEntity)
        viewModelScope.launch { update(updatedDevice) }
    }

    private fun mapDeviceData(deviceEntity: DeviceEntity): DeviceEntity {
        deviceEntity.id = _device.value?.id ?: 999L
        return deviceEntity
    }

    private suspend fun update(deviceEntity: DeviceEntity) {
        database.update(deviceEntity)
    }

    //----------------- WRITING TO SP -----------------//
    private fun writeToSharedPreferences(data: DeviceEntity) {
        val deviceId = data.deviceId
        val deviceName = data.deviceName
        val nameKey = "$deviceId/name"
        val valueKey = "$deviceId/value"
        val names = StringBuilder()
        val values = StringBuilder()

        val n = data.numSensors
        for (i in 0 until n) {
            names.append("Sensor ${i + 1},")
            values.append("0,")
        }
        SharedPreferencesManager.putString(getApplication(), nameKey, names.toString())
        SharedPreferencesManager.putString(getApplication(), valueKey, values.toString())

        // Saving Device Name to Show in Future Alert Notifications
        deviceId?.let {
            SharedPreferencesManager.putString(
                getApplication(), deviceId, deviceName.toString()
            )
        }

        Log.i(Config.LOG_TAG, "RECONFIGURE_VM: WRITING TO SP")
    }

    //------------------------ SAVE COUNT -----------------------//
    private fun saveCount() {
        SharedPreferencesManager.putLong(getApplication(), "REG_COUNT", count)
    }

    //---------------- ON_CLEARED() -------------//
    override fun onCleared() {
        super.onCleared()
        saveCount()
        Log.i(Config.LOG_TAG, "RECONFIGURE_VM: CLIENT CLEARED")
    }
}