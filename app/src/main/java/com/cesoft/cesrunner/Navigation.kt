package com.cesoft.cesrunner

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cesoft.cesrunner.ui.aiagent.AIAgentPage
import com.cesoft.cesrunner.ui.aiagentgroq.AIAgentGroqPage
import com.cesoft.cesrunner.ui.details.TrackDetailsPage
import com.cesoft.cesrunner.ui.gnss.GnssPage
import com.cesoft.cesrunner.ui.home.HomePage
import com.cesoft.cesrunner.ui.map.MapPage
import com.cesoft.cesrunner.ui.settings.SettingsPage
import com.cesoft.cesrunner.ui.tracking.TrackingPage
import com.cesoft.cesrunner.ui.tracks.TracksPage


sealed class Page(val route: String) {

//    data object Base : Page("{id}/{name}") {
//        private const val ARG_ID = "id"
//        private const val ARG_NAME = "name"
//        fun getId(savedStateHandle: SavedStateHandle): Long? =
//            savedStateHandle.get<String>(ARG_ID)?.toLong()
//        fun getName(savedStateHandle: SavedStateHandle): String? =
//            savedStateHandle.get<String>(ARG_NAME)
//    }

    data object Home: Page("home")
    data object Settings: Page("settings")
    data object Tracking: Page("tracking")
    data object Map: Page("map")
    data object Gnss: Page("gnss")
    data object AIAgent: Page("ai_agent")
    data object AIAgentGroq: Page("ai_agent_groq")
    data object Tracks: Page("tracks")
    data object TrackDetail: Page("trackDetail/{id}") {
        private const val ARG_ID = "id"
        fun createRoute(id: Long) = "trackDetail/$id"
        fun getId(savedStateHandle: SavedStateHandle): Long? =
            savedStateHandle.get<String>(ARG_ID)?.toLong()
    }
}

@Composable
fun PageNavigation() {
    val navController = rememberNavController()
    NavHost(navController, Page.Home.route) {
        composable(route = Page.Home.route) { HomePage(navController) }
        composable(route = Page.Settings.route) { SettingsPage(navController) }
        composable(route = Page.Tracking.route) { TrackingPage(navController) }
        composable(route = Page.Map.route) { MapPage(navController) }
        composable(route = Page.Gnss.route) { GnssPage(navController) }
        composable(route = Page.AIAgent.route) { AIAgentPage(navController) }
        composable(route = Page.AIAgentGroq.route) { AIAgentGroqPage(navController) }
        composable(route = Page.Tracks.route) { TracksPage(navController) }
        composable(route = Page.TrackDetail.route) { TrackDetailsPage(navController) }
    }
}
