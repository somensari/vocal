package org.openaac.vocal.core.domain.model

/**
 * Allowed maximum sizes for downloaded SymboTalk symbol images on disk.
 *
 * Bundled starter icons do not count toward this limit.
 */
enum class SymbolCacheMaxSizeMb(val megabytes: Int) {
    Mb50(50),
    Mb100(100),
    Mb200(200),
    Mb500(500),
    ;

    val bytes: Long get() = megabytes.toLong() * BYTES_PER_MEGABYTE

    companion object {
        val Default = Mb50

        fun fromMegabytes(megabytes: Int?): SymbolCacheMaxSizeMb =
            entries.firstOrNull { it.megabytes == megabytes } ?: Default
    }
}

private const val BYTES_PER_MEGABYTE = 1024L * 1024L

/**
 * Stable [Phrase.iconPath] scheme for SymboTalk images stored under the app files directory.
 */
object SymbolCachePaths {
    const val SCHEME_PREFIX = "vocal://symbol-cache/"

    fun isCachedSymbolPath(iconPath: String?): Boolean =
        iconPath?.startsWith(SCHEME_PREFIX) == true

    fun cacheKeyFromPath(iconPath: String): String? =
        iconPath.takeIf { isCachedSymbolPath(it) }?.removePrefix(SCHEME_PREFIX)?.takeIf { it.isNotBlank() }

    fun pathForCacheKey(cacheKey: String): String = "$SCHEME_PREFIX$cacheKey"
}

/**
 * Outcome of resolving a SymboTalk symbol for a phrase label during caregiver configuration.
 */
sealed class SymbolMatchResult {
    /** Symbol image is available locally (downloaded or already cached). */
    data class Matched(val iconPath: String) : SymbolMatchResult()

    /** SymboTalk was unreachable or returned no usable match; phrase may still be saved. */
    data object Unavailable : SymbolMatchResult()

    /** A new download would exceed the caregiver-configured cache size limit. */
    data object CacheLimitReached : SymbolMatchResult()
}

data class SymbolCacheUsage(
    val usedBytes: Long,
    val maxBytes: Long,
) {
    val usedMegabytes: Int
        get() = (usedBytes / (1024L * 1024L)).toInt()

    val maxMegabytes: Int
        get() = (maxBytes / (1024L * 1024L)).toInt()
}

/**
 * Outcome of saving a phrase with automatic SymboTalk symbol matching.
 */
sealed class SavePhraseWithSymbolResult {
    data class Saved(
        val phraseId: Long,
        val symbolWarning: Boolean,
    ) : SavePhraseWithSymbolResult()

    data object CacheLimitReached : SavePhraseWithSymbolResult()
}
