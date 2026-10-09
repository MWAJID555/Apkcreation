package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IntervalPhase
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FastEmerald
import com.example.ui.theme.SlowAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltLime

@Composable
fun IntervalDial(
    phase: IntervalPhase,
    remainingSeconds: Int,
    currentRound: Int,
    totalRounds: Int,
    progress: Float,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val dialSize = 250.dp

    val phaseColor = when (phase) {
        IntervalPhase.PREPARE -> SlowAmber
        IntervalPhase.WORK -> VoltLime
        IntervalPhase.REST -> ElectricCyan
        IntervalPhase.COMPLETED -> FastEmerald
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "interval_progress"
    )

    Box(
        modifier = modifier.size(dialSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(dialSize)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeWidthPx = 10.dp.toPx()
            val radius = (size.width - strokeWidthPx) / 2f

            // Background circle track
            drawCircle(
                color = DarkSurfaceVariant.copy(alpha = 0.5f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthPx)
            )

            drawCircle(
                color = DarkOutline,
                radius = radius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Progress Arc
            val sweepAngle = animatedProgress * 360f
            if (sweepAngle > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to phaseColor.copy(alpha = 0.6f),
                        1f to phaseColor,
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            // Phase Badge
            val phaseLabel = when (phase) {
                IntervalPhase.PREPARE -> "PREPARE"
                IntervalPhase.WORK -> "WORK"
                IntervalPhase.REST -> "REST"
                IntervalPhase.COMPLETED -> "FINISHED"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(phaseColor.copy(alpha = 0.18f))
                    .border(1.dp, phaseColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = phaseLabel,
                    color = phaseColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large Countdown Digits
            Text(
                text = String.format("%02d", remainingSeconds),
                style = MaterialTheme.typography.displayLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 58.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Round indicator
            Text(
                text = if (phase == IntervalPhase.COMPLETED) "ALL ROUNDS DONE" else "ROUND $currentRound OF $totalRounds",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
