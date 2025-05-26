package xyz.yeems214.DuffinsBlog.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import xyz.yeems214.DuffinsBlog.data.api.BlogApiService
import xyz.yeems214.DuffinsBlog.data.model.*
import xyz.yeems214.DuffinsBlog.data.preferences.UserPreferencesManager

class AuthRepository(
    private val apiService: BlogApiService,
    private val userPreferencesManager: UserPreferencesManager
) {
    
    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val authResponse = response.body()!!
                userPreferencesManager.saveAuthData(
                    token = authResponse.token,
                    userId = authResponse.user.id,
                    username = authResponse.user.username,
                    email = authResponse.user.email
                )
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(username: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.register(RegisterRequest(username, email, password))
            if (response.isSuccessful) {
                val authResponse = response.body()!!
                userPreferencesManager.saveAuthData(
                    token = authResponse.token,
                    userId = authResponse.user.id,
                    username = authResponse.user.username,
                    email = authResponse.user.email
                )
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        userPreferencesManager.clearAuthData()
    }
    
    suspend fun isLoggedIn(): Boolean {
        return try {
            withTimeoutOrNull(5000L) { // 5 second timeout
                userPreferencesManager.authToken.first() != null
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getAuthToken(): String? {
        return try {
            withTimeoutOrNull(5000L) { // 5 second timeout
                userPreferencesManager.authToken.first()
            }
        } catch (e: Exception) {
            null
        }
    }
}
