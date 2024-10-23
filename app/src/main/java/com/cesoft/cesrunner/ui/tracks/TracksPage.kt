package com.cesoft.cesrunner.ui.tracks

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.ui.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun TracksPage(
    viewModel: TracksViewModel = koinViewModel(),
) {
    Column(modifier = Modifier) {
        Button(onClick = {}) {
            Text(stringResource(R.string.app_name))
        }
        Button(onClick = {}) {
            Text("RUTAS")
        }
        Button(onClick = {}) {
            Text("GNSS")
        }
    }
}