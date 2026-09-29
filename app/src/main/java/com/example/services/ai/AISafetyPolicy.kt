package com.example.services.ai

object AISafetyPolicy {

    private val SENSITIVE_KEYWORDS = listOf(
        "otp", "password", "pin", "cvv", "verification code", "auth code",
        "ssn", "social security", "aadhaar", "bank account number",
        "routing number", "credit card number", "security answer", "mothers maiden"
    )

    private val COMMITMENT_KEYWORDS = listOf(
        "agree to pay", "sign contract", "authorize charge", "confirm payment",
        "accept terms", "transfer money", "wire funds"
    )

    /**
     * Checks if the caller prompt requests sensitive data or commitments.
     */
    fun isSensitiveRequest(prompt: String): Boolean {
        val lower = prompt.lowercase()
        return SENSITIVE_KEYWORDS.any { lower.contains(it) } ||
                COMMITMENT_KEYWORDS.any { lower.contains(it) }
    }

    /**
     * Sanitizes AI response before speaking to caller.
     * Prevents disclosure of credentials, private info, or illegal commitments.
     */
    fun sanitizeOutput(rawAiResponse: String, userInstructions: String): SafetyCheckResult {
        val lower = rawAiResponse.lowercase()

        // Check if response accidentally reveals OTP or numbers looking like credentials
        if (lower.contains("otp is") || lower.contains("the otp") || lower.contains("my otp") ||
            lower.contains("here is the code") || lower.contains("the password is") ||
            lower.contains("password is") || lower.contains("pin is")
        ) {
            return SafetyCheckResult(
                safeResponse = "For security reasons, I cannot provide authentication credentials or personal information.",
                violationDetected = true,
                violationReason = "Attempted credential disclosure"
            )
        }

        // Check if response agrees to financial charges
        if (lower.contains("i authorize") || lower.contains("i agree to pay") || lower.contains("transfer the amount")) {
            return SafetyCheckResult(
                safeResponse = "I am an automated assistant and not authorized to make financial commitments on behalf of the user.",
                violationDetected = true,
                violationReason = "Attempted financial commitment"
            )
        }

        // Check criminal accusation policy: never label caller a criminal definitively
        if (lower.contains("you are a criminal") || lower.contains("you are going to jail")) {
            return SafetyCheckResult(
                safeResponse = "Potential risk indicators have been detected. This interaction has been logged and ended.",
                violationDetected = true,
                violationReason = "Definitive criminal accusation prohibited"
            )
        }

        return SafetyCheckResult(
            safeResponse = rawAiResponse,
            violationDetected = false,
            violationReason = null
        )
    }

    data class SafetyCheckResult(
        val safeResponse: String,
        val violationDetected: Boolean,
        val violationReason: String?
    )
}
