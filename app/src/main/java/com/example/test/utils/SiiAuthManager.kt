package com.example.test.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SiiAuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "sii_auth"
        private const val KEY_LAST_VALIDATION = "last_validation"
        private const val KEY_EMPRESA_ID_VALIDATED = "empresa_id_validated"
        private const val SII_EXPIRY_TIME = 30 * 60 * 1000L // 30 minutos en milisegundos
    }

    /**
     * Verifica si necesita validación SII para la empresa actual
     */
    fun needsSiiValidation(empresaId: Int): Boolean {
        val lastValidation = prefs.getLong(KEY_LAST_VALIDATION, 0)
        val empresaValidated = prefs.getInt(KEY_EMPRESA_ID_VALIDATED, -1)
        val currentTime = System.currentTimeMillis()

        // Necesita validación si:
        // 1. Nunca se ha validado
        // 2. Ha expirado el tiempo (30 minutos)
        // 3. Es una empresa diferente
        val needsValidation = lastValidation == 0L ||
                             (currentTime - lastValidation) > SII_EXPIRY_TIME ||
                             empresaValidated != empresaId

        Log.d("SiiAuthManager", "=== VERIFICACIÓN ACCESO SII ===")
        Log.d("SiiAuthManager", "Empresa ID: $empresaId")
        Log.d("SiiAuthManager", "Empresa validada: $empresaValidated")
        Log.d("SiiAuthManager", "Última validación: $lastValidation")
        Log.d("SiiAuthManager", "Tiempo actual: $currentTime")
        Log.d("SiiAuthManager", "Tiempo transcurrido: ${(currentTime - lastValidation) / 1000} segundos")
        Log.d("SiiAuthManager", "Necesita validación: $needsValidation")

        return needsValidation
    }

    /**
     * Marca como validado para la empresa especificada
     */
    fun markAsValidated(empresaId: Int) {
        val currentTime = System.currentTimeMillis()
        prefs.edit()
            .putLong(KEY_LAST_VALIDATION, currentTime)
            .putInt(KEY_EMPRESA_ID_VALIDATED, empresaId)
            .apply()

        Log.d("SiiAuthManager", "✅ SII marcado como validado para empresa $empresaId")
        Log.d("SiiAuthManager", "Válido hasta: ${currentTime + SII_EXPIRY_TIME}")
    }

    /**
     * Obtiene los minutos restantes antes de que expire la validación
     */
    fun getRemainingMinutes(empresaId: Int): Int {
        if (needsSiiValidation(empresaId)) {
            return 0
        }

        val lastValidation = prefs.getLong(KEY_LAST_VALIDATION, 0)
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastValidation
        val remaining = SII_EXPIRY_TIME - elapsed

        return maxOf(0, (remaining / (60 * 1000)).toInt())
    }

    /**
     * Limpia la validación SII (útil al cambiar de empresa o logout)
     */
    fun clearValidation() {
        prefs.edit()
            .remove(KEY_LAST_VALIDATION)
            .remove(KEY_EMPRESA_ID_VALIDATED)
            .apply()

        Log.d("SiiAuthManager", "🗑️ Validación SII limpiada")
    }

    /**
     * Obtiene información de estado para mostrar al usuario
     */
    fun getStatusInfo(empresaId: Int): SiiStatusInfo {
        val needsValidation = needsSiiValidation(empresaId)
        val remainingMinutes = getRemainingMinutes(empresaId)

        return SiiStatusInfo(
            hasAccess = !needsValidation,
            remainingMinutes = remainingMinutes,
            empresaId = empresaId
        )
    }
}

data class SiiStatusInfo(
    val hasAccess: Boolean,
    val remainingMinutes: Int,
    val empresaId: Int
) {
    fun getStatusMessage(): String {
        return if (hasAccess) {
            "Acceso SII activo - $remainingMinutes minutos restantes"
        } else {
            "Requiere validación de clave SII"
        }
    }
}
