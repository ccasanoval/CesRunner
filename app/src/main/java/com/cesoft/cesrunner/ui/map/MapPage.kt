package com.cesoft.cesrunner.ui.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.data.gpx.GpxUtil
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.ui.common.GetCustomContents
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.MapCompo
import com.cesoft.cesrunner.ui.common.ToolbarCompo
import com.cesoft.cesrunner.ui.common.rememberMapCompo
import com.cesoft.cesrunner.ui.map.mvi.MapIntent
import com.cesoft.cesrunner.ui.map.mvi.MapSideEffect
import com.cesoft.cesrunner.ui.map.mvi.MapState
import kotlinx.coroutines.launch
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

@Composable
private fun Content(
    state: MapState.Init,
    reduce: (MapIntent) -> Unit
) {
    val context = LocalContext.current
    val mapView = rememberMapCompo(context)
    var track by remember { mutableStateOf(state.track) }
    val docPicker = rememberLauncherForActivityResult(
        contract = GetCustomContents(),
        onResult = { uri ->
            uri.firstOrNull()?.let {
                track = GpxUtil().import(it, context) ?: TrackDto.Empty
            }
        })
    var menuExpanded by remember { mutableStateOf(false) }
    ToolbarCompo(
        title = stringResource(R.string.menu_maps),
        onBack = { reduce(MapIntent.Close) },
        error = state.error,
        actions = {
            IconButton(onClick = { menuExpanded = !menuExpanded }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = null
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = { Icon(painterResource(R.drawable.upload), null) },
                    //leadingIcon = { Icon(Icons.Default.ShoppingCart, null) },
                    text = { Text(stringResource(R.string.import_gpx)) },
                    onClick = {
                        menuExpanded = false
                        docPicker.launch("*/*")//application/gpx+xml
                    },
                )
            }
        }
    ) {
        MapCompo(
            context = context,
            mapView = mapView,
            trackPoints = track.points,
            location = state.location,
            modifier = Modifier.fillMaxSize()
        )
    }
}