# CallShield AI 🛡
> **“Block unwanted calls. Let AI handle the conversation.”**

CallShield AI is an intelligent mobile call-management and AI call-screening platform built natively for Android using **Kotlin**, **Jetpack Compose**, and **Material 3**. It functions as an intelligent **AI Call Firewall**, enforcing customizable rule hierarchies, pattern-based matching (e.g., `91140*`), automated scam and credential phishing detection, and conversational AI voice screening.

---

## 🌟 Key Features

1. **Native Call Screening Integration**:
   - Integrates with Android's `android.telecom.CallScreeningService` to intercept incoming calls at the OS level.
   - Decoupled VoIP/Telephony abstraction layer (`TelephonyVoIPAdapter`) separating cellular OS restrictions from VoIP/SIP/WebRTC screening channels.

2. **Deterministic Rule Engine with Strict Precedence**:
   - Tier 1: **Explicit Allowlist** (Trusted family, work, bank contacts always bypass blocking rules)
   - Tier 2: **Explicit Number Rule** (Exact phone number match)
   - Tier 3: **Pattern Rules** (Wildcard prefix matching like `91140*`, `1800*`, `140*`)
   - Tier 4: **Category Rules** (Telemarketing, Loans, Insurance, Potential Scam)
   - Tier 5: **Unknown Caller Rule**
   - Tier 6: **Default Fallback Action** (Allow / AI Screen / Silence / Block)

3. **AI Voice Agent & Safety Layer**:
   - Pipeline: `Caller Audio → Speech-to-Text → LLM Intent Engine → Safety & Policy Layer → Response Generator → Text-to-Speech`.
   - **Zero-Trust AI Safety Policy**:
     - Never requests or discloses OTPs or passwords.
     - Never agrees to contracts or financial commitments.
     - Identifies as an automated screening assistant (no human impersonation).
     - Prohibits definitive accusations of criminal behavior.

4. **Scam Indicator Engine**:
   - Real-time heuristic and semantic threat analysis detecting:
     - OTP & Verification Code queries
     - Bank credential / PIN phishing
     - Remote access software requests (AnyDesk, TeamViewer)
     - High psychological urgency & authority impersonation
     - Unsolicited lottery / prize claims

5. **AI Call Summaries & Transcripts**:
   - Structured AI assessments with confidence scores and purpose extraction.
   - Full conversation transcripts with timestamped speaker turns, search, and copy-to-clipboard.
   - Clear legal disclaimer: *"AI-generated assessment — may contain errors."*

6. **Caller Memory & Community Intelligence**:
   - Tracks historical interactions per number to provide context on repeat callers.
   - Community-reported reputation data with privacy-safe aggregation.

7. **Interactive Hackathon Demo Runner**:
   - **Scenario 1**: `91140*` Pattern Match & Telemarketing Screen (ABC Finance loan offer).
   - **Scenario 2**: High-Risk Bank Scam Phishing Defense (Account freeze claim & OTP demand blocked).

---

## 📐 Architecture

```
com.example
├── CallShieldApplication.kt
├── MainActivity.kt
├── data
│   ├── local
│   │   ├── CallShieldDatabase.kt
│   │   ├── dao (CallDao, RuleDao, AllowlistDao, TranscriptDao, CallerMemoryDao, CommunityReportDao)
│   │   └── entity (CallEntity, RuleEntity, AllowlistEntity, TranscriptEntity, CallerMemoryEntity, CommunityReportEntity)
│   └── repository (CallShieldRepository.kt)
├── domain
│   ├── engine
│   │   ├── PhoneNormalizer.kt
│   │   ├── RuleEngine.kt
│   │   └── ScamIndicatorEngine.kt
│   └── model (CallAction, CallCategory, RiskLevel, RuleMatchType, AIScreeningState, DomainModels.kt)
├── presentation
│   ├── components (ShieldStatusCard, CallCard, ActionBadge, RiskBadge, AIWaveform, RuleCard, TranscriptBubble, DemoCallDialog)
│   ├── navigation (Screen.kt, BottomBar.kt)
│   ├── screens
│   │   ├── home (HomeScreen.kt)
│   │   ├── calls (CallsScreen.kt, CallDetailScreen.kt)
│   │   ├── rules (RulesScreen.kt)
│   │   ├── ai (AISettingsScreen.kt)
│   │   ├── settings (SettingsScreen.kt)
│   │   └── onboarding (OnboardingScreen.kt)
│   └── viewmodel (CallShieldViewModel.kt)
├── services
│   ├── ai (AISafetyPolicy.kt, AIPersonality.kt, AIScreeningManager.kt)
│   ├── call (CallScreeningServiceImpl.kt, TelephonyVoIPAdapter.kt)
│   └── notifications (CallNotificationManager.kt)
└── ui.theme (Color.kt, Theme.kt, Type.kt)
```

---

## 🚀 Running the App

### Requirements:
- Android Studio Ladybug / Meerkat or Gradle 8.11+
- Android SDK 35+ (Compile SDK: 36, Min SDK: 24)
- JDK 17 / 21

### Build & Run Unit Tests:
```bash
gradle :app:testDebugUnitTest
```
