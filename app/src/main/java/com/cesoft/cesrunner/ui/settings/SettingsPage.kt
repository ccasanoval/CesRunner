package com.cesoft.cesrunner.ui.settings

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.ui.settings.mvi.SettingsIntent
import com.cesoft.cesrunner.ui.settings.mvi.SettingsSideEffect
import com.cesoft.cesrunner.ui.settings.mvi.SettingsState
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsPage(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    MviScreen<SettingsState, SettingsSideEffect>(
        state = viewModel.state,
        onSideEffect = { sideEffect ->
            viewModel.consumeSideEffect(
                sideEffect = sideEffect,
                navController = navController
            )
        },
        onBackPressed = {
            viewModel.execute(SettingsIntent.Close)
        },
    ) { view ->
        Content(state = view, reduce = viewModel::execute)
    }
}


@Composable
private fun Content(
    state: SettingsState,
    reduce: (intent: SettingsIntent) -> Unit,
) {
    Text("Settings")
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun SettingsPage_Preview() {
    val navController = rememberNavController()
    Surface { SettingsPage(navController, SettingsViewModel()) }
}