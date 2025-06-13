package com.cashbacks.features.home.impl.di

import com.cashbacks.features.home.impl.screens.cards.CardsViewModel
import com.cashbacks.features.home.impl.screens.cashbacks.CashbacksViewModel
import com.cashbacks.features.home.impl.screens.categories.CategoriesViewModel
import com.cashbacks.features.home.impl.screens.shops.ShopsViewModel
import com.cashbacks.features.home.impl.viewmodel.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val HomeModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::CategoriesViewModel)
    viewModelOf(::ShopsViewModel)
    viewModelOf(::CashbacksViewModel)
    viewModelOf(::CardsViewModel)
}