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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapters.FacturasAdapter
import com.example.test.models.User
import com.example.test.utils.SessionManager
import com.example.test.viewmodel.DashboardViewModel

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

        findViewById<Button>(R.id.buttonVerFacturas).setOnClickListener {
            Log.d("DashboardActivity", "Botón 'Ver Todas las Facturas' presionado")
            dashboardViewModel.loadAllFacturas()
        }

        findViewById<Button>(R.id.buttonFacturasPendientes).setOnClickListener {
            Log.d("DashboardActivity", "Botón 'Facturas Pendientes' presionado")
            dashboardViewModel.loadFacturasPendientes()
        }

        findViewById<Button>(R.id.buttonFacturasPagadas).setOnClickListener {
            Log.d("DashboardActivity", "Botón 'Facturas Pagadas' presionado")
            dashboardViewModel.loadFacturasPagadas()
        }

        // Botones de paginación
        findViewById<Button>(R.id.buttonCargarMas).setOnClickListener {
            Log.d("DashboardActivity", "Botón 'Cargar Más' presionado")
            dashboardViewModel.loadNextPage()
        }

        findViewById<Button>(R.id.buttonPaginaAnterior).setOnClickListener {
            Log.d("DashboardActivity", "Botón 'Página Anterior' presionado")
            dashboardViewModel.loadPreviousPage()
        }

        findViewById<Button>(R.id.buttonPaginaSiguiente).setOnClickListener {
            Log.d("DashboardActivity", "Botón 'Página Siguiente' presionado")
            dashboardViewModel.loadNextPage()
        }

        findViewById<Button>(R.id.buttonCambiarEmpresa).setOnClickListener {
            Log.d("DashboardActivity", "Botón 'Cambiar Empresa' presionado")
            // Navegar de vuelta al selector de empresas sin cerrar sesión
            val intent = Intent(this, EmpresaSelectorActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.buttonLogout).setOnClickListener {
            Log.d("DashboardActivity", "Logout presionado desde dashboard")
            dashboardViewModel.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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
                "Empresa: ${empresa?.razon_social ?: "No seleccionada"}"

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
            findViewById<ProgressBar>(R.id.progressBar).visibility =
                if (isLoading) View.VISIBLE else View.GONE

            // Deshabilitar botones mientras carga
            findViewById<Button>(R.id.buttonVerFacturas).isEnabled = !isLoading
            findViewById<Button>(R.id.buttonFacturasPendientes).isEnabled = !isLoading
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
                // Mostrar información de paginación
                val layoutPaginacion = findViewById<View>(R.id.layoutPaginacion)
                val buttonAnterior = findViewById<Button>(R.id.buttonPaginaAnterior)
                val buttonSiguiente = findViewById<Button>(R.id.buttonPaginaSiguiente)
                val buttonCargarMas = findViewById<Button>(R.id.buttonCargarMas)
                val textPaginaInfo = findViewById<TextView>(R.id.textPaginaInfo)

                // Mostrar controles de paginación
                layoutPaginacion.visibility = View.VISIBLE

                // Información simple y compacta
                val totalFacturas = dashboardViewModel.getCurrentItemsCount()
                textPaginaInfo.text = "Página ${it.currentPage} de ${it.lastPage} • ${totalFacturas} de ${it.total} facturas"

                // Habilitar/deshabilitar botones según la página actual
                buttonAnterior.isEnabled = dashboardViewModel.canLoadPrevious()
                buttonSiguiente.isEnabled = dashboardViewModel.canLoadNext()
                buttonCargarMas.isEnabled = dashboardViewModel.canLoadNext()

                // Texto simple del botón "Cargar Más"
                buttonCargarMas.text = if (dashboardViewModel.canLoadNext()) "Cargar Más" else "Todas cargadas"

                Log.d("DashboardActivity", "Paginación: ${it.currentPage}/${it.lastPage} - Mostrando $totalFacturas de ${it.total}")
            } ?: run {
                // Ocultar controles si no hay datos de paginación
                findViewById<View>(R.id.layoutPaginacion).visibility = View.GONE
            }
        }

        // Observar loadingMore para deshabilitar botones mientras carga más datos
        dashboardViewModel.loadingMore.observe(this) { isLoadingMore ->
            findViewById<Button>(R.id.buttonCargarMas).isEnabled = !isLoadingMore && dashboardViewModel.canLoadNext()
            findViewById<Button>(R.id.buttonPaginaSiguiente).isEnabled = !isLoadingMore && dashboardViewModel.canLoadNext()
            findViewById<Button>(R.id.buttonPaginaAnterior).isEnabled = !isLoadingMore && dashboardViewModel.canLoadPrevious()

            // Cambiar texto mientras carga
            if (isLoadingMore) {
                findViewById<Button>(R.id.buttonCargarMas).text = "Cargando..."
            }
        }
    }
}
