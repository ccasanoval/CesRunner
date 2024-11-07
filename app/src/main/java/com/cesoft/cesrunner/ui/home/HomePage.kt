package com.cesoft.cesrunner.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.traceEventEnd
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.getSystemService
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.ui.common.ErrorCompo
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.TurnLocationOnDialog
import com.cesoft.cesrunner.ui.home.mvi.HomeIntent
import com.cesoft.cesrunner.ui.home.mvi.HomeState
import com.cesoft.cesrunner.ui.theme.SepMed
import com.cesoft.cesrunner.ui.theme.SepMin
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel


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

@SuppressLint("DefaultLocale")
@Composable
private fun Content(
    state: HomeState.Init,
    reduce: (intent: HomeIntent) -> Unit,
) {
    LaunchedEffect(state) {
        while(true) {
            android.util.Log.e("HomePage", "Content----------------")
            reduce(HomeIntent.Refresh)
            delay(30_000)
        }
    }
    Surface {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            state.error?.let {
                ErrorCompo(it)
            }

            val isTracking = state.currentTrack.isCreated
            if (isTracking) {
                val context = LocalContext.current
                val showAlert = remember { mutableStateOf(true) }
                TurnLocationOnDialog(showAlert) {
                    showAlert.value = false
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                }
                Text(
                    text = stringResource(R.string.tracking_on),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(SepMin)
                )
                Text(
                    text = state.currentTrack.name,
                    modifier = Modifier.padding(SepMin)
                )
                val distance = state.currentTrack.distance
                val distanceStr = if(distance < 1000) "$distance m"
                else String.format("%.3f Km", distance / 1000f)
                //val speed = state.currentTrack.speedMax//TODO: Current Speed
                Text(
                    text = distanceStr,
                    modifier = Modifier.padding(SepMin)
                )
            }
            else {
                Spacer(modifier = Modifier.padding(SepMin))
                Spacer(modifier = Modifier.padding(SepMin))
            }

            HomeButton(
                title = stringResource(if(isTracking) R.string.menu_check else R.string.menu_start),
                onClick = {
                    reduce(HomeIntent.GoStart)
                }
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
        modifier = Modifier
            .padding(SepMed)
            .fillMaxWidth(.6f)
    ) {
        Text(title)
    }
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun HomePage_Preview() {
    val state = HomeState.Init(
        currentTrack = TrackDto(id = 69, name = "Tracking A"),
        error = AppError.NetworkError,
    )
    Content(state) { }
}