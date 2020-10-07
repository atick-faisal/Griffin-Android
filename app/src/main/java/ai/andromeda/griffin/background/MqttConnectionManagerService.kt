package ai.andromeda.griffin.background

import ai.andromeda.griffin.MainActivity
import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.ALERT_CHANNEL_ID
import ai.andromeda.griffin.config.Config.ALERT_NOTIFICATION_ID
import ai.andromeda.griffin.config.Config.ALERT_NOTIFICATION_TITLE
import ai.andromeda.griffin.config.Config.DEVICE_ID_KEY
import ai.andromeda.griffin.config.Config.GLOBAL_BROKER_IP
import ai.andromeda.griffin.config.Config.LOCAL_BROKER_IP
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.config.Config.PERSISTENT_CHANNEL_ID
import ai.andromeda.griffin.config.Config.PERSISTENT_NOTIFICATION_ID
import ai.andromeda.griffin.config.Config.PERSISTENT_NOTIFICATION_TITLE
import ai.andromeda.griffin.config.Config.RESTART_REQUEST_KEY
import ai.andromeda.griffin.config.Config.SUBSCRIPTION_TOPIC
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
import ai.andromeda.griffin.util.SharedPreferencesManager
import ai.andromeda.griffin.util.makeMqttServiceRequest
import ai.andromeda.griffin.util.showMessage
import ai.andromeda.griffin.util.toArray
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

@Suppress("SameParameterValue")
class MqttConnectionManagerService : Service() {

    lateinit var client: MqttAndroidClient
    private lateinit var deviceDatabase: DeviceDatabase

    private var isConnectedLocally = false
    private var connecting = false

    // Service Binder Instance
    private val binder = LocalBinder()

    //---------------------- BINDER CLASS ------------------------//
    inner class LocalBinder : Binder() {
        // Return this instance of MqttConnectionManagerService
        fun getInstance(): MqttConnectionManagerService {
            return this@MqttConnectionManagerService
        }
    }

    //------------------------- ON_CREATE() ----------------------//
    override fun onCreate() {
        super.onCreate()

        // Only create client and database instance once when service started
        deviceDatabase = DeviceDatabase.getInstance(this.applicationContext)
        client = createMqttAndroidClient(GLOBAL_BROKER_IP)

        Log.i(LOG_TAG, "SERVICE: NEW MQTT CLIENT CREATED!")
    }

