package ai.andromeda.griffin.database

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
    suspend fun getAll(): List<DeviceEntity>

    @Query("SELECT * FROM `devices` WHERE id = :deviceId")
    suspend fun get(deviceId: String): DeviceEntity?
}