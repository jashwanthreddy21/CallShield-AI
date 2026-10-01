package com.example.presentation.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CallEntity
import com.example.domain.model.CallAction
import com.example.domain.model.CallDirection
import com.example.domain.model.RiskLevel
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
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

@Composable
fun CallCard(
    call: CallEntity,
    onClick: () -> Unit,
    onQuickBlock: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(call.timestamp))

    val durationText = when {
        call.action == CallAction.BLOCK || call.direction == CallDirection.BLOCKED -> "Blocked"
        call.direction == CallDirection.MISSED -> "Missed"
        call.durationSeconds > 0 -> {
            val mins = call.durationSeconds / 60
            val secs = call.durationSeconds % 60
            if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
        }
        else -> "0s"
    }

    // Avatar styling based on risk & status
    val avatarBg = when {
        call.riskLevel == RiskLevel.HIGH || call.action == CallAction.BLOCK -> SecurityRed.copy(alpha = 0.15f)
        call.action == CallAction.AI_SCREEN -> CyberPurple.copy(alpha = 0.2f)
        call.riskLevel == RiskLevel.MEDIUM -> SecurityOrange.copy(alpha = 0.2f)
        else -> SecurityGreen.copy(alpha = 0.15f)
    }

    val avatarTint = when {
        call.riskLevel == RiskLevel.HIGH || call.action == CallAction.BLOCK -> SecurityRed
        call.action == CallAction.AI_SCREEN -> CyberPurple
        call.riskLevel == RiskLevel.MEDIUM -> SecurityOrange
        else -> SecurityGreen
    }

    val initialLetter = call.callerName.trim().firstOrNull()?.uppercaseChar()?.toString()
        ?: call.phoneNumber.trim().lastOrNull()?.toString()
        ?: "?"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("call_card_${call.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (call.riskLevel == RiskLevel.HIGH) SecurityRed.copy(alpha = 0.4f) else CyberNavyBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Caller Initial / Avatar Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(avatarBg)
                        .border(1.dp, avatarTint.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (call.riskLevel == RiskLevel.HIGH || call.action == CallAction.BLOCK) {
                        Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = SecurityRed, modifier = Modifier.size(20.dp))
                    } else if (call.action == CallAction.AI_SCREEN) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = initialLetter,
                            color = avatarTint,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name and Number
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = call.callerName.ifEmpty { "Unknown Caller" },
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (call.hasTranscript) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("AI Log", color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Direction Icon
                        val (dirIcon, dirTint) = when (call.direction) {
                            CallDirection.OUTGOING -> Icons.AutoMirrored.Filled.CallMade to CyberCyan
                            CallDirection.MISSED -> Icons.AutoMirrored.Filled.CallMissed to SecurityRed
                            CallDirection.BLOCKED -> Icons.Default.Block to SecurityRed
                            CallDirection.AI_SCREENED -> Icons.Default.GraphicEq to CyberPurple
                            CallDirection.INCOMING -> Icons.AutoMirrored.Filled.CallReceived to SecurityGreen
                        }
                        Icon(imageVector = dirIcon, contentDescription = null, tint = dirTint, modifier = Modifier.size(13.dp))

                        Text(
                            text = call.phoneNumber,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Text(text = "•", color = TextMuted, fontSize = 10.sp)
                        Text(text = durationText, color = TextMuted, fontSize = 11.sp)
                    }
                }

                // Quick Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Quick Call back button
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${call.phoneNumber.trim()}"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(34.dp).testTag("quick_call_${call.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call back", tint = CyberCyan, modifier = Modifier.size(17.dp))
                    }

                    // Quick SMS button
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${call.phoneNumber.trim()}"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(34.dp).testTag("quick_sms_${call.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Message, contentDescription = "Send SMS", tint = TextSecondary, modifier = Modifier.size(17.dp))
                    }
                }
            }

            // Summary / Purpose text if available
            if (call.purpose.isNotBlank() || call.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberNavySurface)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (call.summary.isNotBlank()) call.summary else call.purpose,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Badges and Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadge(category = call.category)
                    RiskBadge(riskLevel = call.riskLevel)
                    if (call.threatScore > 50) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SecurityRed.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${call.threatScore}% Risk",
                                color = SecurityRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
