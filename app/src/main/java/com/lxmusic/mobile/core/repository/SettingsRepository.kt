package com.lxmusic.mobile.core.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val KEY_API_SOURCE = stringPreferencesKey("common.apiSource")
        val KEY_QUALITY = stringPreferencesKey("player.quality")
        val KEY_THEME = stringPreferencesKey("common.theme")
        val KEY_BLUR_ENABLED = booleanPreferencesKey("glass.blurEnabled")
        val KEY_BLUR_RADIUS = stringPreferencesKey("glass.blurRadius")
        val KEY_ENABLE_USER_API_LOG = booleanPreferencesKey("userApi.log")
        val KEY_PLAY_MODE = stringPreferencesKey("player.playMode")
    }

    val apiSource: Flow<String> = context.dataStore.data.map { it[KEY_API_SOURCE] ?: "kw" }
    val quality: Flow<String> = context.dataStore.data.map { it[KEY_QUALITY] ?: "128k" }
    val theme: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "system" }
    val blurEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_BLUR_ENABLED] ?: true }
    val blurRadius: Flow<Float> = context.dataStore.data.map { (it[KEY_BLUR_RADIUS] ?: "24").toFloatOrNull() ?: 24f }
    val enableUserApiLog: Flow<Boolean> = context.dataStore.data.map { it[KEY_ENABLE_USER_API_LOG] ?: false }
    val playMode: Flow<String> = context.dataStore.data.map { it[KEY_PLAY_MODE] ?: "listLoop" }

    suspend fun setApiSource(value: String) = context.dataStore.edit { it[KEY_API_SOURCE] = value }
    suspend fun setQuality(value: String) = context.dataStore.edit { it[KEY_QUALITY] = value }
    suspend fun setTheme(value: String) = context.dataStore.edit { it[KEY_THEME] = value }
    suspend fun setBlurEnabled(value: Boolean) = context.dataStore.edit { it[KEY_BLUR_ENABLED] = value }
    suspend fun setBlurRadius(value: Float) = context.dataStore.edit { it[KEY_BLUR_RADIUS] = value.toString() }
    suspend fun setEnableUserApiLog(value: Boolean) = context.dataStore.edit { it[KEY_ENABLE_USER_API_LOG] = value }
    suspend fun setPlayMode(value: String) = context.dataStore.edit { it[KEY_PLAY_MODE] = value }

    suspend fun currentApiSource(): String = apiSource.first()
    suspend fun currentQuality(): String = quality.first()
}
