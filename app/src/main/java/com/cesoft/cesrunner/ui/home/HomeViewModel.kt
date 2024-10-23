package com.cesoft.cesrunner.ui.home

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.adidas.mvi.LoggableState
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.Page
import com.cesoft.cesrunner.ui.home.mvi.HomeIntent
import com.cesoft.cesrunner.ui.home.mvi.HomeSideEffect
import com.cesoft.cesrunner.ui.home.mvi.HomeState
import com.cesoft.cesrunner.ui.home.mvi.HomeTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

class HomeViewModel(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<HomeIntent, State<HomeState, HomeSideEffect>> {

    private val reducer =
        Reducer<HomeIntent, HomeState, HomeSideEffect>(
            coroutineScope = viewModelScope,
            defaultDispatcher = coroutineDispatcher,
            initialInnerState = HomeState.Init,
            logger = null,
            intentExecutor = this::executeIntent,
        )

    override val state = reducer.state

    override fun execute(intent: HomeIntent) {
        reducer.executeIntent(intent)
    }

    private fun executeIntent(intent: HomeIntent) =
        when (intent) {
            HomeIntent.GoStart -> executeLogout()
            HomeIntent.GoSettings -> flow { emit(HomeTransform.AddSideEffect(HomeSideEffect.GoSettings)) }
            HomeIntent.GoMap -> executeLogout()
            HomeIntent.GoTracks -> executeLogout()
            HomeIntent.Close -> executeClose()
        }

    private fun executeClose() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.Close))
    }

    private fun executeLogout() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.Close))
    }

//    private fun executeLogin(intent: LoginIntent.Login) = flow {
//        emit(HomeTransform.SetIsLoggingIn(isLoggingIn = true))
//
//        delay(300)
//
//        emit(LoginTransform.SetIsLoggingIn(isLoggingIn = false))
//
//        if (intent.username.isEmpty() || intent.password.isEmpty()) {
//            emit(LoginTransform.AddSideEffect(LoginSideEffect.ShowInvalidCredentialsError))
//        } else {
//            emit(LoginTransform.SetLoggedIn(intent.username))
//        }
//    }
    fun consumeSideEffect(
        sideEffect: HomeSideEffect,
        navController: NavController,
        context: Context,
    ) {
        when(sideEffect) {
            HomeSideEffect.GoStart -> {
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