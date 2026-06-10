package com.razmenium.app

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Ја чува и применува избраната локализација (MK/EN) на ниво на цела апликација.
 */
object LocaleHelper {

    private const val PREFS_NAME = "settings"
    private const val KEY_LANGUAGE = "language"
    const val DEFAULT_LANGUAGE = "mk"

    fun getLanguage(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun wrap(context: Context): Context {
        val locale = Locale(getLanguage(context))
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
