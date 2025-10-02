package com.example.test

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.test.viewmodel.FacturaDetalleViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class FacturaDetalleActivity : AppCompatActivity() {

    private val facturaDetalleViewModel: FacturaDetalleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_factura_detalle)

        val facturaId = intent.getIntExtra("factura_id", -1)
        Log.d("FacturaDetalleActivity", "=== INICIANDO DETALLE DE FACTURA ===")
        Log.d("FacturaDetalleActivity", "ID de factura recibido: $facturaId")

        if (facturaId == -1) {
            Toast.makeText(this, "Error: ID de factura no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        setupObservers()

        // Cargar el detalle de la factura
        Log.d("FacturaDetalleActivity", "Cargando detalle de factura ID: $facturaId")
        facturaDetalleViewModel.loadFactura(facturaId)
    }

    private fun setupViews() {
        findViewById<Button>(R.id.buttonVolver).setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        facturaDetalleViewModel.factura.observe(this) { factura ->
            factura?.let {
                Log.d("FacturaDetalleActivity", "=== DATOS RECIBIDOS PARA MOSTRAR ===")
                Log.d("FacturaDetalleActivity", "Factura ID recibida: ${it.id}")
                Log.d("FacturaDetalleActivity", "Folio a mostrar: ${it.folio}")
                Log.d("FacturaDetalleActivity", "Cliente a mostrar: ${it.razonSocialCliente}")
                Log.d("FacturaDetalleActivity", "Contacto nombre: ${it.contactoNombre}")
                Log.d("FacturaDetalleActivity", "Contacto correo: ${it.contactoCorreo}")
                Log.d("FacturaDetalleActivity", "Comentario: ${it.comentario}")
                Log.d("FacturaDetalleActivity", "Número operación: ${it.numeroOperacion}")

                // Información principal
                findViewById<TextView>(R.id.textTipoDocumento).text = it.tipoDteString
                findViewById<TextView>(R.id.textFolioDetalle).text = "Folio: ${it.folio}"

                // Cliente
                findViewById<TextView>(R.id.textClienteDetalle).text = it.razonSocialCliente
                findViewById<TextView>(R.id.textRutDetalle).text = "RUT: ${it.rutCliente}"

                // Contacto - usar los campos directos de la API
                if (!it.contactoNombre.isNullOrBlank()) {
                    findViewById<TextView>(R.id.textContactoDetalle).text = "Contacto: ${it.contactoNombre}"
                    findViewById<TextView>(R.id.textContactoCorreo).text = "Email: ${it.contactoCorreo ?: "No especificado"}"
                } else {
                    findViewById<TextView>(R.id.textContactoDetalle).text = "Sin contacto asignado"
                    findViewById<TextView>(R.id.textContactoCorreo).text = "Email: No disponible"
                }

                // Fechas
                findViewById<TextView>(R.id.textFechaEmision).text =
                    "Fecha Emisión: ${formatDate(it.fechaEmision)}"
                findViewById<TextView>(R.id.textFechaRecepcion).text =
                    "Fecha Recepción: ${formatDate(it.fechaRecepcion)}"
                findViewById<TextView>(R.id.textFechaVencimiento).text =
                    "Fecha Vencimiento: ${it.fechaVencimiento?.let { fecha -> formatDate(fecha) } ?: "No especificada"}"

                // Montos
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
                findViewById<TextView>(R.id.textMontoNeto).text = currencyFormat.format(it.montoNeto)
                findViewById<TextView>(R.id.textMontoIva).text = currencyFormat.format(it.montoIva)
                findViewById<TextView>(R.id.textMontoTotal).text = currencyFormat.format(it.montoTotal)

                // Estado y pago
                findViewById<TextView>(R.id.textEstadoDetalle).text = "Estado: ${it.estado}"
                findViewById<TextView>(R.id.textMetodoPago).text =
                    "Método de Pago: ${it.metodoPago ?: "No especificado"}"
                findViewById<TextView>(R.id.textFechaPago).text =
                    "Fecha de Pago: ${it.fechaPago?.let { fecha -> formatDate(fecha) } ?: "No pagada"}"
                findViewById<TextView>(R.id.textNumeroOperacion).text =
                    "Número Operación: ${it.numeroOperacion ?: "No especificado"}"

                // Comentarios - mostrar solo si existe
                if (!it.comentario.isNullOrBlank()) {
                    findViewById<TextView>(R.id.textComentarioLabel).visibility = View.VISIBLE
                    findViewById<View>(R.id.cardComentario).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.textComentario).text = it.comentario
                } else {
                    findViewById<TextView>(R.id.textComentarioLabel).visibility = View.GONE
                    findViewById<View>(R.id.cardComentario).visibility = View.GONE
                }
            }
        }

        facturaDetalleViewModel.loading.observe(this, Observer { isLoading ->
            findViewById<ProgressBar>(R.id.progressBarDetalle).visibility =
                if (isLoading) View.VISIBLE else View.GONE
        })

        facturaDetalleViewModel.error.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                facturaDetalleViewModel.clearError()
            }
        })
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e2: Exception) {
                dateString
            }
        }
    }
}
