package com.cashbacks.app.di

import com.cashbacks.app.di.modules.AppModule
import com.cashbacks.app.di.modules.CardsModule
import com.cashbacks.app.di.modules.CashbacksModule
import com.cashbacks.app.di.modules.CategoriesModule
import com.cashbacks.app.di.modules.DataModule
import com.cashbacks.app.di.modules.SettingsModule
import com.cashbacks.app.di.modules.ShopsModule
import com.cashbacks.app.ui.features.bankcard.BankCardEditingViewModel
import com.cashbacks.app.ui.features.bankcard.BankCardFeature
import com.cashbacks.app.ui.features.bankcard.BankCardViewingViewModel
import com.cashbacks.app.ui.features.cashback.CashbackFeature
import com.cashbacks.app.ui.features.cashback.CashbackViewModel
import com.cashbacks.app.ui.features.category.CategoryFeature
import com.cashbacks.app.ui.features.category.editing.CategoryEditingViewModel
import com.cashbacks.app.ui.features.category.viewing.CategoryViewingViewModel
import com.cashbacks.app.ui.features.home.HomeFeature
import com.cashbacks.app.ui.features.home.cards.CardsViewModel
import com.cashbacks.app.ui.features.home.cashbacks.CashbacksViewModel
import com.cashbacks.app.ui.features.home.categories.CategoriesViewModel
import com.cashbacks.app.ui.features.home.shops.ShopsViewModel
import com.cashbacks.app.ui.features.settings.SettingsFeature
import com.cashbacks.app.ui.features.settings.SettingsViewModel
import com.cashbacks.app.ui.features.shop.ShopFeature
import com.cashbacks.app.ui.features.shop.ShopViewModel
import com.cashbacks.app.viewmodel.MainViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        SettingsModule::class,
        DataModule::class,
        CategoriesModule::class,
        ShopsModule::class,
        CashbacksModule::class,
        CardsModule::class
    ]
)
interface AppComponent {
    fun mainViewModel(): MainViewModel

    fun settingsViewModel(): SettingsViewModel

    fun categoriesViewModel(): CategoriesViewModel
    fun shopsViewModel(): ShopsViewModel
    fun cashbacksViewModel(): CashbacksViewModel
    fun cardsViewModel(): CardsViewModel

    fun categoryViewerViewModel(): CategoryViewingViewModel.Factory

    fun categoryEditorViewModel(): CategoryEditingViewModel.Factory

    fun cashbackViewModel(): CashbackViewModel.Factory

    fun shopViewModel(): ShopViewModel.Factory

    fun bankCardViewerViewModel(): BankCardViewingViewModel.Factory

    fun bankCardEditorViewModel(): BankCardEditingViewModel.Factory

    fun homeFeature(): HomeFeature

    fun settingsFeature(): SettingsFeature

    fun categoryFeature(): CategoryFeature

    fun shopFeature(): ShopFeature

    fun cashbackFeature(): CashbackFeature

    fun bankCardFeature(): BankCardFeature
}