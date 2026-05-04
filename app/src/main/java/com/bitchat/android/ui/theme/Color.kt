package com.bitchat.android.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * VRant — VIT Campus Chat Platform Color Palette
 * Inspired by Vellore Institute of Technology branding
 * 
 * Primary: Deep maroon / crimson (VIT heritage)
 * Accent: Warm gold (VIT gold)
 * Base: Near-black / dark charcoal
 * Surface: Dark elevated surfaces
 */
object VRantColors {
    // ── Primary Maroon Spectrum ──
    val Maroon900 = Color(0xFF4A0E0E)
    val Maroon800 = Color(0xFF6B1515)
    val Maroon700 = Color(0xFF8B1A1A)
    val Maroon600 = Color(0xFFA32222)
    val Maroon500 = Color(0xFFCC2936) // Primary action color
    val Maroon400 = Color(0xFFE0434F)
    val Maroon300 = Color(0xFFEF6B6B)
    val Maroon200 = Color(0xFFF5A3A3)
    val Maroon100 = Color(0xFFFCE4E4)

    // ── Gold Accent Spectrum ──
    val Gold900 = Color(0xFF5C4A00)
    val Gold700 = Color(0xFF8C7200)
    val Gold500 = Color(0xFFC4A747) // Secondary accent
    val Gold400 = Color(0xFFD4BC6A)
    val Gold300 = Color(0xFFE5D48F)
    val Gold200 = Color(0xFFF0E5B8)
    val Gold100 = Color(0xFFFAF5E4)

    // ── Dark Base Palette ──
    val Black = Color(0xFF000000)
    val DarkBase = Color(0xFF0A0A0A)      // App background
    val DarkSurface = Color(0xFF141414)    // Cards, sheets
    val DarkElevated = Color(0xFF1C1C1E)  // Elevated surfaces
    val DarkBorder = Color(0xFF2C2C2E)    // Subtle borders
    val DarkMuted = Color(0xFF3A3A3C)     // Muted elements

    // ── Light Base Palette ──
    val LightBackground = Color(0xFFFAF8F5)
    val LightSurface = Color(0xFFFFFFFF)
    val LightElevated = Color(0xFFF2EFEB)
    val LightBorder = Color(0xFFE0DDD8)
    val LightMuted = Color(0xFFC7C4BF)

    // ── Text Colors ──
    val TextPrimary = Color(0xFFF5F5F5)
    val TextSecondary = Color(0xFFB0B0B0)
    val TextMuted = Color(0xFF707070)
    val TextOnMaroon = Color(0xFFFFFFFF)
    val TextOnGold = Color(0xFF1A1A1A)
    val TextDarkPrimary = Color(0xFF1A1A1A)
    val TextDarkSecondary = Color(0xFF4A4A4A)

    // ── Semantic Colors ──
    val Success = Color(0xFF34C759)
    val Warning = Color(0xFFFF9F0A)
    val Error = Color(0xFFFF453A)
    val Info = Color(0xFF5AC8FA)

    // ── Chat Bubble Colors ──
    val BubbleSent = Color(0xFF8B1A1A)         // Maroon for sent
    val BubbleSentText = Color(0xFFFFFFFF)
    val BubbleReceived = Color(0xFF1C1C1E)     // Dark gray for received
    val BubbleReceivedText = Color(0xFFF5F5F5)
    val BubbleSystem = Color(0xFF2C2C2E)       // Subtle for system messages

    // ── Zone / Map Colors ──
    val ZoneAcademic = Color(0xFFCC2936)       // Maroon - academic blocks
    val ZoneHostel = Color(0xFF5856D6)         // Indigo - hostels
    val ZoneCafeteria = Color(0xFFFF9F0A)      // Amber - food
    val ZoneLibrary = Color(0xFF30B0C7)        // Teal - library
    val ZoneSports = Color(0xFF34C759)         // Green - sports
    val ZoneAdmin = Color(0xFFC4A747)          // Gold - admin
    val ZoneDefault = Color(0xFF8E8E93)        // Gray - misc

    // ── Online / Offline Indicators ──
    val OnlineGreen = Color(0xFF34C759)
    val OfflineGray = Color(0xFF636366)
    val MeshBlue = Color(0xFF007AFF)
    val EncryptedOrange = Color(0xFFFF9F0A)
    val VerifiedGold = Color(0xFFC4A747)

    // ── Navigation ──
    val NavSelected = Maroon500
    val NavUnselected = Color(0xFF8E8E93)
    val NavBackground = Color(0xFF0A0A0A)
    val NavIndicator = Maroon500.copy(alpha = 0.12f)
}
