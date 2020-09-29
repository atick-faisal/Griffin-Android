package ai.andromeda.griffin.scanner

import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.device.DeviceViewModel
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ScannerViewModelFactory(
    private val application: Application,
    private val deviceDatabase: DeviceDatabase
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
            return ScannerViewModel(application, deviceDatabase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}