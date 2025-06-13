package com.cashbacks.features.cashback.domain.usecase

import com.cashbacks.features.cashback.domain.model.MeasureUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Currency
import java.util.Locale

interface GetMeasureUnitsUseCase {
    suspend operator fun invoke(locale: Locale = Locale.getDefault()): List<MeasureUnit>
}


internal class GetMeasureUnitsUseCaseImpl(
    private val dispatcher: CoroutineDispatcher
) : GetMeasureUnitsUseCase {
    override suspend fun invoke(locale: Locale): List<MeasureUnit> {
        return buildList {
            add(MeasureUnit.Percent)
            getLocalCurrencies(locale).forEach {
                add(MeasureUnit.Currency(it))
            }
        }
    }


    // TODO: make locale based on gps coords
    private suspend fun getLocalCurrencies(locale: Locale): List<Currency> =
        suspendCancellableCoroutine { continuation ->
            val localeCurrency = Currency.getInstance(locale)
            val resultCurrencies = buildSet {
                listOf("EUR", "USD").forEach {
                    add(Currency.getInstance(it))
                }
                add(localeCurrency)
            }.sortedBy { it.getDisplayName(locale) }

            continuation.resumeWith(Result.success(resultCurrencies))
        }
}