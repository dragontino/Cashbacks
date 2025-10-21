package com.cashbacks.app.di

import com.cashbacks.app.di.modules.AppModule
import com.cashbacks.core.database.di.DatabaseModule
import com.cashbacks.core.network.di.NetworkModule
import com.cashbacks.features.bankcard.data.di.BankCardDataModule
import com.cashbacks.features.bankcard.domain.di.BankCardDomainModule
import com.cashbacks.features.bankcard.presentation.impl.di.BankCardPresentationModule
import com.cashbacks.features.cashback.data.di.CashbackDataModule
import com.cashbacks.features.cashback.domain.di.CashbackDomainModule
import com.cashbacks.features.cashback.presentation.impl.di.CashbackPresentationModule
import com.cashbacks.features.category.data.di.CategoryDataModule
import com.cashbacks.features.category.domain.di.CategoryDomainModule
import com.cashbacks.features.category.presentation.impl.di.CategoryPresentationModule
import com.cashbacks.features.home.impl.di.HomeModule
import com.cashbacks.features.settings.data.di.SettingsDataModule
import com.cashbacks.features.settings.domain.di.SettingsDomainModule
import com.cashbacks.features.settings.presentation.di.SettingsPresentationModule
import com.cashbacks.features.share.data.di.ShareDataModule
import com.cashbacks.features.share.domain.di.ShareDomainModule
import com.cashbacks.features.shop.data.di.ShopDataModule
import com.cashbacks.features.shop.domain.di.ShopDomainModule
import com.cashbacks.features.shop.presentation.impl.di.ShopPresentationModule

private val SettingsModules = listOf(
    SettingsDomainModule,
    SettingsDataModule,
    SettingsPresentationModule
)


private val BankCardModules = listOf(
    BankCardDomainModule,
    BankCardDataModule,
    BankCardPresentationModule
)


private val CashbackModules = listOf(
    CashbackDomainModule,
    CashbackDataModule,
    CashbackPresentationModule
)


private val ShopModules = listOf(
    ShopDomainModule,
    ShopDataModule,
    ShopPresentationModule
)


private val CategoryModules = listOf(
    CategoryDomainModule,
    CategoryDataModule,
    CategoryPresentationModule
)


private val ShareModules = listOf(
    ShareDomainModule,
    ShareDataModule
)


val ApplicationModules = buildList {
    addAll(SettingsModules)
    addAll(BankCardModules)
    addAll(CashbackModules)
    addAll(ShopModules)
    addAll(CategoryModules)
    addAll(ShareModules)
    add(DatabaseModule)
    add(NetworkModule)
    add(HomeModule)
    add(ShareDomainModule)
    add(AppModule)
}