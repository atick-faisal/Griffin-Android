package ai.andromeda.griffin.home

import ai.andromeda.griffin.config.Config
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
import ai.andromeda.griffin.util.MqttHelper
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttClient

class HomeViewModel(deviceDatabase: DeviceDatabase, application: Application) :
    AndroidViewModel(application) {

    private val client = MqttHelper.getInstance(application)

    val database = deviceDatabase.deviceDao
    val deviceList = database.getAll()

    override fun onCleared() {
        super.onCleared()
        Log.i(LOG_TAG, "CLIENT CLEARED")
        client.close()
    }
}