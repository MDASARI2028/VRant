package com.bitchat.android.campus

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * VIT User Profile — extended identity model for campus-specific features
 */
@Parcelize
data class VITUserProfile(
    val nickname: String,
    val department: VITDepartment = VITDepartment.UNSET,
    val year: Int = 0, // 1-5 (includes integrated programs)
    val registrationNumber: String? = null, // e.g. "21BCE1234"
    val email: String? = null,
    val isVerified: Boolean = false, // VIT email verified
    val isAnonymous: Boolean = true, // anonymous mode (mesh-only)
    val avatarSeed: String = nickname, // used for deterministic avatar generation
    val bio: String? = null,
    val joinedCommunities: List<String> = emptyList()
) : Parcelable {

    val displayName: String
        get() = if (isAnonymous) nickname else (registrationNumber?.take(8) ?: nickname)

    val yearSuffix: String
        get() = when (year) {
            1 -> "1st Year"
            2 -> "2nd Year"
            3 -> "3rd Year"
            4 -> "4th Year"
            5 -> "5th Year"
            else -> ""
        }

    val profileSubtitle: String
        get() {
            val parts = mutableListOf<String>()
            if (department != VITDepartment.UNSET) parts.add(department.shortName)
            if (year > 0) parts.add(yearSuffix)
            if (isVerified) parts.add("✓ Verified")
            return parts.joinToString(" • ")
        }
}

@Parcelize
enum class VITDepartment(val shortName: String, val fullName: String) : Parcelable {
    UNSET("", "Not Set"),
    CSE("CSE", "Computer Science & Engineering"),
    CSE_AI("CSE-AI", "CSE (AI & ML)"),
    CSE_DS("CSE-DS", "CSE (Data Science)"),
    CSE_IOT("CSE-IoT", "CSE (IoT)"),
    CSE_CYBER("CSE-CS", "CSE (Cybersecurity)"),
    CSE_BS("CSE-BS", "CSE (Bioinformatics)"),
    IT("IT", "Information Technology"),
    ECE("ECE", "Electronics & Communication"),
    EEE("EEE", "Electrical & Electronics"),
    MECH("MECH", "Mechanical Engineering"),
    CIVIL("CIVIL", "Civil Engineering"),
    CHEM("CHEM", "Chemical Engineering"),
    BIOTECH("BT", "Biotechnology"),
    BBA("BBA", "Bachelor of Business Administration"),
    LAW("LAW", "VIT School of Law"),
    ARCH("ARCH", "Architecture"),
    OTHER("Other", "Other Department")
}
