package com.cesoft.cesrunner.ui.aiagentgroq

import ai.koog.agents.core.feature.model.toAgentError
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.ui.aiagent.mvi.AIAgentIntent
import com.cesoft.cesrunner.ui.aiagentgroq.mvi.AIAgentGroqIntent
import com.cesoft.cesrunner.ui.aiagentgroq.mvi.AIAgentGroqSideEffect
import com.cesoft.cesrunner.ui.aiagentgroq.mvi.AIAgentGroqState
import com.cesoft.cesrunner.ui.theme.SepMed
import com.cesoft.cesrunner.ui.theme.SepMin
import com.cesoft.cesrunner.ui.theme.fontBig
import org.koin.androidx.compose.koinViewModel
import kotlin.text.firstOrNull

@Composable
fun AIAgentGroqPage(
    navController: NavController,
    viewModel: AIAgentGroqViewModel = koinViewModel(),
) {
    MviScreen(
        state = viewModel.state,
        onSideEffect = { sideEffect: AIAgentGroqSideEffect ->
            viewModel.consumeSideEffect(
                sideEffect = sideEffect,
                navController = navController
            )
        },
        onBackPressed = {
            viewModel.execute(AIAgentGroqIntent.Back)
        },
    ) { state: AIAgentGroqState ->
        when(state) {
            is AIAgentGroqState.Init -> {
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
    state: AIAgentGroqState.Init,
    reduce: (intent: AIAgentGroqIntent) -> Unit,
) {
    android.util.Log.e("AA", "-------------- $state")
    if(state.loading) {
        DisableLoadingCompo()
    }

    val prompt = rememberSaveable { mutableStateOf("Which is the longest run?") }
    LaunchedEffect(state.prompt, state.response) {
        if(state.prompt.isNotBlank()) prompt.value = state.prompt
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PredefinedOptions(prompt)
        OutlinedTextField(
            value = prompt.value,
            onValueChange = { prompt.value = it },
            label = { Text(text = stringResource(R.string.prompt_groq)) },
            maxLines = 5,
            modifier = Modifier
                .weight(.2f)
                .fillMaxWidth()
                .padding(SepMed),
        )
        val kb = LocalSoftwareKeyboardController.current
        Button(
            modifier = Modifier.padding(start = SepMed),
            onClick = {
                kb?.hide()
                reduce(AIAgentGroqIntent.ExecPrompt(prompt = prompt.value))
            }
        ) {
            Text(text = stringResource(R.string.request))
        }
        Spacer(Modifier.height(SepMed))
        if(state.error != null) {
            android.util.Log.e("Page", "state.error---A--------------- ${state.error} / ${state.error.toAgentError()} ")
            android.util.Log.e("Page", "state.error---B--------------- ${state.error.toAgentError().message} / ${state.error.toAgentError().stackTrace.firstOrNull()?.toString()} ")
            Text(
                text = state.error.message ?: "Error ?",
                color = Color.Red,
                modifier = Modifier
                    .weight(.3f)
                    .fillMaxWidth()
                    .padding(SepMed),
            )
        }
        else {
            LazyColumn(Modifier.weight(.4f)) {
                for(o in state.responseData) {
                    item { HorizontalDivider() }
                    item {
                        Text(
                            text = "Id: ${o.id}",
                            color = Color.Blue,
                            fontSize = fontBig,
                            modifier = Modifier.clickable {
                                reduce(AIAgentGroqIntent.GoToTrack(idTrack = o.id))
                            }
                        )
                        Text(stringResource(R.string.name)+": ${o.name}")
                        Text(stringResource(R.string.distance)+": ${o.distance} m")//TODO: To km if > 1000m
                        Text(stringResource(R.string.time)+": ${o.time} . Lat/Lng: ${o.location.latitude} / ${o.location.longitude}")
                        Text(stringResource(R.string.time_ini)+": ${o.timeIni}")
                        Text(stringResource(R.string.vo2max, o.vo2Max))
                    }
                }
                item { HorizontalDivider() }
                item { HorizontalDivider() }
                item {
                    Text(
                        text = state.response,
                        modifier = Modifier.fillMaxWidth().padding(SepMed)
                    )
                }
            }
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
        PredefinedOptionButton("Mas corta") {
            prompt.value = "¿Cuál es la carrera más corta?"
        }
        PredefinedOptionButton("Mas larga") {
            prompt.value = "¿Cuál es la carrera más larga?"
        }
        PredefinedOptionButton("Nombre") {
            prompt.value = "Encuentra la carrera que se llama "
        }
        PredefinedOptionButton("Vo2Max") {
            prompt.value = "¿Qué carrera tiene el mayor vo2Max?"
        }
        PredefinedOptionButton("Vo2Min") {
            prompt.value = "¿Qué carrera tiene el menor vo2Max?"
        }
        PredefinedOptionButton("Complex A") {
            prompt.value = "Ordena las carreras por mayor Vo2Max y dame las tres primeras, gracias."
        }
        PredefinedOptionButton("Complex B") {
            prompt.value = "Dame las carreras que se llaman 'Canarias'" //( o similar) -> hace que no quiera trabajar ??!!
        }
        PredefinedOptionButton("Geo") {
            prompt.value = "¿Qué carrera está cerca de aquí?"
        }
        PredefinedOptionButton("Geo2") {
            prompt.value = "¿Que carrera con nombre canarias está cerca de la ubicación actual?"
        }
        PredefinedOptionButton("Geo3") {
            prompt.value = "Muestra carreras a menos de 50 metros de la ubicación actual"
        }
    }
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun AIAgentPage_Preview() {
    val state = AIAgentGroqState.Init()
    Surface(modifier = Modifier.fillMaxWidth()) {
        Content(state) { }
    }
}

@Preview
@Composable
private fun AIAgentPage_Loading_Preview() {
    val state = AIAgentGroqState.Init(
        prompt = "Prompt test and bla bla bla",
        response = "Response test and bla bla bla",
        loading = true
    )
    Surface(modifier = Modifier.fillMaxWidth()) {
        Content(state) { }
    }
}
