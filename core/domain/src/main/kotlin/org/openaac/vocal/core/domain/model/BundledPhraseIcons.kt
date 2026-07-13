package org.openaac.vocal.core.domain.model

/**
 * Stable [Phrase.iconPath] keys for AAC symbols bundled with the app.
 *
 * These keys are persisted with starter phrases and resolved to platform
 * drawables by the UI layer. Bundled icons never count toward the SymboTalk
 * download cache size limit.
 */
object BundledPhraseIcons {
    const val YES = "vocal://bundled-icons/starter/yes"
    const val NO = "vocal://bundled-icons/starter/no"
    const val HELP = "vocal://bundled-icons/starter/help"
    const val WATER = "vocal://bundled-icons/starter/water"
    const val BATHROOM = "vocal://bundled-icons/starter/bathroom"
    const val HAPPY = "vocal://bundled-icons/starter/happy"
    const val SAD = "vocal://bundled-icons/starter/sad"
    const val MORE = "vocal://bundled-icons/starter/more"
    const val STOP = "vocal://bundled-icons/starter/stop"
    const val PLEASE = "vocal://bundled-icons/starter/please"
    const val THANK_YOU = "vocal://bundled-icons/starter/thank_you"
    const val EAT = "vocal://bundled-icons/starter/eat"
    const val DRINK = "vocal://bundled-icons/starter/drink"
    const val HUNGRY = "vocal://bundled-icons/starter/hungry"
    const val TIRED = "vocal://bundled-icons/starter/tired"
    const val HURT = "vocal://bundled-icons/starter/hurt"
    const val HOME = "vocal://bundled-icons/starter/home"
    const val GO = "vocal://bundled-icons/starter/go"
    const val COME = "vocal://bundled-icons/starter/come"
    const val WANT = "vocal://bundled-icons/starter/want"
    const val LIKE = "vocal://bundled-icons/starter/like"
    const val DONT_LIKE = "vocal://bundled-icons/starter/dont_like"
    const val PLAY = "vocal://bundled-icons/starter/play"
    const val FINISHED = "vocal://bundled-icons/starter/finished"
    const val WAIT = "vocal://bundled-icons/starter/wait"
    const val HOT = "vocal://bundled-icons/starter/hot"
    const val COLD = "vocal://bundled-icons/starter/cold"
    const val BREAK = "vocal://bundled-icons/starter/break"
    const val HELLO = "vocal://bundled-icons/starter/hello"
    const val GOODBYE = "vocal://bundled-icons/starter/goodbye"
    const val LOVE_YOU = "vocal://bundled-icons/starter/love_you"
    const val SCHOOL = "vocal://bundled-icons/starter/school"

    /** Generic fallback when no bundled or cached symbol is available. */
    const val PLACEHOLDER = "vocal://bundled-icons/starter/placeholder"

    const val SCHEME_PREFIX = "vocal://bundled-icons/"

    fun isBundledPath(iconPath: String?): Boolean =
        iconPath?.startsWith(SCHEME_PREFIX) == true
}
