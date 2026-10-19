package com.cesoft.cesrunner.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.entity.ApiKeysDto
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.NumberPicker
import com.cesoft.cesrunner.ui.common.ToolbarCompo
import com.cesoft.cesrunner.ui.settings.mvi.SettingsIntent
import com.cesoft.cesrunner.ui.settings.mvi.SettingsState
import com.cesoft.cesrunner.ui.theme.SepMax
import com.cesoft.cesrunner.ui.theme.SepMed
import com.cesoft.cesrunner.ui.theme.SepMin
import com.cesoft.cesrunner.ui.theme.fontBig
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsPage(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
) {
    MviScreen(
        state = viewModel.state,
        onSideEffect = { },
        onBackPressed = onBack  // Android back button
    ) { state ->
        Content(state = state, onBack = onBack, reduce = viewModel::execute)
    }
}

@Composable
private fun Content(
    state: SettingsState,
    onBack: () -> Unit = {},
    reduce: (intent: SettingsIntent) -> Unit = {},
) {
    ToolbarCompo(
        title = stringResource(R.string.menu_settings),
        onBack = onBack // UI back icon
    ) {
        when(state) {
            is SettingsState.Loading -> {
                reduce(SettingsIntent.Load)
                LoadingCompo()
            }
            is SettingsState.Init -> {
                Settings(
                    initialSettings = state.settings,
                    initialApiKeys = state.apiKeys,
                    onChange = { reduce(SettingsIntent.Save(it)) },
                    onApiKeysChange = { reduce(SettingsIntent.SaveApiKeys(it)) },
                )
            }
        }
    }
}

@Composable
private fun Settings(
    initialSettings: SettingsDto,
    initialApiKeys: ApiKeysDto,
    onChange: (SettingsDto) -> Unit,
    onApiKeysChange: (ApiKeysDto) -> Unit,
) {
    // Each change builds on the latest values, so changing one field doesn't undo another
    var settings by remember { mutableStateOf(initialSettings) }
    var apiKeys by remember { mutableStateOf(initialApiKeys) }
    fun update(newSettings: SettingsDto) {
        settings = newSettings
        onChange(newSettings)
    }
    fun update(newApiKeys: ApiKeysDto) {
        apiKeys = newApiKeys
        onApiKeysChange(newApiKeys)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = SepMin, horizontal = SepMax)
    ) {
        /// Voice
        item { Spacer(Modifier.size(SepMed)) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.voice_on),
                    fontSize = fontBig,
                    modifier = Modifier.padding(start = SepMin).weight(.5f)
                )
                Switch(
                    checked = settings.voice,
                    onCheckedChange = { update(settings.copy(voice = it)) },
                    modifier = Modifier
                )
            }
            HorizontalDivider()
        }
        /// Min values
        item { Spacer(Modifier.size(SepMed)) }
        item {
            Text(
                text = stringResource(R.string.min_activate_location),
                fontSize = fontBig,
                modifier = Modifier.padding(SepMin)
            )
        }
        /// MinInterval
        item { Spacer(Modifier.size(SepMed)) }
        item {
            NumberPicker(
                title = stringResource(R.string.period),
                subtitle = stringResource(R.string.min),
                modifier = Modifier,
                min = 0,
                max = 15,
                value = settings.minInterval,
                onSelect = { update(settings.copy(minInterval = it)) }
            )
        }
        /// MinDistance
        item { Spacer(Modifier.size(SepMed)) }
        item {
            NumberPicker(
                title = stringResource(R.string.distance),
                subtitle = stringResource(R.string.meters),
                modifier = Modifier.padding(bottom = SepMin),
                min = 0,
                max = 50,
                value = settings.minDistance,
                onSelect = { update(settings.copy(minDistance = it)) }
            )
            HorizontalDivider()
        }
        /// API keys
        item { Spacer(Modifier.size(SepMed)) }
        item {
            Text(
                text = stringResource(R.string.api_keys),
                fontSize = fontBig,
                modifier = Modifier.padding(SepMin)
            )
            Text(
                text = stringResource(R.string.api_keys_info),
                modifier = Modifier.padding(SepMin)
            )
        }
        item {
            ApiKeyField("Groq", apiKeys.groq) { update(apiKeys.copy(groq = it)) }
            ApiKeyField("Gemini", apiKeys.gemini) { update(apiKeys.copy(gemini = it)) }
            ApiKeyField("OpenAI", apiKeys.openAi) { update(apiKeys.copy(openAi = it)) }
            ApiKeyField("OpenRouter", apiKeys.openRouter) { update(apiKeys.copy(openRouter = it)) }
            ApiKeyField("DeepSeek", apiKeys.deepSeek) { update(apiKeys.copy(deepSeek = it)) }
            Spacer(Modifier.size(SepMax))
        }
    }
}

@Composable
private fun ApiKeyField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth().padding(vertical = SepMin)
    )
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun Content_Preview() {
    Content(SettingsState.Init(SettingsDto(5, 0, true)))
}
