package com.cesoft.cesrunner.ui.home.mvi

import com.adidas.mvi.sideeffects.SideEffects
import com.adidas.mvi.transform.SideEffectTransform
import com.adidas.mvi.transform.ViewTransform

internal object HomeTransform {

//    object GoTracks: ViewTransform<HomeState, HomeSideEffect>() {
//        override fun mutate(currentState: HomeState): HomeState {
//            return HomeState.
//        }
//    }
//
//    data class SetIsLoggingIn(val isLoggingIn: Boolean) :
//        ViewTransform<LoginState, LoginSideEffect>() {
//        override fun mutate(currentState: LoginState): LoginState {
//            return LoginState.LoggedOut(isLoggingIn = isLoggingIn)
//        }
//    }
//
//    data class SetLoggedIn(val username: String) : ViewTransform<LoginState, LoginSideEffect>() {
//        override fun mutate(currentState: LoginState): LoginState {
//            return LoginState.LoggedIn(username)
//        }
//    }
//
    data class AddSideEffect(
    val sideEffect: HomeSideEffect,
    ) : SideEffectTransform<HomeState, HomeSideEffect>() {
        override fun mutate(sideEffects: SideEffects<HomeSideEffect>): SideEffects<HomeSideEffect> {
            return sideEffects.add(sideEffect)
        }
    }
}