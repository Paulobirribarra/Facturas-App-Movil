package com.example.test.models

data class User(
    val id: Int,
    val email: String,
    val name: String,
    val role: String,
    val empresas: List<Empresa>
)

data class Empresa(
    val id: Int,
    val razon_social: String,
    val rol: String
)

data class LoginRequest(
    val email: String,
    val password: String,
    val device_name: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User,
    val token: String,
    val token_type: String,
    val expires_at: String
)

data class UserResponse(
    val success: Boolean,
    val user: User
)

data class EmpresasResponse(
    val success: Boolean,
    val empresas: List<Empresa>
)

data class EmpresaResponse(
    val success: Boolean,
    val empresa: Empresa
)
