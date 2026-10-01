package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.presentation.components.DemoCallDialog
import com.example.presentation.components.DialerDialog
import com.example.presentation.navigation.CallShieldBottomBar
import com.example.presentation.navigation.Screen
import com.example.presentation.screens.ai.AISettingsScreen
import com.example.presentation.screens.calls.CallDetailScreen
import com.example.presentation.screens.calls.CallsScreen
import com.example.presentation.screens.home.HomeScreen
import com.example.presentation.screens.onboarding.OnboardingScreen
import com.example.presentation.screens.rules.RulesScreen
import com.example.presentation.screens.settings.SettingsScreen
import com.example.presentation.viewmodel.CallShieldViewModel
import com.example.services.call.CallPermissionManager
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CallShieldViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                CallShieldApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CallShieldApp(viewModel: CallShieldViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showOnboarding by remember { mutableStateOf(false) }
    var currentTabRoute by remember { mutableStateOf(Screen.Home.route) }
    var selectedCallDetailId by remember { mutableStateOf<Long?>(null) }
    var showDialer by remember { mutableStateOf(false) }

    // Runtime Permission Launcher for Android Phone State, Call Log, and Call Answering
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshPermissions()
    }

    LaunchedEffect(Unit) {
        if (!CallPermissionManager.areCorePermissionsGranted(context)) {
            permissionLauncher.launch(CallPermissionManager.REQUIRED_PERMISSIONS)
        }
    }

    // Intercept back presses when in sub-screen
    BackHandler(enabled = selectedCallDetailId != null || showOnboarding || showDialer) {
        if (showDialer) {
            showDialer = false
        } else if (selectedCallDetailId != null) {
            selectedCallDetailId = null
        } else if (showOnboarding) {
            showOnboarding = false
        }
    }

    if (showOnboarding) {
        OnboardingScreen(
            onFinish = { showOnboarding = false }
        )
        return
    }

    if (selectedCallDetailId != null) {
        CallDetailScreen(
            callId = selectedCallDetailId!!,
            viewModel = viewModel,
            onBack = { selectedCallDetailId = null }
        )
    } else {
        Scaffold(
            bottomBar = {
                CallShieldBottomBar(
                    currentRoute = currentTabRoute,
                    onNavigate = { route -> currentTabRoute = route }
                )
            },
            containerColor = CyberNavyDark
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTabRoute) {
                    Screen.Home.route -> {
                        HomeScreen(
                            state = uiState,
                            onToggleProtection = { enabled -> viewModel.toggleShield(enabled) },
                            onCallClick = { id -> selectedCallDetailId = id },
                            onNavigateToRules = { currentTabRoute = Screen.Rules.route },
                            onNavigateToAI = { currentTabRoute = Screen.AI.route },
                            onNavigateToCalls = { tab ->
                                viewModel.setCallTab(tab)
                                currentTabRoute = Screen.Calls.route
                            },
                            onRunDemoScenario1 = { viewModel.startDemoSimulation(1) },
                            onRunDemoScenario2 = { viewModel.startDemoSimulation(2) },
                            onOpenDialer = { showDialer = true },
                            onShareApk = { viewModel.shareApk(context) },
                            onDismissGeminiAlert = { viewModel.dismissGeminiAlert() },
                            onQuickBlockNumber = { phone, name ->
                                viewModel.quickBlockNumber(phone, name)
                                Toast.makeText(context, "Added $phone to Blocked List", Toast.LENGTH_SHORT).show()
                            },
                            onTestScreening = { number, name, shouldBlock ->
                                viewModel.testIncomingCallScreening(number, name, shouldBlock)
                            },
                            onRefreshPermissions = {
                                viewModel.refreshPermissions()
                            },
                            onClearHistoryLogs = {
                                viewModel.clearAllHistoryLogs()
                                Toast.makeText(context, "Cleared Room call history logs", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    Screen.Calls.route -> {
                        CallsScreen(
                            state = uiState,
                            onTabSelected = { tab -> viewModel.setCallTab(tab) },
                            onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                            onCallClick = { id -> selectedCallDetailId = id },
                            onOpenDialer = { showDialer = true },
                            onImportCallLogs = {
                                viewModel.importDeviceCallLogs { count ->
                                    Toast.makeText(context, "Synced $count call records successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onExportCsv = { viewModel.exportCallLogsCsv(context) }
                        )
                    }

                    Screen.Rules.route -> {
                        RulesScreen(
                            state = uiState,
                            onToggleRule = { id, enabled -> viewModel.toggleRule(id, enabled) },
                            onDeleteRule = { id -> viewModel.deleteRule(id) },
                            onAddRule = { matchType, pattern, action, category, priority, note ->
                                viewModel.addRule(matchType, pattern, action, category, priority, note)
                            },
                            onAddToAllowlist = { name, number, cat, notes ->
                                viewModel.addToAllowlist(name, number, cat, notes)
                            },
                            onDeleteAllowlist = { id -> viewModel.deleteAllowlist(id) }
                        )
                    }

                    Screen.AI.route -> {
                        AISettingsScreen(
                            state = uiState,
                            onToggleScreening = { enabled -> viewModel.setAIScreeningEnabled(enabled) },
                            onSelectPersonality = { personality -> viewModel.setPersonality(personality) },
                            onUpdateInstructions = { instructions -> viewModel.setInstructions(instructions) },
                            onLaunchVoiceDemo = { viewModel.startDemoSimulation(1) }
                        )
                    }

                    Screen.Settings.route -> {
                        SettingsScreen(
                            state = uiState,
                            onToggleProtection = { enabled -> viewModel.toggleShield(enabled) },
                            onToggleAIScreening = { enabled -> viewModel.setAIScreeningEnabled(enabled) },
                            onToggleTranscriptRetention = { enabled -> viewModel.setTranscriptRetention(enabled) },
                            onToggleAIMemory = { enabled -> viewModel.setAIMemoryEnabled(enabled) },
                            onClearCallHistory = { viewModel.clearAllCallHistory() },
                            onClearAIMemory = { viewModel.clearAllAIMemory() },
                            onShareApk = { viewModel.shareApk(context) },
                            onExportCsv = { viewModel.exportCallLogsCsv(context) }
                        )
                    }
                }
            }
        }
    }

    // Modal dialog for running live AI call screening simulation (Scenarios 1 & 2)
    if (uiState.isSimulatingCall) {
        DemoCallDialog(
            state = uiState,
            onDismiss = { viewModel.dismissSimulation() }
        )
    }

    // Secure Dialer Keypad & Pre-Call Threat Scanner Dialog
    if (showDialer) {
        DialerDialog(
            onDismiss = { showDialer = false },
            onInspectNumber = { num -> viewModel.inspectNumber(num) }
        )
    }
}
