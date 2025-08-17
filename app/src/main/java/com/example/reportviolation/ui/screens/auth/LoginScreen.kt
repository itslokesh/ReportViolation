package com.example.reportviolation.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.reportviolation.ui.navigation.Screen
import kotlinx.coroutines.launch
import com.example.reportviolation.ui.screens.auth.OtpNetworkBridge
import com.example.reportviolation.ui.theme.DarkBlue

@Composable
fun LoginScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("91") }
    var showCountryPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        Text(
            text = "Sign in with phone",
            style = MaterialTheme.typography.headlineMedium,
            color = DarkBlue,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enter your phone number to receive an OTP",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Country code selector matching Create Account CX
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(140.dp)) {
                OutlinedTextField(
                    value = "+$countryCode",
                    onValueChange = {},
                    modifier = Modifier.matchParentSize(),
                    label = { Text("Code", color = Color.Black) },
                    trailingIcon = {
                        IconButton(onClick = { showCountryPicker = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = DarkBlue)
                        }
                    },
                    singleLine = true,
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Black,
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = Color.Black,
                        disabledBorderColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        disabledLabelColor = Color.Black
                    )
                )
                // Ensure the entire field opens the picker on tap
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showCountryPicker = true }
                )
            }
            Spacer(Modifier.width(12.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it.filter { ch -> ch.isDigit() }.take(15) },
                label = { Text("Phone Number") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }
        if (showCountryPicker) {
            com.example.reportviolation.ui.components.CountryPickerDialog(
                onDismiss = { showCountryPicker = false },
                onCountrySelected = { country ->
                    countryCode = country.dialCode
                    showCountryPicker = false
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (phoneNumber.isBlank()) return@Button
                isLoading = true
                val fullPhone = "+${countryCode}-${phoneNumber}"
                scope.launch {
                    val ok = runCatching { OtpNetworkBridge.sendOtp(fullPhone) }.getOrDefault(false)
                    isLoading = false
                    if (ok) {
                        navController.navigate("${Screen.OtpVerification.route}?name=&email=&phone=${phoneNumber}&country=${countryCode}")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && phoneNumber.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Send OTP")
            }
        }
        
        // TODO: Phone number and OTP functionality commented out for testing phase
        /*
        // Phone Number Input (Simplified for testing)
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            placeholder = { Text("Enter your 10-digit phone number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (phoneNumber.length == 10) {
                    isLoading = true
                    // TODO: OTP verification commented out for testing
                    // For testing, directly navigate to dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = phoneNumber.length == 10 && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign In")
            }
        }
        */
        
        // TODO: OTP functionality commented out for testing phase
        /*
        if (!isOtpSent) {
            // Phone Number Input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                placeholder = { Text("Enter your 10-digit phone number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (phoneNumber.length == 10) {
                        isLoading = true
                        // TODO: Send OTP
                        isOtpSent = true
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = phoneNumber.length == 10 && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Send OTP")
                }
            }
        } else {
            // OTP Input
            Text(
                text = "Enter the 6-digit OTP sent to $phoneNumber",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                label = { Text("OTP") },
                placeholder = { Text("Enter 6-digit OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (otp.length == 6) {
                        isLoading = true
                        // TODO: Verify OTP
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = otp.length == 6 && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Verify OTP")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { isOtpSent = false }
            ) {
                Text("Change Phone Number")
            }
        }
        */
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Sign up button removed; registration happens after OTP verify if profile is incomplete
    }
} 