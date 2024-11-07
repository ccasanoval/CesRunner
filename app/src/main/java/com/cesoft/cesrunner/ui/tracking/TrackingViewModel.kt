package com.cesoft.cesrunner.ui.tracking

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.toDateStr
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.usecase.CreateTrackUC
import com.cesoft.cesrunner.domain.usecase.DeleteCurrentTrackUC
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackUC
import com.cesoft.cesrunner.domain.usecase.ReadSettingsUC
import com.cesoft.cesrunner.domain.usecase.SaveCurrentTrackingUC
import com.cesoft.cesrunner.tracking.TrackingServiceFac
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingIntent
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingSideEffect
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingState
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

@SuppressLint("StaticFieldLeak")
class TrackingViewModel(
    private val createTrack: CreateTrackUC,
    private val trackingServiceFac: TrackingServiceFac,
    private val readCurrentTracking: ReadCurrentTrackUC,
    private val saveCurrentTracking: SaveCurrentTrackingUC,
    private val deleteCurrentTracking: DeleteCurrentTrackUC,
    //private val requestLocationUpdates: RequestLocationUpdatesUC,
    ///private val stopLocationUpdates: StopLocationUpdatesUC,
    private val readSettings: ReadSettingsUC,
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
        when(intent) {
            TrackingIntent.Load -> executeLoad()
            TrackingIntent.Refresh -> executeRefresh()

            TrackingIntent.Close -> executeClose()
            TrackingIntent.Stop -> executeStop()

            else -> executeClose()
        }

    private fun executeRefresh() = flow {
        val res = readCurrentTracking()
        res.getOrNull()?.let {
            emit(TrackingTransform.GoInit(it, null))
        } ?: run {
            val error = res.exceptionOrNull()
                ?.let { AppError.fromThrowable(it) } ?: run { AppError.NotFound }
            emit(TrackingTransform.GoInit(TrackDto.Empty, error))
        }
    }
    private fun executeLoad() = flow {
        val res = readCurrentTracking()
        val track: TrackDto? = if(res.isSuccess) {
            res.getOrNull()
        }
        else {
            val settings = readSettings().getOrNull() ?: SettingsDto.Empty
            val time = System.currentTimeMillis()
            val track = TrackDto(
                minInterval = settings.period,
                minDistance = 0.5f,//TODO: Add to settings
                timeIni = time,
                timeEnd = time,
                name = "TRACK: "+time.toDateStr(),
            )
            val resTrack = createTrack(track)
            val id = resTrack.getOrNull()
            if(resTrack.isSuccess && id != null) {
                saveCurrentTracking(id)
                track.copy(id = id)
            }
            else {
                null
            }
        }
        if(track == null) {
            emit(TrackingTransform.GoInit(TrackDto.Empty, AppError.NotFound))
        }
        else {
            trackingServiceFac.start(track.minInterval, track.minDistance)
            emit(TrackingTransform.GoInit(track, null))
        }
    }

    private fun executeClose() = flow {
        emit(TrackingTransform.AddSideEffect(TrackingSideEffect.Close))
    }
    private fun executeStop() = flow {
        deleteCurrentTracking()
        trackingServiceFac.stop()
        emit(TrackingTransform.AddSideEffect(TrackingSideEffect.Close))
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

    companion object {
        private const val TAG = "TrackingVM"
    }
}