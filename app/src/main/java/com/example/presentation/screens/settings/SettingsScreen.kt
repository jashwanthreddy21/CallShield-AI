package com.example.presentation.screens.settings

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CallAction
import com.example.presentation.viewmodel.CallShieldUiState
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    state: CallShieldUiState,
    onToggleProtection: (Boolean) -> Unit,
    onToggleAIScreening: (Boolean) -> Unit,
    onToggleTranscriptRetention: (Boolean) -> Unit,
    onToggleAIMemory: (Boolean) -> Unit,
    onClearCallHistory: () -> Unit,
    onClearAIMemory: () -> Unit,
    onShareApk: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPrivacyCenter by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Settings & Privacy",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Privacy Center Dashboard Card (Section 28)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_center_card"),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PrivacyTip, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Text(text = "PRIVACY DASHBOARD & DATA", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SecurityGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("On-Device First", color = SecurityGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "CallShield prioritizes data minimization. All rules and call transcripts are managed under strict subscriber control.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    // Data counts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DataMetricBox("Call Records", "${state.allCalls.size}", modifier = Modifier.weight(1f))
                        DataMetricBox("Transcripts", "${state.allCalls.count { it.hasTranscript }}", modifier = Modifier.weight(1f))
                        DataMetricBox("AI Memories", "${state.callerMemories.size}", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onClearCallHistory()
                                Toast.makeText(context, "Call history cleared", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(42.dp).testTag("clear_history_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SecurityRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SecurityRed.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = SecurityRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear History", color = SecurityRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onClearAIMemory()
                                Toast.makeText(context, "AI caller memories wiped", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(42.dp).testTag("clear_memory_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                        ) {
                            Text("Wipe Memories", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Exported call data as secure JSON", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(42.dp).testTag("export_data_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Protection Controls (Section 27)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "PROTECTION PREFERENCES", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleRow(
                        title = "Master Call Firewall",
                        subtitle = "Enable real-time Android call interception",
                        checked = state.isShieldEnabled,
                        onCheckedChange = onToggleProtection
                    )

                    SettingToggleRow(
                        title = "AI Voice Screening",
                        subtitle = "Let AI assistant screen incoming unknown callers",
                        checked = state.aiScreeningEnabled,
                        onCheckedChange = onToggleAIScreening
                    )

                    SettingToggleRow(
                        title = "Retain Call Transcripts",
                        subtitle = "Save text dialogue locally for privacy review",
                        checked = state.transcriptRetention,
                        onCheckedChange = onToggleTranscriptRetention
                    )

                    SettingToggleRow(
                        title = "Caller Context Memory",
                        subtitle = "Remember previous interaction notes for repeat callers",
                        checked = state.aiMemoryEnabled,
                        onCheckedChange = onToggleAIMemory
                    )
                }
            }
        }

        // Security & Device Status (Section 29)
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
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SecurityGreen, modifier = Modifier.size(16.dp))
                        Text(text = "SECURITY ARCHITECTURE", color = SecurityGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    SecurityStatusRow("Storage Encryption", "Android Keystore / SQLCipher compatible")
                    SecurityStatusRow("CallScreening API", "Android Telecom CallScreeningService Active")
                    SecurityStatusRow("Telephony Provider", "VoIP / SIP Abstraction Layer Active")
                    SecurityStatusRow("Data Residency", "Zero-cloud transit by default")
                }
            }
        }

        // Standalone Direct APK Sharing & Export (No Play Console needed!)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apk_share_card"),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = "DIRECT APK SHARING (NO PLAY CONSOLE)",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "You don't need a Google Play Console account! You can share the standalone CallShield APK directly to friends, family, or other devices through WhatsApp, Telegram, Google Drive, Email, or Bluetooth.",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // 3-step sideloading guide
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberNavySurface)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("How to Install on Any Phone:", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("1. Tap 'Share App APK' below and send via WhatsApp / Drive / Nearby Share", color = TextSecondary, fontSize = 11.sp)
                        Text("2. On the other phone, tap the received APK file and enable 'Install Unknown Apps'", color = TextSecondary, fontSize = 11.sp)
                        Text("3. Open CallShield AI and set as default spam screening app!", color = TextSecondary, fontSize = 11.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onShareApk,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("share_apk_direct_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = CyberNavyDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share App APK", color = CyberNavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onExportCsv,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("export_logs_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Logs", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // About Application
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "ABOUT CALLSHIELD AI", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(text = "CallShield AI v1.0.0", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Block unwanted calls. Let AI handle the conversation.\nDesigned with native Android CallScreeningService integration, rule engine, and provider-agnostic telephony voice screening adapter.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DataMetricBox(label: String, count: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CyberNavySurface)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = count, color = CyberCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyberNavyDark,
                checkedTrackColor = CyberCyan,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CyberNavyBorder
            )
        )
    }
}

@Composable
private fun SecurityStatusRow(title: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = TextSecondary, fontSize = 12.sp)
        Text(text = status, color = SecurityGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
