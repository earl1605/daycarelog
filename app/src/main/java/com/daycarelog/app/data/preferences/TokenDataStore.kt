package com.daycarelog.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dcl_prefs")

object TokenDataStore {
    private val TOKEN_KEY = stringPreferencesKey("dcl_token")
    private val USER_KEY  = stringPreferencesKey("dcl_user")

    fun getToken(context: Context): Flow<String?> =
        context.dataStore.data.map { it[TOKEN_KEY] }

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun saveUser(context: Context, userJson: String) {
        context.dataStore.edit { it[USER_KEY] = userJson }
    }

    fun getUser(context: Context): Flow<String?> =
        context.dataStore.data.map { it[USER_KEY] }

    suspend fun clear(context: Context) {
        context.dataStore.edit { it.clear() }
    }
}
