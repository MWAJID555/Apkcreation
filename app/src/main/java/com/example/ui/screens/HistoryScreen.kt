package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SessionEntity
import com.example.data.SessionRepository
import com.example.ui.components.LapItem
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FastEmerald
import com.example.ui.theme.RacingRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltLime
import com.example.util.TimeFormatter
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    sessions: List<SessionEntity>,
    onDeleteSession: (Long) -> Unit,
    onClearAll: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sessionToDelete by remember { mutableStateOf<SessionEntity?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (sessions.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, DarkOutlineVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Saved Sessions",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "When you time workouts or laps, tap SAVE to record them here for review.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${sessions.size} SAVED SESSIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Clear All",
                            style = MaterialTheme.typography.labelSmall,
                            color = RacingRed,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showClearAllDialog = true }
                                .padding(4.dp)
                        )
                    }
                }

                items(
                    items = sessions,
                    key = { it.id }
                ) { session ->
                    SessionCard(
                        session = session,
                        onDelete = { sessionToDelete = session },
                        onShare = { shareSession(context, session) }
                    )
                }
            }
        }

        // Delete Single Confirmation Dialog
        sessionToDelete?.let { session ->
            AlertDialog(
                onDismissRequest = { sessionToDelete = null },
                containerColor = DarkSurfaceVariant,
                title = { Text("Delete Session?", color = TextPrimary) },
                text = { Text("Are you sure you want to delete \"${session.title}\"?", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteSession(session.id)
                            sessionToDelete = null
                            scope.launch { snackbarHostState.showSnackbar("Session deleted") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
                    ) {
                        Text("Delete", color = androidx.compose.ui.graphics.Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { sessionToDelete = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Clear All Dialog
        if (showClearAllDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllDialog = false },
                containerColor = DarkSurfaceVariant,
                title = { Text("Clear All History?", color = TextPrimary) },
                text = { Text("This will permanently remove all ${sessions.size} saved sessions.", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearAll()
                            showClearAllDialog = false
                            scope.launch { snackbarHostState.showSnackbar("History cleared") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
                    ) {
                        Text("Clear All", color = androidx.compose.ui.graphics.Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showClearAllDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun SessionCard(
    session: SessionEntity,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val laps = remember(session.lapsData) { SessionRepository.parseLaps(session.lapsData) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, DarkOutlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = TimeFormatter.formatDate(session.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Row {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Session",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Session",
                        tint = RacingRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Time & Stats strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(androidx.compose.ui.graphics.Color(0xFF090D16))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("TOTAL TIME", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    TimeFormatter.formatCompact(session.totalDurationMs),
                    color = VoltLime,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            if (session.lapCount > 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LAPS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${session.lapCount}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("BEST LAP", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        TimeFormatter.formatCompact(session.fastestLapMs),
                        color = FastEmerald,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (session.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = session.notes,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Expandable lap breakdown
        if (laps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Hide Laps Breakdown" else "View ${laps.size} Laps",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    laps.forEach { lap ->
                        LapItem(lap = lap)
                    }
                }
            }
        }
    }
}

private fun shareSession(context: Context, session: SessionEntity) {
    val laps = SessionRepository.parseLaps(session.lapsData)
    val sb = StringBuilder()
    sb.appendLine("⏱️ Chronos Stopwatch - ${session.title}")
    sb.appendLine("Date: ${TimeFormatter.formatDate(session.timestamp)}")
    sb.appendLine("Total Duration: ${TimeFormatter.formatCompact(session.totalDurationMs)}")
    if (laps.isNotEmpty()) {
        sb.appendLine("Total Laps: ${laps.size}")
        sb.appendLine("Best Lap: ${TimeFormatter.formatCompact(session.fastestLapMs)}")
        sb.appendLine("Avg Lap: ${TimeFormatter.formatCompact(session.averageLapMs)}")
        sb.appendLine("--- Lap Breakdown ---")
        laps.forEach {
            sb.appendLine("Lap ${it.lapNumber}: ${TimeFormatter.formatCompact(it.durationMs)} (Split: ${TimeFormatter.formatCompact(it.splitTimeMs)})")
        }
    }
    if (session.notes.isNotBlank()) {
        sb.appendLine("Notes: ${session.notes}")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Session Summary")
    context.startActivity(shareIntent)
}
