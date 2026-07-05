package org.openaac.vocal.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.openaac.vocal.core.domain.model.BoardThemePreset

private val DefaultBlueLightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    secondary = Color(0xFF00838F),
    onSecondary = Color.White,
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
)

private val DefaultBlueDarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),
    secondary = Color(0xFF80DEEA),
    onSecondary = Color(0xFF00363A),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE1E3E8),
    surface = Color(0xFF181C20),
    onSurface = Color(0xFFE1E3E8),
)

private val HighContrastLightColors = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    secondary = Color(0xFF003B73),
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
)

private val HighContrastDarkColors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = Color(0xFFFFD54F),
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF101010),
    onSurface = Color.White,
)

private val SoftPastelLightColors = lightColorScheme(
    primary = Color(0xFF7B61A8),
    onPrimary = Color.White,
    secondary = Color(0xFF6F8F72),
    onSecondary = Color.White,
    background = Color(0xFFFFF8F0),
    onBackground = Color(0xFF2B2118),
    surface = Color(0xFFFFFBF7),
    onSurface = Color(0xFF2B2118),
)

private val SoftPastelDarkColors = darkColorScheme(
    primary = Color(0xFFD8B8FF),
    onPrimary = Color(0xFF3A205F),
    secondary = Color(0xFFC3D8B7),
    onSecondary = Color(0xFF253523),
    background = Color(0xFF201A22),
    onBackground = Color(0xFFEDE0EA),
    surface = Color(0xFF2A232D),
    onSurface = Color(0xFFEDE0EA),
)

private val HighContrastTypography = VocalTypography.copy(
    headlineMedium = VocalTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    titleLarge = VocalTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
    bodyLarge = VocalTypography.bodyLarge.copy(fontSize = 20.sp, lineHeight = 28.sp),
    labelLarge = VocalTypography.labelLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
)

private val SoftPastelTypography = VocalTypography.copy(
    headlineMedium = VocalTypography.headlineMedium.copy(fontWeight = FontWeight.Medium),
    titleLarge = VocalTypography.titleLarge.copy(fontWeight = FontWeight.Medium),
    bodyLarge = VocalTypography.bodyLarge.copy(fontSize = 19.sp, lineHeight = 28.sp),
    labelLarge = VocalTypography.labelLarge.copy(fontSize = 17.sp),
)

/**
 * Material styling for each caregiver-selectable board theme preset.
 */
object VocalThemePresets {
    val all: List<BoardThemePreset> = BoardThemePreset.entries

    internal fun colorsFor(preset: BoardThemePreset, darkTheme: Boolean) = when (preset) {
        BoardThemePreset.DefaultBlue -> if (darkTheme) DefaultBlueDarkColors else DefaultBlueLightColors
        BoardThemePreset.HighContrast -> if (darkTheme) HighContrastDarkColors else HighContrastLightColors
        BoardThemePreset.SoftPastel -> if (darkTheme) SoftPastelDarkColors else SoftPastelLightColors
    }

    internal fun typographyFor(preset: BoardThemePreset): Typography = when (preset) {
        BoardThemePreset.DefaultBlue -> VocalTypography
        BoardThemePreset.HighContrast -> HighContrastTypography
        BoardThemePreset.SoftPastel -> SoftPastelTypography
    }
}

@Composable
fun VocalTheme(
    boardThemePreset: BoardThemePreset = BoardThemePreset.Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = VocalThemePresets.colorsFor(boardThemePreset, darkTheme),
        typography = VocalThemePresets.typographyFor(boardThemePreset),
    ) {
        ProvideBoardColors(
            boardThemePreset = boardThemePreset,
            darkTheme = darkTheme,
            content = content,
        )
    }
}
