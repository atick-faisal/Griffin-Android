package ai.andromeda.griffin.reconfigure

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ReconfigureViewModelFactory(
    private val application: Application,
    private val deviceId: String
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReconfigureViewModel::class.java)) {
            return ReconfigureViewModel(application, deviceId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}