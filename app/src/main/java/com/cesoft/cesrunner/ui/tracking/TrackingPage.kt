package com.cesoft.cesrunner.ui.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.theme.SepMax
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingIntent
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingState
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrackingPage(
    navController: NavController,
    viewModel: TrackingViewModel = koinViewModel(),
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
            viewModel.execute(TrackingIntent.Close)
        },
    ) { view ->
        Content(state = view, reduce = viewModel::execute)
    }
}

@Composable
private fun Content(
    state: TrackingState,
    reduce: (intent: TrackingIntent) -> Unit,
) {
    android.util.Log.e("TrackingPage", "Content----------- $state")
    when(state) {
        is TrackingState.Loading -> {
            reduce(TrackingIntent.Load)
            LoadingCompo()
        }
        is TrackingState.Init -> {
            TrackingInfo(state, reduce)
        }
    }
}

@Composable
private fun TrackingInfo(
    state: TrackingState,
    reduce: (intent: TrackingIntent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("TRACKING PAGE")
        Spacer(modifier = Modifier.padding(SepMax))
        //TODO: Alert: seguro que quiere detener la ruta?
        Button(onClick = { reduce(TrackingIntent.Stop) }) {
            Text(stringResource(R.string.stop))
        }
    }
}
