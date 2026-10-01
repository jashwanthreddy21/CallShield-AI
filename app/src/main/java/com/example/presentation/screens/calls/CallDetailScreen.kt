package com.example.presentation.screens.calls

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CallEntity
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.RiskLevel
import com.example.domain.model.RuleMatchType
import com.example.presentation.components.ActionBadge
import com.example.presentation.components.AudioPlayerCard
import com.example.presentation.components.CategoryBadge
import com.example.presentation.components.DirectionBadge
import com.example.presentation.components.RiskBadge
import com.example.presentation.components.ThreatScoreGauge
import com.example.presentation.components.TranscriptBubble
import com.example.presentation.viewmodel.CallShieldViewModel
import com.example.services.ai.ScreeningDialogueTurn
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallDetailScreen(
    callId: Long,
    viewModel: CallShieldViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val call = uiState.allCalls.find { it.id == callId }

    if (call == null) {
        Box(
            modifier = modifier.fillMaxSize().background(CyberNavyDark),
            contentAlignment = Alignment.Center
        ) {
            Text("Call record not found", color = TextSecondary)
        }
        return
    }

    val memory = uiState.callerMemories.find { it.phoneNumber == call.phoneNumber }
    val report = uiState.communityReports.find { it.phoneNumber == call.phoneNumber }
    val timeFormat = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
    val formattedDate = timeFormat.format(Date(call.timestamp))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call Assessment", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val auditReport = """
                                CALLSHIELD AI INCIDENT SECURITY AUDIT
                                Caller: ${call.callerName} (${call.phoneNumber})
                                Direction: ${call.direction.displayName}
                                Action Taken: ${call.action.displayName}
                                AI Category: ${call.category.displayName}
                                Risk Assessment: ${call.riskLevel.displayName} (Threat Score: ${call.threatScore}/100)
                                Purpose: ${call.purpose}
                                Summary: ${call.summary}
                                Timestamp: $formattedDate
                            """.trimIndent()
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, auditReport)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share CallShield Audit"))
                        },
                        modifier = Modifier.testTag("share_audit_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = CyberCyan)
                    }

                    IconButton(
                        onClick = {
                            viewModel.deleteCall(call.id)
                            Toast.makeText(context, "Call record deleted", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        modifier = Modifier.testTag("delete_call_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = SecurityRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberNavyDark)
            )
        },
        containerColor = CyberNavyDark,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("call_detail_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Caller Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("caller_header_card"),
                    colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = call.callerName.ifEmpty { "Unknown Caller" },
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = call.phoneNumber,
                                    color = CyberCyan,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            ActionBadge(action = call.action)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DirectionBadge(direction = call.direction)
                                Text(text = formattedDate, color = TextMuted, fontSize = 12.sp)
                            }

                            // Quick Call Back Button
                            Button(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${call.phoneNumber}"))
                                    context.startActivity(dialIntent)
                                },
                                modifier = Modifier.height(34.dp).testTag("call_back_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SecurityGreen.copy(alpha = 0.4f))
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = SecurityGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call Back", color = SecurityGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Threat Score Index Gauge Card
            item {
                ThreatScoreGauge(
                    score = call.threatScore,
                    riskLevel = call.riskLevel
                )
            }

            // AI Classification & Risk Assessment Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ai_assessment_card"),
                    colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI ASSESSMENT",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Confidence: ${(call.confidenceScore * 100).toInt()}%",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoryBadge(category = call.category)
                            RiskBadge(riskLevel = call.riskLevel)
                        }

                        // Indicators Section
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (call.riskLevel == RiskLevel.HIGH) "⚠ Potential Risk Indicators Detected" else "Detected Context Indicators",
                            color = if (call.riskLevel == RiskLevel.HIGH) SecurityRed else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val indicators = call.detectedIndicators.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        if (indicators.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                indicators.forEach { indicator ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (call.riskLevel == RiskLevel.HIGH) "•" else "✓",
                                            color = if (call.riskLevel == RiskLevel.HIGH) SecurityRed else SecurityGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(text = indicator, color = TextSecondary, fontSize = 13.sp)
                                    }
                                }
                            }
                        } else {
                            Text(text = "No anomalous risk indicators flagged during screening.", color = TextMuted, fontSize = 12.sp)
                        }

                        Text(
                            text = "AI-generated assessment — may contain errors. User retains full control.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // AI Call Summary Card (Section 11)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("call_summary_card"),
                    colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "CALL SUMMARY",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        SummaryField(label = "Purpose", value = call.purpose.ifEmpty { "Commercial promotion / inquiry" })
                        SummaryField(label = "Summary", value = call.summary.ifEmpty { "Screened by CallShield AI assistant and logged." })
                        SummaryField(label = "Action Taken", value = call.action.displayName)
                        SummaryField(label = "Call Duration", value = "${call.durationSeconds} seconds")

                        Text(
                            text = "AI-generated summary — may contain errors.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Caller Memory Section (Section 22)
            if (memory != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("caller_memory_card"),
                        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
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
                                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "CALLER HISTORY & MEMORY",
                                        color = CyberPurple,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "${memory.interactionCount} calls",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = memory.contextNotes,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Community Intelligence Section (Section 24)
            if (report != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("community_report_card"),
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
                                Icon(imageVector = Icons.Default.People, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "COMMUNITY INTELLIGENCE",
                                    color = CyberCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Text(
                                text = "${report.reportCount} community reports for this number.",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Reported Tags: ${report.topTags}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            Text(
                                text = "Community data is user-reported and not verified by authorities.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Quick Rule & Protection Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.addToAllowlist(call.callerName.ifEmpty { "Trusted Contact" }, call.phoneNumber, "Custom", "Added from call history")
                            Toast.makeText(context, "Added to Trusted Allowlist", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("allowlist_action_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SecurityGreen.copy(alpha = 0.4f))
                    ) {
                        Text("Trust Number", color = SecurityGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.addRule(RuleMatchType.EXACT_NUMBER, call.phoneNumber, CallAction.BLOCK, call.category, 10, "Blocked from call detail")
                            Toast.makeText(context, "Created block rule for ${call.phoneNumber}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("block_action_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityRed.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SecurityRed.copy(alpha = 0.4f))
                    ) {
                        Text("Block Number", color = SecurityRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.reportNumber(call.phoneNumber, call.category, "Spam report")
                            Toast.makeText(context, "Reported to community database", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("report_action_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                    ) {
                        Text("Report", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // AI Call Audio Player
            if (call.hasTranscript || call.action == CallAction.AI_SCREEN || call.durationSeconds > 0) {
                item {
                    AudioPlayerCard(
                        callerName = call.callerName.ifEmpty { "CallShield AI Screened Voice" },
                        durationSeconds = if (call.durationSeconds > 0) call.durationSeconds else 42
                    )
                }
            }

            // Transcript Header & Copy Button (Section 12)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONVERSATION TRANSCRIPT",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Call Transcript", call.summary)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("copy_transcript_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", color = CyberCyan, fontSize = 12.sp)
                    }
                }
            }

            // Representative transcript turns
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (call.category == CallCategory.LOANS) {
                            TranscriptBubble(speaker = "AI", text = "Hello. I'm CallShield's automated call-screening assistant. Whom am I speaking with and what is the reason for your call?", timestamp = call.timestamp)
                            TranscriptBubble(speaker = "CALLER", text = "I'm calling from ABC Finance regarding a personal loan offer up to 5 Lakhs.", timestamp = call.timestamp + 4000L)
                            TranscriptBubble(speaker = "AI", text = "Could you briefly explain the purpose of the offer and a callback number?", timestamp = call.timestamp + 9000L)
                            TranscriptBubble(speaker = "CALLER", text = "We're offering zero-fee pre-approved eligibility. You can reach our Mumbai loan desk.", timestamp = call.timestamp + 15000L)
                            TranscriptBubble(speaker = "AI", text = "Thank you. CallShield has documented your offer. The subscriber will be notified.", timestamp = call.timestamp + 22000L)
                        } else if (call.riskLevel == RiskLevel.HIGH) {
                            TranscriptBubble(speaker = "AI", text = "CallShield automated security filter. Please state your identity and verifiable business purpose.", timestamp = call.timestamp)
                            TranscriptBubble(speaker = "CALLER", text = "Your bank account has been blocked due to KYC expiration! I need you to share the 6-digit OTP right now!", timestamp = call.timestamp + 5000L)
                            TranscriptBubble(speaker = "AI", text = "For security reasons, I cannot provide authentication credentials or personal information.", timestamp = call.timestamp + 11000L)
                            TranscriptBubble(speaker = "CALLER", text = "Share the OTP immediately or your account will be permanently frozen!", timestamp = call.timestamp + 16000L)
                            TranscriptBubble(speaker = "AI", text = "Potential risk indicators detected. Authentication credentials will never be shared. This call has been logged and reported.", timestamp = call.timestamp + 21000L)
                        } else {
                            TranscriptBubble(speaker = "AI", text = "Hello. I am CallShield's automated screening assistant.", timestamp = call.timestamp)
                            TranscriptBubble(speaker = "CALLER", text = "Hi, calling to verify your recent service appointment schedule.", timestamp = call.timestamp + 6000L)
                            TranscriptBubble(speaker = "AI", text = "Thank you. Information logged for subscriber review.", timestamp = call.timestamp + 12000L)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SummaryField(label: String, value: String) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}
