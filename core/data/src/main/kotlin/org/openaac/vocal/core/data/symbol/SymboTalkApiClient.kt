package org.openaac.vocal.core.data.symbol

import org.json.JSONArray
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimal SymboTalk search client (caregiver configuration only).
 *
 * Uses platform [HttpURLConnection] to avoid adding a network stack dependency.
 */
@Singleton
class SymboTalkApiClient @Inject constructor() {

    data class SymbolHit(
        val id: Int,
        val name: String,
        val repoKey: String,
        val imageUrl: String,
        val altUrl: String?,
    ) {
        val cacheKey: String
            get() = "${repoKey}_$id".replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    fun searchByName(
        name: String,
        lang: String = DEFAULT_LANG,
        repo: String = DEFAULT_REPO,
        limit: Int = DEFAULT_LIMIT,
    ): List<SymbolHit> {
        val query = URLEncoder.encode(name.trim(), StandardCharsets.UTF_8.name())
        val url = URL(
            "$BASE_URL?name=$query&lang=$lang&repo=$repo&limit=$limit",
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Cache-Control", "no-cache")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("SymboTalk search failed with HTTP $code")
            }
            val body = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            return parseSymbols(body)
        } finally {
            connection.disconnect()
        }
    }

    fun downloadBytes(imageUrl: String): ByteArray {
        val primary = runCatching { downloadFrom(imageUrl) }
        if (primary.isSuccess) return primary.getOrThrow()
        throw primary.exceptionOrNull() ?: IOException("Download failed")
    }

    fun downloadBytesWithFallback(imageUrl: String, altUrl: String?): ByteArray {
        runCatching { return downloadFrom(imageUrl) }
        if (!altUrl.isNullOrBlank()) {
            return downloadFrom(altUrl)
        }
        throw IOException("Unable to download symbol image")
    }

    private fun downloadFrom(imageUrl: String): ByteArray {
        val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            instanceFollowRedirects = true
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("Image download failed with HTTP $code")
            }
            return BufferedInputStream(connection.inputStream).use { it.readBytes() }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseSymbols(body: String): List<SymbolHit> {
        val trimmed = body.trim()
        if (trimmed.isEmpty() || trimmed == "null") return emptyList()
        val array = JSONArray(trimmed)
        val results = ArrayList<SymbolHit>(array.length())
        for (index in 0 until array.length()) {
            val obj = array.optJSONObject(index) ?: continue
            val imageUrl = obj.optString("image_url").ifBlank { null } ?: continue
            val id = obj.optInt("id", -1)
            if (id < 0) continue
            results += SymbolHit(
                id = id,
                name = obj.optString("name"),
                repoKey = obj.optString("repo_key").ifBlank { "unknown" },
                imageUrl = imageUrl,
                altUrl = obj.optString("alt_url").ifBlank { null },
            )
        }
        return results
    }

    companion object {
        const val BASE_URL = "https://symbotalkapiv1.azurewebsites.net/search/"
        const val DEFAULT_LANG = "en"
        const val DEFAULT_REPO = "all"
        const val DEFAULT_LIMIT = 5
        private const val TIMEOUT_MS = 15_000
    }
}
