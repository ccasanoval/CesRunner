package com.cesoft.cesrunner.ui.aiagent

import ai.koog.agents.core.feature.model.toAgentError
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.toStr
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentIntent
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentSideEffect
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentState
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.theme.SepMed
import org.koin.androidx.compose.koinViewModel

@Composable
fun AIAgentPage(
    navController: NavController,
    viewModel: AIAgentViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    MviScreen(
        state = viewModel.state,
        onSideEffect = { sideEffect: AIAgentSideEffect ->
            viewModel.consumeSideEffect(
                sideEffect = sideEffect,
                navController = navController,
                context = context
            )
        },
        onBackPressed = {
            viewModel.execute(AIAgentIntent.Close)
        },
    ) { state: AIAgentState ->
        when(state) {
            is AIAgentState.Loading -> {
                android.util.Log.e("AIAgentPage", "AIAgentState.Loading---------------------")
                //TODO: Mostrar el content pero deshabilitado y con el sppiner...
                LoadingCompo()
            }
            is AIAgentState.Init -> {
                android.util.Log.e("AIAgentPage", "AIAgentState.Init---------------------$state")
                Content(state = state, reduce = viewModel::execute)
            }
        }
    }
}

@Composable
private fun Content(
    state: AIAgentState.Init,
    reduce: (intent: AIAgentIntent) -> Unit,
) {
    //TODO: Mostrar un icono (i) con posibles prompts
    //TODO: El resultado explicara la respuesta, y quiza tenga enlace a la carrera (id_track - deep link?)
    val prompt = rememberSaveable { mutableStateOf("Which is the longest run?") }
    LaunchedEffect(state.prompt, state.response) {
        if(state.prompt.isNotBlank()) prompt.value = state.prompt
    }
    Column {
        OutlinedTextField(
            value = prompt.value,
            onValueChange = { prompt.value = it },
            label = { Text(text = stringResource(R.string.prompt)) },
            maxLines = 10,
            modifier = Modifier
                .weight(.4f)
                .fillMaxWidth()
                .padding(SepMed),
        )
        Button(onClick = {reduce(AIAgentIntent.ExecPrompt(prompt.value))}) {
            Text(stringResource(R.string.request))
        }
        Spacer(Modifier.height(SepMed))
        if(state.error != null) {
            android.util.Log.e("Page", "state.error---A--------------- ${state.error} / ${state.error.toAgentError()} ")
            android.util.Log.e("Page", "state.error---B--------------- ${state.error.toAgentError().message} / ${state.error.toAgentError().stackTrace.firstOrNull()?.toString()} ")
            Text(
                text = state.error.message ?: "Error ?",
                color = Color.Red,
                modifier = Modifier
                    .weight(.4f)
                    .fillMaxWidth()
                    .padding(SepMed),
            )
        }
        else {
            Text(
                text = state.response,
                modifier = Modifier
                    .weight(.4f)
                    .fillMaxWidth()
                    .padding(SepMed),
            )
        }
    }
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun AIAgentPage_Preview() {
    val state = AIAgentState.Init()
    Surface(modifier = Modifier.fillMaxWidth()) {
        Content(state) { }
    }
}