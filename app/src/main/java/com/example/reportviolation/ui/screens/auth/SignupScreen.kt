package com.example.reportviolation.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reportviolation.ui.navigation.Screen
import com.example.reportviolation.ui.components.CountryPickerDialog
import com.example.reportviolation.ui.theme.DarkBlue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown

@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: SignupViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCountryPicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            color = DarkBlue,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Please provide your details to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Name Input
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("Full Name") },
            placeholder = { Text("Enter your full name") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.nameError != null
        )
        
        if (uiState.nameError != null) {
            Text(
                text = uiState.nameError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Email Input
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email Address") },
            placeholder = { Text("Enter your email address") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.emailError != null
        )
        
        if (uiState.emailError != null) {
            Text(
                text = uiState.emailError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Country and Phone Number Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Country Code Input (styled like OutlinedTextField)
            Surface(
                modifier = Modifier
                    .width(120.dp)
                    .clickable { showCountryPicker = true },
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Code",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "+${uiState.selectedCountry.dialCode}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Country",
                        tint = DarkBlue
                    )
                }
            }
            
            // Phone Number Input
            OutlinedTextField(
                value = uiState.phoneNumber,
                onValueChange = { viewModel.updatePhoneNumber(it) },
                label = { Text("Phone Number") },
                placeholder = { Text("${uiState.selectedCountry.phoneNumberLength} digits") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = uiState.phoneError != null
            )
        }
        
        if (uiState.phoneError != null) {
            Text(
                text = uiState.phoneError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Continue Button
        Button(
            onClick = { viewModel.sendOtp() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isFormValid && !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Continue")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Back to Login
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = { navController.navigate(Screen.Login.route) }
            ) {
                Text(
                    "Sign In",
                    color = DarkBlue
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
    
    // Handle navigation to OTP screen
    LaunchedEffect(uiState.shouldNavigateToOtp) {
        if (uiState.shouldNavigateToOtp) {
            val route = "${Screen.OtpVerification.route}?name=${uiState.name}&email=${uiState.email}&phone=${uiState.phoneNumber}&country=${uiState.selectedCountry.dialCode}"
            navController.navigate(route) {
                popUpTo(Screen.Signup.route) { inclusive = true }
            }
        }
    }
    
    // Country Picker Dialog
    if (showCountryPicker) {
        CountryPickerDialog(
            onDismiss = { showCountryPicker = false },
            onCountrySelected = { country ->
                viewModel.updateSelectedCountry(country)
            }
        )
    }
}
