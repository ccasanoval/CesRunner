package com.cesoft.cesrunner.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.AppError

@Composable
fun ErrorCompo(error: AppError, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.error)+": $error",
            color = Color.Red,
            fontWeight = FontWeight.Bold,
        )
    }
}

//--------------------------------------------------------------------------------------------------
@Preview
@Composable
private fun ErrorCompo_Preview() {
    ErrorCompo(AppError.NetworkError)
}