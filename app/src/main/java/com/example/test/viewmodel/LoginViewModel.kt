package com.example.test.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.test.repository.FacturaRepository
import com.example.test.utils.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val repository = FacturaRepository(sessionManager)

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = repository.login(email, password, "Android App")
                if (response.isSuccessful) {
                    val loginResponse = response.body()!!
                    if (loginResponse.success && loginResponse.user != null && loginResponse.token != null) {
                        // Guardar datos de sesión
                        sessionManager.saveSession(
                            token = loginResponse.token,
                            user = loginResponse.user
                        )
                        _loginResult.value = true
                    } else {
                        _error.value = loginResponse.message ?: "Error en el login"
                    }
                } else {
                    _error.value = "Error de conexión: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de red: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
