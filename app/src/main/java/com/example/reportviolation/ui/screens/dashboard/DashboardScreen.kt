package com.example.reportviolation.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.reportviolation.R
import com.example.reportviolation.ui.navigation.Screen
import com.example.reportviolation.ui.theme.DarkBlue
import com.example.reportviolation.ui.theme.LightBlue
import com.example.reportviolation.ui.theme.LightGray
import com.example.reportviolation.ui.theme.MediumBlue
import com.example.reportviolation.ui.components.ViolationIcon
import com.example.reportviolation.ui.components.ViolationIconDisplayMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController
) {
    // Create ViewModel - in a real app, this would be injected via Hilt or similar DI framework
    val viewModel = remember { DashboardViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.Home, 
                            contentDescription = "Home",
                            tint = if (selectedTab == 0) DarkBlue else Color.Gray
                        ) 
                    },
                    label = { 
                        Text(
                            "Home", 
                            color = if (selectedTab == 0) DarkBlue else Color.Gray
                        ) 
                    },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.Description, 
                            contentDescription = "Reports",
                            tint = if (selectedTab == 1) DarkBlue else Color.Gray
                        ) 
                    },
                    label = { 
                        Text(
                            "Reports", 
                            color = if (selectedTab == 1) DarkBlue else Color.Gray
                        ) 
                    },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.Notifications, 
                            contentDescription = "Notifications",
                            tint = if (selectedTab == 2) DarkBlue else Color.Gray
                        ) 
                    },
                    label = { 
                        Text(
                            "Notifications", 
                            color = if (selectedTab == 2) DarkBlue else Color.Gray
                        ) 
                    },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.Person, 
                            contentDescription = "Profile",
                            tint = if (selectedTab == 3) DarkBlue else Color.Gray
                        ) 
                    },
                    label = { 
                        Text(
                            "Profile", 
                            color = if (selectedTab == 3) DarkBlue else Color.Gray
                        ) 
                    },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeTab(padding, navController, uiState)
            1 -> ReportsTab(padding, navController)
            2 -> NotificationsTab(padding)
            3 -> ProfileTab(padding)
        }
    }
}

@Composable
fun HomeTab(padding: PaddingValues, navController: NavController, uiState: DashboardUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
            .padding(padding)
            .padding(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        // Summary Statistics Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = uiState.totalReports.toString(),
                    label = "Violations\nReported",
                    backgroundColor = DarkBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = uiState.approvedReports.toString(),
                    label = "Resolved",
                    backgroundColor = MediumBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = uiState.pendingReports.toString(),
                    label = "Pending",
                    backgroundColor = LightBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
        
        // Recent Reports Section
        item {
            Text(
                text = "Recent Reports",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Recent Reports List
        if (uiState.recentReports.isNotEmpty()) {
            items(uiState.recentReports) { report ->
                RecentReportItem(report = report, navController = navController)
                if (report != uiState.recentReports.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No reports yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start reporting violations to see them here",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
        
        // Report Violation Button
        item {
            Button(
                onClick = { navController.navigate(Screen.ReportViolation.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue
                ),
                shape = RoundedCornerShape(40.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Report\nViolation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun RecentReportItem(report: RecentReport, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // For demo purposes, navigate to a sample report with ID 1
                navController.navigate("${Screen.ReportDetails.route}/1")
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Violation Icon
            ViolationIcon(
                violationType = report.violationType,
                displayMode = ViolationIconDisplayMode.REPORT_DETAILS
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Violation Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.violationType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            
            // Date
            Text(
                text = report.date,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ReportsTab(padding: PaddingValues, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
            .padding(padding)
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Reports",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Reports History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "View all your submitted violation reports",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate(Screen.ReportsHistory.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Reports")
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsTab(padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
            .padding(padding)
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "No notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You're all caught up!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileTab(padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
            .padding(padding)
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "User Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manage your account settings",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}


