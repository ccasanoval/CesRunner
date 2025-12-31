package com.cesoft.cesrunner.ui.tracks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.usecase.DeleteTrackUC
import com.cesoft.cesrunner.domain.usecase.ReadAllTracksUC
import com.cesoft.cesrunner.ui.tracks.mvi.TracksIntent
import com.cesoft.cesrunner.ui.tracks.mvi.TracksSideEffect
import com.cesoft.cesrunner.ui.tracks.mvi.TracksState
import com.cesoft.cesrunner.ui.tracks.mvi.TracksTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class TracksViewModel(
    private val readAllTracks: ReadAllTracksUC,
    private val deleteTrack: DeleteTrackUC,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<TracksIntent, State<TracksState, TracksSideEffect>> {

    private val reducer =
        Reducer(
            coroutineScope = viewModelScope,
            defaultDispatcher = coroutineDispatcher,
            initialInnerState = TracksState.Loading,
            logger = null,
            intentExecutor = this::executeIntent,
        )

    override val state = reducer.state
    override fun execute(intent: TracksIntent) {
        reducer.executeIntent(intent)
    }
    private fun executeIntent(intent: TracksIntent) =
        when (intent) {
            TracksIntent.Load -> executeLoad()
            is TracksIntent.Details -> executeDetails(intent.id)
            is TracksIntent.Delete -> executeDelete(intent.id)
        }

    //fun consumeSideEffect(sideEffect: TracksSideEffect) {}
    fun refresh() {
        reducer.executeIntent(TracksIntent.Load)
    }

    private fun executeLoad() = flow {
        val res = readAllTracks()
        var error: AppError? = null
        val e = res.exceptionOrNull()
        if(res.isFailure && e !is AppError.NotFound) {
            res.exceptionOrNull()?.let { error = AppError.fromThrowable(it) }
        }
        val tracks = res.getOrNull() ?: listOf()
        emit(TracksTransform.GoInit(tracks, error))
    }

    private fun executeDetails(id: Long) = flow {
        emit(TracksTransform.AddSideEffect(TracksSideEffect.Details(id)))
        delay(100)//Needed for some reason...
        emit(TracksTransform.GoLoad)//This way next time enters here, it reloads
    }

    private fun executeDelete(id: Long) = flow {
        val resDel = deleteTrack(id)
        if(resDel.isSuccess) {
            val res = readAllTracks()
            var error: AppError? = null
            val e = res.exceptionOrNull()
            if(res.isFailure && e !is AppError.NotFound) {
                res.exceptionOrNull()?.let { error = AppError.fromThrowable(it) }
            }
            val tracks = res.getOrNull() ?: listOf()
            emit(TracksTransform.GoInit(tracks, error))
        }
        else {
            android.util.Log.e(TAG, "executeDelete:e: ${resDel.exceptionOrNull()}")
            //emit(state.last())
        }
    }

    companion object {
        private const val TAG = "TracksVM"
    }
}