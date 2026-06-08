// data/remote/api/UserApi.kt
package com.motosapp.data.remote.api

import com.motosapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("users/")
    suspend fun getUsers(
        @Query("search")    search:   String?  = null,
        @Query("is_staff")  isStaff:  Boolean? = null,
        @Query("is_active") isActive: Boolean? = null,
        @Query("page")      page:     Int?     = null,
    ): Response<PaginatedDto<UserDto>>

    @GET("users/{id}/")
    suspend fun getUser(@Path("id") id: Int): Response<UserDto>

    @POST("users/")
    suspend fun createUser(@Body body: UserRequestDto): Response<UserDto>

    @PATCH("users/{id}/")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body body: UserRequestDto,
    ): Response<UserDto>

    @DELETE("users/{id}/")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    @POST("users/{id}/toggle-active/")
    suspend fun toggleActive(@Path("id") id: Int): Response<ToggleActiveResponseDto>

    @GET("users/profile/")
    suspend fun getProfile(): Response<UserDto>

    @GET("users/stats/")
    suspend fun getStats(): Response<UserStatsDto>
}