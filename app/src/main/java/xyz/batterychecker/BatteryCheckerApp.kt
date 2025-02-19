package xyz.batterychecker

import android.app.Application
import xyz.batterychecker.viewmodel.BatteryViewModel

class BatteryCheckerApplication : Application() {
    lateinit var batteryViewModel: BatteryViewModel
    
    override fun onCreate() {
        super.onCreate()
        batteryViewModel = BatteryViewModel(this)
    }
}