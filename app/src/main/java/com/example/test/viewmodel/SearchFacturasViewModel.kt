package com.example.test.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.test.components.PaginatedViewModel
import com.example.test.models.FacturaVenta
import com.example.test.models.Pagination
import com.example.test.repository.FacturaRepository
import com.example.test.utils.SessionManager

class SearchFacturasViewModel(application: Application) : PaginatedViewModel<FacturaVenta>(application) {

    private val sessionManager = SessionManager(application)
    private val repository = FacturaRepository(sessionManager)

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Implementación del método abstracto - mismo código que DashboardViewModel
    override suspend fun loadData(page: Int, filters: Map<String, Any?>): Pair<List<FacturaVenta>, Pagination> {
        val response = repository.getFacturasVentas(
            page = page,
            perPage = 20, // Diferente tamaño de página para demostrar flexibilidad
            search = filters["search"] as? String,
            anio = filters["anio"] as? String,
            mes = filters["mes"] as? String,
            estado = filters["estado"] as? String
        )

        if (response.isSuccessful) {
            response.body()?.let { facturasResponse ->
                Log.d("SearchFacturasVM", "Búsqueda exitosa: ${facturasResponse.facturas.size} facturas encontradas")
                return Pair(facturasResponse.facturas, facturasResponse.pagination)
            }
        }

        throw Exception("Error en búsqueda: ${response.code()}")
    }

    override fun handleError(error: Exception) {
        _error.value = error.message
    }

    // Métodos específicos para búsqueda
    fun searchByText(searchText: String) {
        Log.d("SearchFacturasVM", "Buscando facturas con texto: '$searchText'")
        loadItems(mapOf("search" to searchText))
    }

    fun searchByYear(year: String) {
        Log.d("SearchFacturasVM", "Buscando facturas del año: $year")
        loadItems(mapOf("anio" to year))
    }

    fun searchByClientAndState(clientText: String, estado: String) {
        Log.d("SearchFacturasVM", "Búsqueda avanzada: cliente='$clientText', estado='$estado'")
        loadItems(mapOf(
            "search" to clientText,
            "estado" to estado
        ))
    }

    fun clearError() {
        _error.value = null
    }
}
