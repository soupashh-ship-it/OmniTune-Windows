package com.omnitune.app.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformPlatformContextModule: Module
expect val platformNetworkModule: Module
expect val platformPlayerModule: Module

fun initKoin(): List<Module> {
    return listOf(
        platformPlatformContextModule,
        platformNetworkModule,
        platformPlayerModule
    )
}
