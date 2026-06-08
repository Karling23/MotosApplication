package com.motosapp.data.remote.utils

import retrofit2.Response
import java.net.ConnectException
import java.net.UnknownHostException
import java.net.SocketTimeoutException

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body)
            } else {
                // Si el cuerpo es nulo, pero la llamada fue exitosa (ej. 204 No Content),
                // asumimos que T es Unit y forzamos el cast, o lanzamos error si no lo es.
                // Usualmente body puede ser nulo en DELETE
                @Suppress("UNCHECKED_CAST")
                Result.success(Unit as T)
            }
        } else {
            val errorMessage = when (response.code()) {
                400 -> "Error 400: datos inválidos."
                401, 403 -> "Error 401/403: usuario no autorizado."
                404 -> "Error 404: registro no encontrado."
                in 500..599 -> "Error 500: error del servidor."
                else -> "Error ${response.code()}: error desconocido."
            }
            Result.failure(Exception(errorMessage))
        }
    } catch (e: UnknownHostException) {
        Result.failure(Exception("Error de conexión a internet."))
    } catch (e: ConnectException) {
        Result.failure(Exception("Error de conexión a internet."))
    } catch (e: SocketTimeoutException) {
        Result.failure(Exception("Error de conexión a internet."))
    } catch (e: Exception) {
        Result.failure(Exception("Error inesperado: ${e.localizedMessage}"))
    }
}
