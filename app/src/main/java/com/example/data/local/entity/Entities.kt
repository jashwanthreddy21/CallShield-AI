package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.CallDirection
import com.example.domain.model.RiskLevel
import com.example.domain.model.RuleMatchType

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val callerName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val action: CallAction,
    val category: CallCategory,
    val riskLevel: RiskLevel,
    val direction: CallDirection = CallDirection.INCOMING,
    val threatScore: Int = 15,
    val confidenceScore: Float = 0.85f,
    val purpose: String = "",
    val summary: String = "",
    val sensitiveDetected: Boolean = false,
    val detectedIndicators: String = "",
    val hasTranscript: Boolean = false,
    val isSimulated: Boolean = false
)

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchType: RuleMatchType,
    val pattern: String,
    val action: CallAction,
    val targetCategory: CallCategory? = null,
    val priority: Int = 5,
    val enabled: Boolean = true,
    val matchesCount: Int = 0,
    val createdDate: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "allowlist")
data class AllowlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val contactName: String,
    val category: String, // Family, Friends, Work, Bank, Hospital, College, Custom
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transcripts")
data class TranscriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callId: Long,
    val speaker: String, // "AI", "CALLER", "SYSTEM"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "caller_memory")
data class CallerMemoryEntity(
    @PrimaryKey val phoneNumber: String,
    val callerName: String,
    val firstSeen: Long = System.currentTimeMillis(),
    val lastInteraction: Long = System.currentTimeMillis(),
    val interactionCount: Int = 1,
    val contextNotes: String = ""
)

@Entity(tableName = "community_reports")
data class CommunityReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val category: CallCategory,
    val reportCount: Int = 1,
    val topTags: String = "",
    val reportedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val callerName: String = "",
    val reason: String = "Blocked by user",
    val blockedAt: Long = System.currentTimeMillis(),
    val blockCount: Int = 0
)
