package com.example.test.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.models.Empresa

class EmpresasAdapter(
    private var empresas: List<Empresa>,
    private val onEmpresaClick: (Empresa) -> Unit
) : RecyclerView.Adapter<EmpresasAdapter.EmpresaViewHolder>() {

    class EmpresaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textEmpresaNombre: TextView = itemView.findViewById(R.id.textEmpresaNombre)
        val textEmpresaRol: TextView = itemView.findViewById(R.id.textEmpresaRol)
        val textEmpresaId: TextView = itemView.findViewById(R.id.textEmpresaId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_empresa, parent, false)
        return EmpresaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmpresaViewHolder, position: Int) {
        val empresa = empresas[position]

        Log.d("EmpresasAdapter", "Mostrando empresa: ${empresa.razon_social} (ID: ${empresa.id})")

        holder.textEmpresaNombre.text = empresa.razon_social
        holder.textEmpresaRol.text = "Rol: ${empresa.rol}"
        holder.textEmpresaId.text = "ID: ${empresa.id}"

        holder.itemView.setOnClickListener {
            Log.d("EmpresasAdapter", "Empresa seleccionada: ${empresa.razon_social} (ID: ${empresa.id})")
            onEmpresaClick(empresa)
        }
    }

    override fun getItemCount(): Int {
        Log.d("EmpresasAdapter", "getItemCount: ${empresas.size} empresas disponibles")
        return empresas.size
    }

    fun updateEmpresas(nuevasEmpresas: List<Empresa>) {
        Log.d("EmpresasAdapter", "updateEmpresas: Recibidas ${nuevasEmpresas.size} empresas")
        empresas = nuevasEmpresas
        notifyDataSetChanged()
    }
}
