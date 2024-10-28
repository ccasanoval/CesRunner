package com.cesoft.cesrunner.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cesoft.cesrunner.domain.AppError

@Composable
fun ErrorCompo(error: AppError) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Error: $error")
    }
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun ErrorCompo_Preview() {
    ErrorCompo(AppError.NetworkError)
}