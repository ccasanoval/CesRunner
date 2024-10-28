package com.cesoft.cesrunner.ui.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.ui.home.HomeViewModel
import com.cesoft.cesrunner.ui.home.mvi.HomeIntent
import com.cesoft.cesrunner.ui.home.mvi.HomeState
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrackingPage(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    MviScreen(
        state = viewModel.state,
        onSideEffect = { sideEffect ->
            viewModel.consumeSideEffect(
                sideEffect = sideEffect,
                navController = navController,
                context = context
            )
        },
        onBackPressed = {
            viewModel.execute(HomeIntent.Close)
        },
    ) { view ->
        Content(state = view, reduce = viewModel::execute)
    }
}

@Composable
private fun Content(
    state: HomeState,
    reduce: (intent: HomeIntent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
       Text("TRACKING PAGE")
    }
}
