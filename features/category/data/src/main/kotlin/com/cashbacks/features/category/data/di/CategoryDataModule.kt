package com.cashbacks.features.category.data.di

import com.cashbacks.features.category.data.repo.CategoryRepositoryImpl
import com.cashbacks.features.category.domain.repo.CategoryRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val CategoryDataModule = module {
    single<CategoryRepository> {
        CategoryRepositoryImpl(
            dao = get(),
            context = androidContext()
        )
    }
}