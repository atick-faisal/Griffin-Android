package ai.andromeda.griffin.device

import ai.andromeda.griffin.background.MqttConnectionManagerService
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.SensorModel
import ai.andromeda.griffin.util.SharedPreferencesManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.json.JSONException
import org.json.JSONObject
import kotlin.properties.Delegates

class DeviceViewModel(application: Application, val deviceId: String) :
    AndroidViewModel(application) {

    private val database = DeviceDatabase.getInstance(application).deviceDao
    private val sensors: MutableList<SensorModel> = mutableListOf()
    private var numberOfSensors by Delegates.notNull<Int>()
    private lateinit var mqttService: MqttConnectionManagerService
    private lateinit var client: MqttAndroidClient
    var mBound: Boolean = false

    // ----------------- LIVE DATA VARIABLES -----------------//
    private val _sensorList = MutableLiveData<List<SensorModel>>()
    val sensorList: LiveData<List<SensorModel>>
        get() = _sensorList

    // ---------------- INIT ----------------//
    init {
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
            numberOfSensors = nameArray.size - 1

            for (i in 0 until numberOfSensors) {
                try {
                    sensors.add(SensorModel(
                            sensorName = nameArray[i],
                            sensorStatus = valueArray[i].toInt()
                        )
                    )
                } catch (e: Exception) {
                    Log.i(LOG_TAG, "DEVICE_VM: INTEGER PARSING ERROR : $valueArray")
                }
            }
        }
        Log.i(LOG_TAG, "DEVICE_VM: LOADED : ${sensors.size} SENSORS")
        return sensors
    }

    //---------------- CONTROL SENSOR STATUS ----------------//
    fun toggleStatusAt(position: Int) {
        try {
            val sensor = sensors[position]
            when (sensor.sensorStatus) {
                0 -> sensor.sensorStatus = 1
                1 -> sensor.sensorStatus = 0
                else -> Log.i(LOG_TAG, "DEVICE_VM: CORRUPT SENSOR")
            }
            //------------- SAVING NEW STATUS -----------//
            writeToSharedPreferences()
            _sensorList.value = sensors

            // --------- PUBLISH DATA --------//
            publishData()

            Log.i(LOG_TAG, "DEVICE_VM: SENSOR[$position] = ${sensor.sensorStatus}")

        //----------- PARSING ERROR ------------//
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    fun changeSensorName(position: Int, name: String) {
        sensors[position].sensorName = name
        writeToSharedPreferences()
    }

    // --------------- PUBLISH ---------------//
    private fun publishData() {
        val payload = getPayload()
        if (mBound) {
            if (client.isConnected) {
                mqttService.publish("Sub/$deviceId", payload)
            }
        }
    }

    //-------------------- JSON DATA TO PUBLISH ---------------//
    private fun getPayload(): String {
        val payload = JSONObject()
        try {
            payload.put("Device_ID", deviceId)
            payload.put("Count", 0) // TODO COUNT
            payload.put("Command", "Control")
            payload.put("Number_of_Sensors", numberOfSensors)
            payload.put("Sensors", sensors.map {
                    sensorModel -> sensorModel.sensorStatus
            })
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
            names.append("${sensors[i].sensorName},")
            values.append("${sensors[i].sensorStatus},")
        }
        SharedPreferencesManager.putString(getApplication(), nameKey, names.toString())
        SharedPreferencesManager.putString(getApplication(), valueKey, values.toString())
    }

    //----------- REMOVING DEVICE FROM DATABASE --------- //
    fun removeDevice() {
        viewModelScope.launch {
            delete(deviceId)
        }
    }
    private suspend fun delete(deviceId: String) {
        database.delete(deviceId)
    }

    // ---------- ON_CLEARED() -----------//
    override fun onCleared() {
        super.onCleared()
        Log.i(LOG_TAG, "DEVICE VIEW MODEL CLEARED")
    }
}