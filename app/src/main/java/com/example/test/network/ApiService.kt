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
    ): Response<EmpresaResponse>
}
