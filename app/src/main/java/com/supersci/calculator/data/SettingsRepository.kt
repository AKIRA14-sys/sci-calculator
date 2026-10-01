package com.supersci.calculator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

class SettingsRepository(private val context: Context) {

    companion object {
        val ANGLE_MODE = stringPreferencesKey("angle_mode")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val VAULT_AUTH_METHOD = stringPreferencesKey("vault_auth_method")
        val VAULT_AUTO_LOCK = stringPreferencesKey("vault_auto_lock")
        val VAULT_PIN_HASH = stringPreferencesKey("vault_pin_hash")
        val DARK_MODE_OPTION = stringPreferencesKey("dark_mode_option")
    }

    val angleModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ANGLE_MODE] ?: "DEG"
    }

    val vaultAuthMethodFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[VAULT_AUTH_METHOD] ?: "PIN"
    }

    val vaultAutoLockFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[VAULT_AUTO_LOCK] ?: "1M"
    }

    val vaultPinHashFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[VAULT_PIN_HASH]
    }

    suspend fun setAngleMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[ANGLE_MODE] = mode
        }
    }

    suspend fun setVaultAuthMethod(method: String) {
        context.dataStore.edit { prefs ->
            prefs[VAULT_AUTH_METHOD] = method
        }
    }

    suspend fun setVaultAutoLock(timeout: String) {
        context.dataStore.edit { prefs ->
            prefs[VAULT_AUTO_LOCK] = timeout
        }
    }

    suspend fun setVaultPinHash(hash: String) {
        context.dataStore.edit { prefs ->
            prefs[VAULT_PIN_HASH] = hash
        }
    }
}
