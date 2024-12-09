package com.cesoft.cesrunner.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.toDistanceStr
import com.cesoft.cesrunner.toTimeStr
import com.cesoft.cesrunner.ui.common.ErrorCompo
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.TurnLocationOnDialog
import com.cesoft.cesrunner.ui.home.mvi.HomeIntent
import com.cesoft.cesrunner.ui.home.mvi.HomeState
import com.cesoft.cesrunner.ui.theme.SepMed
import com.cesoft.cesrunner.ui.theme.SepMin
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.compose.koinViewModel

//TODO: Show the GPS availability and current position.... or No location fixed...


@Composable
fun HomePage(
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
    ) { state ->
        when(state) {
            is HomeState.Loading -> {
                viewModel.execute(HomeIntent.Load)
                LoadingCompo()
            }
            is HomeState.Init -> {
                Content(state = state, reduce = viewModel::execute)
            }
        }
    }
}

@Composable
private fun Content(
    state: HomeState.Init,
    reduce: (intent: HomeIntent) -> Unit,
) {
    val track by state.trackFlow.collectAsStateWithLifecycle()
        //?: remember { mutableStateOf<TrackDto?>(null) }
    Surface {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            state.error?.let {
                ErrorCompo(it)
            }
            val isTracking = track?.isCreated == true
            if (isTracking) {
                val context = LocalContext.current
                val showAlert = remember { mutableStateOf(true) }
                TurnLocationOnDialog(showAlert) {
                    showAlert.value = false
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                }
                Text(
                    text = stringResource(R.string.tracking_on) + " " + track?.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(SepMin)
                )
                val trackData =
                    stringResource(R.string.distance) + " " +
                    track?.distance?.toDistanceStr() + " / " +
                    stringResource(R.string.time) + " " +
                    track?.time?.toTimeStr()
                Text(
                    text = trackData,
                    modifier = Modifier.padding(SepMin)
                )
            }
            else {
                Spacer(modifier = Modifier.padding(SepMin))
                Spacer(modifier = Modifier.padding(SepMin))
            }

            val modifier = Modifier.fillMaxWidth(.8f).padding(SepMed)
            val title = if(isTracking) R.string.menu_check else R.string.menu_start
            HomeButton(
                title = title,
                icon = R.mipmap.ic_run,
                modifier = modifier,
                onClick = { reduce(HomeIntent.GoStart) }
            )
            HomeButton(
                title = R.string.menu_tracks,
                icon = R.mipmap.ic_list,
                modifier = modifier,
                onClick = { reduce(HomeIntent.GoTracks) }
            )
            HomeButton(
                title = R.string.menu_maps,
                icon = R.mipmap.ic_map,
                modifier = modifier,
                onClick = { reduce(HomeIntent.GoMap) }
            )
            HomeButton(
                title = R.string.menu_settings,
                icon = R.mipmap.ic_settings,
                modifier = modifier,
                onClick = { reduce(HomeIntent.GoSettings) }
            )
//                HomeButton(
//                    title = R.string.menu_gnss,
//                    icon = R.mipmap.ic_sat,
//                    modifier = modifier,
//                    onClick = { reduce(HomeIntent.GoGnss) }
//                )
        }
    }
}

@Composable
private fun HomeButton(
    @StringRes title: Int,
    icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
        )
        Text(
            text = stringResource(title),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(.9f)
        )
        Box(modifier = Modifier.padding(SepMed))
    }

}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun HomePage_Preview() {
    val state = HomeState.Init(
        //trackFlow = flowOf(TrackDto(id = 69, name = "Tracking A")),
        //trackIdFlow = MutableStateFlow(0L),
        trackFlow = MutableStateFlow(TrackDto.Empty),
        error = AppError.NetworkError,
    )
    Content(state) { }
}
/*
@Preview
@Composable
private fun HomePage_Preview() {
    val state = HomeState.Init(
        //trackFlow = flowOf(TrackDto(id = 69, name = "Tracking A")),
        trackIdFlow = trackIdFlow,
        trackFlow = null,
        error = AppError.NetworkError,
    )
    Content(state) { }
}*/