package com.example.domain.engine

import com.example.domain.model.RiskLevel
import com.example.domain.model.ScamIndicator

object ScamIndicatorEngine {

    private val KNOWN_INDICATORS = listOf(
        ScamIndicator(
            id = "IND_OTP",
            title = "OTP / Verification Code Request",
            description = "Caller explicitly requested a one-time password or auth code.",
            severity = RiskLevel.HIGH
        ),
        ScamIndicator(
            id = "IND_CREDENTIALS",
            title = "Bank Credential / PIN Request",
            description = "Caller attempted to extract banking password, card CVV, or PIN.",
            severity = RiskLevel.HIGH
        ),
        ScamIndicator(
            id = "IND_URGENT_PAYMENT",
            title = "Urgent Payment or Transfer Demand",
            description = "Caller demanded immediate financial settlement to avert penalties.",
            severity = RiskLevel.HIGH
        ),
        ScamIndicator(
            id = "IND_REMOTE_ACCESS",
            title = "Remote Access Tool Request",
            description = "Caller asked to download AnyDesk, TeamViewer, or remote screen sharing software.",
            severity = RiskLevel.HIGH
        ),
        ScamIndicator(
            id = "IND_IMPERSONATION",
            title = "Official / Authority Impersonation",
            description = "Caller claims to be law enforcement, central bank, or customs authority threatening action.",
            severity = RiskLevel.HIGH
        ),
        ScamIndicator(
            id = "IND_LOTTERY",
            title = "Lottery / Unsolicited Prize Claim",
            description = "Caller claimed the user won an unentered lottery or cash prize requiring a processing fee.",
            severity = RiskLevel.MEDIUM
        ),
        ScamIndicator(
            id = "IND_URGENCY",
            title = "High Psychological Urgency",
            description = "Caller manufactured extreme time pressure to prevent deliberate verification.",
            severity = RiskLevel.MEDIUM
        ),
        ScamIndicator(
            id = "IND_MARKETING",
            title = "Unsolicited Commercial Promotion",
            description = "Caller offering personal loans, credit cards, or insurance policies.",
            severity = RiskLevel.LOW
        ),
        ScamIndicator(
            id = "IND_CALLBACK",
            title = "Requested Direct Callback",
            description = "Caller requested a callback number to discuss eligibility.",
            severity = RiskLevel.LOW
        )
    )

    /**
     * Inspects caller dialogue for threat indicators.
     * Returns detected indicators and calculates overall risk assessment.
     */
    fun analyzeTranscript(dialogue: String): DetectionResult {
        val lower = dialogue.lowercase()
        val detected = mutableListOf<ScamIndicator>()

        if (lower.contains("otp") || lower.contains("verification code") || lower.contains("one time password") || lower.contains("auth code")) {
            KNOWN_INDICATORS.find { it.id == "IND_OTP" }?.let { detected.add(it) }
        }

        if (lower.contains("password") || lower.contains("cvv") || lower.contains("pin") || lower.contains("atm card") || lower.contains("debit card number")) {
            KNOWN_INDICATORS.find { it.id == "IND_CREDENTIALS" }?.let { detected.add(it) }
        }

        if (lower.contains("urgent payment") || lower.contains("pay now") || lower.contains("transfer immediately") || lower.contains("penalty fee") || lower.contains("fine")) {
            KNOWN_INDICATORS.find { it.id == "IND_URGENT_PAYMENT" }?.let { detected.add(it) }
        }

        if (lower.contains("anydesk") || lower.contains("teamviewer") || lower.contains("quicksupport") || lower.contains("screen share") || lower.contains("remote access")) {
            KNOWN_INDICATORS.find { it.id == "IND_REMOTE_ACCESS" }?.let { detected.add(it) }
        }

        if (lower.contains("police") || lower.contains("customs") || lower.contains("court") || lower.contains("arrest warrant") || lower.contains("cbi") || lower.contains("federal")) {
            KNOWN_INDICATORS.find { it.id == "IND_IMPERSONATION" }?.let { detected.add(it) }
        }

        if (lower.contains("lottery") || lower.contains("won") || lower.contains("reward claim") || lower.contains("crore") || lower.contains("jackpot")) {
            KNOWN_INDICATORS.find { it.id == "IND_LOTTERY" }?.let { detected.add(it) }
        }

        if (lower.contains("within 10 minutes") || lower.contains("within 15 minutes") || lower.contains("immediately") || lower.contains("expire today")) {
            KNOWN_INDICATORS.find { it.id == "IND_URGENCY" }?.let { detected.add(it) }
        }

        if (lower.contains("loan") || lower.contains("interest rate") || lower.contains("credit card") || lower.contains("insurance policy") || lower.contains("free offer")) {
            KNOWN_INDICATORS.find { it.id == "IND_MARKETING" }?.let { detected.add(it) }
        }

        if (lower.contains("callback") || lower.contains("call you back") || lower.contains("reach you at")) {
            KNOWN_INDICATORS.find { it.id == "IND_CALLBACK" }?.let { detected.add(it) }
        }

        val riskLevel = when {
            detected.any { it.severity == RiskLevel.HIGH } -> RiskLevel.HIGH
            detected.any { it.severity == RiskLevel.MEDIUM } -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        val isSensitive = detected.any { it.severity == RiskLevel.HIGH }

        return DetectionResult(
            detectedIndicators = detected,
            riskLevel = riskLevel,
            hasSensitiveRequest = isSensitive
        )
    }

    data class DetectionResult(
        val detectedIndicators: List<ScamIndicator>,
        val riskLevel: RiskLevel,
        val hasSensitiveRequest: Boolean
    )
}
