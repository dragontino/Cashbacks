package com.cashbacks.app.di.modules

import com.cashbacks.domain.repository.ShopRepository
import com.cashbacks.domain.usecase.shops.AddShopUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.EditShopUseCase
import com.cashbacks.domain.usecase.shops.FetchAllShopsUseCase
import com.cashbacks.domain.usecase.shops.FetchShopsFromCategoryUseCase
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers

@Module
class ShopsModule {
    @Provides
    fun provideAddShopUseCase(shopRepository: ShopRepository) = AddShopUseCase(
        repository = shopRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideShopUseCase(shopRepository: ShopRepository) = EditShopUseCase(
        repository = shopRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideDeleteShopUseCase(shopRepository: ShopRepository) = DeleteShopUseCase(
        repository = shopRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideFetchShopsFromCategoryUseCase(shopRepository: ShopRepository) =
        FetchShopsFromCategoryUseCase(repository = shopRepository, dispatcher = Dispatchers.IO)

    @Provides
    fun provideFetchAllShopsUseCase(shopRepository: ShopRepository) =
        FetchAllShopsUseCase(repository = shopRepository, dispatcher = Dispatchers.IO)
}