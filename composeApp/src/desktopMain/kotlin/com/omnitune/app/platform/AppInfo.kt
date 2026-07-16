package com.omnitune.app.platform

import java.util.Properties

object AppInfo {
    private val properties: Properties by lazy {
        Properties().also { props ->
            AppInfo::class.java.classLoader
                .getResourceAsStream("omnitune-version.properties")
                ?.use(props::load)
        }
    }

    val version: String
        get() = properties.getProperty("version")?.takeIf { it.isNotBlank() } ?: "development"

    val releaseTag: String
        get() = properties.getProperty("releaseTag")?.takeIf { it.isNotBlank() } ?: "development"

    val releaseUrl: String
        get() = properties.getProperty("releaseUrl")?.takeIf { it.isNotBlank() }
            ?: "https://github.com/soupashh-ship-it/OmniTune-Windows/releases"

    val releasesUrl: String
        get() = properties.getProperty("releasesUrl")?.takeIf { it.isNotBlank() }
            ?: "https://github.com/soupashh-ship-it/OmniTune-Windows/releases"

    val latestReleaseApiUrl: String
        get() = properties.getProperty("latestReleaseApiUrl")?.takeIf { it.isNotBlank() }
            ?: "https://api.github.com/repos/soupashh-ship-it/OmniTune-Windows/releases"
}
