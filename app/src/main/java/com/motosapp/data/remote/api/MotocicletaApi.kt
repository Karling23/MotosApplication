package com.motosapp.data.remote.api

import com.motosapp.data.remote.dto.PaginatedDto
import com.motosapp.domain.model.Motocicleta
import com.motosapp.domain.model.MotocicletaPayload
import retrofit2.Response
import retrofit2.http.*

interface MotocicletaApi {
    @GET("motocicletas/")
    suspend fun getMotocicletas(
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PaginatedDto<Motocicleta>>

    @GET("motocicletas/{id}/")
    suspend fun getMotocicleta(@Path("id") id: Int): Response<Motocicleta>

    @POST("motocicletas/")
    suspend fun createMotocicleta(@Body payload: MotocicletaPayload): Response<Motocicleta>

    @PATCH("motocicletas/{id}/")
    suspend fun updateMotocicleta(
        @Path("id") id: Int,
        @Body payload: MotocicletaPayload,
    ): Response<Motocicleta>

    @DELETE("motocicletas/{id}/")
    suspend fun deleteMotocicleta(@Path("id") id: Int): Response<Unit>
}
