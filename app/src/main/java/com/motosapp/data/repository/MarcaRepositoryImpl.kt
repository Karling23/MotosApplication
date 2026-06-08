package com.motosapp.data.repository

import com.motosapp.data.remote.api.MarcaApi
import com.motosapp.data.remote.utils.safeApiCall
import com.motosapp.domain.model.Marca
import com.motosapp.domain.model.MarcaPayload
import com.motosapp.domain.repository.MarcaRepository
import javax.inject.Inject

class MarcaRepositoryImpl @Inject constructor(
    private val api: MarcaApi
) : MarcaRepository {
    
    override suspend fun getMarcas(
        search: String?,
        page: Int?
    ): Result<Pair<List<Marca>, Int>> {
        return safeApiCall { api.getMarcas(search, page) }.map { body ->
            Pair(body.results, body.count)
        }
    }

    override suspend fun getMarca(id: Int): Result<Marca> {
        return safeApiCall { api.getMarca(id) }
    }

    override suspend fun createMarca(payload: MarcaPayload): Result<Marca> {
        return safeApiCall { api.createMarca(payload) }
    }

    override suspend fun updateMarca(id: Int, payload: MarcaPayload): Result<Marca> {
        return safeApiCall { api.updateMarca(id, payload) }
    }

    override suspend fun deleteMarca(id: Int): Result<Unit> {
        return safeApiCall { api.deleteMarca(id) }.map { }
    }
}
