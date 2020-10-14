package ai.andromeda.griffin.receiver

import ai.andromeda.griffin.util.makeMqttServiceRequest
import ai.andromeda.griffin.util.showMessage
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        showMessage(context, "BOOT COMPLETE")
        if (
            intent.action == Intent.ACTION_BOOT_COMPLETED
            || intent.action == Intent.ACTION_SHUTDOWN
        )
            makeMqttServiceRequest()
    }
}