package com.example.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val outlinedIcon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Shield, Icons.Outlined.Shield)
    data object Calls : Screen("calls", "Calls", Icons.Filled.Phone, Icons.Outlined.Phone)
    data object Rules : Screen("rules", "Rules", Icons.Filled.Tune, Icons.Outlined.Tune)
    data object AI : Screen("ai", "AI Screening", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(Home, Calls, Rules, AI, Settings)
    }
}
