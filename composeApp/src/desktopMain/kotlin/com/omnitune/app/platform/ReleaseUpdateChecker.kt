package com.omnitune.app.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

sealed class UpdateCheckResult {
    data class Current(val currentVersion: String, val latestVersion: String) : UpdateCheckResult()
    data class UpdateAvailable(
        val currentVersion: String,
        val latestVersion: String,
        val releaseUrl: String,
    ) : UpdateCheckResult()

    data class Failed(val message: String) : UpdateCheckResult()
}

data class ReleaseMetadata(
    val version: String,
    val htmlUrl: String,
    val prerelease: Boolean,
)

class ReleaseUpdateChecker(
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build(),
    private val latestReleaseApiUrl: String = AppInfo.latestReleaseApiUrl,
    private val currentVersion: String = AppInfo.version,
) {
    suspend fun checkLatest(): UpdateCheckResult = withContext(Dispatchers.IO) {
        runCatching {
            val request = HttpRequest.newBuilder(URI(latestReleaseApiUrl))
                .timeout(Duration.ofSeconds(12))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "OmniTune-Windows/${currentVersion.ifBlank { "development" }}")
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() !in 200..299) {
                return@runCatching UpdateCheckResult.Failed("GitHub returned HTTP ${response.statusCode()}")
            }
            val latest = parseGitHubRelease(response.body())
            evaluate(currentVersion, latest)
        }.getOrElse {
            UpdateCheckResult.Failed(it.message ?: it::class.simpleName ?: "Unknown update-check failure")
        }
    }

    internal fun parseGitHubRelease(json: String): ReleaseMetadata {
        val obj = JSONObject(json)
        val tag = obj.optString("tag_name", "")
        val htmlUrl = obj.optString("html_url", AppInfo.releasesUrl)
        return ReleaseMetadata(
            version = normalizeVersion(tag),
            htmlUrl = htmlUrl.ifBlank { AppInfo.releasesUrl },
            prerelease = obj.optBoolean("prerelease", false),
        )
    }

    internal fun evaluate(current: String, latest: ReleaseMetadata): UpdateCheckResult {
        val normalizedCurrent = normalizeVersion(current)
        if (normalizedCurrent.isBlank() || normalizedCurrent == "development") {
            return UpdateCheckResult.Failed("Development build has no release version to compare")
        }
        return if (compareVersions(latest.version, normalizedCurrent) > 0) {
            UpdateCheckResult.UpdateAvailable(
                currentVersion = normalizedCurrent,
                latestVersion = latest.version,
                releaseUrl = latest.htmlUrl,
            )
        } else {
            UpdateCheckResult.Current(
                currentVersion = normalizedCurrent,
                latestVersion = latest.version,
            )
        }
    }
}

internal fun normalizeVersion(value: String): String {
    return value.trim()
        .removePrefix("v")
        .removePrefix("V")
        .substringBefore("-")
        .trim()
}

internal fun compareVersions(left: String, right: String): Int {
    val a = normalizeVersion(left).split(".").map { it.toIntOrNull() ?: 0 }
    val b = normalizeVersion(right).split(".").map { it.toIntOrNull() ?: 0 }
    val size = maxOf(a.size, b.size)
    for (index in 0 until size) {
        val av = a.getOrElse(index) { 0 }
        val bv = b.getOrElse(index) { 0 }
        if (av != bv) return av.compareTo(bv)
    }
    return 0
}
