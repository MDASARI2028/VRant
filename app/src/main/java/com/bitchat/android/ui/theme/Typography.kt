package com.bitchat.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Base font size for consistent scaling across the app
internal const val BASE_FONT_SIZE = com.bitchat.android.util.AppConstants.UI.BASE_FONT_SIZE_SP // sp

/**
 * VRant Typography — modern sans-serif for UI, monospace for message content.
 * Uses system default sans-serif (which is typically Roboto on Android)
 * for a clean, Gen Z-friendly aesthetic while keeping monospace for chat bubbles.
 */
val VRantTypography = Typography(
    // Display styles — for splash / hero text
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),

    // Headlines — section headers, screen titles
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = (BASE_FONT_SIZE + 3).sp,
        lineHeight = (BASE_FONT_SIZE + 9).sp
    ),

    // Title — navigation bars, cards
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = (BASE_FONT_SIZE + 1).sp,
        lineHeight = (BASE_FONT_SIZE + 7).sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body — primary content text (uses monospace for chat parity)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = (BASE_FONT_SIZE + 1).sp,
        lineHeight = (BASE_FONT_SIZE + 7).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = BASE_FONT_SIZE.sp,
        lineHeight = (BASE_FONT_SIZE + 3).sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = (BASE_FONT_SIZE - 3).sp,
        lineHeight = (BASE_FONT_SIZE + 1).sp
    ),

    // Label — buttons, chips, badges
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = (BASE_FONT_SIZE - 2).sp,
        lineHeight = (BASE_FONT_SIZE + 3).sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = (BASE_FONT_SIZE - 4).sp,
        lineHeight = (BASE_FONT_SIZE + 1).sp,
        letterSpacing = 0.5.sp
    )
)
