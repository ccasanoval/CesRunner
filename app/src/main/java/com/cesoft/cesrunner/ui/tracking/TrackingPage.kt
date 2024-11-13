package com.cesoft.cesrunner.ui.tracking

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.entity.LocationDto
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.toDateStr
import com.cesoft.cesrunner.toTimeStr
import com.cesoft.cesrunner.ui.common.InfoRow
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.MapCompo
import com.cesoft.cesrunner.ui.common.TurnLocationOnDialog
import com.cesoft.cesrunner.ui.common.rememberMapCompo
import com.cesoft.cesrunner.ui.theme.SepMax
import com.cesoft.cesrunner.ui.theme.SepMin
import com.cesoft.cesrunner.ui.theme.fontBig
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingIntent
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingState
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline

//TODO: Counter to ini: Start the route in 3, 2, 1...
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
            ScreenCompo(state, reduce)
        }
    }
}

@Composable
private fun ScreenCompo(
    state: TrackingState.Init,
    reduce: (intent: TrackingIntent) -> Unit,
) {
    //android.util.Log.e("TrackingPage", "ScreenCompo---------------------------")
    val context = LocalContext.current
    val showAlert = remember { mutableStateOf(true) }
    TurnLocationOnDialog(showAlert) {
        showAlert.value = false
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
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

        var points by remember { mutableStateOf(listOf<TrackPointDto>()) }
        var track by remember { mutableStateOf(TrackDto.Empty) }
        LaunchedEffect(state) {
            state.trackFlow.collect {
                it?.let {
                    track = it
                    points = it.points
                }
            }
        }
        //TODO: Make it scrollable like in track details...
        TrackData(track, Modifier.weight(.4f).zIndex(999f))
        Spacer(Modifier.size(SepMax))

        val mapView = rememberMapCompo(context)
        MapCompo(context, mapView, points, Modifier.weight(.4f))
    }
}

@Composable
private fun TrackData(
    track: TrackDto,
    modifier: Modifier = Modifier
) {
    android.util.Log.e("TrackingPage", "TrackData--------- dis = ${track.distance} // pnts = ${track.points.size} ")
    val distance = "${track.distance} m"
    val timeIni = track.timeIni.toDateStr()
    val timeEnd = track.timeEnd.toDateStr()
    val duration = track.timeEnd - track.timeIni
    val altitudes = track.points.map { it.altitude }
    val altitudeMax = altitudes.maxOrNull() ?: 0.0
    val altitudeMin = altitudes.minOrNull() ?: 0.0
    val altitude = String.format(//"$altitudeMin - $altitudeMax m"
        Locale.current.platformLocale,
        "%.0f - %.0f m (dif %.0f m)",
        altitudeMin, altitudeMax, altitudeMax - altitudeMin
    )
    val speeds = track.points.map { it.speed }
    val speedMax = speeds.maxOrNull() ?: 0f
    val speedMed = speeds.average()
    val speed = String.format(
        Locale.current.platformLocale,
        //"%.0f - %.0f Km/h (%d - %d m/s)",
        //speedMin*3.6, speedMax*3.6, speedMin, speedMax)
        "%.0f Km/h (max %.0f Km/h)",
        speedMed*3.6, speedMax*3.6
    )
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            Text(
                text = track.name,
                fontWeight = FontWeight.Bold,
                fontSize = fontBig,
                modifier = Modifier.padding(vertical = SepMin)
            )
        }
        item { InfoRow(stringResource(R.string.distance), distance) }
        item { InfoRow(stringResource(R.string.time), duration.toTimeStr()) }
        item { InfoRow(stringResource(R.string.time_ini), timeIni) }
        item { InfoRow(stringResource(R.string.time_end), timeEnd) }
        item { InfoRow(stringResource(R.string.speed), speed) }
        item { InfoRow(stringResource(R.string.altitude), altitude) }
        item { InfoRow(stringResource(R.string.points), track.points.size.toString()) }
        item { Spacer(Modifier.padding(vertical = SepMax*5)) }
    }
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun TrackData_Preview() {
    val timeIni = System.currentTimeMillis() - 5*60*60*1000 - 35*60*1000 - 45*1000
    val track = TrackDto.Empty.copy(
        name = "Tracking A",
        timeIni = timeIni,
        timeEnd = System.currentTimeMillis(),
        distance = 690,
        points = listOf(
            TrackPointDto(
                id = 69,
                latitude = 0.0,
                longitude = 0.0,
                time = 0,
                accuracy = 0f,
                provider = "",
                altitude = 50.0,
                bearing = 0f,
                speed = 5f)
        )
    )
    val state = TrackingState.Init(trackFlow = flowOf(track))
    Surface(modifier = Modifier.fillMaxSize()) {
        TrackData(track)
    }
}
