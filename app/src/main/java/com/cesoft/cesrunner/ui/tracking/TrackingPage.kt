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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.toDateStr
import com.cesoft.cesrunner.toTimeStr
import com.cesoft.cesrunner.ui.common.InfoRow
import com.cesoft.cesrunner.ui.common.InfoRowBig
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.MapCompo
import com.cesoft.cesrunner.ui.common.SlideToUnlock
import com.cesoft.cesrunner.ui.common.TurnLocationOnDialog
import com.cesoft.cesrunner.ui.common.rememberMapCompo
import com.cesoft.cesrunner.ui.theme.SepMax
import com.cesoft.cesrunner.ui.theme.SepMed
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingIntent
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.compose.koinViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

//TODO: Remember the zoom the user set for all the session...
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
    when(state) {
        is TrackingState.Loading -> {
            reduce(TrackingIntent.Load)
            LoadingCompo()
        }
        is TrackingState.Init -> {
            TrackingStateIni(state, reduce)
        }
    }
}

@Composable
private fun TrackingStateIni(
    state: TrackingState.Init,
    reduce: (intent: TrackingIntent) -> Unit,
) {
    val context = LocalContext.current
    val showAlert = remember { mutableStateOf(true) }
    TurnLocationOnDialog(showAlert) {
        showAlert.value = false
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
    TrackingCompo(state, reduce)
}

@Composable
private fun TrackingCompo(
    state: TrackingState.Init,
    reduce: (intent: TrackingIntent) -> Unit,
) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(SepMax)
    ) {
//        Button(
//            onClick = { reduce(TrackingIntent.Stop) },
//            modifier = Modifier.fillMaxWidth(.5f),
//        ) { Text(stringResource(R.string.stop)) }
        var unlocked by remember { mutableStateOf(false) }
        SlideToUnlock(
            isUnlocked = unlocked,
            modifier = Modifier.padding(SepMed),
            hintText = stringResource(R.string.slide_stop),
            onUnlock = {
                if(!unlocked) reduce(TrackingIntent.Stop)
                unlocked = true
            }
        )

        val trackState by state.trackFlow.collectAsStateWithLifecycle()
        val track = trackState ?: TrackDto.Empty
        val points = trackState?.points ?: listOf()
        val mapView = rememberMapCompo(context)

        TrackData(track, Modifier.weight(.4f))
        Spacer(Modifier.size(SepMax))
        MapCompo(context, mapView, points, Modifier.weight(.55f), doZoom = false)
    }
}

@Composable
private fun TrackData(
    track: TrackDto,
    modifier: Modifier = Modifier
) {
    val dfs = DecimalFormatSymbols(Locale.current.platformLocale)
    val df = DecimalFormat("#,###", dfs).format(track.distance)
    val distance = "$df m"

//    val timeIni = track.timeIni.toDateStr()
//    val timeEnd = track.timeEnd.toDateStr()//Sometimes points get refresh before track data!
    val timeIni = track.points.firstOrNull()?.time
    val timeEnd = track.points.lastOrNull()?.time
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
//    android.util.Log.e("TrackingPage", "DATA------points= ${track.points.size} ")
//    android.util.Log.e("TrackingPage", "DATA------first= ${track.points.firstOrNull()?.time?.toDateStr()} / $timeIni")
//    android.util.Log.e("TrackingPage", "DATA------last= ${track.points.lastOrNull()?.time?.toDateStr()} / $timeEnd")
//    android.util.Log.e("TrackingPage", "DATA------dis= ${TrackDto.calcDistance(track.points)} / $distance")
    LazyColumn(modifier = modifier.fillMaxWidth()) {
//        item {
//            Text(
//                text = track.name,
//                fontWeight = FontWeight.Bold,
//                fontSize = fontBig,
//                modifier = Modifier.padding(vertical = SepMin)
//            )
//        }
        item { InfoRowBig(stringResource(R.string.distance), distance) }
        item { InfoRowBig(stringResource(R.string.time), duration.toTimeStr()) }
        item { InfoRow(stringResource(R.string.time_ini), timeIni?.toDateStr() ?: "") }
        item { InfoRow(stringResource(R.string.time_end), timeEnd?.toDateStr() ?: "") }
        item { InfoRow(stringResource(R.string.speed), speed) }
        item { InfoRow(stringResource(R.string.altitude), altitude) }
        item { InfoRow(stringResource(R.string.points), track.points.size.toString()) }
        item { Spacer(Modifier.padding(vertical = SepMax*5)) }
    }
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun TrackingCompo_Preview() {
    val state = TrackingState.Init(MutableStateFlow(null), null)
    Surface {
        TrackingCompo(state) { }
    }
}

@Preview
@Composable
private fun TrackData_Preview() {
    val timeIni = System.currentTimeMillis() - 5*60*60*1000 - 35*60*1000 - 45*1000
    val track = TrackDto.Empty.copy(
        name = "Tracking A",
        timeIni = timeIni,
        timeEnd = System.currentTimeMillis(),
        distance = 6950,
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
    Surface(modifier = Modifier.fillMaxSize()) {
        TrackData(track)
    }
}
