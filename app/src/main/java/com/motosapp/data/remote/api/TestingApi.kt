package com.motosapp.data.remote.api
import com.motosapp.domain.model.Testing
import com.motosapp.domain.model.TestingPayload
import retrofit2.http.*

interface TestingApi {
    @GET("testings/")
    suspend fun getTestings(): List<Testing>

    @POST("testings/")
    suspend fun createTesting(@Body payload: TestingPayload): Testing
}
