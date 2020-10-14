package ai.andromeda.griffin.util

import ai.andromeda.griffin.background.MqttWorker
import ai.andromeda.griffin.config.Config.ALLOWED_CHARACTERS
import ai.andromeda.griffin.config.Config.ID_LENGTH
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.config.Config.WORK_TAG
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.work.*
import com.google.gson.Gson
import java.util.*


fun generateDeviceId(): String {
    val random = Random()
    val sb = StringBuilder(ID_LENGTH)
    for (i in 0 until ID_LENGTH)
        sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
    return sb.toString()
}

fun showMessage(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun toArray(json: String): IntArray? {
    val arrayParser = Gson()
    return arrayParser.fromJson(json, IntArray::class.java)
}

fun makeMqttServiceRequest() {
    Log.i(LOG_TAG, "UTIL: MQTT SERVICE REQUESTED")
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val workRequest = OneTimeWorkRequestBuilder<MqttWorker>()
        .setConstraints(constraints)
        .build()

    val workManager = WorkManager.getInstance()

    workManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.KEEP, workRequest)
}

fun isWiFiConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
    val activeNetwork =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return activeNetwork?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
}