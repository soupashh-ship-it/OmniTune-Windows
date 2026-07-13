import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.api.tasks.Copy

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

val omniTuneVersion: String = providers.gradleProperty("omnitune.version").get()
val bundledVlcHome: String = providers.environmentVariable("VLC_HOME")
    .orElse("C:/Program Files/VideoLAN/VLC")
    .get()

val windowsAppResourcesDir = layout.buildDirectory.dir("generated/windowsAppResources")
val composeAppImageDir = layout.buildDirectory.dir("compose/binaries/main/app/OmniTune")

val prepareWindowsAppResources by tasks.registering(Copy::class) {
    into(windowsAppResourcesDir)

    from(rootProject.file("THIRD_PARTY_NOTICES.txt")) {
        into("licenses")
    }

    from(bundledVlcHome) {
        include("libvlc.dll")
        include("libvlccore.dll")
        include("plugins/**")
        include("AUTHORS.txt")
        include("COPYING.txt")
        include("NEWS.txt")
        into("native/vlc")
    }

    doFirst {
        val vlcDir = file(bundledVlcHome)
        val libvlc = vlcDir.resolve("libvlc.dll")
        val libvlcCore = vlcDir.resolve("libvlccore.dll")
        val plugins = vlcDir.resolve("plugins")
        require(libvlc.isFile && libvlcCore.isFile && plugins.isDirectory) {
            "VLC/libVLC runtime was not found at '$bundledVlcHome'. Install VLC x64 or set VLC_HOME to a redistributable VLC runtime before packaging."
        }
        rootProject.file("THIRD_PARTY_NOTICES.txt").also {
            require(it.isFile) { "THIRD_PARTY_NOTICES.txt is required for release packaging." }
        }
    }
}

val copyWindowsAppResourcesToDistributable by tasks.registering(Copy::class) {
    dependsOn(prepareWindowsAppResources)
    mustRunAfter("createDistributable")
    from(windowsAppResourcesDir)
    into(composeAppImageDir)
}

kotlin {
    jvm("desktop")
    jvmToolchain(21)

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.animation)
            implementation(compose.materialIconsExtended)

            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.lifecycle.viewmodel.compose)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.encoding)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.cio)
            implementation(libs.ktor.server.websockets)
            implementation(libs.ktor.server.content.negotiation)

            implementation(libs.json)
            implementation(libs.jsoup)
            implementation(libs.re2j)
            implementation(libs.brotli)
            implementation(libs.rhino)
            implementation(libs.apache.lang3)
            implementation(libs.kuromoji.ipadic)
            implementation(libs.coroutines.core)

        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation(libs.vlcj)
            implementation(libs.jmtc)

            implementation(project(":innertube"))
            implementation(project(":kugou"))
            implementation(project(":lrclib"))
            implementation(project(":lastfm"))
            implementation(project(":simpmusic"))
            implementation(project(":betterlyrics"))
            implementation(project(":kizzy"))
            implementation(project(":canvas"))
        }

        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.core)
                implementation(project(":innertube"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.omnitune.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)

            packageName = "OmniTune"
            packageVersion = omniTuneVersion
            description = "Open-source YouTube music player for Windows"
            vendor = "OmniTune"

            windows {
                console = false
                menu = true
                shortcut = true
                perUserInstall = true
                dirChooser = true
                menuGroup = "OmniTune"
                upgradeUuid = "7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d"
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }
        }
    }
}

tasks.matching {
    it.name in setOf(
        "createDistributable"
    )
}.configureEach {
    finalizedBy(copyWindowsAppResourcesToDistributable)
}

tasks.matching {
    it.name in setOf(
        "packageDistributionForCurrentOS",
        "packageReleaseDistributionForCurrentOS",
        "packageExe",
        "packageMsi",
        "packageReleaseExe",
        "packageReleaseMsi"
    )
}.configureEach {
    dependsOn(copyWindowsAppResourcesToDistributable)
}
