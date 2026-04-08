package com.guyi.demo1.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

class ThemeManager(private val context: Context) {

    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val themeColorKey = stringPreferencesKey("theme_color")
    private val fontSizeKey = stringPreferencesKey("font_size")
    private val autoSaveKey = booleanPreferencesKey("auto_save")
    private val notificationsKey = booleanPreferencesKey("notifications")
    private val autoCleanupKey = booleanPreferencesKey("auto_cleanup")

    // 外观
    val isDarkMode: Flow<Boolean?> = context.themeDataStore.data.map { it[darkModeKey] }

    val themeColor: Flow<String> = context.themeDataStore.data.map { it[themeColorKey] ?: "BLUE" }

    val fontSize: Flow<String> = context.themeDataStore.data.map { it[fontSizeKey] ?: "MEDIUM" }

    // 聊天设置
    val autoSave: Flow<Boolean> = context.themeDataStore.data.map { it[autoSaveKey] ?: true }

    val notifications: Flow<Boolean> = context.themeDataStore.data.map { it[notificationsKey] ?: true }

    // 工作区设置
    val autoCleanup: Flow<Boolean> = context.themeDataStore.data.map { it[autoCleanupKey] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.themeDataStore.edit { it[darkModeKey] = enabled }
    }

    suspend fun setThemeColor(color: String) {
        context.themeDataStore.edit { it[themeColorKey] = color }
    }

    suspend fun setFontSize(size: String) {
        context.themeDataStore.edit { it[fontSizeKey] = size }
    }

    suspend fun setAutoSave(enabled: Boolean) {
        context.themeDataStore.edit { it[autoSaveKey] = enabled }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.themeDataStore.edit { it[notificationsKey] = enabled }
    }

    suspend fun setAutoCleanup(enabled: Boolean) {
        context.themeDataStore.edit { it[autoCleanupKey] = enabled }
    }
}
