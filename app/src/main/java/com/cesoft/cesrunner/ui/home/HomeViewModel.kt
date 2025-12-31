package com.cesoft.cesrunner.ui.home

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.adidas.mvi.MviHost
import com.adidas.mvi.State
import com.adidas.mvi.reducer.Reducer
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackFlowUC
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackUC
import com.cesoft.cesrunner.domain.usecase.ReadSettingsUC
import com.cesoft.cesrunner.domain.usecase.ReadVo2MaxUC
import com.cesoft.cesrunner.domain.usecase.RequestLocationUpdatesUC
import com.cesoft.cesrunner.tracking.TrackingServiceFac
import com.cesoft.cesrunner.ui.home.mvi.HomeIntent
import com.cesoft.cesrunner.ui.home.mvi.HomeSideEffect
import com.cesoft.cesrunner.ui.home.mvi.HomeState
import com.cesoft.cesrunner.ui.home.mvi.HomeTransform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val trackingServiceFac: TrackingServiceFac,
    private val readCurrentTrack: ReadCurrentTrackUC,
    private val readCurrentTrackFlow: ReadCurrentTrackFlowUC,
    private val requestLocationUpdates: RequestLocationUpdatesUC,
    private val readVo2Max: ReadVo2MaxUC,
    private val readSettings: ReadSettingsUC,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel(), MviHost<HomeIntent, State<HomeState, HomeSideEffect>> {

    lateinit var navigation: NavBackStack<NavKey>
//    private var location: Location? = null
//    init {
//        android.util.Log.e(TAG, "init---a---------- 00000")
//        val delay = 5*60*1000L
//        requestLocationUpdates(delay, 0f).getOrNull()
//            ?.onEach { l ->
//                location = l
//                location?.let {
//                    android.util.Log.e(TAG, "init--a----------- $location")
//                    reducer.executeIntent(HomeIntent.Load)
//                }
//                delay(500)
//            }
//            ?.launchIn(viewModelScope)
//    }

    private val reducer = Reducer(
        coroutineScope = viewModelScope,
        defaultDispatcher = coroutineDispatcher,
        initialInnerState = HomeState.Loading,
        logger = null,
        intentExecutor = this::executeIntent
    )
    override val state = reducer.state
    override fun execute(intent: HomeIntent) {
        reducer.executeIntent(intent)
    }
    private fun executeIntent(intent: HomeIntent) =
        when(intent) {
            HomeIntent.Close -> executeClose()
            HomeIntent.Load -> executeLoad()
            HomeIntent.GoStart -> executeStart()
            HomeIntent.GoSettings -> executeSettings()
            HomeIntent.GoMap -> executeMap()
            HomeIntent.GoTracks -> executeTracks()
            HomeIntent.GoGnss -> executeGnss()
            HomeIntent.GoAIAgent -> executeAIAgent()
            HomeIntent.GoAIAgentGroq -> executeAIAgentGroq()
        }
    fun consumeSideEffect(
        sideEffect: HomeSideEffect,
        navController: NavController,
        context: Context,
    ) {
        when(sideEffect) {
//            HomeSideEffect.Start -> { navController.navigate(Page.Tracking.route) }
//            HomeSideEffect.GoSettings -> { navController.navigate(Page.Settings.route) }
//            HomeSideEffect.GoTracks -> { navController.navigate(Page.Tracks.route) }
//            HomeSideEffect.GoMap -> { navController.navigate(Page.Map.route) }
//            HomeSideEffect.GoGnss -> { navController.navigate(Page.Gnss.route) }
//            HomeSideEffect.GoAIAgent -> { navController.navigate(Page.AIAgent.route) }
//            HomeSideEffect.GoAIAgentGroq -> { navController.navigate(Page.AIAgentGroq.route) }
            HomeSideEffect.Close -> { (context as Activity).finish() }
            else -> {}
        }
    }

    private fun executeLoad() = flow {
        val locationDelay = 1*60*1000L
        val locationFlow = requestLocationUpdates(locationDelay,0f).getOrNull()
//        var locationFlow: StateFlow<Location?>? = null
//        requestLocationUpdates(1*60*1000L, 0f).getOrNull()?.let {
//            locationFlow = it.stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(5_000),
//                initialValue = null,
//            )
//        }
        val vo2Max = readVo2Max()

        val currentTrack = readCurrentTrack().getOrNull() ?: TrackDto.Empty
        if(currentTrack.isCreated) {
            val settings = readSettings().getOrNull() ?: SettingsDto.Empty
            trackingServiceFac.start(//currentTrack.minInterval, currentTrack.minDistance)
                minInterval = settings.minInterval,
                minDistance = settings.minDistance
            )
        }
        val res = readCurrentTrackFlow()
        res.getOrNull()?.let {
            val flow: StateFlow<TrackDto?> = it.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TrackDto.Empty,
            )
            emit(HomeTransform.GoInit(vo2Max, flow, locationFlow, null))
        } ?: run {
            val e: AppError = res.exceptionOrNull()
                ?.let { AppError.DataBaseError(it) } ?: run { AppError.NotFound }
            val flow = MutableStateFlow<TrackDto?>(null)
            emit(HomeTransform.GoInit(vo2Max, flow, locationFlow, e))
        }
    }

    private fun executeClose() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.Close))
    }

    private fun executeStart() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.Start))
        delay(1000L)
        emit(HomeTransform.GoLoading)
    }

    private fun executeSettings() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.GoSettings))
        delay(1000L)
        emit(HomeTransform.GoLoading)
    }

    private fun executeMap() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.GoMap))
        delay(1000L)
        emit(HomeTransform.GoLoading)
    }

    private fun executeTracks() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.GoTracks))
        delay(1000L)
        emit(HomeTransform.GoLoading)
    }

    private fun executeGnss() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.GoGnss))
        delay(1000L)
        emit(HomeTransform.GoLoading)
    }

    private fun executeAIAgent() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.GoAIAgent))
        delay(200L)
        emit(HomeTransform.GoLoading)
    }
    private fun executeAIAgentGroq() = flow {
        emit(HomeTransform.AddSideEffect(HomeSideEffect.GoAIAgentGroq))
        delay(200L)
        emit(HomeTransform.GoLoading)
    }

    companion object {
        private const val TAG = "HomeVM"
    }
}