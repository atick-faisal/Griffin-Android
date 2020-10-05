package ai.andromeda.griffin.background

import ai.andromeda.griffin.util.makeMqttServiceRequest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MqttBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "GRIFFIN SERVICE STARTING", Toast.LENGTH_LONG).show()
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
            makeMqttServiceRequest()
        }
    }
}