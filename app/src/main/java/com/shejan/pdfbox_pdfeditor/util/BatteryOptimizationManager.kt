package com.shejan.pdfbox_pdfeditor.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission

class BatteryOptimizationManager(private val context: Context) {

    companion object {
        private const val TAG = "BatteryOptManager"
    }

    /**
     * Check if app is in battery optimization whitelist
     */
    fun isIgnoringBatteryOptimizations(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    /**
     * Request to disable battery optimization
     * This opens the settings screen where user can whitelist the app
     */
    fun requestDisableBatteryOptimization() {
        if (isIgnoringBatteryOptimizations()) {
            Log.d(TAG, "Already whitelisted from battery optimization")
            return
        }

        try {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = android.net.Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting battery optimization exemption", e)
            // Fallback to battery settings
            openBatterySettings()
        }
    }

    /**
     * Open battery settings directly
     */
    fun openBatterySettings() {
        try {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening battery settings", e)
        }
    }

    /**
     * Check if battery optimization is enabled globally
     */
    fun isBatteryOptimizationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val result = Settings.Global.getInt(
                    context.contentResolver,
                    "low_power",
                    0
                )
                result != 0
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    /**
     * Get RAM and storage info for display
     */
    fun getDeviceStats(): DeviceStats {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / (1024 * 1024)
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val freeMemory = runtime.freeMemory() / (1024 * 1024)

        return DeviceStats(
            totalRam = maxMemory,
            usedRam = usedMemory,
            freeRam = freeMemory,
            batteryOptimized = !isIgnoringBatteryOptimizations()
        )
    }

    data class DeviceStats(
        val totalRam: Long,
        val usedRam: Long,
        val freeRam: Long,
        val batteryOptimized: Boolean
    )
}