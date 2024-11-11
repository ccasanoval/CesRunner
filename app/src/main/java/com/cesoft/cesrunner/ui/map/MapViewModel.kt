package com.cesoft.cesrunner.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.ui.map.mvi.MapIntent
import com.cesoft.cesrunner.ui.map.mvi.MapSideEffect
import com.cesoft.cesrunner.ui.map.mvi.MapState
import com.cesoft.cesrunner.ui.map.mvi.MapTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

class MapViewModel(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<MapIntent, State<MapState, MapSideEffect>> {

    private val reducer =
        Reducer(
            coroutineScope = viewModelScope,
            defaultDispatcher = coroutineDispatcher,
            initialInnerState = MapState.Init,
            logger = null,
            intentExecutor = this::executeIntent,
        )

    override val state = reducer.state

    override fun execute(intent: MapIntent) {
        reducer.executeIntent(intent)
    }

    private fun executeIntent(intent: MapIntent) =
        when (intent) {
            else -> executeClose()
        }

    private fun executeClose() = flow {
        emit(MapTransform.Close)
    }
}