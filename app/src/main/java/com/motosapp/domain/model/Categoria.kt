package com.motosapp.domain.model

data class Categoria(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val activo: Boolean,
)

data class CategoriaPayload(
    val nombre: String,
    val descripcion: String? = null,
)
