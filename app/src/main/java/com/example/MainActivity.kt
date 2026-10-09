package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppTab
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.IntervalScreen
import com.example.ui.screens.SettingsSheet
import com.example.ui.screens.StopwatchScreen
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltLime
import com.example.viewmodel.StopwatchViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: StopwatchViewModel = viewModel()
            val settings by viewModel.settingsState.collectAsStateWithLifecycle()

            // Handle Keep Screen Awake flag
            LaunchedEffect(settings.keepScreenOn) {
                if (settings.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: StopwatchViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(AppTab.STOPWATCH) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val stopwatchState by viewModel.stopwatchState.collectAsStateWithLifecycle()
    val intervalState by viewModel.intervalState.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val savedSessions by viewModel.savedSessions.collectAsStateWithLifecycle()

    // BackHandler: return to STOPWATCH if on other tabs
    if (selectedTab != AppTab.STOPWATCH) {
        BackHandler {
            selectedTab = AppTab.STOPWATCH
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TextPrimary
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(VoltLime.copy(alpha = 0.18f))
                                .border(1.dp, VoltLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = VoltLime,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CHRONOS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "PRECISION TIMER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = VoltLime,
                                fontSize = 9.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Preferences",
                            tint = TextSecondary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 6.dp
            ) {
                // Tab 1: Stopwatch
                NavigationBarItem(
                    selected = selectedTab == AppTab.STOPWATCH,
                    onClick = { selectedTab = AppTab.STOPWATCH },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.STOPWATCH) Icons.Default.Timer else Icons.Outlined.Timer,
                            contentDescription = "Stopwatch"
                        )
                    },
                    label = { Text("Stopwatch", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F1700),
                        selectedTextColor = VoltLime,
                        indicatorColor = VoltLime,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_stopwatch")
                )

                // Tab 2: Intervals
                NavigationBarItem(
                    selected = selectedTab == AppTab.INTERVALS,
                    onClick = { selectedTab = AppTab.INTERVALS },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.INTERVALS) Icons.Default.FitnessCenter else Icons.Outlined.FitnessCenter,
                            contentDescription = "Intervals"
                        )
                    },
                    label = { Text("Intervals", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F1700),
                        selectedTextColor = VoltLime,
                        indicatorColor = VoltLime,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_intervals")
                )

                // Tab 3: History
                NavigationBarItem(
                    selected = selectedTab == AppTab.HISTORY,
                    onClick = { selectedTab = AppTab.HISTORY },
                    icon = {
                        if (savedSessions.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = VoltLime,
                                        contentColor = Color(0xFF0F1700)
                                    ) {
                                        Text("${savedSessions.size}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == AppTab.HISTORY) Icons.Default.History else Icons.Outlined.History,
                                    contentDescription = "History"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (selectedTab == AppTab.HISTORY) Icons.Default.History else Icons.Outlined.History,
                                contentDescription = "History"
                            )
                        }
                    },
                    label = { Text("History", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F1700),
                        selectedTextColor = VoltLime,
                        indicatorColor = VoltLime,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    AppTab.STOPWATCH -> {
                        StopwatchScreen(
                            state = stopwatchState,
                            showAnalogDial = settingsState.showAnalogDial,
                            onStart = viewModel::startStopwatch,
                            onPause = viewModel::pauseStopwatch,
                            onReset = viewModel::resetStopwatch,
                            onLap = viewModel::recordLap,
                            onSaveSession = viewModel::saveCurrentSession,
                            snackbarHostState = snackbarHostState
                        )
                    }
                    AppTab.INTERVALS -> {
                        IntervalScreen(
                            state = intervalState,
                            presets = viewModel.presets,
                            onSelectPreset = viewModel::selectPreset,
                            onUpdateCustom = viewModel::updateCustomInterval,
                            onStart = viewModel::startIntervalTimer,
                            onPause = viewModel::pauseIntervalTimer,
                            onReset = viewModel::resetIntervalTimer
                        )
                    }
                    AppTab.HISTORY -> {
                        HistoryScreen(
                            sessions = savedSessions,
                            onDeleteSession = viewModel::deleteSession,
                            onClearAll = viewModel::clearAllHistory,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }

    // Settings Modal Bottom Sheet
    if (showSettingsSheet) {
        SettingsSheet(
            settings = settingsState,
            onToggleHaptic = viewModel::toggleHaptic,
            onToggleSound = viewModel::toggleSound,
            onToggleKeepScreenOn = viewModel::toggleKeepScreenOn,
            onToggleAnalogDial = viewModel::toggleAnalogDial,
            onDismiss = { showSettingsSheet = false }
        )
    }
}
