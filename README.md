# OmniTune Windows

A native Windows desktop client for OmniTune, built with Kotlin and Compose Multiplatform.

## Requirements
- Windows 10/11
- Java 21+
- VLC Media Player (required for audio stream resolution via `vlcj`)

## Building and Running
To launch the desktop app locally:
```sh
./gradlew run
```

To package a standalone Windows installer (`.msi`):
```sh
./gradlew packageMsi
```

## Architecture Summary
- **UI:** Jetbrains Compose Desktop
- **Audio Playback:** VLCJ (`uk.co.caprica:vlcj`)
- **Networking:** Ktor Client & `innertube` (ported from Android)
- **Concurrency:** Kotlin Coroutines
