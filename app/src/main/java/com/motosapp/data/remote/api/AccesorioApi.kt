package com.motosapp.data.remote.api

import com.motosapp.data.remote.dto.PaginatedDto
import com.motosapp.domain.model.Accesorio
import com.motosapp.domain.model.AccesorioPayload
import retrofit2.Response
import retrofit2.http.*

interface AccesorioApi {
    @GET("accesorios/")
    suspend fun getAccesorios(
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PaginatedDto<Accesorio>>

    @GET("accesorios/{id}/")
    suspend fun getAccesorio(@Path("id") id: Int): Response<Accesorio>

    @POST("accesorios/")
    suspend fun createAccesorio(@Body payload: AccesorioPayload): Response<Accesorio>

    @PATCH("accesorios/{id}/")
    suspend fun updateAccesorio(
        @Path("id") id: Int,
        @Body payload: AccesorioPayload,
    ): Response<Accesorio>

    @DELETE("accesorios/{id}/")
    suspend fun deleteAccesorio(@Path("id") id: Int): Response<Unit>
}
