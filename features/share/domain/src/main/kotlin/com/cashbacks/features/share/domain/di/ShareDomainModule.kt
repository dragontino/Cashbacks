package com.cashbacks.features.share.domain.di

import com.cashbacks.features.share.domain.usecase.ExportDataUseCase
import com.cashbacks.features.share.domain.usecase.ExportDataUseCaseImpl
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val ShareDomainModule = module {
    single<ExportDataUseCase> {
        ExportDataUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }
}