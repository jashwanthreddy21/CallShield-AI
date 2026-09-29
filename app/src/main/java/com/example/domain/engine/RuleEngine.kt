package com.example.domain.engine

import com.example.data.local.entity.AllowlistEntity
import com.example.data.local.entity.RuleEntity
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.RiskLevel
import com.example.domain.model.RuleEvaluationResult
import com.example.domain.model.RuleMatchType

class RuleEngine {

    /**
     * Evaluates incoming caller against configured rules and allowlist.
     * Follows deterministic precedence:
     * 1. Explicit Allowlist (Always bypasses blocking rules)
     * 2. Explicit Number Rule (Exact phone number)
     * 3. High-priority Pattern Rule (e.g., 91140*, 1800*)
     * 4. Category Rule (e.g. Telemarketing, Loans)
     * 5. Unknown Caller Rule
     * 6. Default Behavior
     */
    fun evaluate(
        incomingNumber: String,
        callerCategory: CallCategory = CallCategory.UNKNOWN,
        allowlist: List<AllowlistEntity>,
        activeRules: List<RuleEntity>,
        defaultAction: CallAction = CallAction.ALLOW
    ): RuleEvaluationResult {
        val normalizedIncoming = PhoneNormalizer.normalize(incomingNumber)

        // 1. Explicit Allowlist Check
        val allowlistMatch = allowlist.firstOrNull {
            PhoneNormalizer.normalize(it.phoneNumber) == normalizedIncoming ||
                    PhoneNormalizer.normalize(it.phoneNumber).removePrefix("+") == normalizedIncoming.removePrefix("+")
        }

        if (allowlistMatch != null) {
            return RuleEvaluationResult(
                matched = true,
                action = CallAction.ALLOW,
                precedence = "1. Explicit Allowlist",
                ruleDescription = "Trusted Contact (${allowlistMatch.contactName} - ${allowlistMatch.category})",
                riskAssessment = RiskLevel.LOW,
                reason = "Caller is in your trusted allowlist. All blocking rules bypassed."
            )
        }

        // Sort rules by priority descending
        val sortedRules = activeRules.sortedByDescending { it.priority }

        // 2. Explicit Number Rule
        val exactNumberRule = sortedRules.firstOrNull { rule ->
            rule.matchType == RuleMatchType.EXACT_NUMBER &&
                    PhoneNormalizer.matchesPattern(normalizedIncoming, rule.pattern)
        }

        if (exactNumberRule != null) {
            return RuleEvaluationResult(
                matched = true,
                ruleId = exactNumberRule.id,
                action = exactNumberRule.action,
                precedence = "2. Explicit Number Rule",
                ruleDescription = "Exact match for ${exactNumberRule.pattern}",
                riskAssessment = if (exactNumberRule.action == CallAction.BLOCK) RiskLevel.HIGH else RiskLevel.LOW,
                reason = "Matched specific number rule for ${exactNumberRule.pattern}."
            )
        }

        // 3. High-priority Pattern Rule (e.g. 91140*, +9191140*)
        val patternRule = sortedRules.firstOrNull { rule ->
            rule.matchType == RuleMatchType.PATTERN &&
                    PhoneNormalizer.matchesPattern(normalizedIncoming, rule.pattern)
        }

        if (patternRule != null) {
            return RuleEvaluationResult(
                matched = true,
                ruleId = patternRule.id,
                action = patternRule.action,
                precedence = "3. Pattern Rule",
                ruleDescription = "Pattern match (${patternRule.pattern})",
                riskAssessment = if (patternRule.action == CallAction.BLOCK) RiskLevel.HIGH else RiskLevel.MEDIUM,
                reason = "Number starts with prefix pattern ${patternRule.pattern}."
            )
        }

        // 4. Category Rule
        if (callerCategory != CallCategory.UNKNOWN) {
            val categoryRule = sortedRules.firstOrNull { rule ->
                rule.matchType == RuleMatchType.CATEGORY &&
                        rule.targetCategory == callerCategory
            }

            if (categoryRule != null) {
                return RuleEvaluationResult(
                    matched = true,
                    ruleId = categoryRule.id,
                    action = categoryRule.action,
                    precedence = "4. Category Rule",
                    ruleDescription = "Category rule for ${callerCategory.displayName}",
                    riskAssessment = if (callerCategory == CallCategory.POTENTIAL_SCAM) RiskLevel.HIGH else RiskLevel.MEDIUM,
                    reason = "Caller matches category '${callerCategory.displayName}'."
                )
            }
        }

        // 5. Unknown Caller Rule
        val unknownCallerRule = sortedRules.firstOrNull { rule ->
            rule.matchType == RuleMatchType.UNKNOWN_CALLER
        }

        if (unknownCallerRule != null) {
            return RuleEvaluationResult(
                matched = true,
                ruleId = unknownCallerRule.id,
                action = unknownCallerRule.action,
                precedence = "5. Unknown Caller Rule",
                ruleDescription = "General Unknown Caller Protection",
                riskAssessment = RiskLevel.MEDIUM,
                reason = "Number not recognized in your contacts or allowlist."
            )
        }

        // 6. Default Fallback
        return RuleEvaluationResult(
            matched = false,
            action = defaultAction,
            precedence = "6. Default Behavior",
            ruleDescription = "Standard Protection Policy",
            riskAssessment = RiskLevel.LOW,
            reason = "No custom rules triggered. Applied default device protection policy."
        )
    }
}
