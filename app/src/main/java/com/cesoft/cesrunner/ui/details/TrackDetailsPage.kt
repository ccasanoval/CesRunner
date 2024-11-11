package com.cesoft.cesrunner.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.toDateStr
import com.cesoft.cesrunner.toDistanceStr
import com.cesoft.cesrunner.toTimeStr
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.ToolbarCompo
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsIntent
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsState
import com.cesoft.cesrunner.ui.theme.SepMin
import com.cesoft.cesrunner.ui.theme.fontMed
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
        Content(state = state, reduce = viewModel::execute)
    }
}

@Composable
fun Content(
    state: TrackDetailsState,
    reduce: (TrackDetailsIntent) -> Unit,//KFunction1<TracksIntent, Unit>
) {
    ToolbarCompo(
        title = stringResource(R.string.menu_tracks),
        onBack = { reduce(TrackDetailsIntent.Close) }
    ) {
        when (state) {
            is TrackDetailsState.Loading -> {
                reduce(TrackDetailsIntent.Load)
                LoadingCompo()
            }
            is TrackDetailsState.Init -> {
                TracksData(state = state, reduce = reduce)
            }
        }
    }
}

@Composable
fun TracksData(
    state: TrackDetailsState.Init,
    reduce: (TrackDetailsIntent) -> Unit,
) {
    Column(modifier = Modifier) {
        //TODO: Search bar
//        Text(
//            text = stringResource(R.string.menu_tracks),
//            fontWeight = FontWeight.Black,
//            fontSize = fontBig,
//            modifier = Modifier.padding(SepMin)
//        )
        LazyColumn {
            for(t in state.tracks) {
                item {
                }
            }
        }
    }
}

@Composable
private fun Item(data: TrackDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(SepMin)
            .clickable { onClick() }
    ) {
        HorizontalDivider()
        Text(
            text = data.name,
            fontSize = fontMed,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = data.timeIni.toDateStr(),
            color = MaterialTheme.colorScheme.secondary
        )
        Row {
            Text(
                text = stringResource(R.string.distance) + ": " + data.distance.toDistanceStr(),
                modifier = Modifier.weight(0.4f)
            )
            Text(
                text = stringResource(R.string.time) + ": " + data.time.toTimeStr(),
                modifier = Modifier.weight(0.4f)
            )
        }
    }
}

//--------------------------------------------------------------------------------------------------
@Composable
@Preview
private fun TrackPage_Preview() {
    val time = System.currentTimeMillis()
    val state = TrackDetailsState.Init(
        tracks = listOf(
            TrackDto(
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
            TrackDto(
                id = 70,
                name = "Track B",
                timeIni = (time - 1.5*60*60*1000).toLong(),
                timeEnd = (time - 0.5*60*60*1000).toLong(),
                distance = 5600,
                points = listOf(
                    TrackPointDto(1080, 40.5, -3.0, time, 0f, "", 10.0, 0.0f, 1f),
                    TrackPointDto(1081, 40.51, -3.01, time, 0f, "", 15.0, 0.1f, 2f),
                    TrackPointDto(1082, 40.52, -3.02, time, 0f, "", 5.0, 0.3f, 3f),
                )
            ),
        ),
        error = AppError.NotFound
    )
    Surface(modifier = Modifier.fillMaxWidth()) {
        Content(state) { }
    }
}