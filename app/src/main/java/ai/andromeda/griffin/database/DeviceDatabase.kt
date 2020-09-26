package ai.andromeda.griffin.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DeviceEntity::class], version = 1)
abstract class DeviceDatabase : RoomDatabase() {
    @Suppress("UNUSED")
    abstract val deviceDao: DeviceDao
    companion object {
        private lateinit var INSTANCE: DeviceDatabase
        fun getInstance(context: Context): DeviceDatabase {
            synchronized(this) {
                if (! ::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        DeviceDatabase::class.java,
                        "DEVICES_DATABASE"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}