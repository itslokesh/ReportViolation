package com.example.reportviolation.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reportviolation.ui.theme.DarkBlue
import com.example.reportviolation.ui.navigation.Screen

@Composable
fun OtpVerificationScreen(
    navController: NavController,
    name: String = "",
    email: String = "",
    phone: String = "",
    country: String = "91",
    viewModel: OtpVerificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize signup data when the screen is first displayed
    LaunchedEffect(name, email, phone, country) {
        if (name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()) {
            viewModel.initializeSignupData(name, email, phone, country)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Verify OTP",
            style = MaterialTheme.typography.headlineMedium,
            color = DarkBlue,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enter the 6-digit OTP sent to your phone",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "+$country $phone",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // OTP Input
        OutlinedTextField(
            value = uiState.otp,
            onValueChange = { viewModel.updateOtp(it) },
            label = { Text("Enter OTP") },
            placeholder = { Text("000000") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.otpError != null
        )
        
        if (uiState.otpError != null) {
            Text(
                text = uiState.otpError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Verify Button
        Button(
            onClick = { viewModel.verifyOtp() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.otp.length == 6 && !uiState.isLoading,
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
                Text("Verify OTP")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Resend OTP
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Didn't receive OTP? ",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = { viewModel.resendOtp() },
                enabled = !uiState.isLoading && uiState.canResendOtp
            ) {
                Text(
                    "Resend",
                    color = DarkBlue
                )
            }
        }
        
        if (!uiState.canResendOtp && uiState.resendTimer > 0) {
            Text(
                text = "Resend OTP in ${uiState.resendTimer}s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Change Details Button
        OutlinedButton(
            onClick = { navController.navigate(Screen.Signup.route) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = DarkBlue
            )
        ) {
            Text("Change Details")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Back to Login
        TextButton(
            onClick = { navController.navigate(Screen.Login.route) }
        ) {
            Text(
                "Back to Login",
                color = DarkBlue
            )
        }
    }
    
    // Handle navigation to dashboard on successful verification
    LaunchedEffect(uiState.shouldNavigateToDashboard) {
        if (uiState.shouldNavigateToDashboard) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
    
    // Handle resend timer
    LaunchedEffect(uiState.resendTimer) {
        if (uiState.resendTimer > 0) {
            kotlinx.coroutines.delay(1000)
            viewModel.decrementResendTimer()
        }
    }
}
