package com.cashbacks.app.di

import com.cashbacks.app.di.modules.CardsModule
import com.cashbacks.app.di.modules.CashbacksModule
import com.cashbacks.app.di.modules.CategoriesModule
import com.cashbacks.app.di.modules.DataModule
import com.cashbacks.app.di.modules.SettingsModule
import com.cashbacks.app.di.modules.ShopsModule
import com.cashbacks.app.viewmodel.BankCardEditorViewModel
import com.cashbacks.app.viewmodel.BankCardViewerViewModel
import com.cashbacks.app.viewmodel.CardsViewModel
import com.cashbacks.app.viewmodel.CashbackViewModel
import com.cashbacks.app.viewmodel.CategoriesViewModel
import com.cashbacks.app.viewmodel.CategoryEditorViewModel
import com.cashbacks.app.viewmodel.CategoryViewerViewModel
import com.cashbacks.app.viewmodel.MainViewModel
import com.cashbacks.app.viewmodel.SettingsViewModel
import com.cashbacks.app.viewmodel.ShopViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
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

    fun categoriesViewModel(): CategoriesViewModel.Factory

    fun categoryViewerViewModel(): CategoryViewerViewModel.Factory

    fun categoryEditorViewModel(): CategoryEditorViewModel.Factory

    fun cashbackViewModel(): CashbackViewModel.Factory

    fun shopViewModel(): ShopViewModel.Factory

    fun cardsViewModel(): CardsViewModel

    fun bankCardViewerViewModel(): BankCardViewerViewModel.Factory

    fun bankCardEditorViewModel(): BankCardEditorViewModel.Factory
}