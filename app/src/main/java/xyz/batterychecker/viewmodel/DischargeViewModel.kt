package xyz.batterychecker.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DischargeState(
    val currentCapacity: Int = 0,
    val estimatedTime: String = "",
    val totalUsage: Int = 0,
    val deepSleepUsage: Int = 0,
    val screenOnUsage: Int = 0,
    val screenOffUsage: Int = 0
)

class DischargeViewModel(application: Application) : AndroidViewModel(application) {
    private val _dischargeState = MutableStateFlow(DischargeState())
    val dischargeState: StateFlow<DischargeState> = _dischargeState

    private var lastCapacity: Int = 0
    private var lastTimestamp: Long = System.currentTimeMillis()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val batteryManager = context?.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                
                // Get current capacity
                val currentCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000
                
                // Get current rate
                val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                val rateMA = Math.abs(currentNow / 1000)

                // Calculate time remaining
                val remainingTime = if (rateMA > 0) {
                    val hours = currentCapacity / rateMA
                    "${hours}h ${(hours * 60 % 60).toInt()}m"
                } else "Calculating..."

                // Calculate usage percentages
                val deepSleepUsage = (rateMA * 0.1).toInt() // Approximate 10% in deep sleep
                val screenOnUsage = (rateMA * 0.67).toInt() // Approximate 67% screen on
                val screenOffUsage = (rateMA * 0.23).toInt() // Approximate 23% screen off

                _dischargeState.value = DischargeState(
                    currentCapacity = currentCapacity,
                    estimatedTime = remainingTime,
                    totalUsage = rateMA,
                    deepSleepUsage = deepSleepUsage,
                    screenOnUsage = screenOnUsage,
                    screenOffUsage = screenOffUsage
                )
            }
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        application.registerReceiver(batteryReceiver, filter)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(batteryReceiver)
    }
}