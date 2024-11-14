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
import com.cesoft.cesrunner.domain.Common.ID_NULL
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.usecase.CreateTrackUC
import com.cesoft.cesrunner.domain.usecase.DeleteCurrentTrackUC
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackIdUC
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackUC
import com.cesoft.cesrunner.domain.usecase.ReadSettingsUC
import com.cesoft.cesrunner.domain.usecase.ReadTrackFlowUC
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
    private val readCurrentTrackId: ReadCurrentTrackIdUC,
    private val readCurrentTrack: ReadCurrentTrackUC,
    private val readTrackFlow: ReadTrackFlowUC,
    private val saveCurrentTracking: SaveCurrentTrackingUC,
    private val deleteCurrentTracking: DeleteCurrentTrackUC,
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

            TrackingIntent.Close -> executeClose()
            TrackingIntent.Stop -> executeStop()

            else -> executeClose()
        }

    private fun executeLoad() = flow {
        var id = readCurrentTrackId().getOrNull() ?: ID_NULL
        if(id == ID_NULL) {
            val settings = readSettings().getOrNull() ?: SettingsDto.Empty
            val time = System.currentTimeMillis()
            val track = TrackDto(
                minInterval = settings.minInterval,
                minDistance = settings.minDistance,
                timeIni = time,
                timeEnd = time,
                name = time.toDateStr(),
            )
            val resTrack = createTrack(track)
            id = resTrack.getOrNull() ?: ID_NULL
            if (resTrack.isSuccess && id != ID_NULL) {
                saveCurrentTracking(id)
            }
        }
        val track = readCurrentTrack().getOrNull() ?: TrackDto.Empty
        trackingServiceFac.start(track.minInterval, track.minDistance)
        val res = readTrackFlow(id)
        res.getOrNull()?.let {
            emit(TrackingTransform.GoInit(it))
        } ?: run {
            val e: AppError = res.exceptionOrNull()
                ?.let { AppError.DataBaseError(it) } ?: run { AppError.NotFound }
            emit(TrackingTransform.GoInit(flow { }, e))
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
        }
    }

    companion object {
        private const val TAG = "TrackingVM"
    }
}