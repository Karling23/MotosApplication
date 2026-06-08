package com.motosapp.data.remote.dto

data class OrderDto(
    val id: Int,
    val usuario: Int,
    val estado: String,
    val total: String, // from API might be string
    val direccion_envio: String
)