# AndroidApp4 – SuperPodcast

**MWD3B Android Development – Assignment 8**

SuperPodcast is the completed and polished version of the podcast application started in Assignment 7. It applies the networking, persistence, media, background-work, and interface concepts from the PodPlay tutorial while adding unusual podcast filters.

## Completed features

- Search podcasts through the iTunes Search API using Retrofit and Kotlin coroutines.
- Show network results in a RecyclerView with podcast artwork, author, genre, and episode count.
- Apply unusual criteria: regular-expression title matching, titles with five or more words, or fewest episodes first.
- Open a podcast details screen and download its RSS feed using OkHttp.
- Parse RSS/XML into playable episode models with `XmlPullParser`.
- Stream remote episode audio using Android `MediaPlayer`.
- Play, pause, resume, stop, and seek through an episode.
- Display the current playback time and total duration.
- Change playback speed while listening: 0.75×, 1.0×, 1.25×, 1.5×, or 2.0×.
- Subscribe or unsubscribe from the results, details, and subscriptions screens.
- Preserve subscriptions locally with SharedPreferences and Gson.
- Show loading, empty, invalid-filter, feed, playback, and network error states.
- Preserve asynchronous screen state with ViewModel, StateFlow, and coroutines.
- Use WorkManager to check subscribed RSS feeds every 12 hours when a network is available.
- Create a notification channel and notify the user when a newer episode is detected.
- Request Android 13+ notification permission without blocking the main application.
- Unit-test the advanced filtering rules and background update comparison.

## Project structure

```text
ca.ahmadwahidi.superpodcast
├── MainActivity.kt
├── data
│   ├── Models.kt
│   ├── PodcastApi.kt
│   └── PodcastRepository.kt
├── ui
│   ├── SearchFragment.kt / SearchViewModel.kt
│   ├── SubscriptionsFragment.kt
│   ├── PodcastDetailsFragment.kt / PodcastDetailsViewModel.kt
│   ├── PodcastAdapter.kt
│   └── EpisodeAdapter.kt
├── util
│   └── PodcastFilter.kt
└── worker
    ├── PodcastUpdateScheduler.kt
    └── PodcastUpdateWorker.kt
```

## Run in Android Studio

1. Extract the ZIP.
2. Open Android Studio and select **Open**.
3. Choose the extracted `AndroidApp4` folder, not the ZIP file.
4. Allow Gradle Sync to finish. The project uses JDK 17 and Android SDK 35.
5. Start an Android 7.0 (API 24) or newer emulator with internet access.
6. Select **Run > Run 'app'**.
7. Search for a topic such as `technology`, `business`, or `science`.
8. On Android 13 or newer, allow notifications when prompted if you want new-episode alerts.

No API key is required. Search results come from Apple's public iTunes Search API, and episodes come from each publisher's RSS feed.

## Suggested demonstration

1. Search for `technology`.
2. Choose **Title has 5+ words**, then press **Apply**.
3. Choose the regex filter and try `daily|tech`.
4. Subscribe to one result.
5. Open the podcast and wait for its RSS episodes to load.
6. Play an episode, pause it, and resume it.
7. Move the seek bar, change the speed, and stop playback.
8. Open **Subscriptions** and confirm the saved show appears after restarting the app.



- A few publishers block or remove old RSS feeds; SuperPodcast displays an error instead of crashing.
- Podcast search requires an internet connection.
- Cleartext traffic is enabled because some public podcast feeds still redirect through HTTP URLs.
- Android controls the exact time of periodic WorkManager execution to protect battery life.
- The first successful background feed check records a baseline; notifications begin only when a later episode is detected.
