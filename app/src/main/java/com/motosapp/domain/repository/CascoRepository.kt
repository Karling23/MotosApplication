package com.motosapp.domain.repository

import com.motosapp.domain.model.Casco
import com.motosapp.domain.model.CascoPayload

interface CascoRepository {
    suspend fun getCascos(
        search: String? = null,
        page: Int? = null,
    ): Result<Pair<List<Casco>, Int>>
    
    suspend fun getCasco(id: Int): Result<Casco>
    suspend fun createCasco(payload: CascoPayload): Result<Casco>
    suspend fun updateCasco(id: Int, payload: CascoPayload): Result<Casco>
    suspend fun deleteCasco(id: Int): Result<Unit>
}
