package com.cesoft.cesrunner.ui.home

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
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.CurrentTrackingDto
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackingUC
import com.cesoft.cesrunner.domain.usecase.RequestLocationUpdatesUC
import com.cesoft.cesrunner.domain.usecase.SaveCurrentTrackingUC
import com.cesoft.cesrunner.domain.usecase.StopLocationUpdatesUC
import com.cesoft.cesrunner.ui.home.mvi.HomeIntent
import com.cesoft.cesrunner.ui.home.mvi.HomeSideEffect
import com.cesoft.cesrunner.ui.home.mvi.HomeState
import com.cesoft.cesrunner.ui.home.mvi.HomeTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

class HomeViewModel(
    val readCurrentTracking: ReadCurrentTrackingUC,
    val saveCurrentTracking: SaveCurrentTrackingUC,
    val requestLocationUpdates: RequestLocationUpdatesUC,
    val stopLocationUpdates: StopLocationUpdatesUC,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<HomeIntent, State<HomeState, HomeSideEffect>> {

    private val reducer =
        Reducer(
            coroutineScope = viewModelScope,
            defaultDispatcher = coroutineDispatcher,
            initialInnerState = HomeState.Loading,
            logger = null,
            intentExecutor = this::executeIntent,
        )

    override val state = reducer.state

    override fun execute(intent: HomeIntent) {
        reducer.executeIntent(intent)
    }

    private fun executeIntent(intent: HomeIntent) =
        when(intent) {
            HomeIntent.Load -> executeLoad()
            HomeIntent.GoStart -> executeStart()
            HomeIntent.GoSettings -> flow { emit(HomeTransform.AddSideEffect(HomeSideEffect.GoSettings)) }
            HomeIntent.GoMap -> executeLogout()
            HomeIntent.GoTracks -> executeLogout()
            HomeIntent.Close -> executeClose()
        }

    private fun executeLoad() = flow {
        val res = readCurrentTracking()
        val currentTracking = res.getOrNull() ?: CurrentTrackingDto.Empty
        var error: AppError? = null
        if(res.isFailure) {
            res.exceptionOrNull()?.let { error = AppError.fromThrowable(it) }
        }
        emit(HomeTransform.GoInit(currentTracking, error))
    }

    private fun executeStart() = flow {
        requestLocationUpdates()
        emit(HomeTransform.AddSideEffect(HomeSideEffect.Start))
    }

    private fun executeClose() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.Close))
    }

    private fun executeLogout() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.Close))
    }

    fun consumeSideEffect(
        sideEffect: HomeSideEffect,
        navController: NavController,
        context: Context,
    ) {
        when(sideEffect) {
            HomeSideEffect.Start -> {
                navController.navigate(Page.Settings.route)
            }
            HomeSideEffect.GoSettings -> {
                navController.navigate(Page.Settings.route)
                //Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
            HomeSideEffect.GoTracks -> {
                Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
            HomeSideEffect.GoMaps -> {
                Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
            HomeSideEffect.Close -> (context as Activity).finish()
        }
    }
}