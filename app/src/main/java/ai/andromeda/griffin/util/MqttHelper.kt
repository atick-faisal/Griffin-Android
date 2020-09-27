package ai.andromeda.griffin.util

import ai.andromeda.griffin.config.Config
import android.app.Application
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttClient

object MqttHelper {
    private val clientId: String = MqttClient.generateClientId()
    private lateinit var INSTANCE: MqttAndroidClient
    fun getInstance(application: Application): MqttAndroidClient {
        synchronized(this) {
            if (! ::INSTANCE.isInitialized) {
                INSTANCE = MqttAndroidClient(
                    application,
                    Config.LOCAL_BROKER_IP,
                    clientId
                )
            }
        }
        return INSTANCE
    }
}