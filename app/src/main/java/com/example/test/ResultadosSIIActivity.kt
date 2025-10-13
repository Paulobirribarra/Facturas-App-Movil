package com.example.test

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapters.FacturasSIIAdapter
import com.example.test.models.ConsultaSIIResponse
import com.example.test.models.FacturaSII
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ResultadosSIIActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FacturasSIIAdapter
    private lateinit var tvTitulo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_sii)

        recyclerView = findViewById(R.id.recyclerFacturasSII)
        tvTitulo = findViewById(R.id.tvTituloResultados)

        setupRecyclerView()
        loadResultados()
    }

    private fun setupRecyclerView() {
        adapter = FacturasSIIAdapter { factura ->
            // Click en factura - mostrar detalles
            Toast.makeText(this, "Factura: ${factura.folio} - ${factura.razonSocial}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadResultados() {
        try {
            // ✅ CORREGIDO: Manejar tanto el formato anterior como el nuevo
            val resultadosJson = intent.getStringExtra("resultados")
            val facturasListJson = intent.getStringExtra("facturas_list")

            when {
                // Nuevo formato: lista de facturas procesadas
                facturasListJson != null -> {
                    val gson = Gson()
                    val listType = object : TypeToken<List<FacturaSII>>() {}.type
                    val facturasList: List<FacturaSII> = gson.fromJson(facturasListJson, listType)

                    if (facturasList.isNotEmpty()) {
                        tvTitulo.text = "Resultados SII - ${facturasList.size} facturas encontradas"
                        adapter.updateFacturas(facturasList)

                        // Mostrar información adicional si está disponible
                        if (resultadosJson != null) {
                            val response = gson.fromJson(resultadosJson, ConsultaSIIResponse::class.java)
                            response.almacenamiento?.let { almacenamiento ->
                                val mensaje = "Total procesadas: ${almacenamiento.totalProcesadas}"
                                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        tvTitulo.text = "Resultados SII - Sin facturas"
                        Toast.makeText(this, "No se encontraron facturas para el período consultado", Toast.LENGTH_LONG).show()
                    }
                }

                // Formato anterior: respuesta completa (mantener para compatibilidad)
                resultadosJson != null -> {
                    val gson = Gson()
                    val response = gson.fromJson(resultadosJson, ConsultaSIIResponse::class.java)

                    if (response.success) {
                        // Intentar extraer facturas de la estructura anidada
                        val facturasList = mutableListOf<FacturaSII>()

                        // Procesar ventas si existen
                        response.data?.ventas?.detalleVentas?.forEach { detalle ->
                            facturasList.add(FacturaSII.fromDetalleVenta(detalle))
                        }

                        // Procesar compras si existen
                        response.data?.compras?.detalleCompras?.forEach { detalle ->
                            facturasList.add(FacturaSII.fromDetalleCompra(detalle))
                        }

                        if (facturasList.isNotEmpty()) {
                            tvTitulo.text = "Resultados SII - ${facturasList.size} facturas"
                            adapter.updateFacturas(facturasList)
                        } else {
                            tvTitulo.text = "Resultados SII - ${response.message ?: "Sin datos"}"
                            Toast.makeText(this, "Consulta completada pero sin facturas encontradas", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        tvTitulo.text = "Error en consulta SII"
                        Toast.makeText(this, "Error: ${response.message ?: "Consulta falló"}", Toast.LENGTH_LONG).show()
                    }
                }

                else -> {
                    tvTitulo.text = "Error - Sin datos"
                    Toast.makeText(this, "No se recibieron resultados de la consulta", Toast.LENGTH_LONG).show()
                }
            }

        } catch (e: Exception) {
            tvTitulo.text = "Error procesando resultados"
            Toast.makeText(this, "Error al procesar resultados: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
