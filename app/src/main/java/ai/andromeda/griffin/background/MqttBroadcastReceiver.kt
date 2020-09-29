package ai.andromeda.griffin.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MqttBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "GRIFFIN SERVICE STARTING", Toast.LENGTH_LONG).show()
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
            val serviceIntent = Intent(
                context, MqttConnectionManagerService::class.java
            )
            context?.startService(serviceIntent)
        }
    }
}