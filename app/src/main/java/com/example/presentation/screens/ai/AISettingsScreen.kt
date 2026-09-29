package com.example.presentation.screens.ai

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.AIWaveform
import com.example.presentation.viewmodel.CallShieldUiState
import com.example.services.ai.AIPersonality
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AISettingsScreen(
    state: CallShieldUiState,
    onToggleScreening: (Boolean) -> Unit,
    onSelectPersonality: (AIPersonality) -> Unit,
    onUpdateInstructions: (String) -> Unit,
    onLaunchVoiceDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editableInstructions by remember(state.aiInstructions) { mutableStateOf(state.aiInstructions) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .padding(horizontal = 16.dp)
            .testTag("ai_settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI Call Assistant",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Voice Screening & Intent Detection",
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberNavyCard),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = CyberPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Master Screening Toggle Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Voice Screening",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Interactively asks callers their name, business purpose, and callback number before ringing",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = state.aiScreeningEnabled,
                        onCheckedChange = onToggleScreening,
                        modifier = Modifier.testTag("ai_screening_master_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberNavyDark,
                            checkedTrackColor = CyberCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CyberNavyBorder
                        )
                    )
                }
            }
        }

        // Live Voice Agent Demonstration Card (Section 19)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Text(text = "LIVE VOICE AGENT PIPELINE", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Ready", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Caller Audio → Speech-to-Text → LLM Intent Engine → Safety Layer → TTS Audio",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onLaunchVoiceDemo,
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("test_voice_agent_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CyberNavyDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Launch Live Screening Simulation", color = CyberNavyDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // AI Personality Selection (Section 18)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ASSISTANT PERSONALITY",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                AIPersonality.values().forEach { personality ->
                    val isSelected = state.selectedPersonality == personality
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPersonality(personality) }
                            .testTag("personality_${personality.name}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyberNavySurface else CyberNavyCard
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CyberCyan else CyberNavyBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = personality.title,
                                    color = if (isSelected) CyberCyan else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = personality.description,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${personality.greeting}\"",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberCyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = CyberNavyDark, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // AI Custom Screening Instructions & Templates (Section 17)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ai_instructions_card"),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "CUSTOM SCREENING INSTRUCTIONS",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Tell the AI what to ask and which topics to immediately reject:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    // Preset template pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InstructionPresetPill("Strict") {
                            editableInstructions = "Politely reject all commercial sales and marketing calls immediately. Log their caller ID and callback."
                            onUpdateInstructions(editableInstructions)
                        }
                        InstructionPresetPill("Professional") {
                            editableInstructions = "Ask the caller for their company, purpose, and callback number.\nNever provide passwords or OTPs."
                            onUpdateInstructions(editableInstructions)
                        }
                        InstructionPresetPill("Security-First") {
                            editableInstructions = "Never disclose passwords, OTPs, financial information, or personal data. Immediately end suspected phishing calls."
                            onUpdateInstructions(editableInstructions)
                        }
                    }

                    OutlinedTextField(
                        value = editableInstructions,
                        onValueChange = {
                            editableInstructions = it
                            onUpdateInstructions(it)
                        },
                        minLines = 4,
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CyberNavySurface,
                            unfocusedContainerColor = CyberNavySurface,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberNavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ai_instructions_input")
                    )
                }
            }
        }

        // AI Safety Policy Layer Guarantees (Section 20)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = SecurityGreen, modifier = Modifier.size(16.dp))
                        Text(text = "ZERO-TRUST AI SAFETY POLICY", color = SecurityGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    val safetyRules = listOf(
                        "Never requests OTPs or authentication codes",
                        "Never reveals passwords or sensitive banking data",
                        "Identifies as automated screening — never impersonates the user",
                        "Prohibits financial commitments or contract authorization",
                        "Prevents definitive criminal accusations solely from AI"
                    )

                    safetyRules.forEach { rule ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("✓", color = SecurityGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(rule, color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InstructionPresetPill(title: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberNavySurface)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = title, color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
