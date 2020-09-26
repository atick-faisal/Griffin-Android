package ai.andromeda.griffin.register

import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.GLOBAL_BROKER_IP
import ai.andromeda.griffin.config.Config.LOCAL_BROKER_IP
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.config.Config.PUBLISH_TOPIC
import ai.andromeda.griffin.config.Config.SUBSCRIPTION_TOPIC
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
import ai.andromeda.griffin.generateDeviceId
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.device_list_item.view.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.UnsupportedEncodingException

@Suppress("SameParameterValue")
class RegisterFragment : Fragment() {

    private lateinit var deviceDatabase: DeviceDatabase
    private lateinit var client: MqttAndroidClient
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_register, container, false)
        rootView.connectionRetryButton.setOnClickListener { connectToBroker() }
        rootView.registerButton.setOnClickListener { registerDevice() }
        deviceDatabase = DeviceDatabase.getInstance(
            requireNotNull(this.activity).application
        )
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        configureMqttClient()
    }

    private fun configureMqttClient() {
        val clientId = MqttClient.generateClientId()
        client = MqttAndroidClient(
            this.activity?.applicationContext,
            LOCAL_BROKER_IP,
            clientId
        )
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
                        subscribe(SUBSCRIPTION_TOPIC)
                        subscribe("NEW_TOPIC")
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

    private fun publish(payload: String) {
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

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT)
            .show()
    }

    private fun onConnectionSuccessful() {
        rootView.noConnectionView.visibility = View.GONE
        rootView.registrationForm.visibility = View.VISIBLE
    }

    private fun registerDevice() {
        val isNameEmpty = rootView.deviceNameInput.text.isEmpty()
        val isSSIDEmpty = rootView.ssidInput.text.isEmpty()
        val isPasswordEmpty = rootView.passwordInput.text.isEmpty()
        val isContactEmpty = rootView.contact1Input.text.isEmpty()
        val isNumSensorEmpty = rootView.sensorNumberInput.text.isEmpty()

        if (
            isNameEmpty ||
            isSSIDEmpty ||
            isPasswordEmpty ||
            isContactEmpty ||
            isNumSensorEmpty
        ) {
            showMessage("REQUIRED FIELDS CAN'T BE EMPTY")
        } else {
            val deviceId = generateDeviceId()
            Log.i(LOG_TAG, "DEVICE ID : $deviceId")

            val deviceName = rootView.deviceNameInput.text.toString()
            val ssid = rootView.ssidInput.text.toString()
            val password = rootView.passwordInput.text.toString()
            val contact1 = rootView.contact1Input.text.toString()
            val contact2 = rootView.contact2Input.text.toString()
            val contact3 = rootView.contact3Input.text.toString()
            val numSensors = rootView.sensorNumberInput.text.toString()
            val additionalInfo = rootView.additionalInfoText.text.toString()

            val data = DeviceEntity(
                deviceId = deviceId,
                deviceName = deviceName,
                ssid = ssid,
                password = password,
                contact1 = contact1,
                contact2 = contact2,
                contact3 = contact3,
                numSensors = numSensors.toInt(),
                additionalInfo = additionalInfo
            )

            val payload = deviceId +
                    deviceName + "," +
                    ssid + "," +
                    password + "," +
                    contact1 + "," +
                    contact2 + "," +
                    contact3 + "," +
                    numSensors + "," +
                    additionalInfo

            publish(payload)
            saveData(data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        client.close()
    }

    private fun saveData(data: DeviceEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            deviceDatabase.deviceDao.insert(data)
        }
    }
}