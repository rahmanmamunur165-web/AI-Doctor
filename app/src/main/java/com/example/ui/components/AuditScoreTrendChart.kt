package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuditScoreTrendPoint
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

/**
 * Interactive Recharts-styled Line & Area Chart for Jetpack Compose.
 * Displays historical SEO audit score trends over time with:
 * - Smooth Monotone Cubic Bezier line interpolation
 * - Gradient area fill beneath the curve
 * - Cartesian grid with dashed horizontal gridlines & Y-axis score intervals
 * - Interactive touch/drag scrubber with vertical reference line and floating Recharts Tooltip
 * - Timeframe filters (30D, 90D, All Time)
 * - Key statistical delta metrics (Start, Current, Net Gain, Audits count)
 */
@Composable
fun AuditScoreTrendChart(
    websiteUrl: String,
    currentScore: Int,
    points: List<AuditScoreTrendPoint>,
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activePointIndex by remember { mutableStateOf<Int?>(null) }

    val startScore = points.firstOrNull()?.score ?: currentScore
    val endScore = points.lastOrNull()?.score ?: currentScore
    val scoreDelta = endScore - startScore
    val avgScore = if (points.isNotEmpty()) points.map { it.score }.average().toInt() else currentScore

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("audit_trend_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFEFF6FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Historical Score Trends",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                    Text(
                        text = "Score progression over time for $websiteUrl",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        modifier = Modifier.padding(top = 4.dp, start = 36.dp)
                    )
                }

                // Delta badge
                Row(
                    modifier = Modifier
                        .background(
                            if (scoreDelta >= 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = if (scoreDelta >= 0) SuccessGreen else Color(0xFFDC2626),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (scoreDelta >= 0) "+$scoreDelta pts" else "$scoreDelta pts",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (scoreDelta >= 0) Color(0xFF166534) else Color(0xFF991B1B)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timeframe Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("30D" to "Last 30 Days", "90D" to "Last 90 Days", "ALL" to "All Time").forEach { (code, label) ->
                        val isSelected = selectedTimeframe == code
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                activePointIndex = null
                                onTimeframeSelected(code)
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF8FAFC),
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) PrimaryBlue else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("audit_trend_timeframe_${code.lowercase()}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Metric Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TrendSummaryItem(label = "Baseline", value = "$startScore / 100")
                TrendSummaryItem(label = "Average", value = "$avgScore / 100")
                TrendSummaryItem(label = "Current", value = "$endScore / 100", isHighlight = true)
                TrendSummaryItem(label = "Gain", value = "+${scoreDelta.coerceAtLeast(0)} pts", isPositive = true)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Tooltip Callout if a point is selected
            val activePoint = activePointIndex?.let { points.getOrNull(it) } ?: points.lastOrNull()
            AnimatedVisibility(
                visible = activePoint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (activePoint != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("audit_trend_tooltip"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(PrimaryBlue, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${activePoint.dateFull.ifEmpty { activePoint.label }}: ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "${activePoint.score}/100",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            )
                            if (activePoint.changeText.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${activePoint.changeText})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (activePoint.changeText.startsWith("+")) SuccessGreen else TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        if (activePoint.notes.isNotEmpty()) {
                            Text(
                                text = activePoint.notes,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Canvas with Recharts Monotone Curve, Cartesian Grid & Tooltip Scrubber
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .testTag("historical_audit_score_chart")
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Y-Axis Labels (100 down to 20)
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = 8.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("100", fontSize = 10.sp, color = TextMuted)
                        Text("75", fontSize = 10.sp, color = TextMuted)
                        Text("50", fontSize = 10.sp, color = TextMuted)
                        Text("25", fontSize = 10.sp, color = TextMuted)
                        Text("0", fontSize = 10.sp, color = TextMuted)
                    }

                    // Canvas Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(points) {
                                    detectTapGestures { offset ->
                                        if (points.isNotEmpty()) {
                                            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                                            val clickedIndex = (offset.x / stepX).toInt().coerceIn(0, points.size - 1)
                                            activePointIndex = clickedIndex
                                        }
                                    }
                                }
                                .pointerInput(points) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            if (points.isNotEmpty()) {
                                                val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                                                val index = (offset.x / stepX).toInt().coerceIn(0, points.size - 1)
                                                activePointIndex = index
                                            }
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            if (points.isNotEmpty()) {
                                                val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                                                val index = (change.position.x / stepX).toInt().coerceIn(0, points.size - 1)
                                                activePointIndex = index
                                            }
                                        }
                                    )
                                }
                        ) {
                            val width = size.width
                            val chartHeight = size.height - 20.dp.toPx() // leave room for x axis labels
                            val baseY = chartHeight

                            // 1. Cartesian Grid Horizontal Lines (Recharts style: light dashed lines)
                            val gridLines = 4
                            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            for (i in 0..gridLines) {
                                val y = (chartHeight / gridLines) * i
                                drawLine(
                                    color = Color(0xFFE2E8F0),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = dashedEffect
                                )
                            }

                            if (points.isNotEmpty()) {
                                val stepX = width / (points.size - 1).coerceAtLeast(1)
                                val maxScore = 100f

                                val computedOffsets = points.mapIndexed { index, pt ->
                                    val x = index * stepX
                                    val normalizedY = (1f - (pt.score.toFloat() / maxScore).coerceIn(0f, 1f)) * chartHeight
                                    Offset(x, normalizedY)
                                }

                                // 2. Gradient Area Fill under Monotone Curve (Recharts Area fill="url(#colorScore)")
                                val areaPath = Path()
                                areaPath.moveTo(computedOffsets.first().x, baseY)
                                areaPath.lineTo(computedOffsets.first().x, computedOffsets.first().y)

                                for (i in 1 until computedOffsets.size) {
                                    val prev = computedOffsets[i - 1]
                                    val curr = computedOffsets[i]
                                    val midX = (prev.x + curr.x) / 2
                                    areaPath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                }
                                areaPath.lineTo(computedOffsets.last().x, baseY)
                                areaPath.close()

                                drawPath(
                                    path = areaPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            PrimaryBlue.copy(alpha = 0.35f),
                                            PrimaryBlue.copy(alpha = 0.08f),
                                            Color.Transparent
                                        ),
                                        startY = 0f,
                                        endY = baseY
                                    ),
                                    style = Fill
                                )

                                // 3. Smooth Monotone Line Stroke (Recharts Line type="monotone" stroke="#2563EB")
                                val linePath = Path()
                                linePath.moveTo(computedOffsets.first().x, computedOffsets.first().y)
                                for (i in 1 until computedOffsets.size) {
                                    val prev = computedOffsets[i - 1]
                                    val curr = computedOffsets[i]
                                    val midX = (prev.x + curr.x) / 2
                                    linePath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                }

                                drawPath(
                                    path = linePath,
                                    color = PrimaryBlue,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // 4. Active Touch Reference Line (Recharts <ReferenceLine strokeDasharray="3 3"/>)
                                val selectedIdx = activePointIndex
                                if (selectedIdx != null && selectedIdx in computedOffsets.indices) {
                                    val activePointOffset = computedOffsets[selectedIdx]
                                    drawLine(
                                        color = Color(0xFF64748B),
                                        start = Offset(activePointOffset.x, 0f),
                                        end = Offset(activePointOffset.x, baseY),
                                        strokeWidth = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                    )
                                }

                                // 5. Data Points Dots (Recharts dot & activeDot)
                                computedOffsets.forEachIndexed { index, pt ->
                                    val isActive = index == selectedIdx || (selectedIdx == null && index == computedOffsets.size - 1)

                                    if (isActive) {
                                        // Outer pulsing halo
                                        drawCircle(
                                            color = PrimaryBlue.copy(alpha = 0.2f),
                                            radius = 10.dp.toPx(),
                                            center = pt
                                        )
                                        // White border ring
                                        drawCircle(
                                            color = Color.White,
                                            radius = 6.dp.toPx(),
                                            center = pt
                                        )
                                        // Inner primary dot
                                        drawCircle(
                                            color = PrimaryBlue,
                                            radius = 4.dp.toPx(),
                                            center = pt
                                        )
                                    } else {
                                        // Standard Recharts dot
                                        drawCircle(
                                            color = Color.White,
                                            radius = 4.dp.toPx(),
                                            center = pt
                                        )
                                        drawCircle(
                                            color = PrimaryBlue,
                                            radius = 2.5.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // X-Axis Date Labels Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEachIndexed { index, pt ->
                    val isSelected = index == activePointIndex || (activePointIndex == null && index == points.size - 1)
                    Text(
                        text = pt.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) PrimaryBlue else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recharts-style Centered Legend & Interaction Tip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(PrimaryBlue, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Overall Audit Score",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(2.dp)
                                .background(Color(0xFFCBD5E1))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Target (85+)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Text(
                    text = "Tap or drag to scrub",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun TrendSummaryItem(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    isPositive: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                isHighlight -> PrimaryBlue
                isPositive -> SuccessGreen
                else -> TextPrimary
            }
        )
    }
}
