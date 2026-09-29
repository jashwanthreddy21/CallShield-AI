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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
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
import com.example.presentation.components.CallCard
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
                        title = "AI Screening",
                        icon = Icons.Default.GraphicEq,
                        onClick = onNavigateToAI,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Blocked",
                        icon = Icons.Default.Block,
                        onClick = { onNavigateToCalls("Blocked") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Suspicious",
                        icon = Icons.Default.Warning,
                        onClick = { onNavigateToCalls("Suspicious") },
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
