package com.motosapp.domain.repository

import com.motosapp.domain.model.Order

interface OrderRepository {
    suspend fun getOrders(
        search: String? = null,
        page: Int? = null,
    ): Result<Pair<List<Order>, Int>>
    
    suspend fun getOrder(id: Int): Result<Order>
    suspend fun createOrder(usuario: Int, total: Double, direccionEnvio: String): Result<Order>
    suspend fun updateStatus(orderId: Int, status: String): Result<Order>
}