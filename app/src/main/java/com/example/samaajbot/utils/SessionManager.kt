package com.example.samaajbot.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "samaajbot_prefs")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TOKEN_KEY      = stringPreferencesKey("auth_token")
    private val USER_ID_KEY    = intPreferencesKey("user_id")
    private val USER_NAME_KEY  = stringPreferencesKey("user_name")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")

    val token: Flow<String?>    = context.dataStore.data.map { it[TOKEN_KEY] }
    val userId: Flow<Int?>      = context.dataStore.data.map { it[USER_ID_KEY] }
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME_KEY] }

    suspend fun saveSession(token: String, userId: Int, name: String, email: String) {
        context.dataStore.edit {
            it[TOKEN_KEY]      = token
            it[USER_ID_KEY]    = userId
            it[USER_NAME_KEY]  = name
            it[USER_EMAIL_KEY] = email
        }
    }

    suspend fun clearSession() = context.dataStore.edit { it.clear() }
}
