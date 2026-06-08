package com.motosapp.data.repository

import com.motosapp.data.remote.api.AccesorioApi
import com.motosapp.data.remote.utils.safeApiCall
import com.motosapp.domain.model.Accesorio
import com.motosapp.domain.model.AccesorioPayload
import com.motosapp.domain.repository.AccesorioRepository
import javax.inject.Inject

class AccesorioRepositoryImpl @Inject constructor(
    private val api: AccesorioApi
) : AccesorioRepository {
    
    override suspend fun getAccesorios(
        search: String?,
        page: Int?
    ): Result<Pair<List<Accesorio>, Int>> {
        return safeApiCall { api.getAccesorios(search, page) }.map { body ->
            Pair(body.results, body.count)
        }
    }

    override suspend fun getAccesorio(id: Int): Result<Accesorio> {
        return safeApiCall { api.getAccesorio(id) }
    }

    override suspend fun createAccesorio(payload: AccesorioPayload): Result<Accesorio> {
        return safeApiCall { api.createAccesorio(payload) }
    }

    override suspend fun updateAccesorio(id: Int, payload: AccesorioPayload): Result<Accesorio> {
        return safeApiCall { api.updateAccesorio(id, payload) }
    }

    override suspend fun deleteAccesorio(id: Int): Result<Unit> {
        return safeApiCall { api.deleteAccesorio(id) }.map { }
    }
}
