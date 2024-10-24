package com.cesoft.cesrunner

import com.cesoft.cesrunner.data.Repository
import com.cesoft.cesrunner.domain.repository.RepositoryContract
import com.cesoft.cesrunner.domain.usecase.ReadSettingsUC
import com.cesoft.cesrunner.domain.usecase.SaveSettingsUC
import com.cesoft.cesrunner.ui.home.HomeViewModel
import com.cesoft.cesrunner.ui.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<CoroutineDispatcher> { Dispatchers.Default }
    single<RepositoryContract> { Repository(get()) }
    single<ReadSettingsUC> { ReadSettingsUC(get()) }
    single<SaveSettingsUC> { SaveSettingsUC(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
}
