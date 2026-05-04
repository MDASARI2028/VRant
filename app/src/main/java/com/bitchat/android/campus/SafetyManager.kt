package com.bitchat.android.campus

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Campus Safety Manager — block/report users, hide presence, content moderation.
 */
class SafetyManager(private val context: Context) {

    companion object {
        private const val TAG = "SafetyManager"
        private const val PREFS_NAME = "vrant_safety"
        private const val KEY_BLOCKED_USERS = "blocked_users"
        private const val KEY_REPORTED_USERS = "reported_users"
        private const val KEY_HIDE_PRESENCE = "hide_presence"
        private const val KEY_HIDE_ON_MAP = "hide_on_map"

        @Volatile
        private var instance: SafetyManager? = null

        fun getInstance(context: Context): SafetyManager {
            return instance ?: synchronized(this) {
                instance ?: SafetyManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Set of blocked user IDs */
    private val _blockedUsers = MutableStateFlow<Set<String>>(loadBlockedUsers())
    val blockedUsers: StateFlow<Set<String>> = _blockedUsers.asStateFlow()

    /** Set of reported user IDs */
    private val _reportedUsers = MutableStateFlow<Set<String>>(loadReportedUsers())
    val reportedUsers: StateFlow<Set<String>> = _reportedUsers.asStateFlow()

    /** Whether to hide presence from others */
    private val _hidePresence = MutableStateFlow(prefs.getBoolean(KEY_HIDE_PRESENCE, false))
    val hidePresence: StateFlow<Boolean> = _hidePresence.asStateFlow()

    /** Whether to hide position on campus map */
    private val _hideOnMap = MutableStateFlow(prefs.getBoolean(KEY_HIDE_ON_MAP, false))
    val hideOnMap: StateFlow<Boolean> = _hideOnMap.asStateFlow()

    /** Block a user */
    fun blockUser(userId: String) {
        val updated = _blockedUsers.value.toMutableSet().apply { add(userId) }
        _blockedUsers.value = updated
        saveBlockedUsers(updated)
        Log.d(TAG, "🚫 Blocked user: $userId")
    }

    /** Unblock a user */
    fun unblockUser(userId: String) {
        val updated = _blockedUsers.value.toMutableSet().apply { remove(userId) }
        _blockedUsers.value = updated
        saveBlockedUsers(updated)
        Log.d(TAG, "✅ Unblocked user: $userId")
    }

    /** Check if a user is blocked */
    fun isBlocked(userId: String): Boolean = _blockedUsers.value.contains(userId)

    /** Report a user */
    fun reportUser(userId: String, reason: String) {
        val updated = _reportedUsers.value.toMutableSet().apply { add(userId) }
        _reportedUsers.value = updated
        saveReportedUsers(updated)
        Log.d(TAG, "⚠️ Reported user: $userId, reason: $reason")
        // TODO: Send report to backend when online features are connected
    }

    /** Toggle presence visibility */
    fun setHidePresence(hide: Boolean) {
        _hidePresence.value = hide
        prefs.edit().putBoolean(KEY_HIDE_PRESENCE, hide).apply()
        Log.d(TAG, "👁️ Hide presence: $hide")
    }

    /** Toggle map visibility */
    fun setHideOnMap(hide: Boolean) {
        _hideOnMap.value = hide
        prefs.edit().putBoolean(KEY_HIDE_ON_MAP, hide).apply()
        Log.d(TAG, "🗺️ Hide on map: $hide")
    }

    /** Filter messages — returns true if message should be shown */
    fun shouldShowMessage(senderId: String): Boolean {
        return !isBlocked(senderId)
    }

    /** Clear all safety data (for emergency wipe) */
    fun clearAll() {
        prefs.edit().clear().apply()
        _blockedUsers.value = emptySet()
        _reportedUsers.value = emptySet()
        _hidePresence.value = false
        _hideOnMap.value = false
        Log.d(TAG, "🗑️ Safety data cleared")
    }

    // ── Persistence ──

    private fun loadBlockedUsers(): Set<String> {
        return prefs.getStringSet(KEY_BLOCKED_USERS, emptySet()) ?: emptySet()
    }

    private fun saveBlockedUsers(users: Set<String>) {
        prefs.edit().putStringSet(KEY_BLOCKED_USERS, users).apply()
    }

    private fun loadReportedUsers(): Set<String> {
        return prefs.getStringSet(KEY_REPORTED_USERS, emptySet()) ?: emptySet()
    }

    private fun saveReportedUsers(users: Set<String>) {
        prefs.edit().putStringSet(KEY_REPORTED_USERS, users).apply()
    }
}
