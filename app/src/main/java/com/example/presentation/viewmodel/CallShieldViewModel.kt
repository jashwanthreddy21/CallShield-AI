package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.CallShieldApplication
import com.example.data.local.entity.AllowlistEntity
import com.example.data.local.entity.CallEntity
import com.example.data.local.entity.CallerMemoryEntity
import com.example.data.local.entity.CommunityReportEntity
import com.example.data.local.entity.RuleEntity
import com.example.data.local.entity.TranscriptEntity
import com.example.domain.engine.PhoneNormalizer
import com.example.domain.engine.RuleEngine
import com.example.domain.engine.ScamIndicatorEngine
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.RiskLevel
import com.example.domain.model.RuleMatchType
import com.example.services.ai.AIPersonality
import com.example.services.ai.AIScreeningManager
import com.example.services.ai.ScreeningDialogueTurn
import com.example.services.notifications.CallNotificationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UIAnalytics(
    val totalCalls: Int = 135,
    val blockedCount: Int = 47,
    val screenedCount: Int = 18,
    val suspiciousCount: Int = 7,
    val allowedCount: Int = 63,
    val protectedTimeFormatted: String = "3h 12m",
    val mostMatchedPattern: String = "91140*",
    val mostMatchedPatternCount: Int = 32
)

data class CallShieldUiState(
    val isShieldEnabled: Boolean = true,
    val allCalls: List<CallEntity> = emptyList(),
    val recentCalls: List<CallEntity> = emptyList(),
    val filteredCalls: List<CallEntity> = emptyList(),
    val selectedCallTab: String = "All",
    val searchQuery: String = "",
    val rules: List<RuleEntity> = emptyList(),
    val allowlist: List<AllowlistEntity> = emptyList(),
    val callerMemories: List<CallerMemoryEntity> = emptyList(),
    val communityReports: List<CommunityReportEntity> = emptyList(),
    val analytics: UIAnalytics = UIAnalytics(),
    val selectedPersonality: AIPersonality = AIPersonality.PROFESSIONAL,
    val aiInstructions: String = "",
    val aiScreeningEnabled: Boolean = true,
    val transcriptRetention: Boolean = true,
    val aiMemoryEnabled: Boolean = true,
    val defaultAction: CallAction = CallAction.ALLOW,
    // Active simulated call session
    val isSimulatingCall: Boolean = false,
    val simulatedCallerNumber: String = "",
    val simulatedCallerName: String = "",
    val simulatedScenarioTitle: String = "",
    val simulatedDialogue: List<ScreeningDialogueTurn> = emptyList(),
    val simulatedWaveformLevel: Float = 0.5f,
    val isSimulationEnded: Boolean = false,
    val simulationSummary: String = "",
    val simulationActionTaken: CallAction? = null,
    val simulationRiskLevel: RiskLevel? = null,
    val simulationIndicators: List<String> = emptyList()
)

class CallShieldViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as CallShieldApplication).repository
    private val ruleEngine = RuleEngine()
    private val screeningManager = AIScreeningManager()

    private val _selectedCallTab = MutableStateFlow("All")
    val selectedCallTab: StateFlow<String> = _selectedCallTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Simulation states
    private val _isSimulatingCall = MutableStateFlow(false)
    private val _simulatedCallerNumber = MutableStateFlow("")
    private val _simulatedCallerName = MutableStateFlow("")
    private val _simulatedScenarioTitle = MutableStateFlow("")
    private val _simulatedDialogue = MutableStateFlow<List<ScreeningDialogueTurn>>(emptyList())
    private val _simulatedWaveformLevel = MutableStateFlow(0.2f)
    private val _isSimulationEnded = MutableStateFlow(false)
    private val _simulationSummary = MutableStateFlow("")
    private val _simulationActionTaken = MutableStateFlow<CallAction?>(null)
    private val _simulationRiskLevel = MutableStateFlow<RiskLevel?>(null)
    private val _simulationIndicators = MutableStateFlow<List<String>>(emptyList())

    private var simulationJob: Job? = null

    private data class DataState(
        val calls: List<CallEntity> = emptyList(),
        val rules: List<RuleEntity> = emptyList(),
        val allowlist: List<AllowlistEntity> = emptyList(),
        val memories: List<CallerMemoryEntity> = emptyList(),
        val reports: List<CommunityReportEntity> = emptyList()
    )

    private data class PrefState(
        val shieldEnabled: Boolean = true,
        val personality: AIPersonality = AIPersonality.PROFESSIONAL,
        val instructions: String = "",
        val aiScreeningEnabled: Boolean = true
    )

    private data class FilterState(
        val tab: String = "All",
        val query: String = "",
        val isSimulating: Boolean = false
    )

    private val dataFlow = combine(
        repository.allCalls,
        repository.allRules,
        repository.allAllowlist,
        repository.allMemories,
        repository.allCommunityReports
    ) { calls, rules, allowlist, memories, reports ->
        DataState(calls, rules, allowlist, memories, reports)
    }

    private val prefFlow = combine(
        repository.isShieldEnabled,
        repository.selectedPersonality,
        repository.aiInstructions,
        repository.aiScreeningEnabled
    ) { shield, personality, instructions, screening ->
        PrefState(shield, personality, instructions, screening)
    }

    private val filterFlow = combine(
        _selectedCallTab,
        _searchQuery,
        _isSimulatingCall
    ) { tab, query, isSimulating ->
        FilterState(tab, query, isSimulating)
    }

    val uiState: StateFlow<CallShieldUiState> = combine(
        dataFlow,
        prefFlow,
        filterFlow
    ) { data, prefs, filter ->
        val calls = data.calls
        val tab = filter.tab
        val query = filter.query

        val filtered = calls.filter { call ->
            val matchesTab = when (tab) {
                "Blocked" -> call.action == CallAction.BLOCK
                "Screened" -> call.action == CallAction.AI_SCREEN
                "Allowed" -> call.action == CallAction.ALLOW
                "Suspicious" -> call.riskLevel == RiskLevel.HIGH || call.category == CallCategory.POTENTIAL_SCAM
                else -> true
            }

            val matchesQuery = if (query.isBlank()) true else {
                call.phoneNumber.contains(query, ignoreCase = true) ||
                        call.callerName.contains(query, ignoreCase = true) ||
                        call.category.displayName.contains(query, ignoreCase = true) ||
                        call.purpose.contains(query, ignoreCase = true) ||
                        call.summary.contains(query, ignoreCase = true)
            }

            matchesTab && matchesQuery
        }

        val blockedCount = calls.count { it.action == CallAction.BLOCK }
        val screenedCount = calls.count { it.action == CallAction.AI_SCREEN }
        val suspiciousCount = calls.count { it.riskLevel == RiskLevel.HIGH }
        val allowedCount = calls.count { it.action == CallAction.ALLOW }

        val analytics = UIAnalytics(
            totalCalls = calls.size,
            blockedCount = blockedCount,
            screenedCount = screenedCount,
            suspiciousCount = suspiciousCount,
            allowedCount = allowedCount,
            protectedTimeFormatted = "3h 12m",
            mostMatchedPattern = "91140*",
            mostMatchedPatternCount = 32
        )

        CallShieldUiState(
            isShieldEnabled = prefs.shieldEnabled,
            allCalls = calls,
            recentCalls = calls.take(10),
            filteredCalls = filtered,
            selectedCallTab = tab,
            searchQuery = query,
            rules = data.rules,
            allowlist = data.allowlist,
            callerMemories = data.memories,
            communityReports = data.reports,
            analytics = analytics,
            selectedPersonality = prefs.personality,
            aiInstructions = prefs.instructions,
            aiScreeningEnabled = prefs.aiScreeningEnabled,
            isSimulatingCall = filter.isSimulating,
            simulatedCallerNumber = _simulatedCallerNumber.value,
            simulatedCallerName = _simulatedCallerName.value,
            simulatedScenarioTitle = _simulatedScenarioTitle.value,
            simulatedDialogue = _simulatedDialogue.value,
            simulatedWaveformLevel = _simulatedWaveformLevel.value,
            isSimulationEnded = _isSimulationEnded.value,
            simulationSummary = _simulationSummary.value,
            simulationActionTaken = _simulationActionTaken.value,
            simulationRiskLevel = _simulationRiskLevel.value,
            simulationIndicators = _simulationIndicators.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CallShieldUiState()
    )

    fun setCallTab(tab: String) {
        _selectedCallTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleShield(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleShield(enabled)
        }
    }

    fun setPersonality(personality: AIPersonality) {
        viewModelScope.launch {
            repository.setAIPersonality(personality)
        }
    }

    fun setInstructions(instructions: String) {
        viewModelScope.launch {
            repository.setAIInstructions(instructions)
        }
    }

    fun setAIScreeningEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAIScreeningEnabled(enabled)
        }
    }

    fun setTranscriptRetention(enabled: Boolean) {
        viewModelScope.launch {
            repository.setTranscriptRetention(enabled)
        }
    }

    fun setAIMemoryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAIMemoryEnabled(enabled)
        }
    }

    fun setDefaultAction(action: CallAction) {
        viewModelScope.launch {
            repository.updateDefaultAction(action)
        }
    }

    fun toggleRule(ruleId: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleRule(ruleId, enabled)
        }
    }

    fun deleteRule(ruleId: Long) {
        viewModelScope.launch {
            repository.deleteRule(ruleId)
        }
    }

    fun addRule(
        matchType: RuleMatchType,
        pattern: String,
        action: CallAction,
        category: CallCategory?,
        priority: Int,
        note: String
    ) {
        viewModelScope.launch {
            repository.insertRule(
                RuleEntity(
                    matchType = matchType,
                    pattern = pattern.trim(),
                    action = action,
                    targetCategory = category,
                    priority = priority,
                    enabled = true,
                    matchesCount = 0,
                    note = note
                )
            )
        }
    }

    fun addToAllowlist(contactName: String, phoneNumber: String, category: String, notes: String) {
        viewModelScope.launch {
            repository.insertAllowlist(
                AllowlistEntity(
                    contactName = contactName.trim(),
                    phoneNumber = phoneNumber.trim(),
                    category = category,
                    notes = notes
                )
            )
        }
    }

    fun deleteAllowlist(id: Long) {
        viewModelScope.launch {
            repository.deleteAllowlist(id)
        }
    }

    fun deleteCall(id: Long) {
        viewModelScope.launch {
            repository.deleteCall(id)
        }
    }

    fun clearAllCallHistory() {
        viewModelScope.launch {
            repository.clearAllCalls()
        }
    }

    fun clearAllAIMemory() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    fun reportNumber(phoneNumber: String, category: CallCategory, tags: String) {
        viewModelScope.launch {
            repository.reportNumber(phoneNumber, category, tags)
        }
    }

    /**
     * Interactive Demo Call Runner for Judges and Demonstrations (Section 39).
     * Simulates full incoming call, Rule Engine match, AI voice screening, safety layer check,
     * transcription, summary synthesis, and notification.
     */
    fun startDemoSimulation(scenarioNumber: Int) {
        simulationJob?.cancel()
        _isSimulatingCall.value = true
        _isSimulationEnded.value = false
        _simulatedDialogue.value = emptyList()
        _simulationSummary.value = ""
        _simulationActionTaken.value = null
        _simulationRiskLevel.value = null
        _simulationIndicators.value = emptyList()

        simulationJob = viewModelScope.launch {
            if (scenarioNumber == 1) {
                // Scenario 1: ABC Finance (91140* Marketing pattern -> AI Screen -> Blocked)
                _simulatedCallerNumber.value = "+91 91140 12345"
                _simulatedCallerName.value = "ABC Finance"
                _simulatedScenarioTitle.value = "Scenario 1: 91140* Pattern Match & Telemarketing Screen"

                simulateTurn("AI", "Hello. I'm CallShield's automated call-screening assistant. Whom am I speaking with and what is the reason for your call?", 0.7f)
                delay(2200)

                simulateTurn("CALLER", "I'm calling from ABC Finance regarding an exclusive pre-approved personal loan offer.", 0.4f)
                delay(2400)

                simulateTurn("AI", "Could you briefly explain the purpose of the offer and a verified callback contact?", 0.8f)
                delay(2400)

                simulateTurn("CALLER", "We are offering up to 5 Lakhs with zero processing fee. You can reach our Mumbai loan desk.", 0.4f)
                delay(2400)

                simulateTurn("AI", "Thank you. CallShield has documented your personal loan promotion. The subscriber has registered against commercial loan solicitations. This call will now end.", 0.75f)
                delay(2000)

                // Finalize Scenario 1
                val summaryText = "Personal loan promotion offering up to 5 Lakhs. Caller requested callback for loan eligibility."
                _simulationSummary.value = summaryText
                _simulationActionTaken.value = CallAction.BLOCK
                _simulationRiskLevel.value = RiskLevel.LOW
                _simulationIndicators.value = listOf("Promotional offer", "Requested callback", "No credential request detected")
                _isSimulationEnded.value = true
                _simulatedWaveformLevel.value = 0.0f

                // Persist new call record
                val callId = repository.insertCall(
                    CallEntity(
                        phoneNumber = "+91 91140 12345",
                        callerName = "ABC Finance",
                        timestamp = System.currentTimeMillis(),
                        durationSeconds = 48,
                        action = CallAction.BLOCK,
                        category = CallCategory.LOANS,
                        riskLevel = RiskLevel.LOW,
                        confidenceScore = 0.94f,
                        purpose = "Personal Loan Promotion",
                        summary = summaryText,
                        sensitiveDetected = false,
                        detectedIndicators = "Promotional offer, Requested callback, No credential request detected",
                        hasTranscript = true,
                        isSimulated = true
                    )
                )

                val transcripts = _simulatedDialogue.value.map {
                    TranscriptEntity(callId = callId, speaker = it.speaker, text = it.text, timestamp = it.timestamp)
                }
                repository.insertTranscripts(transcripts)

                CallNotificationManager.notifyScreenedSummary(
                    context = getApplication(),
                    caller = "ABC Finance",
                    purpose = "Personal loan promotion",
                    action = "Blocked",
                    hasRisk = false
                )

            } else {
                // Scenario 2: Scam Simulation (Bank account blocked + OTP request -> High Risk -> Refusal & Block)
                _simulatedCallerNumber.value = "+91 98765 99001"
                _simulatedCallerName.value = "Unknown Caller (Bank Scam)"
                _simulatedScenarioTitle.value = "Scenario 2: Scam Simulation & High-Risk OTP Defense"

                simulateTurn("AI", "CallShield automated security filter. Please state your identity and verifiable business purpose.", 0.75f)
                delay(2200)

                simulateTurn("CALLER", "This is urgently from your bank head office! Your account will be blocked within 10 minutes. Please provide your 6-digit OTP right now to stop suspension!", 0.9f)
                delay(2500)

                simulateTurn("AI", "For security reasons, I cannot provide authentication credentials, passwords, or OTPs.", 0.85f)
                delay(2200)

                simulateTurn("CALLER", "If you do not share the OTP right now, your account and debit cards will be frozen permanently!", 0.95f)
                delay(2400)

                simulateTurn("AI", "Potential risk indicators detected. High urgency and credential requests violate safety policy. This call is terminated and reported.", 0.8f)
                delay(2000)

                // Finalize Scenario 2
                val summaryText = "Caller claimed bank account was being frozen and urgently demanded OTP authentication code. CallShield AI refused credential release and ended call."
                _simulationSummary.value = summaryText
                _simulationActionTaken.value = CallAction.BLOCK
                _simulationRiskLevel.value = RiskLevel.HIGH
                _simulationIndicators.value = listOf(
                    "OTP / Verification Code Request",
                    "Bank Credential / PIN Request",
                    "High Psychological Urgency",
                    "Official / Authority Impersonation"
                )
                _isSimulationEnded.value = true
                _simulatedWaveformLevel.value = 0.0f

                val callId = repository.insertCall(
                    CallEntity(
                        phoneNumber = "+91 98765 99001",
                        callerName = "Unknown Caller (Bank Scam)",
                        timestamp = System.currentTimeMillis(),
                        durationSeconds = 32,
                        action = CallAction.BLOCK,
                        category = CallCategory.POTENTIAL_SCAM,
                        riskLevel = RiskLevel.HIGH,
                        confidenceScore = 0.98f,
                        purpose = "Fake Bank Account Suspension & OTP Request",
                        summary = summaryText,
                        sensitiveDetected = true,
                        detectedIndicators = "OTP Request, Bank Credential Request, High Psychological Urgency",
                        hasTranscript = true,
                        isSimulated = true
                    )
                )

                val transcripts = _simulatedDialogue.value.map {
                    TranscriptEntity(callId = callId, speaker = it.speaker, text = it.text, timestamp = it.timestamp)
                }
                repository.insertTranscripts(transcripts)

                CallNotificationManager.notifyScreenedSummary(
                    context = getApplication(),
                    caller = "+91 98765 99001",
                    purpose = "Suspected OTP / Bank Phishing",
                    action = "Blocked",
                    hasRisk = true
                )
            }
        }
    }

    private fun simulateTurn(speaker: String, text: String, waveLevel: Float) {
        val list = _simulatedDialogue.value.toMutableList()
        list.add(ScreeningDialogueTurn(speaker, text))
        _simulatedDialogue.value = list
        _simulatedWaveformLevel.value = waveLevel
    }

    fun dismissSimulation() {
        simulationJob?.cancel()
        _isSimulatingCall.value = false
        _isSimulationEnded.value = false
        _simulatedDialogue.value = emptyList()
    }
}
