// data/repository/AuthRepositoryImpl.kt
package com.motosapp.data.repository

import com.motosapp.data.local.TokenDataStore
import com.motosapp.data.remote.api.AuthApi
import com.motosapp.data.remote.dto.*
import com.motosapp.domain.model.LoggedUser
import com.motosapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api:            AuthApi,
    private val tokenDataStore: TokenDataStore,
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<LoggedUser> =
        runCatching {
            val response = api.login(LoginRequest(username, password))
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: ""
                error(parseErrorMessage(errorBody, response.code()))
            }
            val body = response.body() ?: error("Empty body")
            tokenDataStore.saveTokens(body.access, body.refresh)
            tokenDataStore.saveUser(body.userId, body.username, body.email, body.isStaff)
            LoggedUser(body.userId, body.username, body.email, body.isStaff)
        }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        password2: String,
        firstName: String,
        lastName: String,
    ): Result<LoggedUser> = runCatching {
        val response = api.register(RegisterRequest(username, email, password, password2, firstName, lastName))
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: ""
            error(parseErrorMessage(errorBody, response.code()))
        }
        val body = response.body() ?: error("Empty body")
        tokenDataStore.saveTokens(body.access, body.refresh)
        tokenDataStore.saveUser(body.userId, body.username, body.email, body.isStaff)
        LoggedUser(body.userId, body.username, body.email, body.isStaff)
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        val refresh = tokenDataStore.getRefreshToken()
        if (refresh != null) {
            runCatching { api.logout(LogoutRequest(refresh)) }
        }
        tokenDataStore.clearSession()
    }

    override suspend fun getStoredUser(): TokenDataStore.UserSnapshot? =
        tokenDataStore.userSnapshot.first()

    override suspend fun isLoggedIn(): Boolean =
        !tokenDataStore.getAccessToken().isNullOrBlank()

    // Extrae el mensaje de error legible del JSON de Django
    private fun parseErrorMessage(body: String, code: Int): String {
        return try {
            val map = com.google.gson.Gson()
                .fromJson(body, Map::class.java)
            map["detail"]?.toString()
                ?: map["non_field_errors"]?.toString()
                ?: map.entries.firstOrNull()?.let { "${it.key}: ${it.value}" }
                ?: "Error $code"
        } catch (e: Exception) {
            "Error $code"
        }
    }
}