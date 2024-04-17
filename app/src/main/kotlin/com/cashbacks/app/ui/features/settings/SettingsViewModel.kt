package com.cashbacks.app.ui.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ColorDesignMapper.isDark
import com.cashbacks.app.model.ColorDesignMapper.title
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.R
import com.cashbacks.domain.model.ColorDesign
import com.cashbacks.domain.model.Settings
import com.cashbacks.domain.usecase.settings.SettingsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KProperty1

class SettingsViewModel @Inject constructor(
    private val useCase: SettingsUseCase
) : EventsViewModel() {

    private val _settings = mutableStateOf(Settings())
    val settings = derivedStateOf { _settings.value }

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    init {
        flow {
            emit(ViewModelState.Loading)
            delay(250)
            emit(ViewModelState.Viewing)
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
     */
    fun updateSettingsProperty(
        property: KProperty1<Settings, Any>,
        value: Any
    ) {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(200)
            useCase.updateSettingsProperty(
                name = property.name,
                value = value,
                errorMessage = ::showSnackbar
            )
            _state.value = ViewModelState.Viewing
        }
    }

    @Composable
    fun constructThemeText() = buildString {
        val currentTheme = settings.value.colorDesign
        append(currentTheme.title.lowercase())

        when (currentTheme) {
            ColorDesign.System -> {
                val textToAppend = when {
                    currentTheme.isDark -> stringResource(R.string.dark_theme)
                    else -> stringResource(R.string.light_theme)
                }

                append("\n", stringResource(R.string.now, textToAppend).lowercase())
            }

            else -> return@buildString
        }
    }
}