package com.quranmemorization.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

/*
 * ── Palette ──────────────────────────────────────────────────────────────────
 *
 * Design intent: Islamic manuscript heritage meets modern minimalism.
 *
 * Primary:    Deep teal-green  (#1A5C4F) — the green of Islamic calligraphy
 *             and Quranic illuminations.
 * Secondary:  Warm gold        (#C9A84C) — gilt decoration in manuscript borders.
 * Background: Near-white parchment (#F8F4EE) in light mode; deep charcoal in dark.
 * Surface:    White / dark-surface card.
 * Error:      Deep crimson     (#B0292B) — used for mistake indicators.
 *
 * Arabic text is rendered with the system's default Arabic font (Noto Naskh Arabic
 * is pre-installed on Android 6+).  A future iteration can bundle Amiri or Scheherazade
 * from assets/fonts/ for a more calligraphic feel.
 */

// ── Light palette ─────────────────────────────────────────────────────────────

val Teal900   = Color(0xFF1A5C4F)
val Teal700   = Color(0xFF2E7D6B)
val Teal200   = Color(0xFFB2DFDA)
val Gold600   = Color(0xFFC9A84C)
val Gold200   = Color(0xFFEDD88A)
val Parchment = Color(0xFFF8F4EE)
val Crimson   = Color(0xFFB0292B)
val DarkInk   = Color(0xFF1C1B1E)

private val LightColors = lightColorScheme(
    primary          = Teal900,
    onPrimary        = Color.White,
    primaryContainer = Teal200,
    onPrimaryContainer = Teal900,
    secondary        = Gold600,
    onSecondary      = Color.White,
    secondaryContainer = Gold200,
    onSecondaryContainer = Color(0xFF3D2E00),
    background       = Parchment,
    onBackground     = DarkInk,
    surface          = Color.White,
    onSurface        = DarkInk,
    error            = Crimson,
    onError          = Color.White,
    surfaceVariant   = Color(0xFFEBE5DE),
    onSurfaceVariant = Color(0xFF4A4540),
    outline          = Color(0xFFB5ADA5),
)

// ── Dark palette ──────────────────────────────────────────────────────────────

val DarkSurface = Color(0xFF1A1C1E)
val DarkCard    = Color(0xFF242729)

private val DarkColors = darkColorScheme(
    primary          = Teal200,
    onPrimary        = Color(0xFF003730),
    primaryContainer = Teal700,
    onPrimaryContainer = Color(0xFFB2DFDA),
    secondary        = Gold200,
    onSecondary      = Color(0xFF3D2E00),
    secondaryContainer = Color(0xFF5A4400),
    onSecondaryContainer = Gold200,
    background       = DarkSurface,
    onBackground     = Color(0xFFE4E2DE),
    surface          = DarkCard,
    onSurface        = Color(0xFFE4E2DE),
    error            = Color(0xFFFF8A80),
    onError          = Color(0xFF680003),
    surfaceVariant   = Color(0xFF2C2E30),
    onSurfaceVariant = Color(0xFFCAC4C0),
    outline          = Color(0xFF948D89),
)

// ── Typography ────────────────────────────────────────────────────────────────

/**
 * QuranTypography — Material 3 type scale.
 *
 * Display / Headline: used for Juz' numbers, progress percentages.
 * Title:              section headers, card titles.
 * Body:               Arabic verse text, descriptions.
 * Label:              chips, small metadata.
 *
 * Line heights are increased for Arabic's tall letterforms.
 */
val QuranTypography = Typography(
    displayLarge = TextStyle(
        fontWeight   = FontWeight.Bold,
        fontSize     = 57.sp,
        lineHeight   = 64.sp,
        letterSpacing= (-0.25).sp,
    ),
    headlineLarge = TextStyle(
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 32.sp,
        lineHeight   = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 26.sp,
        lineHeight   = 34.sp,
    ),
    titleLarge = TextStyle(
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 20.sp,
        lineHeight   = 28.sp,
    ),
    titleMedium = TextStyle(
        fontWeight   = FontWeight.Medium,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing= 0.15.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight   = FontWeight.Normal,
        fontSize     = 16.sp,
        lineHeight   = 28.sp,   // extra for Arabic
        letterSpacing= 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 24.sp,
        letterSpacing= 0.25.sp,
    ),
    labelLarge = TextStyle(
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing= 0.1.sp,
    ),
    labelSmall = TextStyle(
        fontWeight   = FontWeight.Medium,
        fontSize     = 11.sp,
        lineHeight   = 16.sp,
        letterSpacing= 0.5.sp,
    ),
)

// ── Theme composable ──────────────────────────────────────────────────────────

@Composable
fun QuranMemorizationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = QuranTypography,
        content     = content,
    )
}
