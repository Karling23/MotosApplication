package com.motosapp.data.remote.api

import com.motosapp.data.remote.dto.PaginatedDto
import com.motosapp.domain.model.Marca
import com.motosapp.domain.model.MarcaPayload
import retrofit2.Response
import retrofit2.http.*

interface MarcaApi {
    @GET("marcas/")
    suspend fun getMarcas(
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PaginatedDto<Marca>>

    @GET("marcas/{id}/")
    suspend fun getMarca(@Path("id") id: Int): Response<Marca>

    @POST("marcas/")
    suspend fun createMarca(@Body payload: MarcaPayload): Response<Marca>

    @PATCH("marcas/{id}/")
    suspend fun updateMarca(
        @Path("id") id: Int,
        @Body payload: MarcaPayload,
    ): Response<Marca>

    @DELETE("marcas/{id}/")
    suspend fun deleteMarca(@Path("id") id: Int): Response<Unit>
}
