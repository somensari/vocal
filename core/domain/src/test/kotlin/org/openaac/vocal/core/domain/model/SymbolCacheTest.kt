package org.openaac.vocal.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SymbolCacheTest {
    @Test
    fun defaultCacheMaxSizeIs50Mb() {
        assertEquals(50, SymbolCacheMaxSizeMb.Default.megabytes)
    }

    @Test
    fun cacheMaxSizeOptionsMatchAcceptanceCriteria() {
        assertEquals(
            listOf(50, 100, 200, 500),
            SymbolCacheMaxSizeMb.entries.map { it.megabytes },
        )
    }

    @Test
    fun symbolCachePathRoundTrip() {
        val path = SymbolCachePaths.pathForCacheKey("arasaac_20")
        assertTrue(SymbolCachePaths.isCachedSymbolPath(path))
        assertEquals("arasaac_20", SymbolCachePaths.cacheKeyFromPath(path))
    }
}
