package com.cesoft.cesrunner.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.domain.usecase.GetLocationUC
import com.cesoft.cesrunner.ui.map.mvi.MapIntent
import com.cesoft.cesrunner.ui.map.mvi.MapSideEffect
import com.cesoft.cesrunner.ui.map.mvi.MapState
import com.cesoft.cesrunner.ui.map.mvi.MapTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

class MapViewModel(
    val getLocation: GetLocationUC,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<MapIntent, State<MapState, MapSideEffect>> {

    private val reducer =
        Reducer(
            coroutineScope = viewModelScope,
            defaultDispatcher = coroutineDispatcher,
            initialInnerState = MapState.Loading,
            logger = null,
            intentExecutor = this::executeIntent,
        )
    override val state = reducer.state
    override fun execute(intent: MapIntent) {
        reducer.executeIntent(intent)
    }
    private fun executeIntent(intent: MapIntent) =
        when (intent) {
            MapIntent.Load -> executeLoad()
            else -> executeClose()
        }

    fun consumeSideEffect(
        sideEffect: MapSideEffect,
        navController: NavController,
        context: Context,
    ) {
        when(sideEffect) {
            MapSideEffect.Close -> { navController.popBackStack() }
        }
    }

    private fun executeClose() = flow {
        emit(MapTransform.AddSideEffect(MapSideEffect.Close))
    }
    private fun executeLoad() = flow {
        val location = getLocation()
        val state = MapState.Init(
            location = location
        )
        emit(MapTransform.Load(state))
    }
}