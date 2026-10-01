package com.example.presentation.screens.home

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.BlockedScamSummaryWidget
import com.example.presentation.components.CallCard
import com.example.presentation.components.PhoneProtectionBanner
import com.example.presentation.components.RoomCallHistorySummaryCard
import com.example.presentation.components.ShieldStatusCard
import com.example.presentation.components.StatCard
import com.example.presentation.viewmodel.CallShieldUiState
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

@Composable
fun HomeScreen(
    state: CallShieldUiState,
    onToggleProtection: (Boolean) -> Unit,
    onCallClick: (Long) -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToCalls: (String) -> Unit,
    onRunDemoScenario1: () -> Unit,
    onRunDemoScenario2: () -> Unit,
    onOpenDialer: () -> Unit,
    onShareApk: () -> Unit = {},
    onDismissGeminiAlert: () -> Unit = {},
    onQuickBlockNumber: (String, String) -> Unit = { _, _ -> },
    onTestScreening: (String, String, Boolean) -> Unit = { _, _, _ -> },
    onRefreshPermissions: () -> Unit = {},
    onClearHistoryLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good afternoon, Jashwanth",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (state.isShieldEnabled) "Your phone is protected" else "Firewall is currently paused",
                        color = if (state.isShieldEnabled) SecurityGreen else SecurityOrange,
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
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield badge",
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Android Phone Telephony Permissions & Call Screening Setup Banner
        item {
            PhoneProtectionBanner(
                onPermissionsUpdated = onRefreshPermissions,
                onTestScreening = onTestScreening
            )
        }

        // Room Database Call History Logs & AI Screening Summaries Card
        item {
            RoomCallHistorySummaryCard(
                historyLogs = state.historyLogs,
                onViewAll = { onNavigateToCalls("All") },
                onClearLogs = onClearHistoryLogs
            )
        }

        // Real-time Gemini API Scam Pattern Alert Banner
        state.geminiScamAnalysis?.let { analysis ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_realtime_alert_banner"),
                    colors = CardDefaults.cardColors(containerColor = SecurityRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SecurityRed)
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SecurityRed.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = SecurityRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "GEMINI 3.5 FLASH SCAM ALERT",
                                            color = SecurityRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SecurityRed)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "${analysis.threatScore}/100 Threat",
                                                color = TextPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = analysis.scamCategory,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            androidx.compose.material3.IconButton(
                                onClick = onDismissGeminiAlert,
                                modifier = Modifier.size(28.dp).testTag("dismiss_gemini_alert_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = analysis.alertHeadline,
                            color = SecurityOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = analysis.executiveSummary,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        if (analysis.detectedPatterns.isNotEmpty()) {
                            Text(
                                text = "Identified High-Risk Scam Patterns:",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                analysis.detectedPatterns.forEach { pattern ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("🚨", fontSize = 11.sp)
                                        Text(text = pattern, color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val phone = state.simulatedCallerNumber.ifBlank { "+91 98765 99001" }
                                    onQuickBlockNumber(phone, analysis.scamCategory)
                                    onDismissGeminiAlert()
                                },
                                modifier = Modifier.weight(1f).height(38.dp).testTag("alert_block_now_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SecurityRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add to Blocked List", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onDismissGeminiAlert,
                                modifier = Modifier.weight(0.7f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Acknowledge", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Hero Shield Status Card
        item {
            ShieldStatusCard(
                isProtected = state.isShieldEnabled,
                onToggleProtection = onToggleProtection
            )
        }

        // Hackathon Demo Bar (One-Tap Scenarios)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("demo_scenarios_card"),
                colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Text(
                            text = "HACKATHON DEMO RUNNER",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Simulate full end-to-end incoming call screening with AI voice pipeline and scam detection:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRunDemoScenario1,
                            modifier = Modifier.weight(1f).height(42.dp).testTag("demo_scenario_1_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberNavyCard),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                        ) {
                            Text("Scenario 1: 91140* Loan", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onRunDemoScenario2,
                            modifier = Modifier.weight(1f).height(42.dp).testTag("demo_scenario_2_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SecurityRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SecurityRed.copy(alpha = 0.4f))
                        ) {
                            Text("Scenario 2: OTP Scam", color = SecurityRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Standalone Direct APK Share Banner (No Play Console required)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_apk_share_banner"),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Direct APK Distribution", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Share APK via WhatsApp, Drive or Bluetooth (No Play Console)", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = onShareApk,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp).testTag("home_share_apk_button")
                    ) {
                        Text("Share APK", color = CyberNavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Today's Statistics Grid (4 Cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "TODAY'S PROTECTION STATISTICS",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Blocked",
                        value = state.analytics.blockedCount.toString(),
                        icon = Icons.Default.Block,
                        accentColor = SecurityRed,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "AI Screened",
                        value = state.analytics.screenedCount.toString(),
                        icon = Icons.Default.RecordVoiceOver,
                        accentColor = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Suspicious",
                        value = state.analytics.suspiciousCount.toString(),
                        icon = Icons.Default.Warning,
                        accentColor = SecurityOrange,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Protected Time",
                        value = state.analytics.protectedTimeFormatted,
                        icon = Icons.Default.HourglassTop,
                        accentColor = CyberPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recharts Blocked Calls Intelligence Dashboard Widget
        item {
            BlockedScamSummaryWidget(
                calls = state.allCalls,
                blockedNumbers = state.blockedNumbers,
                onViewAllBlocked = { onNavigateToCalls("Blocked") }
            )
        }

        // Quick Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "QUICK ACTIONS",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        title = "+ Add Rule",
                        icon = Icons.Default.Add,
                        onClick = onNavigateToRules,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Threat Scan",
                        icon = Icons.Default.Dialpad,
                        onClick = onOpenDialer,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "AI Screening",
                        icon = Icons.Default.GraphicEq,
                        onClick = onNavigateToAI,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Call Logs",
                        icon = Icons.Default.Call,
                        onClick = { onNavigateToCalls("All") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recent Activity Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "View All (${state.allCalls.size})",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToCalls("All") }
                )
            }
        }

        // Recent Calls List (takes top 5)
        items(state.recentCalls.take(5)) { call ->
            CallCard(
                call = call,
                onClick = { onCallClick(call.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(58.dp)
            .clickable { onClick() }
            .testTag("quick_action_${title.filter { it.isLetter() }}"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}