    //---------------------------- ON_START_COMMAND() -------------------------//
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Has to display notification in order to keep the service alive
        if (!client.isConnected) { // NESTED_IF : v1.1.1-beta
            showPersistentNotification(getString(R.string.device_offline_warning), false)
            if (!connecting) {
                resetClient()
                connect(client)
            }
        } else {
            showPersistentNotification(getString(R.string.device_online), true)
        }
        return START_STICKY_COMPATIBILITY
    }

    //------------------------- ON_BIND() ---------------------//
    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    //----------------------- CREATE_MQTT_CLIENT() ------------------------//
    private fun createMqttAndroidClient(brokerIp: String): MqttAndroidClient {
        val clientId = MqttClient.generateClientId()
        return MqttAndroidClient(this.applicationContext, brokerIp, clientId)
    }

    //----------------------------- CONNECT() ----------------------------//
    private fun connect(client: MqttAndroidClient) {
        try {
            if (!client.isConnected) {
                connecting = true
                val token = client.connect()

                //-------------------- CONNECTION CALLBACK -------------------//
                token.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        connecting = false
                        showMessage(applicationContext, "CONNECTED!")
                        showPersistentNotification(getString(R.string.device_online), true)
                        Log.i(LOG_TAG, "SERVICE: MQTT CONNECTED!")
                        subscribeToAllDevice()
                        subscribe(SUBSCRIPTION_TOPIC) // TODO REMOVE THIS
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken,
                        exception: Throwable
                    ) {
                        connecting = false
                        showMessage(applicationContext, "CANNOT CONNECT!")
                        Log.i(LOG_TAG, "SERVICE: CANNOT CONNECT!")
                        if (!isConnectedLocally) {
                            tryConnectingLocally()
                        }
                        // stopService()
                    }
                }

                //--------------------- MESSAGE CALLBACK --------------------//
                client.setCallback(object : MqttCallback {
                    override fun messageArrived(
                        topic: String?,
                        message: MqttMessage?
                    ) {
                        showMessage(applicationContext, message.toString())
                        message?.let { processMessage(message.toString()) }

                        //------------------SHIT HAPPENED----------------------//
                        if ("SHIT" == message.toString())
                            showAlertNotification("SH!T HAPPENED")
                        //-----------------------------------------------------//

                        Log.i(LOG_TAG, "SERVICE: MQTT MESSAGE : " + message.toString())
                    }

                    //--------------- CONNECTION LOST --------------------//
                    override fun connectionLost(cause: Throwable?) {
                        connecting = false
                        showPersistentNotification(
                            getString(R.string.device_offline_warning), false
                        )
                        showMessage(applicationContext, "CONNECTION LOST")
                        Log.i(LOG_TAG, "SERVICE: CONNECTION LOST")
                        retryConnection()
                        // stopService()
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                })
            }
        }
        // --------------------- CONNECTION ERROR --------------------//
        catch (e: MqttException) {
            connecting = false
            showMessage(applicationContext, "ERROR WHILE CONNECTING")
            Log.i(LOG_TAG, "ERROR WHILE CONNECTING")
            // stopService()
        }
    }

    //------------------------- SUBSCRIBE TO ALL -----------------------//
    private fun subscribeToAllDevice() {
        val deviceIds = SharedPreferencesManager.getString(applicationContext, DEVICE_ID_KEY)
        deviceIds?.let {
            val deviceIdList = deviceIds.split(",")
            Log.i(LOG_TAG, "SERVICE: DEVICE LIST -> $deviceIdList")
            for (deviceId in deviceIdList) {
                if (deviceId.isNotEmpty()) {
                    subscribe("Pub/$deviceId")
                }
            }
            // showMessage(applicationContext, "SERVICE: SUBSCRIBED TO ALL")
        }
    }

    //-------------------------- SUBSCRIBE() -----------------------//
    private fun subscribe(topic: String) {
        try {
            val subToken = client.subscribe(topic, 1)
            subToken.actionCallback = object : IMqttActionListener {

                //------------- SUBSCRIPTION SUCCESSFUL---------------//
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i(LOG_TAG, "SERVICE: SUBSCRIBED TO : $topic")
                }

                //------------- SUBSCRIPTION FAILED -----------//
                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    Log.i(LOG_TAG, "SERVICE: COULD NOT SUBSCRIBE")
                    showMessage(applicationContext, "COULD NOT SUBSCRIBE")
                    // stopService()
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    //------------------ PUBLISH ----------------------//
    fun publish(topic: String, payload: String) {
        try {
            if (client.isConnected) {
                val encodedPayload = payload.toByteArray(charset("UTF-8"))
                val message = MqttMessage(encodedPayload)
                client.publish(topic, message)
                showMessage(applicationContext, "COMMAND SENT")
                Log.i(LOG_TAG, "SERVICE: PUBLISH -> $payload")
            } else {
                showMessage(applicationContext, "NO CONNECTION")
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: MqttException) {
            showMessage(applicationContext, "PUBLISH FAILED")
            Log.i(LOG_TAG, "SERVICE: PUBLISH FAILED")
            e.printStackTrace()
        }
    }

    // ----------------- PROCESS MQTT MESSAGE -------------------//
    private fun processMessage(message: String) {
        try {
            val json = JSONObject(message)
            val deviceId = json.getString("Device_ID")
            val sensors = json.getString("Sensors")
            val sensorValues = toArray(sensors)
            sensorValues?.let {

                // ----------- WRITE VALUES TO SP AND DB --------------//
                writeToSharedPreferences(sensorValues, deviceId)
                updateDatabase(sensorValues, deviceId)

                //--------------------- SECURITY BREACH -------------------//
                if (sensorValues.sum() > 0) {
                    val deviceName = SharedPreferencesManager.getString(
                        applicationContext, deviceId
                    )
                    showAlertNotification(
                        getString(R.string.sensor_breach, deviceName.toString())
                    )
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    //----------------------- WRITE_TO_SP() -------------------------//
    private fun writeToSharedPreferences(sensorValues: IntArray, deviceId: String) {
        val key = "$deviceId/value"
        val sensorString = StringBuilder()
        for (element in sensorValues) {
            sensorString.append("$element,")
        }
        SharedPreferencesManager.putString(
            this.applicationContext, key, sensorString.toString()
        )
        Log.i(LOG_TAG, "SERVICE: WRITING SENSOR VALUES TO SP : $sensorString")
    }

    //------------------------- UPDATE_DATABASE() ------------------------//
    private fun updateDatabase(sensorValues: IntArray, deviceId: String) {
        val lockedSensors = sensorValues.size - sensorValues.sum()
        CoroutineScope(Dispatchers.IO).launch {
            val device = get(deviceId)
            device?.let {
                if (sensorValues.size == device.numSensors) {
                    if (lockedSensors >= 0) {
                        device.lockedSensors = lockedSensors
                        update(device)
                        Log.i(LOG_TAG, "SERVICE: WRITING SENSOR VALUES TO DB")
                    }
                }
            }
        }
    }

    // --------------- DATABASE SUSPEND METHODS ------------------//
    private suspend fun update(device: DeviceEntity) {
        deviceDatabase.deviceDao.update(device)
    }

    private suspend fun get(deviceId: String): DeviceEntity? {
        return deviceDatabase.deviceDao.get(deviceId)
    }

    //---------------------- PERSISTENT NOTIFICATION ----------------------//
    private fun showPersistentNotification(content: String, connected: Boolean) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.putExtra(RESTART_REQUEST_KEY, true)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, PERSISTENT_CHANNEL_ID)
            .setContentTitle(PERSISTENT_NOTIFICATION_TITLE)
            .setContentText(content)
            .setSmallIcon(
                when {
                    connected -> R.drawable.ic_secure
                    else -> R.drawable.ic_error
                }

            )
            .setContentIntent(pendingIntent)
            .build()

        startForeground(PERSISTENT_NOTIFICATION_ID, notification)
    }

    //----------------------- ALERT NOTIFICATION -----------------------//
    private fun showAlertNotification(content: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle(ALERT_NOTIFICATION_TITLE)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_secure)
            .setContentIntent(pendingIntent)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(ALERT_NOTIFICATION_ID, notification)
        }
    }

    //-------------------- RESET CLIENT ------------------//
    private fun resetClient() {
        isConnectedLocally = false
        if (::client.isInitialized) {
            client.close()
            client = createMqttAndroidClient(GLOBAL_BROKER_IP)
        }
    }

    //-------------------- TRY CONNECTING LOCALLY ------------------//
    private fun tryConnectingLocally() {
        Log.i(LOG_TAG, "SERVICE: CONNECTING LOCALLY")
        isConnectedLocally = true
        if (::client.isInitialized) {
            client.close()
            client = createMqttAndroidClient(LOCAL_BROKER_IP)
            this.connect(client)
        }
    }

    //-------------------- RETRY CONNECTION ---------------------//
    private fun retryConnection() {
        makeMqttServiceRequest()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.i(LOG_TAG, "SERVICE: ON LOW MEMORY")
    }

    override fun onDestroy() {
        Log.i(LOG_TAG, "SERVICE: ON DESTROY CALLED")
        super.onDestroy()
        client.close()
        retryConnection()
    }
}