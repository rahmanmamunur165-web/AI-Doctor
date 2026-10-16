package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SeoRepository
import com.example.model.AuditIssue
import com.example.model.IssueSeverity
import com.example.ui.components.AuditScoreTrendChart
import com.example.ui.components.SeoGaugeChart
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SiteAuditScreen(
    repository: SeoRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audit by repository.currentAudit.collectAsState()
    val isAuditing by repository.isAuditing.collectAsState()

    var urlInput by remember { mutableStateOf(audit.url) }
    var keywordInput by remember { mutableStateOf(audit.targetKeyword) }
    var selectedSeverityFilter by remember { mutableStateOf<IssueSeverity?>(null) }
    var trendTimeframe by remember { mutableStateOf("30D") }

    val trendPoints = remember(audit.url, audit.overallScore, trendTimeframe) {
        repository.getHistoricalAuditTrends(audit.url, audit.overallScore, trendTimeframe)
    }

    val filteredIssues = remember(audit, selectedSeverityFilter) {
        if (selectedSeverityFilter == null) audit.issues
        else audit.issues.filter { it.severity == selectedSeverityFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Audit Controller
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Website SEO Audit & Crawl",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Evidence-based technical inspection with deterministic scoring and SSRF protections.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            label = { Text("Website URL") },
                            leadingIcon = {
                                Icon(Icons.Default.Language, contentDescription = null, tint = PrimaryBlue)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("audit_url_input")
                        )

                        OutlinedTextField(
                            value = keywordInput,
                            onValueChange = { keywordInput = it },
                            label = { Text("Target Keyword") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("audit_kw_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.runAudit(urlInput, keywordInput)
                                }
                            },
                            enabled = !isAuditing,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("run_audit_button")
                        ) {
                            if (isAuditing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Auditing...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Re-Crawl Website", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val csv = repository.exportAuditCsv(audit)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "RankBoost AI Audit - ${audit.url}")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(intent, "Export Audit CSV"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Score & Technical Metrics Card
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SeoGaugeChart(score = audit.overallScore, size = 120.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Audit Score: ${audit.overallScore}/100",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = audit.statusMessage,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (audit.overallScore >= 80) SuccessGreenDark else ErrorRed
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Target: ${audit.url}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                maxLines = 1
                            )
                            Text(
                                text = "Response Time: ${audit.loadTimeMs}ms • HTTPS: ${if (audit.isHttps) "Active ✓" else "Missing ✗"}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Key On-Page Elements Breakdown
                    Text(
                        text = "On-Page Metadata Inspection",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    MetadataRow(label = "Title Tag", value = audit.metaTitle, length = audit.metaTitle.length, max = 60)
                    MetadataRow(label = "Meta Description", value = audit.metaDescription, length = audit.metaDescription.length, max = 160)
                    MetadataRow(label = "H1 Heading", value = audit.h1Heading)
                    MetadataRow(label = "Canonical URL", value = audit.canonicalUrl ?: "Self-referential canonical missing")
                }
            }
        }

        // Historical Audit Score Trend (Recharts Line Chart)
        item {
            AuditScoreTrendChart(
                websiteUrl = audit.url,
                currentScore = audit.overallScore,
                points = trendPoints,
                selectedTimeframe = trendTimeframe,
                onTimeframeSelected = { trendTimeframe = it }
            )
        }

        // Severity Filter Chips
        item {
            val totalCrit = audit.issues.count { it.severity == IssueSeverity.CRITICAL }
            val totalWarn = audit.issues.count { it.severity == IssueSeverity.WARNING }
            val totalInf = audit.issues.count { it.severity == IssueSeverity.INFO }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth().testTag("site_audit_filter_card")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter by Severity (${audit.issues.size} issues)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        if (selectedSeverityFilter != null) {
                            TextButton(
                                onClick = { selectedSeverityFilter = null },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Reset", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedSeverityFilter == null,
                            onClick = { selectedSeverityFilter = null },
                            label = { Text("All (${audit.issues.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEFF6FF),
                                selectedLabelColor = PrimaryBlue,
                                selectedLeadingIconColor = PrimaryBlue
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedSeverityFilter == null,
                                selectedBorderColor = PrimaryBlue
                            )
                        )

                        FilterChip(
                            selected = selectedSeverityFilter == IssueSeverity.CRITICAL,
                            onClick = {
                                selectedSeverityFilter = if (selectedSeverityFilter == IssueSeverity.CRITICAL) null else IssueSeverity.CRITICAL
                            },
                            label = { Text("Critical ($totalCrit)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Default.Dangerous, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEE2E2),
                                selectedLabelColor = ErrorRed,
                                selectedLeadingIconColor = ErrorRed
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedSeverityFilter == IssueSeverity.CRITICAL,
                                selectedBorderColor = ErrorRed
                            )
                        )

                        FilterChip(
                            selected = selectedSeverityFilter == IssueSeverity.WARNING,
                            onClick = {
                                selectedSeverityFilter = if (selectedSeverityFilter == IssueSeverity.WARNING) null else IssueSeverity.WARNING
                            },
                            label = { Text("Warning ($totalWarn)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEF3C7),
                                selectedLabelColor = WarningAmber,
                                selectedLeadingIconColor = WarningAmber
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedSeverityFilter == IssueSeverity.WARNING,
                                selectedBorderColor = WarningAmber
                            )
                        )

                        FilterChip(
                            selected = selectedSeverityFilter == IssueSeverity.INFO,
                            onClick = {
                                selectedSeverityFilter = if (selectedSeverityFilter == IssueSeverity.INFO) null else IssueSeverity.INFO
                            },
                            label = { Text("Info ($totalInf)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEFF6FF),
                                selectedLabelColor = PrimaryBlue,
                                selectedLeadingIconColor = PrimaryBlue
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedSeverityFilter == IssueSeverity.INFO,
                                selectedBorderColor = PrimaryBlue
                            )
                        )
                    }
                }
            }
        }

        // Issues List
        items(filteredIssues) { issue ->
            AuditIssueCard(issue = issue)
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String,
    length: Int? = null,
    max: Int? = null
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            )
            if (length != null && max != null) {
                Text(
                    text = "$length / $max chars",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (length in (max - 20)..max) SuccessGreenDark else TextMuted
                    )
                )
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                .padding(8.dp)
        )
    }
}

@Composable
private fun AuditIssueCard(issue: AuditIssue) {
    val (badgeBg, badgeText, badgeLabel) = when (issue.severity) {
        IssueSeverity.CRITICAL -> Triple(Color(0xFFFEE2E2), ErrorRed, "CRITICAL")
        IssueSeverity.WARNING -> Triple(Color(0xFFFEF3C7), WarningAmber, "WARNING")
        IssueSeverity.INFO -> Triple(Color(0xFFEFF6FF), PrimaryBlue, "RECOMMENDATION")
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = badgeText
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = issue.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    )
                }

                if (issue.requiresHumanReview) {
                    Text(
                        text = "Human Review Req.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentPurple,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = issue.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Text(
                text = issue.description,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Evidence Box
            if (issue.evidence.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Evidence: ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    )
                    Text(
                        text = issue.evidence,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recommendation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0FDF4), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SuccessGreenDark,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Recommended Action:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreenDark
                        )
                    )
                    Text(
                        text = issue.recommendation,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}
