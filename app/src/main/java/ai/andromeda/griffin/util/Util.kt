package ai.andromeda.griffin.util

import ai.andromeda.griffin.background.MqttWorker
import ai.andromeda.griffin.config.Config.ALLOWED_CHARACTERS
import ai.andromeda.griffin.config.Config.ID_LENGTH
import android.content.Context
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
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
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val workRequest = OneTimeWorkRequestBuilder<MqttWorker>()
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance().enqueue(workRequest)
}