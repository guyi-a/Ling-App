package com.guyi.demo1.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// DataStore 扩展属性
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

/**
 * JWT Token 管理器
 * 使用 DataStore 存储 Token
 */
class TokenManager(private val context: Context) {

    private val tokenKey = stringPreferencesKey("access_token")

    /**
     * 保存 Token
     */
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    /**
     * 获取 Token（挂起函数）
     */
    suspend fun getToken(): String? {
        return context.dataStore.data.first()[tokenKey]
    }

    /**
     * 获取 Token（Flow）
     */
    fun getTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[tokenKey]
        }
    }

    /**
     * 清除 Token（登出）
     */
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }

    /**
     * 检查 Token 是否存在
     */
    suspend fun hasToken(): Boolean {
        return getToken() != null
    }
}
