package com.cashbacks.features.share.domain.repo

interface ShareDataRepository {
    suspend fun importData(): Result<Unit>

    suspend fun exportData(): Result<String>
}