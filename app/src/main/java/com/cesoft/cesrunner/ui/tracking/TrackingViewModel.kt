package com.cesoft.cesrunner.ui.tracking

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.domain.entity.CurrentTrackingDto
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackingUC
import com.cesoft.cesrunner.domain.usecase.RequestLocationUpdatesUC
import com.cesoft.cesrunner.domain.usecase.SaveCurrentTrackingUC
import com.cesoft.cesrunner.domain.usecase.StopLocationUpdatesUC
import com.cesoft.cesrunner.tracking.TrackingServiceFac
import com.cesoft.cesrunner.tracking.TrackingWork
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingIntent
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingSideEffect
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingState
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

@SuppressLint("StaticFieldLeak")
class TrackingViewModel(
    private val trackingServiceFac: TrackingServiceFac,
    private val readCurrentTracking: ReadCurrentTrackingUC,
    private val saveCurrentTracking: SaveCurrentTrackingUC,
    private val requestLocationUpdates: RequestLocationUpdatesUC,
    private val stopLocationUpdates: StopLocationUpdatesUC,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<TrackingIntent, State<TrackingState, TrackingSideEffect>> {

    private val reducer = Reducer(
        coroutineScope = viewModelScope,
        defaultDispatcher = coroutineDispatcher,
        initialInnerState = TrackingState.Loading,
        logger = null,
        intentExecutor = this::executeIntent,
    )
    override val state = reducer.state
    override fun execute(intent: TrackingIntent) {
        reducer.executeIntent(intent)
    }
    private fun executeIntent(intent: TrackingIntent) =
        when (intent) {
            is TrackingIntent.Load -> executeLoad()
            //is TrackingIntent.Init -> executeInit()

            is TrackingIntent.Close -> executeClose()

            is TrackingIntent.Stop -> executeStop()

            else -> executeClose()
        }

    private fun executeLoad() = flow {
        val tracking = CurrentTrackingDto(isTracking = true)
        //TrackingWork.create(context)
        trackingServiceFac.start()
        saveCurrentTracking(tracking)
        emit(TrackingTransform.GoInit(tracking, null))
        //emit(TrackingTransform.AddSideEffect(TrackingSideEffect.StartTracking))
    }

    private fun executeClose() = flow {
        emit(TrackingTransform.AddSideEffect(TrackingSideEffect.Close))
    }
    private fun executeStop() = flow {
        val tracking = CurrentTrackingDto(isTracking = false)
        saveCurrentTracking(tracking)
        trackingServiceFac.stop()
        emit(TrackingTransform.AddSideEffect(TrackingSideEffect.Close))
        //emit(TrackingTransform.GoInit(tracking, null))
    }

    fun consumeSideEffect(
        sideEffect: TrackingSideEffect,
        navController: NavController,
        context: Context,
    ) {
        when(sideEffect) {
            TrackingSideEffect.Close -> {
                navController.popBackStack()
            }
//            TrackingSideEffect.StartTracking -> {
//                execute(TrackingIntent.Init)
//            }
//            TrackingSideEffect.StopTracking -> {
//                val tracking = CurrentTrackingDto(isTracking = true)
//                saveCurrentTracking(tracking)
//                trackingServiceFac.stop()
//            }
        }
    }
}