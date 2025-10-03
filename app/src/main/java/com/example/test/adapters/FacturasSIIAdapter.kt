package com.example.test.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.models.FacturaSII
import java.text.NumberFormat
import java.util.*

class FacturasSIIAdapter(
    private val onFacturaClick: (FacturaSII) -> Unit
) : RecyclerView.Adapter<FacturasSIIAdapter.ViewHolder>() {

    private var facturas = listOf<FacturaSII>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_factura_sii, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val factura = facturas[position]
        holder.bind(factura)
    }

    override fun getItemCount() = facturas.size

    fun updateFacturas(nuevasFacturas: List<FacturaSII>) {
        facturas = nuevasFacturas
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFolio: TextView = itemView.findViewById(R.id.tvFolioSII)
        private val tvCliente: TextView = itemView.findViewById(R.id.tvClienteSII)
        private val tvMonto: TextView = itemView.findViewById(R.id.tvMontoSII)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvEstadoSII)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFechaSII)

        fun bind(factura: FacturaSII) {
            tvFolio.text = "Folio: ${factura.folio}"
            tvCliente.text = "Cliente: ${factura.razonSocial}"

            val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
            tvMonto.text = "Monto: ${formatter.format(factura.montoTotal)}"

            tvEstado.text = factura.estado
            tvFecha.text = "Fecha: ${factura.fechaEmision}"

            itemView.setOnClickListener {
                onFacturaClick(factura)
            }
        }
    }
}
