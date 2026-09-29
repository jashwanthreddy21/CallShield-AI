package com.example.presentation.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.AIWaveform
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(1) }

    // Screen 5 preferences state
    var optBlock by remember { mutableStateOf(true) }
    var optScreen by remember { mutableStateOf(true) }
    var optSilence by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .padding(24.dp)
            .testTag("onboarding_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Step Progress Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (step in 1..5) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(4.dp)
                            .width(if (step == currentStep) 32.dp else 12.dp)
                            .clip(CircleShape)
                            .background(if (step <= currentStep) CyberCyan else CyberNavyBorder)
                    )
                }
            }

            // Step Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    1 -> WelcomeStep()
                    2 -> ProtectionStep()
                    3 -> AIScreeningStep()
                    4 -> PermissionsStep()
                    else -> PreferencesStep(
                        optBlock = optBlock,
                        onOptBlockToggle = { optBlock = !optBlock },
                        optScreen = optScreen,
                        onOptScreenToggle = { optScreen = !optScreen },
                        optSilence = optSilence,
                        onOptSilenceToggle = { optSilence = !optSilence }
                    )
                }
            }

            // Bottom CTA Button
            Button(
                onClick = {
                    if (currentStep < 5) {
                        currentStep++
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_cta_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = when (currentStep) {
                        1 -> "Get Started"
                        2 -> "Continue"
                        3 -> "Set Up AI"
                        4 -> "Grant & Continue"
                        else -> "Enter CallShield AI"
                    },
                    color = CyberNavyDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(CyberNavyCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(54.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "CallShield AI",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your Intelligent Call Firewall",
            color = CyberCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Block unwanted calls, screen suspicious callers, and let an AI voice agent handle supported conversations before your phone rings.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ProtectionStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Full-Spectrum Call Protection",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureItem(title = "Block Unwanted Callers", desc = "Filter spam series, robocallers, and aggressive loan agencies.")
            FeatureItem(title = "Detect Suspicious Patterns", desc = "Match prefix patterns like 91140* and known fraud headers.")
            FeatureItem(title = "Screen Supported Calls", desc = "Let the AI assistant answer and clarify caller intent.")
            FeatureItem(title = "Protect Personal Information", desc = "Zero-trust policy refuses OTP or credential extraction.")
        }
    }
}

@Composable
private fun AIScreeningStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = "Intelligent AI Voice Screening",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Let your AI assistant ask callers why they're calling before you answer.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        AIWaveform(level = 0.85f, isSpeaking = true)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "“Hello. I am CallShield's automated screening assistant. Whom am I speaking with and what is the reason for your call?”", color = CyberCyan, fontSize = 13.sp, lineHeight = 18.sp)
                Text(text = "Caller responses are transcribed, summarized, and classified.", color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun PermissionsStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Transparent Permissions",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We request only platform permissions strictly necessary to screen calls and protect your device.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PermissionExplanationCard(icon = Icons.Default.Phone, title = "Call Screening Service", desc = "Allows the app to evaluate incoming callers against your local rules.")
            PermissionExplanationCard(icon = Icons.Default.Notifications, title = "Notifications", desc = "Sends immediate alerts when suspicious calls or AI summaries are ready.")
            PermissionExplanationCard(icon = Icons.Default.Security, title = "Microphone (Optional)", desc = "Only active when you test or run voice agent screening.")
        }
    }
}

@Composable
private fun PreferencesStep(
    optBlock: Boolean,
    onOptBlockToggle: () -> Unit,
    optScreen: Boolean,
    onOptScreenToggle: () -> Unit,
    optSilence: Boolean,
    onOptSilenceToggle: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Protection Preferences",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "How should CallShield handle unwanted callers?",
            color = CyberCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PreferenceChoiceCard(title = "Block automatically", desc = "Disallow known telemarketers and high-risk callers", selected = optBlock, onClick = onOptBlockToggle)
            PreferenceChoiceCard(title = "AI screen suspicious calls", desc = "Ask unknown callers their intent before ringing", selected = optScreen, onClick = onOptScreenToggle)
            PreferenceChoiceCard(title = "Silence automatically", desc = "Mute ringer for flagged marketing numbers", selected = optSilence, onClick = onOptSilenceToggle)
        }
    }
}

@Composable
private fun FeatureItem(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberNavyCard)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CyberNavySurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SecurityGreen, modifier = Modifier.size(18.dp))
        }

        Column {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PermissionExplanationCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = desc, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
private fun PreferenceChoiceCard(
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (selected) CyberNavySurface else CyberNavyCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) CyberCyan else CyberNavyBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = if (selected) CyberCyan else TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = desc, color = TextSecondary, fontSize = 12.sp)
            }
            if (selected) {
                Box(
                    modifier = Modifier.size(22.dp).clip(CircleShape).background(CyberCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = CyberNavyDark, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
