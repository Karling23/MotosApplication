package com.motosapp.domain.repository

import com.motosapp.domain.model.Categoria
import com.motosapp.domain.model.CategoriaPayload

interface CategoriaRepository {
    suspend fun getCategorias(
        search: String? = null,
        page: Int? = null,
    ): Result<Pair<List<Categoria>, Int>>
    
    suspend fun getCategoria(id: Int): Result<Categoria>
    suspend fun createCategoria(payload: CategoriaPayload): Result<Categoria>
    suspend fun updateCategoria(id: Int, payload: CategoriaPayload): Result<Categoria>
    suspend fun deleteCategoria(id: Int): Result<Unit>
}
