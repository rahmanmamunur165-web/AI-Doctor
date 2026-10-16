package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RankTrendPoint
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RankingTrendChart(
    points: List<RankTrendPoint>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Card Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google Ranking Trend",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            // Position indicator badge matching mockup
            Row(
                modifier = Modifier
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Position: 8 ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Y-Axis labels (20 down to 0)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("20", fontSize = 10.sp, color = TextMuted)
                    Text("15", fontSize = 10.sp, color = TextMuted)
                    Text("10", fontSize = 10.sp, color = TextMuted)
                    Text("5", fontSize = 10.sp, color = TextMuted)
                    Text("0", fontSize = 10.sp, color = TextMuted)
                }

                // Grid and trendline canvas
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val width = size.width
                    val height = size.height

                    // Horizontal Grid Lines
                    val lineCount = 4
                    for (i in 0..lineCount) {
                        val y = (height / lineCount) * i
                        drawLine(
                            color = Color(0xFFF1F5F9),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    if (points.isNotEmpty()) {
                        val stepX = width / (points.size - 1).coerceAtLeast(1)
                        val maxVal = 20f
                        val path = Path()
                        val computedPoints = points.mapIndexed { index, pt ->
                            val x = index * stepX
                            val normalizedY = (1f - (pt.position / maxVal).coerceIn(0f, 1f)) * height
                            Offset(x, normalizedY)
                        }

                        // Draw smooth line
                        path.moveTo(computedPoints.first().x, computedPoints.first().y)
                        for (i in 1 until computedPoints.size) {
                            val prev = computedPoints[i - 1]
                            val curr = computedPoints[i]
                            val midX = (prev.x + curr.x) / 2
                            path.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }

                        drawPath(
                            path = path,
                            color = PrimaryBlue,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw dots at data points
                        computedPoints.forEach { pt ->
                            drawCircle(
                                color = Color.White,
                                radius = 5.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = PrimaryBlue,
                                radius = 3.5.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }
        }

        // X-Axis Date Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.label,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend at bottom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(PrimaryBlue, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Your Site",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            )
        }
    }
}
