package com.shejan.pdfbox_pdfeditor.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.ap.pdf.box.R

class ThemeManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "pdf_box_theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLORS = "dynamic_colors"
        private const val KEY_AMOLED_MODE = "amoled_mode"

        const val THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO
        const val THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES
        const val THEME_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getThemeMode(): Int {
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)
    }

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun isDynamicColorsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            prefs.getBoolean(KEY_DYNAMIC_COLORS, true)
        } else {
            false
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            prefs.edit().putBoolean(KEY_DYNAMIC_COLORS, enabled).apply()
        }
    }

    fun isAmoledModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_AMOLED_MODE, false)
    }

    fun setAmoledMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AMOLED_MODE, enabled).apply()
    }

    fun isDynamicColorsAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun isDarkThemeActive(): Boolean {
        val mode = getThemeMode()
        return if (mode == THEME_SYSTEM) {
            val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        } else {
            mode == THEME_DARK
        }
    }

    fun getThemeResId(): Int {
        return if (isDarkThemeActive() && isAmoledModeEnabled()) {
            R.style.Theme_PDFBoxByAP_Amoled
        } else if (isDarkThemeActive()) {
            R.style.Theme_PDFBoxByAP_Dark
        } else {
            R.style.Theme_PDFBoxByAP
        }
    }

    fun applyCurrentTheme() {
        AppCompatDelegate.setDefaultNightMode(getThemeMode())
    }
}
