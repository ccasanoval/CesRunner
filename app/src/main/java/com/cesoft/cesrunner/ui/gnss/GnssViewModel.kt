package com.cesoft.cesrunner.ui.gnss

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.ui.gnss.mvi.GnssIntent
import com.cesoft.cesrunner.ui.gnss.mvi.GnssSideEffect
import com.cesoft.cesrunner.ui.gnss.mvi.GnssState
import com.cesoft.cesrunner.ui.gnss.mvi.GnssTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GnssViewModel(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<GnssIntent, State<GnssState, GnssSideEffect>> {

    private val reducer = Reducer(
        coroutineScope = viewModelScope,
        defaultDispatcher = coroutineDispatcher,
        initialInnerState = GnssState.Loading,
        logger = null,
        intentExecutor = this::executeIntent
    )
    override val state = reducer.state
    override fun execute(intent: GnssIntent) {
        reducer.executeIntent(intent)
    }
    private fun executeIntent(intent: GnssIntent) =
        when(intent) {
            GnssIntent.Close -> executeClose()
            GnssIntent.Load -> executeLoad()
        }

    fun consumeSideEffect(
        sideEffect: GnssSideEffect,
        navController: NavController,
        context: Context,
    ) {
        when(sideEffect) {
            GnssSideEffect.Close -> { (context as Activity).finish() }
        }
    }

    private fun executeClose() = flow {
        emit(GnssTransform.AddSideEffect(GnssSideEffect.Close))
    }

    private fun executeLoad() = flow {
        //TOOD: Get GNSS info
        //https://github.com/barbeau/gpstest
        //https://barbeau.medium.com/gnss-interrupted-the-hidden-android-setting-you-need-to-know-d812d28a3821
        //https://developer.android.com/reference/android/location/GnssMeasurementRequest.Builder#setFullTracking(boolean)
        //https://barbeau.medium.com/crowdsourcing-gnss-capabilities-of-android-devices-d4228645cf25
        emit(GnssTransform.GoInit(GnssState.Init()))
    }
}