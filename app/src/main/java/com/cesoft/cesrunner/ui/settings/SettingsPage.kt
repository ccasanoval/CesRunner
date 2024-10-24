package com.cesoft.cesrunner.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.adidas.mvi.compose.MviScreen
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.ui.common.NumberPicker
import com.cesoft.cesrunner.ui.settings.mvi.SettingsIntent
import com.cesoft.cesrunner.ui.settings.mvi.SettingsState
import com.cesoft.cesrunner.ui.theme.SepMax
import com.cesoft.cesrunner.ui.theme.SepMin
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
    ) { view ->
        Content(state = view, reduce = viewModel::execute)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: SettingsState,
    reduce: (intent: SettingsIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    //containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(stringResource(R.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = { reduce(SettingsIntent.Close) }) {
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
            Settings()
        }
    }
}

@Composable
private fun Settings() {
    var period by remember { mutableIntStateOf(5) }
    LazyColumn(
        verticalArrangement = Arrangement.Center,
        //horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = SepMin, horizontal = SepMax)
    ) {
        item {
            NumberPicker(
                modifier = Modifier.width(60.dp),
                min = 1,
                max = 30,
                value = period,
                onSelect = {
                    period = it
                    android.util.Log.e("AAA", "InfiniteNumberPicker------ $it")// it.toInt()
                })
        }
    }
}

@Composable
private fun SettingsNumberPicker() {

}

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
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun SettingsPage_Preview() {
    val navController = rememberNavController()
    Surface { SettingsPage(navController, SettingsViewModel()) }
}