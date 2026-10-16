package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SeoRepository
import com.example.model.SearchConsoleQueryItem
import com.example.ui.components.RankingTrendChart
import com.example.ui.theme.*

@Composable
fun RankTrackerScreen(
    repository: SeoRepository,
    modifier: Modifier = Modifier
) {
    val rankingTrend by repository.rankingTrend.collectAsState()
    val searchConsole by repository.searchConsole.collectAsState()

    var showAddKeywordDialog by remember { mutableStateOf(false) }
    var newKeywordText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ranking Chart Card
        item {
            RankingTrendChart(points = rankingTrend)
        }

        // Search Console Integration Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (searchConsole.isConnected) Icons.Default.CheckCircle else Icons.Default.LinkOff,
                                contentDescription = null,
                                tint = if (searchConsole.isConnected) SuccessGreen else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Google Search Console",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = if (searchConsole.isConnected) "Property: ${searchConsole.propertyUrl}" else "Not Connected",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Button(
                            onClick = { repository.toggleSearchConsoleConnection() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (searchConsole.isConnected) Color(0xFFFEE2E2) else PrimaryBlue
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (searchConsole.isConnected) "Disconnect" else "Connect GSC",
                                color = if (searchConsole.isConnected) ErrorRed else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (searchConsole.isConnected) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GscStatPill("Clicks", searchConsole.totalClicks, Modifier.weight(1f))
                            GscStatPill("Impressions", searchConsole.totalImpressions, Modifier.weight(1f))
                            GscStatPill("Avg. CTR", searchConsole.avgCtr, Modifier.weight(1f))
                            GscStatPill("Avg. Position", searchConsole.avgPosition, Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Tracked Queries Header with Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search Performance & Queries",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Button(
                    onClick = { showAddKeywordDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("add_keyword_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Track Keyword", fontSize = 12.sp)
                }
            }
        }

        // Queries List
        items(searchConsole.queries) { q ->
            QueryPerformanceCard(item = q)
        }
    }

    // Add Keyword Dialog
    if (showAddKeywordDialog) {
        AlertDialog(
            onDismissRequest = { showAddKeywordDialog = false },
            title = { Text("Track New Keyword", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Enter a keyword to monitor organic search performance trends:",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newKeywordText,
                        onValueChange = { newKeywordText = it },
                        placeholder = { Text("e.g. ai seo audit tool") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("new_keyword_text_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAddKeywordDialog = false
                        newKeywordText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Add to Tracker")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddKeywordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GscStatPill(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 10.sp, color = TextMuted)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun QueryPerformanceCard(item: SearchConsoleQueryItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.query,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${item.clicks} Clicks",
                        style = MaterialTheme.typography.bodySmall.copy(color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${item.impressions} Impr.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = "${item.ctr}% CTR",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Pos: ${item.position}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                )
            }
        }
    }
}
