package com.cesoft.cesrunner.ui.home

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.Page
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackFlowUC
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackUC
import com.cesoft.cesrunner.tracking.TrackingServiceFac
import com.cesoft.cesrunner.ui.home.mvi.HomeIntent
import com.cesoft.cesrunner.ui.home.mvi.HomeSideEffect
import com.cesoft.cesrunner.ui.home.mvi.HomeState
import com.cesoft.cesrunner.ui.home.mvi.HomeTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val trackingServiceFac: TrackingServiceFac,
    private val readCurrentTrack: ReadCurrentTrackUC,
    private val readCurrentTrackFlow: ReadCurrentTrackFlowUC,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<HomeIntent, State<HomeState, HomeSideEffect>> {

    private val reducer = Reducer(
        coroutineScope = viewModelScope,
        defaultDispatcher = coroutineDispatcher,
        initialInnerState = HomeState.Loading,
        logger = null,
        intentExecutor = this::executeIntent
    )
    override val state = reducer.state
    override fun execute(intent: HomeIntent) {
        reducer.executeIntent(intent)
    }
    private fun executeIntent(intent: HomeIntent) =
        when(intent) {
            HomeIntent.Close -> executeClose()
            HomeIntent.Load -> executeLoad()
            HomeIntent.GoStart -> executeStart()
            HomeIntent.GoSettings -> executeSettings()
            HomeIntent.GoMap -> executeMap()
            HomeIntent.GoTracks -> executeTracks()
        }

    //TODO: ask if wanna close tracking...?
    private fun executeClose() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.Close))
    }

    private fun executeLoad() = flow {
        val currentTrack = readCurrentTrack().getOrNull() ?: TrackDto.Empty
        if(currentTrack.isCreated) {
            trackingServiceFac.start(currentTrack.minInterval, currentTrack.minDistance)
        }

        //https://stackoverflow.com/questions/78277363/collecting-flows-in-the-viewmodel
        val flow: StateFlow<TrackDto?>? = readCurrentTrackFlow().getOrNull()?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TrackDto.Empty,
        )
//        val trackIdFlow = readCurrentTrackIdFlow().getOrNull()?.stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5_000),
//            initialValue = ID_NULL,
//        )
        emit(HomeTransform.GoInit(flow, null))
        /*
        private fun transformedFlow(movieId: Int) = combine(
            myRepo.getMovie(movieId).filterNotNull(),
            myRepo.getActors(movieId),
        ) { movie, actors ->
            MyUiState(
                movie = movie,
                actorList = actors,
            )
        }*/

        /*readCurrentTrackIdFlow().getOrNull()?.collect {
            val res = readTrackFlow(currentTrack.id)
            var error: AppError? = null
            val e = res.exceptionOrNull()
            if(res.isFailure && e !is AppError.NotFound) {
                res.exceptionOrNull()?.let { error = AppError.fromThrowable(it) }
            }
            val trackFlow = res.getOrNull() ?: flow {  }
            emit(HomeTransform.GoInit(trackFlow, error))
        }*/
    }

    private fun executeStart() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.Start))
        emit(HomeTransform.GoLoading)
    }

    private fun executeSettings() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.GoSettings))
    }

    private fun executeMap() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.GoMap))
    }

    private fun executeTracks() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.GoTracks))
    }

    fun consumeSideEffect(
        sideEffect: HomeSideEffect,
        navController: NavController,
        context: Context,
    ) {
        when(sideEffect) {
            HomeSideEffect.Start -> {
                navController.navigate(Page.Tracking.route)
            }
            HomeSideEffect.GoSettings -> {
                navController.navigate(Page.Settings.route)
                //Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
            HomeSideEffect.GoTracks -> {
                navController.navigate(Page.Tracks.route)
            }
            HomeSideEffect.GoMap -> {
                navController.navigate(Page.Map.route)
            }
            HomeSideEffect.Close -> (context as Activity).finish()
        }
    }

    companion object {
        private const val TAG = "HomeVM"
    }
}