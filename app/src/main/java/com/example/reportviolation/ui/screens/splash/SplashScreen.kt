package com.example.reportviolation.ui.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.reportviolation.R
import com.example.reportviolation.ui.navigation.Screen
import kotlinx.coroutines.delay
import com.example.reportviolation.data.remote.auth.TokenPrefs
import com.example.reportviolation.data.remote.auth.TokenStore
import com.example.reportviolation.data.remote.auth.SessionPrefs
import com.example.reportviolation.data.remote.ApiClient
import com.example.reportviolation.data.remote.AuthApi

@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 2000)
    )

    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500L)
        // Initialize token prefs and check session age
        runCatching { TokenPrefs.init(context) }
        val access = TokenStore.accessToken
        val loginAt = SessionPrefs.getLoginAt(context)
        val now = System.currentTimeMillis()
        val within24h = loginAt > 0L && (now - loginAt) <= 24 * 60 * 60 * 1000
        if (!access.isNullOrBlank() && within24h) {
            // Fetch profile to ensure registration completed
            val base = ApiClient.retrofit(okhttp3.OkHttpClient.Builder().build())
            val authClient = ApiClient.buildClientWithAuthenticator(base.create(AuthApi::class.java))
            val authApi = ApiClient.retrofit(authClient).create(AuthApi::class.java)
            val destination = try {
                val res = authApi.getCitizenProfile()
                val p = res.data
                val incomplete = (p == null) || (p.name.isNullOrBlank()) || (p.emailEncrypted.isNullOrBlank())
                // Persist a sticky flag so that closing/reopening app still forces complete_profile until done
                SessionPrefs.setProfileComplete(context, !incomplete)
                if (incomplete) "complete_profile" else Screen.Dashboard.route
            } catch (_: Throwable) {
                // On error, use the persisted profile flag to decide; fallback to Login if uncertain
                if (SessionPrefs.isProfileComplete(context)) Screen.Dashboard.route else Screen.Login.route
            }
            navController.navigate(destination) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            // If token expired but we know profile is incomplete, still go to login (flow will end in complete_profile after OTP)
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .alpha(alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Traffic Violation Reporter",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Making roads safer together",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                fontSize = 16.sp,
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
    }
} 