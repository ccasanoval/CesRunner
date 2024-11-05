package com.cesoft.cesrunner.ui.tracking

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.data.toDateStr
import com.cesoft.cesrunner.data.toTimeStr
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.OpenStreetMapView
import com.cesoft.cesrunner.ui.theme.Green
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
    android.util.Log.e("TrackingPage", "TrackingInfo---------------------------")
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(SepMax)
    ) {
        //TODO: Alert: seguro que quiere detener la ruta?
        Button(
            onClick = { reduce(TrackingIntent.Stop) },
            modifier = Modifier.fillMaxWidth(.5f),
        ) {
            Text(stringResource(R.string.stop))
        }

        TrackData(state, reduce)

        Spacer(modifier = Modifier.padding(SepMax))

        //TODO: Map
//        OpenStreetMapView(
//            points = state.currentTracking.points,
//            modifier = Modifier.fillMaxWidth().height(300.dp).border(2.dp, Green)
//        )

        Spacer(modifier = Modifier.padding(SepMax))


    }
}

@Composable
private fun TrackData(
    state: TrackingState.Init,
    reduce: (intent: TrackingIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LaunchedEffect(state) {
            while (true) {
                android.util.Log.e("TrackingPage", "TrackingInfo----------------")
                reduce(TrackingIntent.Refresh)
                delay(30_000)
            }
        }
        //TODO: Allow changing value..
        Text(
            text = state.currentTracking.name,
            fontWeight = FontWeight.Bold,
            fontSize = fonBig,
            modifier = Modifier.padding(vertical = SepMin)
        )
        val distance = "${state.currentTracking.distance} m"
        val timeIni = state.currentTracking.timeIni.toDateStr()
        val timeEnd = state.currentTracking.timeEnd.toDateStr()
        val duration = state.currentTracking.timeEnd - state.currentTracking.timeIni
        val speed = "${state.currentTracking.speedMin} - ${state.currentTracking.speedMax} m/s"
        val altitude = "${state.currentTracking.altitudeMin} - ${state.currentTracking.altitudeMax} m"
        //val durationStr = if(duration > 60*60) "${duration}"
        InfoRow(stringResource(R.string.distance), distance)
        InfoRow(stringResource(R.string.time_ini), timeIni)
        InfoRow(stringResource(R.string.time_end), timeEnd)
        InfoRow(stringResource(R.string.time), duration.toTimeStr())
        InfoRow(stringResource(R.string.speed), speed)
        InfoRow(stringResource(R.string.altitude), altitude)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.5f).padding(top = SepMin)
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