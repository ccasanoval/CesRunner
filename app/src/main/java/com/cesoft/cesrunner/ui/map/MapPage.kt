package com.cesoft.cesrunner.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.MapCompo
import com.cesoft.cesrunner.ui.common.ToolbarCompo
import com.cesoft.cesrunner.ui.common.rememberMapCompo
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsIntent
import com.cesoft.cesrunner.ui.map.mvi.MapIntent
import com.cesoft.cesrunner.ui.map.mvi.MapSideEffect
import com.cesoft.cesrunner.ui.map.mvi.MapState
import org.koin.androidx.compose.koinViewModel


@Composable
fun MapPage(
    navController: NavController,
    viewModel: MapViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val onBack = { viewModel.execute(MapIntent.Close) }
    MviScreen(
        state = viewModel.state,
        onBackPressed = onBack,
        onSideEffect = { sideEffect: MapSideEffect ->
            viewModel.consumeSideEffect(
                sideEffect = sideEffect,
                navController = navController,
                context = context
            )
        }
    ) { state: MapState ->
        ToolbarCompo(
            title = stringResource(R.string.menu_maps),
            onBack = onBack
        ) {
            when (state) {
                is MapState.Loading -> {
                    viewModel.execute(MapIntent.Load)
                    LoadingCompo()
                }

                is MapState.Init -> {
                    Content(state = state, reduce = viewModel::execute)
                }
            }
        }
    }
}

@Composable
private fun Content(
    state: MapState.Init,
    reduce: (MapIntent) -> Unit
) {
    val context = LocalContext.current
    val mapView = rememberMapCompo(context)
    MapCompo(
        context = context,
        mapView = mapView,
        trackPoints = state.track.points,
        zoom = true,
        location = state.location,
        modifier = Modifier.fillMaxSize()
    )
}