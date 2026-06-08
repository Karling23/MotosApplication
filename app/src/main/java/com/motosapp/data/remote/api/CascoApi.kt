package com.motosapp.data.remote.api

import com.motosapp.data.remote.dto.PaginatedDto
import com.motosapp.domain.model.Casco
import com.motosapp.domain.model.CascoPayload
import retrofit2.Response
import retrofit2.http.*

interface CascoApi {
    @GET("cascos/")
    suspend fun getCascos(
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PaginatedDto<Casco>>

    @GET("cascos/{id}/")
    suspend fun getCasco(@Path("id") id: Int): Response<Casco>

    @POST("cascos/")
    suspend fun createCasco(@Body payload: CascoPayload): Response<Casco>

    @PATCH("cascos/{id}/")
    suspend fun updateCasco(
        @Path("id") id: Int,
        @Body payload: CascoPayload,
    ): Response<Casco>

    @DELETE("cascos/{id}/")
    suspend fun deleteCasco(@Path("id") id: Int): Response<Unit>
}
