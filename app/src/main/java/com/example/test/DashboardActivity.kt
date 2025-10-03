package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapters.FacturasAdapter
import com.example.test.models.User
import com.example.test.network.ApiClient
import com.example.test.utils.SessionManager
import com.example.test.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private lateinit var facturasAdapter: FacturasAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        sessionManager = SessionManager(this)
        setupViews()
        setupRecyclerView()
        setupObservers()
    }

    private fun setupViews() {
        Log.d("DashboardActivity", "Configurando vistas del dashboard...")

        findViewById<Button>(R.id.buttonTodasFacturas).setOnClickListener {
            Log.d("DashboardActivity", "Botón 'Ver Todas las Facturas' presionado")
            dashboardViewModel.loadAllFacturas()
        }

        findViewById<Button>(R.id.buttonFacturasPagadas).setOnClickListener {
            Log.d("DashboardActivity", "Botón 'Facturas Pagadas' presionado")
            dashboardViewModel.loadFacturasPagadas()
        }

        // Botones de paginación
        findViewById<Button>(R.id.buttonAnterior).setOnClickListener {
            Log.d("DashboardActivity", "Botón 'Página Anterior' presionado")
            dashboardViewModel.loadPreviousPage()
        }

        findViewById<Button>(R.id.buttonSiguiente).setOnClickListener {
            Log.d("DashboardActivity", "Botón 'Página Siguiente' presionado")
            dashboardViewModel.loadNextPage()
        }

        findViewById<Button>(R.id.buttonLogout).setOnClickListener {
            Log.d("DashboardActivity", "Logout presionado desde dashboard")
            dashboardViewModel.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.buttonConsultaSII).setOnClickListener {
            Log.d("DashboardActivity", "Botón 'Consulta SII' presionado")
            // Primero validar acceso SII antes de navegar
            mostrarDialogoValidacionSII()
        }
    }

    private fun setupRecyclerView() {
        facturasAdapter = FacturasAdapter(emptyList()) { factura ->
            // Al hacer clic en una factura, abrir el detalle
            Log.d("DashboardActivity", "=== FACTURA SELECCIONADA ===")
            Log.d("DashboardActivity", "ID de factura seleccionada: ${factura.id}")
            Log.d("DashboardActivity", "Folio mostrado en lista: ${factura.folio}")
            Log.d("DashboardActivity", "Cliente: ${factura.razonSocialCliente}")
            Log.d("DashboardActivity", "Empresa actual: ${sessionManager.getEmpresaId()}")

            // Toast para confirmar que el clic funciona
            Toast.makeText(this, "Abriendo factura ID: ${factura.id}, Folio: ${factura.folio}", Toast.LENGTH_SHORT).show()

            try {
                val intent = Intent(this, FacturaDetalleActivity::class.java)
                intent.putExtra("factura_id", factura.id)
                Log.d("DashboardActivity", "Intent creado, iniciando FacturaDetalleActivity...")
                startActivity(intent)
                Log.d("DashboardActivity", "startActivity ejecutado exitosamente")
            } catch (e: Exception) {
                Log.e("DashboardActivity", "ERROR al abrir FacturaDetalleActivity: ${e.message}", e)
                Toast.makeText(this, "Error al abrir detalle: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<RecyclerView>(R.id.recyclerViewFacturas).apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = facturasAdapter
        }
    }

    private fun setupObservers() {
        dashboardViewModel.user.observe(this) { user ->
            user?.let {
                findViewById<TextView>(R.id.textWelcome).text = "Bienvenido, ${it.name}"
                findViewById<TextView>(R.id.textUserInfo).text = "${it.email} - ${it.role}"
            }
        }

        dashboardViewModel.empresaActual.observe(this) { empresaId ->
            val user = dashboardViewModel.user.value
            val empresa = user?.empresas?.find { it.id == empresaId }
            findViewById<TextView>(R.id.textEmpresaActual).text =
                "Empresa: ${empresa?.razonSocial ?: "No seleccionada"}"

            // Cargar facturas automáticamente cuando se establece la empresa
            if (empresaId != null) {
                Log.d("DashboardActivity", "Empresa establecida (ID: $empresaId), cargando facturas automáticamente...")
                dashboardViewModel.loadAllFacturas()
            }
        }

        // Observar los items del PaginatedViewModel
        dashboardViewModel.items.observe(this) { facturas ->
            facturasAdapter.updateFacturas(facturas)
        }

        // Observar el loading del PaginatedViewModel
        dashboardViewModel.loading.observe(this) { isLoading ->
            // Usar elementos que existen en el layout
            findViewById<Button>(R.id.buttonTodasFacturas).isEnabled = !isLoading
            findViewById<Button>(R.id.buttonFacturasPagadas).isEnabled = !isLoading
        }

        // Observar el error del DashboardViewModel
        dashboardViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                dashboardViewModel.clearError()
            }
        }

        // Observar la paginación del PaginatedViewModel
        dashboardViewModel.pagination.observe(this) { pagination ->
            pagination?.let {
                // Mostrar información de paginación usando elementos que existen
                val layoutPaginacion = findViewById<View>(R.id.layoutPaginacion)
                val buttonAnterior = findViewById<Button>(R.id.buttonAnterior)
                val buttonSiguiente = findViewById<Button>(R.id.buttonSiguiente)
                val textPaginaActual = findViewById<TextView>(R.id.textPaginaActual)

                // Mostrar controles de paginación
                layoutPaginacion.visibility = View.VISIBLE

                // Información de paginación
                textPaginaActual.text = "Página ${it.currentPage} de ${it.lastPage}"

                // Habilitar/deshabilitar botones según la página actual
                buttonAnterior.isEnabled = dashboardViewModel.canLoadPrevious()
                buttonSiguiente.isEnabled = dashboardViewModel.canLoadNext()

                Log.d("DashboardActivity", "Paginación: ${it.currentPage}/${it.lastPage}")
            } ?: run {
                // Ocultar controles si no hay datos de paginación
                findViewById<View>(R.id.layoutPaginacion).visibility = View.GONE
            }
        }
    }

    private fun mostrarDialogoValidacionSII() {
        Log.d("DashboardActivity", "Mostrando diálogo de validación SII...")

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_validacion_sii, null)

        val etClaveSII = dialogView.findViewById<android.widget.EditText>(R.id.etClaveSII)
        val btnValidar = dialogView.findViewById<android.widget.Button>(R.id.btnValidar)

        builder.setView(dialogView)
        builder.setTitle("Acceso a Consultas SII")
        builder.setCancelable(true)
        val dialog = builder.create()

        btnValidar.setOnClickListener {
            val claveSII = etClaveSII.text.toString().trim()

            if (claveSII.isEmpty()) {
                Toast.makeText(this, "Ingrese su clave SII", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("DashboardActivity", "Validando acceso SII para navegar a consultas...")
            validarAccesoSIIParaNavegacion(claveSII, dialog)
        }

        dialog.show()
    }

    private fun validarAccesoSIIParaNavegacion(claveSII: String, dialog: androidx.appcompat.app.AlertDialog) {
        val empresaId = sessionManager.getEmpresaId()
        val token = sessionManager.getToken()

        if (empresaId == null || token == null) {
            Toast.makeText(this, "Error: No hay sesión activa", Toast.LENGTH_LONG).show()
            return
        }

        Log.d("DashboardActivity", "=== VALIDANDO ACCESO SII ===")
        Log.d("DashboardActivity", "Empresa ID: $empresaId")
        Log.d("DashboardActivity", "Token: ***${token.takeLast(8)}")

        val btnValidar = dialog.findViewById<Button>(R.id.btnValidar)

        // Mostrar indicador de carga
        btnValidar?.isEnabled = false
        btnValidar?.text = "Validando..."

        lifecycleScope.launch {
            try {
                val validacionRequest = mapOf("password_sii" to claveSII)
                val response = ApiClient.apiService.validarAccesoSII(
                    authorization = "Bearer $token",
                    empresaId = empresaId,
                    validacionRequest = validacionRequest
                )

                Log.d("DashboardActivity", "Validación SII - Código: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("DashboardActivity", "✅ Validación SII exitosa: ${responseBody?.success}")

                    dialog.dismiss()
                    Toast.makeText(this@DashboardActivity, "Acceso SII validado correctamente", Toast.LENGTH_SHORT).show()

                    // Ahora navegar a la pantalla de consultas
                    val intent = Intent(this@DashboardActivity, ConsultasSIIActivity::class.java)
                    startActivity(intent)

                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DashboardActivity", "❌ Error validando SII: $errorBody")

                    // Restaurar botón
                    btnValidar?.isEnabled = true
                    btnValidar?.text = "Validar"

                    Toast.makeText(this@DashboardActivity, "Error validando clave SII. Verifique su clave.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Excepción validando SII: ${e.message}", e)

                // Restaurar botón
                btnValidar?.isEnabled = true
                btnValidar?.text = "Validar"

                Toast.makeText(this@DashboardActivity, "Error de conexión validando SII", Toast.LENGTH_LONG).show()
            }
        }
    }
}
