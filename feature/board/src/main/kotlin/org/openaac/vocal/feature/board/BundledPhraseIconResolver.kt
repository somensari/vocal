package org.openaac.vocal.feature.board

import org.openaac.vocal.core.domain.model.BundledPhraseIcons

internal fun bundledPhraseIconResId(iconPath: String?): Int? = when (iconPath) {
    BundledPhraseIcons.YES -> R.drawable.ic_phrase_yes
    BundledPhraseIcons.NO -> R.drawable.ic_phrase_no
    BundledPhraseIcons.HELP -> R.drawable.ic_phrase_help
    BundledPhraseIcons.WATER -> R.drawable.ic_phrase_water
    BundledPhraseIcons.BATHROOM -> R.drawable.ic_phrase_bathroom
    BundledPhraseIcons.HAPPY -> R.drawable.ic_phrase_happy
    BundledPhraseIcons.SAD -> R.drawable.ic_phrase_sad
    BundledPhraseIcons.MORE -> R.drawable.ic_phrase_more
    BundledPhraseIcons.STOP -> R.drawable.ic_phrase_stop
    else -> null
}
