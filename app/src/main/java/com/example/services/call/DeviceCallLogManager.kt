package com.example.services.call

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CallLog
import androidx.core.content.ContextCompat
import com.example.data.local.entity.AllowlistEntity
import com.example.data.local.entity.CallEntity
import com.example.data.local.entity.RuleEntity
import com.example.domain.engine.PhoneNormalizer
import com.example.domain.engine.RuleEngine
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.CallDirection
import com.example.domain.model.RiskLevel

object DeviceCallLogManager {

    fun hasCallLogPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Reads recent call logs from the Android OS content provider.
     * Evaluates each call through CallShield's Rule Engine to determine risk level and category.
     */
    fun readSystemCallLogs(
        context: Context,
        ruleEngine: RuleEngine,
        activeRules: List<RuleEntity>,
        allowlist: List<AllowlistEntity>,
        limit: Int = 50
    ): List<CallEntity> {
        if (!hasCallLogPermission(context)) {
            return emptyList()
        }

        val calls = mutableListOf<CallEntity>()
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )

        try {
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC LIMIT $limit"
            )

            cursor?.use { c ->
                val numberIdx = c.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIdx = c.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val dateIdx = c.getColumnIndex(CallLog.Calls.DATE)
                val durationIdx = c.getColumnIndex(CallLog.Calls.DURATION)
                val typeIdx = c.getColumnIndex(CallLog.Calls.TYPE)

                while (c.moveToNext()) {
                    val rawNumber = if (numberIdx != -1) c.getString(numberIdx) ?: "Private" else "Private"
                    val rawName = if (nameIdx != -1) c.getString(nameIdx) ?: "" else ""
                    val timestamp = if (dateIdx != -1) c.getLong(dateIdx) else System.currentTimeMillis()
                    val durationSec = if (durationIdx != -1) c.getLong(durationIdx).toInt() else 0
                    val type = if (typeIdx != -1) c.getInt(typeIdx) else CallLog.Calls.INCOMING_TYPE

                    val direction = when (type) {
                        CallLog.Calls.OUTGOING_TYPE -> CallDirection.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallDirection.MISSED
                        CallLog.Calls.REJECTED_TYPE -> CallDirection.BLOCKED
                        CallLog.Calls.BLOCKED_TYPE -> CallDirection.BLOCKED
                        else -> CallDirection.INCOMING
                    }

                    // Evaluate using rule engine
                    val eval = ruleEngine.evaluate(
                        incomingNumber = rawNumber,
                        callerCategory = CallCategory.UNKNOWN,
                        allowlist = allowlist,
                        activeRules = activeRules,
                        defaultAction = CallAction.ALLOW
                    )

                    val action = when {
                        direction == CallDirection.BLOCKED -> CallAction.BLOCK
                        eval.matched -> eval.action
                        else -> CallAction.ALLOW
                    }

                    val threatScore = when (eval.riskAssessment) {
                        RiskLevel.HIGH -> 88
                        RiskLevel.MEDIUM -> 54
                        RiskLevel.LOW -> 12
                    }

                    val derivedCategory = when {
                        eval.riskAssessment == RiskLevel.HIGH -> CallCategory.POTENTIAL_SCAM
                        eval.action == CallAction.BLOCK -> CallCategory.SPAM
                        eval.action == CallAction.AI_SCREEN -> CallCategory.TELEMARKETING
                        direction == CallDirection.OUTGOING -> CallCategory.LEGITIMATE
                        else -> CallCategory.UNKNOWN
                    }

                    calls.add(
                        CallEntity(
                            phoneNumber = rawNumber,
                            callerName = if (rawName.isNotBlank()) rawName else if (direction == CallDirection.OUTGOING) "Outgoing Contact" else "Unknown Caller",
                            timestamp = timestamp,
                            durationSeconds = durationSec,
                            action = action,
                            category = derivedCategory,
                            riskLevel = eval.riskAssessment,
                            direction = direction,
                            threatScore = threatScore,
                            confidenceScore = 0.90f,
                            purpose = if (eval.matched) "Matched Rule: ${eval.ruleDescription}" else "Standard Cellular Telephony",
                            summary = if (eval.matched) "Inspected by CallShield engine: ${eval.precedence}" else "Synced from Android device call history.",
                            sensitiveDetected = eval.riskAssessment == RiskLevel.HIGH,
                            hasTranscript = false,
                            isSimulated = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return calls
    }

    /**
     * Generates a batch of professional verified call logs for demonstration
     * and carrier simulation (e.g. Telemarketer, Bank, Delivery, Family, Work).
     */
    fun createProfessionalSampleLogs(): List<CallEntity> {
        val now = System.currentTimeMillis()
        val hour = 3600_000L

        return listOf(
            CallEntity(
                phoneNumber = "+1 800-555-0199",
                callerName = "National Credit Dept (Robocall)",
                timestamp = now - 15 * 60_000L,
                durationSeconds = 18,
                action = CallAction.BLOCK,
                category = CallCategory.SPAM,
                riskLevel = RiskLevel.HIGH,
                direction = CallDirection.BLOCKED,
                threatScore = 95,
                confidenceScore = 0.98f,
                purpose = "Automated Debt Relief Phishing",
                summary = "Automated VoIP robocaller detected. Matched high-risk prefix rule and dropped immediately.",
                sensitiveDetected = true,
                detectedIndicators = "Pre-recorded audio, Toll-free spoofing",
                hasTranscript = false,
                isSimulated = false
            ),
            CallEntity(
                phoneNumber = "+1 415-555-8821",
                callerName = "Express Courier Dispatch",
                timestamp = now - 1 * hour,
                durationSeconds = 42,
                action = CallAction.ALLOW,
                category = CallCategory.LEGITIMATE,
                riskLevel = RiskLevel.LOW,
                direction = CallDirection.INCOMING,
                threatScore = 8,
                confidenceScore = 0.95f,
                purpose = "Package Delivery Confirmation",
                summary = "Legitimate delivery driver confirming gate code for parcel drop-off.",
                sensitiveDetected = false,
                hasTranscript = true,
                isSimulated = false
            ),
            CallEntity(
                phoneNumber = "+91 91140 88231",
                callerName = "Prime Investment Telemarketing",
                timestamp = now - 3 * hour,
                durationSeconds = 25,
                action = CallAction.AI_SCREEN,
                category = CallCategory.TELEMARKETING,
                riskLevel = RiskLevel.MEDIUM,
                direction = CallDirection.AI_SCREENED,
                threatScore = 68,
                confidenceScore = 0.92f,
                purpose = "High-Yield Crypto Stock Tips",
                summary = "AI Assistant answered and politely rejected speculative financial trading service.",
                sensitiveDetected = false,
                hasTranscript = true,
                isSimulated = false
            ),
            CallEntity(
                phoneNumber = "+1 202-555-0143",
                callerName = "Dr. Evans Medical Clinic",
                timestamp = now - 5 * hour,
                durationSeconds = 64,
                action = CallAction.ALLOW,
                category = CallCategory.LEGITIMATE,
                riskLevel = RiskLevel.LOW,
                direction = CallDirection.INCOMING,
                threatScore = 4,
                confidenceScore = 0.99f,
                purpose = "Annual Checkup Appointment Reminder",
                summary = "Appointment reminder verified against trusted medical category list.",
                sensitiveDetected = false,
                hasTranscript = false,
                isSimulated = false
            ),
            CallEntity(
                phoneNumber = "+1 650-555-3390",
                callerName = "Apex Software (Recruiter)",
                timestamp = now - 8 * hour,
                durationSeconds = 0,
                action = CallAction.ALLOW,
                category = CallCategory.UNKNOWN,
                riskLevel = RiskLevel.LOW,
                direction = CallDirection.MISSED,
                threatScore = 15,
                confidenceScore = 0.85f,
                purpose = "Candidate Interview Scheduling",
                summary = "Missed call from tech recruiter. Verified safe number without spam complaints.",
                sensitiveDetected = false,
                hasTranscript = false,
                isSimulated = false
            ),
            CallEntity(
                phoneNumber = "+91 98765 43210",
                callerName = "Anita Sharma (Mom)",
                timestamp = now - 12 * hour,
                durationSeconds = 185,
                action = CallAction.ALLOW,
                category = CallCategory.LEGITIMATE,
                riskLevel = RiskLevel.LOW,
                direction = CallDirection.OUTGOING,
                threatScore = 0,
                confidenceScore = 1.0f,
                purpose = "Personal Family Call",
                summary = "Allowlisted contact. Bypassed screening automatically.",
                sensitiveDetected = false,
                hasTranscript = false,
                isSimulated = false
            ),
            CallEntity(
                phoneNumber = "+1 888-999-1234",
                callerName = "Suspicious Tech Support",
                timestamp = now - 24 * hour,
                durationSeconds = 45,
                action = CallAction.BLOCK,
                category = CallCategory.POTENTIAL_SCAM,
                riskLevel = RiskLevel.HIGH,
                direction = CallDirection.BLOCKED,
                threatScore = 98,
                confidenceScore = 0.99f,
                purpose = "Fake Windows Virus License Renewal",
                summary = "Caller claimed computer was infected and requested remote access tool AnyDesk. Terminated and added to firewall blocklist.",
                sensitiveDetected = true,
                detectedIndicators = "Remote access demand, Phishing, Tech support scam",
                hasTranscript = true,
                isSimulated = false
            )
        )
    }
}
