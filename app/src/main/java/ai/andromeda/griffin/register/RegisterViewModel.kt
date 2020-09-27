package ai.andromeda.griffin.register

import ai.andromeda.griffin.config.Config
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.UnsupportedEncodingException
import java.lang.StringBuilder

class RegisterViewModel(deviceDatabase: DeviceDatabase, application: Application) :
    AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences(
        "SENSORS", Context.MODE_PRIVATE
    )
    private val database = deviceDatabase.deviceDao
    private val clientId: String = MqttClient.generateClientId()
    private val client = MqttAndroidClient(
        application,
        Config.LOCAL_BROKER_IP,
        clientId
    )

    private val _connectionSuccessful = MutableLiveData<Boolean>()
    val connectionSuccessful: LiveData<Boolean>
        get() = _connectionSuccessful

    init {
        _connectionSuccessful.value = null
        connectToBroker()
    }

    fun connectToBroker() {
        try {
            if (!client.isConnected) {
                val token = client.connect()
                ////////////////////////////////////////////////////////////
                token.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        showMessage("MQTT CONNECTED!")
                        Log.i(Config.LOG_TAG, "MQTT CONNECTED!")
                        onConnectionSuccessful()
                        subscribe(Config.SUBSCRIPTION_TOPIC)
                        subscribe("NEW_TOPIC")
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken,
                        exception: Throwable
                    ) {
                        showMessage("CANNOT CONNECT!")
                        Log.i(Config.LOG_TAG, "CANNOT CONNECT!")
                    }
                }
                ////////////////////////////////////////////////////////////
                client.setCallback(object : MqttCallback {
                    override fun messageArrived(
                        topic: String?,
                        message: MqttMessage?
                    ) {
                        Log.i(Config.LOG_TAG, "MESSAGE : " + message.toString())
                        showMessage(message.toString())
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.i(Config.LOG_TAG, "CONNECTION LOST")
                        showMessage("CONNECTION LOST")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                })
            }
        } catch (e: MqttException) {
            showMessage("ERROR WHILE CONNECTING")
            Log.i(Config.LOG_TAG, "ERROR WHILE CONNECTING")
        }
    }

    private fun subscribe(topic: String) {
        try {
            val subToken = client.subscribe(topic, 1)
            subToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i(Config.LOG_TAG, "SUBSCRIBED TO : $topic")
                    showMessage("SUBSCRIBED TO : $topic")
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    Log.i(Config.LOG_TAG, "COULD NOT SUBSCRIBE")
                    showMessage("COULD NOT SUBSCRIBE")
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(payload: String) {
        try {
            val encodedPayload = payload.toByteArray(charset("UTF-8"))
            val message = MqttMessage(encodedPayload)
            client.publish(Config.PUBLISH_TOPIC, message)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
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

    fun saveData(data: DeviceEntity) {
        writeToSharedPreferences(data)
        CoroutineScope(Dispatchers.IO).launch {
            database.insert(data)
        }
    }

    private fun writeToSharedPreferences(data: DeviceEntity) {
        with(sharedPreferences.edit()) {
            val nameKey = data.deviceId + "/name"
            val valueKey = data.deviceId + "/value"
            val names = StringBuilder()
            val values = StringBuilder()
            val n = data.numSensors
            for (i in 0..n) {
                names.append("SENSOR $i,")
                values.append("0,")
            }
            putString(nameKey, names.toString())
            putString(valueKey, values.toString())
            commit()
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}