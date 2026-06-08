package com.motosapp.domain.repository

import com.motosapp.domain.model.Marca
import com.motosapp.domain.model.MarcaPayload

interface MarcaRepository {
    suspend fun getMarcas(
        search: String? = null,
        page: Int? = null,
    ): Result<Pair<List<Marca>, Int>>
    
    suspend fun getMarca(id: Int): Result<Marca>
    suspend fun createMarca(payload: MarcaPayload): Result<Marca>
    suspend fun updateMarca(id: Int, payload: MarcaPayload): Result<Marca>
    suspend fun deleteMarca(id: Int): Result<Unit>
}
