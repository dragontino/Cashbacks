package com.cashbacks.app.viewmodel

import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.model.Settings
import com.cashbacks.domain.usecase.cashbacks.DeleteExpiredCashbacksUseCase
import com.cashbacks.domain.usecase.settings.SettingsUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

class MainViewModel @Inject constructor(
    settingsUseCase: SettingsUseCase,
    private val deleteExpiredCashbacksUseCase: DeleteExpiredCashbacksUseCase,
    private val messageHandler: MessageHandler
) : ViewModel() {

    private val _settings = mutableStateOf(Settings())
    val settings = derivedStateOf { _settings.value }

    fun statusBarStyle(isDarkTheme: Boolean) = when {
        isDarkTheme.xor(settings.value.dynamicColor) ->
            SystemBarStyle.dark(scrim = Color.TRANSPARENT)
        else -> SystemBarStyle.light(scrim = Color.TRANSPARENT, darkScrim = Color.TRANSPARENT)
    }

    fun navigationBarStyle(isDarkTheme: Boolean) = when {
        isDarkTheme -> SystemBarStyle.dark(scrim = Color.TRANSPARENT)
        else -> SystemBarStyle.light(scrim = Color.TRANSPARENT, darkScrim = Color.TRANSPARENT)
    }

    init {
        settingsUseCase.fetchSettings()
            .onEach { _settings.value = it }
            .launchIn(viewModelScope)
    }


    fun deleteExpiredCashbacks(
        success: () -> Unit,
        failure: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            deleteExpiredCashbacksUseCase.deleteExpiredCashbacks(
                today = today,
                onSuccess = success,
                onFailure = { messageHandler.getExceptionMessage(it)?.let(failure) }
            )
        }
    }
}