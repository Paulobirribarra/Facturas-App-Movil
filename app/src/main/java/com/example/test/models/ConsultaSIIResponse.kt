package com.example.test.models

import com.google.gson.annotations.SerializedName

data class ConsultaSIIResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: List<FacturaSII>?,

    @SerializedName("total")
    val total: Int?
)

data class FacturaSII(
    @SerializedName("folio")
    val folio: String,

    @SerializedName("rut_cliente")
    val rutCliente: String,

    @SerializedName("razon_social")
    val razonSocial: String,

    @SerializedName("fecha_emision")
    val fechaEmision: String,

    @SerializedName("monto_neto")
    val montoNeto: Double,

    @SerializedName("monto_iva")
    val montoIva: Double,

    @SerializedName("monto_total")
    val montoTotal: Double,

    @SerializedName("estado")
    val estado: String
)
