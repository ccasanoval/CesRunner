package com.cesoft.cesrunner.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private const val PREFERENCE_NAME = "CesRunnerDataStore"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_NAME)

/// Holds the user's LLM API keys. Excluded from backup and device transfer in app/src/main/res/xml/
/// backup_rules.xml and data_extraction_rules.xml: renaming it requires updating both files
const val SECRETS_DATASTORE_NAME = "CesRunnerSecrets"
private val Context.secretsDataStore: DataStore<Preferences> by preferencesDataStore(name = SECRETS_DATASTORE_NAME)

suspend fun Context.writeSecret(key: String, value: String) {
    secretsDataStore.edit { pref -> pref[stringPreferencesKey(key)] = value }
}
suspend fun Context.readSecret(key: String, default: String = ""): String {
    val prefKey = stringPreferencesKey(key)
    secretsDataStore.data.first()[prefKey]?.let { return it }
    // Keys saved before the secrets DataStore existed: move them out of the backed up DataStore
    val legacy = dataStore.data.first()[prefKey] ?: return default
    secretsDataStore.edit { pref -> pref[prefKey] = legacy }
    dataStore.edit { pref -> pref.remove(prefKey) }
    return legacy
}

suspend fun Context.writeString(key: String, value: String) {
    dataStore.edit { pref -> pref[stringPreferencesKey(key)] = value }
}
suspend fun Context.readString(key: String, default: String = ""): String {
    return dataStore.data.map { pref ->
        pref[stringPreferencesKey(key)]
    }.firstOrNull() ?: default
}

suspend fun Context.writeDouble(key: String, value: Double) {
    dataStore.edit { pref -> pref[doublePreferencesKey(key)] = value }
}
suspend fun Context.readDouble(key: String, default: Double = 0.0): Double {
    return dataStore.data.map { pref ->
        pref[doublePreferencesKey(key)]
    }.firstOrNull() ?: default
}

suspend fun Context.writeInt(key: String, value: Int) {
    dataStore.edit { pref -> pref[intPreferencesKey(key)] = value }
}
suspend fun Context.readInt(key: String, default: Int = 0): Int {
    return dataStore.data.map { pref ->
        pref[intPreferencesKey(key)]
    }.firstOrNull() ?: default
}

suspend fun Context.writeLong(key: String, value: Long) {
    dataStore.edit { pref -> pref[longPreferencesKey(key)] = value }
}
suspend fun Context.readLong(key: String, default: Long = 0): Long {
    return dataStore.data.map { pref ->
        pref[longPreferencesKey(key)]
    }.firstOrNull() ?: default
}
fun Context.readLongFlow(key: String): Flow<Long?> {
    return dataStore.data.map { pref ->
        pref[longPreferencesKey(key)]
    }
}

suspend fun Context.writeBool(key: String, value: Boolean) {
    dataStore.edit { pref -> pref[booleanPreferencesKey(key)] = value }
}
suspend fun Context.readBool(key: String, default: Boolean = false): Boolean {
    return dataStore.data.map { pref ->
        pref[booleanPreferencesKey(key)]
    }.firstOrNull() ?: default
}
