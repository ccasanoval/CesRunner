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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.cesoft.cesrunner.PageNavigation
import com.cesoft.cesrunner.ui.theme.CesRunnerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CesRunnerTheme {
                Scaffold { padding ->
                    Surface(modifier = Modifier.padding(padding)) {
                        android.util.Log.e("AAA", "MainActivity--------")
                        PageNavigation()
                    }
                }
            }
        }
    }

    /// PERMISSIONS ------------------------------------------------------------------------------------
    private fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun checkAllPermissions(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    @SuppressLint("BatteryLife")
    private fun askPermissions(): Boolean {
        // ACCESS_FINE_LOCATION and ACCESS_BACKGROUND_LOCATION at the same time doesn't show perm dialog!!!!
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            result.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            return false
        }
        if (!hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            result.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            return false
        }
        /// Disable battery optimization : also, check Manifest
        val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            android.util.Log.e("MainAct", "isIgnoringBatteryOptimizations------- FALSE")
            val intent = Intent()
            //intent.setAction(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            intent.setAction(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.setData(Uri.parse("package:${packageName}"))
            startActivity(intent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                result.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                return false
            }
        }
        return true
    }

    private val result = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        val denied = result.filter { !it.value }.map { it.key }
        if (denied.isEmpty()) askPermissions()
        if (checkAllPermissions()) permissionContent()
    }

}

/// COMPOSABLE -------------------------------------------------------------------------------------
private fun permissionContent() {
    android.util.Log.e("AAA", "-*-----------------permissionContent-")
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CesRunnerTheme {
        Greeting("Android")
    }
}
