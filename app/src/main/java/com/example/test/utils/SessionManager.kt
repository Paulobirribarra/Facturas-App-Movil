package com.example.test.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.test.models.User
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "app_session"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "user_data"
        private const val KEY_EMPRESA_ID = "empresa_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveSession(token: String, user: User, empresaId: Int? = null) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER, gson.toJson(user))
            putBoolean(KEY_IS_LOGGED_IN, true)
            empresaId?.let { putInt(KEY_EMPRESA_ID, it) }
            apply()
        }
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getAuthHeader(): String? {
        return getToken()?.let { "Bearer $it" }
    }

    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER, null)
        return userJson?.let { gson.fromJson(it, User::class.java) }
    }

    fun getEmpresaId(): Int? {
        val empresaId = prefs.getInt(KEY_EMPRESA_ID, -1)
        return if (empresaId != -1) empresaId else null
    }

    fun setEmpresaId(empresaId: Int) {
        prefs.edit().putInt(KEY_EMPRESA_ID, empresaId).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getToken() != null
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
