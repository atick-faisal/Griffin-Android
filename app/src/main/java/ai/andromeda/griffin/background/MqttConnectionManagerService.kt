package ai.andromeda.griffin.background

import ai.andromeda.griffin.MainActivity
import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.ALERT_NOTIFICATION_ID
import ai.andromeda.griffin.config.Config.ALERT_NOTIFICATION_TITLE
import ai.andromeda.griffin.config.Config.CHANNEL_ID
import ai.andromeda.griffin.config.Config.LOCAL_BROKER_IP
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.config.Config.PERSISTENT_NOTIFICATION_ID
import ai.andromeda.griffin.config.Config.PERSISTENT_NOTIFICATION_TITLE
import ai.andromeda.griffin.config.Config.SUBSCRIPTION_TOPIC
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
import ai.andromeda.griffin.util.SharedPreferencesManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONException
import org.json.JSONObject

@Suppress("SameParameterValue")
class MqttConnectionManagerService : Service() {

    private lateinit var arrayParser: Gson
    private lateinit var client: MqttAndroidClient
    private lateinit var deviceDatabase: DeviceDatabase

    override fun onCreate() {
        super.onCreate()
        arrayParser = Gson()
        client = createMqttAndroidClient(LOCAL_BROKER_IP)
        deviceDatabase = DeviceDatabase.getInstance(this.applicationContext)
        Log.i(LOG_TAG, "CLIENT CREATED!")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.connect(client)
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun createMqttAndroidClient(brokerIp: String): MqttAndroidClient {
        val clientId = MqttClient.generateClientId()
        return MqttAndroidClient(this.applicationContext, brokerIp, clientId)
    }

    private fun connect(client: MqttAndroidClient) {
        try {
            if (!client.isConnected) {
                val token = client.connect()
                ////////////////////////////////////////////////////////////
                token.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        showMessage("MQTT CONNECTED!")
                        Log.i(LOG_TAG, "MQTT CONNECTED!")
                        showPersistentNotification("MQTT Service is Running")
                        subscribe(SUBSCRIPTION_TOPIC)
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken,
                        exception: Throwable
                    ) {
                        showMessage("CANNOT CONNECT!")
                        Log.i(LOG_TAG, "CANNOT CONNECT!")
                        stopService()
                    }
                }
                ////////////////////////////////////////////////////////////
                client.setCallback(object : MqttCallback {
                    override fun messageArrived(topic: String?,
                                                message: MqttMessage?
                    ) {
                        Log.i(LOG_TAG, "MQTT MESSAGE : " + message.toString())
                        showMessage(message.toString())
                        message?.let { processMessage(message.toString()) }
                        //----------------------------------------//
                        if ("SHIT" == message.toString())
                            showAlertNotification("Shit Happened!")
                        //----------------------------------------//
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.i(LOG_TAG, "CONNECTION LOST")
                        showMessage("CONNECTION LOST")
                        stopService()
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) { }
                })
            }
        } catch (e: MqttException) {
            showMessage("ERROR WHILE CONNECTING")
            Log.i(LOG_TAG, "ERROR WHILE CONNECTING")
            stopService()
        }
    }

    private fun subscribe(topic: String) {
        try {
            val subToken = client.subscribe(topic, 1)
            subToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i(LOG_TAG, "SUBSCRIBED TO : $topic")
                    showMessage("SUBSCRIBED TO : $topic")
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    Log.i(LOG_TAG, "COULD NOT SUBSCRIBE")
                    showMessage("COULD NOT SUBSCRIBE")
                    stopService()
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
            .show()
    }

    private fun showPersistentNotification(content: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(PERSISTENT_NOTIFICATION_TITLE)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_secure)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(PERSISTENT_NOTIFICATION_ID, notification)
    }

    private fun showAlertNotification(content: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(ALERT_NOTIFICATION_TITLE)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_secure)
            .setContentIntent(pendingIntent)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(ALERT_NOTIFICATION_ID, notification)
        }
    }

    private fun writeToSharedPreferences(sensorValues: IntArray, deviceId: String) {
        val key = "$deviceId/value"
        val sensorString = StringBuilder()
        for (element in sensorValues) {
            sensorString.append("$element,")
        }
        SharedPreferencesManager.putString(
            this.applicationContext, key, sensorString.toString()
        )
        Log.i(LOG_TAG, "WRITING SENSOR VALUES : $sensorString")
    }

    private fun updateDatabase(sensorValues: IntArray, deviceId: String) {
        val lockedSensors = sensorValues.size - sensorValues.sum()
        CoroutineScope(Dispatchers.IO).launch {
            val device = get(deviceId)
            device?.let {
                device.lockedSensors = lockedSensors
                update(device)
            }
        }
    }

    private suspend fun update(device: DeviceEntity) {
        deviceDatabase.deviceDao.update(device)
    }

    private suspend fun get(deviceId: String): DeviceEntity? {
        return deviceDatabase.deviceDao.get(deviceId)
    }

    private fun processMessage(message: String) {
        try {
            val json = JSONObject(message)
            val deviceId = json.getString("Device_ID")
            val sensors = json.getString("Sensors")
            val sensorValues = toArray(sensors)
            sensorValues?.let {
                if (sensorValues.sum() > 0) {
                    showAlertNotification(getString(R.string.sensor_breach))
                }
                writeToSharedPreferences(sensorValues, deviceId)
                updateDatabase(sensorValues, deviceId)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun toArray(json: String): IntArray? {
        return arrayParser.fromJson(json, IntArray::class.java)
    }

    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }
}