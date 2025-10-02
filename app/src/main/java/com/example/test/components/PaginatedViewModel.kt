package com.example.test.components

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.test.models.Pagination
import kotlinx.coroutines.launch

abstract class PaginatedViewModel<T>(application: Application) : AndroidViewModel(application) {

    protected val paginationManager = PaginationManager<T>()

    // Exponer las propiedades del PaginationManager
    val items: LiveData<List<T>> = paginationManager.items
    val pagination: LiveData<Pagination?> = paginationManager.pagination
    val loading: LiveData<Boolean> = paginationManager.loading
    val loadingMore: LiveData<Boolean> = paginationManager.loadingMore

    // Método abstracto que cada ViewModel debe implementar
    abstract suspend fun loadData(page: Int, filters: Map<String, Any?>): Pair<List<T>, Pagination>

    fun loadItems(filters: Map<String, Any?> = emptyMap(), page: Int = 1, append: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!append) {
                    paginationManager.setLoading(true)
                    paginationManager.setFilters(filters)
                } else {
                    paginationManager.setLoadingMore(true)
                }

                Log.d("PaginatedViewModel", "Cargando datos - página: $page, append: $append")
                Log.d("PaginatedViewModel", "Filtros: $filters")

                val (newItems, newPagination) = loadData(page, filters)
                paginationManager.setData(newItems, newPagination, append)

                Log.d("PaginatedViewModel", "Datos cargados exitosamente - página ${newPagination.currentPage}/${newPagination.lastPage}")

            } catch (e: Exception) {
                Log.e("PaginatedViewModel", "Error al cargar datos: ${e.message}", e)
                handleError(e)
            } finally {
                if (!append) {
                    paginationManager.setLoading(false)
                } else {
                    paginationManager.setLoadingMore(false)
                }
            }
        }
    }

    fun loadNextPage() {
        val nextPage = paginationManager.getNextPage()
        val currentFilters = paginationManager.getCurrentFilters()
        if (nextPage != null && !paginationManager.isLoadingMore()) {
            Log.d("PaginatedViewModel", "Cargando página siguiente: $nextPage con filtros: $currentFilters")
            loadItems(currentFilters, nextPage, append = true)
        } else {
            Log.w("PaginatedViewModel", "No se puede cargar siguiente página: nextPage=$nextPage, isLoadingMore=${paginationManager.isLoadingMore()}")
        }
    }

    fun loadPreviousPage() {
        val previousPage = paginationManager.getPreviousPage()
        val currentFilters = paginationManager.getCurrentFilters()
        if (previousPage != null && !paginationManager.isLoadingMore()) {
            Log.d("PaginatedViewModel", "Cargando página anterior: $previousPage con filtros: $currentFilters")
            loadItems(currentFilters, previousPage, append = false)
        } else {
            Log.w("PaginatedViewModel", "No se puede cargar página anterior: previousPage=$previousPage, isLoadingMore=${paginationManager.isLoadingMore()}")
        }
    }

    fun refresh() {
        Log.d("PaginatedViewModel", "Refrescando datos")
        loadItems(paginationManager.getCurrentFilters())
    }

    fun canLoadNext() = paginationManager.canLoadNext()
    fun canLoadPrevious() = paginationManager.canLoadPrevious()
    fun getTotalItems() = paginationManager.getTotalItems()
    fun getCurrentItemsCount() = paginationManager.getCurrentItemsCount()
    fun getRemainingItems() = paginationManager.getRemainingItems()

    // Método que cada ViewModel puede sobrescribir para manejar errores
    protected open fun handleError(error: Exception) {
        // Implementación por defecto - cada ViewModel puede sobrescribir
    }

    override fun onCleared() {
        super.onCleared()
        paginationManager.reset()
    }
}
