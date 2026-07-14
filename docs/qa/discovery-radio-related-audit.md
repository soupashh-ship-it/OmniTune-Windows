# Discovery, Radio, and Related audit

Date: 2026-07-14

Scope: focused product-depth pass for Browse, Radio, and Now Playing Related. This is not a broad product audit.

## Browse

| Area | Current state |
|---|---|
| Production route | `BrowseView` in `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/Screens.kt` |
| Provider calls | `YouTubeService.explore()`, `YouTubeService.getChartsPage()`, `YouTubeService.browse()` for selected mood/genre |
| Shelves | Mood & Genres, New Releases, provider chart sections, selected mood/genre sections |
| Loading | `HomeShimmer()` |
| Success | Provider-backed shelves/cards only |
| Empty | Selected genre empty state |
| Failure | Retry state now shown for page load and selected genre load |
| Retry | Page retry reloads `explore()` + charts; genre retry reloads selected `browse()` |
| Stale safety | Genre `LaunchedEffect(selectedGenre, genreRetryNonce)` prevents old selected genre effect from being the active branch after selection changes |
| Deduplication | Genre sections use `distinctBy { it.id }`; radio seed derivation also dedupes |
| Continuation | Not broadly added; current visible Browse shelves do not expose a reliable generic continuation contract beyond provider page semantics |

Result: **PARTIAL -> IMPROVED**. Browse no longer leaves users with a dead provider failure state, but full live provider failure matrix and continuation depth remain partial.

## Radio

| Area | Current state |
|---|---|
| Production route | `RadioView` in `Screens.kt`; playback/session behavior in `PlayerViewModel` |
| Start entry points | Track seed through `startRadio(seedId, "song")`; provider endpoint through `startRadio(endpoint)` |
| Supported seeds | Track seeds and provider endpoints currently exposed by Radio UI |
| Session identity | `activeRadioSessionId` invalidates previous radio continuations when a new session starts |
| Current-song sync | Start sets queue, queue index, current song, then plays |
| Initial queue | `RadioSessionPolicy.initialQueue()` dedupes, avoids immediate current-seed duplicate where alternatives exist, and caps to 50 |
| Continuation trigger | `RadioSessionPolicy.shouldRequestContinuation()` requests near queue end only |
| Concurrent continuation | Suppressed while `radioContinuationJob` is active |
| Stale old session | Ignored by comparing captured session id with `activeRadioSessionId` |
| Queue bound | Continuation append capacity caps queue at 80 without shifting current index |
| Recent-repeat suppression | `RadioSessionPolicy.appendContinuation()` suppresses recent-window repeats |
| Failure bound | Consecutive continuation failures stop after 3 |
| Initial failure | Logged; no false queue/current-song state is created |
| Continuation failure | Existing playable queue is preserved; failure counter increments |
| Tests | `RadioSessionPolicyTest` |

Result: **SUBSTANTIALLY IMPROVED** for pure session/continuation logic. Live long-running provider/radio soak remains intentionally unclaimed.

## Related

| Area | Current state |
|---|---|
| Production UI | `RelatedPanel` in `NowPlayingView.kt` |
| Provider path | `PlayerViewModel.loadRelatedFor()` uses `YouTubeService.next()` to find `relatedEndpoint`, then `YouTubeService.related()` |
| Loading | Shimmer rows shown |
| Success | Songs/albums/artists/playlists rendered as clickable rows |
| Empty | Truthful empty state |
| Failure | Concise `"Couldn't load related tracks."` state |
| Retry | `player.retryRelated()` reloads for the current song |
| Stale track switch | Request token and current-song id check prevent older track results from replacing newer track results |
| Deduplication | `RelatedContentPolicy.clean()` uses stable item id |
| Current-track exclusion | Current `SongItem` id excluded |
| Result cap | 32 items |
| Playability | Song rows play; album/artist/playlist rows navigate through existing production callbacks |
| Tests | `RelatedContentPolicyTest` |

Result: **PARTIAL -> IMPROVED**. Related now has truthful loading/failure/retry states and stale protection. Physical late-provider-response race is not separately forced beyond the token/current-song implementation.
