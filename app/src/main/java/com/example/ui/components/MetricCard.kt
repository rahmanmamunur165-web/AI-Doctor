package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KpiMetric
import com.example.ui.theme.*

@Composable
fun MetricCard(
    metric: KpiMetric,
    modifier: Modifier = Modifier
) {
    val (iconBgColor, iconTintColor, iconVector) = when (metric.iconKey) {
        "search" -> Triple(PrimaryBlue, Color.White, Icons.Default.Search)
        "trophy" -> Triple(SuccessGreen, Color.White, Icons.Default.EmojiEvents)
        "click" -> Triple(AccentPurple, Color.White, Icons.Default.AdsClick)
        else -> Triple(AccentTeal, Color.White, Icons.Default.Visibility)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored Icon Circle
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = metric.title,
                tint = iconTintColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Titles and Values
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = metric.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                ),
                maxLines = 1
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = metric.value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 22.sp
                    )
                )
                if (metric.subtext.isNotBlank()) {
                    Text(
                        text = metric.subtext,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }

        // Percentage Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    if (metric.isPositive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = (if (metric.isPositive) "↑ " else "↓ ") + metric.changeText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (metric.isPositive) SuccessGreenDark else ErrorRed
                )
            )
        }
    }
}
