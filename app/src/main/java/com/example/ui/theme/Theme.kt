package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val PremiumColorScheme =
  darkColorScheme(
    primary = PremiumGold,
    onPrimary = DarkBackground,
    secondary = PremiumGoldDark,
    onSecondary = DarkBackground,
    tertiary = WhatsAppGreen,
    background = DarkBackground,
    surface = SurfaceDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextPrimary,
    outline = BorderDark,
    outlineVariant = TextSecondary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default to enforce brand colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = PremiumColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
