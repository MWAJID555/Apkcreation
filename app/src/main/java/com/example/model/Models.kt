package com.example.model

data class Lap(
    val lapNumber: Int,
    val durationMs: Long,
    val splitTimeMs: Long,
    val diffFromPrevMs: Long = 0L,
    val isFastest: Boolean = false,
    val isSlowest: Boolean = false
)

enum class AppTab {
    STOPWATCH,
    INTERVALS,
    HISTORY
}

data class FormattedTime(
    val hours: String,
    val minutes: String,
    val seconds: String,
    val hundredths: String,
    val fullFormatted: String
)

enum class IntervalPhase {
    PREPARE,
    WORK,
    REST,
    COMPLETED
}

data class IntervalPreset(
    val id: String,
    val name: String,
    val workSec: Int,
    val restSec: Int,
    val rounds: Int,
    val description: String
)
