package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.model.Lap
import com.example.ui.components.LapChart
import com.example.ui.components.LapItem
import com.example.ui.components.SaveSessionDialog
import com.example.ui.components.StopwatchDial
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FastEmerald
import com.example.ui.theme.OnVoltLime
import com.example.ui.theme.RacingRed
import com.example.ui.theme.SlowAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltLime
import com.example.util.TimeFormatter
import com.example.viewmodel.StopwatchUiState
import kotlinx.coroutines.launch

@Composable
fun StopwatchScreen(
    state: StopwatchUiState,
    showAnalogDial: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit,
    onSaveSession: (title: String, notes: String, onSaved: () -> Unit) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val formattedTime = TimeFormatter.format(state.elapsedTimeMs)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp), // Leave space for sticky bottom controls
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Analog Dial Section
            if (showAnalogDial) {
                Spacer(modifier = Modifier.height(8.dp))
                StopwatchDial(
                    elapsedTimeMs = state.elapsedTimeMs,
                    currentLapTimeMs = state.currentLapTimeMs,
                    isRunning = state.isRunning,
                    modifier = Modifier.testTag("stopwatch_analog_dial")
                )
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Digital Time Readout
            DigitalTimeDisplay(
                formattedTime = formattedTime,
                currentLapTimeMs = state.currentLapTimeMs,
                hasLaps = state.laps.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lap Stats Summary Strip (Fastest / Slowest / Average)
            if (state.laps.size >= 2) {
                LapSummaryPillBar(
                    fastestMs = state.fastestLapTimeMs,
                    slowestMs = state.slowestLapTimeMs,
                    averageMs = state.averageLapTimeMs,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // Lap Pace Chart & Lap List
            if (state.laps.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .testTag("laps_list"),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.laps.size >= 2) {
                        item(key = "lap_chart") {
                            LapChart(
                                laps = state.laps,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            )
                        }
                    }

                    items(
                        items = state.laps,
                        key = { it.lapNumber }
                    ) { lap ->
                        LapItem(lap = lap)
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, DarkOutlineVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Precision Split Timer",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Press START, then tap LAP to log split times with pace delta analysis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
        }

        // Sticky Bottom Controls Panel
        BottomControlBar(
            isRunning = state.isRunning,
            hasElapsed = state.elapsedTimeMs > 0L,
            onStart = onStart,
            onPause = onPause,
            onReset = onReset,
            onLap = onLap,
            onSave = { showSaveDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )

        // Save Session Dialog
        if (showSaveDialog) {
            SaveSessionDialog(
                totalDurationMs = state.elapsedTimeMs,
                laps = state.laps,
                onDismiss = { showSaveDialog = false },
                onConfirmSave = { title, notes ->
                    onSaveSession(title, notes) {
                        showSaveDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Session saved to history!")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DigitalTimeDisplay(
    formattedTime: com.example.model.FormattedTime,
    currentLapTimeMs: Long,
    hasLaps: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.testTag("digital_time_display")
        ) {
            if (formattedTime.hours != "00") {
                Text(
                    text = "${formattedTime.hours}:",
                    style = MaterialTheme.typography.displayMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 42.sp
                )
            }
            Text(
                text = "${formattedTime.minutes}:${formattedTime.seconds}",
                style = MaterialTheme.typography.displayLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 52.sp
            )
            Text(
                text = ".${formattedTime.hundredths}",
                style = MaterialTheme.typography.displaySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = VoltLime,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
            )
        }

        if (hasLaps) {
            Text(
                text = "Current Lap: " + TimeFormatter.formatCompact(currentLapTimeMs),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = ElectricCyan,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun LapSummaryPillBar(
    fastestMs: Long?,
    slowestMs: Long?,
    averageMs: Long?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, DarkOutlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        fastestMs?.let {
            Column(horizontalAlignment = Alignment.Start) {
                Text("BEST", style = MaterialTheme.typography.labelSmall, color = FastEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(TimeFormatter.formatCompact(it), style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace, color = TextPrimary, fontSize = 12.sp)
            }
        }

        averageMs?.let {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("AVG", style = MaterialTheme.typography.labelSmall, color = ElectricCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(TimeFormatter.formatCompact(it), style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace, color = TextPrimary, fontSize = 12.sp)
            }
        }

        slowestMs?.let {
            Column(horizontalAlignment = Alignment.End) {
                Text("SLOWEST", style = MaterialTheme.typography.labelSmall, color = SlowAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(TimeFormatter.formatCompact(it), style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace, color = TextPrimary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun BottomControlBar(
    isRunning: Boolean,
    hasElapsed: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Button: Reset (enabled when stopped and has elapsed time)
                OutlinedButton(
                    onClick = onReset,
                    enabled = !isRunning && hasElapsed,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("stopwatch_reset_button"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Stopwatch",
                        tint = if (!isRunning && hasElapsed) TextPrimary else TextMuted,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Center Primary Button: START / STOP
                val mainButtonColor by animateColorAsState(
                    targetValue = if (isRunning) RacingRed else VoltLime,
                    label = "main_btn_color"
                )
                val mainTextColor = if (isRunning) Color.White else OnVoltLime

                Button(
                    onClick = if (isRunning) onPause else onStart,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mainButtonColor,
                        contentColor = mainTextColor
                    ),
                    modifier = Modifier
                        .size(76.dp)
                        .testTag("stopwatch_toggle_button"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Right Button: LAP (when running) or SAVE (when stopped with elapsed time)
                if (isRunning) {
                    FilledTonalButton(
                        onClick = onLap,
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarkSurfaceVariant,
                            contentColor = VoltLime
                        ),
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("stopwatch_lap_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Record Lap",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "LAP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    FilledTonalButton(
                        onClick = onSave,
                        enabled = hasElapsed,
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarkSurfaceVariant,
                            contentColor = if (hasElapsed) ElectricCyan else TextMuted
                        ),
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("stopwatch_save_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "Save Session",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "SAVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
