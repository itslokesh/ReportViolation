package com.example.reportviolation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.reportviolation.ui.screens.auth.LoginScreen
import com.example.reportviolation.ui.screens.auth.RegistrationScreen
import com.example.reportviolation.ui.screens.camera.CameraScreen
import com.example.reportviolation.ui.screens.dashboard.DashboardScreen
import com.example.reportviolation.ui.screens.report.ReportViolationScreen
import com.example.reportviolation.ui.screens.reports.ReportsHistoryScreen
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
        
        composable(Screen.Registration.route) {
            RegistrationScreen(navController)
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        
        composable(Screen.ReportViolation.route) {
            ReportViolationScreen(navController)
        }
        
        composable(Screen.Camera.route) {
            CameraScreen(navController)
        }
        
        composable(Screen.ReportsHistory.route) {
            ReportsHistoryScreen(navController)
        }
        
        composable(Screen.Rewards.route) {
            RewardsScreen(navController)
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Registration : Screen("registration")
    object Dashboard : Screen("dashboard")
    object ReportViolation : Screen("report_violation")
    object Camera : Screen("camera")
    object ReportsHistory : Screen("reports_history")
    object Rewards : Screen("rewards")
} 