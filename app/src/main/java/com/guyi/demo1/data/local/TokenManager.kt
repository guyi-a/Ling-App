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
    private val refreshTokenKey = stringPreferencesKey("refresh_token")
    private val userIdKey = stringPreferencesKey("user_id")
    private val usernameKey = stringPreferencesKey("username")

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[tokenKey] = token }
    }

    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { it[refreshTokenKey] = token }
    }

    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.first()[refreshTokenKey]
    }

    suspend fun saveUserInfo(userId: String, username: String) {
        context.dataStore.edit {
            it[userIdKey] = userId
            it[usernameKey] = username
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.first()[tokenKey]
    }

    suspend fun getUserId(): String? {
        return context.dataStore.data.first()[userIdKey]
    }

    suspend fun getUsername(): String? {
        return context.dataStore.data.first()[usernameKey]
    }

    fun getUsernameFlow(): Flow<String?> {
        return context.dataStore.data.map { it[usernameKey] }
    }

    fun getUserIdFlow(): Flow<String?> {
        return context.dataStore.data.map { it[userIdKey] }
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun hasToken(): Boolean {
        return getToken() != null
    }
}
