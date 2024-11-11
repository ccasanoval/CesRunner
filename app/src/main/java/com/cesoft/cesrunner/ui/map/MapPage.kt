package com.cesoft.cesrunner.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.cesoft.cesrunner.R
import org.koin.androidx.compose.koinViewModel


@Composable
fun MapPage(
    navController: NavController,
    viewModel: MapViewModel = koinViewModel(),
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