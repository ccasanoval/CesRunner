package com.cesoft.cesrunner.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.toDistanceStr
import com.cesoft.cesrunner.ui.common.ErrorCompo
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.TurnLocationOnDialog
import com.cesoft.cesrunner.ui.home.mvi.HomeIntent
import com.cesoft.cesrunner.ui.home.mvi.HomeState
import com.cesoft.cesrunner.ui.theme.SepMed
import com.cesoft.cesrunner.ui.theme.SepMin
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel


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
    ) { view ->
        //android.util.Log.e("HomePage", "--------HomePage----- $view")
        when(view) {
            is HomeState.Loading -> {
                viewModel.execute(HomeIntent.Load)
                LoadingCompo()
            }
            is HomeState.Init -> {
                Content(state = view, reduce = viewModel::execute)
            }
        }
    }
}

@Composable
private fun Content(
    state: HomeState.Init,
    reduce: (intent: HomeIntent) -> Unit,
) {
    android.util.Log.e("HomePage", "Content---------------- 0000 ")
    var track by remember { mutableStateOf<TrackDto?>(null) }
    LaunchedEffect(state) {
//        while(true) {
//            android.util.Log.e("HomePage", "Content----------------")
//            reduce(HomeIntent.Refresh)
//            delay(30_000)
//        }
        state.trackFlow.collect { t ->
            android.util.Log.e("HomePage", "Content---------------- a $t")
            t?.let { track = it }
        }
    }
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
                    text = stringResource(R.string.tracking_on),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(SepMin)
                )
                Text(
                    text = track?.name ?:"",
                    modifier = Modifier.padding(SepMin)
                )
                val distanceStr = track?.distance?.toDistanceStr() ?: ""
                //val speed = state.currentTrack.speedMax//TODO: Current Speed
                Text(
                    text = distanceStr,
                    modifier = Modifier.padding(SepMin)
                )
            }
            else {
                Spacer(modifier = Modifier.padding(SepMin))
                Spacer(modifier = Modifier.padding(SepMin))
            }

            HomeButton(
                title = stringResource(if(isTracking) R.string.menu_check else R.string.menu_start),
                onClick = {
                    reduce(HomeIntent.GoStart)
                }
            )
            HomeButton(
                title = stringResource(R.string.menu_settings),
                onClick = { reduce(HomeIntent.GoSettings) }
            )
            HomeButton(
                title = stringResource(R.string.menu_tracks),
                onClick = { reduce(HomeIntent.GoTracks) }
            )
            HomeButton(
                title = stringResource(R.string.menu_maps),
                onClick = { reduce(HomeIntent.GoMap) }
            )
            HomeButton(
                title = stringResource(R.string.menu_gnss),
                onClick = { reduce(HomeIntent.GoMap) }
            )
        }
    }
}

@Composable
private fun HomeButton(
    title: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(SepMed)
            .fillMaxWidth(.6f)
    ) {
        Text(title)
    }
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun HomePage_Preview() {
    val state = HomeState.Init(
        trackFlow = flowOf(TrackDto(id = 69, name = "Tracking A")),
        error = AppError.NetworkError,
    )
    Content(state) { }
}