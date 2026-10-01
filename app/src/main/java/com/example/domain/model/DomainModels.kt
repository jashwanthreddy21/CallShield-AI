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

enum class CallDirection(val displayName: String) {
    INCOMING("Incoming"),
    OUTGOING("Outgoing"),
    MISSED("Missed"),
    BLOCKED("Blocked"),
    AI_SCREENED("AI Screened")
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

data class CallThreatInspectionResult(
    val phoneNumber: String,
    val threatScore: Int,
    val riskLevel: RiskLevel,
    val matchedRuleName: String? = null,
    val isAllowlisted: Boolean = false,
    val communityReportCount: Int = 0,
    val detectedKeywords: List<String> = emptyList(),
    val recommendation: String = ""
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
