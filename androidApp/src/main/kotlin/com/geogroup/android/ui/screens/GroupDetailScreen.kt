package com.geogroup.android.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.geogroup.android.alerts.AlertsListenerService
import com.geogroup.android.data.FirebaseGroupRepository
import com.geogroup.android.geofence.GeofenceHelper
import com.geogroup.shared.model.TrackingGroup
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/**
 * Shows the group's members + its geofence on a map. Also the point where
 * we (a) register the on-device geofence with Play Services and (b) kick
 * off the AlertsListenerService that fans exit events out as notifications
 * to this device.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(navController: NavController, groupId: String) {
    val context = LocalContext.current
    val repo = remember { FirebaseGroupRepository() }
    val geofenceHelper = remember { GeofenceHelper(context) }
    var group by remember { mutableStateOf<TrackingGroup?>(null) }

    LaunchedEffect(groupId) {
        repo.observeGroup(groupId).collect { fetched ->
            group = fetched
            fetched?.geofence?.let { config ->
                geofenceHelper.registerGeofence(groupId, config)
                val serviceIntent = Intent(context, AlertsListenerService::class.java).apply {
                    putExtra(AlertsListenerService.EXTRA_GROUP_ID, groupId)
                    putExtra(AlertsListenerService.EXTRA_GROUP_NAME, fetched.name)
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(group?.name ?: "Group") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            val geofence = group?.geofence
            if (geofence != null) {
                val center = LatLng(geofence.latitude, geofence.longitude)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(center, 15f)
                }
                GoogleMap(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    cameraPositionState = cameraPositionState
                ) {
                    Circle(
                        center = center,
                        radius = geofence.radiusMeters.toDouble(),
                        fillColor = androidx.compose.ui.graphics.Color(0x220000FF),
                        strokeColor = androidx.compose.ui.graphics.Color.Blue
                    )
                    Marker(state = MarkerState(position = center), title = geofence.label)
                }
            } else {
                Column(Modifier.padding(16.dp)) {
                    Text("No geofence set for this group yet.")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { navController.navigate("group/$groupId/geofence") }) {
                        Text("Set geofence")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Members (${group?.memberIds?.size ?: 0})", Modifier.padding(horizontal = 16.dp))
            group?.memberIds?.forEach { uid ->
                Text("• $uid", Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
