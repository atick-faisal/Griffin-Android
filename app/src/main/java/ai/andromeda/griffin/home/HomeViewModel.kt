package ai.andromeda.griffin.home

import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel

class HomeViewModel(deviceDatabase: DeviceDatabase, application: Application) :
    AndroidViewModel(application) {

    val database = deviceDatabase.deviceDao
    val deviceList = database.getAll()

    override fun onCleared() {
        super.onCleared()
        Log.i(LOG_TAG, "HOME_VM: HOME VIEW MODEL CLEARED")
    }
}