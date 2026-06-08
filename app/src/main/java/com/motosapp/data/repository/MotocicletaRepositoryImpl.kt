package com.motosapp.data.repository

import com.motosapp.data.remote.api.MotocicletaApi
import com.motosapp.data.remote.utils.safeApiCall
import com.motosapp.domain.model.Motocicleta
import com.motosapp.domain.model.MotocicletaPayload
import com.motosapp.domain.repository.MotocicletaRepository
import javax.inject.Inject

class MotocicletaRepositoryImpl @Inject constructor(
    private val api: MotocicletaApi
) : MotocicletaRepository {
    
    override suspend fun getMotocicletas(
        search: String?,
        page: Int?
    ): Result<Pair<List<Motocicleta>, Int>> {
        return safeApiCall { api.getMotocicletas(search, page) }.map { body ->
            Pair(body.results, body.count)
        }
    }

    override suspend fun getMotocicleta(id: Int): Result<Motocicleta> {
        return safeApiCall { api.getMotocicleta(id) }
    }

    override suspend fun createMotocicleta(payload: MotocicletaPayload): Result<Motocicleta> {
        return safeApiCall { api.createMotocicleta(payload) }
    }

    override suspend fun updateMotocicleta(id: Int, payload: MotocicletaPayload): Result<Motocicleta> {
        return safeApiCall { api.updateMotocicleta(id, payload) }
    }

    override suspend fun deleteMotocicleta(id: Int): Result<Unit> {
        return safeApiCall { api.deleteMotocicleta(id) }.map { }
    }
}
