package com.example.reportviolation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.reportviolation.data.model.ViolationType
import com.example.reportviolation.utils.getLocalizedViolationTypeName

@Composable
fun ViolationIconDemo() {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Violation Icon Usage Guidelines",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        // Selection Interface Example
        item {
            Text(
                text = "1. Selection Interface (48dp touch target)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Icon Container: 48dp x 48dp touch target\n" +
                       "Background: Rounded square\n" +
                       "Selected State: Blue background (#1976D2)\n" +
                       "Unselected State: Light gray background (#F5F5F5)\n" +
                       "Icon Color: White (selected), Gray (unselected)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Selection interface example
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViolationType.values().take(4).forEach { violationType ->
                    var isSelected by remember { mutableStateOf(false) }
                    
                    ViolationIcon(
                        violationType = violationType,
                        displayMode = ViolationIconDisplayMode.SELECTION,
                        isSelected = isSelected,
                        modifier = Modifier.clickable { isSelected = !isSelected }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Report Details Example
        item {
            Text(
                text = "2. Report Details (32dp icon in 40dp circle)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Icon Display: 32dp icon size\n" +
                       "Container: 40dp circle\n" +
                       "Background: Violation type color\n" +
                       "Position: Leading icon in violation type row",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Report details example
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ViolationType.values().take(4).forEach { violationType ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ViolationIcon(
                            violationType = violationType,
                            displayMode = ViolationIconDisplayMode.REPORT_DETAILS
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = getLocalizedViolationTypeName(violationType, context),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Quick Selection Example
        item {
            Text(
                text = "3. Quick Selection (20dp icon, no background)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Compact Mode: 20dp icon\n" +
                       "No background container\n" +
                       "Color: Primary blue (#1976D2)\n" +
                       "Spacing: 8dp between icons",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Quick selection example
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViolationType.values().forEach { violationType ->
                    ViolationIcon(
                        violationType = violationType,
                        displayMode = ViolationIconDisplayMode.QUICK_SELECTION
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // All Violation Types
        item {
            Text(
                text = "All Available Violation Types",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViolationType.values().forEach { violationType ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ViolationIcon(
                            violationType = violationType,
                            displayMode = ViolationIconDisplayMode.REPORT_DETAILS
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = getLocalizedViolationTypeName(violationType, context),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
