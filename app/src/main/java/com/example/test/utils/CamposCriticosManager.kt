package com.example.test.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.test.models.AlmacenamientoInfo

/**
 * Utilidad para manejar la preservación de campos críticos en consultas SII
 * Previene pérdida de datos editados manualmente
 */
class CamposCriticosManager(private val context: Context) {

    /**
     * Verifica si una consulta SII podría sobrescribir datos críticos
     * y muestra advertencia al usuario
     */
    fun verificarYAdvertirSobreescritura(
        callback: (Boolean) -> Unit // true = continuar, false = cancelar
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("⚠️ Advertencia - Consulta SII")
        builder.setMessage(
            "La consulta SII puede actualizar datos de facturas existentes.\n\n" +
            "Si has editado manualmente información como:\n" +
            "• Estados de pago\n" +
            "• Métodos de pago\n" +
            "• Comentarios\n" +
            "• Números de operación\n\n" +
            "Estos cambios podrían perderse.\n\n" +
            "¿Deseas continuar con la consulta?"
        )

        builder.setPositiveButton("Continuar") { _, _ ->
            callback(true)
        }

        builder.setNegativeButton("Cancelar") { _, _ ->
            callback(false)
        }

        builder.setNeutralButton("Más info") { dialog, _ ->
            dialog.dismiss()
            mostrarInformacionDetallada {
                verificarYAdvertirSobreescritura(callback)
            }
        }

        builder.setCancelable(false)
        builder.show()
    }

    /**
     * Muestra información detallada sobre el manejo de campos críticos
     */
    private fun mostrarInformacionDetallada(onClose: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("ℹ️ Campos Críticos - Información")
        builder.setMessage(
            "CAMPOS PRESERVADOS AUTOMÁTICAMENTE:\n\n" +
            "✅ Facturas marcadas como 'pagadas'\n" +
            "✅ Métodos de pago personalizados\n" +
            "✅ Fechas de pago ingresadas\n" +
            "✅ Comentarios y observaciones\n" +
            "✅ Números de operación\n" +
            "✅ Contactos asignados\n\n" +
            "IMPORTANTE:\n" +
            "• Solo se preservan si fueron editados correctamente\n" +
            "• La app web marca las modificaciones automáticamente\n" +
            "• Los datos del SII siempre son oficiales\n\n" +
            "RECOMENDACIÓN:\n" +
            "Si tienes dudas, revisa tus facturas importantes en la app web antes de sincronizar."
        )

        builder.setPositiveButton("Entendido") { _, _ ->
            onClose()
        }

        builder.show()
    }

    /**
     * Procesa y analiza los resultados de almacenamiento después de una consulta SII
     */
    fun procesarResultadosAlmacenamiento(
        almacenamiento: AlmacenamientoInfo,
        onResultado: (String, Boolean) -> Unit // mensaje, esAdvertencia
    ) {
        val mensaje = StringBuilder()
        var hayAdvertencias = false

        // Estadísticas básicas
        mensaje.append("📊 RESULTADOS DE SINCRONIZACIÓN:\n\n")
        mensaje.append("✅ Total procesadas: ${almacenamiento.totalProcesadas}\n")
        mensaje.append("🆕 Nuevas insertadas: ${almacenamiento.nuevasInsertadas}\n")
        mensaje.append("🔄 Actualizadas: ${almacenamiento.actualizadas}\n")

        if (almacenamiento.errores > 0) {
            mensaje.append("❌ Errores: ${almacenamiento.errores}\n")
            hayAdvertencias = true
        }

        // Análisis de preservación
        mensaje.append("\n📋 ANÁLISIS DE CAMPOS CRÍTICOS:\n\n")

        when {
            almacenamiento.actualizadas > 0 -> {
                mensaje.append("⚠️ ${almacenamiento.actualizadas} facturas fueron actualizadas.\n\n")
                mensaje.append("El sistema intentó preservar automáticamente:\n")
                mensaje.append("• Datos de pagos editados manualmente\n")
                mensaje.append("• Comentarios y observaciones\n")
                mensaje.append("• Números de operación\n\n")
                mensaje.append("💡 RECOMENDACIÓN:\n")
                mensaje.append("Revisa las facturas importantes para verificar que los datos críticos se mantuvieron correctos.")
                hayAdvertencias = true
            }
            almacenamiento.nuevasInsertadas == almacenamiento.totalProcesadas -> {
                mensaje.append("✅ Solo se insertaron facturas nuevas.\n")
                mensaje.append("No hay riesgo de pérdida de datos editados.")
            }
            else -> {
                mensaje.append("✅ Sincronización sin conflictos detectados.")
            }
        }

        // Errores específicos
        almacenamiento.detallesErrores?.takeIf { it.isNotEmpty() }?.let { errores ->
            mensaje.append("\n🔍 DETALLES DE ERRORES:\n")
            errores.take(3).forEach { error ->
                mensaje.append("• $error\n")
            }
            if (errores.size > 3) {
                mensaje.append("• ... y ${errores.size - 3} errores más\n")
            }
            hayAdvertencias = true
        }

        onResultado(mensaje.toString(), hayAdvertencias)
    }

    /**
     * Muestra diálogo con los resultados del procesamiento
     */
    fun mostrarResultadosDetallados(
        almacenamiento: AlmacenamientoInfo,
        onContinuar: () -> Unit
    ) {
        procesarResultadosAlmacenamiento(almacenamiento) { mensaje, esAdvertencia ->
            val builder = AlertDialog.Builder(context)

            if (esAdvertencia) {
                builder.setTitle("⚠️ Consulta SII Completada - Revisar")
                builder.setIcon(android.R.drawable.ic_dialog_alert)
            } else {
                builder.setTitle("✅ Consulta SII Completada")
                builder.setIcon(android.R.drawable.ic_dialog_info)
            }

            builder.setMessage(mensaje)

            if (esAdvertencia) {
                builder.setPositiveButton("Revisar Facturas") { _, _ ->
                    // TODO: Implementar navegación a lista de facturas actualizadas
                    onContinuar()
                }
                builder.setNegativeButton("Continuar") { _, _ ->
                    onContinuar()
                }
            } else {
                builder.setPositiveButton("Continuar") { _, _ ->
                    onContinuar()
                }
            }

            builder.show()
        }
    }

    /**
     * Crea un resumen rápido para mostrar en Toast
     */
    fun crearResumenRapido(almacenamiento: AlmacenamientoInfo): String {
        return when {
            almacenamiento.errores > 0 -> {
                "⚠️ Procesadas: ${almacenamiento.totalProcesadas}, Errores: ${almacenamiento.errores}"
            }
            almacenamiento.actualizadas > 0 -> {
                "🔄 Nuevas: ${almacenamiento.nuevasInsertadas}, Actualizadas: ${almacenamiento.actualizadas}"
            }
            else -> {
                "✅ ${almacenamiento.totalProcesadas} facturas procesadas correctamente"
            }
        }
    }
}
