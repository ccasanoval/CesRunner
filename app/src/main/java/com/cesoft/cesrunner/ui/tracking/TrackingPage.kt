package com.cesoft.cesrunner.ui.tracking

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.toDateStr
import com.cesoft.cesrunner.toTimeStr
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.tracking.TrackingService
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.TurnLocationOnDialog
import com.cesoft.cesrunner.ui.theme.Green
import com.cesoft.cesrunner.ui.theme.SepMax
import com.cesoft.cesrunner.ui.theme.SepMin
import com.cesoft.cesrunner.ui.theme.fonBig
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingIntent
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingState
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

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
    android.util.Log.e("TrackingPage", "TrackingInfo---------------------------")
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

        TrackData(state, reduce)

        Spacer(modifier = Modifier.padding(SepMax*2))
        HorizontalDivider(thickness = 3.dp, color = Green)

        //TODO: Map
//        OpenStreetMapView(
//            points = state.currentTracking.points,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(300.dp)
//                .border(2.dp, Green)
//                .padding(SepMax*2)
//        )
        MapCompo(state)

        Spacer(modifier = Modifier.padding(SepMax))

    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun MapCompo(state: TrackingState.Init) {
    var geoPoint by mutableStateOf(GeoPoint(0.0,0.0))
    LaunchedEffect(state) {
        if(state.currentTracking.points.isNotEmpty()) {
            val lat = state.currentTracking.points.last().latitude
            val lon = state.currentTracking.points.last().longitude
            geoPoint = GeoPoint(lat, lon)
            android.util.Log.e("TrackingPAge", "------------------ $lat / $lon ")
        }
    }
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(300.dp).border(2.dp, Color.Red),
        factory = { context ->
            // Creates the view
            MapView(context).apply {
                // Do anything that needs to happen on the view init here
                // For example set the tile source or add a click listener
                setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)//USGS_TOPO//USGS_SAT // OPEN_SEAMAP // HIKEBIKEMAP//TODO: Settings
                setOnClickListener {
                    TODO("Handle click here")
                }
            }
        },
        update = { view ->
            // Code to update or recompose the view goes here
            // Since geoPoint is read here, the view will recompose whenever it is updated
            view.controller.setCenter(geoPoint)
            //view.controller.zoomTo(12)
        }
    )

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
                delay(TrackingService.MIN_PERIOD/4)//TODO: DATABASE DATA FLOW !!!
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
        val altitude = "${state.currentTracking.altitudeMin} - ${state.currentTracking.altitudeMax} m"
        val speedMin = state.currentTracking.speedMin
        val speedMax = state.currentTracking.speedMax
        val speed = String.format(
            Locale.current.platformLocale,
            "%.0f - %.0f Km/h (%d - %d m/s)",
            speedMin*3.6, speedMax*3.6, speedMin, speedMax)
        //val durationStr = if(duration > 60*60) "${duration}"
        InfoRow(stringResource(R.string.distance), distance)
        InfoRow(stringResource(R.string.time), duration.toTimeStr())
        InfoRow(stringResource(R.string.time_ini), timeIni)
        InfoRow(stringResource(R.string.time_end), timeEnd)
        InfoRow(stringResource(R.string.speed), speed)
        InfoRow(stringResource(R.string.altitude), altitude)
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
@Preview
@Composable
private fun TrackData_Preview() {
    val timeIni = System.currentTimeMillis() - 5*60*60*1000 - 35*60*1000 - 45*1000
    val state = TrackingState.Init(
        currentTracking = TrackDto.Empty.copy(
            name = "Tracking A",
            timeIni = timeIni,
            timeEnd = System.currentTimeMillis(),
            distance = 690,
            altitudeMax = 1200,
            altitudeMin = 600,
            speedMax = 5,
            speedMin = 1,
        )
    )
    Surface(modifier = Modifier.fillMaxSize()) {
        TrackData(state) { }
    }
}

//@Preview
//@Composable
//private fun TrackingPage_Preview() {
//    val timeIni = System.currentTimeMillis() - 5*60*60*1000 - 35*60*1000 - 45*1000
//    val state = TrackingState.Init(
//        currentTracking = TrackDto.Empty.copy(
//            name = "Tracking A",
//            timeIni = timeIni,
//            timeEnd = System.currentTimeMillis(),
//            distance = 690,
//            altitudeMax = 1200,
//            altitudeMin = 600,
//            speedMax = 15,
//            speedMin = 5,
//        )
//    )
//    Surface {
//        ScreenCompo(state) { }
//    }
//}