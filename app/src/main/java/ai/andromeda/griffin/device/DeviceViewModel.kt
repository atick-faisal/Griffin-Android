package ai.andromeda.griffin.device

import ai.andromeda.griffin.SharedPreferencesManager
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.SensorModel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DeviceViewModel(
    application: Application,
    val deviceId: String?
) : AndroidViewModel(application) {

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

        val sensors: MutableList<SensorModel> = mutableListOf()

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
}