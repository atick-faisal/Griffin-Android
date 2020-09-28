package ai.andromeda.griffin.share

import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.home.HomeViewModel
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ShareViewModelFactory(
    private val database: DeviceDatabase,
    private val application: Application,
    private val deviceId: String
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShareViewModel::class.java)) {
            return ShareViewModel(database, application, deviceId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}