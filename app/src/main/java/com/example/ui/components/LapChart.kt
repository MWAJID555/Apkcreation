package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Lap
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FastEmerald
import com.example.ui.theme.SlowAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltLime
import com.example.util.TimeFormatter

@Composable
fun LapChart(
    laps: List<Lap>,
    modifier: Modifier = Modifier
) {
    if (laps.size < 2) return

    // Order laps chronologically from lap 1 upwards for charting
    val chronologicalLaps = laps.sortedBy { it.lapNumber }
    val maxDuration = chronologicalLaps.maxOf { it.durationMs }.toFloat()
    val minDuration = chronologicalLaps.minOf { it.durationMs }.toFloat()
    val avgDuration = chronologicalLaps.map { it.durationMs }.average().toFloat()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, DarkOutlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LAP PACE DISTRIBUTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Avg: " + TimeFormatter.formatCompact(avgDuration.toLong()),
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            // Canvas Bar Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .padding(top = 10.dp, bottom = 4.dp)
            ) {
                val totalBars = chronologicalLaps.size
                val barSpacing = 4.dp.toPx()
                val availableWidth = size.width - (barSpacing * (totalBars - 1))
                val barWidth = (availableWidth / totalBars).coerceIn(4.dp.toPx(), 28.dp.toPx())

                val chartHeight = size.height - 12.dp.toPx()
                val baselineY = size.height

                // Draw Average Line
                if (maxDuration > 0f) {
                    val avgFraction = (avgDuration / maxDuration).coerceIn(0.15f, 1f)
                    val avgY = baselineY - (chartHeight * avgFraction)
                    drawLine(
                        color = ElectricCyan.copy(alpha = 0.4f),
                        start = Offset(0f, avgY),
                        end = Offset(size.width, avgY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                // Draw Bars
                chronologicalLaps.forEachIndexed { index, lap ->
                    val fraction = if (maxDuration > 0f) {
                        (lap.durationMs / maxDuration).coerceIn(0.2f, 1f)
                    } else 0.5f

                    val barH = chartHeight * fraction
                    val startX = index * (barWidth + barSpacing)
                    val startY = baselineY - barH

                    val barColor = when {
                        lap.isFastest -> FastEmerald
                        lap.isSlowest -> SlowAmber
                        else -> VoltLime.copy(alpha = 0.85f)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(startX, startY),
                        size = Size(barWidth, barH),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
            }

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Lap 1",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "Lap ${chronologicalLaps.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
