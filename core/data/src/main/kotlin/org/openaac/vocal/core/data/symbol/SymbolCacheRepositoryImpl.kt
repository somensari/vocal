package org.openaac.vocal.core.data.symbol

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.openaac.vocal.core.domain.model.SymbolCachePaths
import org.openaac.vocal.core.domain.model.SymbolMatchResult
import org.openaac.vocal.core.domain.repository.SymbolCacheRepository
import org.openaac.vocal.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SymbolCacheRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiClient: SymboTalkApiClient,
    private val userPreferencesRepository: UserPreferencesRepository,
) : SymbolCacheRepository {

    private val cacheDir: File
        get() = File(context.filesDir, CACHE_DIR_NAME).also { it.mkdirs() }

    override suspend fun matchAndCacheSymbol(label: String): SymbolMatchResult =
        withContext(Dispatchers.IO) {
            val query = label.trim()
            if (query.isEmpty()) {
                return@withContext SymbolMatchResult.Unavailable
            }

            val hits = try {
                apiClient.searchByName(query)
            } catch (_: IOException) {
                return@withContext SymbolMatchResult.Unavailable
            } catch (_: Exception) {
                return@withContext SymbolMatchResult.Unavailable
            }

            val hit = hits.firstOrNull() ?: return@withContext SymbolMatchResult.Unavailable
            val localFile = fileForCacheKey(hit.cacheKey)
            if (localFile.exists() && localFile.length() > 0L) {
                return@withContext SymbolMatchResult.Matched(
                    SymbolCachePaths.pathForCacheKey(hit.cacheKey),
                )
            }

            val maxBytes = userPreferencesRepository.symbolCacheMaxSizeMb.first().bytes
            val usedBytes = cacheUsageBytes()
            if (usedBytes >= maxBytes) {
                return@withContext SymbolMatchResult.CacheLimitReached
            }

            val bytes = try {
                apiClient.downloadBytesWithFallback(hit.imageUrl, hit.altUrl)
            } catch (_: IOException) {
                return@withContext SymbolMatchResult.Unavailable
            } catch (_: Exception) {
                return@withContext SymbolMatchResult.Unavailable
            }

            if (usedBytes + bytes.size > maxBytes) {
                return@withContext SymbolMatchResult.CacheLimitReached
            }

            val temp = File(cacheDir, "${hit.cacheKey}.tmp")
            try {
                temp.writeBytes(bytes)
                if (!temp.renameTo(localFile)) {
                    localFile.writeBytes(bytes)
                    temp.delete()
                }
            } catch (_: IOException) {
                temp.delete()
                return@withContext SymbolMatchResult.Unavailable
            }

            SymbolMatchResult.Matched(SymbolCachePaths.pathForCacheKey(hit.cacheKey))
        }

    override suspend fun getCacheUsageBytes(): Long = withContext(Dispatchers.IO) {
        cacheUsageBytes()
    }

    override suspend fun deleteUnreferencedCache(referencedIconPaths: Collection<String>): Int =
        withContext(Dispatchers.IO) {
            val keepKeys = referencedIconPaths
                .mapNotNull { SymbolCachePaths.cacheKeyFromPath(it) }
                .toSet()
            var deleted = 0
            cacheDir.listFiles()?.forEach { file ->
                if (!file.isFile) return@forEach
                if (file.name.endsWith(".tmp")) {
                    if (file.delete()) deleted++
                    return@forEach
                }
                if (file.name !in keepKeys) {
                    if (file.delete()) deleted++
                }
            }
            deleted
        }

    override fun resolveLocalFilePath(iconPath: String?): String? {
        val key = SymbolCachePaths.cacheKeyFromPath(iconPath ?: return null) ?: return null
        val file = fileForCacheKey(key)
        return file.takeIf { it.exists() && it.length() > 0L }?.absolutePath
    }

    private fun fileForCacheKey(cacheKey: String): File = File(cacheDir, cacheKey)

    private fun cacheUsageBytes(): Long =
        cacheDir.listFiles()
            ?.asSequence()
            ?.filter { it.isFile && !it.name.endsWith(".tmp") }
            ?.sumOf { it.length() }
            ?: 0L

    companion object {
        const val CACHE_DIR_NAME = "symbol_cache"
    }
}
