package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.test.viewmodel.LoginViewModel
import com.example.test.network.ApiClient

class LoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar ApiClient con configuración segura
        ApiClient.initialize(this)

        val usernameEditText = findViewById<EditText>(R.id.editTextUsername)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)

        // Función para realizar el login
        val performLogin = {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginViewModel.login(username, password)
            } else {
                Toast.makeText(this, "Por favor ingrese email y contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        // Escucha del botón Enter en el campo de contraseña
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
                performLogin()
                true
            } else {
                false
            }
        }

        // También escuchar Enter en el campo de usuario
        usernameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                passwordEditText.requestFocus()
                true
            } else {
                false
            }
        }

        // Observar el estado del login
        loginViewModel.loginResult.observe(this) { success ->
            if (success) {
                Log.d("LoginActivity", "Login exitoso. Navegando al selector de empresas...")
                val intent = Intent(this, EmpresaSelectorActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        loginViewModel.loading.observe(this, Observer { isLoading ->
            loginButton.isEnabled = !isLoading
            loginButton.text = if (isLoading) "Iniciando sesión..." else "Iniciar Sesión"
        })

        loginViewModel.error.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                loginViewModel.clearError()
            }
        })

        loginButton.setOnClickListener {
            performLogin()
        }
    }
}
