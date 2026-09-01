package com.geogroup.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** Minimal email/password auth so the demo can show two distinct member accounts. */
@Composable
fun AuthScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    LaunchedEffect(Unit) {
        if (auth.currentUser != null) navController.navigate("groups")
    }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("GeoGroup", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(16.dp))
        Row {
            Button(onClick = {
                scope.launch {
                    runCatching { auth.signInWithEmailAndPassword(email, password).await() }
                        .onSuccess { navController.navigate("groups") }
                        .onFailure { error = it.message }
                }
            }) { Text("Sign in") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = {
                scope.launch {
                    runCatching { auth.createUserWithEmailAndPassword(email, password).await() }
                        .onSuccess { navController.navigate("groups") }
                        .onFailure { error = it.message }
                }
            }) { Text("Sign up") }
        }
    }
}
