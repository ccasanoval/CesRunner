package com.cesoft.cesrunner.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

private const val sharedPrefsFile = "CesRunnerSecure"
private val mainKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
private fun getEncryptedPrefs(context: Context) = EncryptedSharedPreferences.create(
    sharedPrefsFile,
    mainKeyAlias,
    context,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
fun Context.writeSecure(key: String, value: String) {
    val pref: SharedPreferences = getEncryptedPrefs(this)
    pref.edit().putString(key, value).apply()
}
fun Context.readSecure(key: String): String? {
    val pref: SharedPreferences = getEncryptedPrefs(this)
    return pref.getString(key, null)
}
fun Context.deleteSecure(key: String) {
    val pref: SharedPreferences = getEncryptedPrefs(this)
    pref.edit().remove(key).apply()
}

private const val tokenField = "CesRunnerToken"
fun Context.setToken(token: String?) {
    token?.let {
        this.writeSecure(tokenField, token)
    } ?: run {
        this.deleteSecure(tokenField)
    }
}
fun Context.getToken() = this.readSecure(tokenField)

