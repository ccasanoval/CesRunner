package com.cesoft.cesrunner.ui.tracks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.ui.tracks.mvi.TracksIntent
import com.cesoft.cesrunner.ui.tracks.mvi.TracksSideEffect
import com.cesoft.cesrunner.ui.tracks.mvi.TracksState
import com.cesoft.cesrunner.ui.tracks.mvi.TracksTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

class TracksViewModel(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<TracksIntent, State<TracksState, TracksSideEffect>> {

    private val reducer =
        Reducer(
            coroutineScope = viewModelScope,
            defaultDispatcher = coroutineDispatcher,
            initialInnerState = TracksState.Init,
            logger = null,
            intentExecutor = this::executeIntent,
        )

    override val state = reducer.state

    override fun execute(intent: TracksIntent) {
        reducer.executeIntent(intent)
    }

    private fun executeIntent(intent: TracksIntent) =
        when (intent) {
            else -> executeClose()
        }

    private fun executeClose() = flow {
        emit(TracksTransform.Close)
    }
}