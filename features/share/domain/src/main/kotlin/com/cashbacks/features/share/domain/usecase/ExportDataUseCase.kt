package com.cashbacks.features.share.domain.usecase

import android.util.Log
import com.cashbacks.features.share.domain.repo.ShareDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface ExportDataUseCase {
    suspend operator fun invoke(): Result<String>
}


internal class ExportDataUseCaseImpl(
    private val repository: ShareDataRepository,
    private val dispatcher: CoroutineDispatcher
) : ExportDataUseCase {
    private companion object {
        const val TAG = "ExportDataUseCase"
    }

    override suspend fun invoke(): Result<String> {
        return withContext(dispatcher) {
            repository.exportData().onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}