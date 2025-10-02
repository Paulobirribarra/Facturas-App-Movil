package com.example.test.components

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.test.models.Pagination

class PaginationManager<T> {

    private val _items = MutableLiveData<List<T>>()
    val items: LiveData<List<T>> = _items

    private val _pagination = MutableLiveData<Pagination?>()
    val pagination: LiveData<Pagination?> = _pagination

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _loadingMore = MutableLiveData<Boolean>()
    val loadingMore: LiveData<Boolean> = _loadingMore

    private var currentPage = 1
    private var currentFilters: Map<String, Any?> = emptyMap()
    private var isLoadingMore = false

    fun setData(newItems: List<T>, newPagination: Pagination, append: Boolean = false) {
        Log.d("PaginationManager", "setData: ${newItems.size} items, página ${newPagination.currentPage}/${newPagination.lastPage}, append: $append")

        if (append) {
            val currentItems = _items.value?.toMutableList() ?: mutableListOf()
            currentItems.addAll(newItems)
            _items.value = currentItems
            Log.d("PaginationManager", "Items agregados. Total ahora: ${currentItems.size}")
        } else {
            _items.value = newItems
            Log.d("PaginationManager", "Items reemplazados. Total: ${newItems.size}")
        }

        _pagination.value = newPagination
        currentPage = newPagination.currentPage
    }

    fun setLoading(loading: Boolean) {
        _loading.value = loading
    }

    fun setLoadingMore(loadingMore: Boolean) {
        isLoadingMore = loadingMore
        _loadingMore.value = loadingMore
    }

    fun setFilters(filters: Map<String, Any?>) {
        currentFilters = filters
        currentPage = 1
        Log.d("PaginationManager", "Filtros actualizados: $filters")
    }

    fun getCurrentPage() = currentPage
    fun getCurrentFilters() = currentFilters
    fun isLoadingMore() = isLoadingMore

    fun canLoadNext(): Boolean {
        val pagination = _pagination.value
        return pagination != null && currentPage < pagination.lastPage && !isLoadingMore
    }

    fun canLoadPrevious(): Boolean {
        return currentPage > 1 && !isLoadingMore
    }

    fun getNextPage() = if (canLoadNext()) currentPage + 1 else null
    fun getPreviousPage() = if (canLoadPrevious()) currentPage - 1 else null

    fun getTotalItems() = _pagination.value?.total ?: 0
    fun getCurrentItemsCount() = _items.value?.size ?: 0
    fun getRemainingItems() = getTotalItems() - getCurrentItemsCount()

    fun reset() {
        _items.value = emptyList()
        _pagination.value = null
        currentPage = 1
        currentFilters = emptyMap()
        isLoadingMore = false
        _loading.value = false
        _loadingMore.value = false
        Log.d("PaginationManager", "PaginationManager reseteado")
    }
}
