plugins {
    kotlin("multiplatform") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("app.cash.sqldelight") version "2.0.1"
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "21"
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                
                implementation("io.ktor:ktor-client-core:3.0.0")
                implementation("io.ktor:ktor-client-okhttp:3.0.0")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
                implementation("io.ktor:ktor-client-encoding:3.0.0")
                implementation("org.brotli:dec:0.1.2")
                implementation("com.github.teamnewpipe:newpipeextractor:v0.26.3")
                implementation("uk.co.caprica:vlcj:4.8.2")
                implementation("com.google.re2j:re2j:1.8")
                implementation("org.mozilla:rhino:1.8.1")
                implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
                implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha06")
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0-alpha06")
                implementation(compose.materialIconsExtended)
            }

        }
    }
}
sqldelight {
    databases {
        create("OmniDatabase") {
            packageName.set("com.omnitune.windows.db")
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.omnitune.windows.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageVersion = "1.0.0"
        }
    }
}
