package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiService
import com.example.data.SeoRepository
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GeminiChatScreen(
    repository: SeoRepository,
    geminiService: GeminiService,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val chatMessages by repository.chatMessages.collectAsState()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(GeminiService.MODEL_FLASH) }

    val promptSuggestions = listOf(
        "How to optimize for featured snippets?",
        "Write FAQPage JSON-LD schema",
        "How to fix poor Core Web Vitals?",
        "Suggest internal linking structure",
        "Explain canonical tag vs 301 redirect"
    )

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Chat Header with Model Selector & Clear
        Card(
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "RankBoost AI Assistant",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Role: Chief SEO Architect & Search Strategist",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(onClick = { repository.clearChat() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Model Selection Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Model:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                    )

                    FilterChip(
                        selected = selectedModel == GeminiService.MODEL_FLASH,
                        onClick = { selectedModel = GeminiService.MODEL_FLASH },
                        label = { Text("gemini-3.8-flash (General)", fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = selectedModel == GeminiService.MODEL_FLASH_LITE,
                        onClick = { selectedModel = GeminiService.MODEL_FLASH_LITE },
                        label = { Text("gemini-3.1-flash-lite (Fast)", fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = selectedModel == GeminiService.MODEL_PRO,
                        onClick = { selectedModel = GeminiService.MODEL_PRO },
                        label = { Text("gemini-4-argon (Complex)", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages) { message ->
                MessageBubble(message = message)
            }

            if (isSending) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyzing SEO guidelines and drafting recommendation...",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                }
            }
        }

        // Suggested Prompt Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(promptSuggestions) { suggestion ->
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .clickable {
                            inputText = suggestion
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        // Bottom Input Row
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask about technical audits, meta tags, schema...", fontSize = 13.sp) },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                Button(
                    onClick = {
                        val query = inputText.trim()
                        if (query.isNotBlank() && !isSending) {
                            inputText = ""
                            coroutineScope.launch {
                                repository.addChatMessage(MessageSender.USER, query)
                                isSending = true
                                val reply = geminiService.sendChatMessage(
                                    history = chatMessages,
                                    userMessage = query,
                                    modelName = selectedModel
                                )
                                repository.addChatMessage(MessageSender.ASSISTANT, reply)
                                isSending = false
                            }
                        }
                    },
                    enabled = inputText.isNotBlank() && !isSending,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) PrimaryBlue else Color.White)
                .border(
                    width = if (isUser) 0.dp else 1.dp,
                    color = if (isUser) Color.Transparent else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isUser) Color.White else TextPrimary,
                    lineHeight = 20.sp
                )
            )
        }
    }
}
