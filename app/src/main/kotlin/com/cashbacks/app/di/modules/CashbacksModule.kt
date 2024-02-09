package com.cashbacks.app.di.modules

import com.cashbacks.domain.repository.CashbackRepository
import com.cashbacks.domain.usecase.cashback.CashbackCategoryUseCase
import com.cashbacks.domain.usecase.cashback.CashbackShopUseCase
import com.cashbacks.domain.usecase.cashback.EditCashbackUseCase
import com.cashbacks.domain.usecase.cashback.FetchCashbacksUseCase
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers

@Module
class CashbacksModule {
    @Provides
    fun provideCashbackCategoryUseCase(cashbackRepository: CashbackRepository) = CashbackCategoryUseCase(
        repository = cashbackRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideCashbackShopUseCase(cashbackRepository: CashbackRepository) = CashbackShopUseCase(
        repository = cashbackRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideEditCashbackUseCase(cashbackRepository: CashbackRepository) = EditCashbackUseCase(
        repository = cashbackRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideFetchCashbacksUseCase(cashbackRepository: CashbackRepository) =
        FetchCashbacksUseCase(repository = cashbackRepository, dispatcher = Dispatchers.IO)
}