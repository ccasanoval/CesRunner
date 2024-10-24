package com.cesoft.cesrunner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.usecase.ReadSettingsUC
import com.cesoft.cesrunner.domain.usecase.SaveSettingsUC
import com.cesoft.cesrunner.ui.settings.mvi.SettingsIntent
import com.cesoft.cesrunner.ui.settings.mvi.SettingsSideEffect
import com.cesoft.cesrunner.ui.settings.mvi.SettingsState
import com.cesoft.cesrunner.ui.settings.mvi.SettingsTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

class SettingsViewModel(
    val readSettings: ReadSettingsUC,
    val saveSettings: SaveSettingsUC,
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

    override val state = reducer.state
    override fun execute(intent: SettingsIntent) { reducer.executeIntent(intent) }

//    private fun loading() = flow {
//        emit(SettingsTransform.SetIsLoggingIn(isLoggingIn = true))
//        delay(300)
//        emit(LoginTransform.SetIsLoggingIn(isLoggingIn = false))
//    }

    private fun executeIntent(intent: SettingsIntent) =
        when(intent) {
            SettingsIntent.Load -> exeLoad()
            SettingsIntent.Close -> exeClose()
            is SettingsIntent.Save -> exeSave(intent.settings)
        }

    private fun exeClose() = flow {
        emit(SettingsTransform.AddSideEffect(SettingsSideEffect.Close))
    }
    private fun exeLoad() = flow {
        val ret = readSettings()
        val settings = ret.getOrNull()
        settings?.let {
            emit(SettingsTransform.Load(SettingsState.Init(it)))//TODO: refactor this shit
        } ?: run {
            //TODO: show failure...
        }
    }
    private fun exeSave(settings: SettingsDto) = flow {
        android.util.Log.e("AAA", "SAVE----------------------- ${settings}")
        saveSettings(settings)
        emit(SettingsTransform.AddSideEffect(SettingsSideEffect.Close))
    }

    fun consumeSideEffect(
        sideEffect: SettingsSideEffect,
        navController: NavController
    ) {
        when(sideEffect) {
            SettingsSideEffect.Close -> {
                android.util.Log.e("AAA", "------------------- 0")
                navController.popBackStack()
//                navController.navigate(route = Page.Home.route) {
//                    launchSingleTop = true
//                    restoreState = true
//                }
            }
        }
    }

}