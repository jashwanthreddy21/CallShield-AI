package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CallHistoryLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for CallHistoryLogEntity.
 * Provides reactive Flow streams and suspend functions for storing and querying
 * phone numbers, timestamps, and AI-generated screening summaries.
 */
@Dao
interface CallHistoryLogDao {

    @Query("SELECT * FROM call_history_logs ORDER BY timestamp DESC")
    fun getAllHistoryLogs(): Flow<List<CallHistoryLogEntity>>

    @Query("SELECT * FROM call_history_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistoryLogs(limit: Int = 20): Flow<List<CallHistoryLogEntity>>

    @Query("SELECT * FROM call_history_logs WHERE phoneNumber = :phoneNumber ORDER BY timestamp DESC")
    fun getLogsForNumber(phoneNumber: String): Flow<List<CallHistoryLogEntity>>

    @Query("SELECT * FROM call_history_logs WHERE id = :id")
    fun getLogById(id: Long): Flow<CallHistoryLogEntity?>

    @Query("SELECT * FROM call_history_logs WHERE id = :id")
    suspend fun getLogByIdDirect(id: Long): CallHistoryLogEntity?

    @Query("SELECT * FROM call_history_logs WHERE phoneNumber LIKE '%' || :query || '%' OR callerName LIKE '%' || :query || '%' OR aiScreeningSummary LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchLogs(query: String): Flow<List<CallHistoryLogEntity>>

    @Query("SELECT * FROM call_history_logs WHERE isBlocked = 1 ORDER BY timestamp DESC")
    fun getBlockedCalls(): Flow<List<CallHistoryLogEntity>>

    @Query("SELECT * FROM call_history_logs WHERE wasLiftedByAI = 1 ORDER BY timestamp DESC")
    fun getCallsLiftedByAI(): Flow<List<CallHistoryLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CallHistoryLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<CallHistoryLogEntity>): List<Long>

    @Update
    suspend fun updateLog(log: CallHistoryLogEntity)

    @Query("DELETE FROM call_history_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM call_history_logs WHERE phoneNumber = :phoneNumber")
    suspend fun deleteLogsForNumber(phoneNumber: String)

    @Query("DELETE FROM call_history_logs")
    suspend fun clearAllHistoryLogs()

    @Query("SELECT COUNT(*) FROM call_history_logs")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM call_history_logs WHERE isBlocked = 1")
    fun getBlockedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM call_history_logs WHERE wasLiftedByAI = 1")
    fun getAILiftedCount(): Flow<Int>
}
