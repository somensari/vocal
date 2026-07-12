package org.openaac.vocal.core.domain.monitoring

/**
 * Privacy-safe New Relic custom event / breadcrumb vocabulary.
 *
 * Never put phrase text, icon paths, recorded audio paths, or other AAC content
 * in attribute values — only structural metadata (counts, error codes, screens).
 */
object MonitoringEvents {
    /** Custom event type (must not start with Mobile / NewRelic / NR_). */
    const val EVENT_TYPE = "VocalApp"

    object Name {
        const val ScreenView = "ScreenView"
        const val BoardReady = "BoardReady"
        const val SpeakPhrase = "SpeakPhrase"
        const val SpeechError = "SpeechError"
        const val PhraseSaved = "PhraseSaved"
        const val PhraseDeleted = "PhraseDeleted"
        const val ThemeChanged = "ThemeChanged"
        const val SpeechTest = "SpeechTest"
        const val DefaultBoardEnsured = "DefaultBoardEnsured"
    }

    object Attr {
        const val Screen = "screen"
        const val PhraseCount = "phrase_count"
        const val HasRecordedAudio = "has_recorded_audio"
        const val Error = "error"
        const val Theme = "theme"
        const val Success = "success"
        const val IsNew = "is_new"
        const val Component = "component"
    }

    object Screen {
        const val Board = "board"
        const val Settings = "settings"
    }

    object Interaction {
        const val EnsureDefaultBoard = "EnsureDefaultBoard"
        const val SpeakPhrase = "SpeakPhrase"
        const val SavePhrase = "SavePhrase"
        const val DeletePhrase = "DeletePhrase"
        const val TestSpeech = "TestSpeech"
    }
}
