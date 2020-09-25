package ai.andromeda.griffin.register

import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.LOCAL_BROKER_IP
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.config.Config.PUBLISH_TOPIC
import ai.andromeda.griffin.config.Config.SUBSCRIPTION_TOPIC
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.fragment_sign_up.view.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.UnsupportedEncodingException


@Suppress("SameParameterValue")
class RegisterFragment : Fragment() {

    private lateinit var client: MqttAndroidClient
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_register, container, false)
        rootView.connectionRetryButton.setOnClickListener { connectToBroker() }
        rootView.registerButton.setOnClickListener { registerDevice() }
        return rootView
    }

    private fun connectToBroker() {
        val clientId = MqttClient.generateClientId()
        client = MqttAndroidClient(
            this.activity?.applicationContext,
            LOCAL_BROKER_IP,
            clientId
        )
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
            val payload = rootView.deviceNameInput.text.toString() + "," +
                    rootView.ssidInput.text.toString() + "," +
                    rootView.passwordInput.text.toString() + "," +
                    rootView.contact1Input.text.toString() + "," +
                    rootView.contact2Input.text.toString() + "," +
                    rootView.contact3Input.text.toString() + "," +
                    rootView.additionalInfoText.text.toString()

            publish(payload)
        }
    }
}