package com.omnitune.app.window.screens

import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.ArtistItem
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YTItem
import com.omnitune.innertube.pages.HomePage
import java.time.LocalTime

internal data class HomeReferenceModel(
    val greeting: String,
    val heroCandidateCount: Int,
    val featuredItem: YTItem?,
    val companionItems: List<YTItem>,
    val continueListening: List<YTItem>,
    val quickPicks: List<YTItem>,
    val madeForYou: List<YTItem>,
    val trending: List<YTItem>,
    val newReleases: List<YTItem>,
)

internal fun buildHomeReferenceModel(
    home: HomePage,
    queueItems: List<YTItem>,
    heroIndex: Int,
    currentTime: LocalTime = LocalTime.now(),
): HomeReferenceModel {
    val usedIds = mutableSetOf<String>()

    fun classify(sectionTitle: String): HomeSectionType {
        val normalized = sectionTitle.lowercase().trim()
        return when {
            normalized.contains("quick") || normalized.contains("pick") || normalized.contains("listen again") -> HomeSectionType.QuickPicks
            normalized.contains("made for") || normalized.contains("mix") -> HomeSectionType.Personalized
            normalized.contains("new release") || normalized.contains("new album") -> HomeSectionType.NewReleases
            normalized.contains("trending") || normalized.contains("popular") || normalized.contains("chart") -> HomeSectionType.Trending
            normalized.contains("continue") || normalized.contains("jump back") -> HomeSectionType.ContinueListening
            else -> HomeSectionType.Unknown
        }
    }

    val classifiedSections = home.sections.groupBy { section -> classify(section.title) }

    fun sectionItems(type: HomeSectionType): List<YTItem> =
        classifiedSections[type].orEmpty().flatMap { it.items }

    fun YTItem.stableIdentity(): String = "${id.ifBlank { title }}:$title"

    fun pickDistinct(take: Int, vararg sources: List<YTItem>): List<YTItem> {
        val result = mutableListOf<YTItem>()
        val candidates = sources.flatMap { it }.distinctBy { it.stableIdentity() }
        candidates.forEach { item ->
            if (result.size < take && usedIds.add(item.stableIdentity())) {
                result += item
            }
        }
        candidates.forEach { item ->
            if (result.size < take && result.none { it.stableIdentity() == item.stableIdentity() }) {
                result += item
            }
        }
        result.forEach { usedIds.add(it.stableIdentity()) }
        return result
    }

    val greeting = when (currentTime.hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    val allItems = home.sections.flatMap { it.items }
    val allSongs = allItems.filterIsInstance<SongItem>()
    val mediaItems = allItems.filter { it is AlbumItem || it is PlaylistItem || it is ArtistItem }
    val heroCandidates = mediaItems
        .ifEmpty { allItems }
        .filter { it.thumbnail != null }
        .distinctBy { it.stableIdentity() }
    val featuredItem = heroCandidates.getOrNull(heroIndex.coerceIn(0, (heroCandidates.size - 1).coerceAtLeast(0)))
    if (featuredItem != null) usedIds.add(featuredItem.stableIdentity())

    return HomeReferenceModel(
        greeting = greeting,
        heroCandidateCount = heroCandidates.size,
        featuredItem = featuredItem,
        companionItems = pickDistinct(
            4,
            allSongs,
            sectionItems(HomeSectionType.QuickPicks),
            allItems,
        ),
        continueListening = pickDistinct(
            4,
            queueItems,
            sectionItems(HomeSectionType.ContinueListening),
            allSongs,
            allItems,
        ),
        quickPicks = pickDistinct(
            6,
            sectionItems(HomeSectionType.QuickPicks),
            sectionItems(HomeSectionType.Personalized),
            allItems,
        ),
        madeForYou = pickDistinct(
            3,
            sectionItems(HomeSectionType.Personalized),
            mediaItems,
            allItems,
        ),
        trending = pickDistinct(
            5,
            sectionItems(HomeSectionType.Trending),
            allSongs,
            allItems,
        ),
        newReleases = pickDistinct(
            5,
            sectionItems(HomeSectionType.NewReleases),
            mediaItems,
            allItems,
        ),
    )
}
