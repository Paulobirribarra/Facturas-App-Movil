package com.example.test.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.test.models.FacturaVentaDetalle
import com.example.test.repository.FacturaRepository
import com.example.test.utils.SessionManager
import kotlinx.coroutines.launch
import android.util.Log

class FacturaDetalleViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val repository = FacturaRepository(sessionManager)

    private val _factura = MutableLiveData<FacturaVentaDetalle?>()
    val factura: LiveData<FacturaVentaDetalle?> = _factura

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadFactura(facturaId: Int) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                Log.d("FacturaDetalleVM", "=== CONSULTANDO DETALLE DE FACTURA ===")
                Log.d("FacturaDetalleVM", "ID de factura solicitado: $facturaId")

                val response = repository.getFacturaVenta(facturaId)

                Log.d("FacturaDetalleVM", "Código de respuesta: ${response.code()}")
                Log.d("FacturaDetalleVM", "Respuesta exitosa: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    response.body()?.let { facturaResponse ->
                        Log.d("FacturaDetalleVM", "=== DATOS RECIBIDOS ===")
                        Log.d("FacturaDetalleVM", "Success: ${facturaResponse.success}")
                        Log.d("FacturaDetalleVM", "Factura ID: ${facturaResponse.factura.id}")
                        Log.d("FacturaDetalleVM", "Folio: ${facturaResponse.factura.folio}")
                        Log.d("FacturaDetalleVM", "Cliente: ${facturaResponse.factura.razonSocialCliente}")
                        Log.d("FacturaDetalleVM", "Tipo DTE: ${facturaResponse.factura.tipoDteString}")
                        Log.d("FacturaDetalleVM", "Monto Total: ${facturaResponse.factura.montoTotal}")

                        _factura.value = facturaResponse.factura
                    }
                } else {
                    Log.e("FacturaDetalleVM", "Error en respuesta: ${response.code()}")
                    _error.value = "Error al cargar factura: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("FacturaDetalleVM", "Error de conexión: ${e.message}", e)
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
