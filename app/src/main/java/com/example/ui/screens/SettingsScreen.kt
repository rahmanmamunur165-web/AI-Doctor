package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SeoRepository
import com.example.model.PricingPlan
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    repository: SeoRepository,
    modifier: Modifier = Modifier
) {
    val userPlan by repository.userPlan.collectAsState()
    val selectedCountry by repository.selectedCountry.collectAsState()

    val plans = listOf(
        PricingPlan(
            id = "free",
            name = "Starter",
            price = "$0",
            period = "/month",
            description = "For bloggers & personal website owners",
            isPopular = false,
            features = listOf("5 Website Audits / day", "Basic On-Page Checks", "Community Support")
        ),
        PricingPlan(
            id = "pro",
            name = "Pro",
            price = "$49",
            period = "/month",
            description = "For growth marketers & high-ranking SaaS",
            isPopular = true,
            features = listOf(
                "Unlimited Website Scans",
                "Gemini AI Recommendations",
                "Google Search Console Sync",
                "CSV & PDF Export",
                "Rank Tracker (50 Keywords)"
            )
        ),
        PricingPlan(
            id = "agency",
            name = "Agency",
            price = "$199",
            period = "/month",
            description = "For digital agencies managing multiple clients",
            isPopular = false,
            features = listOf(
                "All Pro features included",
                "Unlimited Client Properties",
                "White-label Reports",
                "Dedicated Account Manager",
                "API & Webhook Access"
            )
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JD",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "John Doe",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEDE9FE), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$userPlan Plan",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AccentPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Text(
                            text = "john@example.com • Targeting: ${selectedCountry.name} (${selectedCountry.flagEmoji})",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }
        }

        // Subscription Plans Header
        item {
            Text(
                text = "Subscription & Usage Tiers",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        }

        // Plan Cards
        items(plans.size) { index ->
            val plan = plans[index]
            val isCurrent = userPlan.equals(plan.name, ignoreCase = true)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (plan.isPopular) Color(0xFFF8FAFC) else Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (plan.isPopular) 2.dp else 1.dp,
                    color = if (plan.isPopular) PrimaryBlue else CardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = plan.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            if (plan.isPopular) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryBlue, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "MOST POPULAR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = plan.price,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    fontSize = 22.sp
                                )
                            )
                            Text(
                                text = plan.period,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                    }

                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    plan.features.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SuccessGreenDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { repository.setUserPlan(plan.name) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrent) Color(0xFFE2E8F0) else PrimaryBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text(
                            text = if (isCurrent) "Current Plan" else "Select ${plan.name}",
                            color = if (isCurrent) TextSecondary else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Compliance & Evidence-Based SEO Pledge
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Evidence-Based SEO Policy",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "RankBoost AI strictly complies with Google Search Essentials. We provide verifiable data and deterministic technical checks. We do not promise guaranteed #1 rankings or fabricate search volume.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
