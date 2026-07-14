package com.omnitune.app.di

import com.omnitune.app.platform.PlatformContext
import com.omnitune.app.platform.FileBackedOmniDownloadManager
import com.omnitune.app.platform.OmniDownloadManager
import com.omnitune.app.platform.SettingsRepository
import com.omnitune.app.platform.VlcjAudioEngine
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.service.YouTubeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPlatformContextModule: Module = module {
    single { PlatformContext() }
}

actual val platformNetworkModule: Module = module {
    single { YouTubeService() }
}

actual val platformPlayerModule: Module = module {
    single { SettingsRepository(platformContext = get()) }
    single<OmniDownloadManager> { FileBackedOmniDownloadManager(get(), get()) }
    single { VlcjAudioEngine(CoroutineScope(SupervisorJob() + Dispatchers.Default)) }
    single { PlayerViewModel(get(), get(), get(), get()) }
}
