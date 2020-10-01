package ai.andromeda.griffin.home

import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class HomeViewModel(application: Application) :
    AndroidViewModel(application) {

    //----------------- LOAD DEVICE LIST LIVE DATA ------------------//
    val database = DeviceDatabase.getInstance(application).deviceDao
    val deviceList = database.getAll()
}