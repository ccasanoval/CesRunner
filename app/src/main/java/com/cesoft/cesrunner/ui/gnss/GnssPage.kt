package com.cesoft.cesrunner.ui.gnss

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.gnss.mvi.GnssIntent
import com.cesoft.cesrunner.ui.gnss.mvi.GnssSideEffect
import com.cesoft.cesrunner.ui.gnss.mvi.GnssState
import org.koin.androidx.compose.koinViewModel

@Composable
fun GnssPage(
    navController: NavController,
    viewModel: GnssViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    MviScreen(
        state = viewModel.state,
        onSideEffect = { sideEffect: GnssSideEffect ->
            viewModel.consumeSideEffect(
                sideEffect = sideEffect,
                navController = navController,
                context = context
            )
        },
        onBackPressed = {
            viewModel.execute(GnssIntent.Close)
        },
    ) { state: GnssState ->
        when(state) {
            is GnssState.Loading -> {
                viewModel.execute(GnssIntent.Load)
                LoadingCompo()
            }
            is GnssState.Init -> {
                Content(state = state, reduce = viewModel::execute)
            }
        }
    }
}

@Composable
private fun Content(
    state: GnssState.Init,
    reduce: (intent: GnssIntent) -> Unit,
) {
    Text("AAA")
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun GnssPage_Preview() {
    val state = GnssState.Init()
    Surface(modifier = Modifier.fillMaxWidth()) {
        Content(state) { }
    }
}