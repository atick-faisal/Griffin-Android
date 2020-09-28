package ai.andromeda.griffin.device

import ai.andromeda.griffin.config.Config
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.SensorModel
import ai.andromeda.griffin.util.SharedPreferencesManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.properties.Delegates

class SharedViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _sensorList = MutableLiveData<List<SensorModel>>()
    val sensorList: LiveData<List<SensorModel>>
        get() = _sensorList

    private var sensors: MutableList<SensorModel> = mutableListOf()
    private var numberOfSensors by Delegates.notNull<Int>()

    fun changeSensorName(deviceId: String, position: Int, name: String) {
        val names = SharedPreferencesManager
            .getString(getApplication(), "$deviceId/name")


        val values = SharedPreferencesManager
            .getString(getApplication(), "$deviceId/value")

        if (names != null && values != null) {
            val nameArray = names.split(",")
            val valueArray = values.split(",")

            numberOfSensors = nameArray.size - 1

            for (i in 0 until numberOfSensors) {
                sensors.add(
                    SensorModel(
                        sensorName = nameArray[i],
                        sensorStatus = valueArray[i].toInt()
                    )
                )
            }
        }
        sensors[position].sensorName = name
        writeToSharedPreferences(deviceId)
        _sensorList.value = sensors
    }

    private fun writeToSharedPreferences(deviceId: String) {
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

    fun doneUpdating() {
        sensors = mutableListOf()
        _sensorList.value = null
    }
}