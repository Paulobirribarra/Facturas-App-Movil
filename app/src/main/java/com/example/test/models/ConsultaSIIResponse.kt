package com.example.test.models

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

// Deserializador personalizado para manejar números que pueden venir como strings vacíos
class SafeDoubleDeserializer : JsonDeserializer<Double> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Double {
        return try {
            when {
                json == null || json.isJsonNull -> 0.0
                json.isJsonPrimitive -> {
                    val value = json.asString.trim()
                    if (value.isEmpty() || value == "null") 0.0 else value.toDouble()
                }
                else -> 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }
}

// Deserializador personalizado para enteros
class SafeIntDeserializer : JsonDeserializer<Int> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Int {
        return try {
            when {
                json == null || json.isJsonNull -> 0
                json.isJsonPrimitive -> {
                    val value = json.asString.trim()
                    if (value.isEmpty() || value == "null") 0 else value.toInt()
                }
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }
}

// Deserializador personalizado para enteros nullable
class SafeNullableIntDeserializer : JsonDeserializer<Int?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Int? {
        return try {
            when {
                json == null || json.isJsonNull -> null
                json.isJsonPrimitive -> {
                    val value = json.asString.trim()
                    if (value.isEmpty() || value == "null") null else value.toInt()
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}

// Modelo principal de respuesta SII corregido según especificación
data class ConsultaSIIResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: SiiData?,

    @SerializedName("almacenamiento")
    val almacenamiento: AlmacenamientoInfo?,

    @SerializedName("timestamp")
    val timestamp: String?
)

// Contenedor de datos SII
data class SiiData(
    @SerializedName("ventas")
    val ventas: VentasData?,

    @SerializedName("compras")
    val compras: ComprasData?
)

// Contenedor de facturas de ventas
data class VentasData(
    @SerializedName("detalleVentas")
    val detalleVentas: List<DetalleVenta>?
)

// Contenedor de facturas de compras
data class ComprasData(
    @SerializedName("detalleCompras")
    val detalleCompras: List<DetalleCompra>?
)

// Detalle completo de factura de venta según especificación
data class DetalleVenta(
    @SerializedName("folio")
    val folio: Int,

    @SerializedName("tipoDte")
    val tipoDte: Int,

    @SerializedName("tipoDTEString")
    val tipoDTEString: String,

    @SerializedName("fechaEmision")
    val fechaEmision: String,

    @SerializedName("fechaRecepcion")
    val fechaRecepcion: String,

    @SerializedName("rutCliente")
    val rutCliente: String,

    @SerializedName("razonSocial")
    val razonSocial: String,

    @SerializedName("montoNeto")
    val montoNeto: Double,

    @SerializedName("montoIva")
    val montoIva: Double,

    @SerializedName("montoTotal")
    val montoTotal: Double,

    @SerializedName("montoExento")
    val montoExento: Double,

    @SerializedName("montoIvaRecuperable")
    val montoIvaRecuperable: Double,

    @SerializedName("tipoVenta")
    val tipoVenta: String,

    @SerializedName("estado")
    val estado: String,

    // Campos detallados de IVA
    @SerializedName("ivaRetenidoTotal")
    val ivaRetenidoTotal: Double,

    @SerializedName("ivaRetenidoParcial")
    val ivaRetenidoParcial: Double,

    @SerializedName("ivaNoRetenido")
    val ivaNoRetenido: Double,

    @SerializedName("ivaPropio")
    val ivaPropio: Double,

    @SerializedName("ivaTerceros")
    val ivaTerceros: Double,

    @SerializedName("ivaFueraPlazo")
    val ivaFueraPlazo: Double,

    // Campos adicionales según especificación
    @SerializedName("rutEmisorLiqFactura")
    val rutEmisorLiqFactura: String?,

    @SerializedName("netoComisionLiqFactura")
    val netoComisionLiqFactura: Double,

    @SerializedName("exentoComisionLiqFactura")
    val exentoComisionLiqFactura: Double,

    @SerializedName("ivaComisionLiqFactura")
    val ivaComisionLiqFactura: Double,

    @SerializedName("creditoEmpresaConstructora")
    val creditoEmpresaConstructora: Double,

    @SerializedName("garantiaDepEnvases")
    val garantiaDepEnvases: Double,

    @SerializedName("montoNoFacturable")
    val montoNoFacturable: Double,

    @SerializedName("folioDocReferencia")
    val folioDocReferencia: Int?,

    @SerializedName("tipoDocReferencia")
    val tipoDocReferencia: Int
)

// Detalle completo de factura de compra según especificación
data class DetalleCompra(
    @SerializedName("folio")
    val folio: Int,

    @SerializedName("tipoDTE")
    val tipoDTE: Int,

    @SerializedName("tipoDTEString")
    val tipoDTEString: String,

    @SerializedName("fechaEmision")
    val fechaEmision: String,

    @SerializedName("fechaRecepcion")
    val fechaRecepcion: String,

    @SerializedName("fechaAcuse")
    val fechaAcuse: String,

    @SerializedName("rutProveedor")
    val rutProveedor: String,

    @SerializedName("razonSocial")
    val razonSocial: String,

    @SerializedName("montoNeto")
    val montoNeto: Double,

    @SerializedName("montoIvaRecuperable")
    val montoIvaRecuperable: Double,

    @SerializedName("montoIvaNoRecuperable")
    val montoIvaNoRecuperable: Double,

    @SerializedName("montoTotal")
    val montoTotal: Double,

    @SerializedName("montoExento")
    val montoExento: Double,

    @SerializedName("codigoIvaNoRecuperable")
    val codigoIvaNoRecuperable: Int?,

    @SerializedName("tipoCompra")
    val tipoCompra: String,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("acuseRecibo")
    val acuseRecibo: String
)

// Información de almacenamiento devuelta por el backend
data class AlmacenamientoInfo(
    @SerializedName("total_procesadas")
    val totalProcesadas: Int,

    @SerializedName("nuevas_insertadas")
    val nuevasInsertadas: Int,

    @SerializedName("actualizadas")
    val actualizadas: Int,

    @SerializedName("errores")
    val errores: Int,

    @SerializedName("detalles_errores")
    val detallesErrores: List<String>?
)

// Modelo de compatibilidad para la UI actual (mantener FacturaSII para el adapter)
data class FacturaSII(
    val folio: String,
    val rutCliente: String,
    val razonSocial: String,
    val fechaEmision: String,
    val montoNeto: Double,
    val montoIva: Double,
    val montoTotal: Double,
    val estado: String
) {
    companion object {
        // Función para convertir DetalleVenta a FacturaSII para compatibilidad
        fun fromDetalleVenta(detalle: DetalleVenta): FacturaSII {
            return FacturaSII(
                folio = detalle.folio.toString(),
                rutCliente = detalle.rutCliente,
                razonSocial = detalle.razonSocial,
                fechaEmision = detalle.fechaEmision,
                montoNeto = detalle.montoNeto,
                montoIva = detalle.montoIva,
                montoTotal = detalle.montoTotal,
                estado = detalle.estado
            )
        }

        // Función para convertir DetalleCompra a FacturaSII para compatibilidad
        fun fromDetalleCompra(detalle: DetalleCompra): FacturaSII {
            return FacturaSII(
                folio = detalle.folio.toString(),
                rutCliente = detalle.rutProveedor,
                razonSocial = detalle.razonSocial,
                fechaEmision = detalle.fechaEmision,
                montoNeto = detalle.montoNeto,
                montoIva = detalle.montoIvaRecuperable,
                montoTotal = detalle.montoTotal,
                estado = detalle.estado
            )
        }
    }
}
