package com.cesoft.cesrunner.ui.home.mvi

import com.adidas.mvi.sideeffects.SideEffects
import com.adidas.mvi.transform.SideEffectTransform
import com.adidas.mvi.transform.ViewTransform
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import kotlinx.coroutines.flow.StateFlow

internal object HomeTransform {

    data object GoLoading: ViewTransform<HomeState, HomeSideEffect>() {
        override fun mutate(currentState: HomeState): HomeState {
            return HomeState.Loading
        }
    }

    data class GoInit(
        val trackFlow: StateFlow<TrackDto?>?,
        val error: AppError?,
    ): ViewTransform<HomeState, HomeSideEffect>() {
        override fun mutate(currentState: HomeState): HomeState {
            return HomeState.Init(trackFlow, error)
        }
    }

    data class AddSideEffect(
        val sideEffect: HomeSideEffect
    ): SideEffectTransform<HomeState, HomeSideEffect>() {
        override fun mutate(sideEffects: SideEffects<HomeSideEffect>): SideEffects<HomeSideEffect> {
            return sideEffects.add(sideEffect)
        }
    }
}