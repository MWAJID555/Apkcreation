package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Lap
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.FastEmerald
import com.example.ui.theme.FastEmeraldBg
import com.example.ui.theme.RacingRed
import com.example.ui.theme.SlowAmber
import com.example.ui.theme.SlowAmberBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeFormatter

@Composable
fun LapItem(
    lap: Lap,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        lap.isFastest -> FastEmerald.copy(alpha = 0.5f)
        lap.isSlowest -> SlowAmber.copy(alpha = 0.5f)
        else -> DarkOutlineVariant
    }

    val bgColor = when {
        lap.isFastest -> FastEmeraldBg.copy(alpha = 0.35f)
        lap.isSlowest -> SlowAmberBg.copy(alpha = 0.35f)
        else -> DarkSurfaceVariant.copy(alpha = 0.6f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Column: Lap number + Fastest/Slowest tag
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("LAP %02d", lap.lapNumber),
                    style = MaterialTheme.typography.labelLarge,
                    color = when {
                        lap.isFastest -> FastEmerald
                        lap.isSlowest -> SlowAmber
                        else -> TextSecondary
                    },
                    fontWeight = FontWeight.Bold
                )

                if (lap.isFastest) {
                    Spacer(modifier = Modifier.width(8.dp))
                    LapBadge(
                        text = "FASTEST",
                        textColor = FastEmerald,
                        bgColor = FastEmeraldBg,
                        icon = Icons.Default.ElectricBolt
                    )
                } else if (lap.isSlowest) {
                    Spacer(modifier = Modifier.width(8.dp))
                    LapBadge(
                        text = "SLOWEST",
                        textColor = SlowAmber,
                        bgColor = SlowAmberBg,
                        icon = Icons.Default.HourglassBottom
                    )
                }
            }

            Text(
                text = "Split " + TimeFormatter.formatCompact(lap.splitTimeMs),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Right Column: Lap Time + Delta
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = TimeFormatter.formatCompact(lap.durationMs),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    lap.isFastest -> FastEmerald
                    lap.isSlowest -> SlowAmber
                    else -> TextPrimary
                },
                fontSize = 17.sp
            )

            if (lap.lapNumber > 1) {
                val deltaFormatted = TimeFormatter.formatDelta(lap.diffFromPrevMs)
                val deltaColor = when {
                    lap.diffFromPrevMs < 0 -> FastEmerald // Faster than previous
                    lap.diffFromPrevMs > 0 -> RacingRed // Slower than previous
                    else -> TextMuted
                }
                Text(
                    text = deltaFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = deltaColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun LapBadge(
    text: String,
    textColor: Color,
    bgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(0.8.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.padding(end = 2.dp).width(11.dp)
            )
            Text(
                text = text,
                color = textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
