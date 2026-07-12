package org.openaac.vocal.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardThemePresetTest {

    @Test
    fun fromId_returnsMatchingPreset() {
        assertEquals(BoardThemePreset.HighContrast, BoardThemePreset.fromId("high_contrast"))
    }

    @Test
    fun fromId_unknownFallsBackToDefault() {
        assertEquals(BoardThemePreset.Default, BoardThemePreset.fromId("unknown"))
        assertEquals(BoardThemePreset.Default, BoardThemePreset.fromId(null))
    }

    @Test
    fun ids_areUniqueAndStable() {
        val ids = BoardThemePreset.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all { it.isNotBlank() })
    }

    @Test
    fun default_isDefaultBlue() {
        assertEquals(BoardThemePreset.DefaultBlue, BoardThemePreset.Default)
    }
}
