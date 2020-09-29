package ai.andromeda.griffin.background

import ai.andromeda.griffin.config.Config.LOG_TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class MqttWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        Log.i(LOG_TAG, "WORK REQUESTED!")
        startMqttService()
        return Result.success()
    }

    private fun startMqttService() {
        val intent = Intent(
            context, MqttConnectionManagerService::class.java
        )
        context.startService(intent)
    }
}