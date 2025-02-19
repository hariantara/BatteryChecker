package xyz.batterychecker.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import xyz.batterychecker.database.BatteryDatabase
import xyz.batterychecker.database.BatteryHistoryEntity
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

data class BatteryHistoryState(
    val dailyHistory: List<BatteryHistoryItem> = emptyList(),
    val weeklyHistory: List<BatteryHistoryItem> = emptyList(),
    val monthlyHistory: List<BatteryHistoryItem> = emptyList(),
    val selectedTab: Int = 0,
    val chartEntries: List<ChartEntry> = emptyList()
)

data class BatteryHistoryItem(
    val timestamp: LocalDateTime,
    val startPercentage: Int,
    val endPercentage: Int,
    val duration: Long, // in minutes
    val temperature: Float,
    val voltage: Float
)

data class ChartEntry(
    val timePoint: String,
    val avgLevel: Float,
    val maxLevel: Int,
    val minLevel: Int,
    val avgTemp: Float
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val _historyState = MutableStateFlow(BatteryHistoryState())
    val historyState: StateFlow<BatteryHistoryState> = _historyState
    private val batteryDao = BatteryDatabase.getInstance(application).batteryHistoryDao()
    private var lastRecordedLevel: Int? = null
    private var lastRecordedTime: LocalDateTime? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val batteryManager = context?.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val percentage = (level * 100f / scale).toInt()
                val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                
                val currentTime = LocalDateTime.now()
                val chargingTime = if (lastRecordedTime != null && lastRecordedLevel != null) {
                    ChronoUnit.MINUTES.between(lastRecordedTime, currentTime)
                } else 0

                viewModelScope.launch {
                    batteryDao.insertHistory(
                        BatteryHistoryEntity(
                            timestamp = currentTime,
                            batteryLevel = percentage,
                            temperature = temperature,
                            voltage = voltage,
                            isCharging = isCharging,
                            chargingTime = chargingTime
                        )
                    )
                }

                lastRecordedLevel = percentage
                lastRecordedTime = currentTime
            }
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        getApplication<Application>().registerReceiver(batteryReceiver, filter)
        loadBatteryHistory()
    }

    private fun loadBatteryHistory() {
        viewModelScope.launch {
            val (historyFlow, aggregatedFlow) = when (historyState.value.selectedTab) {
                0 -> Pair(batteryDao.getDailyHistory(), batteryDao.getDailyAggregated())
                1 -> Pair(batteryDao.getWeeklyHistory(), batteryDao.getWeeklyAggregated())
                else -> Pair(batteryDao.getMonthlyHistory(), batteryDao.getMonthlyAggregated())
            }

            // Collect both regular history and aggregated data
            historyFlow.collect { entities ->
                val historyItems = entities.map { entity ->
                    BatteryHistoryItem(
                        timestamp = entity.timestamp,
                        startPercentage = entity.batteryLevel,
                        endPercentage = entity.batteryLevel,
                        duration = entity.chargingTime,
                        temperature = entity.temperature,
                        voltage = entity.voltage
                    )
                }

                aggregatedFlow.collect { aggregatedData ->
                    val chartEntries = aggregatedData.map { data ->
                        ChartEntry(
                            timePoint = data.timePoint,
                            avgLevel = data.avgLevel,
                            maxLevel = data.maxLevel,
                            minLevel = data.minLevel,
                            avgTemp = data.avgTemp
                        )
                    }

                    _historyState.value = _historyState.value.copy(
                        dailyHistory = if (historyState.value.selectedTab == 0) historyItems else emptyList(),
                        weeklyHistory = if (historyState.value.selectedTab == 1) historyItems else emptyList(),
                        monthlyHistory = if (historyState.value.selectedTab == 2) historyItems else emptyList(),
                        chartEntries = chartEntries
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(batteryReceiver)
    }

    fun updateSelectedTab(index: Int) {
        _historyState.value = _historyState.value.copy(selectedTab = index)
        loadBatteryHistory()
    }
}