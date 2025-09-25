package com.cesoft.cesrunner.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.toDateStr
import com.cesoft.cesrunner.toDistanceStr
import com.cesoft.cesrunner.toStr
import com.cesoft.cesrunner.toTimeStr
import com.cesoft.cesrunner.ui.common.AskDelete
import com.cesoft.cesrunner.ui.common.InfoRow
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.MapCompo
import com.cesoft.cesrunner.ui.common.ToolbarCompo
import com.cesoft.cesrunner.ui.common.rememberMapCompo
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsIntent
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsState
import com.cesoft.cesrunner.ui.theme.SepMax
import com.cesoft.cesrunner.ui.theme.SepMin
import org.koin.androidx.compose.koinViewModel

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
        when (state) {
            is TrackDetailsState.Loading -> {
                viewModel.execute(TrackDetailsIntent.Load)
                LoadingCompo()
            }
            is TrackDetailsState.Init -> {
                Content(state, viewModel::execute)
            }
        }
    }
}

@Composable
fun Content(
    state: TrackDetailsState.Init,
    reduce: (TrackDetailsIntent) -> Unit,
) {
    val askDelete = remember { mutableStateOf(false) }
    AskDelete(show = askDelete, onDelete = { reduce(TrackDetailsIntent.Delete) })
    ToolbarCompo(
        title = stringResource(R.string.menu_track_details),
        onBack = { reduce(TrackDetailsIntent.Close) },
        error = state.error,
        message = state.message?.toStr(LocalContext.current),
        actions = { ActionsMenu(reduce, askDelete) }
    ) {
        TrackDetailsCompo(state, reduce)
    }
}

@Composable
private fun ActionsMenu(
    reduce: (TrackDetailsIntent) -> Unit,
    askDelete: MutableState<Boolean>
) {
    var menuExpanded by remember { mutableStateOf(false) }
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
            leadingIcon = { Icon(painterResource(R.drawable.download), null) },
            text = { Text(stringResource(R.string.export_gpx)) },
            onClick = {
                menuExpanded = false
                reduce(TrackDetailsIntent.Export)
            },
        )
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.Delete, null) },
            text = { Text(stringResource(R.string.delete)) },
            onClick = {
                menuExpanded = false
                askDelete.value = true
            },
        )
    }
}

@Composable
private fun TrackDetailsCompo(
    state: TrackDetailsState.Init,
    reduce: (TrackDetailsIntent) -> Unit
) {
    val context = LocalContext.current
    val mapView = rememberMapCompo(context)
    Column {
        TrackData(
            state = state,
            reduce = reduce,
            modifier = Modifier.weight(.3f)
        )
        MapCompo(
            context = context,
            mapView = mapView,
            trackPoints = state.track.points,
            location = state.location,
            modifier = Modifier.weight(.4f)
        )
    }
}

@Composable
private fun TrackData(
    state: TrackDetailsState.Init,
    reduce: (TrackDetailsIntent) -> Unit,
    modifier: Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val track = state.track
    val distance = track.distance.toDistanceStr()
    val timeIni = track.timeIni.toDateStr()
    val timeEnd = track.timeEnd.toDateStr()
    val duration = (track.timeEnd - track.timeIni).toTimeStr()
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
    //val timeMinutes = (track.timeEnd - track.timeIni)/60_000
    //val vo2max = (track.distance / timeMinutes) * 0.2 + 3.5
    val vo2max = track.calcVo2Max()
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
                    keyboardController?.hide()
                    reduce(TrackDetailsIntent.SaveName(trackName))
                }) {
                    Icon(
                        painterResource(android.R.drawable.ic_menu_save),
                        contentDescription = stringResource(R.string.save)
                    )
                }
            }
        }
        item { InfoRow("VO2 Max", stringResource(R.string.vo2max).format(vo2max)) }
        item { InfoRow(stringResource(R.string.distance), distance) }
        item { InfoRow(stringResource(R.string.time), duration) }
        item { InfoRow(stringResource(R.string.time_ini), timeIni) }
        item { InfoRow(stringResource(R.string.time_end), timeEnd) }
        item { InfoRow(stringResource(R.string.speed), speed) }
        item { InfoRow(stringResource(R.string.altitude), altitude) }
        item { InfoRow(stringResource(R.string.points), track.points.size.toString()) }
        item { Spacer(modifier = Modifier.padding(vertical = SepMax*5)) }
    }
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