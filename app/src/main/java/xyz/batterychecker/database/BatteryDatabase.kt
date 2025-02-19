package xyz.batterychecker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xyz.batterychecker.database.BatteryHistoryDao

@Database(entities = [BatteryHistoryEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class BatteryDatabase : RoomDatabase() {
    abstract fun batteryHistoryDao(): BatteryHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: BatteryDatabase? = null

        fun getInstance(context: Context): BatteryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BatteryDatabase::class.java,
                    "battery_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 