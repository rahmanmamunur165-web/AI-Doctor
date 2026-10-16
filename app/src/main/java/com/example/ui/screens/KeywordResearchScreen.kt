package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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
import com.example.model.KeywordSuggestion
import com.example.ui.theme.*

@Composable
fun KeywordResearchScreen(
    repository: SeoRepository,
    modifier: Modifier = Modifier
) {
    val selectedCountry by repository.selectedCountry.collectAsState()
    val initialKeywords by repository.keywordSuggestions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedIntentFilter by remember { mutableStateOf<String?>(null) }
    var userKeywordList by remember { mutableStateOf(initialKeywords) }
    var addedFeedback by remember { mutableStateOf<String?>(null) }

    val displayedKeywords = remember(userKeywordList, searchQuery, selectedIntentFilter) {
        userKeywordList.filter {
            (searchQuery.isBlank() || it.keyword.contains(searchQuery, ignoreCase = true)) &&
            (selectedIntentFilter == null || it.intent.equals(selectedIntentFilter, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Keyword Research & Topic Clustering",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Explore search intent, keyword difficulty, and semantic variations for ${selectedCountry.name} (${selectedCountry.flagEmoji}).",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search or filter keywords...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                Button(
                                    onClick = {
                                        val newKw = KeywordSuggestion(
                                            id = "kw_${System.currentTimeMillis()}",
                                            keyword = searchQuery.trim().lowercase(),
                                            intent = "Commercial",
                                            volumeEst = "2.2K",
                                            difficulty = 34,
                                            cpcEst = "$2.40",
                                            ideasCount = 8
                                        )
                                        userKeywordList = listOf(newKw) + userKeywordList
                                        addedFeedback = "Added '${searchQuery.trim()}' to research list"
                                        searchQuery = ""
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).padding(end = 4.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontSize = 11.sp)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("keyword_search_input")
                    )

                    if (addedFeedback != null) {
                        Text(
                            text = addedFeedback!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SuccessGreenDark,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    // Disclaimer Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Estimated volumes based on historical benchmarks. Connect Google Search Console or authorized API for live data.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                        )
                    }
                }
            }
        }

        // Search Intent Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Intent:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                FilterChip(
                    selected = selectedIntentFilter == null,
                    onClick = { selectedIntentFilter = null },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedIntentFilter == "Informational",
                    onClick = { selectedIntentFilter = "Informational" },
                    label = { Text("Informational") }
                )
                FilterChip(
                    selected = selectedIntentFilter == "Commercial",
                    onClick = { selectedIntentFilter = "Commercial" },
                    label = { Text("Commercial") }
                )
                FilterChip(
                    selected = selectedIntentFilter == "Transactional",
                    onClick = { selectedIntentFilter = "Transactional" },
                    label = { Text("Transactional") }
                )
            }
        }

        // Keyword items list
        items(displayedKeywords) { kw ->
            KeywordItemCard(keyword = kw)
        }
    }
}

@Composable
private fun KeywordItemCard(keyword: KeywordSuggestion) {
    val (intentBg, intentColor) = when (keyword.intent) {
        "Informational" -> Pair(Color(0xFFEFF6FF), PrimaryBlue)
        "Commercial" -> Pair(Color(0xFFEDE9FE), AccentPurple)
        "Transactional" -> Pair(Color(0xFFECFDF5), SuccessGreenDark)
        else -> Pair(Color(0xFFFEF3C7), WarningAmber)
    }

    val (diffColor, diffLabel) = when {
        keyword.difficulty < 30 -> Pair(SuccessGreenDark, "Easy")
        keyword.difficulty < 50 -> Pair(WarningAmber, "Moderate")
        else -> Pair(ErrorRed, "Hard")
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = keyword.keyword,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(intentBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = keyword.intent,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = intentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Est. Vol: ${keyword.volumeEst}/mo",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "Difficulty: ${keyword.difficulty} ($diffLabel)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = diffColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "Est. CPC: ${keyword.cpcEst}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            OutlinedButton(
                onClick = { },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Cluster", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
