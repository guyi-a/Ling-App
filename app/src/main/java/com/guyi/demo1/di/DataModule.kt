package com.guyi.demo1.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// DataStore 扩展属性
private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "token_prefs")
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    @TokenDataStore
    fun provideTokenDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.tokenDataStore
    }

    @Provides
    @Singleton
    @SettingsDataStore
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.settingsDataStore
    }
}

// Qualifiers for different DataStores
@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TokenDataStore

@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SettingsDataStore
