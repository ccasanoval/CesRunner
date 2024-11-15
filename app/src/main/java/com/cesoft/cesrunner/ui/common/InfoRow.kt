package com.cesoft.cesrunner.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cesoft.cesrunner.ui.theme.SepMin
import com.cesoft.cesrunner.ui.theme.fontBig
import com.cesoft.cesrunner.ui.theme.fontMed

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(top = SepMin)) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            fontSize = fontMed,
            modifier = Modifier.weight(0.25f).padding(horizontal = SepMin)
        )
        Text(
            text = value,
            fontSize = fontMed,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.6f)
        )
    }
    HorizontalDivider()
}

@Composable
fun InfoRowBig(label: String, value: String) {
    Row(modifier = Modifier.padding(top = SepMin)) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            fontSize = fontMed,
            modifier = Modifier.weight(0.25f).padding(horizontal = SepMin)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = fontBig,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(0.6f)
        )
    }
    HorizontalDivider()
}