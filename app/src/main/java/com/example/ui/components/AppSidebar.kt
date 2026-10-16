package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CountryOption
import com.example.model.ScreenDestination
import com.example.ui.theme.NavySidebar
import com.example.ui.theme.NavySidebarActive
import com.example.ui.theme.NavySidebarBorder
import com.example.ui.theme.NavySidebarTextMuted

@Composable
fun AppSidebar(
    currentDestination: ScreenDestination,
    onDestinationSelected: (ScreenDestination) -> Unit,
    selectedCountry: CountryOption,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(NavySidebar)
            .padding(vertical = 20.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section with logo and navigation items
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Logo & Brand Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF06B6D4)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "AI SEO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "Ranking Assistant",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NavySidebarTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Items List
            val navItems = listOf(
                ScreenDestination.DASHBOARD to Icons.Default.Home,
                ScreenDestination.KEYWORD_RESEARCH to Icons.Default.Search,
                ScreenDestination.SEO_ANALYZER to Icons.Default.Analytics,
                ScreenDestination.CONTENT_OPTIMIZER to Icons.Default.EditNote,
                ScreenDestination.RANK_TRACKER to Icons.AutoMirrored.Filled.ShowChart,
                ScreenDestination.SITE_AUDIT to Icons.Default.CheckCircleOutline,
                ScreenDestination.SITEMAP_INDEXING to Icons.Default.AccountTree,
                ScreenDestination.AI_CHATBOT to Icons.Default.SmartToy,
                ScreenDestination.REPORTS to Icons.Default.Assessment,
                ScreenDestination.SETTINGS to Icons.Default.Settings
            )

            navItems.forEach { (dest, icon) ->
                val isSelected = currentDestination == dest
                SidebarNavItem(
                    title = dest.title,
                    icon = icon,
                    isSelected = isSelected,
                    onClick = { onDestinationSelected(dest) }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Bottom section: Targeting indicator & Upgrade to Pro card
        Column(modifier = Modifier.fillMaxWidth()) {
            // Targeting badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = selectedCountry.flagEmoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Targeting:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NavySidebarTextMuted,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = selectedCountry.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = NavySidebarTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Upgrade to Pro Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF162033))
                        )
                    )
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFF59E0B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upgrade to Pro",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Unlock unlimited scans, advanced insights & more.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NavySidebarTextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onUpgradeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    ) {
                        Text(
                            text = "Go Pro",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) NavySidebarActive else Color.Transparent
    val contentColor = if (isSelected) Color.White else NavySidebarTextMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = contentColor,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor,
                fontSize = 13.sp
            )
        )
    }
}
