package com.motosapp.data.remote.api

import com.motosapp.data.remote.dto.PaginatedDto
import com.motosapp.domain.model.Categoria
import com.motosapp.domain.model.CategoriaPayload
import retrofit2.Response
import retrofit2.http.*

interface CategoriaApi {
    @GET("categorias/")
    suspend fun getCategorias(
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PaginatedDto<Categoria>>

    @GET("categorias/{id}/")
    suspend fun getCategoria(@Path("id") id: Int): Response<Categoria>

    @POST("categorias/")
    suspend fun createCategoria(@Body payload: CategoriaPayload): Response<Categoria>

    @PATCH("categorias/{id}/")
    suspend fun updateCategoria(
        @Path("id") id: Int,
        @Body payload: CategoriaPayload,
    ): Response<Categoria>

    @DELETE("categorias/{id}/")
    suspend fun deleteCategoria(@Path("id") id: Int): Response<Unit>
}
