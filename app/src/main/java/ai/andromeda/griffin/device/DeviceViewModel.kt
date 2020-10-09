package ai.andromeda.griffin.device

import ai.andromeda.griffin.background.MqttConnectionManagerService
import ai.andromeda.griffin.config.Config.DEVICE_ID_KEY
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
import ai.andromeda.griffin.database.SensorModel
import ai.andromeda.griffin.util.SharedPreferencesManager
import ai.andromeda.griffin.util.showMessage
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
import kotlin.properties.Delegates

class DeviceViewModel(application: Application, val deviceId: String) :
    AndroidViewModel(application) {

    private val database = DeviceDatabase.getInstance(application).deviceDao
    private var sensors: MutableList<SensorModel> = mutableListOf()
    private var numberOfSensors by Delegates.notNull<Int>()
    private lateinit var mqttService: MqttConnectionManagerService
    private lateinit var client: MqttAndroidClient
    var mBound: Boolean = false

    //--------------------- COUNTER -----------------------//
    private var count = SharedPreferencesManager.getLong(
        application, "$deviceId/count"
    )

    // ----------------- LIVE DATA VARIABLES -----------------//

    // This is for refreshing the sensor list
    // When new message comes the device list is refreshed automatica
    val deviceList = database.getAll()

    private val _sensorList = MutableLiveData<List<SensorModel>>()
    val sensorList: LiveData<List<SensorModel>>
        get() = _sensorList

    //---------------- REFRESH DATA -----------------//
    fun refreshData() {
        _sensorList.value = getSensorList()
    }

    // --------------- INITIALIZE CLIENT ----------------//
    fun initializeClient(service: MqttConnectionManagerService) {
        mqttService = service
        client = service.client
    }

    //---------------------- GET SENSORS FROM SP -------------------//
    private fun getSensorList(): List<SensorModel> {
        Log.i(LOG_TAG, "DEVICE_VM: LOADING SENSORS...")
        val names = SharedPreferencesManager.getString(getApplication(), "$deviceId/name")
        val values = SharedPreferencesManager.getString(getApplication(), "$deviceId/value")

        if (names != null && values != null) {
            val nameArray = names.split(",")
            val valueArray = values.split(",")

            // ------ TOTAL SENSORS -------//
            // CHECKING SIZE OF NAME ARRAY CAUSE INCOMING MESSAGE
            // CAN'T ALTER THIS
            numberOfSensors = nameArray.size - 1

            // Empty sensors
            sensors = mutableListOf()

            for (i in 0 until numberOfSensors) {
                try {
                    sensors.add(SensorModel(
                            sensorName = nameArray[i],
                            sensorStatus = valueArray[i].toInt()
                        )
                    )
                } catch (e: Exception) {
                    Log.i(LOG_TAG, "DEVICE_VM: INTEGER PARSING ERROR : $nameArray")
                }
            }
        }
        Log.i(LOG_TAG, "DEVICE_VM: LOADED : ${sensors.size} SENSORS")
        return sensors
    }

    //---------------- CONTROL SENSOR STATUS ----------------//
    fun toggleStatusAt(position: Int) {
        try {
            if (mBound) {
                if (client.isConnected) {
                    val sensor = sensors[position]
                    when (sensor.sensorStatus) {
                        0 -> sensor.sensorStatus = 1
                        1 -> sensor.sensorStatus = 0
                        else -> Log.i(LOG_TAG, "DEVICE_VM: CORRUPT SENSOR")
                    }

                    //------------- SAVING NEW STATUS -----------//
                    writeToSharedPreferences()
                    updateDatabase()
                    _sensorList.value = sensors

                    // --------- PUBLISH DATA --------//
                    publishData()

                    Log.i(LOG_TAG, "DEVICE_VM: SENSOR[$position] = ${sensor.sensorStatus}")
                }
                else {
                    showMessage(getApplication(), "NO CONNECTION")
                }
            }

        //----------- PARSING ERROR ------------//
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    // ----------------- CHANGE_SENSOR_NAME() --------------------//
    fun changeSensorName(deviceId: String, position: Int, name: String) {
        val names = SharedPreferencesManager
            .getString(getApplication(), "$deviceId/name")

        if (names != null) {
            val nameArray: MutableList<String> = names.split(",").toMutableList()
            try {
                nameArray[position] = name
                saveChanges(deviceId, nameArray)
                Log.i(LOG_TAG, "EDIT_VM: DEVICE NAME CHANGED")
            } catch (e: ArrayIndexOutOfBoundsException) {
                Log.i(LOG_TAG, "EDIT_VM: NAME ARRAY INDEX OUT OF BOUND")
                e.printStackTrace()
            }
        }
    }

    // --------------- SAVE CHANGES -------------------//
    private fun saveChanges(deviceId: String, nameArray: List<String>) {
        val nameKey = "$deviceId/name"
        val names = StringBuilder()
        for (i in 0 until nameArray.size - 1) {
            names.append("${nameArray[i]},")
        }
        SharedPreferencesManager.putString(getApplication(), nameKey, names.toString())
    }

    // --------------- PUBLISH ---------------//
    private fun publishData() {
        val payload = getPayload()
        if (mBound) {
            // mqttService.publish("Sub/$deviceId", payload)
            mqttService.publish("Debug", payload) // TODO CHANGE PUBLISH TOPIC
        }
    }

    //-------------------- JSON DATA TO PUBLISH ---------------//
    private fun getPayload(): String {
        val payload = JSONObject()
        val sensorArray = sensors.map { sensorModel -> sensorModel.sensorStatus }
        try {
            payload.put("Device_ID", deviceId)
            payload.put("Count", ++count)
            payload.put("Command", "Control")
            payload.put("Number_of_Sensors", numberOfSensors)
            for (sensor in sensorArray) {
                payload.accumulate("Sensors", sensor)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return payload.toString()
    }

    //------------------ WRITE CHANGES TO SP ----------------//
    private fun writeToSharedPreferences() {
        val nameKey = "$deviceId/name"
        val valueKey = "$deviceId/value"
        val names = StringBuilder()
        val values = StringBuilder()

        for (i in 0 until numberOfSensors) {
            try {
                names.append("${sensors[i].sensorName},")
                values.append("${sensors[i].sensorStatus},")
            } catch (e: ArrayIndexOutOfBoundsException) {
                Log.i(LOG_TAG, "DEVICE_VM: ARRAY INDEX OUT OF BOUND")
                e.printStackTrace()
            }
        }
        SharedPreferencesManager.putString(getApplication(), nameKey, names.toString())
        SharedPreferencesManager.putString(getApplication(), valueKey, values.toString())
    }

    //------------------------- UPDATE_DATABASE() ------------------------//
    private fun updateDatabase() {
        val sensorValues = sensors.map { it.sensorStatus }
        Log.i(LOG_TAG, "DEVICE_VM: SENSOR VALUES -> $sensorValues")
        val lockedSensors = sensorValues.size - sensorValues.sum()
        CoroutineScope(Dispatchers.IO).launch {
            val device = get(deviceId)
            device?.let {
                if (sensorValues.size == device.numSensors) {
                    if (lockedSensors >= 0) {
                        device.lockedSensors = lockedSensors
                        update(device)
                        Log.i(LOG_TAG, "SERVICE: WRITING SENSOR VALUES TO DB")
                    }
                }
            }
        }
    }

    // --------------- DATABASE SUSPEND METHODS ------------------//
    private suspend fun update(device: DeviceEntity) {
        database.update(device)
    }

    private suspend fun get(deviceId: String): DeviceEntity? {
        return database.get(deviceId)
    }

    //----------- REMOVING DEVICE FROM DATABASE --------- //
    fun removeDevice() {
        removeIdFromSharedPreferences()
        viewModelScope.launch {
            delete(deviceId)
        }
    }

    private fun removeIdFromSharedPreferences() {
        val deviceIds = SharedPreferencesManager.getString(getApplication(), DEVICE_ID_KEY)
        deviceIds?.let {
            val newDeviceIds = deviceIds.replace(deviceId, "")
            SharedPreferencesManager.putString(getApplication(), DEVICE_ID_KEY, newDeviceIds)
            Log.i(LOG_TAG, "DEVICE_VM: NEW IDS -> $newDeviceIds")
        }
    }

    private suspend fun delete(deviceId: String) {
        database.delete(deviceId)
    }

    //------------------------ SAVE COUNT -----------------------//
    private fun saveCount() {
        SharedPreferencesManager.putLong(getApplication(), "$deviceId/count", count)
    }

    // ---------- ON_CLEARED() -----------//
    override fun onCleared() {
        super.onCleared()
        saveCount()
        Log.i(LOG_TAG, "DEVICE_VM: VIEW MODEL CLEARED")
    }
}