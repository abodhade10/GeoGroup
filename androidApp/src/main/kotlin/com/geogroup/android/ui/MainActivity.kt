package com.geogroup.android.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geogroup.android.ui.screens.AuthScreen
import com.geogroup.android.ui.screens.CreateGeofenceScreen
import com.geogroup.android.ui.screens.GroupDetailScreen
import com.geogroup.android.ui.screens.GroupListScreen

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* results handled per-screen via rememberLauncherForActivityResult where needed */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRuntimePermissions()
        setContent {
            MaterialTheme {
                Surface { GeoGroupNavHost() }
            }
        }
    }

    private fun requestRuntimePermissions() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms += Manifest.permission.POST_NOTIFICATIONS
        }
        permissionLauncher.launch(perms.toTypedArray())
        // ACCESS_BACKGROUND_LOCATION must be requested separately, after the
        // foreground grant, per Android 11+ rules -- see README for the flow.
    }
}

@Composable
fun GeoGroupNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") { AuthScreen(navController) }
        composable("groups") { GroupListScreen(navController) }
        composable("group/{groupId}") { backStackEntry ->
            GroupDetailScreen(navController, backStackEntry.arguments?.getString("groupId").orEmpty())
        }
        composable("group/{groupId}/geofence") { backStackEntry ->
            CreateGeofenceScreen(navController, backStackEntry.arguments?.getString("groupId").orEmpty())
        }
    }
}
