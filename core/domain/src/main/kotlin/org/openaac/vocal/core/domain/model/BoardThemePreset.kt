package org.openaac.vocal.core.domain.model

/**
 * Named board appearance presets caregivers can choose from.
 *
 * Persist the stable [id] rather than enum ordinals so preferences remain safe
 * if the list is reordered.
 */
enum class BoardThemePreset(val id: String) {
    DefaultBlue("default_blue"),
    HighContrast("high_contrast"),
    SoftPastel("soft_pastel"),
    ;

    companion object {
        val Default = DefaultBlue

        fun fromId(id: String?): BoardThemePreset =
            entries.firstOrNull { it.id == id } ?: Default
    }
}
