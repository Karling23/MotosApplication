package com.motosapp.data.repository

import com.motosapp.data.remote.api.CascoApi
import com.motosapp.data.remote.utils.safeApiCall
import com.motosapp.domain.model.Casco
import com.motosapp.domain.model.CascoPayload
import com.motosapp.domain.repository.CascoRepository
import javax.inject.Inject

class CascoRepositoryImpl @Inject constructor(
    private val api: CascoApi
) : CascoRepository {
    
    override suspend fun getCascos(
        search: String?,
        page: Int?
    ): Result<Pair<List<Casco>, Int>> {
        return safeApiCall { api.getCascos(search, page) }.map { body ->
            Pair(body.results, body.count)
        }
    }

    override suspend fun getCasco(id: Int): Result<Casco> {
        return safeApiCall { api.getCasco(id) }
    }

    override suspend fun createCasco(payload: CascoPayload): Result<Casco> {
        return safeApiCall { api.createCasco(payload) }
    }

    override suspend fun updateCasco(id: Int, payload: CascoPayload): Result<Casco> {
        return safeApiCall { api.updateCasco(id, payload) }
    }

    override suspend fun deleteCasco(id: Int): Result<Unit> {
        return safeApiCall { api.deleteCasco(id) }.map { }
    }
}
