package com.cesoft.cesrunner.ui.aiagent

import ai.koog.agents.core.feature.model.toAgentError
import ai.koog.prompt.dsl.prompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentIntent
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentSideEffect
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentState
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.theme.SepMed
import com.cesoft.cesrunner.ui.theme.SepMin
import kotlinx.serialization.descriptors.SerialDescriptor
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
            viewModel.execute(AIAgentIntent.Back)
        },
    ) { state: AIAgentState ->
        when(state) {
//            is AIAgentState.Loading -> {
//                android.util.Log.e("AIAgentPage", "AIAgentState.Loading---------------------")
//                //TODO: Mostrar el content pero deshabilitado y con el sppiner...
//                LoadingCompo()
//            }
            is AIAgentState.Init -> {
                android.util.Log.e("AIAgentPage", "AIAgentState.Init---------------------$state")
                Content(state = state, reduce = viewModel::execute)
            }
        }
    }
}

@Composable
fun DisableLoadingCompo(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .alpha(.75f)
            .zIndex(100f)
    ) {
        Box {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun Content(
    state: AIAgentState.Init,
    reduce: (intent: AIAgentIntent) -> Unit,
) {
    if(state.loading) {
        DisableLoadingCompo()
    }

    //TODO: id_track - deep link?
    val prompt = rememberSaveable { mutableStateOf("Which is the longest run?") }
    LaunchedEffect(state.prompt, state.response) {
        if(state.prompt.isNotBlank()) prompt.value = state.prompt
    }
    Column {
        PredefinedOptions(prompt)
        OutlinedTextField(
            value = prompt.value,
            onValueChange = { prompt.value = it },
            label = { Text(text = stringResource(R.string.prompt)) },
            maxLines = 5,
            modifier = Modifier
                .weight(.3f)
                .fillMaxWidth()
                .padding(SepMed),
        )
        Button(
            modifier = Modifier.padding(start = SepMed),
            onClick = { reduce(AIAgentIntent.ExecPrompt(prompt = prompt.value)) }
        ) {
            Text(
                text = stringResource(R.string.request)
            )
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

@Composable
private fun PredefinedOptionButton(title: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, Color.Gray),
        shape = RoundedCornerShape(50), // or shape = CircleShape
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
        modifier = Modifier.padding(SepMin)
    ) {
        Text(text = title, fontSize = 10.sp)
    }
}
@Composable
private fun PredefinedOptions(prompt: MutableState<String>) {
    FlowRow {
        PredefinedOptionButton("La más corta") {
            prompt.value = "¿Cuál es la carrera más corta?"
        }
        PredefinedOptionButton("La más larga") {
            prompt.value = "¿Cuál es la carrera más larga?"
        }
        PredefinedOptionButton("Dura 5 min") {
            prompt.value = "¿Qué carrera tiene aproximadamente una duración de 5 minutos?"
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

@Preview
@Composable
private fun AIAgentPage_Loading_Preview() {
    val state = AIAgentState.Init(
        prompt = "Prompt test and bla bla bla",
        response = "Response test and bla bla bla",
        loading = true
    )
    Surface(modifier = Modifier.fillMaxWidth()) {
        Content(state) { }
    }
}