package org.openaac.vocal.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import org.openaac.vocal.core.domain.model.BoardThemePreset

data class BoardColors(
    val boardGridBackground: Color,
    val boardCellBackground: Color,
    val boardCellContent: Color,
)

val LocalBoardColors = staticCompositionLocalOf {
    BoardColors(
        boardGridBackground = Color(0xFF37474F),
        boardCellBackground = Color(0xFFECEFF1),
        boardCellContent = Color(0xFF1A1C1E),
    )
}

object VocalBoardColors {
    internal fun forPreset(preset: BoardThemePreset, darkTheme: Boolean): BoardColors = when (preset) {
        BoardThemePreset.DefaultBlue -> if (darkTheme) {
            BoardColors(
                boardGridBackground = Color(0xFF0D1117),
                boardCellBackground = Color(0xFF263238),
                boardCellContent = Color(0xFFECEFF1),
            )
        } else {
            BoardColors(
                boardGridBackground = Color(0xFF37474F),
                boardCellBackground = Color(0xFFE3F2FD),
                boardCellContent = Color(0xFF0D47A1),
            )
        }
        BoardThemePreset.HighContrast -> if (darkTheme) {
            BoardColors(
                boardGridBackground = Color(0xFF000000),
                boardCellBackground = Color(0xFF212121),
                boardCellContent = Color(0xFFFFFFFF),
            )
        } else {
            BoardColors(
                boardGridBackground = Color(0xFF212121),
                boardCellBackground = Color(0xFFFFFFFF),
                boardCellContent = Color(0xFF000000),
            )
        }
        BoardThemePreset.SoftPastel -> if (darkTheme) {
            BoardColors(
                boardGridBackground = Color(0xFF1A1420),
                boardCellBackground = Color(0xFF3D3350),
                boardCellContent = Color(0xFFF5EFFA),
            )
        } else {
            BoardColors(
                boardGridBackground = Color(0xFF6B5B7A),
                boardCellBackground = Color(0xFFFFF3E8),
                boardCellContent = Color(0xFF3E2F4F),
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
