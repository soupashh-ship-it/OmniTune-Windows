package com.omnitune.windows.app

import com.omnitune.windows.data.DatabaseFactory
import com.omnitune.windows.db.OmniDatabase
import com.omnitune.windows.playback.OmniPlayer
import com.omnitune.windows.playback.VlcjOmniPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object DependencyContainer {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    val database: OmniDatabase by lazy {
        DatabaseFactory.createDatabase()
    }
    val player: OmniPlayer by lazy {
        VlcjOmniPlayer()
    }

    val playbackCoordinator: com.omnitune.windows.domain.playback.PlaybackCoordinator by lazy {
        com.omnitune.windows.domain.playback.PlaybackCoordinator(applicationScope, player)
    }
}
