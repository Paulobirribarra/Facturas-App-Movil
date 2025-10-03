package com.example.test.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("empresas")
    val empresas: List<Empresa>
)

data class Empresa(
    @SerializedName("id")
    val id: Int,
    @SerializedName("razon_social")
    val razonSocial: String,
    @SerializedName("rol")
    val rol: String
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("device_name")
    val deviceName: String
)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("user")
    val user: User?,
    @SerializedName("token")
    val token: String?,
    @SerializedName("token_type")
    val tokenType: String?,
    @SerializedName("expires_at")
    val expiresAt: String?
)

data class UserResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("user")
    val user: User
)

data class EmpresasResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("empresas")
    val empresas: List<Empresa>
)

data class EmpresaResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("empresa")
    val empresa: Empresa
)
