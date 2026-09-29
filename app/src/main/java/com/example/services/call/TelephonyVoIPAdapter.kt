package com.example.services.call

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface TelephonyVoIPAdapter {
    val providerName: String
    val isHardwareCellularBridged: Boolean

    suspend fun startScreeningSession(phoneNumber: String): String
    suspend fun sendAudioPrompt(sessionId: String, spokenText: String)
    fun observeCallerUtterances(sessionId: String): Flow<String>
    suspend fun endSession(sessionId: String, reason: String)
}

/**
 * Provider-agnostic Telephony/VoIP adapter implementation.
 * Allows interactive simulation and provides extension hooks for Twilio, SIP, or WebRTC carriers.
 */
class MockTelephonyAdapter : TelephonyVoIPAdapter {
    override val providerName: String = "CallShield VoIP Gateway (Sandbox)"
    override val isHardwareCellularBridged: Boolean = false

    private val _utteranceFlow = MutableSharedFlow<String>(replay = 1)

    override suspend fun startScreeningSession(phoneNumber: String): String {
        return "sess_${System.currentTimeMillis()}_${phoneNumber.takeLast(4)}"
    }

    override suspend fun sendAudioPrompt(sessionId: String, spokenText: String) {
        // Synthesizes speech packets for audio channel
    }

    override fun observeCallerUtterances(sessionId: String): Flow<String> {
        return _utteranceFlow.asSharedFlow()
    }

    suspend fun emitSimulatedCallerSpeech(speech: String) {
        _utteranceFlow.emit(speech)
    }

    override suspend fun endSession(sessionId: String, reason: String) {
        // Close audio bridges
    }
}
