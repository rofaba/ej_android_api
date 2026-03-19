package com.example.simpsons.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Personaje(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val nombre: String,

    @SerializedName("age")
    val edad: Int?,

    @SerializedName("birthdate")
    val fechaNacimiento: String?,

    @SerializedName("gender")
    val genero: String?,

    @SerializedName("occupation")
    val ocupacion: String?,

    @SerializedName("portrait_path")
    val urlImagen: String?,

    @SerializedName("phrases")
    val frases: List<String>?,

    @SerializedName("status")
    val estado: String?
) : Parcelable {

    val imagenCompleta: String
        get() = "https://cdn.thesimpsonsapi.com/500${urlImagen ?: ""}"
}