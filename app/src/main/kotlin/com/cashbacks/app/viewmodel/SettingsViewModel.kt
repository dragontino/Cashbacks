package com.cashbacks.app.viewmodel

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.R
import com.cashbacks.app.model.ColorDesignMapper.title
import com.cashbacks.domain.model.ColorDesign
import com.cashbacks.domain.model.Settings
import com.cashbacks.domain.usecase.SettingsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1

class SettingsViewModel(
    private val useCase: SettingsUseCase
) : ViewModel() {
    enum class State {
        Loading,
        Ready
    }

    private val _settings = mutableStateOf(Settings())
    val settings = derivedStateOf { _settings.value }

    private val _state = mutableStateOf(State.Ready)
    val state = derivedStateOf { _state.value }

    init {
        flow {
            emit(State.Loading)
            delay(150)
            emit(State.Ready)
        }.onEach { _state.value = it }.launchIn(viewModelScope)

        useCase.fetchSettings()
            .onEach { _settings.value = it }
            .launchIn(viewModelScope)
    }



    /**
     * Функция, позволяющая обновить значение параметра с именем [property], изменив его на [value]
     *
     * Пример использования:
     * ```
     * viewModel.updateSettingsProperty(Settings::colorDesign, 1)
     * ```
     * @param property имя параметра, который нужно обновить
     * @param value новое значение параметра
     * @param error блок результата (не обязательный)
     */
    fun updateSettingsProperty(
        property: KProperty1<Settings, Any>,
        value: Any,
        error: (exception: Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            _state.value = State.Loading
            delay(200)
            val result = useCase.updateSettingsProperty(property.name, value)
            _state.value = State.Ready
            result.exceptionOrNull()?.let { error(it) }
        }
    }



    fun constructThemeText(
        currentTheme: ColorDesign,
        isDark: Boolean,
        context: Context
    ) = buildString {
        append(currentTheme.title(context).lowercase())

        when (currentTheme) {
            ColorDesign.System -> {
                val textToAppend = if (isDark) {
                    context.getString(R.string.dark_theme)
                } else {
                    context.getString(R.string.light_theme)
                }

                append("\n", context.getString(R.string.now, textToAppend).lowercase())
            }

            else -> return@buildString
        }
    }
}