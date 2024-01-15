package com.cashbacks.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cashbacks.app.app.App

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val application: App
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(
                    settingsUseCase = application.dependencyFactory.provideSettingsUseCase()
                ) as T

            modelClass.isAssignableFrom(CategoriesViewModel::class.java) ->
                CategoriesViewModel(
                    categoryUseCase = application.dependencyFactory.provideCategoriesUseCase()
                ) as T

            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(
                    useCase = application.dependencyFactory.provideSettingsUseCase()
                ) as T

            modelClass.isAssignableFrom(BankCardViewModel::class.java) ->
                BankCardViewModel() as T

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}