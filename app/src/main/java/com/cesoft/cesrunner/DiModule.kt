package com.cesoft.cesrunner

import com.cesoft.cesrunner.ui.home.HomeViewModel
import com.cesoft.cesrunner.ui.settings.SettingsViewModel
import com.cesoft.cesrunner.ui.tracks.TracksViewModel
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class DiModule {
    @KoinViewModel
    internal fun provideHomeViewModel(): HomeViewModel {
        return HomeViewModel()
    }
    @KoinViewModel
    internal fun provideSettingsViewModel(): SettingsViewModel {
        return SettingsViewModel()
    }
    @KoinViewModel
    internal fun provideTracksViewModel(): TracksViewModel {
        return TracksViewModel()
    }
}