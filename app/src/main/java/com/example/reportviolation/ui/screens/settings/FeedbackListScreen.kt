package com.example.reportviolation.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reportviolation.data.remote.ApiClient
import com.example.reportviolation.data.remote.FeedbackApi
import com.example.reportviolation.data.remote.FeedbackItem
import com.example.reportviolation.data.remote.FeedbackListPage
import com.example.reportviolation.utils.DateTimeUtils
import okhttp3.OkHttpClient
import java.time.Instant
import com.example.reportviolation.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackListScreen(navController: NavController) {
    val api = remember {
        val base = ApiClient.retrofit(OkHttpClient.Builder().build())
        val client = ApiClient.buildClientWithAuthenticator(base.create(com.example.reportviolation.data.remote.AuthApi::class.java))
        ApiClient.retrofit(client).create(FeedbackApi::class.java)
    }

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<FeedbackItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val res = api.listMine()
            if (res.success) {
                val page = res.data
                items = page?.feedback ?: emptyList()
            } else {
                error = res.error ?: res.message ?: "Failed to load feedbacks"
            }
        } catch (e: Exception) {
            error = e.message ?: "Failed to load feedbacks"
        } finally {
            isLoading = false
        }
    }

    val navigateToProfileTab = remember(navController) {
        {
            navController.navigate("${Screen.Dashboard.route}?initialTab=3") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    BackHandler { navigateToProfileTab() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Feedbacks") },
                navigationIcon = {
                    IconButton(onClick = { navigateToProfileTab() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error)
                }
                items.isEmpty() -> {
                    Text(text = "No feedbacks yet")
                }
                else -> {
                    val knownTypes = listOf("APP_FEEDBACK", "REPORT_FEEDBACK", "SERVICE_FEEDBACK", "FEATURE_REQUEST")
                    val grouped = remember(items) {
                        val map = items.groupBy { it.feedbackType ?: "OTHER" }
                        val orderedKeys = buildList {
                            addAll(knownTypes.filter { map.containsKey(it) })
                            addAll(map.keys.filter { it !in knownTypes })
                        }
                        orderedKeys.map { it to (map[it] ?: emptyList()) }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        grouped.forEach { (type, list) ->
                            item(key = "header_$type") {
                                Text(
                                    text = "${friendlyType(type)} (${list.size})",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                            items(list, key = { it.id }) { fb ->
                                Card {
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 120.dp)
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = formattedIstDate(fb.createdAt),
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = fb.description ?: "",
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            item(key = "spacer_$type") { Spacer(Modifier.height(4.dp)) }
                        }
                    }
                }
            }
        }
    }
}

private fun friendlyType(type: String?): String {
    return when (type) {
        "APP_FEEDBACK" -> "App Feedback"
        "REPORT_FEEDBACK" -> "Report Feedback"
        "SERVICE_FEEDBACK" -> "Service Feedback"
        "FEATURE_REQUEST" -> "Feature Request"
        null, "", "OTHER" -> "Other"
        else -> type.split('_').joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.titlecase() } }
    }
}

private fun formattedIstDate(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return try {
        val instant = try {
            Instant.parse(createdAt)
        } catch (_: Exception) {
            try {
                java.time.OffsetDateTime.parse(createdAt).toInstant()
            } catch (_: Exception) {
                java.time.ZonedDateTime.parse(createdAt).toInstant()
            }
        }

        val nowIst = DateTimeUtils.nowZonedIst()
        val timeIst = instant.atZone(DateTimeUtils.IST)
        val duration = java.time.Duration.between(timeIst, nowIst)

        val seconds = duration.seconds
        val minutes = duration.toMinutes()

        when {
            seconds < 60 && seconds >= 0 -> "Just now"
            minutes in 1..59 -> "$minutes mins ago"
            timeIst.toLocalDate().isEqual(nowIst.toLocalDate()) ->
                "Today, " + DateTimeUtils.formatForUi(timeIst.toLocalDateTime(), pattern = "hh:mm a")
            timeIst.toLocalDate().plusDays(1).isEqual(nowIst.toLocalDate()) ->
                "Yesterday, " + DateTimeUtils.formatForUi(timeIst.toLocalDateTime(), pattern = "hh:mm a")
            timeIst.year == nowIst.year ->
                DateTimeUtils.formatForUi(timeIst.toLocalDateTime(), pattern = "dd MMM, hh:mm a")
            else ->
                DateTimeUtils.formatForUi(timeIst.toLocalDateTime(), pattern = "dd MMM yyyy, hh:mm a")
        }
    } catch (_: Exception) {
        ""
    }
}



