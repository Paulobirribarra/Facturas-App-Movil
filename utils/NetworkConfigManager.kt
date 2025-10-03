package com.example.test.utils

import android.content.Context
import android.os.Build
import com.example.test.BuildConfig

object NetworkConfigManager {
    private const val PREF_NAME = "network_config"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_AUTO_DETECT = "auto_detect_enabled"

    fun getBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedUrl = prefs.getString(KEY_BASE_URL, null)

        return savedUrl ?: getDefaultUrl()
    }

    fun setBaseUrl(context: Context, url: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BASE_URL, url)
            .apply()
    }

    fun enableAutoDetection(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AUTO_DETECT, enabled)
            .apply()
    }

    fun isAutoDetectionEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_DETECT, true)
    }

    private fun getDefaultUrl(): String {
        return when {
            isEmulator() -> "http://10.0.2.2:8000/api/"
            BuildConfig.DEBUG -> "http://192.168.32.1:8000/api/" // Tu IP actual
            else -> "https://tu-api-produccion.com/api/"
        }
    }

    // Cambiar a público para que sea accesible desde ApiClient
    fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
               Build.FINGERPRINT.startsWith("unknown") ||
               Build.MODEL.contains("google_sdk") ||
               Build.MODEL.contains("Emulator") ||
               Build.MODEL.contains("Android SDK built for")
    }

    fun getAvailableEndpoints(): List<String> {
        return listOf(
            "http://10.0.2.2:8000/api/",      // Emulador
            "http://192.168.32.1:8000/api/",   // Tu red local actual
            "http://127.0.0.1:8000/api/",     // Localhost
            "http://localhost:8000/api/"       // Localhost alternativo
        )
    }
}
