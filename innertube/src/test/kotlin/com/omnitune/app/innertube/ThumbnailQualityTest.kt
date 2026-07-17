package com.omnitune.app.innertube

import com.omnitune.innertube.toHighResThumbnail
import kotlin.test.Test
import kotlin.test.assertEquals

class ThumbnailQualityTest {
    @Test
    fun googleImageThumbnailRequestsCrispArtwork() {
        val url = "https://lh3.googleusercontent.com/example=w60-h60-l90-rj"

        assertEquals(
            "https://lh3.googleusercontent.com/example=w1080-h1080-l95-rj",
            url.toHighResThumbnail(),
        )
    }

    @Test
    fun squareGoogleImageThumbnailRequestsCrispArtwork() {
        val url = "https://yt3.ggpht.com/example=s120-c-k-c0x00ffffff-no-rj"

        assertEquals(
            "https://yt3.ggpht.com/example=w1080-h1080-l95-rj",
            url.toHighResThumbnail(),
        )
    }

    @Test
    fun youtubeDefaultThumbnailRequestsMaxResolutionArtwork() {
        val url = "https://i.ytimg.com/vi/video-id/hqdefault.jpg"

        assertEquals(
            "https://i.ytimg.com/vi/video-id/maxresdefault.jpg",
            url.toHighResThumbnail(),
        )
    }

    @Test
    fun localCoverPathIsNotRewritten() {
        val path = "C:\\Users\\soupa\\Pictures\\playlist=cover.png"

        assertEquals(path, path.toHighResThumbnail())
    }
}
