package ai.andromeda.griffin.share

import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import ai.andromeda.griffin.database.DeviceEntity
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
    deviceDatabase: DeviceDatabase,
    application: Application,
    val deviceId: String
) : AndroidViewModel(application) {

    val database = deviceDatabase.deviceDao
    
    private val _device = MutableLiveData<DeviceEntity>()
    val device: LiveData<DeviceEntity>
        get() = _device
    
    private val _qrBitmap = MutableLiveData<Bitmap>()
    val qrBitmap: LiveData<Bitmap>
        get() = _qrBitmap

    init {
        getDevice()
    }

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
    
    private fun getDevice() {
        viewModelScope.launch {
            _device.value = getDeviceFromDatabase()
        }
    }

    private fun getStringData(mDevice: DeviceEntity): String {
        val json = getJSONObject(mDevice)
        return json.toString()
    }

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
            json.put("additionalInfo", device.additionalInfo)
            json.put("lockedSensors", device.lockedSensors)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.i(LOG_TAG, "JSON : ${json.toString()}")
        return json
    }

    private suspend fun getDeviceFromDatabase(): DeviceEntity? {
        return database.get(deviceId = deviceId)
    }
}