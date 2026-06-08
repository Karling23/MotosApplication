package com.motosapp.data.repository

import com.motosapp.data.remote.api.CategoriaApi
import com.motosapp.data.remote.utils.safeApiCall
import com.motosapp.domain.model.Categoria
import com.motosapp.domain.model.CategoriaPayload
import com.motosapp.domain.repository.CategoriaRepository
import javax.inject.Inject

class CategoriaRepositoryImpl @Inject constructor(
    private val api: CategoriaApi
) : CategoriaRepository {
    
    override suspend fun getCategorias(
        search: String?,
        page: Int?
    ): Result<Pair<List<Categoria>, Int>> {
        return safeApiCall { api.getCategorias(search, page) }.map { body ->
            Pair(body.results, body.count)
        }
    }

    override suspend fun getCategoria(id: Int): Result<Categoria> {
        return safeApiCall { api.getCategoria(id) }
    }

    override suspend fun createCategoria(payload: CategoriaPayload): Result<Categoria> {
        return safeApiCall { api.createCategoria(payload) }
    }

    override suspend fun updateCategoria(id: Int, payload: CategoriaPayload): Result<Categoria> {
        return safeApiCall { api.updateCategoria(id, payload) }
    }

    override suspend fun deleteCategoria(id: Int): Result<Unit> {
        return safeApiCall { api.deleteCategoria(id) }.map { }
    }
}
