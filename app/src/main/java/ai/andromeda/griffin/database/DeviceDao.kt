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

    @Query("DELETE FROM `devices` WHERE deviceId = :deviceId")
    suspend fun delete(deviceId: String)

    @Query("SELECT * FROM `devices`")
    fun getAll(): LiveData<List<DeviceEntity>>

    @Query("SELECT * FROM `devices` WHERE deviceId = :deviceId LIMIT 1")
    suspend fun get(deviceId: String): DeviceEntity?
}