package com.shejan.pdfbox_pdfeditor

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.shejan.pdfbox_pdfeditor.util.ThemeManager
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class PDFBoxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize PDFBox
        PDFBoxResourceLoader.init(this)

        val themeManager = ThemeManager(this)
        
        // Apply night mode globally at startup to avoid flicker
        AppCompatDelegate.setDefaultNightMode(themeManager.getThemeMode())

        DynamicColors.applyToActivitiesIfAvailable(this, DynamicColorsOptions.Builder()
            .setPrecondition { _, _ -> 
                themeManager.isDynamicColorsEnabled() 
            }
            .build())
    }
}
