package com.example.simpsons.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpsons.data.model.Personaje
import com.example.simpsons.data.repository.SimpsonsRepository
import kotlinx.coroutines.launch

class SimpsonsViewModel(private val repository: SimpsonsRepository) : ViewModel() {

    private val _personajes = MutableLiveData<List<Personaje>>()
    val personajes: LiveData<List<Personaje>> get() = _personajes

    private val _estadoCarga = MutableLiveData<EstadoUI>()
    val estadoCarga: LiveData<EstadoUI> get() = _estadoCarga

    init {
        cargarDatos()
    }

    /*
        Función permite reutilizar la carga inicial, SwipeRefresh, botón de recarga.
     */
    fun cargarDatos() {
        viewModelScope.launch {
            _estadoCarga.value = EstadoUI.Cargando
            try {
                val response = repository.obtenerPersonajes()

                /*
                    PARTE MODIFICABLE EN EXAMEN:
                    Aquí suele cambiar la forma de obtener la lista:
                    - response.results
                    - response.data
                    - response.items
                    - response directamente
                 */
                val lista = response.results

                if (lista.isEmpty()) {
                    _personajes.postValue(emptyList())
                    _estadoCarga.value = EstadoUI.Vacio
                } else {
                    /*
                        PARTE MODIFICABLE EN EXAMEN:
                        Aquí puedes limitar resultados, ordenar o filtrar.
                     */

                    _personajes.postValue(lista.take(20))
                    _estadoCarga.value = EstadoUI.Exito
                }
            } catch (e: Exception) {
                android.util.Log.e("SimpsonsApp", "Error: ${e.message}", e)
                _estadoCarga.postValue(EstadoUI.Error(e.message ?: "Error desconocido"))
            }
        }
    }

    sealed class EstadoUI {
        object Cargando : EstadoUI()
        object Vacio : EstadoUI()
        object Exito : EstadoUI()
        data class Error(val mensaje: String) : EstadoUI()
    }
}