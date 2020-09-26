package ai.andromeda.griffin.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "device_id")
    val deviceId: String?,
    @ColumnInfo(name = "device_name")
    val deviceName: String?,
    @ColumnInfo(name = "ssid")
    val ssid: String?,
    @ColumnInfo(name = "password")
    val password: String?,
    @ColumnInfo(name = "contact_1")
    val contact1: String?,
    @ColumnInfo(name = "contact_2")
    val contact2: String = "",
    @ColumnInfo(name = "contact_3")
    val contact3: String = "",
    @ColumnInfo(name = "num_sensors")
    val numSensors: Int?,
    @ColumnInfo(name = "additional_info")
    val additionalInfo: String = "",
    @ColumnInfo(name = "locked_sensors")
    val lockedSensors: Int = 0
)