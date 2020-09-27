package ai.andromeda.griffin.device

import ai.andromeda.griffin.database.DeviceDatabase
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DeviceViewModelFactory(
    private val application: Application,
    private val deviceId: String?
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceViewModel::class.java)) {
            return DeviceViewModel(application, deviceId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}