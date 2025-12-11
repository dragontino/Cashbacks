package com.cashbacks.components.login.di

import com.cashbacks.components.login.data.repos.LoginRepositoryImpl
import com.cashbacks.components.login.domain.repos.LoginRepository
import com.cashbacks.components.login.domain.usecase.CheckIsAlreadySignedUpUseCase
import com.cashbacks.components.login.domain.usecase.CheckIsAlreadySignedUpUseCaseImpl
import com.cashbacks.components.login.domain.usecase.SignInUseCase
import com.cashbacks.components.login.domain.usecase.SignInUseCaseImpl
import com.cashbacks.components.login.domain.usecase.SignUpUseCase
import com.cashbacks.components.login.domain.usecase.SignUpUseCaseImpl
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val LoginComponentModule = module {
    single<SignUpUseCase> {
        SignUpUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO,
            context = get()
        )
    }


    single<SignInUseCase> {
        SignInUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO,
            context = get()
        )
    }


    single<CheckIsAlreadySignedUpUseCase> {
        CheckIsAlreadySignedUpUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }


    single<LoginRepository> {
        LoginRepositoryImpl(get(), get())
    }
}