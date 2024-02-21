package com.cashbacks.app.di.modules

import com.cashbacks.domain.repository.CashbackRepository
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.DeleteExpiredCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.EditCashbackUseCase
import com.cashbacks.domain.usecase.cashbacks.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.SearchCashbacksUseCase
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers

@Module
class CashbacksModule {
    @Provides
    fun provideDeleteCashbacksUseCase(cashbackRepository: CashbackRepository) = DeleteCashbacksUseCase(
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

    @Provides
    fun providesSearchCashbacksUseCase(cashbackRepository: CashbackRepository) =
        SearchCashbacksUseCase(repository = cashbackRepository, dispatcher = Dispatchers.IO)
    
    @Provides
    fun provideDeleteExpiredCashbacksUseCase(cashbackRepository: CashbackRepository) =
        DeleteExpiredCashbacksUseCase(repository = cashbackRepository, dispatcher = Dispatchers.IO)
}