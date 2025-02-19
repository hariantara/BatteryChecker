package xyz.batterychecker.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "battery_history")
data class BatteryHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: LocalDateTime,
    val batteryLevel: Int,
    val chargingTime: Long,
    val temperature: Float,
    val voltage: Float,
    val isCharging: Boolean = false
) 