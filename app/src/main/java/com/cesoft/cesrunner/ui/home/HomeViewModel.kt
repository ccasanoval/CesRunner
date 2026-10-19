package com.cesoft.cesrunner.ui.home

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackFlowUC
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackUC
import com.cesoft.cesrunner.domain.usecase.ReadVo2MaxUC
import com.cesoft.cesrunner.domain.usecase.RequestLocationUpdatesUC
import com.cesoft.cesrunner.tracking.TrackingServiceFac
import com.cesoft.cesrunner.ui.home.mvi.HomeIntent
import com.cesoft.cesrunner.ui.home.mvi.HomeSideEffect
import com.cesoft.cesrunner.ui.home.mvi.HomeState
import com.cesoft.cesrunner.ui.home.mvi.HomeTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val trackingServiceFac: TrackingServiceFac,
    private val readCurrentTrack: ReadCurrentTrackUC,
    private val readCurrentTrackFlow: ReadCurrentTrackFlowUC,
    private val requestLocationUpdates: RequestLocationUpdatesUC,
    private val readVo2Max: ReadVo2MaxUC,
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
        }
    fun consumeSideEffect(
        sideEffect: HomeSideEffect,
        context: Context,
    ) {
        when(sideEffect) {
            HomeSideEffect.Close -> { (context as? Activity)?.finish() }
        }
    }

    private fun executeLoad() = flow {
        // Updates only while the screen is visible, the tracking service has its own updates
        val locationFlow: StateFlow<TrackPointDto?> = requestLocationUpdates(LOCATION_INTERVAL, 0f)
            .catch { e -> Log.e(TAG, "executeLoad: location: $e") }// ie: permission revoked
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )
        val vo2Max = readVo2Max()

        // Restart the service if there's a tracking in progress (ie: the app was killed)
        val currentTrack = readCurrentTrack().getOrNull() ?: TrackDto.Empty
        if(currentTrack.isCreated) {
            trackingServiceFac.start()
        }
        val res = readCurrentTrackFlow()
        res.getOrNull()?.let {
            val flow: StateFlow<TrackDto?> = it.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TrackDto.Empty,
            )
            emit(HomeTransform.GoInit(vo2Max, flow, locationFlow, null))
        } ?: run {
            val e: AppError = res.exceptionOrNull()
                ?.let { AppError.DataBaseError(it) } ?: run { AppError.NotFound }
            val flow = MutableStateFlow<TrackDto?>(null)
            emit(HomeTransform.GoInit(vo2Max, flow, locationFlow, e))
        }
    }

    private fun executeClose() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.Close))
    }

    companion object {
        private const val TAG = "HomeVM"
        private const val LOCATION_INTERVAL = 60_000L
    }
}
