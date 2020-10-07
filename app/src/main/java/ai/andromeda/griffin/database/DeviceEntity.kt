package ai.andromeda.griffin.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey
    val deviceId: String,
    @ColumnInfo(name = "device_name")
    var deviceName: String?,
    @ColumnInfo(name = "ssid")
    var ssid: String?,
    @ColumnInfo(name = "password")
    var password: String?,
    @ColumnInfo(name = "contact_1")
    var contact1: String?,
    @ColumnInfo(name = "contact_2")
    var contact2: String = "",
    @ColumnInfo(name = "contact_3")
    var contact3: String = "",
    @ColumnInfo(name = "num_sensors")
    var numSensors: Int = 0,
    @ColumnInfo(name = "additional_info")
    var customMessage: String = "",
    @ColumnInfo(name = "locked_sensors")
    var lockedSensors: Int = 0
)