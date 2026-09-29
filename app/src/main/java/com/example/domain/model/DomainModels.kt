package com.example.domain.model

enum class CallAction(val displayName: String) {
    ALLOW("Allow"),
    BLOCK("Block"),
    SILENCE("Silence"),
    AI_SCREEN("AI Screen")
}

enum class CallCategory(val displayName: String) {
    TELEMARKETING("Telemarketing"),
    INSURANCE("Insurance"),
    LOANS("Personal Loans"),
    SPAM("Spam"),
    POTENTIAL_SCAM("Potential Scam"),
    CUSTOMER_SERVICE("Customer Service"),
    UNKNOWN("Unknown Caller"),
    LEGITIMATE("Legitimate")
}

enum class RiskLevel(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

enum class RuleMatchType(val displayName: String) {
    EXACT_NUMBER("Phone Number"),
    PATTERN("Number Pattern"),
    CATEGORY("Category"),
    UNKNOWN_CALLER("Unknown Caller")
}

enum class AIScreeningState {
    CONNECTING,
    LISTENING,
    THINKING,
    SPEAKING,
    ENDED
}

data class ScamIndicator(
    val id: String,
    val title: String,
    val description: String,
    val severity: RiskLevel
)

data class RuleEvaluationResult(
    val matched: Boolean,
    val ruleId: Long? = null,
    val ruleDescription: String = "",
    val action: CallAction,
    val precedence: String,
    val riskAssessment: RiskLevel = RiskLevel.LOW,
    val reason: String
)
