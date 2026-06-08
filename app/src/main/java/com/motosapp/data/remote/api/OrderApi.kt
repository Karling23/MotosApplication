package com.motosapp.data.remote.api

import com.motosapp.data.remote.dto.OrderDto
import com.motosapp.data.remote.dto.PaginatedDto
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    @GET("pedidos/")
    suspend fun getOrders(
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PaginatedDto<OrderDto>>

    @GET("pedidos/{id}/")
    suspend fun getOrder(@Path("id") id: Int): Response<OrderDto>

    @POST("pedidos/")
    suspend fun createOrder(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<OrderDto>

    @PATCH("pedidos/{id}/")
    suspend fun updateString(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<OrderDto>
}