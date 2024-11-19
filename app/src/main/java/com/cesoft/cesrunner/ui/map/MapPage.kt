package com.cesoft.cesrunner.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.MapCompo
import com.cesoft.cesrunner.ui.common.rememberMapCompo
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
    MviScreen(
        state = viewModel.state,
        onSideEffect = { sideEffect: MapSideEffect ->
            viewModel.consumeSideEffect(
                sideEffect = sideEffect,
                navController = navController,
                context = context
            )
        },
        onBackPressed = {
            viewModel.execute(MapIntent.Close)
        },
    ) { state: MapState ->
        when(state) {
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