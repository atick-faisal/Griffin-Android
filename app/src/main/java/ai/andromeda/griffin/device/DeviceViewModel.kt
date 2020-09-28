package ai.andromeda.griffin.device

import ai.andromeda.griffin.config.Config
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.config.Config.PUBLISH_TOPIC
import ai.andromeda.griffin.database.SensorModel
import ai.andromeda.griffin.util.SharedPreferencesManager
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import kotlin.properties.Delegates

class DeviceViewModel(
    application: Application,
    val deviceId: String?
) : AndroidViewModel(application) {

    private val clientId: String = MqttClient.generateClientId()
    private val client = MqttAndroidClient(
        application,
        Config.LOCAL_BROKER_IP,
        clientId
    )
    private val sensors: MutableList<SensorModel> = mutableListOf()
    private val _sensorList = MutableLiveData<List<SensorModel>>()
    val sensorList: LiveData<List<SensorModel>>
        get() = _sensorList

    private val _connectionSuccessful = MutableLiveData<Boolean>()
    val connectionSuccessful: LiveData<Boolean>
        get() = _connectionSuccessful

    private var numberOfSensors by Delegates.notNull<Int>()

    init {
        _sensorList.value = getSensorList()
        connectToBroker()
    }

    private fun getSensorList(): List<SensorModel> {
        Log.i(LOG_TAG, "INIT CALLED")
        val names = SharedPreferencesManager
            .getString(getApplication(), "$deviceId/name")


        val values = SharedPreferencesManager
            .getString(getApplication(), "$deviceId/value")

        if (names != null && values != null) {
            val nameArray = names.split(",")
            val valueArray = values.split(",")

            numberOfSensors = nameArray.size - 1

            for (i in 0 until numberOfSensors) {
                sensors.add(
                    SensorModel(
                        sensorName = nameArray[i],
                        sensorStatus = valueArray[i].toInt()
                    )
                )
            }
        }
        Log.i(LOG_TAG, "SIZE : ${sensors.size}")

        return sensors
    }

    fun toggleStatusAt(position: Int) {
        when (sensors[position].sensorStatus) {
            0 -> sensors[position].sensorStatus = 1
            1 -> sensors[position].sensorStatus = 0
        }

        Log.i(LOG_TAG, "SENSORS : ${sensors[position].sensorStatus}")

        publish()
        writeToSharedPreferences()
        _sensorList.value = sensors
    }

    private fun connectToBroker() {
        try {
            if (!client.isConnected) {
                val token = client.connect()
                ////////////////////////////////////////////////////////////
                token.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        showMessage("MQTT CONNECTED!")
                        Log.i(LOG_TAG, "MQTT CONNECTED!")
                        onConnectionSuccessful()
                        subscribe(Config.SUBSCRIPTION_TOPIC)
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken,
                        exception: Throwable
                    ) {
                        showMessage("CANNOT CONNECT!")
                        Log.i(LOG_TAG, "CANNOT CONNECT!")
                    }
                }
                ////////////////////////////////////////////////////////////
                client.setCallback(object : MqttCallback {
                    override fun messageArrived(
                        topic: String?,
                        message: MqttMessage?
                    ) {
                        Log.i(LOG_TAG, "MESSAGE : " + message.toString())
                        showMessage(message.toString())
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.i(LOG_TAG, "CONNECTION LOST")
                        showMessage("CONNECTION LOST")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                })
            }
        } catch (e: MqttException) {
            showMessage("ERROR WHILE CONNECTING")
            Log.i(LOG_TAG, "ERROR WHILE CONNECTING")
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
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish() {
        val payload = getPayload()
        try {
            val encodedPayload = payload.toByteArray(charset("UTF-8"))
            val message = MqttMessage(encodedPayload)
            client.publish(PUBLISH_TOPIC, message)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun getPayload(): String {
        val payload = JSONObject()
        try {
            payload.put("Device_ID", deviceId)
            payload.put("Count", 0)
            payload.put("Command", "Control")
            payload.put("Number_of_Sensors", numberOfSensors)
            payload.put("Sensors", sensors.map {
                    sensorModel -> sensorModel.sensorStatus
            })
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return payload.toString()
    }

    private fun showMessage(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT)
            .show()
    }

    private fun onConnectionSuccessful() {
        _connectionSuccessful.value = true
    }

    fun doneShowingViews() {
        _connectionSuccessful.value = null
    }

    private fun writeToSharedPreferences() {
        val nameKey = "$deviceId/name"
        val valueKey = "$deviceId/value"
        val names = StringBuilder()
        val values = StringBuilder()

        for (i in 0 until numberOfSensors) {
            names.append("${sensors[i].sensorName},")
            values.append("${sensors[i].sensorStatus},")
        }
        SharedPreferencesManager.putString(getApplication(), nameKey, names.toString())
        SharedPreferencesManager.putString(getApplication(), valueKey, values.toString())
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
        Log.i(LOG_TAG, "CLIENT CLEARED")
    }
}