package org.openaac.vocal.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.openaac.vocal.core.domain.model.MAX_PHRASE_GROUPS

/**
 * Fixed palette of subtle group background tints (max [MAX_PHRASE_GROUPS]).
 * Colors are auto-assigned by [colorIndex]; caregivers do not pick custom colors.
 *
 * Shades stay soft so [BoardColors.boardCellContent] remains readable on tinted cells.
 */
object PhraseGroupPalette {
    private val lightTints = listOf(
        Color(0xFFD6EAF8), // soft blue
        Color(0xFFD5F5E3), // soft green
        Color(0xFFFDEBD0), // soft amber
        Color(0xFFF5D0E6), // soft rose
        Color(0xFFD6F5F2), // soft teal
        Color(0xFFFCF3CF), // soft yellow
        Color(0xFFE8DAEF), // soft lavender
        Color(0xFFFADBD8), // soft coral
    )

    private val darkTints = listOf(
        Color(0xFF1B3A4B),
        Color(0xFF1E3D2F),
        Color(0xFF4A3A1F),
        Color(0xFF3D2433),
        Color(0xFF1A3C3A),
        Color(0xFF3D3A1A),
        Color(0xFF33243D),
        Color(0xFF3D2422),
    )

    init {
        require(lightTints.size == MAX_PHRASE_GROUPS)
        require(darkTints.size == MAX_PHRASE_GROUPS)
    }

    fun tintFor(colorIndex: Int, darkTheme: Boolean): Color {
        val palette = if (darkTheme) darkTints else lightTints
        val index = colorIndex.coerceIn(0, palette.lastIndex)
        return palette[index]
    }
}

/** Resolves a group tint for the current theme, or null when ungrouped. */
@Composable
fun phraseGroupBackground(colorIndex: Int?): Color? {
    if (colorIndex == null) return null
    return PhraseGroupPalette.tintFor(colorIndex, darkTheme = isSystemInDarkTheme())
}
