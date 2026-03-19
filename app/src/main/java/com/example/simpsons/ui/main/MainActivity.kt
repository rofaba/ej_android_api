package com.example.simpsons.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simpsons.R
import com.example.simpsons.data.model.Personaje
import com.example.simpsons.data.network.SimpsonsApiService
import com.example.simpsons.data.repository.SimpsonsRepository
import com.example.simpsons.databinding.ActivityMainBinding
import com.example.simpsons.ui.detail.DetailActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SimpsonsViewModel
    private lateinit var adapter: PersonajeAdapter

    private var listaOriginal: List<Personaje> = emptyList()
    private val favoritos = mutableSetOf<Int>()
    private var soloFavoritos = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "The Simpsons"

        configurarRecyclerView()
        configurarViewModel()
        configurarSwipeRefresh()
        configurarBusqueda()
        observarViewModel()
    }

    private fun configurarRecyclerView() {
        adapter = PersonajeAdapter(
            emptyList(),
            { personaje ->
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("PERSONAJE_EXTRA", personaje)
                startActivity(intent)
            },
            { personaje ->
                mostrarOpciones(personaje)
            },
            favoritos
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun configurarViewModel() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://thesimpsonsapi.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(SimpsonsApiService::class.java)
        val repository = SimpsonsRepository(api)
        val factory = SimpsonsViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[SimpsonsViewModel::class.java]
    }

    private fun configurarSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.cargarDatos()
        }
    }

    private fun configurarBusqueda() {
        val listener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarLista(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarLista(newText)
                return true
            }
        }

        binding.searchView.setOnQueryTextListener(listener)
    }

    private fun filtrarLista(texto: String?) {
        val query = texto.orEmpty().trim()

        if (query.isEmpty()) {
            adapter.actualizarLista(listaOriginal)

            if (listaOriginal.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.tvMensaje.visibility = View.VISIBLE
                binding.tvMensaje.text = "No hay personajes"
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.tvMensaje.visibility = View.GONE
            }
            return
        }

        val listaFiltrada = listaOriginal.filter { personaje ->
            personaje.nombre.contains(query, ignoreCase = true)
            // Variante examen:
            // || personaje.ocupacion.contains(query, ignoreCase = true)
            // || personaje.genero.contains(query, ignoreCase = true)
        }

        adapter.actualizarLista(listaFiltrada)

        if (listaFiltrada.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.tvMensaje.visibility = View.VISIBLE
            binding.tvMensaje.text = "No se encontraron resultados"
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.tvMensaje.visibility = View.GONE
        }
    }

    private fun observarViewModel() {
        viewModel.personajes.observe(this) { lista ->
            listaOriginal = lista

            val textoActual = binding.searchView.query?.toString().orEmpty()
            filtrarLista(textoActual)
        }

        viewModel.estadoCarga.observe(this) { estado ->
            binding.swipeRefresh.isRefreshing = false

            when (estado) {
                is SimpsonsViewModel.EstadoUI.Cargando -> {
                    if (!binding.swipeRefresh.isRefreshing) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                    binding.tvMensaje.visibility = View.GONE
                }

                is SimpsonsViewModel.EstadoUI.Exito -> {
                    binding.progressBar.visibility = View.GONE
                    if (listaOriginal.isNotEmpty()) {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.tvMensaje.visibility = View.GONE
                    }
                }

                is SimpsonsViewModel.EstadoUI.Vacio -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.tvMensaje.visibility = View.VISIBLE
                    binding.tvMensaje.text = "No hay personajes disponibles"
                }

                is SimpsonsViewModel.EstadoUI.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.tvMensaje.visibility = View.VISIBLE
                    binding.tvMensaje.text = "Error: ${estado.mensaje}"
                }
            }
        }
    }

    private fun mostrarOpciones(personaje: Personaje) {
        val opciones = arrayOf("Editar", "Favorito", "Eliminar")

        AlertDialog.Builder(this)
            .setTitle(personaje.nombre)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> editarPersonaje(personaje)
                    1 -> marcarFavorito(personaje)
                    2 -> eliminarPersonaje(personaje)
                }
            }
            .show()
    }

    private fun editarPersonaje(personaje: Personaje) {
        val editText = EditText(this)
        editText.setText(personaje.nombre)
        editText.setSelection(editText.text.length)

        AlertDialog.Builder(this)
            .setTitle("Editar nombre")
            .setMessage("Modifica el nombre del personaje")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = editText.text.toString().trim()

                if (nuevoNombre.isEmpty()) {
                    Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                listaOriginal = listaOriginal.map {
                    if (it.id == personaje.id) {
                        it.copy(nombre = nuevoNombre)
                    } else {
                        it
                    }
                }

                val textoActual = binding.searchView.query?.toString().orEmpty()
                filtrarLista(textoActual)

                Toast.makeText(this, "Nombre actualizado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun marcarFavorito(personaje: Personaje) {
        if (favoritos.contains(personaje.id)) {
            favoritos.remove(personaje.id)
            Toast.makeText(this, "${personaje.nombre} quitado de favoritos", Toast.LENGTH_SHORT).show()
        } else {
            favoritos.add(personaje.id)
            Toast.makeText(this, "${personaje.nombre} añadido a favoritos", Toast.LENGTH_SHORT).show()
        }

        adapter.notifyDataSetChanged()
    }

    private fun eliminarPersonaje(personaje: Personaje) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Seguro que quieres eliminar a ${personaje.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                listaOriginal = listaOriginal.filter { it.id != personaje.id }
                favoritos.remove(personaje.id)

                val textoActual = binding.searchView.query?.toString().orEmpty()
                filtrarLista(textoActual)

                Toast.makeText(this, "Personaje eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }
    private fun mostrarSoloFavoritos() {
        val listaFiltrada = listaOriginal.filter { personaje ->
            favoritos.contains(personaje.id)
        }

        adapter.actualizarLista(listaFiltrada)

        if (listaFiltrada.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.tvMensaje.visibility = View.VISIBLE
            binding.tvMensaje.text = "No hay favoritos"
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.tvMensaje.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        menu?.findItem(R.id.action_refresh)?.icon?.setTint(getColor(android.R.color.holo_purple))
        menu?.findItem(R.id.action_clear)?.icon?.setTint(getColor(android.R.color.holo_purple))

        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_refresh -> {
                viewModel.cargarDatos()
                true
            }

            R.id.action_clear -> {
                binding.searchView.setQuery("", false)
                filtrarLista("")
                true
            }

            // Favorito con Toggle option
            R.id.action_favoritos -> {
                soloFavoritos = !soloFavoritos

                if (soloFavoritos) {
                    mostrarSoloFavoritos()
                } else {
                    adapter.actualizarLista(listaOriginal)
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
}