package com.example.services.ai

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.domain.engine.ScamIndicatorEngine
import com.example.domain.model.CallAction
import com.example.domain.model.RiskLevel
import com.example.services.notifications.CallNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiScamAnalysis(
    val isScam: Boolean,
    val riskLevel: RiskLevel,
    val scamCategory: String,
    val threatScore: Int, // 0 - 100
    val confidence: Float, // 0.0 - 1.0
    val detectedPatterns: List<String>,
    val executiveSummary: String,
    val alertHeadline: String,
    val recommendedAction: CallAction,
    val modelUsed: String = "gemini-3.5-flash",
    val isAIGenerated: Boolean = true,
    val quotaExceeded: Boolean = false,
    val analyzedAt: Long = System.currentTimeMillis()
)

object GeminiTranscriptAnalyzer {

    private const val TAG = "GeminiAnalyzer"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Analyzes full call dialogue or transcript using Gemini API (gemini-3.5-flash).
     * Automatically triggers a real-time notification alert if high-risk scam patterns are found.
     */
    suspend fun analyzeTranscript(
        context: Context,
        phoneNumber: String,
        callerName: String,
        dialogueTurns: List<ScreeningDialogueTurn>,
        triggerNotificationAlert: Boolean = true
    ): GeminiScamAnalysis = withContext(Dispatchers.IO) {
        val transcriptText = dialogueTurns.joinToString("\n") { turn ->
            "${turn.speaker}: ${turn.text}"
        }

        if (transcriptText.isBlank()) {
            return@withContext createFallbackAnalysis(
                transcriptText = "No speech detected",
                phoneNumber = phoneNumber,
                callerName = callerName
            )
        }

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // If API key is missing or placeholder, use intelligent local heuristic fallback
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local ScamIndicatorEngine.")
            val fallback = createFallbackAnalysis(transcriptText, phoneNumber, callerName)
            if (triggerNotificationAlert && (fallback.riskLevel == RiskLevel.HIGH)) {
                CallNotificationManager.notifyHighRiskScamAlert(
                    context = context,
                    phoneNumber = phoneNumber,
                    callerName = callerName,
                    headline = fallback.alertHeadline,
                    scamCategory = fallback.scamCategory,
                    indicators = fallback.detectedPatterns,
                    threatScore = fallback.threatScore
                )
            }
            return@withContext fallback
        }

