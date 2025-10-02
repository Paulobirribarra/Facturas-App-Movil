package com.example.test.models

import com.google.gson.annotations.SerializedName

data class FacturaVenta(
    val id: Int,
    @SerializedName("numero_factura")
    val numeroFactura: String?,
    @SerializedName("fecha_emision")
    val fechaEmision: String,
    @SerializedName("fecha_vencimiento")
    val fechaVencimiento: String?,
    @SerializedName("rut_cliente")
    val rutCliente: String,
    @SerializedName("razon_social_cliente")
    val razonSocialCliente: String,
    @SerializedName("monto_neto")
    val montoNeto: Double,
    @SerializedName("monto_iva")
    val montoIva: Double,
    @SerializedName("monto_total")
    val montoTotal: Double,
    val pagada: Boolean,
    @SerializedName("estado_text")
    val estadoText: String,
    @SerializedName("tipo_dte")
    val tipoDte: Int,
    val folio: Int,
    @SerializedName("contacto_nombre")
    val contactoNombre: String?
)

data class FacturaVentaDetalle(
    val id: Int,
    val folio: Int,
    @SerializedName("tipo_dte")
    val tipoDte: Int,
    @SerializedName("tipo_dte_string")
    val tipoDteString: String,
    @SerializedName("id_cliente")
    val idCliente: Int,
    @SerializedName("rut_cliente")
    val rutCliente: String,
    @SerializedName("razon_social_cliente")
    val razonSocialCliente: String,
    @SerializedName("id_contacto")
    val idContacto: Int?,
    @SerializedName("fecha_emision")
    val fechaEmision: String,
    @SerializedName("fecha_recepcion")
    val fechaRecepcion: String,
    @SerializedName("fecha_vencimiento")
    val fechaVencimiento: String?,
    @SerializedName("monto_neto")
    val montoNeto: Double,
    @SerializedName("monto_iva")
    val montoIva: Double,
    @SerializedName("monto_total")
    val montoTotal: Double,
    @SerializedName("monto_exento")
    val montoExento: Double,
    @SerializedName("tipo_venta")
    val tipoVenta: String,
    val estado: String,
    val pagada: Boolean,
    @SerializedName("metodo_pago")
    val metodoPago: String?,
    @SerializedName("fecha_pago")
    val fechaPago: String?,
    val comentario: String?,
    @SerializedName("numero_operacion")
    val numeroOperacion: String?,
    @SerializedName("numero_factura")
    val numeroFactura: String?,
    // Campos de contacto directos (como los devuelve la API)
    @SerializedName("contacto_nombre")
    val contactoNombre: String?,
    @SerializedName("contacto_correo")
    val contactoCorreo: String?,
    // Objetos anidados (si existen)
    val cliente: Cliente?,
    val contacto: Contacto?
)

data class Cliente(
    val id: Int,
    val rut: String,
    @SerializedName("razon_social")
    val razonSocial: String,
    val direccion: String?,
    val telefono: String?,
    val correo: String?,
    val encargado: String?,
    val celular: String?
)

data class Contacto(
    val id: Int,
    @SerializedName("id_cliente")
    val idCliente: Int,
    val nombre: String,
    @SerializedName("rut_personal")
    val rutPersonal: String?,
    @SerializedName("correo_principal")
    val correoPrincipal: String?,
    @SerializedName("telefono_fijo")
    val telefonoFijo: String?,
    @SerializedName("telefono_celular_1")
    val telefonoCelular1: String?,
    val sector: String?
)

data class Pagination(
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("last_page")
    val lastPage: Int,
    @SerializedName("per_page")
    val perPage: Int,
    val total: Int,
    val from: Int,
    val to: Int
)

data class FiltersApplied(
    @SerializedName("empresa_id")
    val empresaId: String,
    val search: String?,
    val anio: String?,
    val mes: String?,
    val estado: String?
)
