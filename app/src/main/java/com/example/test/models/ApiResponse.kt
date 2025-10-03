package com.example.test.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FacturasVentasResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("facturas")
    val facturas: List<FacturaVenta>,
    @SerializedName("pagination")
    val pagination: Pagination,
    @SerializedName("filters_applied")
    val filtersApplied: FiltersApplied
)

data class FacturaVentaDetalleResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("factura")
    val factura: FacturaVentaDetalle
)

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: T? = null
)

data class ValidarAccesoRequest(
    val password_sii: String
)

// Respuesta de validación SII
data class ValidarAccesoResponse(
    val success: Boolean,
    val message: String,
    val expires_at: String?,
    val empresa: EmpresaInfo?
)

data class EmpresaInfo(
    val id: Int,
    val rut_empresa: String
)

// Estadísticas de inserción
data class EstadisticasInsert(
    val totalProcesadas: Int,
    val nuevasInsertadas: Int,
    val actualizadas: Int,
    val errores: Int,
    val clientesCreados: Int
) : Serializable
