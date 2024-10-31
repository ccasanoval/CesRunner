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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.cesoft.cesrunner.PageNavigation
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.tracking.TrackingService
import com.cesoft.cesrunner.ui.theme.CesRunnerTheme
import com.cesoft.cesrunner.ui.theme.SepMax

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var hasPermission by rememberSaveable { mutableStateOf(askPermissions()) }
            CesRunnerTheme {
                Scaffold { padding ->
                    Surface(modifier = Modifier.padding(padding)) {
                        if(hasPermission) {
                            PageNavigation()
                        }
                        else {
                            PermissionsAlert {
                                hasPermission = askPermissions()
                                Log.e(TAG, "PermissionsAlert---click--------- $hasPermission")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //askPermissions()
        Log.e(TAG, "onStart------------ ")
    }

    /// PERMISSIONS ------------------------------------------------------------------------------------
    private fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("BatteryLife")
    private fun askPermissions(): Boolean {
        Log.e(TAG, "askPermissions--------- ")

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
        //TODO: No navega a home !!!!!!! salvo cuando pulsas boton !!!!!!!!!!
        /// Disable battery optimization : also, check Manifest
        /*val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Log.e(TAG, "isIgnoringBatteryOptimizations------- FALSE")
            val intent = Intent()
            //intent.setAction(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            intent.setAction(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.setData(Uri.parse("package:${packageName}"))
            startActivity(intent)
        }*/
        return true
    }

    private val result = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        val denied = result.filter { !it.value }.map { it.key }
        if (denied.isEmpty()) askPermissions()
        //if (checkAllPermissions()) permissionContent()
    }

    /// SERVICE ------------------------------------------------------------------------------------
    fun startTrackingService() {
        val intent = Intent(this, TrackingService::class.java)
        startService(intent)
    }

    companion object {
        private const val TAG = "MainAct"
    }
}

/// COMPOSABLE -------------------------------------------------------------------------------------
//private fun permissionContent() { android.util.Log.e("AAA", "-*-----------------permissionContent-") }
@Composable
private fun PermissionsAlert(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.accept_permissions),
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.padding(SepMax)
        )
        Text(
            text = stringResource(R.string.accept_permissions2),
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.padding(SepMax)
        )
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
