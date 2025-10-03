package com.example.test.repository

import com.example.test.models.*
import com.example.test.network.ApiClient
import com.example.test.utils.SessionManager
import retrofit2.Response
import android.util.Log

class FacturaRepository(private val sessionManager: SessionManager) {

    private val apiService = ApiClient.apiService

    suspend fun login(email: String, password: String, deviceName: String): Response<LoginResponse> {
        val loginRequest = LoginRequest(email, password, deviceName)
        return apiService.login(loginRequest)
    }

    suspend fun getFacturasVentas(
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        anio: String? = null,
        mes: String? = null,
        estado: String? = null
    ): Response<FacturasVentasResponse> {
        val authHeader = sessionManager.getAuthHeader() ?: throw IllegalStateException("No hay sesión activa")
        val empresaId = sessionManager.getEmpresaId()

        Log.d("FacturaRepository", "=== CONSULTA FACTURAS VENTAS ===")
        Log.d("FacturaRepository", "Empresa ID seleccionada: $empresaId")
        Log.d("FacturaRepository", "Parámetros: page=$page, perPage=$perPage, estado=$estado")
        Log.d("FacturaRepository", "Header X-Empresa-ID: $empresaId")

        return apiService.getFacturasVentas(
            authorization = authHeader,
            empresaId = empresaId,
            page = page,
            perPage = perPage,
            search = search,
            anio = anio,
            mes = mes,
            estado = estado
        )
    }

    suspend fun getFacturaVenta(id: Int): Response<FacturaVentaDetalleResponse> {
        val authHeader = sessionManager.getAuthHeader() ?: throw IllegalStateException("No hay sesión activa")
        val empresaId = sessionManager.getEmpresaId()

        Log.d("FacturaRepository", "=== CONSULTA DETALLE FACTURA ===")
        Log.d("FacturaRepository", "ID de factura: $id")
        Log.d("FacturaRepository", "Empresa ID seleccionada: $empresaId")
        Log.d("FacturaRepository", "Header X-Empresa-ID: $empresaId")
        Log.d("FacturaRepository", "URL será: mobile/facturas/ventas/$id")

        return apiService.getFacturaVenta(
            id = id,
            authorization = authHeader,
            empresaId = empresaId
        )
    }

    suspend fun getEmpresas(): Response<EmpresasResponse> {
        val authHeader = sessionManager.getAuthHeader() ?: throw IllegalStateException("No hay sesión activa")
        return apiService.getEmpresas(authHeader)
    }

    suspend fun cambiarEmpresa(empresaId: Int): Response<ApiResponse<Any>> {
        val authHeader = sessionManager.getAuthHeader() ?: throw IllegalStateException("No hay sesión activa")
        return apiService.cambiarEmpresa(empresaId, authHeader)
    }

    suspend fun logout(): Response<ApiResponse<Any>> {
        val authHeader = sessionManager.getAuthHeader() ?: throw IllegalStateException("No hay sesión activa")
        return apiService.logout(authHeader)
    }
}
