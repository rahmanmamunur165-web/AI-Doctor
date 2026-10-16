package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiService
import com.example.data.ReportExporter
import com.example.data.SeoRepository
import com.example.model.AuditCheckItem
import com.example.model.AuditIssue
import com.example.model.AuditResult
import com.example.model.IssueSeverity
import com.example.model.ScreenDestination
import com.example.ui.components.AuditScoreTrendChart
import com.example.ui.components.SeoGaugeChart
import com.example.ui.theme.*
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeoAuditResultsScreen(
    repository: SeoRepository,
    geminiService: GeminiService? = null,
    onNavigate: ((ScreenDestination) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audit by repository.currentAudit.collectAsState()
    val isAuditing by repository.isAuditing.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSeverityFilter by remember { mutableStateOf<IssueSeverity?>(null) }
    var showPassedChecksOnly by remember { mutableStateOf(false) }
    var resolvedIssueIds by remember { mutableStateOf(setOf<String>()) }
    var activeAiExplanation by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isAiGenerating by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var trendTimeframe by remember { mutableStateOf("30D") }

    // Dynamic calculated score accounting for locally resolved issues
    val effectiveScore = remember(audit.overallScore, resolvedIssueIds) {
        (audit.overallScore + resolvedIssueIds.size * 3).coerceAtMost(100)
    }

    val trendPoints = remember(audit.url, effectiveScore, trendTimeframe) {
        repository.getHistoricalAuditTrends(audit.url, effectiveScore, trendTimeframe)
    }

    fun exportAsPdf() {
        try {
            val pdfFile = ReportExporter.exportToPdf(context, audit, effectiveScore)
            ReportExporter.shareFile(context, pdfFile, "application/pdf", "Export SEO Audit PDF Report")
            Toast.makeText(context, "PDF audit report generated successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to generate PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportAsCsv() {
        try {
            val csvContent = repository.exportAuditCsv(audit)
            val csvFile = ReportExporter.exportToCsv(context, audit, csvContent)
            ReportExporter.shareFile(context, csvFile, "text/csv", "Export SEO Audit CSV Data")
            Toast.makeText(context, "CSV audit report exported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Filter issues based on severity and search query
    val displayedIssues = remember(audit.issues, selectedSeverityFilter, searchQuery) {
        audit.issues.filter { issue ->
            val matchesSeverity = selectedSeverityFilter == null || issue.severity == selectedSeverityFilter
            val matchesQuery = searchQuery.isBlank() ||
                    issue.title.contains(searchQuery, ignoreCase = true) ||
                    issue.category.contains(searchQuery, ignoreCase = true) ||
                    issue.description.contains(searchQuery, ignoreCase = true)
            matchesSeverity && matchesQuery
        }
    }

    val criticalIssues = remember(displayedIssues) { displayedIssues.filter { it.severity == IssueSeverity.CRITICAL } }
    val warningIssues = remember(displayedIssues) { displayedIssues.filter { it.severity == IssueSeverity.WARNING } }
    val infoIssues = remember(displayedIssues) { displayedIssues.filter { it.severity == IssueSeverity.INFO } }

    val totalCritical = audit.issues.count { it.severity == IssueSeverity.CRITICAL }
    val totalWarnings = audit.issues.count { it.severity == IssueSeverity.WARNING }
    val totalInfo = audit.issues.count { it.severity == IssueSeverity.INFO }
    val totalPassed = audit.checklist.count { it.isPassed }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ================= 1. COMPREHENSIVE SCORE CARD =================
        item {
            AuditScoreCard(
                audit = audit,
                score = effectiveScore,
                resolvedCount = resolvedIssueIds.size,
                onExportClick = { showExportDialog = true },
                onReAudit = {
                    coroutineScope.launch {
                        repository.runAudit(audit.url, audit.targetKeyword)
                    }
                },
                isAuditing = isAuditing
            )
        }

        // ================= 1.25 HISTORICAL AUDIT SCORE TRENDS (RECHARTS LINE CHART) =================
        item {
            AuditScoreTrendChart(
                websiteUrl = audit.url,
                currentScore = effectiveScore,
                points = trendPoints,
                selectedTimeframe = trendTimeframe,
                onTimeframeSelected = { trendTimeframe = it }
            )
        }

        // ================= 1.5 PROMINENT EXPORT AUDIT REPORT ACTIONS =================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_audit_results_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFEFF6FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Export Audit Results",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Formatted PDF executive report or raw CSV dataset",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { exportAsCsv() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("export_csv_button")
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { exportAsPdf() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("export_pdf_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // ================= 2. IDENTIFIED ISSUES SUMMARY =================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Identified Issues & Recommendations",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Prioritized checklist of on-page, technical, and structural improvements based on deterministic crawl evidence.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // 4 Counter Pills (Critical, Warnings, Opportunities, Passed)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 4
                    ) {
                        SeverityCounterBadge(
                            label = "Critical",
                            count = totalCritical,
                            badgeColor = Color(0xFFFEE2E2),
                            textColor = ErrorRed,
                            isSelected = selectedSeverityFilter == IssueSeverity.CRITICAL && !showPassedChecksOnly,
                            onClick = {
                                showPassedChecksOnly = false
                                selectedSeverityFilter = if (selectedSeverityFilter == IssueSeverity.CRITICAL) null else IssueSeverity.CRITICAL
                            },
                            modifier = Modifier.weight(1f)
                        )

                        SeverityCounterBadge(
                            label = "Warnings",
                            count = totalWarnings,
                            badgeColor = Color(0xFFFEF3C7),
                            textColor = WarningAmber,
                            isSelected = selectedSeverityFilter == IssueSeverity.WARNING && !showPassedChecksOnly,
                            onClick = {
                                showPassedChecksOnly = false
                                selectedSeverityFilter = if (selectedSeverityFilter == IssueSeverity.WARNING) null else IssueSeverity.WARNING
                            },
                            modifier = Modifier.weight(1f)
                        )

                        SeverityCounterBadge(
                            label = "Opportunities",
                            count = totalInfo,
                            badgeColor = Color(0xFFEFF6FF),
                            textColor = PrimaryBlue,
                            isSelected = selectedSeverityFilter == IssueSeverity.INFO && !showPassedChecksOnly,
                            onClick = {
                                showPassedChecksOnly = false
                                selectedSeverityFilter = if (selectedSeverityFilter == IssueSeverity.INFO) null else IssueSeverity.INFO
                            },
                            modifier = Modifier.weight(1f)
                        )

                        SeverityCounterBadge(
                            label = "Passed",
                            count = totalPassed,
                            badgeColor = Color(0xFFECFDF5),
                            textColor = SuccessGreenDark,
                            isSelected = showPassedChecksOnly,
                            onClick = {
                                showPassedChecksOnly = !showPassedChecksOnly
                                selectedSeverityFilter = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search filter field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter issues by keyword (e.g. alt, canonical, schema)...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.FilterList, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("filter_issues_input")
                    )
                }
            }
        }

        // ================= 3. AI EXPLANATION BANNER (IF ACTIVE) =================
        if (activeAiExplanation != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Remediation Assistant",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = AccentPurple)
                                )
                            }
                            IconButton(onClick = { activeAiExplanation = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }

                        Text(
                            text = "Target Issue: ${activeAiExplanation!!.first}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = activeAiExplanation!!.second,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp, color = TextPrimary)
                            )
                        }
                    }
                }
            }
        }

        // ================= 4. PROMINENT SEVERITY FILTERING CHIP ROW =================
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("severity_filtering_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Toggle Severity View",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        if (selectedSeverityFilter != null) {
                            TextButton(
                                onClick = {
                                    selectedSeverityFilter = null
                                    showPassedChecksOnly = false
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("View All", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Chip Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("severity_chip_row"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // All Chip
                        FilterChip(
                            selected = selectedSeverityFilter == null && !showPassedChecksOnly,
                            onClick = {
                                selectedSeverityFilter = null
                                showPassedChecksOnly = false
                            },
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
                                selected = selectedSeverityFilter == null && !showPassedChecksOnly,
                                selectedBorderColor = PrimaryBlue
                            ),
                            modifier = Modifier.testTag("chip_filter_all")
                        )

                        // Critical Chip
                        FilterChip(
                            selected = selectedSeverityFilter == IssueSeverity.CRITICAL,
                            onClick = {
                                showPassedChecksOnly = false
                                selectedSeverityFilter = if (selectedSeverityFilter == IssueSeverity.CRITICAL) null else IssueSeverity.CRITICAL
                            },
                            label = { Text("Critical ($totalCritical)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
                            ),
                            modifier = Modifier.testTag("chip_filter_critical")
                        )

                        // Warning Chip
                        FilterChip(
                            selected = selectedSeverityFilter == IssueSeverity.WARNING,
                            onClick = {
                                showPassedChecksOnly = false
                                selectedSeverityFilter = if (selectedSeverityFilter == IssueSeverity.WARNING) null else IssueSeverity.WARNING
                            },
                            label = { Text("Warning ($totalWarnings)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
                            ),
                            modifier = Modifier.testTag("chip_filter_warning")
                        )

                        // Info Chip
                        FilterChip(
                            selected = selectedSeverityFilter == IssueSeverity.INFO,
                            onClick = {
                                showPassedChecksOnly = false
                                selectedSeverityFilter = if (selectedSeverityFilter == IssueSeverity.INFO) null else IssueSeverity.INFO
                            },
                            label = { Text("Info ($totalInfo)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
                            ),
                            modifier = Modifier.testTag("chip_filter_info")
                        )
                    }
                }
            }
        }

        // ================= 5. RECOMMENDATIONS CATEGORIZED BY SEVERITY =================
        if (!showPassedChecksOnly) {
            // A. CRITICAL PRIORITY SECTION
            if (criticalIssues.isNotEmpty()) {
                item {
                    SeveritySectionHeader(
                        title = "Critical Priority Issues",
                        count = criticalIssues.size,
                        badgeColor = Color(0xFFFEE2E2),
                        textColor = ErrorRed,
                        icon = Icons.Default.Dangerous,
                        subtitle = "Immediate crawl or ranking impairment requiring urgent developer resolution."
                    )
                }

                items(criticalIssues, key = { it.id }) { issue ->
                    RecommendationCard(
                        issue = issue,
                        isResolved = resolvedIssueIds.contains(issue.id),
                        onToggleResolved = {
                            resolvedIssueIds = if (resolvedIssueIds.contains(issue.id)) {
                                resolvedIssueIds - issue.id
                            } else {
                                resolvedIssueIds + issue.id
                            }
                        },
                        onAskAi = {
                            coroutineScope.launch {
                                isAiGenerating = true
                                val response = geminiService?.sendChatMessage(
                                    history = emptyList(),
                                    userMessage = "Explain how to fix this critical SEO issue: '${issue.title}'. Evidence: '${issue.evidence}'. Provide the exact code or server configuration fix."
                                ) ?: "Recommended fix: ${issue.recommendation}\n\nGoogle Search Essentials Mandate: Enforce HTTPS across all assets and ensure canonical link elements use absolute https:// URLs with permanent 301 server redirects."
                                activeAiExplanation = Pair(issue.title, response)
                                isAiGenerating = false
                            }
                        },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("SEO Fix", "${issue.title}\n\n${issue.recommendation}"))
                            Toast.makeText(context, "Copied recommendation!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // B. WARNING PRIORITY SECTION
            if (warningIssues.isNotEmpty()) {
                item {
                    SeveritySectionHeader(
                        title = "High Priority Warnings",
                        count = warningIssues.size,
                        badgeColor = Color(0xFFFEF3C7),
                        textColor = WarningAmber,
                        icon = Icons.Default.Warning,
                        subtitle = "On-page and structural factors that degrade search intent alignment and user experience."
                    )
                }

                items(warningIssues, key = { it.id }) { issue ->
                    RecommendationCard(
                        issue = issue,
                        isResolved = resolvedIssueIds.contains(issue.id),
                        onToggleResolved = {
                            resolvedIssueIds = if (resolvedIssueIds.contains(issue.id)) {
                                resolvedIssueIds - issue.id
                            } else {
                                resolvedIssueIds + issue.id
                            }
                        },
                        onAskAi = {
                            coroutineScope.launch {
                                isAiGenerating = true
                                val response = geminiService?.sendChatMessage(
                                    history = emptyList(),
                                    userMessage = "Provide an optimized solution for: '${issue.title}'. Description: '${issue.description}'. Keep it aligned with Google Helpful Content guidelines."
                                ) ?: "Recommended fix: ${issue.recommendation}\n\nReview introductory paragraphs and subheadings to incorporate topic-related synonyms naturally without repetitive keyword stuffing."
                                activeAiExplanation = Pair(issue.title, response)
                                isAiGenerating = false
                            }
                        },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("SEO Fix", "${issue.title}\n\n${issue.recommendation}"))
                            Toast.makeText(context, "Copied recommendation!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // C. OPPORTUNITIES & INFO SECTION
            if (infoIssues.isNotEmpty()) {
                item {
                    SeveritySectionHeader(
                        title = "Optimization Opportunities",
                        count = infoIssues.size,
                        badgeColor = Color(0xFFEFF6FF),
                        textColor = PrimaryBlue,
                        icon = Icons.Default.Lightbulb,
                        subtitle = "Structured data, rich snippets, and internal linking strategies for higher SERP CTR."
                    )
                }

                items(infoIssues, key = { it.id }) { issue ->
                    RecommendationCard(
                        issue = issue,
                        isResolved = resolvedIssueIds.contains(issue.id),
                        onToggleResolved = {
                            resolvedIssueIds = if (resolvedIssueIds.contains(issue.id)) {
                                resolvedIssueIds - issue.id
                            } else {
                                resolvedIssueIds + issue.id
                            }
                        },
                        onAskAi = {
                            coroutineScope.launch {
                                isAiGenerating = true
                                val response = geminiService?.sendChatMessage(
                                    history = emptyList(),
                                    userMessage = "Generate ready-to-copy JSON-LD schema markup or implementation steps for: '${issue.title}'."
                                ) ?: "Recommended fix: ${issue.recommendation}\n\nAdd Schema.org JSON-LD FAQPage markup to HTML <head> to qualify for rich interactive search results."
                                activeAiExplanation = Pair(issue.title, response)
                                isAiGenerating = false
                            }
                        },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("SEO Fix", "${issue.title}\n\n${issue.recommendation}"))
                            Toast.makeText(context, "Copied recommendation!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            if (displayedIssues.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No issues matching the selected severity filter.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                        )
                    }
                }
            }
        }

        // ================= 6. PASSED CHECKS SECTION =================
        if (showPassedChecksOnly || displayedIssues.isEmpty()) {
            item {
                SeveritySectionHeader(
                    title = "Verified & Passed Checks",
                    count = audit.checklist.size,
                    badgeColor = Color(0xFFDCFCE7),
                    textColor = SuccessGreenDark,
                    icon = Icons.Default.CheckCircle,
                    subtitle = "Elements strictly meeting Google Search Essentials guidelines."
                )
            }

            items(audit.checklist) { item ->
                PassedCheckCard(item = item)
            }
        }
    }

    if (showExportDialog) {
        ExportReportDialog(
            url = audit.url,
            onExportPdf = {
                showExportDialog = false
                exportAsPdf()
            },
            onExportCsv = {
                showExportDialog = false
                exportAsCsv()
            },
            onDismiss = { showExportDialog = false }
        )
    }
}

// ================= COMPONENT: AUDIT SCORE CARD =================
@Composable
private fun AuditScoreCard(
    audit: AuditResult,
    score: Int,
    resolvedCount: Int,
    onExportClick: () -> Unit,
    onReAudit: () -> Unit,
    isAuditing: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Badge & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SEO AUDIT SCORE CARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = PrimaryBlue,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                    if (resolvedCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFDCFCE7), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+${resolvedCount * 3} pts fixed",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreenDark
                                )
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onExportClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFEFF6FF),
                            contentColor = PrimaryBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("export_audit_results_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export (PDF / CSV)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onReAudit, enabled = !isAuditing) {
                        if (isAuditing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Re-crawl", tint = PrimaryBlue)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Score Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeoGaugeChart(score = score, size = 130.dp)

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val statusText = if (score >= 85) "Well Optimized 🎉" else "Needs Improvement ⚠️"
                    val statusColor = if (score >= 85) SuccessGreenDark else ErrorRed

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = audit.url,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        ),
                        maxLines = 1
                    )

                    Text(
                        text = "Keyword: \"${audit.targetKeyword}\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )

                    Text(
                        text = "Response: ${audit.loadTimeMs}ms • HTTPS: ${if (audit.isHttps) "Active" else "Insecure"}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(14.dp))

            // Sub-Scores Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubScoreMetric("Technical", if (audit.isHttps) 96 else 65, Modifier.weight(1f))
                SubScoreMetric("On-Page", 88, Modifier.weight(1f))
                SubScoreMetric("Mobile", 100, Modifier.weight(1f))
                SubScoreMetric("Speed", if (audit.loadTimeMs < 500) 92 else 70, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SubScoreMetric(title: String, score: Int, modifier: Modifier = Modifier) {
    val barColor = when {
        score >= 85 -> SuccessGreen
        score >= 70 -> WarningAmber
        else -> ErrorRed
    }

    Column(
        modifier = modifier
            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = "$score%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(score / 100f)
                    .height(4.dp)
                    .background(barColor, RoundedCornerShape(2.dp))
            )
        }
    }
}

// ================= COMPONENT: SEVERITY COUNTER BADGE =================
@Composable
private fun SeverityCounterBadge(
    label: String,
    count: Int,
    badgeColor: Color,
    textColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderModifier = if (isSelected) Modifier.border(2.dp, textColor, RoundedCornerShape(10.dp)) else Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) badgeColor else Color.White)
            .then(borderModifier)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    fontSize = 16.sp
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) textColor else TextSecondary,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }
    }
}

// ================= COMPONENT: SEVERITY SECTION HEADER =================
@Composable
private fun SeveritySectionHeader(
    title: String,
    count: Int,
    badgeColor: Color,
    textColor: Color,
    icon: ImageVector,
    subtitle: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(badgeColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
            }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// ================= COMPONENT: INDIVIDUAL RECOMMENDATION CARD =================
@Composable
private fun RecommendationCard(
    issue: AuditIssue,
    isResolved: Boolean,
    onToggleResolved: () -> Unit,
    onAskAi: () -> Unit,
    onCopy: () -> Unit
) {
    val stripeColor = when (issue.severity) {
        IssueSeverity.CRITICAL -> ErrorRed
        IssueSeverity.WARNING -> WarningAmber
        IssueSeverity.INFO -> PrimaryBlue
    }

    val badgeBg = when (issue.severity) {
        IssueSeverity.CRITICAL -> Color(0xFFFEE2E2)
        IssueSeverity.WARNING -> Color(0xFFFEF3C7)
        IssueSeverity.INFO -> Color(0xFFEFF6FF)
    }

    val badgeTextColor = when (issue.severity) {
        IssueSeverity.CRITICAL -> ErrorRed
        IssueSeverity.WARNING -> WarningAmber
        IssueSeverity.INFO -> PrimaryBlue
    }

    val badgeLabel = when (issue.severity) {
        IssueSeverity.CRITICAL -> "CRITICAL ISSUE"
        IssueSeverity.WARNING -> "WARNING"
        IssueSeverity.INFO -> "OPPORTUNITY"
    }

    val scoreGain = when (issue.severity) {
        IssueSeverity.CRITICAL -> "+6 pts"
        IssueSeverity.WARNING -> "+4 pts"
        IssueSeverity.INFO -> "+2 pts"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isResolved) Color(0xFFF8FAFC) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isResolved) 1.dp else 1.5.dp,
            color = if (isResolved) Color(0xFFCBD5E1) else stripeColor.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Colored Severity Stripe
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(if (isResolved) SuccessGreenDark else stripeColor)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Header Row: Severity Pill, Category Pill, Potential Gain Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (isResolved) Color(0xFFDCFCE7) else badgeBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isResolved) "RESOLVED" else badgeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isResolved) SuccessGreenDark else badgeTextColor,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = issue.category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF0FDF4), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = scoreGain,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreenDark,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Issue Title
                Text(
                    text = issue.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isResolved) TextMuted else TextPrimary,
                        fontSize = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Issue Description
                Text(
                    text = issue.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                )

                // Evidence Container
                if (issue.evidence.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "DIAGNOSTIC EVIDENCE FOUND",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = issue.evidence,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Recommended Action Solution Container
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreenDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RECOMMENDED ACTION PLAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreenDark,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = issue.recommendation,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons: Ask AI, Copy, Mark as Resolved
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onAskAi,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurple),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Fix", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onCopy,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }

                    // Mark as Resolved Checkbox Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleResolved() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isResolved,
                            onCheckedChange = { onToggleResolved() },
                            colors = CheckboxDefaults.colors(checkedColor = SuccessGreenDark)
                        )
                        Text(
                            text = if (isResolved) "Fixed" else "Mark Fixed",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isResolved) SuccessGreenDark else TextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

// ================= COMPONENT: PASSED CHECK CARD =================
@Composable
private fun PassedCheckCard(item: AuditCheckItem) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreenDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    if (item.details.isNotBlank()) {
                        Text(
                            text = item.details,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "PASSED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreenDark,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

// ================= COMPONENT: EXPORT REPORT DIALOG =================
@Composable
private fun ExportReportDialog(
    url: String,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export SEO Audit Report",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Choose format to export results for $url:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                // Option 1: PDF Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExportPdf() }
                        .testTag("dialog_export_pdf_option")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PDF Document (.pdf)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Executive summary report with score gauge, breakdown, and categorized recommendations.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Option 2: CSV Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExportCsv() }
                        .testTag("dialog_export_csv_option")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CSV Spreadsheet (.csv)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Raw tabular data compatible with Google Sheets, Excel, or analytical tools.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

