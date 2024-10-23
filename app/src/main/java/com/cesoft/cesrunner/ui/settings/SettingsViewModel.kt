package com.cesoft.cesrunner.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.Page
import com.cesoft.cesrunner.ui.home.mvi.HomeSideEffect
import com.cesoft.cesrunner.ui.settings.mvi.SettingsIntent
import com.cesoft.cesrunner.ui.settings.mvi.SettingsSideEffect
import com.cesoft.cesrunner.ui.settings.mvi.SettingsState
import com.cesoft.cesrunner.ui.settings.mvi.SettingsTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

class SettingsViewModel(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<SettingsIntent, State<SettingsState, SettingsSideEffect>> {

    private val reducer =
        Reducer<SettingsIntent, SettingsState, SettingsSideEffect>(
            coroutineScope = viewModelScope,
            defaultDispatcher = coroutineDispatcher,
            initialInnerState = SettingsState.Init,
            logger = null,
            intentExecutor = this::executeIntent,
        )

    override val state = reducer.state
    override fun execute(intent: SettingsIntent) {//TODO: Remove executeIntent and use this?
        reducer.executeIntent(intent)
    }

    private fun executeIntent(intent: SettingsIntent) =
        when (intent) {
            SettingsIntent.Close -> executeClose()
        }

    private fun executeClose() = flow {
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