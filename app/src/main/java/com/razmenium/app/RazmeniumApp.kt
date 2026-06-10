package com.razmenium.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class RazmeniumApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Примени ја зачуваната тема (темна/светла) уште при стартување
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(
            prefs.getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        )
    }

    companion object {
        const val PREFS_NAME = "settings"
        const val KEY_NIGHT_MODE = "night_mode"
    }
}
