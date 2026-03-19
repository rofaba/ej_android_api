package com.example.simpsons.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simpsons.data.model.Personaje
import com.example.simpsons.databinding.ItemPersonajeBinding
import com.squareup.picasso.Picasso

class PersonajeAdapter(
    private var personajes: List<Personaje>,
    private val onClick: (Personaje) -> Unit,
    private val onLongClick: (Personaje) -> Unit,
    private val favoritos: Set<Int>
) : RecyclerView.Adapter<PersonajeAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPersonajeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPersonajeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val personaje = personajes[position]

        holder.binding.tvNombre.text = personaje.nombre

        if (favoritos.contains(personaje.id)) {
            holder.binding.ivFavorito.visibility = View.VISIBLE
        } else {
            holder.binding.ivFavorito.visibility = View.GONE
        }

        Picasso.get()
            .load(personaje.imagenCompleta)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.binding.ivPersonaje)

        holder.itemView.setOnClickListener {
            onClick(personaje)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(personaje)
            true
        }
    }

    override fun getItemCount(): Int = personajes.size

    fun actualizarLista(nuevaLista: List<Personaje>) {
        personajes = nuevaLista
        notifyDataSetChanged()
    }
}