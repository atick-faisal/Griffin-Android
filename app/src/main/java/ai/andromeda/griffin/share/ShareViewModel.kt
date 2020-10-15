package ai.andromeda.griffin.share

import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
import ai.andromeda.griffin.util.SharedPreferencesManager
import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.zxing.WriterException
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class ShareViewModel(
    application: Application,
    val deviceId: String
) : AndroidViewModel(application) {

    val database = DeviceDatabase.getInstance(application).deviceDao

    //-------------------- LIVE DATA ---------------------//
    private val _device = MutableLiveData<DeviceEntity>()
    val device: LiveData<DeviceEntity>
        get() = _device

    private val _qrBitmap = MutableLiveData<Bitmap>()
    val qrBitmap: LiveData<Bitmap>
        get() = _qrBitmap

    //------------------- SENSORS ---------------------//
    private val sensorNames = SharedPreferencesManager.getString(
        getApplication(), "$deviceId/name"
    )
    private val sensorValues = SharedPreferencesManager.getString(
        getApplication(), "$deviceId/value"
    )

    //--------------- INIT -----------------//
    init {
        getDevice()
    }

    //----------------- GENERATE BITMAP ---------------//
    fun generateBitmap(mDevice: DeviceEntity) {
        val data = getStringData(mDevice)

        val qrgEncoder =
            QRGEncoder(data, null, QRGContents.Type.TEXT, 500)
        try {
            _qrBitmap.value = qrgEncoder.encodeAsBitmap()
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    //------------------ JSON TO STRING -------------------//
    private fun getStringData(mDevice: DeviceEntity): String {
        val json = getJSONObject(mDevice)
        return json.toString()
    }

    //----------------- JSON OBJECT OF THE DEVICE ----------------//
    private fun getJSONObject(device: DeviceEntity): JSONObject {
        val json = JSONObject()
        try {
            json.put("deviceId", device.deviceId)
            json.put("deviceName", device.deviceName)
            json.put("ssid", device.ssid)
            json.put("password", device.password)
            json.put("contact1", device.contact1)
            json.put("contact2", device.contact2)
            json.put("contact3", device.contact3)
            json.put("numSensors", device.numSensors)
            json.put("customMessage", device.customMessage)
            json.put("lockedSensors", device.lockedSensors)
            json.put("sensorNames", sensorNames)
            json.put("sensorValues", sensorValues)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.i(LOG_TAG, "SHARE_VM: DEVICE JSON : $json")
        return json
    }

    //------------------- DATABASE OPERATIONS -------------------//
    private fun getDevice() {
        viewModelScope.launch {
            _device.value = getDeviceFromDatabase()
        }
    }

    private suspend fun getDeviceFromDatabase(): DeviceEntity? {
        return database.get(deviceId = deviceId)
    }
}