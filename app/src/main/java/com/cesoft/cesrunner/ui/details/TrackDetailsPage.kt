package com.cesoft.cesrunner.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.toDateStr
import com.cesoft.cesrunner.toTimeStr
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.ToolbarCompo
import com.cesoft.cesrunner.ui.common.addMyLocation
import com.cesoft.cesrunner.ui.common.rememberMapView
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsIntent
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsState
import com.cesoft.cesrunner.ui.theme.Green
import com.cesoft.cesrunner.ui.theme.SepMax
import com.cesoft.cesrunner.ui.theme.SepMin
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline


@Composable
fun TrackDetailsPage(
    navController: NavController,
    viewModel: TrackDetailsViewModel = koinViewModel(),
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
            viewModel.execute(TrackDetailsIntent.Close)
        },
    ) { state ->
        Content(state = state, reduce = viewModel::execute)
    }
}

@Composable
fun Content(
    state: TrackDetailsState,
    reduce: (TrackDetailsIntent) -> Unit,
) {
    ToolbarCompo(
        title = stringResource(R.string.menu_track_details),
        onBack = { reduce(TrackDetailsIntent.Close) }
    ) {
        when (state) {
            is TrackDetailsState.Loading -> {
                reduce(TrackDetailsIntent.Load)
                LoadingCompo()
            }
            is TrackDetailsState.Init -> {
                Column {
                    TrackData(
                        state = state,
                        reduce = reduce,
                        modifier = Modifier.weight(0.3f)
                    )
                    MapCompo(
                        track = state.track,
                        modifier = Modifier.weight(0.3f).padding(SepMax)
                    )
                }
            }
        }
    }
}

@Composable
fun MapCompo(
    track: TrackDto,
    modifier: Modifier
) {
    val context = LocalContext.current
    val mapView = rememberMapView(context)
    var points by remember { mutableStateOf(listOf<GeoPoint>()) }
    points = track.points.map { p -> GeoPoint(p.latitude, p.longitude) }

    val polyline = Polyline(mapView)
    polyline.color = Green.toArgb()
    polyline.setPoints(points)
    polyline.infoWindow = null
    mapView.overlayManager.add(polyline)

    //TODO: No estamos guardando el ultimo punto, la distancia no es correcta
    android.util.Log.e("TrackingPAge", "MapCompo----------- dis = ${polyline.distance} ")

    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) { view ->
        view.overlays.removeAll { true }
        addMyLocation(context, view)
        view.addOnFirstLayoutListener { v, left, top, right, bottom ->
            mapView.overlayManager.add(polyline)
            view.zoomToBoundingBox(polyline.bounds, false, 100)
            view.invalidate()
        }
    }
}

@Composable
private fun TrackData(
    state: TrackDetailsState.Init,
    reduce: (TrackDetailsIntent) -> Unit,
    modifier: Modifier
) {
    val track = state.track
    val distance = "${track.distance} m"
    val timeIni = track.timeIni.toDateStr()
    val timeEnd = track.timeEnd.toDateStr()
    val duration = track.timeEnd - track.timeIni
    val altitudes = track.points.map { it.altitude }
    val altitudeMax = altitudes.maxOrNull() ?: 0.0
    val altitudeMin = altitudes.minOrNull() ?: 0.0
    val altitude = String.format(//"$altitudeMin - $altitudeMax m"
        Locale.current.platformLocale,
        "%.0f - %.0f m (dif %.0f)",
        altitudeMin, altitudeMax, altitudeMax - altitudeMin
    )
    val speeds = track.points.map { it.speed }
    val speedMax = speeds.maxOrNull() ?: 0f
    val speedMed = speeds.average()
    val speed = String.format(
        Locale.current.platformLocale,
        //"%.0f - %.0f Km/h (%d - %d m/s)",
        //speedMin*3.6, speedMax*3.6, speedMin, speedMax)
        "%.0f Km/h (max %.0f)",
        speedMed*3.6, speedMax*3.6
    )
    android.util.Log.e("TrackingPage", "TrackData---------------- points = ${track.points.size} ")
    LazyColumn(modifier = modifier
        .fillMaxWidth()
        .padding(SepMin)) {
        item {
            var trackName by remember { mutableStateOf(track.name) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = SepMin)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = trackName,
                    onValueChange = { trackName = it },
                    label = { Text(text = stringResource(R.string.name)) },
                    maxLines = 1,
                    modifier = Modifier.weight(.7f),
                )
                IconButton(onClick = {
                    reduce(TrackDetailsIntent.SaveName(trackName))
                }) {
                    Icon(
                        painterResource(android.R.drawable.ic_menu_save),
                        contentDescription = stringResource(R.string.save)
                    )
                }
            }
        }
        item { InfoRow(stringResource(R.string.distance), distance) }
        item { InfoRow(stringResource(R.string.time), duration.toTimeStr()) }
        item { InfoRow(stringResource(R.string.time_ini), timeIni) }
        item { InfoRow(stringResource(R.string.time_end), timeEnd) }
        item { InfoRow(stringResource(R.string.speed), speed) }
        item { InfoRow(stringResource(R.string.altitude), altitude) }
        item { InfoRow(stringResource(R.string.points), track.points.size.toString()) }
        item { Spacer(modifier = Modifier.size(SepMax*5)) }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(top = SepMin)) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.3f)
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.6f)
        )
    }
    HorizontalDivider()
}

//--------------------------------------------------------------------------------------------------
@Composable
@Preview
private fun TrackPage_Preview() {
    val time = System.currentTimeMillis()
    val state = TrackDetailsState.Init(
        track = TrackDto(
            id = 69,
            name = "Track A",
            timeIni = time - 3*60*60*1000,
            timeEnd = time - 2*60*60*1000,
            distance = 3500,
            points = listOf(
                TrackPointDto(1069, 40.5, -3.0, time, 0f, "", 10.0, 0.0f, 1f),
                TrackPointDto(1070, 40.51, -3.01, time, 0f, "", 15.0, 0.1f, 2f),
                TrackPointDto(1071, 40.52, -3.02, time, 0f, "", 5.0, 0.3f, 3f),
            )
        ),
        error = AppError.NotFound
    )
    Surface(modifier = Modifier.fillMaxWidth()) {
        Content(state) { }
    }
}