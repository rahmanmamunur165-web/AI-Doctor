package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiService
import com.example.data.SeoRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ContentOptimizerScreen(
    repository: SeoRepository,
    geminiService: GeminiService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentAudit by repository.currentAudit.collectAsState()

    var keyword by remember { mutableStateOf(currentAudit.targetKeyword) }
    var url by remember { mutableStateOf(currentAudit.url) }
    var selectedTask by remember { mutableStateOf("title_meta") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf("") }

    val tasks = listOf(
        "title_meta" to "Titles & Meta",
        "headings" to "Heading Hierarchy",
        "faqs" to "FAQ & Schema",
        "content_gaps" to "Content Gaps"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Content & Schema Optimizer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                Text(
                    text = "Generate evidence-based meta tags, semantic headings, and JSON-LD schema adhering to Google Search Essentials.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("Target Keyword") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("content_keyword_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Page URL or Topic") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("content_url_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Task Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tasks.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedTask == key,
                            onClick = { selectedTask = key },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isGenerating = true
                            generatedResult = geminiService.generateContentOptimization(
                                targetKeyword = keyword,
                                url = url,
                                taskType = selectedTask
                            )
                            isGenerating = false
                        }
                    },
                    enabled = !isGenerating && keyword.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_content_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optimizing Content...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Recommendations", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Generated Output Card
        if (generatedResult.isNotBlank() || isGenerating) {
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
                        Text(
                            text = "Generated Optimization Result",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )

                        if (generatedResult.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("SEO Content", generatedResult)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy content",
                                    tint = PrimaryBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isGenerating) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = PrimaryBlue)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Consulting RankBoost AI Models...",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = generatedResult,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextPrimary,
                                    lineHeight = 22.sp,
                                    fontFamily = if (selectedTask == "faqs") FontFamily.Monospace else FontFamily.Default
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Notice: ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                            )
                            Text(
                                text = "Human editorial review is required prior to publication. Avoid keyword stuffing.",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }
                    }
                }
            }
        }
    }
}
