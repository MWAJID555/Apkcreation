package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalDurationMs: Long,
    val lapCount: Int,
    val fastestLapMs: Long,
    val slowestLapMs: Long,
    val averageLapMs: Long,
    val lapsData: String, // Format: "1:3240:3240,2:3100:6340"
    val notes: String = ""
)
