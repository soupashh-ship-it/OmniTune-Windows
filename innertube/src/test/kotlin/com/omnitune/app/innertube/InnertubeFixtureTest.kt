package com.omnitune.innertube

import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YouTubeClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class InnertubeFixtureTest {

    private val originalClient = YouTube.innerTube.httpClient

    @OptIn(ExperimentalSerializationApi::class)
    private fun mockClient(fixtureName: String): HttpClient {
        val content = File("src/test/resources/fixtures/$fixtureName").readText()
        return mockClientWithContent(content)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun mockClientWithContent(content: String): HttpClient {
        val mockEngine = MockEngine { request ->
            respond(
                content = content,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(mockEngine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    encodeDefaults = true
                })
            }
            defaultRequest {
                url(YouTubeClient.API_URL_YOUTUBE_MUSIC)
            }
        }
    }

    @After
    fun teardown() {
        YouTube.innerTube.httpClient = originalClient
    }

    @Test
    fun `search parses song results correctly`() = runBlocking {
        YouTube.innerTube.httpClient = mockClient("search_shape_of_you.json")
        try {
            val result = YouTube.search("shape of you", YouTube.SearchFilter.FILTER_SONG)
            val items = result.getOrThrow().items
            
            assertTrue(items.isNotEmpty(), "Should parse items from fixture")
            
            val first = items.firstOrNull { it is SongItem } as? SongItem
            assertTrue(first != null, "Should find a SongItem")
            assertTrue(first.title.isNotEmpty())
            assertTrue(first.id.isNotEmpty())
            assertTrue(first.artists.isNotEmpty())
        } finally {
            YouTube.innerTube.httpClient = originalClient
        }
    }

    @Test
    fun `search parses album results correctly`() = runBlocking {
        YouTube.innerTube.httpClient = mockClient("search_album.json")
        try {
            val result = YouTube.search("divide ed sheeran", YouTube.SearchFilter.FILTER_ALBUM)
            val items = result.getOrThrow().items
            
            assertTrue(items.isNotEmpty(), "Should parse items from fixture")
            val first = items.firstOrNull { it is AlbumItem } as? AlbumItem
            assertTrue(first != null, "Should find an AlbumItem")
            assertTrue(first.title.isNotEmpty())
            assertTrue(first.browseId.isNotEmpty())
        } finally {
            YouTube.innerTube.httpClient = originalClient
        }
    }

    @Test
    fun `network error returns failure result`() = runBlocking {
        val mockEngine = MockEngine { 
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }
        YouTube.innerTube.httpClient = HttpClient(mockEngine) {
            expectSuccess = true
        }
        
        try {
            val result = YouTube.search("shape of you", YouTube.SearchFilter.FILTER_SONG)
            assertTrue(result.isFailure, "Search should return a failure result on 500 error")
        } finally {
            YouTube.innerTube.httpClient = originalClient
        }
    }

    @Test
    fun `malformed json returns failure result`() = runBlocking {
        val mockEngine = MockEngine { 
            respond(
                content = "{ \"invalid\": true ",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        YouTube.innerTube.httpClient = HttpClient(mockEngine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        
        try {
            val result = YouTube.playlist("RDCLAK5uy_m-z0R29Fv0o80Qj5oXwE5nF0M7yN-Q4OQ")
            assertTrue(result.isFailure, "Playlist parsing should return failure on malformed JSON")
        } finally {
            YouTube.innerTube.httpClient = originalClient
        }
    }

    @Test
    fun `search with missing result sections returns empty result instead of throwing`() = runBlocking {
        YouTube.innerTube.httpClient = mockClientWithContent("""{"contents":{}}""")
        try {
            val result = YouTube.search("schema drift", YouTube.SearchFilter.FILTER_SONG)
            val items = result.getOrThrow().items
            assertTrue(items.isEmpty(), "Search should degrade to an empty list when result sections are missing")
        } finally {
            YouTube.innerTube.httpClient = originalClient
        }
    }

    @Test
    fun `playlist with missing header returns failure instead of uncaught parser crash`() = runBlocking {
        YouTube.innerTube.httpClient = mockClientWithContent("""{"contents":{}}""")
        try {
            val result = YouTube.playlist("VLschema-drift")
            assertTrue(result.isFailure, "Playlist should return a controlled failure when required header data is missing")
        } finally {
            YouTube.innerTube.httpClient = originalClient
        }
    }

}
