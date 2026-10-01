package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity to store call history logs, including phone numbers,
 * timestamps, and AI-generated screening summaries.
 */
@Entity(tableName = "call_history_logs")
data class CallHistoryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val callerName: String = "Unknown Caller",
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val actionTaken: String = "SCREENED", // BLOCKED, ALLOWED, SCREENED, SILENCED, LIFTED_BY_AI
    val riskLevel: String = "LOW", // LOW, MEDIUM, HIGH, CRITICAL
    val category: String = "UNKNOWN", // SPAM, FRAUD, TELEMARKETING, LEGITIMATE, UNKNOWN
    val threatScore: Int = 0, // 0 - 100
    val aiScreeningSummary: String = "", // AI-generated screening summary
    val detectedKeywords: String = "",
    val isBlocked: Boolean = false,
    val wasLiftedByAI: Boolean = false, // Whether the call was answered/lifted by AI
    val callerIntent: String = "",
    val rawNotes: String = ""
)
