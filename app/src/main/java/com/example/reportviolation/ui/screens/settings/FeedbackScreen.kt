package com.example.reportviolation.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reportviolation.ui.theme.DarkBlue
import com.example.reportviolation.data.remote.ApiClient
import com.example.reportviolation.data.remote.FeedbackApi
import com.example.reportviolation.data.remote.FeedbackSubmitRequest
import com.example.reportviolation.ui.navigation.Screen
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(navController: NavController) {
    var selectedTypeLabel by remember { mutableStateOf("App feedback") }
    var backendType by remember { mutableStateOf("APP_FEEDBACK") }
    var message by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    val charLimit = 2000 // Backend limit per spec (10–2000)

    val options: List<Pair<String, String>> = listOf(
        "App feedback" to "APP_FEEDBACK",
        "Report feedback" to "REPORT_FEEDBACK",
        "Service feedback" to "SERVICE_FEEDBACK",
        "Feature request" to "FEATURE_REQUEST",
    )

    val api = remember {
        val base = ApiClient.retrofit(OkHttpClient.Builder().build())
        val client = ApiClient.buildClientWithAuthenticator(base.create(com.example.reportviolation.data.remote.AuthApi::class.java))
        ApiClient.retrofit(client).create(FeedbackApi::class.java)
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    val navigateToProfileTab = remember(navController) {
        {
            navController.navigate("${Screen.Dashboard.route}?initialTab=3") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    BackHandler { navigateToProfileTab() }

    // Root Scaffold owns bottom tabs; keep this screen simple
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = "Send feedback", style = MaterialTheme.typography.titleLarge)
            }

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedTypeLabel,
                    onValueChange = {},
                    label = { Text("Type") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { (label, value) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedTypeLabel = label
                                backendType = value
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = message,
                onValueChange = { input ->
                    val clipped = if (input.length > charLimit) input.take(charLimit) else input
                    message = clipped
                },
                label = { Text("Your feedback") },
                placeholder = { Text("Describe your feedback") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                singleLine = false,
                maxLines = 6
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "${message.length}/$charLimit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            }

            val meetsMinLength = message.trim().length >= 10
            Button(
                onClick = {
                    if (message.isBlank()) {
                        error = "Please enter your feedback"
                        return@Button
                    }
                    error = null
                    isSubmitting = true
                    val body = FeedbackSubmitRequest(
                        feedbackType = backendType,
                        category = "SUGGESTION",
                        title = selectedTypeLabel,
                        description = message,
                        priority = "MEDIUM",
                    )
                    scope.launch {
                        try {
                            keyboardController?.hide()
                            val res = api.submit(body)
                            if (res.success) {
                                message = ""
                                // Show snackbar clearly above keyboard
                                snackbarHostState.showSnackbar(
                                    message = "Feedback submitted successfully",
                                    withDismissAction = false,
                                    duration = SnackbarDuration.Short
                                )
                                // Show a temporary footer message, then navigate to home
                                kotlinx.coroutines.delay(2500)
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                error = res.error ?: res.message ?: "Failed to send feedback"
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "Failed to send feedback"
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting && meetsMinLength && message.length <= charLimit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Submit")
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


