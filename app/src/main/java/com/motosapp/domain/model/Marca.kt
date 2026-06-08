package com.motosapp.domain.model

import com.google.gson.annotations.SerializedName

data class Marca(
    val id: Int,
    val nombre: String,
    val pais_origen: String?,
    val descripcion: String?,
    val activo: Boolean
)

data class MarcaPayload(
    val nombre: String,
    @SerializedName("pais_origen") val paisOrigen: String?,
    val descripcion: String? = null,
    val activo: Boolean = true
)
