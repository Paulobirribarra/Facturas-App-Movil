package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.test.network.ApiClient
import com.example.test.utils.SessionManager
import com.example.test.models.ConsultaSIIResponse
import kotlinx.coroutines.launch
import com.google.gson.Gson

class ConsultasSIIActivity : AppCompatActivity() {
    private lateinit var spinnerTipoConsulta: Spinner
    private lateinit var npMes: NumberPicker
    private lateinit var npAnio: NumberPicker
    private lateinit var btnConsultar: Button
    private lateinit var sessionManager: SessionManager
    private val apiService by lazy { ApiClient.apiService }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultas_sii)

        spinnerTipoConsulta = findViewById(R.id.spinnerTipoConsulta)
        npMes = findViewById(R.id.npMes)
        npAnio = findViewById(R.id.npAnio)
        btnConsultar = findViewById(R.id.btnConsultar)
        sessionManager = SessionManager(this)

        npMes.minValue = 1
        npMes.maxValue = 12
        npAnio.minValue = 2020
        npAnio.maxValue = 2025
        npAnio.value = 2025

        btnConsultar.setOnClickListener {
            consultarSII()
        }
    }

    private fun consultarSII() {
        val tipoConsulta = spinnerTipoConsulta.selectedItemPosition // 0: ventas, 1: compras
        val mes = npMes.value
        val anio = npAnio.value
        val empresaId = sessionManager.getEmpresaId()
        val token = sessionManager.getToken()

        // Logs detallados para depuración
        Log.d("ConsultasSII", "=== INICIANDO CONSULTA SII ===")
        Log.d("ConsultasSII", "Tipo de consulta: ${if (tipoConsulta == 0) "VENTAS" else "COMPRAS"}")
        Log.d("ConsultasSII", "Mes seleccionado: $mes")
        Log.d("ConsultasSII", "Año seleccionado: $anio")
        Log.d("ConsultasSII", "Empresa ID: $empresaId")
        Log.d("ConsultasSII", "Token presente: ${if (token != null) "SÍ (***${token.takeLast(8)})" else "NO"}")

        if (empresaId == null || token == null) {
            Log.e("ConsultasSII", "ERROR: Faltan datos de sesión - EmpresaID: $empresaId, Token: ${token != null}")
            Toast.makeText(this, "Error: No hay sesión activa", Toast.LENGTH_LONG).show()
            return
        }

        // Ejecutar consulta directamente (la validación SII ya se hizo en Dashboard)
        ejecutarConsultaSII(tipoConsulta, mes, anio, empresaId, token)
    }

    private fun ejecutarConsultaSII(tipoConsulta: Int, mes: Int, anio: Int, empresaId: Int, token: String) {
        // Log del endpoint que se va a llamar
        val endpoint = if (tipoConsulta == 0) {
            "mobile/sii/consultar-ventas"
        } else {
            "mobile/sii/consultar-compras"
        }
        Log.d("ConsultasSII", "Endpoint a llamar: $endpoint")
        Log.d("ConsultasSII", "URL completa: http://192.168.32.1:8000/api/$endpoint")
        Log.d("ConsultasSII", "Headers - Authorization: Bearer ***${token.takeLast(8)}")
        Log.d("ConsultasSII", "Headers - X-Empresa-ID: $empresaId")
        Log.d("ConsultasSII", "Query params - mes: $mes, anio: $anio")

        // Mostrar indicador de carga y deshabilitar botón
        btnConsultar.isEnabled = false
        btnConsultar.text = "Consultando SII... (puede tomar hasta 3 minutos)"

        // Mostrar toast informativo
        Toast.makeText(this, "Consultando datos del SII. Esto puede tomar varios minutos...", Toast.LENGTH_LONG).show()

        lifecycleScope.launch {
            try {
                Log.d("ConsultasSII", "Ejecutando llamada HTTP...")
                Log.d("ConsultasSII", "⚠️ ADVERTENCIA: La consulta SII puede tomar hasta 3 minutos")

                val response = if (tipoConsulta == 0) {
                    apiService.consultarVentasSII(
                        authorization = "Bearer $token",
                        empresaId = empresaId,
                        mes = mes,
                        anio = anio
                    )
                } else {
                    apiService.consultarComprasSII(
                        authorization = "Bearer $token",
                        empresaId = empresaId,
                        mes = mes,
                        anio = anio
                    )
                }

                Log.d("ConsultasSII", "=== RESPUESTA RECIBIDA ===")
                Log.d("ConsultasSII", "Código de respuesta: ${response.code()}")
                Log.d("ConsultasSII", "Es exitosa: ${response.isSuccessful}")
                Log.d("ConsultasSII", "Mensaje: ${response.message()}")

                // Log del cuerpo de la respuesta para depuración
                val responseBody = response.body()
                if (responseBody != null) {
                    Log.d("ConsultasSII", "Success: ${responseBody.success}")
                    Log.d("ConsultasSII", "Message: ${responseBody.message}")
                    Log.d("ConsultasSII", "Data count: ${responseBody.data?.size ?: 0}")
                } else {
                    Log.w("ConsultasSII", "Response body es null")
                }

                // Log del cuerpo de error si existe (limitado para evitar miles de líneas)
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    // Limitar el log del error body para evitar spam
                    val errorSummary = if (errorBody != null && errorBody.contains("<!DOCTYPE html>")) {
                        "Error HTML - Posible error 500 del servidor Laravel"
                    } else {
                        errorBody?.take(500) ?: "Sin error body"
                    }
                    Log.e("ConsultasSII", "Error body: $errorSummary")
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Log.d("ConsultasSII", "✅ Consulta exitosa, navegando a resultados...")
                        val intent = Intent(this@ConsultasSIIActivity, ResultadosSIIActivity::class.java)
                        val gson = Gson()
                        intent.putExtra("resultados", gson.toJson(body))
                        startActivity(intent)
                    } else {
                        Log.e("ConsultasSII", "❌ Consulta falló - Success: false")
                        Toast.makeText(this@ConsultasSIIActivity, "Error: ${body?.message ?: "Sin datos"}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e("ConsultasSII", "❌ Error HTTP ${response.code()}: ${response.message()}")
                    when (response.code()) {
                        401 -> Toast.makeText(this@ConsultasSIIActivity, "Error de autenticación (401) - Token inválido", Toast.LENGTH_LONG).show()
                        403 -> {
                            val errorBody = response.errorBody()?.string()
                            if (errorBody?.contains("SII_ACCESS_REQUIRED") == true) {
                                Toast.makeText(this@ConsultasSIIActivity, "Acceso SII expirado. Regresando al Dashboard...", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@ConsultasSIIActivity, "Error de permisos (403)", Toast.LENGTH_LONG).show()
                            }
                        }
                        404 -> Toast.makeText(this@ConsultasSIIActivity, "Error (404) - Endpoint no encontrado", Toast.LENGTH_LONG).show()
                        500 -> Toast.makeText(this@ConsultasSIIActivity, "Error del servidor (500)", Toast.LENGTH_LONG).show()
                        else -> Toast.makeText(this@ConsultasSIIActivity, "Error en la consulta SII ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ConsultasSII", "❌ EXCEPCIÓN: ${e.message}", e)

                // Manejar diferentes tipos de timeouts
                when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> {
                        Toast.makeText(this@ConsultasSIIActivity,
                            "Timeout: La consulta SII está tomando más tiempo del esperado. Intente nuevamente.",
                            Toast.LENGTH_LONG).show()
                    }
                    e.message?.contains("connect", ignoreCase = true) == true -> {
                        Toast.makeText(this@ConsultasSIIActivity,
                            "Error de conexión. Verifique su conexión a internet.",
                            Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this@ConsultasSIIActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } finally {
                // Restaurar el botón sin importar si fue exitoso o falló
                btnConsultar.isEnabled = true
                btnConsultar.text = "Consultar"
            }
        }
    }
}
