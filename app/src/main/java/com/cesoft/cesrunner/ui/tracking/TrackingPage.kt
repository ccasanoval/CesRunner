package com.cesoft.cesrunner.ui.tracking

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.data.toDateStr
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.home.mvi.HomeIntent
import com.cesoft.cesrunner.ui.theme.SepMax
import com.cesoft.cesrunner.ui.theme.SepMin
import com.cesoft.cesrunner.ui.theme.fonBig
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingIntent
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingState
import kotlinx.coroutines.delay
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
    state: TrackingState.Init,
    reduce: (intent: TrackingIntent) -> Unit,
) {
    //TODO: Update like home screen
    LaunchedEffect(state) {
        while(true) {
            android.util.Log.e("TrackingPage", "TrackingInfo----------------")
            //reduce(TrackingIntent.Refresh)
            delay(30_000)
        }
    }
    Column(
        verticalArrangement = Arrangement.Center,
        //horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(SepMax)
    ) {
        //Text(stringResource(R.string.tra))
        //TODO: Allow changing value..
        Text(
            text = state.currentTracking.name,
            fontWeight = FontWeight.Bold,
            fontSize = fonBig,
            modifier = Modifier.padding(vertical = SepMax)
        )
        InfoRow(stringResource(R.string.distance), "${state.currentTracking.distance} m")
        InfoRow(stringResource(R.string.time_ini), state.currentTracking.timeIni.toDateStr())
        InfoRow(stringResource(R.string.time_end), state.currentTracking.timeEnd.toDateStr())
        InfoRow(stringResource(R.string.speed_max), "${state.currentTracking.speedMax} m/s")
        InfoRow(stringResource(R.string.speed_min), "${state.currentTracking.speedMin} m/s")
        InfoRow(stringResource(R.string.altitude_max), "${state.currentTracking.altitudeMax} m")
        InfoRow(stringResource(R.string.altitude_min), "${state.currentTracking.altitudeMax} m")


        Spacer(modifier = Modifier.padding(SepMax))

        //TODO: Mapa

        Spacer(modifier = Modifier.padding(SepMax))

        //TODO: Alert: seguro que quiere detener la ruta?
        Button(
            onClick = { reduce(TrackingIntent.Stop) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.stop))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.5f).padding(vertical = SepMin)
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.5f)
        )
    }
}


@Preview
@Composable
private fun TrackingPage_Preview() {
    val state = TrackingState.Init(
        currentTracking = TrackDto.Empty.copy(
            name = "Tracking A",
            timeIni = System.currentTimeMillis() - 5*60*60*1000,
            timeEnd = System.currentTimeMillis(),
            distance = 690,
            altitudeMax = 1200,
            altitudeMin = 600,
            speedMax = 15,
            speedMin = 5,
        )
    )
    Surface {
        TrackingInfo(state) { }
    }
}