package ai.andromeda.griffin.home

import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class HomeViewModel(deviceDatabase: DeviceDatabase, application: Application) :
    AndroidViewModel(application) {

    val database = deviceDatabase.deviceDao
    val deviceList = database.getAll()
}