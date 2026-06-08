package com.motosapp.domain.model

data class Order(
    val id: Int,
    val usuario: Int,
    val estado: String,
    val total: Double,
    val direccion_envio: String
)

data class OrderPayload(
    val usuario: Int,
    val estado: String,
    val total: Double,
    val direccion_envio: String
)
