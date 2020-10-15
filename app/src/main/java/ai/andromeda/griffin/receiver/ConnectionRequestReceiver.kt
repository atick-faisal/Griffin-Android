package ai.andromeda.griffin.receiver

import ai.andromeda.griffin.background.MqttConnectionManagerService
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.util.isWiFiConnected
import ai.andromeda.griffin.util.showMessage
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

//---------- USEFUL IN CASE OF LOCAL ESP SERVER WITH NO INTERNET ACCESS ----------//
class ConnectionRequestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (isWiFiConnected(context)) {
            Log.i(LOG_TAG, "CRR: TRYING TO RECONNECT")
            val serviceIntent = Intent(context, MqttConnectionManagerService::class.java)
            context.startService(serviceIntent)
        }
        else {
            showMessage(context, "NO CONNECTION")
        }
    }
}