package com.omnitune.app.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Duration

sealed class UpdateCheckResult {
    data class Current(val currentVersion: String, val latestVersion: String) : UpdateCheckResult()
    data class UpdateAvailable(
        val currentVersion: String,
        val latestVersion: String,
        val releaseUrl: String,
        val installerAsset: ReleaseAsset?,
        val checksumAsset: ReleaseAsset?,
    ) : UpdateCheckResult()

    data class Failed(val message: String) : UpdateCheckResult()
}

data class ReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long,
)

data class ReleaseMetadata(
    val version: String,
    val htmlUrl: String,
    val prerelease: Boolean,
    val assets: List<ReleaseAsset> = emptyList(),
)

data class DownloadedUpdateInstaller(
    val file: File,
    val checksumVerified: Boolean,
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
            val latest = parseGitHubReleaseResponse(response.body())
            evaluate(currentVersion, latest)
        }.getOrElse {
            UpdateCheckResult.Failed(it.message ?: it::class.simpleName ?: "Unknown update-check failure")
        }
    }

    suspend fun downloadInstaller(update: UpdateCheckResult.UpdateAvailable, updatesDir: File): DownloadedUpdateInstaller =
        withContext(Dispatchers.IO) {
            val asset = update.installerAsset ?: error("No Windows installer asset was found for ${update.latestVersion}.")
            updatesDir.mkdirs()
            val target = File(updatesDir, sanitizeUpdateFileName(asset.name))
            val temp = File(updatesDir, "${target.name}.part")

            val request = HttpRequest.newBuilder(URI(asset.downloadUrl))
                .timeout(Duration.ofMinutes(10))
                .header("Accept", "application/octet-stream")
                .header("User-Agent", "OmniTune-Windows/${currentVersion.ifBlank { "development" }}")
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
            if (response.statusCode() !in 200..299) {
                error("GitHub asset download returned HTTP ${response.statusCode()}.")
            }
            response.body().use { input ->
                Files.copy(input, temp.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            if (asset.sizeBytes > 0L && temp.length() != asset.sizeBytes) {
                temp.delete()
                error("Downloaded installer size mismatch.")
            }
            moveReplacing(temp, target)

            val verified = update.checksumAsset?.let { checksumAsset ->
                verifySha256(target, checksumAsset)
            } ?: false

            DownloadedUpdateInstaller(target, verified)
        }

    internal fun parseGitHubReleaseResponse(json: String): ReleaseMetadata {
        val trimmed = json.trim()
        return if (trimmed.startsWith("[")) {
            parseGitHubReleases(JSONArray(trimmed)).maxWithOrNull { left, right ->
                compareVersions(left.version, right.version)
            } ?: error("No releases returned by GitHub.")
        } else {
            parseGitHubRelease(trimmed)
        }
    }

    internal fun parseGitHubReleases(array: JSONArray): List<ReleaseMetadata> {
        return (0 until array.length()).mapNotNull { index ->
            array.optJSONObject(index)?.let { parseGitHubRelease(it) }
        }.filter { it.version.isNotBlank() && !it.version.equals("development", ignoreCase = true) }
    }

    internal fun parseGitHubRelease(json: String): ReleaseMetadata {
        return parseGitHubRelease(JSONObject(json))
    }

    private fun parseGitHubRelease(obj: JSONObject): ReleaseMetadata {
        val tag = obj.optString("tag_name", "")
        val htmlUrl = obj.optString("html_url", AppInfo.releasesUrl)
        return ReleaseMetadata(
            version = normalizeVersion(tag),
            htmlUrl = htmlUrl.ifBlank { AppInfo.releasesUrl },
            prerelease = obj.optBoolean("prerelease", false),
            assets = parseAssets(obj.optJSONArray("assets")),
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
                installerAsset = latest.preferredWindowsInstallerAsset(),
                checksumAsset = latest.preferredWindowsInstallerAsset()?.let { installer ->
                    latest.checksumAssetFor(installer)
                },
            )
        } else {
            UpdateCheckResult.Current(
                currentVersion = normalizedCurrent,
                latestVersion = latest.version,
            )
        }
    }

    private fun parseAssets(array: JSONArray?): List<ReleaseAsset> {
        if (array == null) return emptyList()
        return (0 until array.length()).mapNotNull { index ->
            val obj = array.optJSONObject(index) ?: return@mapNotNull null
            val name = obj.optString("name", "")
            val url = obj.optString("browser_download_url", "")
            if (name.isBlank() || url.isBlank()) return@mapNotNull null
            ReleaseAsset(
                name = name,
                downloadUrl = url,
                sizeBytes = obj.optLong("size", 0L),
            )
        }
    }

    private fun moveReplacing(source: File, target: File) {
        runCatching {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        }.getOrElse {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun verifySha256(file: File, checksumAsset: ReleaseAsset): Boolean {
        val expected = downloadText(checksumAsset)
            .lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?.substringBefore(" ")
            ?.trim()
            ?.lowercase()
            ?: error("Checksum asset ${checksumAsset.name} is empty.")

        val actual = sha256(file)
        if (!expected.matches(Regex("[a-f0-9]{64}"))) {
            error("Checksum asset ${checksumAsset.name} does not contain a SHA-256 digest.")
        }
        if (actual != expected) {
            error("Downloaded installer checksum mismatch.")
        }
        return true
    }

    private fun downloadText(asset: ReleaseAsset): String {
        val request = HttpRequest.newBuilder(URI(asset.downloadUrl))
            .timeout(Duration.ofSeconds(30))
            .header("Accept", "text/plain")
            .header("User-Agent", "OmniTune-Windows/${currentVersion.ifBlank { "development" }}")
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            error("GitHub checksum download returned HTTP ${response.statusCode()}.")
        }
        return response.body()
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (read > 0) digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

internal fun ReleaseMetadata.preferredWindowsInstallerAsset(): ReleaseAsset? {
    val candidates = assets.filter { asset ->
        val lower = asset.name.lowercase()
        lower.endsWith(".exe") || lower.endsWith(".msi")
    }
    return candidates.firstOrNull { it.name.contains("custom", ignoreCase = true) && it.name.endsWith(".exe", ignoreCase = true) }
        ?: candidates.firstOrNull { it.name.contains("Setup", ignoreCase = true) && it.name.endsWith(".exe", ignoreCase = true) }
        ?: candidates.firstOrNull { it.name.endsWith(".exe", ignoreCase = true) }
        ?: candidates.firstOrNull { it.name.endsWith(".msi", ignoreCase = true) }
}

internal fun ReleaseMetadata.checksumAssetFor(installer: ReleaseAsset): ReleaseAsset? {
    val expectedName = "${installer.name}.sha256"
    return assets.firstOrNull { it.name.equals(expectedName, ignoreCase = true) }
}

internal fun sanitizeUpdateFileName(name: String): String {
    return name.replace(Regex("[^A-Za-z0-9._-]"), "_")
        .take(160)
        .ifBlank { "OmniTune-update-installer.exe" }
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
