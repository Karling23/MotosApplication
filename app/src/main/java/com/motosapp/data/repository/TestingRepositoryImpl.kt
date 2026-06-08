package com.motosapp.data.repository
import com.motosapp.data.remote.api.TestingApi
import com.motosapp.domain.model.Testing
import com.motosapp.domain.model.TestingPayload
import com.motosapp.domain.repository.TestingRepository
import javax.inject.Inject

class TestingRepositoryImpl @Inject constructor(
    private val api: TestingApi
) : TestingRepository {
    override suspend fun getTestings(): Result<List<Testing>> = runCatching { api.getTestings() }
    override suspend fun createTesting(payload: TestingPayload): Result<Testing> = runCatching { api.createTesting(payload) }
}
