// data/remote/interceptor/AuthInterceptor.kt
package com.motosapp.data.remote.interceptor

import com.motosapp.BuildConfig
import com.motosapp.data.local.TokenDataStore
import com.motosapp.data.remote.dto.TokenRefreshRequest
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Solo reintentar una vez
        if (response.request.header("X-Retry") != null) return null

        val refresh = runBlocking { tokenDataStore.getRefreshToken() }
            ?: return null

        // Llamada síncrona al endpoint de refresh
        val client   = OkHttpClient()
        val gson     = Gson()
        val body     = gson.toJson(TokenRefreshRequest(refresh))
        val request  = Request.Builder()
            .url("${BuildConfig.API_BASE_URL}auth/token/refresh/")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val refreshResponse = try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            return null
        }

        if (!refreshResponse.isSuccessful) {
            runBlocking { tokenDataStore.clearSession() }
            return null
        }

        val responseBody = refreshResponse.body?.string() ?: return null
        val newAccess    = try {
            gson.fromJson(responseBody, Map::class.java)["access"] as? String
        } catch (e: Exception) { null } ?: return null

        runBlocking { tokenDataStore.saveAccessToken(newAccess) }

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .header("X-Retry", "true")
            .build()
    }
}

// Interceptor que añade el Bearer token a cada petición
class BearerTokenInterceptor(
    private val tokenDataStore: TokenDataStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val access = runBlocking { tokenDataStore.getAccessToken() }
        val request = if (access != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $access")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}