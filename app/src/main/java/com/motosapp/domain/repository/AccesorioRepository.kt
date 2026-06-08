package com.motosapp.domain.repository

import com.motosapp.domain.model.Accesorio
import com.motosapp.domain.model.AccesorioPayload

interface AccesorioRepository {
    suspend fun getAccesorios(
        search: String? = null,
        page: Int? = null,
    ): Result<Pair<List<Accesorio>, Int>>
    
    suspend fun getAccesorio(id: Int): Result<Accesorio>
    suspend fun createAccesorio(payload: AccesorioPayload): Result<Accesorio>
    suspend fun updateAccesorio(id: Int, payload: AccesorioPayload): Result<Accesorio>
    suspend fun deleteAccesorio(id: Int): Result<Unit>
}
