package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AllowlistEntity
import com.example.data.local.entity.BlockedNumberEntity
import com.example.data.local.entity.CallEntity
import com.example.data.local.entity.CallerMemoryEntity
import com.example.data.local.entity.CommunityReportEntity
import com.example.data.local.entity.RuleEntity
import com.example.data.local.entity.TranscriptEntity
import com.example.domain.model.CallAction
import com.example.domain.model.RiskLevel
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Query("SELECT * FROM calls ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCalls(limit: Int = 10): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls WHERE id = :id")
    fun getCallById(id: Long): Flow<CallEntity?>

    @Query("SELECT * FROM calls WHERE id = :id")
    suspend fun getCallByIdDirect(id: Long): CallEntity?

    @Query("SELECT * FROM calls WHERE action = :action ORDER BY timestamp DESC")
    fun getCallsByAction(action: CallAction): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls WHERE riskLevel = :risk ORDER BY timestamp DESC")
    fun getCallsByRisk(risk: RiskLevel): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls WHERE phoneNumber = :phoneNumber ORDER BY timestamp DESC")
    fun getCallsForNumber(phoneNumber: String): Flow<List<CallEntity>>

    @Query("SELECT COUNT(*) FROM calls")
    fun getCallsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM calls WHERE action = :action")
    fun getCountByAction(action: CallAction): Flow<Int>

    @Query("SELECT COUNT(*) FROM calls WHERE riskLevel = :risk")
    fun getCountByRisk(risk: RiskLevel): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCalls(calls: List<CallEntity>): List<Long>

    @Update
    suspend fun updateCall(call: CallEntity)

    @Query("DELETE FROM calls WHERE id = :id")
    suspend fun deleteCall(id: Long)

    @Query("DELETE FROM calls")
    suspend fun clearAllCalls()
}

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY priority DESC, createdDate DESC")
    fun getAllRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules WHERE enabled = 1 ORDER BY priority DESC")
    suspend fun getActiveRulesSync(): List<RuleEntity>

    @Query("SELECT * FROM rules WHERE id = :id")
    fun getRuleById(id: Long): Flow<RuleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RuleEntity): Long

    @Update
    suspend fun updateRule(rule: RuleEntity)

    @Query("DELETE FROM rules WHERE id = :id")
    suspend fun deleteRule(id: Long)

    @Query("UPDATE rules SET matchesCount = matchesCount + 1 WHERE id = :ruleId")
    suspend fun incrementMatchCount(ruleId: Long)

    @Query("UPDATE rules SET enabled = :enabled WHERE id = :ruleId")
    suspend fun toggleRule(ruleId: Long, enabled: Boolean)
}

@Dao
interface AllowlistDao {
    @Query("SELECT * FROM allowlist ORDER BY contactName ASC")
    fun getAllAllowlist(): Flow<List<AllowlistEntity>>

    @Query("SELECT * FROM allowlist")
    suspend fun getAllAllowlistSync(): List<AllowlistEntity>

    @Query("SELECT * FROM allowlist WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getByPhoneNumber(phoneNumber: String): AllowlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllowlist(entry: AllowlistEntity): Long

    @Query("DELETE FROM allowlist WHERE id = :id")
    suspend fun deleteAllowlist(id: Long)

    @Query("DELETE FROM allowlist WHERE phoneNumber = :phoneNumber")
    suspend fun deleteByPhoneNumber(phoneNumber: String)
}

@Dao
interface TranscriptDao {
    @Query("SELECT * FROM transcripts WHERE callId = :callId ORDER BY timestamp ASC")
    fun getTranscriptsForCall(callId: Long): Flow<List<TranscriptEntity>>

    @Query("SELECT * FROM transcripts WHERE callId = :callId ORDER BY timestamp ASC")
    suspend fun getTranscriptsForCallSync(callId: Long): List<TranscriptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscript(entry: TranscriptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TranscriptEntity>)

    @Query("DELETE FROM transcripts WHERE callId = :callId")
    suspend fun deleteForCall(callId: Long)

    @Query("DELETE FROM transcripts")
    suspend fun clearAllTranscripts()
}

@Dao
interface CallerMemoryDao {
    @Query("SELECT * FROM caller_memory ORDER BY lastInteraction DESC")
    fun getAllMemories(): Flow<List<CallerMemoryEntity>>

    @Query("SELECT * FROM caller_memory WHERE phoneNumber = :phoneNumber")
    fun getMemoryForNumber(phoneNumber: String): Flow<CallerMemoryEntity?>

    @Query("SELECT * FROM caller_memory WHERE phoneNumber = :phoneNumber")
    suspend fun getMemoryForNumberSync(phoneNumber: String): CallerMemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(memory: CallerMemoryEntity)

    @Query("DELETE FROM caller_memory WHERE phoneNumber = :phoneNumber")
    suspend fun deleteMemory(phoneNumber: String)

    @Query("DELETE FROM caller_memory")
    suspend fun clearAllMemories()
}

@Dao
interface CommunityReportDao {
    @Query("SELECT * FROM community_reports WHERE phoneNumber = :phoneNumber")
    fun getReportForNumber(phoneNumber: String): Flow<CommunityReportEntity?>

    @Query("SELECT * FROM community_reports WHERE phoneNumber = :phoneNumber")
    suspend fun getReportForNumberSync(phoneNumber: String): CommunityReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(report: CommunityReportEntity)

    @Query("SELECT * FROM community_reports ORDER BY reportCount DESC")
    fun getAllReports(): Flow<List<CommunityReportEntity>>
}

@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY blockedAt DESC")
    fun getAllBlockedNumbers(): Flow<List<BlockedNumberEntity>>

    @Query("SELECT * FROM blocked_numbers ORDER BY blockedAt DESC")
    suspend fun getAllBlockedNumbersSync(): List<BlockedNumberEntity>

    @Query("SELECT * FROM blocked_numbers WHERE phoneNumber = :phoneNumber LIMIT 1")
    fun getBlockedNumber(phoneNumber: String): Flow<BlockedNumberEntity?>

    @Query("SELECT * FROM blocked_numbers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getBlockedNumberSync(phoneNumber: String): BlockedNumberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedNumber(entry: BlockedNumberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBlockedNumbers(entries: List<BlockedNumberEntity>): List<Long>

    @Query("DELETE FROM blocked_numbers WHERE id = :id")
    suspend fun deleteBlockedNumber(id: Long)

    @Query("DELETE FROM blocked_numbers WHERE phoneNumber = :phoneNumber")
    suspend fun deleteBlockedNumberByPhone(phoneNumber: String)

    @Query("UPDATE blocked_numbers SET blockCount = blockCount + 1 WHERE id = :id")
    suspend fun incrementBlockCount(id: Long)

    @Query("DELETE FROM blocked_numbers")
    suspend fun clearAllBlockedNumbers()
}
