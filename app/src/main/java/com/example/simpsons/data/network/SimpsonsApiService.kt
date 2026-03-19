package com.example.simpsons.data.network

import com.example.simpsons.data.model.SimpsonsResponse
import retrofit2.http.GET

interface SimpsonsApiService {
    @GET("characters")
    suspend fun getPersonajes(): SimpsonsResponse
}