package com.motosapp.data.repository

import com.motosapp.data.remote.api.OrderApi
import com.motosapp.data.remote.dto.OrderDto
import com.motosapp.data.remote.utils.safeApiCall
import com.motosapp.domain.model.Order
import com.motosapp.domain.repository.OrderRepository
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val api: OrderApi
) : OrderRepository {
    
    override suspend fun getOrders(
        search: String?,
        page: Int?
    ): Result<Pair<List<Order>, Int>> {
        return safeApiCall { api.getOrders(search, page) }.map { body ->
            Pair(body.results.map { it.toDomain() }, body.count)
        }
    }

    override suspend fun getOrder(id: Int): Result<Order> {
        return safeApiCall { api.getOrder(id) }.map { it.toDomain() }
    }

    override suspend fun createOrder(usuario: Int, total: Double, direccionEnvio: String): Result<Order> {
        val request = mapOf(
            "usuario" to usuario,
            "estado" to "PENDIENTE",
            "total" to total.toString(),
            "direccion_envio" to direccionEnvio
        )
        return safeApiCall { api.createOrder(request) }.map { it.toDomain() }
    }

    override suspend fun updateStatus(orderId: Int, status: String): Result<Order> {
        val request = mapOf("estado" to status)
        return safeApiCall { api.updateString(orderId, request) }.map { it.toDomain() }
    }

    private fun OrderDto.toDomain() = Order(
        id = id,
        usuario = usuario,
        estado = estado,
        total = total.toDoubleOrNull() ?: 0.0,
        direccion_envio = direccion_envio
    )
}