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
        //createNotificationChannel()
        //makeMqttServiceRequest()
        delayedInit()
    }

    private fun delayedInit() {
        applicationScope.launch {
            createNotificationChannel()
            makeMqttServiceRequest()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val persistentNotificationChannel = NotificationChannel(
                PERSISTENT_CHANNEL_ID,
                "MQTT SERVICE CHANNEL",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }

            val alertNotificationChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "ALERT NOTIFICATION CHANNEL",
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )

            notificationManager.createNotificationChannel(persistentNotificationChannel)
            notificationManager.createNotificationChannel(alertNotificationChannel)
        }
    }

// TODO PERIODIC WORK
//        val workRequest = PeriodicWorkRequestBuilder<MqttWorker>(
//            WORK_REPEAT_PERIOD, TimeUnit.MINUTES
//        )
//            .setConstraints(constraints)
//            .build()
//        WorkManager.getInstance().enqueueUniquePeriodicWork(
//            WORK_NAME,
//            ExistingPeriodicWorkPolicy.REPLACE,
//            workRequest
//        )

}
