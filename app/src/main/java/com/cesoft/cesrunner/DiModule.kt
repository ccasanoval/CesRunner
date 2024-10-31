package com.cesoft.cesrunner

import androidx.room.Room
import com.cesoft.cesrunner.data.Repository
import com.cesoft.cesrunner.data.local.AppDatabase
import com.cesoft.cesrunner.data.location.LocationDataSource
import com.cesoft.cesrunner.domain.repository.RepositoryContract
import com.cesoft.cesrunner.domain.usecase.CreateTrackUC
import com.cesoft.cesrunner.domain.usecase.DeleteCurrentTrackingUC
import com.cesoft.cesrunner.domain.usecase.ReadAllTracksUC
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackingUC
import com.cesoft.cesrunner.domain.usecase.ReadSettingsUC
import com.cesoft.cesrunner.domain.usecase.ReadTrackUC
import com.cesoft.cesrunner.domain.usecase.RequestLocationUpdatesUC
import com.cesoft.cesrunner.domain.usecase.SaveCurrentTrackingUC
import com.cesoft.cesrunner.domain.usecase.SaveSettingsUC
import com.cesoft.cesrunner.domain.usecase.StopLocationUpdatesUC
import com.cesoft.cesrunner.tracking.TrackingServiceFac
import com.cesoft.cesrunner.ui.home.HomeViewModel
import com.cesoft.cesrunner.ui.settings.SettingsViewModel
import com.cesoft.cesrunner.ui.tracking.TrackingViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    /// CORE
    single<CoroutineDispatcher> { Dispatchers.Default }
    single<LocationDataSource> { LocationDataSource(get()) }
    single<RepositoryContract> { Repository(get(), get(), get()) }
    single<TrackingServiceFac> { TrackingServiceFac(get()) }
    single<AppDatabase> {
        Room.databaseBuilder(get(), AppDatabase::class.java, "CesRunner").build()
    }

    /// PREF
    single<ReadSettingsUC> { ReadSettingsUC(get()) }
    single<SaveSettingsUC> { SaveSettingsUC(get()) }
    single<ReadCurrentTrackingUC> { ReadCurrentTrackingUC(get()) }
    single<SaveCurrentTrackingUC> { SaveCurrentTrackingUC(get()) }
    single<DeleteCurrentTrackingUC> { DeleteCurrentTrackingUC(get()) }
    /// TRACKING SERVICE
    single<RequestLocationUpdatesUC> { RequestLocationUpdatesUC(get()) }
    single<StopLocationUpdatesUC> { StopLocationUpdatesUC(get()) }
    /// TRACKING DB
    single<CreateTrackUC> { CreateTrackUC(get()) }
    single<ReadTrackUC> { ReadTrackUC(get()) }
    single<ReadAllTracksUC> { ReadAllTracksUC(get()) }

    /// VIEWMODEL
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { TrackingViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
