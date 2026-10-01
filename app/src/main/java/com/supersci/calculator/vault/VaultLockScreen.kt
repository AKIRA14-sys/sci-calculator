package com.supersci.calculator.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun VaultLockScreen(
    onUnlockSuccess: () -> Unit,
    savedPinHash: String?,
    onSetPin: (String) -> Unit
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val isSetupMode = savedPinHash == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Vault Lock",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSetupMode) "Setup Private Vault PIN" else "Enter Private Vault PIN",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = enteredPin,
            onValueChange = {
                if (it.length <= 8) enteredPin = it
            },
            label = { Text("4-8 Digit PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (enteredPin.length < 4) {
                    errorMessage = "PIN must be at least 4 digits"
                    return@Button
                }

                val vaultManager = VaultManager(context)
                val hash = vaultManager.hashPin(enteredPin)

                if (isSetupMode) {
                    onSetPin(hash)
                    onUnlockSuccess()
                } else {
                    if (hash == savedPinHash) {
                        onUnlockSuccess()
                    } else {
                        errorMessage = "Incorrect PIN"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(if (isSetupMode) "Set PIN & Unlock" else "Unlock Vault")
        }
    }
}
