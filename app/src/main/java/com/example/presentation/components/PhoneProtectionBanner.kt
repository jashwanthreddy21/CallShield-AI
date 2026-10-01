package com.example.presentation.components

import android.app.Activity
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CallHistoryLogEntity
import com.example.services.call.CallPermissionManager
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

/**
 * Interactive card providing one-tap Android telephony permission grants,
 * Call Screening Role setup, and real-time validation for AI call lifting & spam blocking.
 */
@Composable
fun PhoneProtectionBanner(
    onPermissionsUpdated: () -> Unit,
    onTestScreening: (number: String, name: String, shouldBlock: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPhoneState by remember { mutableStateOf(CallPermissionManager.hasPhoneStatePermission(context)) }
    var hasAnswerCalls by remember { mutableStateOf(CallPermissionManager.hasAnswerCallsPermission(context)) }
    var hasCallLog by remember { mutableStateOf(CallPermissionManager.hasCallLogPermission(context)) }
    var hasRole by remember { mutableStateOf(CallPermissionManager.isCallScreeningRoleHeld(context)) }

    fun refreshAll() {
        hasPhoneState = CallPermissionManager.hasPhoneStatePermission(context)
        hasAnswerCalls = CallPermissionManager.hasAnswerCallsPermission(context)
        hasCallLog = CallPermissionManager.hasCallLogPermission(context)
        hasRole = CallPermissionManager.isCallScreeningRoleHeld(context)
        onPermissionsUpdated()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshAll()
        Toast.makeText(context, "Permissions updated successfully", Toast.LENGTH_SHORT).show()
    }

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        refreshAll()
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "CallShield is now your default Call Screening app!", Toast.LENGTH_SHORT).show()
        }
    }

    val isFullyConfigured = hasPhoneState && hasAnswerCalls && hasCallLog

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("phone_protection_permission_card"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isFullyConfigured) SecurityGreen.copy(alpha = 0.5f) else SecurityOrange.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFullyConfigured) SecurityGreen.copy(alpha = 0.2f) else SecurityOrange.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFullyConfigured) Icons.Default.Shield else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isFullyConfigured) SecurityGreen else SecurityOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "ANDROID PHONE PERMISSIONS",
                            color = if (isFullyConfigured) SecurityGreen else SecurityOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isFullyConfigured) "Telephony Interceptor Active" else "Permissions Required to Block & Answer",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isFullyConfigured) SecurityGreen.copy(alpha = 0.2f) else SecurityOrange.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isFullyConfigured) "ACTIVE" else "SETUP NEEDED",
                        color = if (isFullyConfigured) SecurityGreen else SecurityOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "To allow CallShield to detect incoming calls, automatically reject spam callers, and permit AI to lift/answer calls, grant Android telephony permissions below:",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            // Permission checklist
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PermissionCheckRow(
                    title = "Phone State & Ringing Detection (READ_PHONE_STATE)",
                    isGranted = hasPhoneState
                )
                PermissionCheckRow(
                    title = "AI Call Answering & Lifting (ANSWER_PHONE_CALLS)",
                    isGranted = hasAnswerCalls
                )
                PermissionCheckRow(
                    title = "System Call Log Sync (READ_CALL_LOG)",
                    isGranted = hasCallLog
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    PermissionCheckRow(
                        title = "Android Default Call Screener Role",
                        isGranted = hasRole
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isFullyConfigured) {
                    Button(
                        onClick = {
                            permissionLauncher.launch(CallPermissionManager.REQUIRED_PERMISSIONS)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("grant_permissions_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = CyberNavyDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grant Permissions", color = CyberNavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasRole) {
                    OutlinedButton(
                        onClick = {
                            val intent = CallPermissionManager.createCallScreeningRoleIntent(context)
                            if (intent != null) {
                                roleLauncher.launch(intent)
                            } else {
                                Toast.makeText(context, "Call Screening role not supported on this device", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("set_screener_role_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                    ) {
                        Text("Set as Screener", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick Testing Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onTestScreening("+1 800-555-0199", "National Robocall Dept", true)
                        Toast.makeText(context, "Tested Spam Block -> Recorded to Room database!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("test_block_call_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecurityRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SecurityRed.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = SecurityRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Real Block", color = SecurityRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                OutlinedButton(
                    onClick = {
                        onTestScreening("+91 91140 12345", "ABC Finance Inbound", false)
                        Toast.makeText(context, "Tested AI Call Lift & Screen -> Recorded to Room database!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("test_ai_lift_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.PhoneInTalk, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test AI Lift & Screen", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun PermissionCheckRow(
    title: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isGranted) SecurityGreen else SecurityOrange,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = title,
                color = if (isGranted) TextSecondary else TextPrimary,
                fontSize = 11.sp
            )
        }

        Text(
            text = if (isGranted) "Granted" else "Missing",
            color = if (isGranted) SecurityGreen else SecurityOrange,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Room Database Call History Logs Card with AI-Generated Screening Summaries
 */
@Composable
fun RoomCallHistorySummaryCard(
    historyLogs: List<CallHistoryLogEntity>,
    onViewAll: () -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("room_call_history_card"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            .background(CyberPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyberPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "ROOM DATABASE CALL LOGS",
                            color = CyberPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "AI-Generated Screening Summaries",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "${historyLogs.size} logs",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "Caller numbers, millisecond timestamps, and full AI screening transcripts stored locally in SQLite via Room Entity & DAO:",
                color = TextSecondary,
                fontSize = 12.sp
            )

            if (historyLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No call history logs stored yet.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    historyLogs.take(3).forEach { log ->
                        CallHistoryLogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun CallHistoryLogItem(log: CallHistoryLogEntity) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }

    val actionColor = when (log.actionTaken) {
        "BLOCKED" -> SecurityRed
        "SCREENED" -> SecurityOrange
        "ALLOWED" -> SecurityGreen
        else -> CyberCyan
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CyberNavySurface)
            .border(1.dp, CyberNavyBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = log.phoneNumber,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${log.callerName} • $formattedTime",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(actionColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = log.actionTaken,
                    color = actionColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // AI Screening Summary bubble
        if (log.aiScreeningSummary.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberNavyCard)
                    .border(1.dp, CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Summary",
                        tint = CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = log.aiScreeningSummary,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
