package com.bitchat.android.online

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * VRant API Client — handles all online backend communication.
 * Uses OkHttp (already in project) for REST calls.
 *
 * Designed with Supabase-compatible REST API patterns.
 * Can be pointed at either a custom Node/Express backend or Supabase.
 */
class VRantApiClient(private val context: Context) {

    companion object {
        private const val TAG = "VRantApiClient"

        // TODO: Replace with actual backend URL
        private const val BASE_URL = "https://your-supabase-project.supabase.co/rest/v1"
        private const val ANON_KEY = "your-supabase-anon-key"

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        @Volatile
        private var instance: VRantApiClient? = null

        fun getInstance(context: Context): VRantApiClient {
            return instance ?: synchronized(this) {
                instance ?: VRantApiClient(context.applicationContext).also { instance = it }
            }
        }
    }

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build()
            chain.proceed(request)
        }
        .build()

    private var authToken: String? = null

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // ── Auth Endpoints ──

    /**
     * Register/login with VIT email
     */
    suspend fun signInWithEmail(email: String, password: String): Result<AuthResponse> {
        return postRequest(
            "/auth/v1/token?grant_type=password",
            mapOf("email" to email, "password" to password)
        )
    }

    /**
     * Sign up with VIT email
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<AuthResponse> {
        return postRequest(
            "/auth/v1/signup",
            mapOf("email" to email, "password" to password)
        )
    }

    /**
     * Set auth token for subsequent requests
     */
    fun setAuthToken(token: String) {
        authToken = token
        _isOnline.value = true
        Log.d(TAG, "✅ Auth token set, online mode enabled")
    }

    /**
     * Clear auth
     */
    fun clearAuth() {
        authToken = null
        _isOnline.value = false
    }

    // ── Community Endpoints ──

    /**
     * Fetch all communities
     */
    suspend fun getCommunities(): Result<List<OnlineCommunity>> {
        return getRequest("/communities?select=*&order=member_count.desc")
    }

    /**
     * Join a community
     */
    suspend fun joinCommunity(communityId: String, userId: String): Result<Unit> {
        return postRequest<Unit>(
            "/community_members",
            mapOf("community_id" to communityId, "user_id" to userId)
        )
    }

    /**
     * Leave a community
     */
    suspend fun leaveCommunity(communityId: String, userId: String): Result<Unit> {
        return deleteRequest("/community_members?community_id=eq.$communityId&user_id=eq.$userId")
    }

    // ── Message Endpoints ──

    /**
     * Send a message to a community or DM
     */
    suspend fun sendMessage(message: OnlineMessage): Result<OnlineMessage> {
        return postRequest("/messages", message)
    }

    /**
     * Fetch messages for a channel
     */
    suspend fun getMessages(channelId: String, limit: Int = 50): Result<List<OnlineMessage>> {
        return getRequest("/messages?channel_id=eq.$channelId&order=created_at.desc&limit=$limit")
    }

    // ── User Profile Endpoints ──

    /**
     * Update user profile on server
     */
    suspend fun updateProfile(profile: OnlineUserProfile): Result<OnlineUserProfile> {
        return postRequest("/profiles", profile)
    }

    /**
     * Get user profile
     */
    suspend fun getProfile(userId: String): Result<OnlineUserProfile> {
        return getRequest("/profiles?id=eq.$userId&select=*")
    }

    // ── Zone Activity Endpoints ──

    /**
     * Report zone presence (anonymous)
     */
    suspend fun reportZonePresence(zoneId: String): Result<Unit> {
        return postRequest<Unit>(
            "/zone_activity",
            mapOf(
                "zone_id" to zoneId,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    /**
     * Get zone activity counts
     */
    suspend fun getZoneActivity(): Result<Map<String, Int>> {
        return getRequest("/zone_activity?select=zone_id,count&group_by=zone_id")
    }

    // ── Generic HTTP Methods ──

    private suspend inline fun <reified T> getRequest(endpoint: String): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL$endpoint")
                    .apply {
                        authToken?.let { addHeader("Authorization", "Bearer $it") }
                    }
                    .get()
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val type = object : TypeToken<T>() {}.type
                    val result = gson.fromJson<T>(body, type)
                    Result.success(result)
                } else {
                    Result.failure(ApiException(response.code, response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "GET $endpoint failed: ${e.message}")
                Result.failure(e)
            }
        }
    }

    private suspend inline fun <reified T> postRequest(endpoint: String, body: Any): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = gson.toJson(body).toRequestBody(JSON_MEDIA_TYPE)
                val request = Request.Builder()
                    .url("$BASE_URL$endpoint")
                    .apply {
                        authToken?.let { addHeader("Authorization", "Bearer $it") }
                    }
                    .post(jsonBody)
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    if (responseBody.isNotEmpty() && T::class != Unit::class) {
                        val type = object : TypeToken<T>() {}.type
                        Result.success(gson.fromJson(responseBody, type))
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        Result.success(Unit as T)
                    }
                } else {
                    Result.failure(ApiException(response.code, response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "POST $endpoint failed: ${e.message}")
                Result.failure(e)
            }
        }
    }

    private suspend fun deleteRequest(endpoint: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL$endpoint")
                    .apply {
                        authToken?.let { addHeader("Authorization", "Bearer $it") }
                    }
                    .delete()
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(ApiException(response.code, response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "DELETE $endpoint failed: ${e.message}")
                Result.failure(e)
            }
        }
    }

    fun shutdown() {
        scope.cancel()
        httpClient.dispatcher.executorService.shutdown()
        httpClient.connectionPool.evictAll()
    }
}

class ApiException(val code: Int, message: String) : Exception("HTTP $code: $message")

// ── Data Models ──

data class AuthResponse(
    val access_token: String? = null,
    val token_type: String? = null,
    val expires_in: Long? = null,
    val user: AuthUser? = null
)

data class AuthUser(
    val id: String? = null,
    val email: String? = null
)

data class OnlineCommunity(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val emoji: String = "",
    val category: String = "",
    val member_count: Int = 0,
    val created_at: String? = null
)

data class OnlineMessage(
    val id: String? = null,
    val channel_id: String = "",
    val sender_id: String = "",
    val sender_name: String = "",
    val content: String = "",
    val message_type: String = "text",
    val created_at: String? = null,
    val is_encrypted: Boolean = false
)

data class OnlineUserProfile(
    val id: String = "",
    val nickname: String = "",
    val department: String? = null,
    val year: Int? = null,
    val registration_number: String? = null,
    val is_verified: Boolean = false,
    val avatar_url: String? = null,
    val bio: String? = null,
    val updated_at: String? = null
)
