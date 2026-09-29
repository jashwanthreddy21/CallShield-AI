package com.example.services.ai

import com.example.domain.engine.ScamIndicatorEngine
import com.example.domain.model.AIScreeningState
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.RiskLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ScreeningDialogueTurn(
    val speaker: String, // "AI" or "CALLER"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ScreeningResult(
    val category: CallCategory,
    val purpose: String,
    val riskLevel: RiskLevel,
    val confidence: Float,
    val sensitiveRequestDetected: Boolean,
    val summary: String,
    val recommendedAction: CallAction,
    val detectedIndicators: List<String>,
    val durationSeconds: Int,
    val transcripts: List<ScreeningDialogueTurn>
)

class AIScreeningManager {

    private val _screeningState = MutableStateFlow(AIScreeningState.CONNECTING)
    val screeningState: StateFlow<AIScreeningState> = _screeningState.asStateFlow()

    private val _dialogue = MutableStateFlow<List<ScreeningDialogueTurn>>(emptyList())
    val dialogue: StateFlow<List<ScreeningDialogueTurn>> = _dialogue.asStateFlow()

    private val _currentAIUtterance = MutableStateFlow("")
    val currentAIUtterance: StateFlow<String> = _currentAIUtterance.asStateFlow()

    fun resetSession() {
        _screeningState.value = AIScreeningState.CONNECTING
        _dialogue.value = emptyList()
        _currentAIUtterance.value = ""
    }

    /**
     * Executes the screening conversation turn through the full pipeline:
     * Audio/STT -> Conversation Manager -> LLM/Policy -> Safety Layer -> TTS
     */
    suspend fun processCallerUtterance(
        callerUtterance: String,
        personality: AIPersonality,
        userInstructions: String
    ): String {
        // Record caller turn
        addTurn("CALLER", callerUtterance)

        _screeningState.value = AIScreeningState.THINKING
        delay(900) // Realistic reasoning delay

        // Analyze for scam / sensitive indicators
        val detection = ScamIndicatorEngine.analyzeTranscript(callerUtterance)

        val rawResponse = when {
            detection.hasSensitiveRequest -> {
                "For security reasons, I cannot provide authentication credentials or personal information."
            }
            callerUtterance.lowercase().contains("loan") || callerUtterance.lowercase().contains("credit card") -> {
                "Thank you for the information. The subscriber has registered a preference against unsolicited credit and loan offers. Your request has been logged."
            }
            callerUtterance.lowercase().contains("insurance") -> {
                "Understood. CallShield has documented your insurance policy inquiry. The subscriber will review your details if interested."
            }
            callerUtterance.lowercase().contains("account") || callerUtterance.lowercase().contains("bank") -> {
                "CallShield security policy requires direct verification via official banking channels. Please state your official branch and reference number."
            }
            else -> {
                when (personality) {
                    AIPersonality.SECURITY_FOCUSED -> "Identity logged. Please provide your business registration number and verified callback contact."
                    AIPersonality.BRIEF -> "Noted. What is your callback number?"
                    AIPersonality.FRIENDLY -> "Thank you for explaining! I'll make sure the subscriber gets this summary."
                    AIPersonality.FORMAL -> "Your statement has been documented into the subscriber's secure screening registry."
                    else -> "Thank you. Could you briefly confirm your organization and a verified callback number?"
                }
            }
        }

        // Run through AI Safety Layer
        val safetyResult = AISafetyPolicy.sanitizeOutput(rawResponse, userInstructions)
        val finalResponse = safetyResult.safeResponse

        _screeningState.value = AIScreeningState.SPEAKING
        _currentAIUtterance.value = finalResponse
        addTurn("AI", finalResponse)

        delay(1400) // Speech duration
        _screeningState.value = AIScreeningState.LISTENING

        return finalResponse
    }

    private fun addTurn(speaker: String, text: String) {
        val list = _dialogue.value.toMutableList()
        list.add(ScreeningDialogueTurn(speaker, text))
        _dialogue.value = list
    }

    /**
     * Synthesizes final AI assessment, summary, and action recommendation
     */
    fun finalizeScreening(durationSeconds: Int): ScreeningResult {
        _screeningState.value = AIScreeningState.ENDED
        val allTurns = _dialogue.value
        val fullTranscript = allTurns.joinToString(" ") { "${it.speaker}: ${it.text}" }

        val detection = ScamIndicatorEngine.analyzeTranscript(fullTranscript)

        val category = when {
            detection.riskLevel == RiskLevel.HIGH -> CallCategory.POTENTIAL_SCAM
            fullTranscript.lowercase().contains("loan") -> CallCategory.LOANS
            fullTranscript.lowercase().contains("insurance") -> CallCategory.INSURANCE
            fullTranscript.lowercase().contains("offer") || fullTranscript.lowercase().contains("marketing") -> CallCategory.TELEMARKETING
            fullTranscript.lowercase().contains("customer service") || fullTranscript.lowercase().contains("support") -> CallCategory.CUSTOMER_SERVICE
            else -> CallCategory.UNKNOWN
        }

        val recommendedAction = when (category) {
            CallCategory.POTENTIAL_SCAM -> CallAction.BLOCK
            CallCategory.LOANS, CallCategory.INSURANCE, CallCategory.TELEMARKETING -> CallAction.BLOCK
            CallCategory.CUSTOMER_SERVICE -> CallAction.ALLOW
            else -> CallAction.AI_SCREEN
        }

        val purpose = when (category) {
            CallCategory.POTENTIAL_SCAM -> "Suspected credential or payment phishing attempt"
            CallCategory.LOANS -> "Personal loan or pre-approved credit marketing"
            CallCategory.INSURANCE -> "Health or vehicle insurance renewal promotion"
            CallCategory.TELEMARKETING -> "Commercial telemarketing offer"
            CallCategory.CUSTOMER_SERVICE -> "Account service or delivery inquiry"
            else -> "Unspecified inquiry"
        }

        val summary = when (category) {
            CallCategory.POTENTIAL_SCAM -> "Caller requested sensitive credentials or manufactured high urgency. Automatically screened and flagged for blocking."
            CallCategory.LOANS -> "Caller offered a pre-approved personal loan promotion and requested callback."
            CallCategory.INSURANCE -> "Caller inquired regarding insurance policy terms."
            else -> "Caller screened by CallShield assistant. Full transcript recorded for subscriber review."
        }

        return ScreeningResult(
            category = category,
            purpose = purpose,
            riskLevel = detection.riskLevel,
            confidence = if (detection.riskLevel == RiskLevel.HIGH) 0.96f else 0.88f,
            sensitiveRequestDetected = detection.hasSensitiveRequest,
            summary = summary,
            recommendedAction = recommendedAction,
            detectedIndicators = detection.detectedIndicators.map { it.title },
            durationSeconds = durationSeconds,
            transcripts = allTurns
        )
    }
}
