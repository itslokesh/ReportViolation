package com.example.reportviolation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.reportviolation.ui.screens.auth.LoginScreen
import com.example.reportviolation.ui.screens.auth.OtpVerificationScreen
import com.example.reportviolation.ui.screens.auth.CompleteProfileScreen
import com.example.reportviolation.ui.screens.camera.CameraScreen
import com.example.reportviolation.ui.screens.dashboard.DashboardScreen
import com.example.reportviolation.ui.screens.map.MapScreen
import com.example.reportviolation.ui.screens.report.ReportViolationScreen
import com.example.reportviolation.ui.screens.reports.ReportsHistoryScreen
import com.example.reportviolation.ui.screens.reports.ReportDetailsScreen
import com.example.reportviolation.ui.screens.rewards.RewardsScreen
import com.example.reportviolation.ui.screens.splash.SplashScreen

@Composable
fun AppNavigation(navController: NavHostController = androidx.navigation.compose.rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
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
        
        composable(Screen.ReportsHistory.route) {
            ReportsHistoryScreen(navController)
        }
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
} 