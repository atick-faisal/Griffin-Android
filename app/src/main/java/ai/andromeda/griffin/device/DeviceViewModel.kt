package ai.andromeda.griffin.device

import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.SensorModel
import ai.andromeda.griffin.util.SharedPreferencesManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DeviceViewModel(
    application: Application,
    val deviceId: String?
) : AndroidViewModel(application) {

    private val sensors: MutableList<SensorModel> = mutableListOf()
    private val _sensorList = MutableLiveData<List<SensorModel>>()
    val sensorList: LiveData<List<SensorModel>>
        get() = _sensorList

    init {
        _sensorList.value = getSensorList()
    }

    private fun getSensorList(): List<SensorModel> {
        Log.i(LOG_TAG, "INIT CALLED")
        val names = SharedPreferencesManager
            .getString(getApplication(), "$deviceId/name")


        val values = SharedPreferencesManager
            .getString(getApplication(), "$deviceId/value")

        if (names != null && values != null) {
            val nameArray = names.split(",")
            val valueArray = values.split(",")

            for (i in 0 until nameArray.size - 1) {
                sensors.add(
                    SensorModel(
                        sensorName = nameArray[i],
                        sensorStatus = valueArray[i].toInt()
                    )
                )
            }
        }
        Log.i(LOG_TAG, "SIZE : ${sensors.size}")

        return sensors
    }

    fun toggleStatusAt(position: Int) {
        val newSensors: List<SensorModel>
        newSensors = sensors

        when (sensors[position].sensorStatus) {
            0 -> newSensors[position].sensorStatus = 1
            1 -> newSensors[position].sensorStatus = 0
        }

        Log.i(LOG_TAG, "SENSORS : ${sensors[position].sensorStatus}")

        _sensorList.value = newSensors
    }
}