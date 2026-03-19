package com.example.simpsons.data.model

import com.google.gson.annotations.SerializedName

data class SimpsonsResponse(

    @SerializedName("results")
    val results: List<Personaje>

)