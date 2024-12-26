package com.cashbacks.domain.repository

interface ShareDataRepository {
    suspend fun importData(): Result<Unit>

    suspend fun exportData(): Result<String>
}