package com.motosapp.domain.repository

import com.motosapp.domain.model.Motocicleta
import com.motosapp.domain.model.MotocicletaPayload

interface MotocicletaRepository {
    suspend fun getMotocicletas(
        search: String? = null,
        page: Int? = null,
    ): Result<Pair<List<Motocicleta>, Int>>
    
    suspend fun getMotocicleta(id: Int): Result<Motocicleta>
    suspend fun createMotocicleta(payload: MotocicletaPayload): Result<Motocicleta>
    suspend fun updateMotocicleta(id: Int, payload: MotocicletaPayload): Result<Motocicleta>
    suspend fun deleteMotocicleta(id: Int): Result<Unit>
}
