package com.cashbacks.app.di.modules

import com.cashbacks.domain.repository.CategoryRepository
import com.cashbacks.domain.usecase.categories.AddCategoryUseCase
import com.cashbacks.domain.usecase.categories.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.categories.FetchCategoriesUseCase
import com.cashbacks.domain.usecase.categories.GetCategoryUseCase
import com.cashbacks.domain.usecase.categories.UpdateCategoryUseCase
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers

@Module
class CategoriesModule {
    @Provides
    fun provideAddCategoryUseCase(categoryRepository: CategoryRepository) = AddCategoryUseCase(
        repository = categoryRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideFetchCategoriesUseCase(categoryRepository: CategoryRepository) = FetchCategoriesUseCase(
        repository = categoryRepository, dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideUpdateCategoryUseCase(categoryRepository: CategoryRepository) = UpdateCategoryUseCase(
        categoryRepository = categoryRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideGetCategoryUseCase(categoryRepository: CategoryRepository) = GetCategoryUseCase(
        repository = categoryRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideDeleteCategoryUseCase(categoryRepository: CategoryRepository) = DeleteCategoryUseCase(
        repository = categoryRepository,
        dispatcher = Dispatchers.IO
    )
}