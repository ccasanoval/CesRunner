package com.cesoft.cesrunner.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.MessageType
import com.cesoft.cesrunner.data.gpx.GpxUtil
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.usecase.DeleteTrackUC
import com.cesoft.cesrunner.domain.usecase.GetLocationUC
import com.cesoft.cesrunner.domain.usecase.ReadTrackUC
import com.cesoft.cesrunner.domain.usecase.UpdateTrackUC
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsIntent
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsSideEffect
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsState
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class TrackDetailsViewModel(
    private val readTrack: ReadTrackUC,
    private val updateTrack: UpdateTrackUC,
    private val getLocation: GetLocationUC,
    private val deleteTrack: DeleteTrackUC,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<TrackDetailsIntent, State<TrackDetailsState, TrackDetailsSideEffect>> {
    private var track: TrackDto = TrackDto.Empty
    var trackId: Long = -1

    private val reducer =
        Reducer(
            coroutineScope = viewModelScope,
            defaultDispatcher = coroutineDispatcher,
            initialInnerState = TrackDetailsState.Loading,
            logger = null,
            intentExecutor = this::executeIntent,
        )

    override val state = reducer.state
    override fun execute(intent: TrackDetailsIntent) {
        reducer.executeIntent(intent)
    }
    private fun executeIntent(intent: TrackDetailsIntent) =
        when (intent) {
            TrackDetailsIntent.Load -> executeLoad()
            TrackDetailsIntent.Delete -> executeDelete()
            TrackDetailsIntent.Export -> executeExport()
            is TrackDetailsIntent.SaveName -> executeSaveName(intent.name)
        }

    fun consumeSideEffect(
        sideEffect: TrackDetailsSideEffect,
        onBack: () -> Unit
    ) {
        when(sideEffect) {
            TrackDetailsSideEffect.Close -> onBack()
        }
    }

    private fun executeLoad() = flow {
        val location = getLocation()
        //val id = Page.TrackDetail.getId(savedStateHandle)
        if(trackId > 0) {
            val res = readTrack(trackId)
            track = res.getOrNull() ?: TrackDto.Empty
            var error: AppError? = null
            val e = res.exceptionOrNull()
            if(res.isFailure && e !is AppError.NotFound) {
                res.exceptionOrNull()?.let { error = AppError.fromThrowable(it) }
            }
            emit(TrackDetailsTransform.GoInit(track, location, error))
            error?.let {
                delay(ERROR_TIME)//Delete error from state
                emit(TrackDetailsTransform.GoInit(track, location, null, null))
            }
        } else {
            emit(TrackDetailsTransform.GoInit(track, location, AppError.NotFound))
            delay(ERROR_TIME)//Delete error from state
            emit(TrackDetailsTransform.GoInit(track, location, null, null))
        }
    }
    private fun executeDelete() = flow {
        val location = getLocation()
        val res = deleteTrack(track.id)
        if(res.isSuccess) {
            emit(TrackDetailsTransform.AddSideEffect(TrackDetailsSideEffect.Close))
        }
        else {
            val error = AppError.fromThrowable(res.exceptionOrNull() ?: UnknownError())
            emit(TrackDetailsTransform.GoInit(track, location, error))
            delay(ERROR_TIME)//Delete error from state
            emit(TrackDetailsTransform.GoInit(track, location, null, null))
        }
    }
    private fun executeExport() = flow {
        val location = getLocation()
        val res = GpxUtil().export(track)
        if(res.isSuccess) {
            emit(TrackDetailsTransform.GoInit(track, location, null, MessageType.Exported))
            delay(MESSAGE_TIME)//Delete message from state
            emit(TrackDetailsTransform.GoInit(track, location, null, null))
        }
        else {
            val error = AppError.fromThrowable(res.exceptionOrNull() ?: UnknownError())
            emit(TrackDetailsTransform.GoInit(track, location, error))
            delay(ERROR_TIME)//Delete error from state
            emit(TrackDetailsTransform.GoInit(track, location, null, null))
        }
    }
    private fun executeSaveName(name: String) = flow {
        //val res = readTrack(id)
        val location = getLocation()
        val newTrack = track.copy(name = name)
        val res = updateTrack(newTrack)
        if(res.isSuccess) {
            track = newTrack
            emit(TrackDetailsTransform.GoInit(track, location, null, MessageType.Saved))
            delay(MESSAGE_TIME)//Delete message from state
            emit(TrackDetailsTransform.GoInit(track, location, null, null))
        }
        else {
            val e = res.exceptionOrNull()?.let { AppError.DataBaseError(it) } ?: AppError.NotFound
            emit(TrackDetailsTransform.GoInit(track, location, e))
            delay(ERROR_TIME)//Delete error from state
            emit(TrackDetailsTransform.GoInit(track, location, null, null))
        }
    }

    companion object {
        private const val TAG = "TrackDetailsVM"
        private const val MESSAGE_TIME = 3000L//ms
        private const val ERROR_TIME = 3000L//ms
    }
}