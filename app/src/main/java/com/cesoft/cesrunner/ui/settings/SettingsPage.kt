package com.cesoft.cesrunner.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.ui.common.LoadingCompo
import com.cesoft.cesrunner.ui.common.NumberPicker
import com.cesoft.cesrunner.ui.settings.mvi.SettingsIntent
import com.cesoft.cesrunner.ui.settings.mvi.SettingsState
import com.cesoft.cesrunner.ui.theme.SepMax
import com.cesoft.cesrunner.ui.theme.SepMed
import com.cesoft.cesrunner.ui.theme.SepMin
import com.cesoft.cesrunner.ui.theme.fontBig
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsPage(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    MviScreen(
        state = viewModel.state,
        onSideEffect = { sideEffect ->
            viewModel.consumeSideEffect(
                sideEffect = sideEffect,
                navController = navController
            )
        },
        onBackPressed = {
            viewModel.execute(SettingsIntent.Close)
        },
    ) { state ->
        Content(state = state, reduce = viewModel::execute)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: SettingsState,
    reduce: (intent: SettingsIntent) -> Unit,
) {
    var onClose by remember { mutableStateOf({ reduce(SettingsIntent.Close) }) }
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    //containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(stringResource(R.string.menu_settings))
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
//                actions = {
//                    IconButton(onClick = { /* do something */ }) {
//                        Icon(
//                            imageVector = Icons.Filled.Menu,
//                            contentDescription = "Localized description"
//                        )
//                    }
//                },
            )
        },
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when(state) {
                is SettingsState.Loading -> {
                    reduce(SettingsIntent.Load)
                    LoadingCompo()
                }
                is SettingsState.Init -> {
                    var settings by remember { mutableStateOf(state.settings) }
                    onClose = {
                        reduce(SettingsIntent.Save(settings))
                        //reduce(SettingsIntent.Close)
                    }
                    Settings(state.settings) { settings = it }
                }
            }
        }
    }
}

@Composable
private fun Settings(
    settings: SettingsDto,
    onChange: (SettingsDto) -> Unit
) {
    var period by remember { mutableIntStateOf(settings.minInterval) }
    var distance by remember { mutableIntStateOf(settings.minDistance) }
    var voice by remember { mutableStateOf(settings.voice) }
    LazyColumn(
        //verticalArrangement = Arrangement.Center,
        //horizontalAlignment = Alignment.CenterHorizontally,
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
                    checked = voice,
                    onCheckedChange = {
                        voice = it
                        onChange(settings.copy(voice = it))
                    },
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
                value = period,
                onSelect = {
                    period = it
                    onChange(settings.copy(minInterval = it))
                }
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
                value = distance,
                onSelect = {
                    distance = it
                    onChange(settings.copy(minDistance = it))
                }
            )
            HorizontalDivider()
        }
    }
}
/*
@Composable
private fun ItemInt(
    title: String,
    value: Int,
    onChange: (Int) -> Unit
) {
    Column {
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { onChange(it.toInt()) },
            label = { Text(title) }
        )
    }
}*/

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun Content_Preview() {
    Content(SettingsState.Init(SettingsDto(5, 0, true))) {}
}
//
//@Preview
//@Composable
//private fun SettingsPage_Preview() {
//    val navController = rememberNavController()
//    Surface { SettingsPage(navController, SettingsViewModel()) }
//}