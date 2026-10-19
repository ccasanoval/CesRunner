package com.cesoft.cesrunner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.domain.entity.ApiKeysDto
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.usecase.ReadApiKeysUC
import com.cesoft.cesrunner.domain.usecase.ReadSettingsUC
import com.cesoft.cesrunner.domain.usecase.SaveApiKeysUC
import com.cesoft.cesrunner.domain.usecase.SaveSettingsUC
import com.cesoft.cesrunner.ui.settings.mvi.SettingsIntent
import com.cesoft.cesrunner.ui.settings.mvi.SettingsSideEffect
import com.cesoft.cesrunner.ui.settings.mvi.SettingsState
import com.cesoft.cesrunner.ui.settings.mvi.SettingsTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val readSettings: ReadSettingsUC,
    private val saveSettings: SaveSettingsUC,
    private val readApiKeys: ReadApiKeysUC,
    private val saveApiKeys: SaveApiKeysUC,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<SettingsIntent, State<SettingsState, SettingsSideEffect>> {

    private val reducer =
        Reducer(
            coroutineScope = viewModelScope,
            defaultDispatcher = coroutineDispatcher,
            initialInnerState = SettingsState.Loading,
            logger = null,
            intentExecutor = this::executeIntent,
        )

    // Each intent runs in its own coroutine: saving from a single collector keeps the writes
    // in order, and as these are StateFlows, only the latest value is written
    private val settingsToSave = MutableStateFlow<SettingsDto?>(null)
    private val apiKeysToSave = MutableStateFlow<ApiKeysDto?>(null)
    init {
        viewModelScope.launch { settingsToSave.filterNotNull().collect { saveSettings(it) } }
        viewModelScope.launch { apiKeysToSave.filterNotNull().collect { saveApiKeys(it) } }
    }

    override val state = reducer.state
    override fun execute(intent: SettingsIntent) { reducer.executeIntent(intent) }

    private fun executeIntent(intent: SettingsIntent) =
        when(intent) {
            SettingsIntent.Load -> exeLoad()
            is SettingsIntent.Save -> exeSave(intent.settings)
            is SettingsIntent.SaveApiKeys -> exeSaveApiKeys(intent.apiKeys)
        }

    private fun exeLoad() = flow {
        val settings = readSettings().getOrNull() ?: SettingsDto.Empty
        val apiKeys = readApiKeys().getOrNull() ?: ApiKeysDto.Empty
        emit(SettingsTransform.Load(SettingsState.Init(settings, apiKeys)))
    }

    // Each change is saved as soon as it's made, the screen keeps its own state so it isn't reloaded
    private fun exeSave(settings: SettingsDto) = flow<SettingsTransform.Load> {
        settingsToSave.value = settings
    }
    private fun exeSaveApiKeys(apiKeys: ApiKeysDto) = flow<SettingsTransform.Load> {
        apiKeysToSave.value = apiKeys
    }
}
