package org.openaac.vocal.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import org.openaac.vocal.core.domain.model.BoardThemePreset

data class BoardColors(
    val gridBackground: Color,
    val cellBackground: Color,
    val cellContent: Color,
)

val LocalBoardColors = staticCompositionLocalOf {
    BoardColors(
        gridBackground = Color(0xFF37474F),
        cellBackground = Color(0xFFECEFF1),
        cellContent = Color(0xFF1A1C1E),
    )
}

object VocalBoardColors {
    internal fun forPreset(preset: BoardThemePreset, darkTheme: Boolean): BoardColors = when (preset) {
        BoardThemePreset.DefaultBlue -> if (darkTheme) {
            BoardColors(
                gridBackground = Color(0xFF0D1117),
                cellBackground = Color(0xFF263238),
                cellContent = Color(0xFFECEFF1),
            )
        } else {
            BoardColors(
                gridBackground = Color(0xFF37474F),
                cellBackground = Color(0xFFE3F2FD),
                cellContent = Color(0xFF0D47A1),
            )
        }
        BoardThemePreset.HighContrast -> if (darkTheme) {
            BoardColors(
                gridBackground = Color(0xFF000000),
                cellBackground = Color(0xFF212121),
                cellContent = Color(0xFFFFFFFF),
            )
        } else {
            BoardColors(
                gridBackground = Color(0xFF212121),
                cellBackground = Color(0xFFFFFFFF),
                cellContent = Color(0xFF000000),
            )
        }
        BoardThemePreset.SoftPastel -> if (darkTheme) {
            BoardColors(
                gridBackground = Color(0xFF1A1420),
                cellBackground = Color(0xFF3D3350),
                cellContent = Color(0xFFF5EFFA),
            )
        } else {
            BoardColors(
                gridBackground = Color(0xFF6B5B7A),
                cellBackground = Color(0xFFFFF3E8),
                cellContent = Color(0xFF3E2F4F),
            )
        }
    }
}

@Composable
fun ProvideBoardColors(
    boardThemePreset: BoardThemePreset,
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalBoardColors provides VocalBoardColors.forPreset(boardThemePreset, darkTheme),
        content = content,
    )
}

@Composable
fun boardColors(): BoardColors = LocalBoardColors.current
