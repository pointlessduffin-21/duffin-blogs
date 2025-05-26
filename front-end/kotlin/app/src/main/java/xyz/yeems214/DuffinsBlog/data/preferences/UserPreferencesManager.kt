package xyz.yeems214.DuffinsBlog.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {
    
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val EMAIL_KEY = stringPreferencesKey("email")
    }
    
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN_KEY]
    }
    
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }
    
    val username: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }
    
    val email: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[EMAIL_KEY]
    }
    
    suspend fun saveAuthData(token: String, userId: String, username: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            preferences[EMAIL_KEY] = email
        }
    }
    
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USERNAME_KEY)
            preferences.remove(EMAIL_KEY)
        }
    }
}
