package com.cesoft.cesrunner.ui.settings

import com.cesoft.cesrunner.MainDispatcherRule
import com.cesoft.cesrunner.domain.entity.ApiKeysDto
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.usecase.ReadApiKeysUC
import com.cesoft.cesrunner.domain.usecase.ReadSettingsUC
import com.cesoft.cesrunner.domain.usecase.SaveApiKeysUC
import com.cesoft.cesrunner.domain.usecase.SaveSettingsUC
import com.cesoft.cesrunner.ui.settings.mvi.SettingsIntent
import com.cesoft.cesrunner.ui.settings.mvi.SettingsState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val readSettings: ReadSettingsUC = mockk()
    private val saveSettings: SaveSettingsUC = mockk(relaxed = true)
    private val readApiKeys: ReadApiKeysUC = mockk()
    private val saveApiKeys: SaveApiKeysUC = mockk(relaxed = true)
    private val settings = SettingsDto(minInterval = 2, minDistance = 10, voice = false)
    private val apiKeys = ApiKeysDto(groq = "groq-key")

    private fun createViewModel() = SettingsViewModel(
        readSettings, saveSettings, readApiKeys, saveApiKeys, mainDispatcherRule.dispatcher
    )

    @Test
    fun `load shows the stored settings and api keys`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { readSettings() } returns Result.success(settings)
        coEvery { readApiKeys() } returns Result.success(apiKeys)
        val viewModel = createViewModel()

        viewModel.execute(SettingsIntent.Load)
        advanceUntilIdle()

        assertEquals(SettingsState.Init(settings, apiKeys), viewModel.state.value.view)
    }

    @Test
    fun `load uses defaults when reading fails`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { readSettings() } returns Result.failure(Exception())
        coEvery { readApiKeys() } returns Result.failure(Exception())
        val viewModel = createViewModel()

        viewModel.execute(SettingsIntent.Load)
        advanceUntilIdle()

        assertEquals(SettingsState.Init(SettingsDto.Empty, ApiKeysDto.Empty), viewModel.state.value.view)
    }

    @Test
    fun `save stores the settings`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.execute(SettingsIntent.Save(settings))
        advanceUntilIdle()

        coVerify { saveSettings(settings) }
    }

    @Test
    fun `consecutive changes end with the latest value stored`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.execute(SettingsIntent.Save(settings.copy(voice = true)))
        viewModel.execute(SettingsIntent.Save(settings.copy(voice = true, minDistance = 20)))
        advanceUntilIdle()

        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            saveSettings(settings.copy(voice = true))
            saveSettings(settings.copy(voice = true, minDistance = 20))
        }
    }

    @Test
    fun `save api keys stores them`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.execute(SettingsIntent.SaveApiKeys(apiKeys))
        advanceUntilIdle()

        coVerify { saveApiKeys(apiKeys) }
    }
}
