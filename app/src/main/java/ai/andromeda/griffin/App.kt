package ai.andromeda.griffin

import ai.andromeda.griffin.config.Config.ALERT_CHANNEL_ID
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.config.Config.PERSISTENT_CHANNEL_ID
import ai.andromeda.griffin.util.makeMqttServiceRequest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        Log.i(LOG_TAG, "APP: ON_CREATE() CALLED")
        delayedInit()
    }

    //------------ COROUTINE ---------------//
    private fun delayedInit() {
        applicationScope.launch {
            createNotificationChannel()
            makeMqttServiceRequest()
        }
    }

    //------------------- NOTIFICATION CHANNELS -------------------//
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //--------------- PERSISTENT CHANNEL ---------------//
            val persistentNotificationChannel = NotificationChannel(
                PERSISTENT_CHANNEL_ID,
                "MQTT SERVICE CHANNEL",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                description = getString(R.string.persistent_channel_description)
            }

            //------------------ ALERT CHANNEL -----------------//
            val alertNotificationChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "ALERT NOTIFICATION CHANNEL",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
                description = getString(R.string.alert_channel_description)
            }

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )

            notificationManager.createNotificationChannel(persistentNotificationChannel)
            notificationManager.createNotificationChannel(alertNotificationChannel)
        }
    }
}
