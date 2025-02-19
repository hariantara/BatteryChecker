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
import java.io.File

data class BatteryHealthState(
    val healthPercentage: Float = 0f,
    val healthStatus: String = "",
    val originalCapacity: Int = 0,
    val currentCapacity: Int = 0,
    val chargeCycles: Int = 0,
    val temperature: Float = 0f,
    val recommendations: List<String> = emptyList()
)

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val _healthState = MutableStateFlow(BatteryHealthState())
    val healthState: StateFlow<BatteryHealthState> = _healthState

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val batteryManager = context?.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                
                val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
                val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                val designCapacity = getBatteryDesignCapacity(context)
                val currentCapacity = getCurrentCapacity(batteryManager)
                val cycles = getBatteryCycles(context)
                
                val healthPercentage = calculateHealthPercentage(health, currentCapacity, designCapacity)
                val healthStatus = getHealthStatus(health)
                
                _healthState.value = BatteryHealthState(
                    healthPercentage = healthPercentage,
                    healthStatus = healthStatus,
                    originalCapacity = designCapacity,
                    currentCapacity = currentCapacity,
                    chargeCycles = cycles,
                    temperature = temperature,
                    recommendations = generateRecommendations(
                        healthPercentage,
                        temperature,
                        cycles,
                        currentCapacity.toFloat() / designCapacity.toFloat()
                    )
                )
            }
        }
    }

    private fun generateRecommendations(
        health: Float,
        temp: Float,
        cycles: Int,
        capacityRatio: Float
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (health < 80f) {
            recommendations.add("Consider battery replacement soon")
        }
        if (temp > 40f) {
            recommendations.add("Avoid charging while using heavy apps")
        }
        if (cycles > 500) {
            recommendations.add("Battery has high cycle count, monitor health closely")
        }
        if (capacityRatio < 0.8f) {
            recommendations.add("Battery capacity significantly reduced")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Battery is in good condition")
        }
        
        return recommendations
    }

    private fun getBatteryCycles(context: Context): Int {
        return try {
            val file = File("/sys/class/power_supply/battery/cycle_count")
            if (file.exists() && file.canRead()) {
                file.readText().trim().toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun getBatteryDesignCapacity(context: Context): Int {
        val powerProfile = try {
            Class.forName("com.android.internal.os.PowerProfile")
                .getConstructor(Context::class.java)
                .newInstance(context)
        } catch (e: Exception) {
            e.printStackTrace()
            return 4000 // Fallback value if can't get real capacity
        }

        return try {
            val method = powerProfile.javaClass.getMethod("getBatteryCapacity")
            (method.invoke(powerProfile) as Double).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            4000 // Fallback value if can't get real capacity
        }
    }

    private fun getCurrentCapacity(batteryManager: BatteryManager): Int {
        return try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000
        } catch (e: Exception) {
            0
        }
    }

    private fun calculateHealthPercentage(health: Int, currentCapacity: Int, designCapacity: Int): Float {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> 95f
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 70f
            BatteryManager.BATTERY_HEALTH_DEAD -> 20f
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 60f
            BatteryManager.BATTERY_HEALTH_COLD -> 80f
            else -> 85f // BATTERY_HEALTH_UNKNOWN or other states
        }
    }

    private fun getHealthStatus(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good condition"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Too Cold"
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> "Unknown"
            else -> "Fair condition"
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