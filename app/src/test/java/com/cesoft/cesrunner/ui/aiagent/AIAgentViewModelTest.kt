package com.cesoft.cesrunner.ui.aiagent

import com.cesoft.cesrunner.MainDispatcherRule
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.ApiKeysDto
import com.cesoft.cesrunner.domain.usecase.ReadApiKeysUC
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentIntent
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AIAgentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val readApiKeys: ReadApiKeysUC = mockk()

    @Test
    fun `prompt without api key shows the missing key error`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { readApiKeys() } returns Result.success(ApiKeysDto.Empty)
        val viewModel = AIAgentViewModel(
            mockk(), mockk(), mockk(), readApiKeys, mainDispatcherRule.dispatcher
        )

        viewModel.execute(AIAgentIntent.ExecPrompt("longest run?"))
        advanceUntilIdle()

        val state = viewModel.state.value.view as AIAgentState.Init
        assertEquals(AppError.MissingApiKey, state.error)
        assertFalse(state.loading)
    }
}
