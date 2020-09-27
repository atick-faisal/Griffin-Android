package ai.andromeda.griffin.home

import ai.andromeda.griffin.database.DeviceDatabase
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HomeViewModelFactory(
    private val database: DeviceDatabase,
    private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(database, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}