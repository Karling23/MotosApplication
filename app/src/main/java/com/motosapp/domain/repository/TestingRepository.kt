package com.motosapp.domain.repository
import com.motosapp.domain.model.Testing
import com.motosapp.domain.model.TestingPayload

interface TestingRepository {
    suspend fun getTestings(): Result<List<Testing>>
    suspend fun createTesting(payload: TestingPayload): Result<Testing>
}
