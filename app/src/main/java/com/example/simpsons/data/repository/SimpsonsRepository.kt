package com.example.simpsons.data.repository

import com.example.simpsons.data.network.SimpsonsApiService

class SimpsonsRepository(
    private val api: SimpsonsApiService
) {
    suspend fun obtenerPersonajes() =
        api.getPersonajes()

}