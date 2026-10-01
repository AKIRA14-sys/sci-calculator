package com.supersci.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.supersci.calculator.ui.screens.CalculatorScreen
import com.supersci.calculator.ui.screens.GameHubScreen
import com.supersci.calculator.ui.screens.SettingsScreen
import com.supersci.calculator.ui.screens.VaultScreen

sealed class NavRoutes(val route: String, val title: String, val icon: ImageVector) {
    data object Calculator : NavRoutes("calculator", "Calculator", Icons.Default.Calculate)
    data object Vault : NavRoutes("vault", "Vault", Icons.Default.Lock)
    data object GameHub : NavRoutes("gamehub", "Game Hub", Icons.Default.Gamepad)
    data object Settings : NavRoutes("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainAppShell()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.Calculator.route

    val items = listOf(
        NavRoutes.Calculator,
        NavRoutes.Vault,
        NavRoutes.GameHub,
        NavRoutes.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Calculator.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Calculator.route) { CalculatorScreen() }
            composable(NavRoutes.Vault.route) { VaultScreen() }
            composable(NavRoutes.GameHub.route) { GameHubScreen() }
            composable(NavRoutes.Settings.route) { SettingsScreen() }
        }
    }
}
