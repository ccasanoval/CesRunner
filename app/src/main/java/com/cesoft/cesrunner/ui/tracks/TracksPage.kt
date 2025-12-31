package com.cesoft.cesrunner.ui.tracks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.toDateStr
import com.cesoft.cesrunner.toDistanceStr
import com.cesoft.cesrunner.toTimeStr
import com.cesoft.cesrunner.ui.common.AskDelete
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.ToolbarCompo
import com.cesoft.cesrunner.ui.theme.SepMed
import com.cesoft.cesrunner.ui.theme.SepMin
import com.cesoft.cesrunner.ui.theme.fontMed
import com.cesoft.cesrunner.ui.tracks.mvi.TracksIntent
import com.cesoft.cesrunner.ui.tracks.mvi.TracksState
import org.koin.androidx.compose.koinViewModel

@Composable
fun TracksPage(
    onDetails: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: TracksViewModel = koinViewModel()
) {
    LaunchedEffect(onDetails) {
        viewModel.refresh() // In case user deleted the track in details
    }
    MviScreen(
        state = viewModel.state,
        onSideEffect = { sideEffect ->
//            viewModel.consumeSideEffect(sideEffect = sideEffect)
        },
        onBackPressed = onBack,
    ) { state ->
        Content(
            state = state,
            reduce = viewModel::execute,
            onDetails = onDetails,
            onBack = onBack
        )
    }
}

@Composable
fun Content(
    state: TracksState,
    reduce: (TracksIntent) -> Unit = {},
    onDetails: (Long) -> Unit = {},
    onBack: () -> Unit = {}
) {
    ToolbarCompo(
        title = stringResource(R.string.menu_tracks),
        onBack = onBack
    ) {
        when (state) {
            is TracksState.Loading -> {
                reduce(TracksIntent.Load)
                LoadingCompo()
            }
            is TracksState.Init -> {
                TracksData(state = state, reduce = reduce, onDetails = onDetails)
            }
        }
    }
}

@Composable
fun TracksData(
    state: TracksState.Init,
    reduce: (TracksIntent) -> Unit,
    onDetails: (Long) -> Unit = {}
) {
    Column(modifier = Modifier) {
        //TODO: Search bar: by date and name?
        LazyColumn(modifier = Modifier.padding(SepMin)) {
            for(track in state.tracks) {
                item {
                    Item(
                        data =  track,
                        onClick = {
                            //reduce(TracksIntent.Details(track.id))
                            onDetails(track.id)
                                  },
                        onDelete = { reduce(TracksIntent.Delete(track.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Item(
    data: TrackDto,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val askDelete = remember { mutableStateOf(false) }
    AskDelete(show = askDelete, onDelete = onDelete)
    Column(
        modifier = Modifier
            .padding(SepMin)
            .clickable { onClick() }
    ) {
        HorizontalDivider()
        Row(modifier = Modifier.padding(SepMin)) {
            Text(
                text = data.name,
                fontSize = fontMed,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(.9f)
            )
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { askDelete.value = true }
            )
        }
        Text(
            text = data.timeIni.toDateStr(),
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = SepMed)
        )
        Row(modifier = Modifier.padding(start = SepMed, top = SepMin)) {
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
    val state = TracksState.Init(
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
        Content(state)
    }
}