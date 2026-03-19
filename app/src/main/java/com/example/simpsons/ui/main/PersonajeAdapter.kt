package com.example.simpsons.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simpsons.data.model.Personaje
import com.example.simpsons.databinding.ItemPersonajeBinding
import com.squareup.picasso.Picasso

class PersonajeAdapter(
    // Lista actual que se está mostrando en el RecyclerView
    // IMPORTANTE:
    // aquí llega la lista normal, filtrada, de favoritos, etc.
    private var personajes: List<Personaje>,

    // Acción al hacer click normal sobre un item
    // Se define desde MainActivity
    private val onClick: (Personaje) -> Unit,

    // Acción al hacer long click sobre un item
    // También se define desde MainActivity
    private val onLongClick: (Personaje) -> Unit,

    // Conjunto de IDs marcados como favoritos
    // Se usa para decidir si mostrar la estrella o no
    private val favoritos: Set<Int>
) : RecyclerView.Adapter<PersonajeAdapter.ViewHolder>() {

    // ViewHolder: mantiene referencias a las vistas del item
    // Usa ViewBinding con item_personaje.xml
    class ViewHolder(val binding: ItemPersonajeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Infla el layout de cada item del RecyclerView
        // IMPORTANTE EN EXAMEN:
        // si cambias el diseño del item, normalmente tocarás item_personaje.xml
        val binding = ItemPersonajeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Obtiene el personaje correspondiente a la posición actual
        val personaje = personajes[position]

        // Muestra el nombre del personaje en el TextView del item
        // Si mañana piden mostrar otro campo (ocupación, género, edad), toca aquí
        holder.binding.tvNombre.text = personaje.nombre

        // Muestra u oculta la estrella de favorito
        // Si el ID del personaje está en favoritos, la estrella se ve
        // Si no, se oculta
        // IMPORTANTE:
        // si mañana piden otro icono/estado visual, tocarás este bloque
        if (favoritos.contains(personaje.id)) {
            holder.binding.ivFavorito.visibility = View.VISIBLE
        } else {
            holder.binding.ivFavorito.visibility = View.GONE
        }

        // Carga la imagen del personaje con Picasso
        // Usa imagenCompleta, que viene calculada desde Personaje.kt
        // Si mañana cambian la librería (ej. Glide) o la URL de imagen, tocarás aquí o en Personaje.kt
        Picasso.get()
            .load(personaje.imagenCompleta)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.binding.ivPersonaje)

        // Click normal sobre el item
        // Ahora abre el detalle porque MainActivity se lo pasa así
        // Si mañana piden otra acción al pulsar, la lógica principal estará en MainActivity
        holder.itemView.setOnClickListener {
            onClick(personaje)
        }

        // Long click sobre el item
        // Ahora abre el diálogo con Editar / Favorito / Eliminar
        // Si mañana piden quitar o cambiar el long click, revisa este bloque y MainActivity
        holder.itemView.setOnLongClickListener {
            onLongClick(personaje)
            true
        }
    }

    override fun getItemCount(): Int = personajes.size

    fun actualizarLista(nuevaLista: List<Personaje>) {
        // Reemplaza la lista actual por una nueva
        // Se usa cuando:
        // - llegan datos de la API
        // - aplicas búsqueda
        // - muestras favoritos
        // - eliminas personajes
        personajes = nuevaLista

        // Refresca TODO el RecyclerView
        // Si mañana piden optimizar, aquí podrían usar DiffUtil
        // Pero para examen esto está perfecto
        notifyDataSetChanged()
    }
}