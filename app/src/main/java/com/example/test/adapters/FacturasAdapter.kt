package com.example.test.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.models.FacturaVenta
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class FacturasAdapter(
    private var facturas: List<FacturaVenta>,
    private val onFacturaClick: (FacturaVenta) -> Unit
) : RecyclerView.Adapter<FacturasAdapter.FacturaViewHolder>() {

    class FacturaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textFolio: TextView = itemView.findViewById(R.id.textFolio)
        val textEstado: TextView = itemView.findViewById(R.id.textEstado)
        val textCliente: TextView = itemView.findViewById(R.id.textCliente)
        val textRut: TextView = itemView.findViewById(R.id.textRut)
        val textFecha: TextView = itemView.findViewById(R.id.textFecha)
        val textMonto: TextView = itemView.findViewById(R.id.textMonto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_factura, parent, false)
        return FacturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {
        val factura = facturas[position]

        // Log para depuración
        Log.d("FacturasAdapter", "Mostrando factura ${position + 1}: Folio ${factura.folio}")

        holder.textFolio.text = "Folio: ${factura.folio}"
        holder.textEstado.text = factura.estadoText
        holder.textCliente.text = factura.razonSocialCliente
        holder.textRut.text = "RUT: ${factura.rutCliente}"

        // Formatear fecha
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(factura.fechaEmision)
            holder.textFecha.text = date?.let { outputFormat.format(it) } ?: factura.fechaEmision
        } catch (e: Exception) {
            holder.textFecha.text = factura.fechaEmision
        }

        // Formatear monto
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        holder.textMonto.text = currencyFormat.format(factura.montoTotal)

        // Color del estado
        when (factura.estadoText.lowercase()) {
            "pendiente" -> {
                holder.textEstado.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_orange_dark))
            }
            "pagada" -> {
                holder.textEstado.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            }
            else -> {
                holder.textEstado.setBackgroundColor(holder.itemView.context.getColor(android.R.color.darker_gray))
            }
        }

        holder.itemView.setOnClickListener {
            onFacturaClick(factura)
        }
    }

    override fun getItemCount(): Int {
        Log.d("FacturasAdapter", "getItemCount: ${facturas.size} facturas")
        return facturas.size
    }

    fun updateFacturas(nuevasFacturas: List<FacturaVenta>) {
        Log.d("FacturasAdapter", "updateFacturas: Recibidas ${nuevasFacturas.size} facturas")
        facturas = nuevasFacturas
        notifyDataSetChanged()
    }
}
