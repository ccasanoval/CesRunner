package com.cesoft.cesrunner.ui.details

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.usecase.ReadAllTracksUC
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsIntent
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsSideEffect
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsState
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

class TrackDetailsViewModel(
    val readAllTracks: ReadAllTracksUC,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<TrackDetailsIntent, State<TrackDetailsState, TrackDetailsSideEffect>> {

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
            TrackDetailsIntent.Close -> executeClose()
        }
    fun consumeSideEffect(
        sideEffect: TrackDetailsSideEffect,
        navController: NavController,
        context: Context,
    ) {
        when (sideEffect) {
            TrackDetailsSideEffect.Close -> { navController.popBackStack() }
        }
    }

    private fun executeClose() = flow {
        emit(TrackDetailsTransform.AddSideEffect(TrackDetailsSideEffect.Close))
    }
    private fun executeLoad() = flow {
        val res = readAllTracks()
        var error: AppError? = null
        val e = res.exceptionOrNull()
        if(res.isFailure && e !is AppError.NotFound) {
            res.exceptionOrNull()?.let { error = AppError.fromThrowable(it) }
        }
        val tracks = res.getOrNull() ?: listOf()
        emit(TrackDetailsTransform.GoInit(tracks, error))
    }
}