        // Call Gemini 3.5 Flash REST API
        try {
            val systemPrompt = """
                You are CallShield's advanced Telecom Security and Anti-Fraud Engine.
                Analyze the following incoming phone call transcript for fraud, phishing, urgency manipulation, and scam tactics.
                
                Respond ONLY with a valid JSON object matching this exact schema:
                {
                  "isScam": true,
                  "riskLevel": "HIGH" | "MEDIUM" | "LOW",
                  "scamCategory": "Bank & OTP Phishing" | "Extortion & Arrest Scams" | "Fake Tech Support" | "Predatory Loans & Debt" | "Investment & Crypto Spam" | "Robocall & Lottery" | "Legitimate Call",
                  "threatScore": 0-100,
                  "confidence": 0.0-1.0,
                  "detectedPatterns": ["list", "of", "specific", "threat", "indicators"],
                  "executiveSummary": "1-2 sentence description of what the caller was attempting to do.",
                  "alertHeadline": "Urgent headline for user notification (under 60 chars)",
                  "recommendedAction": "BLOCK" | "AI_SCREEN" | "ALLOW"
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemPrompt\n\n=== CALL TRANSCRIPT ===\n$transcriptText")
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.1)
                    put("responseMimeType", "application/json")
                })
            }

            val endpoint = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val isQuota = response.code == 429 || responseBody.contains("RESOURCE_EXHAUSTED", ignoreCase = true) || responseBody.contains("quota", ignoreCase = true)
                Log.w(TAG, "Gemini API HTTP ${response.code}: $responseBody (quotaExceeded: $isQuota)")
                val fallback = createFallbackAnalysis(transcriptText, phoneNumber, callerName, quotaExceeded = isQuota)

                if (triggerNotificationAlert && (fallback.riskLevel == RiskLevel.HIGH)) {
                    CallNotificationManager.notifyHighRiskScamAlert(
                        context = context,
                        phoneNumber = phoneNumber,
                        callerName = callerName,
                        headline = fallback.alertHeadline,
                        scamCategory = fallback.scamCategory,
                        indicators = fallback.detectedPatterns,
                        threatScore = fallback.threatScore
                    )
                }
                return@withContext fallback
            }

            // Parse response candidates
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            val content = candidates?.optJSONObject(0)?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textOutput = parts?.optJSONObject(0)?.optString("text", "") ?: ""

            if (textOutput.isNotBlank()) {
                val analysisJson = JSONObject(textOutput.trim())
                val isScam = analysisJson.optBoolean("isScam", false)
                val riskLevelStr = analysisJson.optString("riskLevel", "LOW")
                val riskLevel = when (riskLevelStr.uppercase()) {
                    "HIGH", "CRITICAL" -> RiskLevel.HIGH
                    "MEDIUM" -> RiskLevel.MEDIUM
                    else -> RiskLevel.LOW
                }
                val scamCategory = analysisJson.optString("scamCategory", "General Scam")
                val threatScore = analysisJson.optInt("threatScore", if (riskLevel == RiskLevel.HIGH) 94 else 35)
                val confidence = analysisJson.optDouble("confidence", 0.95).toFloat()
                
                val patternsArray = analysisJson.optJSONArray("detectedPatterns")
                val patternsList = mutableListOf<String>()
                if (patternsArray != null) {
                    for (i in 0 until patternsArray.length()) {
                        patternsList.add(patternsArray.getString(i))
                    }
                }

                val summary = analysisJson.optString("executiveSummary", "Screened by Gemini 3.5 Flash")
                val headline = analysisJson.optString("alertHeadline", "Threat Identified by AI")
                val recActionStr = analysisJson.optString("recommendedAction", "BLOCK")
                val action = when (recActionStr.uppercase()) {
                    "BLOCK" -> CallAction.BLOCK
                    "ALLOW" -> CallAction.ALLOW
                    else -> CallAction.AI_SCREEN
                }

                val result = GeminiScamAnalysis(
                    isScam = isScam,
                    riskLevel = riskLevel,
                    scamCategory = scamCategory,
                    threatScore = threatScore,
                    confidence = confidence,
                    detectedPatterns = patternsList,
                    executiveSummary = summary,
                    alertHeadline = headline,
                    recommendedAction = action,
                    modelUsed = MODEL_NAME,
                    isAIGenerated = true,
                    quotaExceeded = false
                )

                // Trigger real-time notification alert if high-risk scam pattern is identified
                if (triggerNotificationAlert && (riskLevel == RiskLevel.HIGH || threatScore >= 75)) {
                    CallNotificationManager.notifyHighRiskScamAlert(
                        context = context,
                        phoneNumber = phoneNumber,
                        callerName = callerName,
                        headline = headline,
                        scamCategory = scamCategory,
                        indicators = patternsList,
                        threatScore = threatScore
                    )
                }

                return@withContext result
            }

            return@withContext createFallbackAnalysis(transcriptText, phoneNumber, callerName)
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking Gemini API: ${e.message}", e)
            val fallback = createFallbackAnalysis(transcriptText, phoneNumber, callerName)
            if (triggerNotificationAlert && (fallback.riskLevel == RiskLevel.HIGH)) {
                CallNotificationManager.notifyHighRiskScamAlert(
                    context = context,
                    phoneNumber = phoneNumber,
                    callerName = callerName,
                    headline = fallback.alertHeadline,
                    scamCategory = fallback.scamCategory,
                    indicators = fallback.detectedPatterns,
                    threatScore = fallback.threatScore
                )
            }
            return@withContext fallback
        }
    }

    /**
     * Resilient on-device fallback analysis when Gemini API is offline,
     * rate-limited (429), or quota is exceeded.
     */
    fun createFallbackAnalysis(
        transcriptText: String,
        phoneNumber: String,
        callerName: String,
        quotaExceeded: Boolean = false
    ): GeminiScamAnalysis {
        val detection = ScamIndicatorEngine.analyzeTranscript(transcriptText)
        val textLower = transcriptText.lowercase()

        val isHighRisk = detection.riskLevel == RiskLevel.HIGH || detection.hasSensitiveRequest
        val category = when {
            textLower.contains("otp") || textLower.contains("bank") || textLower.contains("cvv") || textLower.contains("account") ->
                "Bank & OTP Phishing"
            textLower.contains("police") || textLower.contains("arrest") || textLower.contains("warrant") || textLower.contains("irs") ->
                "Extortion & Arrest Scams"
            textLower.contains("anydesk") || textLower.contains("remote") || textLower.contains("tech support") || textLower.contains("virus") ->
                "Fake Tech Support"
            textLower.contains("loan") || textLower.contains("credit") || textLower.contains("debt") ->
                "Predatory Loans & Debt"
            textLower.contains("invest") || textLower.contains("crypto") || textLower.contains("stocks") ->
                "Investment & Crypto Spam"
            isHighRisk -> "Potential Phishing Scam"
            else -> "Legitimate Inquiry"
        }

        val threatScore = when {
            detection.hasSensitiveRequest -> 98
            isHighRisk -> 92
            detection.detectedIndicators.isNotEmpty() -> 65
            else -> 18
        }

        val headline = if (isHighRisk) {
            "🚨 CRITICAL FRAUD: $category"
        } else {
            "🛡 Screened: Verified Non-Threat"
        }

        val summary = if (isHighRisk) {
            "Caller demonstrated high urgency and attempted to acquire sensitive verification credentials. Flagged for automatic drop."
        } else {
            "Caller provided standard inquiry without suspicious solicitation markers."
        }

        val indicators = detection.detectedIndicators.map { it.title }.toMutableList()
        if (indicators.isEmpty() && isHighRisk) {
            indicators.add("Unsolicited Identity Verification")
        }

        return GeminiScamAnalysis(
            isScam = isHighRisk,
            riskLevel = if (isHighRisk) RiskLevel.HIGH else RiskLevel.LOW,
            scamCategory = category,
            threatScore = threatScore,
            confidence = if (isHighRisk) 0.94f else 0.88f,
            detectedPatterns = indicators,
            executiveSummary = summary,
            alertHeadline = headline,
            recommendedAction = if (isHighRisk) CallAction.BLOCK else CallAction.ALLOW,
            modelUsed = if (quotaExceeded) "Local Engine (Gemini Quota Exceeded)" else "Local Safety Engine",
            isAIGenerated = false,
            quotaExceeded = quotaExceeded
        )
    }
}
