package com.cesoft.cesrunner.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.cesoft.cesrunner.PageNavigation
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.ui.theme.CesRunnerTheme
import com.cesoft.cesrunner.ui.theme.SepMax
import com.cesoft.cesrunner.ui.theme.fontBigger
import com.cesoft.cesrunner.ui.theme.fontBiggest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val hasPermission = rememberSaveable { mutableStateOf(hasAllPermissions()) }
            LaunchedEffect(hasPermission.value) {
                launch {
                    while( ! hasPermission.value) {
                        delay(5000)
                        hasPermission.value = hasAllPermissions()
                    }
                }
            }
            CesRunnerTheme {
                Scaffold { padding ->
                    Content(hasPermission, padding)
                }
            }
        }
    }

    /// PERMISSIONS ------------------------------------------------------------------------------------
    private fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
    }
    private fun hasAllPermissions(): Boolean {
        val p1 = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val p2 = hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        val p3 = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        return p1 && p2 && p3
    }

    @SuppressLint("BatteryLife")
    private fun askPermissions(): Boolean {
        // ACCESS_FINE_LOCATION and ACCESS_BACKGROUND_LOCATION at the same time doesn't show perm dialog!!!!
        if(!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            result.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            return false
        }
        if(!hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            result.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            return false
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(!hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                result.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                return false
            }
        }
        /// Disable battery optimization : also, check Manifest
        val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Log.e(TAG, "isIgnoringBatteryOptimizations------- FALSE")
            val intent = Intent()
            //intent.setAction(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            intent.setAction(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.setData(Uri.parse("package:${packageName}"))
            startActivity(intent)
        }
        return true
    }

    private val result = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        val denied = result.filter { !it.value }.map { it.key }
        if (denied.isEmpty()) askPermissions()
    }

    /// COMPOSABLE -------------------------------------------------------------------------------------
    @Composable
    private fun Content(hasPermission: MutableState<Boolean>, padding: PaddingValues) {
        Surface(modifier = Modifier.padding(padding)) {
            if(hasPermission.value) {
                PageNavigation()
            }
            else {
                PermissionsAlert {
                    hasPermission.value = askPermissions()
                }
            }
        }
    }

    @Composable
    private fun PermissionsAlert(onClick: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.permissions),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Black,
                fontSize = fontBiggest,
                modifier = Modifier.padding(SepMax)
            )
            Text(
                text = stringResource(R.string.accept_permissions),
                fontSize = fontBigger,
                modifier = Modifier.padding(SepMax)
            )
            Text(
                text = stringResource(R.string.accept_permissions2),
                fontSize = fontBigger,
                modifier = Modifier.padding(SepMax)
            )
            Spacer(Modifier.size(SepMax))
            Button(onClick = onClick, modifier = Modifier.padding(SepMax)) {
                Text(text = stringResource(R.string.accept))
            }
        }
    }

    @Preview
    @Composable
    private fun PermissionsAlert_Preview() {
        Surface(modifier = Modifier.fillMaxSize()) {
            PermissionsAlert { }
        }
    }

    companion object {
        private const val TAG = "MainAct"
    }
}
