package com.example.services.call

import android.content.Context
import android.util.Log
import com.example.data.local.CallShieldDatabase
import com.example.data.local.entity.BlockedNumberEntity
import com.example.data.local.entity.CallEntity
import com.example.domain.engine.PhoneNormalizer
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.CallDirection
import com.example.domain.model.RiskLevel
import com.example.services.notifications.CallNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InterceptionDecision(
    val isBlocked: Boolean,
    val matchedBlockedEntry: BlockedNumberEntity? = null,
    val action: CallAction,
    val reason: String,
    val isAllowlisted: Boolean = false,
    val normalizedNumber: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Logic service that intercepts incoming calls and compares them against
 * the Room database Blocked List table and trusted allowlist.
 */
class BlockedCallInterceptor(private val database: CallShieldDatabase) {

    /**
     * Intercepts an incoming call and compares it against the Room database 'blocked_numbers' table.
     * Returns an [InterceptionDecision] indicating whether the call must be rejected/dropped.
     */
    suspend fun interceptCall(
        incomingNumber: String,
        callerName: String = "",
        shouldLogCall: Boolean = true,
        notifyUser: Boolean = true,
        context: Context? = null
    ): InterceptionDecision = withContext(Dispatchers.IO) {
        val normalized = PhoneNormalizer.normalize(incomingNumber)
        Log.d(TAG, "Intercepting incoming call: $incomingNumber (normalized: $normalized)")

        // 1. High-priority check: Allowlist check takes precedence to prevent blocking trusted numbers
        val allowlist = database.allowlistDao().getAllAllowlistSync()
        val isAllowlisted = allowlist.any {
            PhoneNormalizer.matchesPattern(it.phoneNumber, incomingNumber) ||
                    PhoneNormalizer.normalize(it.phoneNumber) == normalized
        }

        if (isAllowlisted) {
            Log.d(TAG, "Incoming number $incomingNumber is in Allowlist. Permitted.")
            return@withContext InterceptionDecision(
                isBlocked = false,
                matchedBlockedEntry = null,
                action = CallAction.ALLOW,
                reason = "Allowed: Number is present in trusted allowlist",
                isAllowlisted = true,
                normalizedNumber = normalized
            )
        }

        // 2. Query Room database table 'blocked_numbers'
        val blockedList = database.blockedNumberDao().getAllBlockedNumbersSync()
        val matchedBlocked = blockedList.find { blocked ->
            PhoneNormalizer.matchesPattern(blocked.phoneNumber, incomingNumber) ||
                    PhoneNormalizer.normalize(blocked.phoneNumber) == normalized
        }

        if (matchedBlocked != null) {
            Log.w(TAG, "CALL INTERCEPTED! Number $incomingNumber matches Blocked List table (Reason: ${matchedBlocked.reason})")

            // Increment the block counter in Room table
            database.blockedNumberDao().incrementBlockCount(matchedBlocked.id)

            // Record call in Room call logs table
            if (shouldLogCall) {
                database.callDao().insertCall(
                    CallEntity(
                        phoneNumber = incomingNumber,
                        callerName = if (matchedBlocked.callerName.isNotBlank()) {
                            matchedBlocked.callerName
                        } else if (callerName.isNotBlank()) {
                            callerName
                        } else {
                            "Blocked Caller"
                        },
                        timestamp = System.currentTimeMillis(),
                        durationSeconds = 0,
                        action = CallAction.BLOCK,
                        category = CallCategory.SPAM,
                        riskLevel = RiskLevel.HIGH,
                        direction = CallDirection.BLOCKED,
                        threatScore = 99,
                        confidenceScore = 1.0f,
                        purpose = "Blocked List Interception",
                        summary = "Call intercepted and blocked by Room database list: ${matchedBlocked.reason}",
                        sensitiveDetected = true,
                        detectedIndicators = "In Blocked List database",
                        hasTranscript = false,
                        isSimulated = false
                    )
                )
            }

            // Post notification if requested
            if (notifyUser && context != null) {
                CallNotificationManager.notifyBlockedCall(
                    context = context,
                    phoneNumber = incomingNumber,
                    reason = "Blocked List: ${matchedBlocked.reason}"
                )
            }

            return@withContext InterceptionDecision(
                isBlocked = true,
                matchedBlockedEntry = matchedBlocked,
                action = CallAction.BLOCK,
                reason = "Blocked by list: ${matchedBlocked.reason}",
                isAllowlisted = false,
                normalizedNumber = normalized
            )
        }

        // 3. Not in Blocked List
        Log.d(TAG, "Number $incomingNumber is not in Blocked List table.")
        return@withContext InterceptionDecision(
            isBlocked = false,
            matchedBlockedEntry = null,
            action = CallAction.ALLOW,
            reason = "Clean: Not present in Blocked List",
            isAllowlisted = false,
            normalizedNumber = normalized
        )
    }

    companion object {
        private const val TAG = "BlockedCallInterceptor"
    }
}
