package com.example.presentation.screens.rules

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.BlockedNumberEntity
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.RuleMatchType
import com.example.presentation.components.RuleCard
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    state: CallShieldUiState,
    onToggleRule: (Long, Boolean) -> Unit,
    onDeleteRule: (Long) -> Unit,
    onAddRule: (RuleMatchType, String, CallAction, CallCategory?, Int, String) -> Unit,
    onAddToAllowlist: (String, String, String, String) -> Unit,
    onDeleteAllowlist: (Long) -> Unit,
    onAddBlockedNumber: (String, String, String) -> Unit = { _, _, _ -> },
    onRemoveBlockedNumber: (Long) -> Unit = {},
    onTestInterceptor: (String, String) -> Unit = { _, _ -> },
    onClearTestDecision: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddBlockedDialog by remember { mutableStateOf(false) }
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var showAddAllowlistDialog by remember { mutableStateOf(false) }

    var testPhoneNumberInput by remember { mutableStateOf("+1 800-555-0199") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .testTag("rules_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Firewall & Security Rules",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Metrics Summary Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RuleMetricPill(title = "Blocked List", count = "${state.blockedNumbers.size}", modifier = Modifier.weight(1f))
                    RuleMetricPill(title = "Active Rules", count = "${state.rules.size}", modifier = Modifier.weight(1f))
                    RuleMetricPill(title = "Allowlist", count = "${state.allowlist.size}", modifier = Modifier.weight(1f))
                }
            }

            // Precedence Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("rule_precedence_card"),
                    colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Text(
                                text = "CALL INTERCEPTOR PRECEDENCE",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val precedenceSteps = listOf(
                            "1. Trusted Allowlist (Bypasses all blocking rules)",
                            "2. Room Blocked Numbers Table (Exact matches dropped immediately)",
                            "3. Custom Rules & Prefix Patterns (e.g. 91140* Telemarketing)",
                            "4. Default Firewall Behavior (Allow / AI Screening)"
                        )

                        precedenceSteps.forEachIndexed { idx, step ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = step,
                                    color = if (idx == 0) SecurityGreen else if (idx == 1) SecurityRed else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (idx <= 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Tabs: Blocked List vs Protection Rules vs Allowlist
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CyberNavyCard,
                    contentColor = CyberCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = if (selectedTab == 0) SecurityRed else if (selectedTab == 1) CyberCyan else SecurityGreen
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Blocked List (${state.blockedNumbers.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) SecurityRed else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Rules (${state.rules.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) CyberCyan else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Allowlist (${state.allowlist.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 2) SecurityGreen else TextSecondary
                            )
                        }
                    )
                }
            }

            // Tab 0: Blocked List (Room Database Table & Logic Service Test)
            if (selectedTab == 0) {
                // Interactive Interceptor Test Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("interceptor_test_card"),
                        colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PhoneCallback, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "TEST INCOMING CALL INTERCEPTOR",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Enter any phone number to simulate an incoming call and verify that the CallScreening logic service intercepts and checks it against the Room Blocked List table.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = testPhoneNumberInput,
                                    onValueChange = { testPhoneNumberInput = it },
                                    placeholder = { Text("Enter phone number to test", fontSize = 12.sp, color = TextMuted) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = CyberNavyCard,
                                        unfocusedContainerColor = CyberNavyCard,
                                        focusedBorderColor = CyberCyan,
                                        unfocusedBorderColor = CyberNavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("test_interceptor_input")
                                )

                                Button(
                                    onClick = {
                                        if (testPhoneNumberInput.isNotBlank()) {
                                            onTestInterceptor(testPhoneNumberInput, "Simulation Caller")
                                        }
                                    },
                                    modifier = Modifier.height(48.dp).testTag("run_test_interceptor_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CyberNavyDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test", color = CyberNavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // If test result exists, show decision banner
                            state.testInterceptionDecision?.let { decision ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (decision.isBlocked) SecurityRed.copy(alpha = 0.15f)
                                            else if (decision.isAllowlisted) SecurityGreen.copy(alpha = 0.15f)
                                            else CyberCyan.copy(alpha = 0.15f)
                                        )
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (decision.isBlocked) Icons.Default.Block
                                                    else if (decision.isAllowlisted) Icons.Default.CheckCircle
                                                    else Icons.Default.Shield,
                                                    contentDescription = null,
                                                    tint = if (decision.isBlocked) SecurityRed
                                                    else if (decision.isAllowlisted) SecurityGreen
                                                    else CyberCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = if (decision.isBlocked) "DECISION: ⛔ REJECT & DROP CALL"
                                                    else if (decision.isAllowlisted) "DECISION: ✓ ALLOW (TRUSTED CONTACT)"
                                                    else "DECISION: ✓ PERMITTED (CLEAN NUMBER)",
                                                    color = if (decision.isBlocked) SecurityRed
                                                    else if (decision.isAllowlisted) SecurityGreen
                                                    else CyberCyan,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = decision.reason,
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "Action: ${decision.action.name} • DisallowCall=${decision.isBlocked} • RejectCall=${decision.isBlocked}",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }

                                        IconButton(onClick = onClearTestDecision) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ROOM DATABASE 'BLOCKED_NUMBERS' TABLE",
                            color = SecurityRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Button(
                            onClick = { showAddBlockedDialog = true },
                            modifier = Modifier.height(32.dp).testTag("add_blocked_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SecurityRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SecurityRed.copy(alpha = 0.5f))
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = SecurityRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Block Number", color = SecurityRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (state.blockedNumbers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                                Text("No blocked numbers yet", color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("Add unwanted callers to drop them automatically.", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(state.blockedNumbers) { blocked ->
                        BlockedNumberCard(
                            blocked = blocked,
                            onUnblock = { onRemoveBlockedNumber(blocked.id) }
                        )
                    }
                }
            } else if (selectedTab == 1) {
                // Tab 1: Protection Rules List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE PATTERN & CATEGORY RULES",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Button(
                            onClick = { showAddRuleDialog = true },
                            modifier = Modifier.height(32.dp).testTag("add_rule_header_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Rule", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(state.rules) { rule ->
                    RuleCard(
                        rule = rule,
                        onToggle = { enabled -> onToggleRule(rule.id, enabled) },
                        onDelete = { onDeleteRule(rule.id) }
                    )
                }
            } else {
                // Tab 2: Smart Allowlist List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRUSTED NUMBERS (OVERRIDE FIREWALL)",
                            color = SecurityGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Button(
                            onClick = { showAddAllowlistDialog = true },
                            modifier = Modifier.height(32.dp).testTag("add_allowlist_header_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SecurityGreen.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = SecurityGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Trusted", color = SecurityGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(state.allowlist) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("allowlist_item_${entry.id}"),
                        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = entry.contactName,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SecurityGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = entry.category, color = SecurityGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = entry.phoneNumber, color = TextSecondary, fontSize = 13.sp)
                                if (entry.notes.isNotBlank()) {
                                    Text(text = entry.notes, color = TextMuted, fontSize = 11.sp)
                                }
                            }

                            IconButton(
                                onClick = { onDeleteAllowlist(entry.id) },
                                modifier = Modifier.size(36.dp).testTag("delete_allowlist_${entry.id}")
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete allowlist entry", tint = SecurityRed)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                when (selectedTab) {
                    0 -> showAddBlockedDialog = true
                    1 -> showAddRuleDialog = true
                    else -> showAddAllowlistDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("rules_fab"),
            containerColor = if (selectedTab == 0) SecurityRed else if (selectedTab == 1) CyberCyan else SecurityGreen,
            contentColor = if (selectedTab == 1) CyberNavyDark else TextPrimary
        ) {
            Icon(
                imageVector = if (selectedTab == 0) Icons.Default.Block else if (selectedTab == 1) Icons.Default.Add else Icons.Default.PersonAdd,
                contentDescription = "Add"
            )
        }
    }

    // Modal Dialog: Add Blocked Number to Room Database
    if (showAddBlockedDialog) {
        AddBlockedNumberDialog(
            onDismiss = { showAddBlockedDialog = false },
            onSave = { phone, name, reason ->
                onAddBlockedNumber(phone, name, reason)
                showAddBlockedDialog = false
                Toast.makeText(context, "Added $phone to Blocked List table", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Dialog: Add Protection Rule
    if (showAddRuleDialog) {
        AddRuleDialog(
            onDismiss = { showAddRuleDialog = false },
            onSave = { matchType, pattern, action, category, priority, note ->
                onAddRule(matchType, pattern, action, category, priority, note)
                showAddRuleDialog = false
            }
        )
    }

    // Modal Dialog: Add Trusted Contact to Allowlist
    if (showAddAllowlistDialog) {
        AddAllowlistDialog(
            onDismiss = { showAddAllowlistDialog = false },
            onSave = { name, phone, cat, notes ->
                onAddToAllowlist(name, phone, cat, notes)
                showAddAllowlistDialog = false
            }
        )
    }
}

@Composable
fun BlockedNumberCard(
    blocked: BlockedNumberEntity,
    onUnblock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(blocked.blockedAt))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("blocked_item_${blocked.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SecurityRed.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SecurityRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = SecurityRed, modifier = Modifier.size(20.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = blocked.phoneNumber,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (blocked.blockCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SecurityRed.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "Dropped ${blocked.blockCount}x",
                                    color = SecurityRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (blocked.callerName.isNotBlank()) {
                        Text(text = blocked.callerName, color = TextSecondary, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Reason: ${blocked.reason} • Added $formattedDate",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onUnblock,
                colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder),
                modifier = Modifier.height(34.dp).testTag("unblock_${blocked.id}")
            ) {
                Text("Unblock", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun AddBlockedNumberDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var callerName by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Spam & Robocall") }

    val quickReasons = listOf("Spam & Robocall", "Phishing Scam", "Harassment", "Telemarketing", "Fake Bank / OTP")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("add_blocked_dialog"),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SecurityRed.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = SecurityRed, modifier = Modifier.size(18.dp))
                    Text(
                        text = "ADD TO BLOCKED LIST",
                        color = SecurityRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Calls from this number will be intercepted by CallShield and dropped immediately.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number (e.g. +1 800-555-0199)", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecurityRed,
                        unfocusedBorderColor = CyberNavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("blocked_phone_input")
                )

                OutlinedTextField(
                    value = callerName,
                    onValueChange = { callerName = it },
                    label = { Text("Caller Name / Organization (optional)", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecurityRed,
                        unfocusedBorderColor = CyberNavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("blocked_name_input")
                )

                Text(text = "Reason for Blocking", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecurityRed,
                        unfocusedBorderColor = CyberNavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("blocked_reason_input")
                )

                // Quick reason chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    quickReasons.take(3).forEach { r ->
                        Button(
                            onClick = { reason = r },
                            modifier = Modifier.weight(1f).height(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (reason == r) SecurityRed else CyberNavySurface
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(r.take(12), color = if (reason == r) TextPrimary else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (phoneNumber.isNotBlank()) {
                                onSave(phoneNumber, callerName, reason)
                            }
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("save_blocked_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Block Number", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RuleMetricPill(title: String, count: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CyberNavyCard)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = count, color = CyberCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = title, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onSave: (RuleMatchType, String, CallAction, CallCategory?, Int, String) -> Unit
) {
    var matchType by remember { mutableStateOf(RuleMatchType.PATTERN) }
    var pattern by remember { mutableStateOf("") }
    var action by remember { mutableStateOf(CallAction.BLOCK) }
    var category by remember { mutableStateOf<CallCategory?>(CallCategory.TELEMARKETING) }
    var priority by remember { mutableIntStateOf(5) }
    var note by remember { mutableStateOf("") }

    val matchTypes = listOf(RuleMatchType.PATTERN, RuleMatchType.EXACT_NUMBER, RuleMatchType.CATEGORY)
    val actions = listOf(CallAction.BLOCK, CallAction.AI_SCREEN, CallAction.SILENCE, CallAction.ALLOW)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("add_rule_dialog"),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "CREATE PROTECTION RULE",
                    color = CyberCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(text = "Rule Type", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    matchTypes.forEach { mt ->
                        Button(
                            onClick = { matchType = mt },
                            modifier = Modifier.weight(1f).height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (matchType == mt) CyberCyan else CyberNavySurface
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(mt.displayName.take(8), color = if (matchType == mt) CyberNavyDark else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text("Pattern / Number (e.g. 91140* or +1 800-*)", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("rule_pattern_input")
                )

                Text(text = "Action", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    actions.forEach { act ->
                        Button(
                            onClick = { action = act },
                            modifier = Modifier.weight(1f).height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (action == act) CyberCyan else CyberNavySurface
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(act.displayName, color = if (action == act) CyberNavyDark else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description (Optional)", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("rule_note_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (pattern.isNotBlank()) {
                                onSave(matchType, pattern, action, category, priority, note)
                            }
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("save_rule_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create Rule", color = CyberNavyDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddAllowlistDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var contactName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Family") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Family", "Friends", "Work", "Bank", "Hospital", "College", "Custom")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("add_allowlist_dialog"),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SecurityGreen.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ADD TRUSTED CONTACT",
                    color = SecurityGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(text = "Trusted contacts bypass all blocking & screening rules.", color = TextSecondary, fontSize = 12.sp)

                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Contact Name", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("allowlist_name_input")
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number (e.g. +91 98765 00000)", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("allowlist_phone_input")
                )

                Text(text = "Category", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(4).forEach { cat ->
                        Button(
                            onClick = { category = cat },
                            modifier = Modifier.weight(1f).height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (category == cat) SecurityGreen else CyberNavySurface
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(cat, color = if (category == cat) CyberNavyDark else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (contactName.isNotBlank() && phoneNumber.isNotBlank()) {
                                onSave(contactName, phoneNumber, category, notes)
                            }
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("save_allowlist_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add to Allowlist", color = CyberNavyDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
