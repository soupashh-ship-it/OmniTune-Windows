package com.omnitune.app.platform

import com.omnitune.app.service.YouTubeService
import com.omnitune.innertube.YouTube
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ProviderReliabilityRuntimeQaTest {
    @Test
    fun representativeProviderQueriesReturnBoundedResultsWhenExplicitlyEnabled() = runBlocking {
        val enabled = System.getProperty("omnitune.providerQa") == "true" ||
            System.getenv("OMNITUNE_PROVIDER_QA") == "true"
        if (!enabled) return@runBlocking

        val service = YouTubeService()
        val cases = listOf(
            QueryCase("globally popular track", "Blinding Lights", YouTube.SearchFilter.FILTER_SONG),
            QueryCase("Indian artist", "Arijit Singh", YouTube.SearchFilter.FILTER_ARTIST),
            QueryCase("long multi-word title", "Bohemian Rhapsody remastered queen", YouTube.SearchFilter.FILTER_SONG),
            QueryCase("album query", "Random Access Memories", YouTube.SearchFilter.FILTER_ALBUM),
            QueryCase("playlist query", "lofi beats playlist", YouTube.SearchFilter.FILTER_FEATURED_PLAYLIST),
            QueryCase("unicode query", "नाटू नाटू", YouTube.SearchFilter.FILTER_SONG),
        )

        val reportItems = JSONArray()
        var nonEmptyCases = 0
        withTimeout(90_000L) {
            for (case in cases) {
                val result = runCatching { service.search(case.query, case.filter).items }
                val count = result.getOrNull()?.size ?: 0
                if (count > 0) nonEmptyCases++
                reportItems.put(
                    JSONObject()
                        .put("label", case.label)
                        .put("query", case.query)
                        .put("filter", case.filter.value)
                        .put("resultCount", count)
                        .put("status", if (result.isSuccess) "OK" else "FAILED")
                        .put("error", result.exceptionOrNull()?.message ?: JSONObject.NULL)
                )
            }
        }

        val projectRoot = File(System.getProperty("user.dir")).let {
            if (it.name == "composeApp") it.parentFile else it
        }
        val reportFile = File(projectRoot, "docs/qa/provider-reliability-runtime-qa.json")
        reportFile.parentFile.mkdirs()
        reportFile.writeText(
            JSONObject()
                .put("cases", reportItems)
                .put("nonEmptyCases", nonEmptyCases)
                .put("totalCases", cases.size)
                .toString(2)
        )

        assertTrue(nonEmptyCases >= 4, "Provider returned non-empty results for only $nonEmptyCases/${cases.size} representative queries.")
    }

    private data class QueryCase(
        val label: String,
        val query: String,
        val filter: YouTube.SearchFilter,
    )
}
