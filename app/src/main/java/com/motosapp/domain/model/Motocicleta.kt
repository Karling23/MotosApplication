package com.motosapp.domain.model

import com.google.gson.annotations.SerializedName

data class Motocicleta(
    override val id: Int,
    val marca: Int,
    val categoria: Int,
    val modelo: String,
    val anio: Int,
    val cilindrada: Int,
    val color: String,
    val precio: String,
    val stock: Int,
    val is_active: Boolean,
    val descripcion: String,
    val imagen: String? = null
) : ShopItem {
    override val displayName: String get() = modelo
    override val displayPrice: Double get() = precio.toDoubleOrNull() ?: 0.0
    override val displayImageUrl: String? get() = imagen
    override val itemType: String get() = "Motocicleta"
}

data class MotocicletaPayload(
    val marca: Int,
    val categoria: Int,
    val modelo: String,
    val anio: Int,
    val cilindrada: Int,
    val color: String,
    val precio: String,
    val stock: Int,
    @SerializedName("is_active") val isActive: Boolean,
    val descripcion: String,
)

