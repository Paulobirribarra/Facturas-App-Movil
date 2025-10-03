package com.example.test.network

import com.example.test.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Autenticación
    @POST("mobile/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("mobile/logout")
    suspend fun logout(@Header("Authorization") authorization: String): Response<ApiResponse<Any>>

    @GET("mobile/user")
    suspend fun getUser(@Header("Authorization") authorization: String): Response<UserResponse>

    // Facturas de Ventas
    @GET("mobile/facturas/ventas")
    suspend fun getFacturasVentas(
        @Header("Authorization") authorization: String,
        @Header("X-Empresa-ID") empresaId: Int? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("search") search: String? = null,
        @Query("anio") anio: String? = null,
        @Query("mes") mes: String? = null,
        @Query("estado") estado: String? = null
    ): Response<FacturasVentasResponse>

    @GET("mobile/facturas/ventas/{id}")
    suspend fun getFacturaVenta(
        @Path("id") id: Int,
        @Header("Authorization") authorization: String,
        @Header("X-Empresa-ID") empresaId: Int? = null
    ): Response<FacturaVentaDetalleResponse>

    // Empresas
    @GET("mobile/empresas")
    suspend fun getEmpresas(
        @Header("Authorization") authorization: String
    ): Response<EmpresasResponse>

    @GET("mobile/empresa/actual")
    suspend fun getEmpresaActual(
        @Header("Authorization") authorization: String
    ): Response<EmpresaResponse>

    @POST("mobile/empresa/cambiar/{id}")
    suspend fun cambiarEmpresa(
        @Path("id") empresaId: Int,
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<Any>>

    // Consultas SII - Endpoints reales según documentación
    @GET("mobile/sii/consultar-ventas")
    suspend fun consultarVentasSII(
        @Header("Authorization") authorization: String,
        @Header("X-Empresa-ID") empresaId: Int,
        @Query("mes") mes: Int,
        @Query("anio") anio: Int
    ): Response<ConsultaSIIResponse>

    @GET("mobile/sii/consultar-compras")
    suspend fun consultarComprasSII(
        @Header("Authorization") authorization: String,
        @Header("X-Empresa-ID") empresaId: Int,
        @Query("mes") mes: Int,
        @Query("anio") anio: Int
    ): Response<ConsultaSIIResponse>

    // Validación SII - Endpoints reales
    @POST("mobile/sii/validar-acceso")
    suspend fun validarAccesoSII(
        @Header("Authorization") authorization: String,
        @Header("X-Empresa-ID") empresaId: Int,
        @Body validacionRequest: Any
    ): Response<ApiResponse<Any>>

    @GET("mobile/sii/estado-acceso")
    suspend fun getEstadoAccesoSII(
        @Header("Authorization") authorization: String,
        @Header("X-Empresa-ID") empresaId: Int
    ): Response<ApiResponse<Any>>

    @POST("mobile/sii/revocar-acceso")
    suspend fun revocarAccesoSII(
        @Header("Authorization") authorization: String,
        @Header("X-Empresa-ID") empresaId: Int
    ): Response<ApiResponse<Any>>

    @GET("mobile/sii/resumen-mensual")
    suspend fun resumenMensualSII(
        @Header("Authorization") authorization: String,
        @Header("X-Empresa-ID") empresaId: Int,
        @Query("mes") mes: Int,
        @Query("anio") anio: Int
    ): Response<ConsultaSIIResponse>

    @GET("mobile/sii/estado")
    suspend fun estadoConexionSII(
        @Header("Authorization") authorization: String,
        @Header("X-Empresa-ID") empresaId: Int
    ): Response<ApiResponse<Any>>

    // Métodos anteriores (mantener para compatibilidad)
    @GET("mobile/ventas/{mes}/{anio}")
    suspend fun consultarVentas(
        @Header("Authorization") authorization: String,
        @Header("X-Empresa-ID") empresaId: Int,
        @Path("mes") mes: Int,
        @Path("anio") anio: Int
    ): Response<ConsultaSIIResponse>

    @GET("mobile/compras/{mes}/{anio}")
    suspend fun consultarCompras(
        @Header("Authorization") authorization: String,
        @Header("X-Empresa-ID") empresaId: Int,
        @Path("mes") mes: Int,
        @Path("anio") anio: Int
    ): Response<ConsultaSIIResponse>
}
