package com.cesoft.cesrunner

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cesoft.cesrunner.ui.home.HomePage
import com.cesoft.cesrunner.ui.settings.SettingsPage


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
}

@Composable
fun PageNavigation() {
    val navController = rememberNavController()
    NavHost(navController, Page.Home.route) {
        //homePage()
        //settingsPage()
        composable(route = Page.Home.route) { HomePage(navController) }
        composable(route = Page.Settings.route) { SettingsPage(navController) }
    }
}