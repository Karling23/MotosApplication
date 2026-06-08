package com.motosapp.domain.model

import com.google.gson.annotations.SerializedName

data class Accesorio(
    override val id: Int,
    val nombre: String,
    val marca: Int,
    val categoria_accesorio: String,
    val precio: String,
    val stock: Int,
    val is_active: Boolean,
    val descripcion: String,
    val imagen: String? = null
) : ShopItem {
    override val displayName: String get() = nombre
    override val displayPrice: Double get() = precio.toDoubleOrNull() ?: 0.0
    override val displayImageUrl: String? get() = imagen
    override val itemType: String get() = "Accesorio"
}

data class AccesorioPayload(
    val nombre: String,
    val marca: Int,
    @SerializedName("categoria_accesorio") val categoriaAccesorio: String,
    val precio: String,
    val stock: Int,
    @SerializedName("is_active") val isActive: Boolean,
    val descripcion: String,
)
