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
            Toast.makeText(this, "Factura: ${factura.folio}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadResultados() {
        val resultadosJson = intent.getStringExtra("resultados")
        if (resultadosJson != null) {
            try {
                val response = Gson().fromJson(resultadosJson, ConsultaSIIResponse::class.java)
                if (response.success && response.data != null) {
                    tvTitulo.text = "Resultados SII - ${response.message ?: "Consulta completada"}"
                    adapter.updateFacturas(response.data)
                } else {
                    Toast.makeText(this, "No se encontraron datos", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al procesar resultados: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "No se recibieron resultados", Toast.LENGTH_LONG).show()
        }
    }
}
