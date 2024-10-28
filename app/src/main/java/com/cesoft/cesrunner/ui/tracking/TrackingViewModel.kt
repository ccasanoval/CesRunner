package com.cesoft.cesrunner.ui.tracking

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.Page
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackingUC
import com.cesoft.cesrunner.domain.usecase.RequestLocationUpdatesUC
import com.cesoft.cesrunner.domain.usecase.SaveCurrentTrackingUC
import com.cesoft.cesrunner.domain.usecase.StopLocationUpdatesUC
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingIntent
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingSideEffect
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingState
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

class TrackingViewModel(
    val readCurrentTracking: ReadCurrentTrackingUC,
    val saveCurrentTracking: SaveCurrentTrackingUC,
    val requestLocationUpdates: RequestLocationUpdatesUC,
    val stopLocationUpdates: StopLocationUpdatesUC,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<TrackingIntent, State<TrackingState, TrackingSideEffect>> {

    private val reducer =
        Reducer(
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
            else -> executeClose()
        }

    private fun executeClose() = flow {
        emit(TrackingTransform.AddSideEffect(TrackingSideEffect.Close))
    }

    fun consumeSideEffect(
        sideEffect: TrackingSideEffect,
        navController: NavController,
        context: Context,
    ) {
        when(sideEffect) {
            TrackingSideEffect.Close -> (context as Activity).finish()
        }
    }
}