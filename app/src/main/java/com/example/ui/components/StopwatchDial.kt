package com.example.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.VoltLime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StopwatchDial(
    elapsedTimeMs: Long,
    currentLapTimeMs: Long,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val dialSize = 260.dp

    // Calculate rotation angles
    val secondsAngle = ((elapsedTimeMs % 60000L) / 60000f) * 360f
    val minutesAngle = ((elapsedTimeMs % (30 * 60000L)) / (30 * 60000f)) * 360f
    val lapProgressAngle = ((currentLapTimeMs % 60000L) / 60000f) * 360f

    val glowAlpha by animateFloatAsState(
        targetValue = if (isRunning) 0.8f else 0.25f,
        animationSpec = tween(durationMillis = 400),
        label = "dial_glow"
    )

    Box(
        modifier = modifier
            .size(dialSize)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f - 4.dp.toPx()

            // 1. Draw outer ambient glowing ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        VoltLime.copy(alpha = 0.08f * glowAlpha),
                        DarkSurfaceVariant.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )

            // 2. Base dial background plate
            drawCircle(
                color = DarkSurfaceVariant.copy(alpha = 0.5f),
                radius = outerRadius,
                center = center
            )

            // Outer bezel stroke
            drawCircle(
                color = DarkOutline,
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 3. Lap progress arc (subtle cyan track showing current lap within 60s)
            if (currentLapTimeMs > 0) {
                val lapArcRadius = outerRadius - 10.dp.toPx()
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to ElectricCyan.copy(alpha = 0.2f),
                        1f to ElectricCyan.copy(alpha = 0.85f),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = lapProgressAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - lapArcRadius, center.y - lapArcRadius),
                    size = Size(lapArcRadius * 2, lapArcRadius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. Dial ticks (60 seconds)
            drawDialTicks(center = center, outerRadius = outerRadius)

            // 5. Draw 30-minute chronograph sub-dial (placed at top half of the dial)
            val subDialCenter = Offset(center.x, center.y - outerRadius * 0.42f)
            val subDialRadius = outerRadius * 0.26f
            drawSubDial(subDialCenter, subDialRadius, minutesAngle)

            // 6. Draw main sweep second hand (Volt Lime)
            drawSecondHand(center, outerRadius, secondsAngle, glowAlpha)

            // 7. Center pivot pin
            drawCircle(
                color = VoltLime,
                radius = 6.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color(0xFF0F172A),
                radius = 2.5.dp.toPx(),
                center = center
            )
        }
    }
}

private fun DrawScope.drawDialTicks(center: Offset, outerRadius: Float) {
    val tickOuter = outerRadius - 4.dp.toPx()
    val textPaint = Paint().apply {
        color = android.graphics.Color.argb(200, 148, 163, 184)
        textSize = 9.dp.toPx()
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.MONOSPACE
    }

    for (i in 0 until 60) {
        val angleDeg = i * 6f
        val angleRad = (angleDeg - 90) * (PI / 180.0)
        val isMajor = i % 5 == 0

        val tickLength = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
        val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
        val tickColor = if (isMajor) {
            if (i == 0) VoltLime else Color(0xFFCBD5E1)
        } else {
            Color(0xFF475569)
        }

        val startX = (center.x + (tickOuter - tickLength) * cos(angleRad)).toFloat()
        val startY = (center.y + (tickOuter - tickLength) * sin(angleRad)).toFloat()
        val endX = (center.x + tickOuter * cos(angleRad)).toFloat()
        val endY = (center.y + tickOuter * sin(angleRad)).toFloat()

        drawLine(
            color = tickColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = tickWidth,
            cap = StrokeCap.Round
        )

        // Draw numbers for 00, 15, 30, 45 (or every 5 seconds)
        if (i % 15 == 0) {
            val label = if (i == 0) "60" else i.toString()
            val textRadius = tickOuter - tickLength - 8.dp.toPx()
            val textX = (center.x + textRadius * cos(angleRad)).toFloat()
            val textY = (center.y + textRadius * sin(angleRad) + 3.dp.toPx()).toFloat()
            drawContext.canvas.nativeCanvas.drawText(label, textX, textY, textPaint)
        }
    }
}

private fun DrawScope.drawSubDial(center: Offset, radius: Float, angle: Float) {
    // Sub-dial background
    drawCircle(
        color = Color(0xFF0F172A).copy(alpha = 0.7f),
        radius = radius,
        center = center
    )
    drawCircle(
        color = DarkOutline.copy(alpha = 0.8f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    // Sub-dial ticks (every 5 min for 30 min dial = 6 ticks)
    for (i in 0 until 6) {
        val tickAngle = (i * 60 - 90) * (PI / 180.0)
        val tickOuter = radius - 2.dp.toPx()
        val tickInner = radius - 5.dp.toPx()
        drawLine(
            color = Color(0xFF94A3B8),
            start = Offset(
                (center.x + tickInner * cos(tickAngle)).toFloat(),
                (center.y + tickInner * sin(tickAngle)).toFloat()
            ),
            end = Offset(
                (center.x + tickOuter * cos(tickAngle)).toFloat(),
                (center.y + tickOuter * sin(tickAngle)).toFloat()
            ),
            strokeWidth = 1.2.dp.toPx()
        )
    }

    // Sub-dial hand
    rotate(degrees = angle, pivot = center) {
        drawLine(
            color = ElectricCyan,
            start = center,
            end = Offset(center.x, center.y - (radius - 5.dp.toPx())),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = ElectricCyan,
            radius = 2.5.dp.toPx(),
            center = center
        )
    }
}

private fun DrawScope.drawSecondHand(
    center: Offset,
    outerRadius: Float,
    angle: Float,
    glowAlpha: Float
) {
    val handLength = outerRadius - 12.dp.toPx()
    val counterWeightLength = outerRadius * 0.22f

    rotate(degrees = angle, pivot = center) {
        // Glowing shadow line when running
        if (glowAlpha > 0.3f) {
            drawLine(
                color = VoltLime.copy(alpha = 0.3f * glowAlpha),
                start = Offset(center.x, center.y + counterWeightLength),
                end = Offset(center.x, center.y - handLength),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Main needle
        drawLine(
            color = VoltLime,
            start = Offset(center.x, center.y + counterWeightLength),
            end = Offset(center.x, center.y - handLength),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Arrowhead / accent bulb near the tip
        drawCircle(
            color = VoltLime,
            radius = 4.dp.toPx(),
            center = Offset(center.x, center.y - (handLength - 14.dp.toPx()))
        )

        // Counterweight circle
        drawCircle(
            color = VoltLime,
            radius = 4.5.dp.toPx(),
            center = Offset(center.x, center.y + counterWeightLength * 0.7f)
        )
    }
}
