package com.cesoft.cesrunner

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.cesoft.cesrunner.ui.aiagent.AIAgentPage
import com.cesoft.cesrunner.ui.aiagentgroq.AIAgentGroqPage
import com.cesoft.cesrunner.ui.details.TrackDetailsPage
import com.cesoft.cesrunner.ui.home.HomePage
import com.cesoft.cesrunner.ui.map.MapPage
import com.cesoft.cesrunner.ui.settings.SettingsPage
import com.cesoft.cesrunner.ui.tracking.TrackingPage
import com.cesoft.cesrunner.ui.tracks.TracksPage
import kotlinx.serialization.Serializable

@Serializable data object HomeNavKey : NavKey
@Serializable data object SettingsNavKey : NavKey
@Serializable data object TrackingNavKey : NavKey
@Serializable data object MapNavKey : NavKey
@Serializable data object AIAgentNavKey: NavKey
@Serializable data object AIAgentGroqNavKey: NavKey
@Serializable data object TracksNavKey: NavKey
@Serializable data class TrackDetailNavKey(val id: Long): NavKey

@Composable
fun PageNavigation() {
    val backStack = rememberNavBackStack(HomeNavKey)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        // Essential decorators for production apps
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        // Entry provider using DSL
        entryProvider = entryProvider {
            entry<HomeNavKey> {
                HomePage(
                    onStart = { backStack.add(TrackingNavKey) },
                    onTracks = { backStack.add(TracksNavKey) },
                    onMap = { backStack.add(MapNavKey) },
                    onAIAgent = { backStack.add(AIAgentNavKey) },
                    onAIAgentGroq = { backStack.add(AIAgentGroqNavKey) },
                    onSettings = { backStack.add(SettingsNavKey) }
                )
            }
            entry<TrackingNavKey> {
                TrackingPage({ backStack.removeLastOrNull() })
            }
            entry<TracksNavKey> {
                TracksPage(
                    onDetails = { id -> backStack.add(TrackDetailNavKey(id)) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<MapNavKey> {
                MapPage({ backStack.removeLastOrNull() })
            }
            entry<SettingsNavKey> {
                SettingsPage(
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<AIAgentNavKey> {
                AIAgentPage(
                    onGoBack = { backStack.removeLastOrNull() },
                    onGoToTrack = { id -> backStack.add(TrackDetailNavKey(id)) }
                )
            }
            entry<AIAgentGroqNavKey> {
                AIAgentGroqPage(
                    onGoBack = { backStack.removeLastOrNull() },
                    onGoToTrack = { id -> backStack.add(TrackDetailNavKey(id)) }
                )
            }
            entry<TrackDetailNavKey> { key ->
                TrackDetailsPage(
                    id = key.id,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        },
        // Smooth animations
        transitionSpec = {
            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
        },
        popTransitionSpec = {
            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
        }
    )
}

