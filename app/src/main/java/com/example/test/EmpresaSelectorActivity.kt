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
import com.example.test.adapters.EmpresasAdapter
import com.example.test.viewmodel.EmpresaSelectorViewModel

class EmpresaSelectorActivity : AppCompatActivity() {

    private val empresaSelectorViewModel: EmpresaSelectorViewModel by viewModels()
    private lateinit var empresasAdapter: EmpresasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EmpresaSelectorActivity", "=== INICIANDO SELECTOR DE EMPRESAS ===")
        setContentView(R.layout.activity_empresa_selector)

        setupViews()
        setupRecyclerView()
        setupObservers()
    }

    private fun setupViews() {
        Log.d("EmpresaSelectorActivity", "Configurando vistas...")

        findViewById<Button>(R.id.buttonLogoutSelector).setOnClickListener {
            Log.d("EmpresaSelectorActivity", "Logout presionado desde selector")
            empresaSelectorViewModel.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        Log.d("EmpresaSelectorActivity", "Configurando RecyclerView de empresas...")

        empresasAdapter = EmpresasAdapter(emptyList()) { empresa ->
            Log.d("EmpresaSelectorActivity", "Usuario seleccionó empresa: ${empresa.razonSocial}")
            empresaSelectorViewModel.seleccionarEmpresa(empresa)
        }

        findViewById<RecyclerView>(R.id.recyclerViewEmpresas).apply {
            layoutManager = LinearLayoutManager(this@EmpresaSelectorActivity)
            adapter = empresasAdapter
        }
    }

    private fun setupObservers() {
        Log.d("EmpresaSelectorActivity", "Configurando observadores...")

        empresaSelectorViewModel.user.observe(this) { user ->
            user?.let {
                Log.d("EmpresaSelectorActivity", "Usuario observado: ${it.name}")
                findViewById<TextView>(R.id.textUserInfo).text = "${it.name} (${it.email})"
            }
        }

        empresaSelectorViewModel.empresas.observe(this) { empresas ->
            Log.d("EmpresaSelectorActivity", "Empresas observadas: ${empresas.size} empresas disponibles")
            empresas.forEach { empresa ->
                Log.d("EmpresaSelectorActivity", "  - ${empresa.razonSocial} (ID: ${empresa.id}, Rol: ${empresa.rol})")
            }
            empresasAdapter.updateEmpresas(empresas)
        }

        empresaSelectorViewModel.loading.observe(this) { isLoading ->
            Log.d("EmpresaSelectorActivity", "Estado de carga: $isLoading")
            findViewById<ProgressBar>(R.id.progressBarEmpresas).visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        empresaSelectorViewModel.error.observe(this) { error ->
            error?.let {
                Log.e("EmpresaSelectorActivity", "Error observado: $it")
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                empresaSelectorViewModel.clearError()
            }
        }

        empresaSelectorViewModel.empresaSeleccionada.observe(this) { seleccionada ->
            if (seleccionada) {
                Log.d("EmpresaSelectorActivity", "Empresa seleccionada exitosamente. Navegando al Dashboard...")
                val intent = Intent(this, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
