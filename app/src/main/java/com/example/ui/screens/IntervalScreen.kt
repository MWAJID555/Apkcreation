package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IntervalPreset
import com.example.ui.components.IntervalDial
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.OnVoltLime
import com.example.ui.theme.RacingRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltLime
import com.example.util.TimeFormatter
import com.example.viewmodel.IntervalUiState

@Composable
fun IntervalScreen(
    state: IntervalUiState,
    presets: List<IntervalPreset>,
    onSelectPreset: (IntervalPreset) -> Unit,
    onUpdateCustom: (workSec: Int, restSec: Int, rounds: Int) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 110.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Preset Chips Carousel
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(presets) { preset ->
                    val isSelected = state.workDurationSec == preset.workSec &&
                            state.restDurationSec == preset.restSec &&
                            state.totalRounds == preset.rounds

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) VoltLime.copy(alpha = 0.15f) else DarkSurfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) VoltLime else DarkOutline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = !state.isRunning) { onSelectPreset(preset) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) VoltLime else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${preset.workSec}s / ${preset.restSec}s · ${preset.rounds}R",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Circular Progress Dial
            IntervalDial(
                phase = state.phase,
                remainingSeconds = state.remainingSecondsInPhase,
                currentRound = state.currentRound,
                totalRounds = state.totalRounds,
                progress = state.progress,
                isRunning = state.isRunning,
                modifier = Modifier.testTag("interval_dial")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Workout Total Elapsed & Total Duration Info
            val totalWorkoutSec = (state.workDurationSec + state.restDurationSec) * state.totalRounds + state.prepareDurationSec
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.6f))
                    .border(1.dp, DarkOutlineVariant, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ELAPSED", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        TimeFormatter.formatCompact(state.totalElapsedSec * 1000L).substringBefore("."),
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(DarkOutline)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL WORKOUT", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        TimeFormatter.formatCompact(totalWorkoutSec * 1000L).substringBefore("."),
                        color = ElectricCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Custom Duration Steppers (Disabled while running)
            if (!state.isRunning && !state.isActive) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant.copy(alpha = 0.4f))
                        .border(1.dp, DarkOutlineVariant, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "CUSTOM INTERVAL SETUP",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Work time row
                    IntervalStepperRow(
                        label = "Work Interval",
                        valueText = "${state.workDurationSec}s",
                        accentColor = VoltLime,
                        onDecrement = {
                            if (state.workDurationSec > 5) {
                                onUpdateCustom(state.workDurationSec - 5, state.restDurationSec, state.totalRounds)
                            }
                        },
                        onIncrement = {
                            onUpdateCustom(state.workDurationSec + 5, state.restDurationSec, state.totalRounds)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rest time row
                    IntervalStepperRow(
                        label = "Rest Interval",
                        valueText = "${state.restDurationSec}s",
                        accentColor = ElectricCyan,
                        onDecrement = {
                            if (state.restDurationSec >= 5) {
                                onUpdateCustom(state.workDurationSec, state.restDurationSec - 5, state.totalRounds)
                            }
                        },
                        onIncrement = {
                            onUpdateCustom(state.workDurationSec, state.restDurationSec + 5, state.totalRounds)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rounds row
                    IntervalStepperRow(
                        label = "Total Rounds",
                        valueText = "${state.totalRounds}",
                        accentColor = TextPrimary,
                        onDecrement = {
                            if (state.totalRounds > 1) {
                                onUpdateCustom(state.workDurationSec, state.restDurationSec, state.totalRounds - 1)
                            }
                        },
                        onIncrement = {
                            onUpdateCustom(state.workDurationSec, state.restDurationSec, state.totalRounds + 1)
                        }
                    )
                }
            }
        }

        // Bottom Controls
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color(0xFF090D16).copy(alpha = 0.95f),
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = DarkOutlineVariant)
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    OutlinedButton(
                        onClick = onReset,
                        enabled = state.isActive,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("interval_reset_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Interval Timer",
                            tint = if (state.isActive) TextPrimary else TextMuted,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Main Start/Pause
                    val mainBtnColor by animateColorAsState(
                        targetValue = if (state.isRunning) RacingRed else VoltLime,
                        label = "interval_btn_color"
                    )

                    Button(
                        onClick = if (state.isRunning) onPause else onStart,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = mainBtnColor,
                            contentColor = if (state.isRunning) Color.White else OnVoltLime
                        ),
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("interval_toggle_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isRunning) "Pause" else "Start",
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IntervalStepperRow(
    label: String,
    valueText: String,
    accentColor: Color,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = valueText,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier
                    .width(64.dp)
                    .padding(horizontal = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
