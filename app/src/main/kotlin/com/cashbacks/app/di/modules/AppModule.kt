package com.cashbacks.app.di.modules

import com.cashbacks.app.app.App
import com.cashbacks.app.model.MessageHandlerImpl
import com.cashbacks.app.ui.features.bankcard.BankCardFeature
import com.cashbacks.app.ui.features.cashback.CashbackFeature
import com.cashbacks.app.ui.features.category.CategoryFeature
import com.cashbacks.app.ui.features.home.HomeFeature
import com.cashbacks.app.ui.features.settings.SettingsFeature
import com.cashbacks.app.ui.features.shop.ShopFeature
import com.cashbacks.domain.model.MessageHandler
import dagger.Module
import dagger.Provides

@Module
class AppModule(private val application: App) {
    @Provides
    fun providesAppExceptionMessage(): MessageHandler {
        return MessageHandlerImpl(application)
    }

    @Provides
    fun providesHomeFeature(): HomeFeature {
        return HomeFeature(application)
    }

    @Provides
    fun providesSettingsFeature(): SettingsFeature {
        return SettingsFeature(application)
    }

    @Provides
    fun providesCategoryFeature(): CategoryFeature {
        return CategoryFeature(application)
    }

    @Provides
    fun providesShopFeature(): ShopFeature {
        return ShopFeature(application)
    }

    @Provides
    fun providesCashbackFeature(): CashbackFeature {
        return CashbackFeature(application)
    }

    @Provides
    fun providesBankCardFeature(): BankCardFeature {
        return BankCardFeature(application)
    }
}