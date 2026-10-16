package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SeoGaugeChart(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    strokeWidth: Dp = 12.dp
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = (score.coerceIn(0, 100) / 100f),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val scoreColor = when {
        score >= 80 -> SuccessGreen
        score >= 60 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    val gradientBrush = Brush.sweepGradient(
        listOf(
            Color(0xFF10B981),
            Color(0xFF34D399),
            Color(0xFF059669),
            Color(0xFF10B981)
        )
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 8.dp)) {
            val strokePx = strokeWidth.toPx()
            val startAngle = 145f
            val sweepAngle = 250f

            // Background arc
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress arc
            if (animatedProgress.value > 0f) {
                drawArc(
                    brush = gradientBrush,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * animatedProgress.value,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            )
            Text(
                text = "SEO Score",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
