package com.geogroup.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.geogroup.android.data.FirebaseGroupRepository
import com.geogroup.shared.model.GeofenceConfig
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

/**
 * Tap on the map to drop the geofence center; radius is adjustable with a
 * slider. Saving writes GeofenceConfig onto the group doc, which triggers
 * every member's GroupDetailScreen listener to register the geofence
 * on-device (see GroupDetailScreen's LaunchedEffect).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGeofenceScreen(navController: NavController, groupId: String) {
    val repo = remember { FirebaseGroupRepository() }
    val scope = rememberCoroutineScope()
    var label by remember { mutableStateOf("Home base") }
    var picked by remember { mutableStateOf<LatLng?>(null) }
    var radius by remember { mutableStateOf(200f) }

    val defaultPosition = LatLng(18.5204, 73.8567) // default center; user taps to move it
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 14f)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Set geofence") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxWidth().weight(1f),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng -> picked = latLng }
            ) {
                picked?.let { center ->
                    Circle(
                        center = center,
                        radius = radius.toDouble(),
                        fillColor = androidx.compose.ui.graphics.Color(0x220000FF),
                        strokeColor = androidx.compose.ui.graphics.Color.Blue
                    )
                    Marker(state = MarkerState(position = center))
                }
            }

            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(label, { label = it }, label = { Text("Region name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("Radius: ${radius.toInt()} m")
                Slider(value = radius, onValueChange = { radius = it }, valueRange = 50f..2000f)
                Spacer(Modifier.height(8.dp))
                Button(
                    enabled = picked != null,
                    onClick = {
                        val center = picked ?: return@Button
                        scope.launch {
                            repo.setGeofence(
                                groupId,
                                GeofenceConfig(label, center.latitude, center.longitude, radius)
                            )
                            navController.popBackStack()
                        }
                    }
                ) { Text("Save geofence") }
            }
        }
    }
}
