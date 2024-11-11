package com.cesoft.cesrunner.ui.tracks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.toDateStr
import com.cesoft.cesrunner.toDistanceStr
import com.cesoft.cesrunner.toTimeStr
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.theme.fontMed
import com.cesoft.cesrunner.ui.tracks.mvi.TracksIntent
import com.cesoft.cesrunner.ui.tracks.mvi.TracksState
import org.koin.androidx.compose.koinViewModel

@Composable
fun TracksPage(
    navController: NavController,
    viewModel: TracksViewModel = koinViewModel(),
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
            viewModel.execute(TracksIntent.Close)
        },
    ) { view ->
        //android.util.Log.e("HomePage", "--------HomePage----- $view")
        when(view) {
            is TracksState.Loading -> {
                viewModel.execute(TracksIntent.Load)
                LoadingCompo()
            }
            is TracksState.Init -> {
                Content(state = view, reduce = viewModel::execute)
            }
        }
    }
}

@Composable
fun Content(
    state: TracksState.Init,
    reduce: (TracksIntent) -> Unit,
) {
    Column(modifier = Modifier) {
       //TODO: Search bar
        LazyColumn {
            for(t in state.tracks) {
                item { Item(t) }
            }
        }
    }
}

@Composable
private fun Item(data: TrackDto) {
    Column {
        Row {
            Text(text = data.name, fontSize = fontMed)
        }
        Row {
            Text(text = data.timeIni.toDateStr())
        }
        Row {
            Text(text = data.distance.toDistanceStr())
            Text(text = data.time.toTimeStr())
        }
    }
}