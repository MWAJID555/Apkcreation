package com.example.data

import com.example.model.Lap
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {

    val allSessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    suspend fun saveSession(
        title: String,
        totalDurationMs: Long,
        laps: List<Lap>,
        notes: String = ""
    ): Long {
        val fastest = laps.minOfOrNull { it.durationMs } ?: 0L
        val slowest = laps.maxOfOrNull { it.durationMs } ?: 0L
        val avg = if (laps.isNotEmpty()) laps.map { it.durationMs }.average().toLong() else 0L

        // Encode laps as "lapNum:duration:split" separated by comma
        val encodedLaps = laps.joinToString(",") { "${it.lapNumber}:${it.durationMs}:${it.splitTimeMs}" }

        val entity = SessionEntity(
            title = title.ifBlank { "Session" },
            timestamp = System.currentTimeMillis(),
            totalDurationMs = totalDurationMs,
            lapCount = laps.size,
            fastestLapMs = fastest,
            slowestLapMs = slowest,
            averageLapMs = avg,
            lapsData = encodedLaps,
            notes = notes
        )
        return sessionDao.insertSession(entity)
    }

    suspend fun deleteSession(id: Long) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun clearAll() {
        sessionDao.clearAll()
    }

    companion object {
        fun parseLaps(lapsData: String): List<Lap> {
            if (lapsData.isBlank()) return emptyList()
            val list = mutableListOf<Lap>()
            val items = lapsData.split(",")
            var prevDuration = 0L

            val parsedTuples = items.mapNotNull { item ->
                val parts = item.split(":")
                if (parts.size >= 3) {
                    val num = parts[0].toIntOrNull() ?: 1
                    val dur = parts[1].toLongOrNull() ?: 0L
                    val split = parts[2].toLongOrNull() ?: 0L
                    Triple(num, dur, split)
                } else null
            }

            val minDuration = parsedTuples.minOfOrNull { it.second } ?: 0L
            val maxDuration = parsedTuples.maxOfOrNull { it.second } ?: 0L

            for (i in parsedTuples.indices) {
                val t = parsedTuples[i]
                val diff = if (i > 0) t.second - prevDuration else 0L
                prevDuration = t.second
                list.add(
                    Lap(
                        lapNumber = t.first,
                        durationMs = t.second,
                        splitTimeMs = t.third,
                        diffFromPrevMs = diff,
                        isFastest = parsedTuples.size > 1 && t.second == minDuration,
                        isSlowest = parsedTuples.size > 1 && t.second == maxDuration
                    )
                )
            }
            return list
        }
    }
}
