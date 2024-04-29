package com.cashbacks.app.di.modules

import com.cashbacks.domain.repository.ShopRepository
import com.cashbacks.domain.usecase.shops.AddShopUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.FetchAllShopsUseCase
import com.cashbacks.domain.usecase.shops.FetchShopsFromCategoryUseCase
import com.cashbacks.domain.usecase.shops.GetShopUseCase
import com.cashbacks.domain.usecase.shops.SearchShopsUseCase
import com.cashbacks.domain.usecase.shops.UpdateShopUseCase
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
    fun provideUpdateShopUseCase(shopRepository: ShopRepository) = UpdateShopUseCase(
        repository = shopRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideDeleteShopUseCase(shopRepository: ShopRepository) = DeleteShopUseCase(
        repository = shopRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideGetShopUseCase(shopRepository: ShopRepository) = GetShopUseCase(
        repository = shopRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideFetchShopsFromCategoryUseCase(shopRepository: ShopRepository) =
        FetchShopsFromCategoryUseCase(repository = shopRepository, dispatcher = Dispatchers.IO)

    @Provides
    fun provideFetchAllShopsUseCase(shopRepository: ShopRepository) =
        FetchAllShopsUseCase(repository = shopRepository, dispatcher = Dispatchers.IO)

    @Provides
    fun provideSearchShopsUseCase(shopRepository: ShopRepository) =
        SearchShopsUseCase(repository = shopRepository, dispatcher = Dispatchers.IO)
}