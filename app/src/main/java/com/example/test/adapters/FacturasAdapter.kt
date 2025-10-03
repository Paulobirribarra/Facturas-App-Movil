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

    override fun getItemCount(): Int {
        Log.d("FacturasAdapter", "getItemCount: ${facturas.size} facturas")
        return facturas.size
    }

    fun updateFacturas(nuevasFacturas: List<FacturaVenta>) {
        facturas = nuevasFacturas
        notifyDataSetChanged()
    }

    class FacturaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFolio: TextView = itemView.findViewById(R.id.tvFolio)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvCliente: TextView = itemView.findViewById(R.id.tvCliente)
        val tvMonto: TextView = itemView.findViewById(R.id.tvMonto)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
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

        holder.tvFolio.text = "Folio: ${factura.folio}"
        holder.tvEstado.text = factura.estadoText
        holder.tvCliente.text = factura.razonSocialCliente

        // Formatear fecha
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(factura.fechaEmision)
            holder.tvFecha.text = date?.let { outputFormat.format(it) } ?: factura.fechaEmision
        } catch (e: Exception) {
            holder.tvFecha.text = factura.fechaEmision
        }

        // Formatear monto
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        holder.tvMonto.text = currencyFormat.format(factura.montoTotal)

        // Color del estado
        when (factura.estadoText.lowercase()) {
            "pendiente" -> {
                holder.tvEstado.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_orange_dark))
            }
            "pagada" -> {
                holder.tvEstado.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            }
            else -> {
                holder.tvEstado.setBackgroundColor(holder.itemView.context.getColor(android.R.color.darker_gray))
            }
        }

        // Click listener
        holder.itemView.setOnClickListener {
            onFacturaClick(factura)
        }
    }
}
