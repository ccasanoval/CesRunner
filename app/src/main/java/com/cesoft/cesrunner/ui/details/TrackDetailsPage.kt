package com.cesoft.cesrunner.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.ToolbarCompo
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsIntent
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsState
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrackDetailsPage(
    navController: NavController,
    viewModel: TrackDetailsViewModel = koinViewModel(),
) {
    android.util.Log.e("AAAA", "------------------ ")
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