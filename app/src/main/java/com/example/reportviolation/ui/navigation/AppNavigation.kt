package com.example.reportviolation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.reportviolation.ui.screens.auth.LoginScreen
import com.example.reportviolation.ui.screens.auth.RegistrationScreen
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
        
        composable(Screen.Registration.route) {
            RegistrationScreen(navController)
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
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
        
        composable(
            route = "${Screen.ReportDetails.route}/{reportId}",
            arguments = listOf(
                navArgument("reportId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getLong("reportId") ?: 0L
            ReportDetailsScreen(navController, reportId)
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
    object Map : Screen("map")
    object ReportsHistory : Screen("reports_history")
    object ReportDetails : Screen("report_details")
    object Rewards : Screen("rewards")
} 