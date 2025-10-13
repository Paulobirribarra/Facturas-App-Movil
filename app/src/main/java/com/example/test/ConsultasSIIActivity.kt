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
import com.example.test.utils.SiiAuthManager
import com.example.test.utils.CamposCriticosManager
import com.example.test.models.ConsultaSIIResponse
import com.example.test.models.FacturaSII
import kotlinx.coroutines.launch
import com.google.gson.Gson

class ConsultasSIIActivity : AppCompatActivity() {
    private lateinit var spinnerTipoConsulta: Spinner
    private lateinit var npMes: NumberPicker
    private lateinit var npAnio: NumberPicker
    private lateinit var btnConsultar: Button
    private lateinit var sessionManager: SessionManager
    private lateinit var siiAuthManager: SiiAuthManager
    private lateinit var camposCriticosManager: CamposCriticosManager
    private val apiService by lazy { ApiClient.apiService }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultas_sii)

        spinnerTipoConsulta = findViewById(R.id.spinnerTipoConsulta)
        npMes = findViewById(R.id.npMes)
        npAnio = findViewById(R.id.npAnio)
        btnConsultar = findViewById(R.id.btnConsultar)
        sessionManager = SessionManager(this)
        siiAuthManager = SiiAuthManager(this)
        camposCriticosManager = CamposCriticosManager(this)

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

        // ✅ CRÍTICO: Verificar validación SII antes de consultar
        if (siiAuthManager.needsSiiValidation(empresaId)) {
            Log.e("ConsultasSII", "❌ Acceso SII expirado o no válido")
            Toast.makeText(this, "Su acceso SII ha expirado. Regresando al dashboard para revalidar...", Toast.LENGTH_LONG).show()
            finish() // Regresar al dashboard para revalidar
            return
        }

        val remainingMinutes = siiAuthManager.getRemainingMinutes(empresaId)
        Log.d("ConsultasSII", "✅ Acceso SII válido - $remainingMinutes minutos restantes")

        // ✅ NUEVO: Advertir sobre posible pérdida de campos críticos
        camposCriticosManager.verificarYAdvertirSobreescritura { continuar ->
            if (continuar) {
                // Usuario confirmó que quiere continuar
                ejecutarConsultaSII(tipoConsulta, mes, anio, empresaId, token)
            } else {
                // Usuario canceló la consulta
                Log.d("ConsultasSII", "Usuario canceló consulta para evitar pérdida de datos críticos")
                Toast.makeText(this, "Consulta cancelada por seguridad", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun ejecutarConsultaSII(tipoConsulta: Int, mes: Int, anio: Int, empresaId: Int, token: String) {
        // ✅ CORREGIDO: Usar endpoints según especificación
        val endpoint = if (tipoConsulta == 0) {
            "mobile/ventas/$mes/$anio"
        } else {
            "mobile/compras/$mes/$anio"
        }

        Log.d("ConsultasSII", "Endpoint: $endpoint")
        Log.d("ConsultasSII", "Headers - Authorization: Bearer ***${token.takeLast(8)}")
        Log.d("ConsultasSII", "Headers - X-Empresa-ID: $empresaId")

        // Mostrar indicador de carga y deshabilitar botón
        btnConsultar.isEnabled = false
        btnConsultar.text = "Consultando SII... (puede tomar hasta 3 minutos)"

        Toast.makeText(this, "Consultando datos del SII. Esto puede tomar varios minutos...", Toast.LENGTH_LONG).show()

        lifecycleScope.launch {
            try {
                Log.d("ConsultasSII", "Ejecutando consulta SII...")

                // ✅ CORREGIDO: Usar endpoints con parámetros correctos
                val response = if (tipoConsulta == 0) {
                    apiService.consultarVentasSII(
                        mes = mes,
                        anio = anio,
                        authorization = "Bearer $token",
                        empresaId = empresaId
                    )
                } else {
                    apiService.consultarComprasSII(
                        mes = mes,
                        anio = anio,
                        authorization = "Bearer $token",
                        empresaId = empresaId
                    )
                }

                Log.d("ConsultasSII", "=== RESPUESTA RECIBIDA ===")
                Log.d("ConsultasSII", "Código: ${response.code()}")
                Log.d("ConsultasSII", "Exitosa: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("ConsultasSII", "Body success: ${body?.success}")
                    Log.d("ConsultasSII", "Body message: ${body?.message}")

                    if (body?.success == true) {
                        // ✅ Procesar respuesta exitosa
                        val facturasList = procesarRespuestaSII(body, tipoConsulta)

                        if (facturasList.isNotEmpty()) {
                            Log.d("ConsultasSII", "✅ Consulta exitosa - ${facturasList.size} facturas procesadas")

                            // Mostrar información de almacenamiento si está disponible
                            body.almacenamiento?.let { almacenamiento ->
                                val mensaje = "Procesadas: ${almacenamiento.totalProcesadas}, " +
                                            "Nuevas: ${almacenamiento.nuevasInsertadas}, " +
                                            "Actualizadas: ${almacenamiento.actualizadas}"
                                Log.d("ConsultasSII", "Almacenamiento: $mensaje")
                                Toast.makeText(this@ConsultasSIIActivity, mensaje, Toast.LENGTH_SHORT).show()
                            }

                            // Navegar a resultados
                            navegarAResultados(body, facturasList)
                        } else {
                            Log.w("ConsultasSII", "⚠️ Consulta exitosa pero sin facturas")
                            Toast.makeText(this@ConsultasSIIActivity, "Consulta exitosa pero no se encontraron facturas para el período seleccionado", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e("ConsultasSII", "❌ Consulta falló - Success: false")
                        Toast.makeText(this@ConsultasSIIActivity, "Error: ${body?.message ?: "Sin datos"}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // ✅ Manejo simplificado de errores
                    val errorBody = response.errorBody()?.string()
                    Log.e("ConsultasSII", "❌ HTTP ${response.code()}: ${response.message()}")

                    if (errorBody != null && errorBody.length < 1000) {
                        Log.e("ConsultasSII", "Error body: $errorBody")
                    }

                    manejarErrorHTTP(response.code(), errorBody)
                }
            } catch (e: Exception) {
                Log.e("ConsultasSII", "❌ Excepción: ${e.message}", e)
                manejarExcepcion(e)
            } finally {
                // Restaurar el botón
                btnConsultar.isEnabled = true
                btnConsultar.text = "Consultar"
            }
        }
    }

    /**
     * ✅ Navega a resultados mostrando análisis de campos críticos
     */
    private fun navegarAResultados(body: ConsultaSIIResponse, facturasList: List<FacturaSII>) {
        // ✅ NUEVO: Analizar y mostrar resultados de campos críticos
        body.almacenamiento?.let { almacenamiento ->
            camposCriticosManager.mostrarResultadosDetallados(almacenamiento) {
                // Continuar con la navegación después de mostrar el análisis
                navegarAResultadosInterno(body, facturasList)
            }
        } ?: run {
            // Si no hay información de almacenamiento, navegar directamente
            navegarAResultadosInterno(body, facturasList)
        }
    }

    /**
     * ✅ Navegación interna a resultados
     */
    private fun navegarAResultadosInterno(body: ConsultaSIIResponse, facturasList: List<FacturaSII>) {
        Log.d("ConsultasSII", "Navegando a ResultadosSIIActivity con ${facturasList.size} facturas")

        val intent = Intent(this, ResultadosSIIActivity::class.java)
        val gson = Gson()

        // Crear respuesta compatible para ResultadosSIIActivity
        val responseCompatible = ConsultaSIIResponse(
            success = true,
            message = body.message,
            data = null,
            almacenamiento = body.almacenamiento,
            timestamp = body.timestamp
        )

        intent.putExtra("resultados", gson.toJson(responseCompatible))
        intent.putExtra("facturas_list", gson.toJson(facturasList))

        // ✅ NUEVO: Agregar información adicional para análisis
        body.almacenamiento?.let { almacenamiento ->
            intent.putExtra("resumen_rapido", camposCriticosManager.crearResumenRapido(almacenamiento))
        }

        startActivity(intent)
    }

    /**
     * ✅ NUEVO: Procesa la estructura anidada de datos SII según especificación
     */
    private fun procesarRespuestaSII(response: ConsultaSIIResponse, tipoConsulta: Int): List<FacturaSII> {
        val facturasList = mutableListOf<FacturaSII>()

        try {
            when (tipoConsulta) {
                0 -> { // Ventas
                    response.data?.ventas?.detalleVentas?.forEach { detalle ->
                        val factura = FacturaSII.fromDetalleVenta(detalle)
                        facturasList.add(factura)
                    }
                    Log.d("ConsultasSII", "Procesadas ${facturasList.size} facturas de venta")
                }
                1 -> { // Compras
                    response.data?.compras?.detalleCompras?.forEach { detalle ->
                        val factura = FacturaSII.fromDetalleCompra(detalle)
                        facturasList.add(factura)
                    }
                    Log.d("ConsultasSII", "Procesadas ${facturasList.size} facturas de compra")
                }
            }
        } catch (e: Exception) {
            Log.e("ConsultasSII", "Error procesando respuesta SII: ${e.message}", e)
        }

        return facturasList
    }

    /**
     * ✅ MEJORADO: Manejo específico de errores HTTP
     */
    private fun manejarErrorHTTP(codigo: Int, errorBody: String?) {
        when (codigo) {
            401 -> {
                Toast.makeText(this, "Error de autenticación (401) - Token inválido", Toast.LENGTH_LONG).show()
                // Podríamos redirigir al login aquí
            }
            403 -> {
                if (errorBody?.contains("SII_ACCESS_REQUIRED") == true) {
                    Toast.makeText(this, "Acceso SII expirado. Regresando al Dashboard para revalidar...", Toast.LENGTH_SHORT).show()
                    siiAuthManager.clearValidation() // Limpiar validación expirada
                    finish()
                } else {
                    Toast.makeText(this, "Error de permisos (403)", Toast.LENGTH_LONG).show()
                }
            }
            404 -> Toast.makeText(this, "Error (404) - Endpoint no encontrado. Verifique la configuración del servidor.", Toast.LENGTH_LONG).show()
            500 -> Toast.makeText(this, "Error del servidor (500) - Problema en el backend", Toast.LENGTH_LONG).show()
            else -> Toast.makeText(this, "Error en la consulta SII: HTTP $codigo", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * ✅ MEJORADO: Manejo específico de excepciones
     */
    private fun manejarExcepcion(e: Exception) {
        when {
            e.message?.contains("timeout", ignoreCase = true) == true -> {
                Toast.makeText(this, "Timeout: La consulta SII tardó más de lo esperado. El servidor puede estar procesando muchas consultas.", Toast.LENGTH_LONG).show()
            }
            e.message?.contains("connect", ignoreCase = true) == true -> {
                Toast.makeText(this, "Error de conexión. Verifique su conexión a internet y que el servidor esté disponible.", Toast.LENGTH_LONG).show()
            }
            e.message?.contains("JSON", ignoreCase = true) == true -> {
                Toast.makeText(this, "Error procesando respuesta del servidor. Formato de datos inesperado.", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
