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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
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
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    state: CallShieldUiState,
    onToggleRule: (Long, Boolean) -> Unit,
    onDeleteRule: (Long) -> Unit,
    onAddRule: (RuleMatchType, String, CallAction, CallCategory?, Int, String) -> Unit,
    onAddToAllowlist: (String, String, String, String) -> Unit,
    onDeleteAllowlist: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var showAddAllowlistDialog by remember { mutableStateOf(false) }

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
                    text = "Protection Rules & Firewall",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Metrics Summary Grid (Section 13)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RuleMetricPill(title = "Active Rules", count = "12", modifier = Modifier.weight(1f))
                        RuleMetricPill(title = "Blocked Numbers", count = "27", modifier = Modifier.weight(1f))
                        RuleMetricPill(title = "Patterns", count = "8", modifier = Modifier.weight(1f))
                        RuleMetricPill(title = "Allowlist", count = "${state.allowlist.size}", modifier = Modifier.weight(1f))
                    }
                }
            }

            // Rule Precedence Card (Section 16 & 34)
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
                                text = "DETERMINISTIC RULE PRECEDENCE",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val precedenceSteps = listOf(
                            "1. Explicit Allowlist (Bypasses all blocking rules)",
                            "2. Specific Number Rule (Exact phone number match)",
                            "3. Pattern Rule (Prefixes such as 91140* or 1800*)",
                            "4. Category Rule (e.g. Telemarketing, Loans)",
                            "5. Default Behavior (Allow / Screen policy)"
                        )

                        precedenceSteps.forEachIndexed { idx, step ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = step,
                                    color = if (idx == 0) SecurityGreen else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Tabs: Protection Rules vs Allowlist
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CyberNavyCard,
                    contentColor = CyberCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyberCyan
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Protection Rules (${state.rules.size})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Trusted Allowlist (${state.allowlist.size})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Tab 0: Protection Rules List
            if (selectedTab == 0) {
                items(state.rules) { rule ->
                    RuleCard(
                        rule = rule,
                        onToggle = { enabled -> onToggleRule(rule.id, enabled) },
                        onDelete = { onDeleteRule(rule.id) }
                    )
                }
            } else {
                // Tab 1: Smart Allowlist List (Section 16)
                item {
                    Text(
                        text = "TRUSTED NUMBERS BYPASS ALL BLOCKING RULES",
                        color = SecurityGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
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
                                modifier = Modifier.size(32.dp).testTag("delete_allowlist_${entry.id}")
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = SecurityRed, modifier = Modifier.size(18.dp))
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
                if (selectedTab == 0) showAddRuleDialog = true else showAddAllowlistDialog = true
            },
            containerColor = CyberCyan,
            contentColor = CyberNavyDark,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_rule_or_allowlist")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = if (selectedTab == 0) Icons.Default.Add else Icons.Default.PersonAdd, contentDescription = null)
                Text(text = if (selectedTab == 0) "Create Rule" else "Add Contact", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    // 5-Step Create Rule Wizard Dialog (Section 14)
    if (showAddRuleDialog) {
        CreateRuleDialog(
            onDismiss = { showAddRuleDialog = false },
            onSave = { matchType, pattern, action, category, priority, note ->
                onAddRule(matchType, pattern, action, category, priority, note)
                showAddRuleDialog = false
            }
        )
    }

    // Add to Allowlist Dialog
    if (showAddAllowlistDialog) {
        AddAllowlistDialog(
            onDismiss = { showAddAllowlistDialog = false },
            onSave = { name, number, cat, notes ->
                onAddToAllowlist(name, number, cat, notes)
                showAddAllowlistDialog = false
            }
        )
    }
}

@Composable
private fun RuleMetricPill(title: String, count: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, color = CyberCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = title, color = TextMuted, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRuleDialog(
    onDismiss: () -> Unit,
    onSave: (RuleMatchType, String, CallAction, CallCategory?, Int, String) -> Unit
) {
    var matchType by remember { mutableStateOf(RuleMatchType.PATTERN) }
    var patternValue by remember { mutableStateOf("91140*") }
    var selectedAction by remember { mutableStateOf(CallAction.AI_SCREEN) }
    var selectedCategory by remember { mutableStateOf(CallCategory.TELEMARKETING) }
    var priority by remember { mutableIntStateOf(8) }
    var note by remember { mutableStateOf("Telemarketing prefix firewall") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("create_rule_dialog"),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "CREATE PROTECTION RULE",
                    color = CyberCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Step 1: Match Type
                Text(text = "1. Match Type", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RuleMatchType.values().take(3).forEach { type ->
                        Button(
                            onClick = { matchType = type },
                            modifier = Modifier.weight(1f).height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (matchType == type) CyberCyan else CyberNavySurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = type.displayName.take(12),
                                color = if (matchType == type) CyberNavyDark else TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Step 2: Value / Pattern
                Text(text = "2. Pattern / Value ('*' matches wildcard prefix)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = patternValue,
                    onValueChange = { patternValue = it },
                    placeholder = { Text("e.g. 91140*, +9191140*, 1800*", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberNavySurface,
                        unfocusedContainerColor = CyberNavySurface,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberNavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("rule_pattern_input")
                )

                // Step 3: Action
                Text(text = "3. Action When Matched", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(CallAction.AI_SCREEN, CallAction.BLOCK, CallAction.SILENCE, CallAction.ALLOW).forEach { act ->
                        Button(
                            onClick = { selectedAction = act },
                            modifier = Modifier.weight(1f).height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedAction == act) CyberCyan else CyberNavySurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = act.displayName,
                                color = if (selectedAction == act) CyberNavyDark else TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Step 4 & 5: Rule Preview Card (Section 14)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberNavySurface)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(text = "RULE PREVIEW", color = CyberPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "IF number matches: $patternValue\nTHEN perform action: ${selectedAction.displayName}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Buttons
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
                            if (patternValue.isNotBlank()) {
                                onSave(matchType, patternValue, selectedAction, selectedCategory, priority, note)
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
