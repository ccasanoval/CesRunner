package com.cesoft.cesrunner.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.cesoft.cesrunner.ui.home.mvi.HomeIntent
import com.cesoft.cesrunner.ui.home.mvi.HomeState
import org.koin.androidx.compose.koinViewModel
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.CurrentTrackingDto
import com.cesoft.cesrunner.ui.common.ErrorCompo
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.theme.SepMax
import com.cesoft.cesrunner.ui.theme.SepMed

@Composable
fun HomePage(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel(),
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
            viewModel.execute(HomeIntent.Close)
        },
    ) { view ->
        //android.util.Log.e("HomePage", "--------HomePage----- $view")
        when(view) {
            is HomeState.Loading -> {
                viewModel.execute(HomeIntent.Load)
                LoadingCompo()
            }
            is HomeState.Init -> {
                Content(state = view, reduce = viewModel::execute)
            }
        }
    }
}

@Composable
private fun Content(
    state: HomeState.Init,
    reduce: (intent: HomeIntent) -> Unit,
) {
    Surface {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            state.error?.let {
                ErrorCompo(it)
            }

            val isTracking = state.currentTracking.isTracking
            if (isTracking) {
                Text(
                    text = stringResource(R.string.tracking_on),
                    modifier = Modifier.padding(SepMax)
                )
            }
            else {
                Spacer(modifier = Modifier.padding(SepMax))
            }
            HomeButton(
                title = stringResource(if(isTracking) R.string.menu_check else R.string.menu_start),
                onClick = { reduce(HomeIntent.GoStart) }
            )
            HomeButton(
                title = stringResource(R.string.menu_settings),
                onClick = { reduce(HomeIntent.GoSettings) }
            )
            HomeButton(
                title = stringResource(R.string.menu_tracks),
                onClick = { reduce(HomeIntent.GoTracks) }
            )
            HomeButton(
                title = stringResource(R.string.menu_maps),
                onClick = { reduce(HomeIntent.GoMap) }
            )
            HomeButton(
                title = stringResource(R.string.menu_gnss),
                onClick = { reduce(HomeIntent.GoMap) }
            )
        }
    }
}

@Composable
private fun HomeButton(
    title: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(SepMed)
    ) {
        Text(title)
    }
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun HomePage_Preview() {
    val state = HomeState.Init(
        currentTracking = CurrentTrackingDto(isTracking = true),
        error = AppError.NetworkError,
    )
    Content(state) { }
}