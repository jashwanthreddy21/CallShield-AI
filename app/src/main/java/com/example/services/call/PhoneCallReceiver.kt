package com.example.services.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.telephony.TelephonyManager
import android.util.Log
import com.example.data.local.CallShieldDatabase
import com.example.data.local.entity.CallEntity
import com.example.data.local.entity.CallHistoryLogEntity
import com.example.domain.engine.RuleEngine
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.CallDirection
import com.example.domain.model.RiskLevel
import com.example.services.notifications.CallNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Real-time BroadcastReceiver for incoming phone calls (TelephonyManager.ACTION_PHONE_STATE_CHANGED).
 * When an incoming call rings:
 * 1. Checks Room database (Blocklist, Rules, Allowlist).
 * 2. If high-risk spam or blocked: silences/terminates ringing and records to Room call history.
 * 3. If AI screening: displays prompt with action to let AI "Lift / Answer" and screen call.
 * 4. Stores complete phone numbers, timestamps, and AI-generated screening summaries in Room.
 */
class PhoneCallReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val ruleEngine = RuleEngine()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: "Private Caller"

        Log.d(TAG, "Phone State changed to: $state for number: $incomingNumber")

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            handleIncomingCallRinging(context, incomingNumber)
        }
    }

    private fun handleIncomingCallRinging(context: Context, incomingNumber: String) {
        receiverScope.launch {
            try {
                val db = CallShieldDatabase.getDatabase(context)

                // 1. Check against Blocked Number table
                val interceptor = BlockedCallInterceptor(db)
                val decision = interceptor.interceptCall(
                    incomingNumber = incomingNumber,
                    shouldLogCall = true,
                    notifyUser = true,
                    context = context
                )

                if (decision.isBlocked) {
                    Log.w(TAG, "Real incoming call from $incomingNumber BLOCKED by Room database!")

                    // Silence ringer immediately
                    silenceRinger(context)

                    // End call if supported
                    CallPermissionManager.endCall(context)

                    val summary = "AI Protection blocked incoming call from $incomingNumber. Reason: Number found on local blocked firewall."

                    // Store in Room CallHistoryLogEntity
                    db.callHistoryLogDao().insertLog(
                        CallHistoryLogEntity(
                            phoneNumber = incomingNumber,
                            callerName = "Blocked Spam Caller",
                            timestamp = System.currentTimeMillis(),
                            durationSeconds = 0,
                            actionTaken = "BLOCKED",
                            riskLevel = "HIGH",
                            category = "SPAM",
                            threatScore = 95,
                            aiScreeningSummary = summary,
                            detectedKeywords = "Blocked list match",
                            isBlocked = true,
                            wasLiftedByAI = false
                        )
                    )

                    // Also store in legacy CallEntity for UI sync
                    db.callDao().insertCall(
                        CallEntity(
                            phoneNumber = incomingNumber,
                            callerName = "Blocked Spam Caller",
                            timestamp = System.currentTimeMillis(),
                            action = CallAction.BLOCK,
                            category = CallCategory.SPAM,
                            riskLevel = RiskLevel.HIGH,
                            direction = CallDirection.BLOCKED,
                            threatScore = 95,
                            summary = summary,
                            sensitiveDetected = true,
                            isSimulated = false
                        )
                    )

                    CallNotificationManager.notifyBlockedCall(
                        context = context,
                        phoneNumber = incomingNumber,
                        reason = "Matched Blocked List"
                    )
                    return@launch
                }

                if (decision.isAllowlisted) {
                    Log.d(TAG, "Incoming call $incomingNumber allowed (Allowlisted)")
                    return@launch
                }

                // 2. Evaluate active Rules in Room
                val allowlist = db.allowlistDao().getAllAllowlistSync()
                val rules = db.ruleDao().getActiveRulesSync()

                val eval = ruleEngine.evaluate(
                    incomingNumber = incomingNumber,
                    callerCategory = CallCategory.UNKNOWN,
                    allowlist = allowlist,
                    activeRules = rules,
                    defaultAction = CallAction.ALLOW
                )

                when (eval.action) {
                    CallAction.BLOCK -> {
                        silenceRinger(context)
                        CallPermissionManager.endCall(context)

                        val summary = "Rule triggered: ${eval.ruleDescription}. Dropped automatically."

                        db.callHistoryLogDao().insertLog(
                            CallHistoryLogEntity(
                                phoneNumber = incomingNumber,
                                callerName = "Spam Number",
                                timestamp = System.currentTimeMillis(),
                                durationSeconds = 0,
                                actionTaken = "BLOCKED",
                                riskLevel = "HIGH",
                                category = "SPAM",
                                threatScore = 90,
                                aiScreeningSummary = summary,
                                isBlocked = true,
                                wasLiftedByAI = false
                            )
                        )

                        if (eval.ruleId != null) {
                            db.ruleDao().incrementMatchCount(eval.ruleId)
                        }

                        CallNotificationManager.notifyBlockedCall(
                            context = context,
                            phoneNumber = incomingNumber,
                            reason = eval.reason
                        )
                    }

                    CallAction.AI_SCREEN -> {
                        Log.d(TAG, "Incoming call $incomingNumber flagged for AI Screening")
                        val summary = "AI Assistant standing by to screen incoming caller. Reason: ${eval.reason}"

                        db.callHistoryLogDao().insertLog(
                            CallHistoryLogEntity(
                                phoneNumber = incomingNumber,
                                callerName = "Screening in Progress",
                                timestamp = System.currentTimeMillis(),
                                durationSeconds = 0,
                                actionTaken = "SCREENED",
                                riskLevel = "MEDIUM",
                                category = "TELEMARKETING",
                                threatScore = 55,
                                aiScreeningSummary = summary,
                                isBlocked = false,
                                wasLiftedByAI = false
                            )
                        )

                        CallNotificationManager.notifyScreeningActive(
                            context = context,
                            phoneNumber = incomingNumber
                        )
                    }

                    CallAction.SILENCE -> {
                        silenceRinger(context)
                    }

                    CallAction.ALLOW -> {
                        // Standard call, let phone ring normally
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling incoming call: ${e.message}", e)
            }
        }
    }

    private fun silenceRinger(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.ringerMode = AudioManager.RINGER_MODE_SILENT
        } catch (e: Exception) {
            Log.w(TAG, "Could not silence ringer: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "PhoneCallReceiver"
    }
}
