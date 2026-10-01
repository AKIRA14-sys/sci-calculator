package com.supersci.calculator.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.supersci.calculator.data.DatabaseProvider
import com.supersci.calculator.data.VaultFileEntity
import com.supersci.calculator.vault.VaultLockScreen
import com.supersci.calculator.vault.VaultManager
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun VaultScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbProvider = remember { DatabaseProvider.getInstance(context) }
    val vaultManager = remember { VaultManager(context) }

    var isUnlocked by remember { mutableStateOf(false) }
    var savedPinHash by remember { mutableStateOf<String?>(null) }
    var fileList by remember { mutableStateOf(listOf<VaultFileEntity>()) }

    fun refreshFiles() {
        scope.launch {
            fileList = dbProvider.db.vaultFileDao().getAllFiles()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { inputUri ->
            scope.launch {
                try {
                    val contentResolver = context.contentResolver
                    val fileName = "imported_" + System.currentTimeMillis()
                    val id = UUID.randomUUID().toString()
                    val destFile = File(vaultManager.getVaultDir(), id)

                    contentResolver.openInputStream(inputUri)?.use { inputStream ->
                        vaultManager.encryptFile(inputStream, destFile)
                    }

                    val entity = VaultFileEntity(
                        id = id,
                        originalName = fileName,
                        encryptedPath = destFile.absolutePath,
                        mimeType = contentResolver.getType(inputUri) ?: "application/octet-stream",
                        fileSize = destFile.length()
                    )

                    dbProvider.db.vaultFileDao().insert(entity)
                    refreshFiles()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    if (!isUnlocked) {
        VaultLockScreen(
            onUnlockSuccess = {
                isUnlocked = true
                refreshFiles()
            },
            savedPinHash = savedPinHash,
            onSetPin = { savedPinHash = it }
        )
    } else {
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
                    text = "🔐 Private Vault",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                        Icon(Icons.Default.Add, contentDescription = "Import File")
                    }
                    Button(onClick = { isUnlocked = false }) {
                        Text("Lock")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (fileList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Vault is empty.\nTap '+' to import files securely.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(fileList) { file ->
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
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(file.originalName, fontWeight = FontWeight.Bold)
                                    Text("${file.fileSize / 1024} KB | ${file.mimeType}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        File(file.encryptedPath).delete()
                                        dbProvider.db.vaultFileDao().delete(file)
                                        refreshFiles()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
