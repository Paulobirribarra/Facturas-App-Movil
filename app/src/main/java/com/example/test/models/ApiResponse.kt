package com.example.test.models

import com.google.gson.annotations.SerializedName

data class FacturasVentasResponse(
    val success: Boolean,
    val facturas: List<FacturaVenta>,
    val pagination: Pagination,
    @SerializedName("filters_applied")
    val filtersApplied: FiltersApplied
)

data class FacturaVentaDetalleResponse(
    val success: Boolean,
    val factura: FacturaVentaDetalle
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)
