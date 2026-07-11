# Nocturne Prism — Phase 6 Artist & Album Implementation Map

AREA: Artist Profile State
CURRENT FILE: `Screens.kt` (`ArtistView`), `PlayerViewModel.kt`
CURRENT COMPONENT: `ArtistView`
STATE SOURCE: `PlayerViewModel.currentArtistId`, `YouTubeService.artist()`
REFERENCE TARGET: Cinematic hero, sections for Popular, Albums, Singles, Fans Also Like, About.
REUSE STRATEGY: Extract to `ArtistProfileView.kt`.
FUNCTIONAL DEPENDENCY: YouTube API.
RISK: Medium.
PLANNED CHANGE: Build `ArtistProfileView.kt`. Map `ArtistPage.sections` dynamically but style specific known sections ("Songs", "Albums", "Singles", "Fans might also like") explicitly per the reference. Render `description` in an "About" panel. 

AREA: Artist Hero
CURRENT FILE: `Screens.kt` (`ArtistView`)
CURRENT COMPONENT: `ArtistView`
STATE SOURCE: `ArtistItem`
REFERENCE TARGET: Cinematic crop, gradient overlay, large typography, primary actions.
REUSE STRATEGY: Build `OmniArtistHero`.
RISK: Low.
PLANNED CHANGE: Remove generic 16:9 crop. Use a blurred backdrop with a sharp circular/square portrait overlay if high-res cinematic crops aren't available, or a full cinematic mask if art allows. Wire "Play" to play the artist's first "Songs" section.

AREA: Album Detail State
CURRENT FILE: `Screens.kt` (`AlbumView`), `PlayerViewModel.kt`
CURRENT COMPONENT: `AlbumView`
STATE SOURCE: `PlayerViewModel.currentAlbumId`, `YouTubeService.album()`
REFERENCE TARGET: Hero header, track table, right-column for credits/featured.
REUSE STRATEGY: Extract to `AlbumDetailView.kt`.
FUNCTIONAL DEPENDENCY: YouTube API.
RISK: Medium.
PLANNED CHANGE: Build `AlbumDetailView.kt`. Implement `OmniAlbumHero`. Render tracks using `OmniSongRow`. Derive "Featured Artists" by diffing track artists vs album artists. Omit unavailable "Credits" and "Fans also like" gracefully to preserve data honesty.

AREA: Navigation & Playback
CURRENT FILE: `PlayerViewModel.kt`
CURRENT COMPONENT: N/A
STATE SOURCE: N/A
REFERENCE TARGET: Seamless artist <-> album loop without dropping queue state.
REUSE STRATEGY: Enhance queue handling if needed, but existing `playSong` array insertion is mostly safe.
RISK: Low.
PLANNED CHANGE: Wire all `onClick` handlers explicitly to `openAlbum` and `openArtist`. Wire all `Play` buttons to `playAlbum` or a new `playArtist` routine.
