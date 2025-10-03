package com.example.test.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.test.components.PaginatedViewModel
import com.example.test.models.*
import com.example.test.repository.FacturaRepository
import com.example.test.utils.SessionManager
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : PaginatedViewModel<FacturaVenta>(application) {

    private val sessionManager = SessionManager(application)
    private val repository = FacturaRepository(sessionManager)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _empresaActual = MutableLiveData<Int?>()
    val empresaActual: LiveData<Int?> = _empresaActual

    init {
        Log.d("DashboardViewModel", "=== INICIANDO DASHBOARD ===")
        loadUserData()
        Log.d("DashboardViewModel", "Dashboard listo. Esperando acción del usuario...")
    }

    private fun loadUserData() {
        _user.value = sessionManager.getUser()
        val empresaId = sessionManager.getEmpresaId()
        _empresaActual.value = empresaId
        Log.d("DashboardViewModel", "Empresa cargada desde sesión: ID $empresaId")

        val user = sessionManager.getUser()
        val empresa = user?.empresas?.find { it.id == empresaId }
        Log.d("DashboardViewModel", "Empresa seleccionada: ${empresa?.razonSocial} (ID: $empresaId)")
    }

    // Implementación del método abstracto del PaginatedViewModel
    override suspend fun loadData(page: Int, filters: Map<String, Any?>): Pair<List<FacturaVenta>, Pagination> {
        val response = repository.getFacturasVentas(
            page = page,
            perPage = 15,
            search = filters["search"] as? String,
            anio = filters["anio"] as? String,
            mes = filters["mes"] as? String,
            estado = filters["estado"] as? String
        )

        if (response.isSuccessful) {
            val body = response.body()!!
            return Pair(body.facturas, body.pagination)
        } else {
            throw Exception("Error al cargar facturas: ${response.message()}")
        }
    }

    // Métodos convenientes para cargar facturas con filtros específicos
    fun loadAllFacturas() {
        Log.d("DashboardViewModel", "Cargando todas las facturas...")
        loadItems(mapOf("estado" to null))
    }

    fun loadFacturasPendientes() {
        loadItems(mapOf("estado" to "pendiente"))
    }

    fun loadFacturasPagadas() {
        Log.d("DashboardViewModel", "Cargando facturas pagadas...")
        loadItems(mapOf("estado" to "pagada"))
    }

    fun loadFacturasWithSearch(search: String) {
        loadItems(mapOf("search" to search))
    }

    fun cambiarEmpresa(empresaId: Int) {
        viewModelScope.launch {
            try {
                paginationManager.setLoading(true)
                val response = repository.cambiarEmpresa(empresaId)

                if (response.isSuccessful) {
                    sessionManager.setEmpresaId(empresaId)
                    _empresaActual.value = empresaId
                    // Recargar facturas con la nueva empresa
                    refresh()
                } else {
                    _error.value = "Error al cambiar empresa: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error al cambiar empresa: ${e.message}"
            } finally {
                paginationManager.setLoading(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
            } catch (e: Exception) {
                // Ignorar errores del logout
            } finally {
                sessionManager.clearSession()
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
