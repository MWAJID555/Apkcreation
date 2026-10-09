package com.example.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SessionEntity
import com.example.data.SessionRepository
import com.example.model.IntervalPhase
import com.example.model.IntervalPreset
import com.example.model.Lap
import com.example.util.FeedbackManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class StopwatchUiState(
    val isRunning: Boolean = false,
    val elapsedTimeMs: Long = 0L,
    val currentLapTimeMs: Long = 0L,
    val laps: List<Lap> = emptyList(),
    val fastestLapTimeMs: Long? = null,
    val slowestLapTimeMs: Long? = null,
    val averageLapTimeMs: Long? = null
)

data class IntervalUiState(
    val isActive: Boolean = false,
    val isRunning: Boolean = false,
    val phase: IntervalPhase = IntervalPhase.PREPARE,
    val currentRound: Int = 1,
    val totalRounds: Int = 8,
    val workDurationSec: Int = 20,
    val restDurationSec: Int = 10,
    val prepareDurationSec: Int = 5,
    val remainingSecondsInPhase: Int = 5,
    val phaseDurationSec: Int = 5,
    val progress: Float = 0f,
    val totalElapsedSec: Int = 0
)

data class SettingsUiState(
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
    val showAnalogDial: Boolean = true
)

class StopwatchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SessionRepository
    val feedbackManager: FeedbackManager = FeedbackManager(application)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SessionRepository(db.sessionDao())
    }

    val savedSessions: StateFlow<List<SessionEntity>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _stopwatchState = MutableStateFlow(StopwatchUiState())
    val stopwatchState: StateFlow<StopwatchUiState> = _stopwatchState.asStateFlow()

    private val _intervalState = MutableStateFlow(
        IntervalUiState(
            phase = IntervalPhase.PREPARE,
            remainingSecondsInPhase = 5,
            phaseDurationSec = 5
        )
    )
    val intervalState: StateFlow<IntervalUiState> = _intervalState.asStateFlow()

    private val _settingsState = MutableStateFlow(SettingsUiState())
    val settingsState: StateFlow<SettingsUiState> = _settingsState.asStateFlow()

    // Timing internals
    private var stopwatchJob: Job? = null
    private var baseAccumulatedMs = 0L
    private var startRealtime = 0L
    private var totalCompletedLapsDurationMs = 0L

    // Interval internals
    private var intervalJob: Job? = null

    val presets = listOf(
        IntervalPreset("tabata", "Tabata Protocol", 20, 10, 8, "20s Work / 10s Rest · 8 Rounds"),
        IntervalPreset("hiit", "HIIT Sprints", 30, 30, 10, "30s Work / 30s Rest · 10 Rounds"),
        IntervalPreset("boxing", "Boxing Rounds", 180, 60, 5, "3 min Work / 1 min Rest · 5 Rounds"),
        IntervalPreset("emom", "1-Min EMOM", 45, 15, 10, "45s Work / 15s Rest · 10 Rounds")
    )

    // ==========================================
    // STOPWATCH LOGIC
    // ==========================================

    fun startStopwatch() {
        if (_stopwatchState.value.isRunning) return
        startRealtime = SystemClock.elapsedRealtime()

        feedbackManager.playStart(
            soundEnabled = _settingsState.value.soundEnabled,
            hapticEnabled = _settingsState.value.hapticEnabled
        )

        _stopwatchState.update { it.copy(isRunning = true) }

        stopwatchJob = viewModelScope.launch {
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                val currentElapsed = baseAccumulatedMs + (now - startRealtime)
                val currentLap = (currentElapsed - totalCompletedLapsDurationMs).coerceAtLeast(0L)

                _stopwatchState.update {
                    it.copy(
                        elapsedTimeMs = currentElapsed,
                        currentLapTimeMs = currentLap
                    )
                }
                delay(16) // ~60fps smooth tick for centisecond accuracy
            }
        }
    }

    fun pauseStopwatch() {
        if (!_stopwatchState.value.isRunning) return
        stopwatchJob?.cancel()
        stopwatchJob = null

        val now = SystemClock.elapsedRealtime()
        baseAccumulatedMs += (now - startRealtime)
        val currentLap = (baseAccumulatedMs - totalCompletedLapsDurationMs).coerceAtLeast(0L)

        feedbackManager.playStop(
            soundEnabled = _settingsState.value.soundEnabled,
            hapticEnabled = _settingsState.value.hapticEnabled
        )

        _stopwatchState.update {
            it.copy(
                isRunning = false,
                elapsedTimeMs = baseAccumulatedMs,
                currentLapTimeMs = currentLap
            )
        }
    }

    fun resetStopwatch() {
        stopwatchJob?.cancel()
        stopwatchJob = null
        baseAccumulatedMs = 0L
        startRealtime = 0L
        totalCompletedLapsDurationMs = 0L

        feedbackManager.playClick(
            soundEnabled = _settingsState.value.soundEnabled,
            hapticEnabled = _settingsState.value.hapticEnabled
        )

        _stopwatchState.value = StopwatchUiState()
    }

    fun recordLap() {
        val state = _stopwatchState.value
        val currentTotalElapsed = if (state.isRunning) {
            baseAccumulatedMs + (SystemClock.elapsedRealtime() - startRealtime)
        } else {
            state.elapsedTimeMs
        }

        if (currentTotalElapsed == 0L) return

        val thisLapDuration = currentTotalElapsed - totalCompletedLapsDurationMs
        totalCompletedLapsDurationMs = currentTotalElapsed

        val lapNumber = state.laps.size + 1
        val previousLapDuration = state.laps.firstOrNull()?.durationMs ?: 0L
        val diffFromPrev = if (state.laps.isNotEmpty()) thisLapDuration - previousLapDuration else 0L

        val newLap = Lap(
            lapNumber = lapNumber,
            durationMs = thisLapDuration,
            splitTimeMs = currentTotalElapsed,
            diffFromPrevMs = diffFromPrev
        )

        // Prepend new lap to list (newest first)
        val updatedLaps = listOf(newLap) + state.laps

        // Recalculate fastest & slowest
        val minDuration = updatedLaps.minOfOrNull { it.durationMs } ?: 0L
        val maxDuration = updatedLaps.maxOfOrNull { it.durationMs } ?: 0L
        val avgDuration = (updatedLaps.map { it.durationMs }.average()).toLong()

        val markedLaps = updatedLaps.map { lap ->
            lap.copy(
                isFastest = updatedLaps.size > 1 && lap.durationMs == minDuration,
                isSlowest = updatedLaps.size > 1 && lap.durationMs == maxDuration
            )
        }

        feedbackManager.playLap(
            soundEnabled = _settingsState.value.soundEnabled,
            hapticEnabled = _settingsState.value.hapticEnabled
        )

        _stopwatchState.update {
            it.copy(
                currentLapTimeMs = 0L,
                laps = markedLaps,
                fastestLapTimeMs = if (markedLaps.size > 1) minDuration else null,
                slowestLapTimeMs = if (markedLaps.size > 1) maxDuration else null,
                averageLapTimeMs = avgDuration
            )
        }
    }

    fun saveCurrentSession(title: String, notes: String, onSaved: () -> Unit) {
        val state = _stopwatchState.value
        if (state.elapsedTimeMs == 0L) return

        viewModelScope.launch {
            // Laps to save in chronological order (#1 first)
            val chronologicalLaps = if (state.laps.isNotEmpty()) {
                state.laps.reversed()
            } else {
                listOf(Lap(1, state.elapsedTimeMs, state.elapsedTimeMs))
            }

            repository.saveSession(
                title = title,
                totalDurationMs = state.elapsedTimeMs,
                laps = chronologicalLaps,
                notes = notes
            )
            onSaved()
        }
    }

    // ==========================================
    // INTERVAL WORKOUT TIMER LOGIC
    // ==========================================

    fun selectPreset(preset: IntervalPreset) {
        resetIntervalTimer()
        _intervalState.update {
            it.copy(
                workDurationSec = preset.workSec,
                restDurationSec = preset.restSec,
                totalRounds = preset.rounds,
                phaseDurationSec = it.prepareDurationSec,
                remainingSecondsInPhase = it.prepareDurationSec
            )
        }
    }

    fun updateCustomInterval(workSec: Int, restSec: Int, rounds: Int) {
        resetIntervalTimer()
        _intervalState.update {
            it.copy(
                workDurationSec = workSec.coerceIn(5, 3600),
                restDurationSec = restSec.coerceIn(0, 3600),
                totalRounds = rounds.coerceIn(1, 99)
            )
        }
    }

    fun startIntervalTimer() {
        if (_intervalState.value.isRunning) return

        feedbackManager.playStart(
            soundEnabled = _settingsState.value.soundEnabled,
            hapticEnabled = _settingsState.value.hapticEnabled
        )

        _intervalState.update { it.copy(isRunning = true, isActive = true) }

        intervalJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                tickInterval()
            }
        }
    }

    private fun tickInterval() {
        val current = _intervalState.value
        if (current.phase == IntervalPhase.COMPLETED) {
            pauseIntervalTimer()
            return
        }

        val remaining = current.remainingSecondsInPhase - 1
        val elapsedSec = current.totalElapsedSec + 1

        if (remaining in 1..3) {
            feedbackManager.playCountdown(
                soundEnabled = _settingsState.value.soundEnabled,
                hapticEnabled = _settingsState.value.hapticEnabled
            )
        }

        if (remaining > 0) {
            val progress = 1f - (remaining.toFloat() / current.phaseDurationSec.toFloat())
            _intervalState.update {
                it.copy(
                    remainingSecondsInPhase = remaining,
                    progress = progress,
                    totalElapsedSec = elapsedSec
                )
            }
            return
        }

        // Phase transitioned
        when (current.phase) {
            IntervalPhase.PREPARE -> {
                feedbackManager.playWorkPhase(
                    soundEnabled = _settingsState.value.soundEnabled,
                    hapticEnabled = _settingsState.value.hapticEnabled
                )
                _intervalState.update {
                    it.copy(
                        phase = IntervalPhase.WORK,
                        currentRound = 1,
                        remainingSecondsInPhase = it.workDurationSec,
                        phaseDurationSec = it.workDurationSec,
                        progress = 0f,
                        totalElapsedSec = elapsedSec
                    )
                }
            }
            IntervalPhase.WORK -> {
                if (current.restDurationSec > 0 && current.currentRound < current.totalRounds) {
                    feedbackManager.playRestPhase(
                        soundEnabled = _settingsState.value.soundEnabled,
                        hapticEnabled = _settingsState.value.hapticEnabled
                    )
                    _intervalState.update {
                        it.copy(
                            phase = IntervalPhase.REST,
                            remainingSecondsInPhase = it.restDurationSec,
                            phaseDurationSec = it.restDurationSec,
                            progress = 0f,
                            totalElapsedSec = elapsedSec
                        )
                    }
                } else if (current.currentRound < current.totalRounds) {
                    // No rest phase, go straight to next work round
                    feedbackManager.playWorkPhase(
                        soundEnabled = _settingsState.value.soundEnabled,
                        hapticEnabled = _settingsState.value.hapticEnabled
                    )
                    _intervalState.update {
                        it.copy(
                            phase = IntervalPhase.WORK,
                            currentRound = it.currentRound + 1,
                            remainingSecondsInPhase = it.workDurationSec,
                            phaseDurationSec = it.workDurationSec,
                            progress = 0f,
                            totalElapsedSec = elapsedSec
                        )
                    }
                } else {
                    // Finished all rounds!
                    feedbackManager.playFinished(
                        soundEnabled = _settingsState.value.soundEnabled,
                        hapticEnabled = _settingsState.value.hapticEnabled
                    )
                    _intervalState.update {
                        it.copy(
                            phase = IntervalPhase.COMPLETED,
                            remainingSecondsInPhase = 0,
                            progress = 1f,
                            isRunning = false,
                            totalElapsedSec = elapsedSec
                        )
                    }
                    intervalJob?.cancel()
                }
            }
            IntervalPhase.REST -> {
                val nextRound = current.currentRound + 1
                feedbackManager.playWorkPhase(
                    soundEnabled = _settingsState.value.soundEnabled,
                    hapticEnabled = _settingsState.value.hapticEnabled
                )
                _intervalState.update {
                    it.copy(
                        phase = IntervalPhase.WORK,
                        currentRound = nextRound,
                        remainingSecondsInPhase = it.workDurationSec,
                        phaseDurationSec = it.workDurationSec,
                        progress = 0f,
                        totalElapsedSec = elapsedSec
                    )
                }
            }
            IntervalPhase.COMPLETED -> {
                // Done
            }
        }
    }

    fun pauseIntervalTimer() {
        intervalJob?.cancel()
        intervalJob = null
        feedbackManager.playStop(
            soundEnabled = _settingsState.value.soundEnabled,
            hapticEnabled = _settingsState.value.hapticEnabled
        )
        _intervalState.update { it.copy(isRunning = false) }
    }

    fun resetIntervalTimer() {
        intervalJob?.cancel()
        intervalJob = null
        feedbackManager.playClick(
            soundEnabled = _settingsState.value.soundEnabled,
            hapticEnabled = _settingsState.value.hapticEnabled
        )
        _intervalState.update {
            it.copy(
                isActive = false,
                isRunning = false,
                phase = IntervalPhase.PREPARE,
                currentRound = 1,
                remainingSecondsInPhase = it.prepareDurationSec,
                phaseDurationSec = it.prepareDurationSec,
                progress = 0f,
                totalElapsedSec = 0
            )
        }
    }

    // ==========================================
    // SETTINGS & HISTORY
    // ==========================================

    fun toggleHaptic(enabled: Boolean) {
        _settingsState.update { it.copy(hapticEnabled = enabled) }
    }

    fun toggleSound(enabled: Boolean) {
        _settingsState.update { it.copy(soundEnabled = enabled) }
    }

    fun toggleKeepScreenOn(enabled: Boolean) {
        _settingsState.update { it.copy(keepScreenOn = enabled) }
    }

    fun toggleAnalogDial(show: Boolean) {
        _settingsState.update { it.copy(showAnalogDial = show) }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopwatchJob?.cancel()
        intervalJob?.cancel()
        feedbackManager.release()
    }
}
