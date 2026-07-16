package com.omnitune.app.window

enum class OmniDesktopSizeClass {
    Compact,
    Medium,
    Expanded,
    Large,
}

object OmniResponsiveLayout {
    fun sizeClassForContentWidth(widthDp: Float): OmniDesktopSizeClass = when {
        widthDp >= 1320f -> OmniDesktopSizeClass.Large
        widthDp >= 1040f -> OmniDesktopSizeClass.Expanded
        widthDp >= 660f -> OmniDesktopSizeClass.Medium
        else -> OmniDesktopSizeClass.Compact
    }

    fun settingsColumnCount(contentWidthDp: Float): Int = when (sizeClassForContentWidth(contentWidthDp)) {
        OmniDesktopSizeClass.Large,
        OmniDesktopSizeClass.Expanded -> 3
        OmniDesktopSizeClass.Medium -> 2
        OmniDesktopSizeClass.Compact -> 1
    }

    fun settingsMaxContentWidth(columns: Int): Float = when (columns.coerceIn(1, 3)) {
        3 -> 1120f
        2 -> 820f
        else -> 520f
    }
}
