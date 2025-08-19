package com.example.reportviolation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.reportviolation.ui.screens.auth.LoginScreen
import com.example.reportviolation.ui.screens.auth.OtpVerificationScreen
import com.example.reportviolation.ui.screens.auth.CompleteProfileScreen
import com.example.reportviolation.ui.screens.camera.CameraScreen
import com.example.reportviolation.ui.screens.dashboard.DashboardScreen
import com.example.reportviolation.ui.screens.map.MapScreen
import com.example.reportviolation.ui.screens.report.ReportViolationScreen
import com.example.reportviolation.ui.screens.reports.ReportDetailsScreen
import com.example.reportviolation.ui.screens.rewards.RewardsScreen
import com.example.reportviolation.ui.screens.rewards.RewardTransactionsScreen
import com.example.reportviolation.ui.screens.splash.SplashScreen
import com.example.reportviolation.ui.screens.settings.FeedbackScreen
import com.example.reportviolation.ui.screens.settings.FeedbackListScreen

@Composable
fun AppNavigation(navController: NavHostController = androidx.navigation.compose.rememberNavController()) {
    var selectedTab by remember { mutableStateOf(0) }
    val backStackEntry = navController.currentBackStackEntryAsState()
    val route = backStackEntry.value?.destination?.route ?: ""
    val effectiveSelectedTab = when {
        route.startsWith(Screen.ReportDetails.route) -> 1
        route.startsWith(Screen.ReportViolation.route) -> 1
        route.startsWith(Screen.Dashboard.route) -> selectedTab
        else -> selectedTab
    }
    val showBottomBar = when {
        route.startsWith(Screen.Splash.route) -> false
        route.startsWith(Screen.Login.route) -> false
        route.startsWith(Screen.OtpVerification.route) -> false
        else -> route.startsWith(Screen.Dashboard.route)
    }
    Scaffold(
        bottomBar = {
            if (showBottomBar) NavigationBar {
                NavigationBarItem(
                    selected = effectiveSelectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navController.navigate("${Screen.Dashboard.route}?initialTab=0") {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = effectiveSelectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate("${Screen.Dashboard.route}?initialTab=1") {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                        }
                    },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Reports") },
                    label = { Text("Reports") }
                )
                NavigationBarItem(
                    selected = effectiveSelectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        navController.navigate("${Screen.Dashboard.route}?initialTab=2") {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                        }
                    },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    label = { Text("Notifications") }
                )
                NavigationBarItem(
                    selected = effectiveSelectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        navController.navigate("${Screen.Dashboard.route}?initialTab=3") {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        
        // Registration route removed; flow is handled by OTP and CompleteProfile
        
        composable(
            route = "${Screen.OtpVerification.route}?name={name}&email={email}&phone={phone}&country={country}",
            arguments = listOf(
                navArgument("name") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("phone") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("country") {
                    type = NavType.StringType
                    defaultValue = "91"
                }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val country = backStackEntry.arguments?.getString("country") ?: "91"
            OtpVerificationScreen(navController, name, email, phone, country)
        }
        
        composable(
            route = "${Screen.Dashboard.route}?initialTab={initialTab}",
            arguments = listOf(
                navArgument("initialTab") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
            LaunchedEffect(initialTab) { selectedTab = initialTab }
            DashboardScreen(navController, initialTab)
        }
        
        composable(Screen.ReportViolation.route) {
            ReportViolationScreen(navController)
        }
        
        composable(
            route = "${Screen.Camera.route}?mode={mode}",
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "PHOTO"
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "PHOTO"
            CameraScreen(navController, mode)
        }
        
        composable(Screen.Map.route) {
            MapScreen(navController)
        }
        
        // ReportsHistory route removed; Reports live under Dashboard -> Reports tab
        composable("complete_profile") {
            CompleteProfileScreen(navController)
        }
        
        composable(
            route = "${Screen.ReportDetails.route}/{reportId}?sourceTab={sourceTab}",
            arguments = listOf(
                navArgument("reportId") {
                    type = NavType.LongType
                },
                navArgument("sourceTab") {
                    type = NavType.StringType
                    defaultValue = "home"
                }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getLong("reportId") ?: 0L
            val sourceTab = backStackEntry.arguments?.getString("sourceTab") ?: "home"
            ReportDetailsScreen(navController, reportId, sourceTab)
        }
        
        composable(Screen.Rewards.route) {
            RewardsScreen(navController)
        }
        composable(Screen.RewardTransactions.route) {
            RewardTransactionsScreen(navController)
        }
        composable(Screen.Feedback.route) {
            FeedbackScreen(navController)
        }
        composable(Screen.FeedbackList.route) {
            FeedbackListScreen(navController)
        }
    }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    // Signup route removed; sign-in flow handles registration completion
    object OtpVerification : Screen("otp_verification")
    object Dashboard : Screen("dashboard")
    object ReportViolation : Screen("report_violation")
    object Camera : Screen("camera")
    object Map : Screen("map")
    object ReportsHistory : Screen("reports_history")
    object ReportDetails : Screen("report_details")
    object Rewards : Screen("rewards")
    object RewardTransactions : Screen("reward_transactions")
    object Feedback : Screen("feedback")
    object FeedbackList : Screen("feedback_list")
} 