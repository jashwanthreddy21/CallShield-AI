package com.example

import com.example.data.local.entity.AllowlistEntity
import com.example.data.local.entity.RuleEntity
import com.example.domain.engine.PhoneNormalizer
import com.example.domain.engine.RuleEngine
import com.example.domain.engine.ScamIndicatorEngine
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.RiskLevel
import com.example.domain.model.RuleMatchType
import com.example.services.ai.AISafetyPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallShieldEngineTest {

    private val ruleEngine = RuleEngine()

    @Test
    fun testPhoneNormalizer_stripsFormattingAndMatchesWildcard() {
        val normalized = PhoneNormalizer.normalize("+91 (91140) 12-345")
        assertEquals("+919114012345", normalized)

        val matchesPrefix = PhoneNormalizer.matchesPattern(normalized, "91140*")
        assertTrue("Expected 91140* to match +919114012345", matchesPrefix)

        val nonMatch = PhoneNormalizer.matchesPattern(normalized, "1800*")
        assertFalse("Expected 1800* NOT to match +919114012345", nonMatch)
    }

    @Test
    fun testRulePrecedence_allowlistBypassesBlockingRule() {
        val incoming = "+91 91140 12345"

        val allowlist = listOf(
            AllowlistEntity(phoneNumber = "+919114012345", contactName = "Trusted Partner", category = "Work")
        )

        val rules = listOf(
            RuleEntity(
                matchType = RuleMatchType.PATTERN,
                pattern = "91140*",
                action = CallAction.BLOCK,
                priority = 10,
                enabled = true
            )
        )

        val result = ruleEngine.evaluate(
            incomingNumber = incoming,
            callerCategory = CallCategory.UNKNOWN,
            allowlist = allowlist,
            activeRules = rules,
            defaultAction = CallAction.ALLOW
        )

        assertEquals("Allowlist must take precedence over pattern block", CallAction.ALLOW, result.action)
        assertTrue(result.precedence.contains("Allowlist"))
    }

    @Test
    fun testRulePrecedence_patternMatchesWhenNoAllowlist() {
        val incoming = "+91 91140 99887"

        val rules = listOf(
            RuleEntity(
                matchType = RuleMatchType.PATTERN,
                pattern = "91140*",
                action = CallAction.AI_SCREEN,
                priority = 8,
                enabled = true
            )
        )

        val result = ruleEngine.evaluate(
            incomingNumber = incoming,
            callerCategory = CallCategory.TELEMARKETING,
            allowlist = emptyList(),
            activeRules = rules,
            defaultAction = CallAction.ALLOW
        )

        assertEquals(CallAction.AI_SCREEN, result.action)
        assertTrue(result.precedence.contains("Pattern Rule"))
    }

    @Test
    fun testScamIndicatorEngine_flagsOtpRequestAsHighRisk() {
        val scamTranscript = "Urgent from bank! Provide your 6 digit OTP immediately to stop suspension."
        val result = ScamIndicatorEngine.analyzeTranscript(scamTranscript)

        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertTrue(result.hasSensitiveRequest)
        assertTrue(result.detectedIndicators.any { it.title.contains("OTP") })
    }

    @Test
    fun testAISafetyPolicy_blocksOtpDisclosure() {
        val rawAiResponse = "Sure, the OTP is 849201 for verification."
        val sanitized = AISafetyPolicy.sanitizeOutput(rawAiResponse, "")

        assertTrue(sanitized.violationDetected)
        assertEquals("For security reasons, I cannot provide authentication credentials or personal information.", sanitized.safeResponse)
    }

    @Test
    fun testAISafetyPolicy_blocksFinancialCommitment() {
        val rawAiResponse = "I agree to pay the fine of 500 dollars right now."
        val sanitized = AISafetyPolicy.sanitizeOutput(rawAiResponse, "")

        assertTrue(sanitized.violationDetected)
        assertEquals("I am an automated assistant and not authorized to make financial commitments on behalf of the user.", sanitized.safeResponse)
    }

    @Test
    fun testScreenNavigation_itemsNotNull() {
        val items = com.example.presentation.navigation.Screen.bottomNavItems
        assertEquals(5, items.size)
        items.forEach { screen ->
            org.junit.Assert.assertNotNull("Screen item must not be null", screen)
            org.junit.Assert.assertNotNull("Screen route must not be null", screen.route)
            assertTrue("Screen route must not be blank", screen.route.isNotBlank())
        }
    }

    @Test
    fun testCallDirection_allValuesHaveDisplayNames() {
        com.example.domain.model.CallDirection.values().forEach { dir ->
            assertTrue(dir.displayName.isNotBlank())
        }
    }
}
