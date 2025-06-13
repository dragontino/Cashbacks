package com.cashbacks.app.ui

import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashbacks.common.utils.today
import com.cashbacks.features.cashback.domain.usecase.DeleteExpiredCashbacksUseCase
import com.cashbacks.features.settings.domain.model.Settings
import com.cashbacks.features.settings.domain.usecase.FetchSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class MainViewModel(
    fetchSettings: FetchSettingsUseCase,
    private val deleteExpiredCashbacks: DeleteExpiredCashbacksUseCase
) : ViewModel() {

    val settingsStateFlow: StateFlow<Settings> = fetchSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

    fun statusBarStyle(isDarkTheme: Boolean) = when {
        isDarkTheme.xor(settingsStateFlow.value.dynamicColor) ->
            SystemBarStyle.Companion.dark(scrim = Color.TRANSPARENT)
        else -> SystemBarStyle.Companion.light(scrim = Color.TRANSPARENT, darkScrim = Color.TRANSPARENT)
    }

    fun navigationBarStyle(isDarkTheme: Boolean) = when {
        isDarkTheme -> SystemBarStyle.Companion.dark(scrim = Color.TRANSPARENT)
        else -> SystemBarStyle.Companion.light(scrim = Color.TRANSPARENT, darkScrim = Color.TRANSPARENT)
    }


    fun deleteExpiredCashbacks(
        success: (Int) -> Unit,
        failure: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            val today = Clock.System.today()
            deleteExpiredCashbacks(today)
                .onSuccess { if (it > 0) success(it) }
                .onFailure { it.message?.let(failure) }
        }
    }
}