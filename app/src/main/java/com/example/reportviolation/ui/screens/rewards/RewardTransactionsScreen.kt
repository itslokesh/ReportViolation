package com.example.reportviolation.ui.screens.rewards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reportviolation.data.remote.RewardTransaction
import com.example.reportviolation.ui.screens.auth.OtpNetworkBridge
import com.example.reportviolation.ui.theme.DarkBlue
import com.example.reportviolation.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardTransactionsScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<RewardTransaction>>(emptyList()) }

    LaunchedEffect(Unit) {
        var page = 1
        val limit = 20
        val collected = mutableListOf<RewardTransaction>()
        runCatching {
            while (true) {
                val res = OtpNetworkBridge.listRewardTransactions(page, limit)
                if (!res.success) {
                    error = res.message ?: res.error
                    break
                }
                val data = res.data ?: break
                collected += data.transactions
                if (data.pagination.page >= data.pagination.pages) break
                page += 1
            }
        }.onFailure { error = it.message }
        items = collected
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reward Transactions") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (!error.isNullOrBlank()) {
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (items.isEmpty()) {
                    Text("No transactions yet")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items, key = { it.id }) { tx ->
                            RewardTransactionItem(tx)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardTransactionItem(tx: RewardTransaction) {
    val color = when (tx.type) {
        "EARN" -> Color(0xFF2E7D32)
        "REDEEM" -> Color(0xFFC62828)
        else -> DarkBlue
    }
    ListItem(
        headlineContent = {
            Text(
                text = tx.description ?: tx.type,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            val ist = runCatching {
                val instant = java.time.Instant.parse(tx.createdAt)
                DateTimeUtils.formatForUi(DateTimeUtils.toIstLocalDateTime(instant), "dd MMM yyyy, hh:mm a")
            }.getOrElse { tx.createdAt }
            Text(ist)
        },
        trailingContent = {
            Text(
                text = (if (tx.type == "REDEEM") "-" else "+") + tx.points,
                color = color,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    )
    Divider()
}


