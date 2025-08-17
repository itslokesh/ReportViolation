package com.example.reportviolation.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.reportviolation.ui.theme.DarkBlue
import com.example.reportviolation.ui.navigation.Screen
import com.example.reportviolation.data.remote.ApiClient
import com.example.reportviolation.data.remote.AuthApi
import com.example.reportviolation.data.remote.CitizenRegisterBody
import okhttp3.OkHttpClient

@Composable
fun CompleteProfileScreen(navController: NavController) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val authApi = remember {
        val base = ApiClient.retrofit(OkHttpClient.Builder().build())
        val authClient = ApiClient.buildClientWithAuthenticator(base.create(AuthApi::class.java))
        ApiClient.retrofit(authClient).create(AuthApi::class.java)
    }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Top back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                // Reset tokens and session so user returns to phone entry
                com.example.reportviolation.data.remote.auth.TokenStore.update(null, null)
                runCatching { com.example.reportviolation.data.remote.auth.TokenPrefs.persist(context) }
                com.example.reportviolation.data.remote.auth.SessionPrefs.setProfileComplete(context, false)
                navController.navigate(com.example.reportviolation.ui.navigation.Screen.Login.route) {
                    popUpTo(com.example.reportviolation.ui.navigation.Screen.Login.route) { inclusive = true }
                }
            }) {
                Text("Back")
            }
        }

        Text(
            text = "Complete your profile",
            style = MaterialTheme.typography.headlineMedium,
            color = DarkBlue,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "You're a new user. Please enter your name and email to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank()) {
                    error = "Name and email are required"
                    return@Button
                }
                isLoading = true
                error = null
                // Build payload including phone if we have it stored from last OTP flow
                scope.launch {
                    try {
                        val phone = com.example.reportviolation.data.remote.auth.SessionPrefs.getLastPhone(context) ?: ""
                        val country = com.example.reportviolation.data.remote.auth.SessionPrefs.getLastCountry(context) ?: "91"
                        val fullPhone = if (phone.isNotBlank()) "+${country}-${phone}" else ""
                        val body = CitizenRegisterBody(
                            phoneNumber = fullPhone,
                            name = name,
                            email = email,
                        )
                        println("API_REGISTER_CITIZEN payload=" + com.google.gson.Gson().toJson(body))
                        val res = authApi.registerCitizen(body)
                        println("API_REGISTER_CITIZEN response=" + com.google.gson.Gson().toJson(res))
                        if (res.success) {
                            // Update in-memory tokens if backend rotated them on registration
                            res.data?.let { com.example.reportviolation.data.remote.auth.TokenStore.update(it.token, it.refreshToken) }
                            // Persist tokens now that registration completed
                            runCatching { com.example.reportviolation.data.remote.auth.TokenPrefs.persist(context) }
                            // Mark profile as complete so Splash never skips to home prematurely
                            com.example.reportviolation.data.remote.auth.SessionPrefs.setProfileComplete(context, true)
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            error = res.error ?: res.message ?: "Registration failed"
                        }
                    } catch (e: Exception) {
                        error = e.message ?: "Registration failed"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
            }
            Text("Continue")
        }
    }
}


