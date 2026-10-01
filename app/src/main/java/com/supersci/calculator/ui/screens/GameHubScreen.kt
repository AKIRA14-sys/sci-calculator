package com.supersci.calculator.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.supersci.calculator.gamehub.GameAppInfo
import com.supersci.calculator.gamehub.GameLauncherManager
import com.supersci.calculator.gamehub.GamePanelOverlayService

@Composable
fun GameHubScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val launcherManager = remember { GameLauncherManager(context) }
    var games by remember { mutableStateOf(listOf<GameAppInfo>()) }
    var searchQuery by remember { mutableStateOf("") }
    var isOverlayActive by remember { mutableStateOf(false) }
    var showAllAppsMode by remember { mutableStateOf(false) }

    fun refreshAppList() {
        games = if (showAllAppsMode) {
            launcherManager.getAllLaunchableApps()
        } else {
            launcherManager.scanInstalledGames()
        }
    }

    LaunchedEffect(showAllAppsMode) {
        refreshAppList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎮 Game Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Row {
                FilterChip(
                    selected = showAllAppsMode,
                    onClick = { showAllAppsMode = !showAllAppsMode },
                    label = { Text(if (showAllAppsMode) "All Apps" else "Games Only") }
                )
                IconButton(onClick = { refreshAppList() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Apps")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // GamePanel Overlay Toggle Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("GamePanel AI Crosshair Overlay", fontWeight = FontWeight.Bold)
                    Text("Visual target assistance overlay", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = isOverlayActive,
                    onCheckedChange = { active ->
                        if (active) {
                            if (!Settings.canDrawOverlays(context)) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } else {
                                GamePanelOverlayService.start(context)
                                isOverlayActive = true
                            }
                        } else {
                            GamePanelOverlayService.stop(context)
                            isOverlayActive = false
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search installed applications...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        val filteredGames = games.filter { it.appName.contains(searchQuery, ignoreCase = true) }

        if (filteredGames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "No games detected.\nToggle 'All Apps' to view all installed applications." else "No matching apps found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredGames) { game ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (game.icon != null) {
                                Image(
                                    bitmap = game.icon.toBitmap().asImageBitmap(),
                                    contentDescription = game.appName,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = game.appName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = game.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { launcherManager.launchGame(game.packageName) }
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LAUNCH")
                            }
                        }
                    }
                }
            }
        }
    }
}
