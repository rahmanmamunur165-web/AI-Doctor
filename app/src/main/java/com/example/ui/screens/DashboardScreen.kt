package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SeoRepository
import com.example.model.CountryOption
import com.example.model.ScreenDestination
import com.example.ui.components.MetricCard
import com.example.ui.components.RankingTrendChart
import com.example.ui.components.SeoGaugeChart
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    repository: SeoRepository,
    onNavigate: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val currentAudit by repository.currentAudit.collectAsState()
    val kpis by repository.kpis.collectAsState()
    val keywordSuggestions by repository.keywordSuggestions.collectAsState()
    val rankingTrend by repository.rankingTrend.collectAsState()
    val competitors by repository.competitors.collectAsState()
    val tips by repository.optimizationTips.collectAsState()
    val isAuditing by repository.isAuditing.collectAsState()
    val selectedCountry by repository.selectedCountry.collectAsState()

    var inputUrl by remember { mutableStateOf(currentAudit.url) }
    var inputKeyword by remember { mutableStateOf(currentAudit.targetKeyword) }
    var showCountryPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ================= HERO BANNER =================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFDF2F8), // Soft pinkish
                            Color(0xFFEFF6FF), // Soft blue
                            Color(0xFFDBEAFE)  // Sky blue
                        )
                    )
                )
                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Headline with colored "Google"
                val googleText = buildAnnotatedString {
                    append("Boost Your Website's ")
                    withStyle(SpanStyle(color = Color(0xFF4285F4))) { append("G") }
                    withStyle(SpanStyle(color = Color(0xFFEA4335))) { append("o") }
                    withStyle(SpanStyle(color = Color(0xFFFBBC05))) { append("o") }
                    withStyle(SpanStyle(color = Color(0xFF4285F4))) { append("g") }
                    withStyle(SpanStyle(color = Color(0xFF34A853))) { append("l") }
                    withStyle(SpanStyle(color = Color(0xFFEA4335))) { append("e") }
                    append(" Ranking with AI")
                }

                Text(
                    text = googleText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        lineHeight = 32.sp,
                        color = TextPrimary
                    )
                )

                Text(
                    text = "Analyze. Optimize. Rank Higher.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Input bar: URL input, Keyword input, Country Selector, Analyze Now button
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // URL Input
                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            placeholder = { Text("https://example.com", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(52.dp)
                                .testTag("url_input")
                        )

                        // Target Keyword Input
                        OutlinedTextField(
                            value = inputKeyword,
                            onValueChange = { inputKeyword = it },
                            placeholder = { Text("best digital marketing tools", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("keyword_input")
                        )

                        // Country Selector Pill
                        Box {
                            Row(
                                modifier = Modifier
                                    .height(52.dp)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                    .clickable { showCountryPicker = true }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedCountry.flagEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    selectedCountry.code,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showCountryPicker,
                                onDismissRequest = { showCountryPicker = false }
                            ) {
                                repository.availableCountries.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.flagEmoji} ${c.name} (${c.code})") },
                                        onClick = {
                                            repository.setCountry(c)
                                            showCountryPicker = false
                                        }
                                    )
                                }
                            }
                        }

                        // Analyze Now Button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.runAudit(inputUrl, inputKeyword)
                                }
                            },
                            enabled = !isAuditing,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("analyze_now_button")
                        ) {
                            if (isAuditing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing...", fontWeight = FontWeight.Bold)
                            } else {
                                Text("Analyze Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        // ================= 4 KPI STAT CARDS =================
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2
        ) {
            kpis.forEach { metric ->
                MetricCard(
                    metric = metric,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ================= MID SECTION: 3 KEY CARDS =================
        // 1. SEO Analysis Results (Score gauge + checklist)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "SEO Analysis Results",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Optimized status pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFECFDF5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentAudit.statusMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreenDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Circular Gauge and Checklist row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Score Gauge
                    SeoGaugeChart(
                        score = currentAudit.overallScore,
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    // Checklist
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentAudit.checklist.forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (item.isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (item.isPassed) SuccessGreen else ErrorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // View Detailed Report button
                Button(
                    onClick = { onNavigate(ScreenDestination.SEO_ANALYZER) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("view_detailed_report_button")
                ) {
                    Text("View Detailed Report", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 2. Keyword Suggestions Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Keyword Suggestions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                keywordSuggestions.forEach { kw ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = kw.keyword,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedButton(
                            onClick = { onNavigate(ScreenDestination.KEYWORD_RESEARCH) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryBlue
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("View Ideas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(ScreenDestination.KEYWORD_RESEARCH) }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View All Keywords",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // 3. Google Ranking Trend Chart Card
        RankingTrendChart(points = rankingTrend)

        // ================= BOTTOM SECTION =================
        // Row of Competitors & Tips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2
        ) {
            // Competitor Analysis Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Competitor Analysis",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    competitors.forEach { comp ->
                        val isHighlighted = comp.isUserSite
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isHighlighted) Color(0xFFEFF6FF) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (comp.isUserSite) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    Text(
                                        text = "${comp.rank}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Text(
                                    text = comp.domain,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isHighlighted) PrimaryBlue else TextPrimary
                                    )
                                )
                            }

                            Text(
                                text = "DR ${comp.domainRating}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHighlighted) PrimaryBlue else TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // SEO Optimization Tips Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SEO Optimization Tips",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    tips.forEach { tip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { repository.toggleOptimizationTip(tip.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (tip.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (tip.isCompleted) SuccessGreen else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tip.text,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onNavigate(ScreenDestination.CONTENT_OPTIMIZER) },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    ) {
                        Text("View Full Suggestions →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Actions & Join Pro Plan Row
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2
        ) {
            // Quick Actions Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Generate SEO Content
                    Button(
                        onClick = { onNavigate(ScreenDestination.CONTENT_OPTIMIZER) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F3FF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate SEO Content",
                            color = AccentPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Check Sitemap
                    Button(
                        onClick = { onNavigate(ScreenDestination.SITEMAP_INDEXING) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Check Sitemap",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Request Indexing
                    Button(
                        onClick = { onNavigate(ScreenDestination.SITEMAP_INDEXING) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SuccessGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Request Indexing",
                            color = SuccessGreenDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Join Pro Plan Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFFEF3C7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Crown",
                            tint = WarningAmber,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Join Pro Plan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Get advanced SEO tools, priority support & more!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onNavigate(ScreenDestination.SETTINGS) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text("Upgrade Now →", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
