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

data class BatteryState(
    val percentage: Float = 0f,
    val isCharging: Boolean = false,
    val cycleWear: Float = 0f,
    val voltage: Float = 0f,
    val temperature: Float = 0f,
    val timeToFull: Long = 0L,
    val batteryDuration: Long = 0L,
    val batteryModel: String = "Unknown"
)

class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    private val _batteryState = MutableStateFlow(BatteryState())
    val batteryState: StateFlow<BatteryState> = _batteryState

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                
                // Get temperature in Celsius (originally in tenths of degree)
                val tempCelsius = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                
                // Get voltage in Volts (originally in millivolts)
                val voltageVolts = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f

                val batteryPercentage = level * 100f / scale
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                status == BatteryManager.BATTERY_STATUS_FULL

                _batteryState.value = BatteryState(
                    percentage = batteryPercentage,
                    isCharging = isCharging,
                    cycleWear = calculateCycleWear(),
                    temperature = tempCelsius,
                    voltage = voltageVolts,
                    batteryModel = getBatteryModel(context!!)
                )
            }
        }
    }

    init {
        registerBatteryReceiver()
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        getApplication<Application>().registerReceiver(batteryReceiver, filter)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateCycleWear(): Float {
        // Implement the logic to calculate cycle wear
        return 0.35f // Default value for now
    }

    private fun getBatteryModel(context: Context): String {
        return try {
            // Try multiple paths where battery information might be stored
            val paths = listOf(
                "/sys/class/power_supply/battery/model_name",
                "/sys/class/power_supply/bms/battery_type",
                "/sys/class/power_supply/battery/battery_type",
                "/sys/class/power_supply/bms/model_name"
            )
            
            for (path in paths) {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val model = file.readText().trim()
                    if (model.isNotEmpty()) {
                        return model
                    }
                }
            }

            // Try to get battery info from system service
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryInfo = batteryManager.toString()
            if (batteryInfo.contains("model") || batteryInfo.contains("type")) {
                return batteryInfo.substringAfter("model=").substringBefore(",").trim()
            }

            // If system files don't work, try getting from Build properties
            val manufacturer = android.os.Build.MANUFACTURER
            val model = android.os.Build.MODEL
            
            // For Xiaomi/Poco devices
            if (manufacturer.equals("Xiaomi", ignoreCase = true)) {
                val xiaomiModel = getPropValue("ro.product.mod_device", context) ?: 
                                getPropValue("ro.product.vendor.model", context)
                if (!xiaomiModel.isNullOrEmpty()) {
                    return xiaomiModel
                }
            }

            // Return a combination of manufacturer and model if all else fails
            "$manufacturer $model"
            
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }
    }

    private fun getPropValue(prop: String, context: Context): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $prop")
            process.inputStream.bufferedReader().readText().trim()
                .takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            try {
                // Alternative way to get battery info
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                when (prop) {
                    "ro.product.mod_device" -> android.os.Build.DEVICE
                    "ro.product.vendor.model" -> android.os.Build.MODEL
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}