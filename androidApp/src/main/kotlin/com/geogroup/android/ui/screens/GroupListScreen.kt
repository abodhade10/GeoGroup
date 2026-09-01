package com.geogroup.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.geogroup.android.data.FirebaseGroupRepository
import com.geogroup.shared.model.TrackingGroup
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(navController: NavController) {
    val repo = remember { FirebaseGroupRepository() }
    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    var groups by remember { mutableStateOf<List<TrackingGroup>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        repo.observeMyGroups(uid).collect { groups = it }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Your groups") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Create group")
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            items(groups) { group ->
                ListItem(
                    headlineContent = { Text(group.name) },
                    supportingContent = {
                        val geofence = group.geofence
                        Text(
                            if (geofence != null) "Geofence: ${geofence.label}"
                            else "No geofence set yet"
                        )
                    },
                    modifier = Modifier.clickable { navController.navigate("group/${group.id}") }
                )
                HorizontalDivider()
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create a tracking group") },
            text = {
                OutlinedTextField(newGroupName, { newGroupName = it }, label = { Text("Group name") })
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val group = repo.createGroup(newGroupName, uid)
                        showCreateDialog = false
                        navController.navigate("group/${group.id}")
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
        )
    }
}
