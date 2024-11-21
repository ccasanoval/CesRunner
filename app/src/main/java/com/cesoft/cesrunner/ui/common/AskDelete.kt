package com.cesoft.cesrunner.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.ui.theme.SepMed


@Composable
fun AskDelete(
    show: MutableState<Boolean>,
    onDelete: () -> Unit
) {
    if(!show.value) return
    AlertDialog(
        onDismissRequest = { show.value = false },
        title = {
            Row {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                )
                Text(
                    text = stringResource(R.string.ask_delete_title),
                    modifier = Modifier.padding(horizontal = SepMed)
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.ask_delete_message),
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDelete()
                show.value = false
            }) {
                Text(stringResource(R.string.delete), color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = { show.value = false }) {
                Text(stringResource(R.string.cancel), color = Color.Gray)
            }
        }
    )
}