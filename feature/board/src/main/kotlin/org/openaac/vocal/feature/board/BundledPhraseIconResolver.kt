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
    BundledPhraseIcons.PLEASE -> R.drawable.ic_phrase_please
    BundledPhraseIcons.THANK_YOU -> R.drawable.ic_phrase_thank_you
    BundledPhraseIcons.EAT -> R.drawable.ic_phrase_eat
    BundledPhraseIcons.DRINK -> R.drawable.ic_phrase_drink
    BundledPhraseIcons.HUNGRY -> R.drawable.ic_phrase_hungry
    BundledPhraseIcons.TIRED -> R.drawable.ic_phrase_tired
    BundledPhraseIcons.HURT -> R.drawable.ic_phrase_hurt
    BundledPhraseIcons.HOME -> R.drawable.ic_phrase_home
    BundledPhraseIcons.GO -> R.drawable.ic_phrase_go
    BundledPhraseIcons.COME -> R.drawable.ic_phrase_come
    BundledPhraseIcons.WANT -> R.drawable.ic_phrase_want
    BundledPhraseIcons.LIKE -> R.drawable.ic_phrase_like
    BundledPhraseIcons.DONT_LIKE -> R.drawable.ic_phrase_dont_like
    BundledPhraseIcons.PLAY -> R.drawable.ic_phrase_play
    BundledPhraseIcons.FINISHED -> R.drawable.ic_phrase_finished
    BundledPhraseIcons.WAIT -> R.drawable.ic_phrase_wait
    BundledPhraseIcons.HOT -> R.drawable.ic_phrase_hot
    BundledPhraseIcons.COLD -> R.drawable.ic_phrase_cold
    BundledPhraseIcons.BREAK -> R.drawable.ic_phrase_break
    BundledPhraseIcons.HELLO -> R.drawable.ic_phrase_hello
    BundledPhraseIcons.GOODBYE -> R.drawable.ic_phrase_goodbye
    BundledPhraseIcons.LOVE_YOU -> R.drawable.ic_phrase_love_you
    BundledPhraseIcons.SCHOOL -> R.drawable.ic_phrase_school
    BundledPhraseIcons.PLACEHOLDER -> R.drawable.ic_phrase_placeholder
    else -> null
}

internal fun placeholderPhraseIconResId(): Int = R.drawable.ic_phrase_placeholder
