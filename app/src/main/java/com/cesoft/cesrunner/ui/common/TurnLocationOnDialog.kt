package com.cesoft.cesrunner.ui.common

import android.location.LocationManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.getSystemService
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.ui.theme.SepMed


@Composable
fun TurnLocationOnDialog(
    showAlert: MutableState<Boolean>,
    onAccept: () -> Unit
) {
    if(!showAlert.value) return
    val context = LocalContext.current
    val locationManager = context.getSystemService<LocationManager>()
    val isGpsOn = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    if(isGpsOn) return
    AlertDialog(
        onDismissRequest = { showAlert.value = false },
        title = {
            Row {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                    contentDescription = stringResource(R.string.location_off),
                )
                Text(
                    text = stringResource(id = R.string.location_off),
                    modifier = Modifier.padding(horizontal = SepMed)
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.location_off_msg),
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(stringResource(R.string.accept), color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = {showAlert.value = false}) {
                Text(stringResource(R.string.cancel), color = Color.Gray)
            }
        }
    )
}
