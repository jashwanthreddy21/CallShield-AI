package com.example.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.RiskLevel
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityGreenDark
import com.example.ui.theme.SecurityOrange
import com.example.ui.theme.SecurityOrangeDark
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.SecurityRedDark
import com.example.ui.theme.SecurityYellow
import com.example.ui.theme.SecurityYellowDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ActionBadge(action: CallAction, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (action) {
        CallAction.BLOCK -> Triple(SecurityRedDark.copy(alpha = 0.4f), SecurityRed, Icons.Default.Block)
        CallAction.AI_SCREEN -> Triple(CyberNavySurface, CyberCyan, Icons.Default.RecordVoiceOver)
        CallAction.ALLOW -> Triple(SecurityGreenDark.copy(alpha = 0.4f), SecurityGreen, Icons.Default.CheckCircle)
        CallAction.SILENCE -> Triple(SecurityYellowDark.copy(alpha = 0.4f), SecurityYellow, Icons.Default.VolumeOff)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("action_badge_${action.name}")
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
        Text(text = action.displayName, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun RiskBadge(riskLevel: RiskLevel, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (riskLevel) {
        RiskLevel.LOW -> Triple(SecurityGreenDark.copy(alpha = 0.35f), SecurityGreen, "Low Risk")
        RiskLevel.MEDIUM -> Triple(SecurityYellowDark.copy(alpha = 0.35f), SecurityYellow, "Medium Risk")
        RiskLevel.HIGH -> Triple(SecurityRedDark.copy(alpha = 0.35f), SecurityRed, "High Risk")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 3.dp)
            .testTag("risk_badge_${riskLevel.name}")
    ) {
        if (riskLevel == RiskLevel.HIGH) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = textColor, modifier = Modifier.size(11.dp))
        }
        Text(text = label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CategoryBadge(category: CallCategory, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (category) {
        CallCategory.POTENTIAL_SCAM, CallCategory.SPAM -> Pair(SecurityRedDark.copy(alpha = 0.4f), SecurityRed)
        CallCategory.TELEMARKETING, CallCategory.INSURANCE, CallCategory.LOANS -> Pair(SecurityOrangeDark.copy(alpha = 0.35f), SecurityOrange)
        CallCategory.CUSTOMER_SERVICE -> Pair(CyberNavySurface, CyberCyan)
        CallCategory.LEGITIMATE -> Pair(SecurityGreenDark.copy(alpha = 0.35f), SecurityGreen)
        CallCategory.UNKNOWN -> Pair(CyberNavyBorder, TextSecondary)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("category_badge_${category.name}")
    ) {
        Text(text = category.displayName, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("stat_card_$title"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                }
            }
            Text(text = value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AIWaveform(
    level: Float,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Canvas(modifier = modifier.height(36.dp).fillMaxWidth().testTag("ai_waveform")) {
        val barCount = 18
        val spacing = size.width / (barCount * 1.6f)
        val barWidth = spacing * 0.6f
        val startX = (size.width - (barCount * spacing)) / 2f
        val centerY = size.height / 2f

        for (i in 0 until barCount) {
            val factor = when (i % 3) {
                0 -> phase1
                1 -> phase2
                else -> phase3
            }
            val baseAmp = if (isSpeaking) 0.85f else (level.coerceIn(0.15f, 0.9f))
            val barHeight = (size.height * 0.8f * factor * baseAmp).coerceAtLeast(6f)
            val x = startX + i * spacing
            val y = centerY - barHeight / 2f

            drawRoundRect(
                color = if (i % 2 == 0) CyberCyan else CyberPurple,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}
