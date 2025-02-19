package xyz.batterychecker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "battery_history")
data class BatteryHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: LocalDateTime,
    val batteryLevel: Int,
    val temperature: Float,
    val voltage: Float,
    val isCharging: Boolean,
    val chargingTime: Long = 0
)