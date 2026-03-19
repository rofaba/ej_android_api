package com.example.simpsons.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.simpsons.data.model.Personaje
import com.example.simpsons.databinding.ActivityDetailBinding
import com.squareup.picasso.Picasso

class DetailActivity : AppCompatActivity() {
       private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se recupera objeto Parcelable desde la MainActivity
        val personaje = intent.getParcelableExtra<Personaje>("PERSONAJE_EXTRA")

        personaje?.let {
            title = it.nombre

            if (it.imagenCompleta.isNotEmpty()) {
                Picasso.get().load(it.imagenCompleta).into(binding.ivPersonajeDetalle)
            }

            binding.tvNombreDetalle.text = it.nombre

            // Validar campos nulos
            val fecha = if (!it.fechaNacimiento.isNullOrEmpty()) it.fechaNacimiento else "No disponible"
            binding.tvFecha.text = "Fecha de nacimiento: $fecha"

            binding.tvGenero.text = "Género: ${it.genero ?: "Desconocido"}"
            binding.tvOcupacion.text = "Ocupación: ${it.ocupacion ?: "Desconocida"}"
            binding.tvEstatus.text = "Estatus: ${it.estado ?: "Desconocido"}"

            // Validar lista de frases vacía
            val frasesTexto = if (!it.frases.isNullOrEmpty()) {
                "Frases:\n" + it.frases.joinToString("\n") { frase -> "• $frase" }
            } else {
                "Frases: No disponibles"
            }
            binding.tvFrases.text = frasesTexto
        }
    }
}