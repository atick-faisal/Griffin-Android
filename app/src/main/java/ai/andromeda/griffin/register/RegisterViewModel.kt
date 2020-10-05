package ai.andromeda.griffin.register

import ai.andromeda.griffin.config.Config
import ai.andromeda.griffin.config.Config.DEVICE_ID_KEY
import ai.andromeda.griffin.config.Config.LOCAL_BROKER_IP
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.config.Config.PUBLISH_TOPIC
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
import ai.andromeda.griffin.util.SharedPreferencesManager
import ai.andromeda.griffin.util.showMessage
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class RegisterViewModel(application: Application) :
    AndroidViewModel(application) {

    private val database = DeviceDatabase.getInstance(application).deviceDao
    private lateinit var client: MqttAndroidClient

    //--------------------- LIVE DATA --------------------//
    private val _connectionSuccessful = MutableLiveData<Boolean>()
    val connectionSuccessful: LiveData<Boolean>
        get() = _connectionSuccessful

    //--------------------- COUNTER -----------------------//
    private var count = SharedPreferencesManager.getLong(
        application, "REG_COUNT"
    )

    init {
        _connectionSuccessful.value = null
    }

    //----------------------- MQTT CONNECT -------------------//
    fun connectToBroker() {
        val clientId: String = MqttClient.generateClientId()
        client = MqttAndroidClient(getApplication(), LOCAL_BROKER_IP, clientId)
        try {
            if (!client.isConnected) {
                val token = client.connect()

                //-------------------- CONNECTION CALLBACK -------------------//
                token.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        showMessage(getApplication(), "CONNECTED!")
                        Log.i(LOG_TAG, "REGISTER_VM: MQTT CONNECTED!")
                        // subscribe(SUBSCRIPTION_TOPIC) // TODO SUBSCRIBE FOR CALLBACKS
                        onConnectionSuccessful()
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken,
                        exception: Throwable
                    ) {
                        showMessage(getApplication(), "CANNOT CONNECT!")
                        Log.i(LOG_TAG, "REGISTER_VM: CANNOT CONNECT!")
                    }
                }
                //-------------------- CLIENT CALLBACKS -------------------//
                client.setCallback(object : MqttCallback {
                    override fun messageArrived(
                        topic: String?, message: MqttMessage?
                    ) {
                        // TODO CHECK FOR SUCCESSFUL REGISTRATION
                        Log.i(LOG_TAG, "REGISTER_VM: MESSAGE : " + message.toString())
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.i(LOG_TAG, "CONNECTION LOST")
                        showMessage(getApplication(), "CONNECTION LOST")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                })
            }

            //----------- ERROR HANDLING ---------//
        } catch (e: MqttException) {
            showMessage(getApplication(), "CONNECTION ERROR")
            Log.i(LOG_TAG, "REGISTER_VM: ERROR WHILE CONNECTING")
        }
    }

    //---------------------- SUBSCRIBE -------------------//
//    private fun subscribe(topic: String) {
//        try {
//            val subToken = client.subscribe(topic, 1)
//            subToken.actionCallback = object : IMqttActionListener {
//                override fun onSuccess(asyncActionToken: IMqttToken) {
//                    Log.i(LOG_TAG, "SUBSCRIBED TO : $topic")
//                    showMessage("SUBSCRIBED TO : $topic")
//                }
//
//                override fun onFailure(
//                    asyncActionToken: IMqttToken,
//                    exception: Throwable
//                ) {
//                    Log.i(LOG_TAG, "COULD NOT SUBSCRIBE")
//                    showMessage("COULD NOT SUBSCRIBE")
//                }
//            }
//        } catch (e: MqttException) {
//            e.printStackTrace()
//        }
//    }

    //------------------- PUBLISH DATA -------------------//
    fun publish(data: DeviceEntity) {
        val payload = getJsonObject(data)
        try {
            if (::client.isInitialized) {
                if (client.isConnected) {
                    val encodedPayload = payload.toByteArray(charset("UTF-8"))
                    val message = MqttMessage(encodedPayload)
                    client.publish(PUBLISH_TOPIC, message)
                    showMessage(getApplication(), "REGISTERED")
                    Log.i(LOG_TAG, "REGISTER_VM: PUBLISH -> $payload")
                }
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    //--------------------- CREATE JSON FORMATTED DATA -----------------//
    private fun getJsonObject(data: DeviceEntity): String {
        val payload = JSONObject()
        try {
            payload.put("Device_ID", data.deviceId)
            payload.put("Count", ++count)
            payload.put("Command", "Configuration")
            payload.put("SSID", data.ssid)
            payload.put("Password", data.password)
            payload.put("Number_of_Sensors", data.numSensors)
            payload.put("Number_of_Contacts", 3)
            payload.put("Contact_1", data.contact1)
            payload.put("Contact_2", data.contact2)
            payload.put("Contact_3", data.contact3)
            payload.put("Message", data.customMessage)
        } catch (e: JSONException) {
            Log.i(LOG_TAG, "REGISTER_VM: JSON ERROR")
            e.printStackTrace()
        }
        return payload.toString()
    }

    //----------------- CONNECTION CALLBACKS ---------------//
    private fun onConnectionSuccessful() {
        _connectionSuccessful.value = true
    }

    fun doneShowingViews() {
        _connectionSuccessful.value = null
    }

    //-------------------- DATABASE OPERATIONS -----------//
    fun saveData(data: DeviceEntity) {
        writeToSharedPreferences(data)
        CoroutineScope(Dispatchers.IO).launch {
            database.insert(data)
        }
        Log.i(LOG_TAG, "REGISTER_VM: WRITING TO DB")
    }

    //----------------- WRITING TO SP -----------------//
    private fun writeToSharedPreferences(data: DeviceEntity) {
        val deviceId = data.deviceId
        val deviceName = data.deviceName
        val nameKey = "$deviceId/name"
        val valueKey = "$deviceId/value"
        val names = StringBuilder()
        val values = StringBuilder()

        val n = data.numSensors
        for (i in 0 until n) {
            names.append("Sensor ${i + 1},")
            values.append("0,")
        }
        SharedPreferencesManager.putString(getApplication(), nameKey, names.toString())
        SharedPreferencesManager.putString(getApplication(), valueKey, values.toString())

        // Saving Device Name to Show in Future Alert Notifications
        deviceId?.let {
            SharedPreferencesManager.putString(
                getApplication(), deviceId, deviceName.toString()
            )
        }

        //------------------------- SAVE DEVICE ID ------------------------//
        var allId = SharedPreferencesManager.getString(getApplication(), DEVICE_ID_KEY)
        allId = if (allId != null) {
            "$allId,$deviceId"
        } else {
            "$deviceId"
        }
        SharedPreferencesManager.putString(getApplication(), DEVICE_ID_KEY, allId)

        Log.i(LOG_TAG, "REGISTER_VM: WRITING TO SP")
    }

    //------------------------ SAVE COUNT -----------------------//
    private fun saveCount() {
        SharedPreferencesManager.putLong(getApplication(), "REG_COUNT", count)
    }

    //---------------- ON_CLEARED() -------------//
    override fun onCleared() {
        super.onCleared()
        saveCount()
        client.close()
        Log.i(LOG_TAG, "REGISTER_VM: CLIENT CLEARED")
    }
}