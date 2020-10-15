package ai.andromeda.griffin.home

import ai.andromeda.griffin.config.Config.AUTO_START_KEY
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.util.SharedPreferencesManager
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class HomeViewModel(application: Application) :
    AndroidViewModel(application) {

    //----------------- LOAD DEVICE LIST LIVE DATA ------------------//
    val database = DeviceDatabase.getInstance(application).deviceDao
    val deviceList = database.getAll()

    private val _askAutoStart = MutableLiveData<Long>()
    val aksAutoStart: LiveData<Long>
        get() = _askAutoStart

    //-------------- INIT --------------//
    init {
        _askAutoStart.value = null
        initAskAutoStart()
    }

    //----------- INIT AUTO START ----------//
    private fun initAskAutoStart() {
        _askAutoStart.value = SharedPreferencesManager.getLong(
            getApplication(), AUTO_START_KEY
        )
    }

    //---------- ASKED FOR AUTO START PERMISSION ----------//
    fun doneAskingPermission() {
        _askAutoStart.value = null
        SharedPreferencesManager.putLong(getApplication(), AUTO_START_KEY, 1L)
    }
}