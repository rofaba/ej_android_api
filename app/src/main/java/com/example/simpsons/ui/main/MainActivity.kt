package com.example.simpsons.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

    // Binding de la pantalla principal (activity_main.xml)
    private lateinit var binding: ActivityMainBinding

    // ViewModel: gestiona carga de datos y estados de UI
    private lateinit var viewModel: SimpsonsViewModel

    // Adapter del RecyclerView
    private lateinit var adapter: PersonajeAdapter

    // Lista completa cargada desde la API
    // IMPORTANTE EN EXAMEN:
    // si piden filtro, favoritos, ordenar o eliminar, casi siempre tocarás esta lista
    private var listaOriginal: List<Personaje> = emptyList()

    // IDs de personajes marcados como favoritos
    // IMPORTANTE:
    // no hay base de datos, esto vive solo en memoria
    private val favoritos = mutableSetOf<Int>()

    // Flag para saber si estamos mostrando solo favoritos o toda la lista
    private var soloFavoritos = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa el binding y carga el layout principal
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Título de la ActionBar
        // Si mañana piden cambiar el título, toca aquí
        title = "The Simpsons"

        // Configura RecyclerView, ViewModel, swipe refresh, búsqueda y observers
        configurarRecyclerView()
        configurarViewModel()
        configurarSwipeRefresh()
        configurarBusqueda()
        observarViewModel()
    }

    private fun configurarRecyclerView() {
        // Crea el adapter
        // AQUÍ tocas si te piden cambiar el comportamiento del click o long click
        adapter = PersonajeAdapter(
            emptyList(),
            { personaje ->
                // CLICK NORMAL: abre pantalla de detalle
                // Si piden que al pulsar haga otra cosa, toca aquí
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("PERSONAJE_EXTRA", personaje)
                startActivity(intent)
            },
            { personaje ->
                // LONG CLICK: abre menú contextual con opciones
                // Si piden quitar o cambiar el long click, toca aquí
                mostrarOpciones(personaje)
            },
            favoritos
        )

        // LayoutManager vertical del RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Asigna el adapter al RecyclerView
        binding.recyclerView.adapter = adapter
    }

    private fun configurarViewModel() {
        // Configuración de Retrofit
        // IMPORTANTE EN EXAMEN:
        // si cambian la API o la URL base, toca aquí
        val retrofit = Retrofit.Builder()
            .baseUrl("https://thesimpsonsapi.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Crea la interfaz de la API
        val api = retrofit.create(SimpsonsApiService::class.java)

        // Crea repository y factory
        val repository = SimpsonsRepository(api)
        val factory = SimpsonsViewModelFactory(repository)

        // Inicializa el ViewModel
        viewModel = ViewModelProvider(this, factory)[SimpsonsViewModel::class.java]
    }

    private fun configurarSwipeRefresh() {
        // Swipe down para recargar desde la API
        // Si mañana piden quitar el swipe refresh, toca aquí y el XML
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.cargarDatos()
        }
    }

    private fun configurarBusqueda() {
        // Listener del SearchView
        // Si piden buscar localmente, tocarás este bloque y/o filtrarLista()
        val listener = object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                // Se dispara al enviar la búsqueda
                filtrarLista(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Se dispara mientras el usuario escribe
                filtrarLista(newText)
                return true
            }
        }

        binding.searchView.setOnQueryTextListener(listener)
    }

    private fun filtrarLista(texto: String?) {
        // Recibe el texto escrito en el SearchView
        val query = texto.orEmpty().trim()

        // Si no hay texto, muestra toda la lista original
        // IMPORTANTE:
        // si mañana piden cambiar el comportamiento cuando el buscador está vacío, toca aquí
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

        // Filtro local actual: busca por nombre
        // IMPORTANTE EN EXAMEN:
        // si piden buscar por ocupación, género o varios campos, cambia esta condición
        val listaFiltrada = listaOriginal.filter { personaje ->
            personaje.nombre.contains(query, ignoreCase = true)

            // Ejemplos de cambios posibles:
            // personaje.ocupacion?.contains(query, true) == true
            // personaje.genero?.contains(query, true) == true
            // personaje.nombre.contains(query, true) || personaje.ocupacion?.contains(query, true) == true
        }

        // Actualiza la lista del adapter con el resultado filtrado
        adapter.actualizarLista(listaFiltrada)

        // Muestra mensaje si no hay resultados
        if (listaFiltrada.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.tvMensaje.visibility = View.VISIBLE
            binding.tvMensaje.text = "No se encontraron resultados"
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.tvMensaje.visibility = View.GONE
        }
    }

    private fun mostrarSoloFavoritos() {
        // Filtra solo los personajes cuyos IDs están en favoritos
        // Si piden "mostrar favoritos", esta es la función clave
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

    private fun observarViewModel() {
        // Observer de la lista de personajes que viene del ViewModel
        viewModel.personajes.observe(this) { lista ->

            // Guarda siempre la lista original completa
            // IMPORTANTE:
            // aquí nace la "fuente de verdad" para filtro, favoritos, eliminar, editar
            listaOriginal = lista

            // Si hay algo escrito en el buscador, reaplica el filtro
            // Si mañana piden que al cargar siempre muestre todo, tocarías esta lógica
            val textoActual = binding.searchView.query?.toString().orEmpty()
            filtrarLista(textoActual)
        }

        // Observer del estado de carga
        // Maneja loading, éxito, vacío y error
        viewModel.estadoCarga.observe(this) { estado ->

            // Detiene el spinner del SwipeRefreshLayout
            binding.swipeRefresh.isRefreshing = false

            when (estado) {

                is SimpsonsViewModel.EstadoUI.Cargando -> {
                    // Estado de carga
                    // Si piden cambiar el comportamiento visual mientras carga, toca aquí
                    if (!binding.swipeRefresh.isRefreshing) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                    binding.tvMensaje.visibility = View.GONE
                }

                is SimpsonsViewModel.EstadoUI.Exito -> {
                    // Estado éxito
                    binding.progressBar.visibility = View.GONE
                    if (listaOriginal.isNotEmpty()) {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.tvMensaje.visibility = View.GONE
                    }
                }

                is SimpsonsViewModel.EstadoUI.Vacio -> {
                    // Estado vacío
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.tvMensaje.visibility = View.VISIBLE
                    binding.tvMensaje.text = "No hay personajes disponibles"
                }

                is SimpsonsViewModel.EstadoUI.Error -> {
                    // Estado error
                    // Si mañana piden cambiar el mensaje de error, toca aquí
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.tvMensaje.visibility = View.VISIBLE
                    binding.tvMensaje.text = "Error: ${estado.mensaje}"
                }
            }
        }
    }

    private fun mostrarOpciones(personaje: Personaje) {
        // Menú contextual que aparece al hacer long click
        // Si piden añadir o quitar opciones, cambia este array y el when
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
        // Cuadro de texto para editar el nombre
        // Si mañana piden editar otro campo (ocupación, género, etc.), toca aquí
        val editText = EditText(this)
        editText.setText(personaje.nombre)
        editText.setSelection(editText.text.length)

        AlertDialog.Builder(this)
            .setTitle("Editar nombre")
            .setMessage("Modifica el nombre del personaje")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->

                val nuevoNombre = editText.text.toString().trim()

                // Validación básica
                if (nuevoNombre.isBlank()) {
                    Toast.makeText(this, "Nombre inválido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Reemplaza el personaje editado por una copia con el nuevo nombre
                // IMPORTANTE:
                // si piden editar ocupación, sería it.copy(ocupacion = nuevaOcupacion)
                listaOriginal = listaOriginal.map {
                    if (it.id == personaje.id) {
                        it.copy(nombre = nuevoNombre)
                    } else {
                        it
                    }
                }

                // Reaplica el filtro actual para refrescar la vista
                val textoActual = binding.searchView.query?.toString().orEmpty()
                filtrarLista(textoActual)

                Toast.makeText(this, "Nombre actualizado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun marcarFavorito(personaje: Personaje) {
        // Añade o quita el personaje de favoritos
        // Si mañana piden cambiar la lógica de favoritos, toca aquí
        if (favoritos.contains(personaje.id)) {
            favoritos.remove(personaje.id)
            Toast.makeText(this, "${personaje.nombre} quitado de favoritos", Toast.LENGTH_SHORT).show()
        } else {
            favoritos.add(personaje.id)
            Toast.makeText(this, "${personaje.nombre} añadido a favoritos", Toast.LENGTH_SHORT).show()
        }

        // Refresca visualmente la lista para mostrar u ocultar la estrella
        adapter.notifyDataSetChanged()
    }

    private fun eliminarPersonaje(personaje: Personaje) {
        // Diálogo de confirmación para eliminar
        // Si piden eliminar sin confirmar, simplificar aquí
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Seguro que quieres eliminar a ${personaje.nombre}?")
            .setPositiveButton("Sí") { _, _ ->

                // Elimina el personaje de la lista original
                listaOriginal = listaOriginal.filter { it.id != personaje.id }

                // Si era favorito, también lo quita del set de favoritos
                favoritos.remove(personaje.id)

                // Reaplica filtro actual para refrescar la vista
                val textoActual = binding.searchView.query?.toString().orEmpty()
                filtrarLista(textoActual)

                Toast.makeText(this, "Personaje eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Infla el menú de la ActionBar desde res/menu/menu_main.xml
        // Si mañana piden quitar o añadir iconos/botones, toca este XML y/o esta función
        menuInflater.inflate(R.menu.menu_main, menu)

        // Cambia color de los iconos del menú
        // Si no te piden color, esto se puede quitar sin afectar la lógica
        val color = getColor(android.R.color.holo_red_light)
        menu?.findItem(R.id.action_refresh)?.icon?.setTint(color)
        menu?.findItem(R.id.action_clear)?.icon?.setTint(color)
        menu?.findItem(R.id.action_favoritos)?.icon?.setTint(color)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Maneja los clics del menú superior
        // Si mañana piden nuevas acciones en la ActionBar, toca aquí
        return when (item.itemId) {

            R.id.action_favoritos -> {
                // Activa/desactiva vista solo favoritos
                soloFavoritos = !soloFavoritos

                if (soloFavoritos) {
                    mostrarSoloFavoritos()
                } else {
                    // Vuelve a mostrar toda la lista
                    adapter.actualizarLista(listaOriginal)
                }
                true
            }

            R.id.action_refresh -> {
                // Recarga datos desde la API
                viewModel.cargarDatos()
                true
            }

            R.id.action_clear -> {
                // Limpia el SearchView y reaplica lista completa
                binding.searchView.setQuery("", false)
                filtrarLista("")
                true
            }

            R.id.action_todos -> {
                // Fuerza mostrar toda la lista y sale del modo favoritos
                soloFavoritos = false
                adapter.actualizarLista(listaOriginal)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}