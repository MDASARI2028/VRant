package com.bitchat.android.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

/**
 * VRant Dark Color Scheme — VIT-inspired maroon/gold on near-black
 * Default and recommended theme for the campus chat experience
 */
private val DarkColorScheme = darkColorScheme(
    primary = VRantColors.Maroon500,            // Primary action color — vibrant crimson
    onPrimary = VRantColors.TextOnMaroon,
    primaryContainer = VRantColors.Maroon700,
    onPrimaryContainer = VRantColors.Maroon100,
    secondary = VRantColors.Gold500,            // Gold accent
    onSecondary = VRantColors.TextOnGold,
    secondaryContainer = VRantColors.Gold700,
    onSecondaryContainer = VRantColors.Gold100,
    tertiary = VRantColors.Info,
    onTertiary = VRantColors.Black,
    background = VRantColors.DarkBase,          // Near-black background
    onBackground = VRantColors.TextPrimary,
    surface = VRantColors.DarkSurface,          // Elevated card surfaces
    onSurface = VRantColors.TextPrimary,
    surfaceVariant = VRantColors.DarkElevated,
    onSurfaceVariant = VRantColors.TextSecondary,
    outline = VRantColors.DarkBorder,
    outlineVariant = VRantColors.DarkMuted,
    error = VRantColors.Error,
    onError = VRantColors.TextOnMaroon
)

/**
 * VRant Light Color Scheme — maroon/gold on warm white
 */
private val LightColorScheme = lightColorScheme(
    primary = VRantColors.Maroon700,
    onPrimary = VRantColors.TextOnMaroon,
    primaryContainer = VRantColors.Maroon100,
    onPrimaryContainer = VRantColors.Maroon900,
    secondary = VRantColors.Gold700,
    onSecondary = VRantColors.TextOnMaroon,
    secondaryContainer = VRantColors.Gold100,
    onSecondaryContainer = VRantColors.Gold900,
    tertiary = VRantColors.Info,
    onTertiary = VRantColors.LightSurface,
    background = VRantColors.LightBackground,
    onBackground = VRantColors.TextDarkPrimary,
    surface = VRantColors.LightSurface,
    onSurface = VRantColors.TextDarkPrimary,
    surfaceVariant = VRantColors.LightElevated,
    onSurfaceVariant = VRantColors.TextDarkSecondary,
    outline = VRantColors.LightBorder,
    outlineVariant = VRantColors.LightMuted,
    error = VRantColors.Error,
    onError = VRantColors.TextOnMaroon
)

/**
 * VRant Theme — wraps Material 3 with VIT campus branding.
 * Kept as `BitchatTheme` function name to avoid touching every call-site.
 * Internal naming references VRant; public API preserved for compatibility.
 */
@Composable
fun BitchatTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    // App-level override from ThemePreferenceManager
    val themePref by ThemePreferenceManager.themeFlow.collectAsState(initial = ThemePreference.System)
    val shouldUseDark = when (darkTheme) {
        true -> true
        false -> false
        null -> when (themePref) {
            ThemePreference.Dark -> true
            ThemePreference.Light -> false
            ThemePreference.System -> isSystemInDarkTheme()
        }
    }

    val colorScheme = if (shouldUseDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    if (!shouldUseDark) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = if (!shouldUseDark) {
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else 0
            }
            window.navigationBarColor = colorScheme.background.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VRantTypography,
        content = content
    )
}
