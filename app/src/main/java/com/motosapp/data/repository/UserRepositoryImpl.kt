// data/repository/UserRepositoryImpl.kt
package com.motosapp.data.repository

import com.motosapp.data.remote.api.UserApi
import com.motosapp.data.remote.dto.toDomain
import com.motosapp.data.remote.dto.toRequest
import com.motosapp.data.remote.utils.safeApiCall
import com.motosapp.domain.model.User
import com.motosapp.domain.model.UserPayload
import com.motosapp.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
) : UserRepository {

    override suspend fun getUsers(
        search:   String?,
        isStaff:  Boolean?,
        isActive: Boolean?,
        page:     Int?,
    ): Result<Pair<List<User>, Int>> {
        return safeApiCall { api.getUsers(search, isStaff, isActive, page) }.map { body ->
            Pair(body.results.map { it.toDomain() }, body.count)
        }
    }

    override suspend fun getUser(id: Int): Result<User> {
        return safeApiCall { api.getUser(id) }.map { it.toDomain() }
    }

    override suspend fun createUser(payload: UserPayload): Result<User> {
        return safeApiCall { api.createUser(payload.toRequest()) }.map { it.toDomain() }
    }

    override suspend fun updateUser(id: Int, payload: UserPayload): Result<User> {
        return safeApiCall { api.updateUser(id, payload.toRequest()) }.map { it.toDomain() }
    }

    override suspend fun deleteUser(id: Int): Result<Unit> {
        return safeApiCall { api.deleteUser(id) }.map { }
    }

    override suspend fun toggleActive(id: Int): Result<Boolean> {
        return safeApiCall { api.toggleActive(id) }.map { it.isActive }
    }

    override suspend fun getStats(): Result<Map<String, Int>> {
        return safeApiCall { api.getStats() }.map { s ->
            mapOf(
                "total"    to s.total,
                "active"   to s.active,
                "inactive" to s.inactive,
                "staff"    to s.staff,
            )
        }
    }
}