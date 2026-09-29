package com.example.services.call

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.example.data.local.CallShieldDatabase
import com.example.data.local.entity.CallEntity
import com.example.domain.engine.RuleEngine
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.RiskLevel
import com.example.services.notifications.CallNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Native Android CallScreeningService integration.
 * Hooked into Android OS Telecom subsystem for real call screening.
 */
class CallScreeningServiceImpl : CallScreeningService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val ruleEngine = RuleEngine()

    override fun onScreenCall(callDetails: Call.Details) {
        val incomingHandle = callDetails.handle?.schemeSpecificPart ?: "Unknown"
        Log.d(TAG, "Native CallScreeningService intercepted call from: $incomingHandle")

        serviceScope.launch {
            try {
                val db = CallShieldDatabase.getDatabase(applicationContext)
                val allowlist = db.allowlistDao().getAllAllowlistSync()
                val rules = db.ruleDao().getActiveRulesSync()

                val result = ruleEngine.evaluate(
                    incomingNumber = incomingHandle,
                    callerCategory = CallCategory.UNKNOWN,
                    allowlist = allowlist,
                    activeRules = rules,
                    defaultAction = CallAction.ALLOW
                )

                val responseBuilder = CallResponse.Builder()

                when (result.action) {
                    CallAction.BLOCK -> {
                        responseBuilder.setDisallowCall(true)
                        responseBuilder.setRejectCall(true)
                        responseBuilder.setSkipNotification(false)
                        responseBuilder.setSkipCallLog(false)
                        if (result.ruleId != null) {
                            db.ruleDao().incrementMatchCount(result.ruleId)
                        }

                        // Save call record
                        val callId = db.callDao().insertCall(
                            CallEntity(
                                phoneNumber = incomingHandle,
                                callerName = "Blocked Caller",
                                action = CallAction.BLOCK,
                                category = CallCategory.SPAM,
                                riskLevel = RiskLevel.HIGH,
                                purpose = "Unsolicited or Blocked number",
                                summary = "Call automatically blocked based on rule precedence: ${result.precedence}",
                                sensitiveDetected = false,
                                isSimulated = false
                            )
                        )

                        CallNotificationManager.notifyBlockedCall(
                            context = applicationContext,
                            phoneNumber = incomingHandle,
                            reason = result.reason
                        )
                    }

                    CallAction.SILENCE -> {
                        responseBuilder.setDisallowCall(false)
                        responseBuilder.setSilenceCall(true)
                        responseBuilder.setSkipNotification(false)
                    }

                    CallAction.AI_SCREEN -> {
                        // Mark for screening / alert user
                        responseBuilder.setDisallowCall(false)
                        responseBuilder.setSilenceCall(true)
                        CallNotificationManager.notifyScreeningActive(
                            context = applicationContext,
                            phoneNumber = incomingHandle
                        )
                    }

                    CallAction.ALLOW -> {
                        responseBuilder.setDisallowCall(false)
                        responseBuilder.setSilenceCall(false)
                        responseBuilder.setSkipNotification(false)
                    }
                }

                respondToCall(callDetails, responseBuilder.build())
            } catch (e: Exception) {
                Log.e(TAG, "Error in onScreenCall: ${e.message}", e)
                val fallbackResponse = CallResponse.Builder().setDisallowCall(false).build()
                respondToCall(callDetails, fallbackResponse)
            }
        }
    }

    companion object {
        private const val TAG = "CallScreeningService"
    }
}
