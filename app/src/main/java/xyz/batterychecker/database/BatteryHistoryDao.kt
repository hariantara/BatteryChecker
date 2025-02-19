package xyz.batterychecker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryHistoryDao {
    @Insert
    suspend fun insertHistory(history: BatteryHistoryEntity)

    @Query("SELECT * FROM battery_history WHERE timestamp >= datetime('now', '-24 hours') ORDER BY timestamp DESC")
    fun getDailyHistory(): Flow<List<BatteryHistoryEntity>>

    @Query("SELECT * FROM battery_history WHERE timestamp >= datetime('now', '-7 days') ORDER BY timestamp DESC")
    fun getWeeklyHistory(): Flow<List<BatteryHistoryEntity>>

    @Query("SELECT * FROM battery_history WHERE timestamp >= datetime('now', '-30 days') ORDER BY timestamp DESC")
    fun getMonthlyHistory(): Flow<List<BatteryHistoryEntity>>

    @Query("""
        SELECT 
            strftime('%H:00', timestamp) as timePoint,
            AVG(batteryLevel) as avgLevel,
            AVG(temperature) as avgTemp,
            MAX(batteryLevel) as maxLevel,
            MIN(batteryLevel) as minLevel
        FROM battery_history 
        WHERE timestamp >= datetime('now', '-24 hours')
        GROUP BY timePoint
        ORDER BY timePoint DESC
    """)
    fun getDailyAggregated(): Flow<List<BatteryAggregateData>>

    @Query("""
        SELECT 
            strftime('%Y-%m-%d', timestamp) as timePoint,
            AVG(batteryLevel) as avgLevel,
            AVG(temperature) as avgTemp,
            MAX(batteryLevel) as maxLevel,
            MIN(batteryLevel) as minLevel
        FROM battery_history 
        WHERE timestamp >= datetime('now', '-7 days')
        GROUP BY timePoint
        ORDER BY timePoint DESC
    """)
    fun getWeeklyAggregated(): Flow<List<BatteryAggregateData>>

    @Query("""
        SELECT 
            strftime('%Y-%m-%d', timestamp) as timePoint,
            AVG(batteryLevel) as avgLevel,
            AVG(temperature) as avgTemp,
            MAX(batteryLevel) as maxLevel,
            MIN(batteryLevel) as minLevel
        FROM battery_history 
        WHERE timestamp >= datetime('now', '-30 days')
        GROUP BY timePoint
        ORDER BY timePoint DESC
    """)
    fun getMonthlyAggregated(): Flow<List<BatteryAggregateData>>
} 