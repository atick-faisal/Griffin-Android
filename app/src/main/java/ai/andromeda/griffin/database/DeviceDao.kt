package ai.andromeda.griffin.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Suppress("UNUSED")
@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DeviceEntity)

    @Update
    suspend fun update(entity: DeviceEntity)

    @Delete
    suspend fun delete(entity: DeviceEntity)

    @Query("SELECT * FROM `devices`")
    fun getAll(): LiveData<List<DeviceEntity>>

    @Query("SELECT * FROM `devices` WHERE device_id = :deviceId LIMIT 1")
    suspend fun get(deviceId: String): DeviceEntity?
}