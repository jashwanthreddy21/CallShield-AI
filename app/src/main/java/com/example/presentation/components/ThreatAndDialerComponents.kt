package com.example.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.CallDirection
import com.example.domain.model.CallThreatInspectionResult
import com.example.domain.model.RiskLevel
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityOrange
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun DirectionBadge(direction: CallDirection, modifier: Modifier = Modifier) {
    val (label, icon, color) = when (direction) {
        CallDirection.INCOMING -> Triple("Incoming", Icons.AutoMirrored.Filled.CallReceived, CyberCyan)
        CallDirection.OUTGOING -> Triple("Outgoing", Icons.AutoMirrored.Filled.CallMade, SecurityGreen)
        CallDirection.MISSED -> Triple("Missed", Icons.AutoMirrored.Filled.CallMissed, SecurityRed)
        CallDirection.BLOCKED -> Triple("Blocked", Icons.Default.Block, SecurityRed)
        CallDirection.AI_SCREENED -> Triple("AI Screened", Icons.Default.RecordVoiceOver, CyberPurple)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .testTag("direction_badge_${direction.name}")
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(11.dp))
        Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ThreatScoreGauge(
    score: Int,
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val (accentColor, ratingLabel) = when {
        score >= 70 -> Pair(SecurityRed, "HIGH THREAT")
        score >= 35 -> Pair(SecurityOrange, "MODERATE RISK")
        else -> Pair(SecurityGreen, "LOW RISK / VERIFIED")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("threat_score_gauge"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    Text(text = "CALL THREAT INDEX", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = ratingLabel, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$score / 100",
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (score >= 70) "Scam/Phishing signals detected" else "Standard reputation",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            LinearProgressIndicator(
                progress = { (score / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = CyberNavySurface
            )
        }
    }
}

@Composable
fun AudioPlayerCard(
    callerName: String,
    durationSeconds: Int = 42,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableFloatStateOf(0f) }
    var speed by remember { mutableStateOf("1.0x") }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(500)
            currentPosition += 0.5f
            if (currentPosition >= durationSeconds) {
                currentPosition = 0f
                isPlaying = false
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("audio_player_card"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Text(text = "AI CALL SCREENING AUDIO RECORDING", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberNavySurface)
                        .clickable {
                            speed = when (speed) {
                                "1.0x" -> "1.5x"
                                "1.5x" -> "2.0x"
                                else -> "1.0x"
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = speed, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Waveform visualizer
            AIWaveform(
                level = if (isPlaying) 0.8f else 0.15f,
                isSpeaking = isPlaying
            )

            // Progress Slider
            Slider(
                value = currentPosition,
                onValueChange = { currentPosition = it },
                valueRange = 0f..durationSeconds.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = CyberCyan,
                    activeTrackColor = CyberCyan,
                    inactiveTrackColor = CyberNavySurface
                ),
                modifier = Modifier.fillMaxWidth().height(20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentSec = currentPosition.toInt()
                val currentMin = currentSec / 60
                val remSec = currentSec % 60
                val totalMin = durationSeconds / 60
                val totalRemSec = durationSeconds % 60

                Text(
                    text = String.format("%02d:%02d / %02d:%02d", currentMin, remSec, totalMin, totalRemSec),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CyberCyan)
                        .testTag("play_audio_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = CyberNavyDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DialerDialog(
    initialNumber: String = "",
    onDismiss: () -> Unit,
    onInspectNumber: (String) -> CallThreatInspectionResult,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dialedNumber by remember { mutableStateOf(initialNumber) }
    var inspectionResult by remember { mutableStateOf<CallThreatInspectionResult?>(null) }

    LaunchedEffect(dialedNumber) {
        if (dialedNumber.length >= 3) {
            inspectionResult = onInspectNumber(dialedNumber)
        } else {
            inspectionResult = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(22.dp))
                .background(CyberNavyDark)
                .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                .padding(18.dp)
                .testTag("dialer_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Text(text = "SECURE CALLSHIELD DIALER", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp).testTag("close_dialer")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // Dialed Number Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberNavySurface)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (dialedNumber.isEmpty()) "Enter phone number" else dialedNumber,
                        color = if (dialedNumber.isEmpty()) TextMuted else TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }

                // Live Threat Inspection Pill
                inspectionResult?.let { inspect ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (inspect.riskLevel == RiskLevel.HIGH) SecurityRed.copy(alpha = 0.15f)
                                else if (inspect.isAllowlisted) SecurityGreen.copy(alpha = 0.15f)
                                else CyberNavyCard
                            )
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (inspect.riskLevel == RiskLevel.HIGH) Icons.Default.Warning else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (inspect.riskLevel == RiskLevel.HIGH) SecurityRed else if (inspect.isAllowlisted) SecurityGreen else CyberCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = inspect.recommendation,
                                color = if (inspect.riskLevel == RiskLevel.HIGH) SecurityRed else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Dialpad Grid (3x4)
                val keypadButtons = listOf(
                    listOf("1" to "", "2" to "ABC", "3" to "DEF"),
                    listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                    listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                    listOf("*" to "", "0" to "+", "#" to "")
                )

                keypadButtons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { (digit, sub) ->
                            DialpadKey(
                                digit = digit,
                                subtext = sub,
                                onClick = { dialedNumber += digit },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Bottom actions: Backspace & Dial
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(54.dp))

                    // Green Call Button
                    Button(
                        onClick = {
                            if (dialedNumber.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$dialedNumber"))
                                context.startActivity(intent)
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("dialer_call_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen),
                        shape = CircleShape
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Dial", tint = CyberNavyDark, modifier = Modifier.size(24.dp))
                    }

                    // Backspace Button
                    IconButton(
                        onClick = {
                            if (dialedNumber.isNotEmpty()) {
                                dialedNumber = dialedNumber.dropLast(1)
                            }
                        },
                        modifier = Modifier.size(54.dp).testTag("dialer_backspace")
                    ) {
                        Icon(imageVector = Icons.Default.Backspace, contentDescription = "Backspace", tint = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialpadKey(
    digit: String,
    subtext: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(52.dp)
            .clickable { onClick() }
            .testTag("dialpad_key_$digit"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = digit, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (subtext.isNotEmpty()) {
                Text(text = subtext, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
