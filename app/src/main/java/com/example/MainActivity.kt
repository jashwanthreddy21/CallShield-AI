package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.presentation.components.DemoCallDialog
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
    val uiState by viewModel.uiState.collectAsState()

    var showOnboarding by remember { mutableStateOf(false) }
    var currentTabRoute by remember { mutableStateOf(Screen.Home.route) }
    var selectedCallDetailId by remember { mutableStateOf<Long?>(null) }

    // Intercept back presses when in sub-screen
    BackHandler(enabled = selectedCallDetailId != null || showOnboarding) {
        if (selectedCallDetailId != null) {
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
                            onRunDemoScenario2 = { viewModel.startDemoSimulation(2) }
                        )
                    }

                    Screen.Calls.route -> {
                        CallsScreen(
                            state = uiState,
                            onTabSelected = { tab -> viewModel.setCallTab(tab) },
                            onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                            onCallClick = { id -> selectedCallDetailId = id }
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
                            onClearAIMemory = { viewModel.clearAllAIMemory() }
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
}
