package xyz.batterychecker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface BatteryHistoryDao {
    @Insert
    suspend fun insertHistory(history: BatteryHistoryEntity)

    @Query("SELECT * FROM battery_history WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getHistorySince(startTime: LocalDateTime): Flow<List<BatteryHistoryEntity>>

    @Query("SELECT * FROM battery_history WHERE timestamp >= datetime('now', '-24 hours')")
    fun getDailyHistory(): Flow<List<BatteryHistoryEntity>>

    @Query("SELECT * FROM battery_history WHERE timestamp >= datetime('now', '-7 days')")
    fun getWeeklyHistory(): Flow<List<BatteryHistoryEntity>>

    @Query("SELECT * FROM battery_history WHERE timestamp >= datetime('now', '-30 days')")
    fun getMonthlyHistory(): Flow<List<BatteryHistoryEntity>>
}