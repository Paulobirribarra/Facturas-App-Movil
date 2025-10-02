package com.example.test.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.test.models.*
import com.example.test.repository.FacturaRepository
import com.example.test.utils.SessionManager
import kotlinx.coroutines.launch

class EmpresaSelectorViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val repository = FacturaRepository(sessionManager)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _empresas = MutableLiveData<List<Empresa>>()
    val empresas: LiveData<List<Empresa>> = _empresas

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _empresaSeleccionada = MutableLiveData<Boolean>()
    val empresaSeleccionada: LiveData<Boolean> = _empresaSeleccionada

    init {
        Log.d("EmpresaSelectorVM", "Iniciando EmpresaSelectorViewModel")
        loadUserData()
        loadEmpresas()
    }

    private fun loadUserData() {
        val userData = sessionManager.getUser()
        Log.d("EmpresaSelectorVM", "Usuario cargado: ${userData?.name} (${userData?.email})")
        _user.value = userData
    }

    private fun loadEmpresas() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                Log.d("EmpresaSelectorVM", "Cargando lista de empresas...")

                // Primero intentamos obtener las empresas del usuario actual
                val userData = sessionManager.getUser()
                if (userData?.empresas?.isNotEmpty() == true) {
                    Log.d("EmpresaSelectorVM", "Empresas obtenidas del usuario local: ${userData.empresas.size}")
                    _empresas.value = userData.empresas
                } else {
                    // Si no hay empresas locales, las obtenemos de la API
                    Log.d("EmpresaSelectorVM", "Obteniendo empresas desde la API...")
                    val response = repository.getEmpresas()

                    if (response.isSuccessful) {
                        response.body()?.let { empresasResponse ->
                            Log.d("EmpresaSelectorVM", "Empresas obtenidas de la API: ${empresasResponse.empresas.size}")
                            _empresas.value = empresasResponse.empresas
                        }
                    } else {
                        Log.e("EmpresaSelectorVM", "Error al obtener empresas: ${response.code()}")
                        _error.value = "Error al cargar empresas: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                Log.e("EmpresaSelectorVM", "Error de conexión al cargar empresas: ${e.message}", e)
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun seleccionarEmpresa(empresa: Empresa) {
        Log.d("EmpresaSelectorVM", "Seleccionando empresa: ${empresa.razon_social} (ID: ${empresa.id})")

        // Simplemente guardamos la empresa localmente SIN hacer llamada al servidor
        // El backend ya maneja la separación por empresa mediante el header X-Empresa-ID
        sessionManager.setEmpresaId(empresa.id)
        Log.d("EmpresaSelectorVM", "Empresa ${empresa.razon_social} (ID: ${empresa.id}) guardada localmente")
        Log.d("EmpresaSelectorVM", "Las consultas posteriores usarán X-Empresa-ID: ${empresa.id}")
        Log.d("EmpresaSelectorVM", "Navegando al dashboard...")

        _empresaSeleccionada.value = true
    }

    fun logout() {
        viewModelScope.launch {
            try {
                Log.d("EmpresaSelectorVM", "Cerrando sesión...")
                repository.logout()
            } catch (e: Exception) {
                Log.w("EmpresaSelectorVM", "Error al cerrar sesión: ${e.message}")
            } finally {
                sessionManager.clearSession()
                Log.d("EmpresaSelectorVM", "Sesión cerrada localmente")
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
