package ai.andromeda.griffin

import ai.andromeda.griffin.background.MqttWorker
import ai.andromeda.griffin.config.Config.ALERT_CHANNEL_ID
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.config.Config.PERSISTENT_CHANNEL_ID
import ai.andromeda.griffin.config.Config.WORK_NAME
import ai.andromeda.griffin.config.Config.WORK_REPEAT_PERIOD
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class App : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        delayedInit()
    }

    private fun delayedInit() {
        applicationScope.launch {
            createNotificationChannel()
            setupMqttService()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val persistentNotificationChannel = NotificationChannel(
                PERSISTENT_CHANNEL_ID,
                "MQTT SERVICE CHANNEL",
                NotificationManager.IMPORTANCE_LOW
            )

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

    private fun setupMqttService() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

//        val workRequest = PeriodicWorkRequestBuilder<MqttWorker>(
//            WORK_REPEAT_PERIOD, TimeUnit.MINUTES
//        )
//            .setConstraints(constraints)
//            .build() // TODO PERIODIC WORK

        val workRequest = OneTimeWorkRequestBuilder<MqttWorker>()
            .setConstraints(constraints)
            .build()

//        WorkManager.getInstance().enqueueUniquePeriodicWork(
//            WORK_NAME,
//            ExistingPeriodicWorkPolicy.REPLACE,
//            workRequest
//        )

        WorkManager.getInstance().enqueue(workRequest)
    }
}
