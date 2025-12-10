package com.cashbacks.features.login.impl.di

import com.cashbacks.features.login.impl.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val LoginModule = module {
    viewModelOf(::LoginViewModel)
}