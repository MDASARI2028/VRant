package com.bitchat.android.campus

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * VIT Authentication & Profile Manager
 * Handles VIT email validation, profile persistence, and anonymous mode.
 */
class VITAuthManager(private val context: Context) {

    companion object {
        private const val TAG = "VITAuthManager"
        private const val PREFS_NAME = "vrant_auth"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_DEPARTMENT = "department"
        private const val KEY_YEAR = "year"
        private const val KEY_REG_NUMBER = "reg_number"
        private const val KEY_EMAIL = "email"
        private const val KEY_IS_VERIFIED = "is_verified"
        private const val KEY_IS_ANONYMOUS = "is_anonymous"
        private const val KEY_BIO = "bio"
        private const val KEY_ONBOARDED = "has_onboarded"

        /** Valid VIT email domains */
        private val VIT_EMAIL_DOMAINS = listOf(
            "vit.ac.in",
            "vitstudent.ac.in",
            "vitbhopal.ac.in",
            "vitap.ac.in",
            "vitchennai.ac.in"
        )

        @Volatile
        private var instance: VITAuthManager? = null

        fun getInstance(context: Context): VITAuthManager {
            return instance ?: synchronized(this) {
                instance ?: VITAuthManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs by lazy {
        try {
            val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create encrypted prefs, falling back to plain: ${e.message}")
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<VITUserProfile> = _profile.asStateFlow()

    private val _hasOnboarded = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDED, false))
    val hasOnboarded: StateFlow<Boolean> = _hasOnboarded.asStateFlow()

    /** Validate a VIT email address */
    fun isValidVITEmail(email: String): Boolean {
        val trimmed = email.trim().lowercase()
        if (!trimmed.contains("@")) return false
        val domain = trimmed.substringAfter("@")
        return VIT_EMAIL_DOMAINS.any { domain == it }
    }

    /** Validate a VIT registration number (e.g. 21BCE1234) */
    fun isValidRegNumber(regNumber: String): Boolean {
        val pattern = Regex("^\\d{2}[A-Z]{2,4}\\d{4,5}$")
        return pattern.matches(regNumber.trim().uppercase())
    }

    /** Update the user profile */
    fun updateProfile(profile: VITUserProfile) {
        _profile.value = profile
        saveProfile(profile)
        Log.d(TAG, "✅ Profile updated: ${profile.nickname}, dept=${profile.department.shortName}")
    }

    /** Set nickname only (quick update from chat header) */
    fun setNickname(nickname: String) {
        val updated = _profile.value.copy(nickname = nickname)
        updateProfile(updated)
    }

    /** Verify VIT email (client-side domain check for now) */
    fun verifyEmail(email: String): Boolean {
        if (!isValidVITEmail(email)) {
            Log.w(TAG, "❌ Invalid VIT email: $email")
            return false
        }
        val updated = _profile.value.copy(
            email = email,
            isVerified = true,
            isAnonymous = false
        )
        updateProfile(updated)
        Log.d(TAG, "✅ VIT email verified: $email")
        return true
    }

    /** Toggle anonymous mode */
    fun setAnonymous(anonymous: Boolean) {
        val updated = _profile.value.copy(isAnonymous = anonymous)
        updateProfile(updated)
    }

    /** Complete onboarding */
    fun completeOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDED, true).apply()
        _hasOnboarded.value = true
    }

    /** Clear all profile data (for emergency wipe) */
    fun clearProfile() {
        prefs.edit().clear().apply()
        _profile.value = VITUserProfile(nickname = "anon")
        _hasOnboarded.value = false
        Log.d(TAG, "🗑️ Profile cleared")
    }

    // ── Persistence ──

    private fun loadProfile(): VITUserProfile {
        return VITUserProfile(
            nickname = prefs.getString(KEY_NICKNAME, "anon") ?: "anon",
            department = try {
                VITDepartment.valueOf(prefs.getString(KEY_DEPARTMENT, "UNSET") ?: "UNSET")
            } catch (_: Exception) {
                VITDepartment.UNSET
            },
            year = prefs.getInt(KEY_YEAR, 0),
            registrationNumber = prefs.getString(KEY_REG_NUMBER, null),
            email = prefs.getString(KEY_EMAIL, null),
            isVerified = prefs.getBoolean(KEY_IS_VERIFIED, false),
            isAnonymous = prefs.getBoolean(KEY_IS_ANONYMOUS, true),
            bio = prefs.getString(KEY_BIO, null)
        )
    }

    private fun saveProfile(profile: VITUserProfile) {
        prefs.edit()
            .putString(KEY_NICKNAME, profile.nickname)
            .putString(KEY_DEPARTMENT, profile.department.name)
            .putInt(KEY_YEAR, profile.year)
            .putString(KEY_REG_NUMBER, profile.registrationNumber)
            .putString(KEY_EMAIL, profile.email)
            .putBoolean(KEY_IS_VERIFIED, profile.isVerified)
            .putBoolean(KEY_IS_ANONYMOUS, profile.isAnonymous)
            .putString(KEY_BIO, profile.bio)
            .apply()
    }
}
