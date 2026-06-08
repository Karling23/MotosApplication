package com.motosapp.domain.model

data class Testing(
    val id: Int,
    val usuario: Int,
    val motocicleta: Int,
    val fecha_test: String,
    val estado: String,
    val comentarios: String?
)

data class TestingPayload(
    val usuario: Int,
    val motocicleta: Int,
    val fecha_test: String,
    val estado: String = "programado",
    val comentarios: String? = null
)
