# AndroidApp4 – SuperPodcast

**MWD3B Android Development – Assignment 7**  
**Created by Ahmad Wahidi**

SuperPodcast is a Kotlin Android application based on the networking concepts from the PodPlay tutorial. It searches the public iTunes podcast directory, applies unusual search criteria, loads podcast episodes from RSS feeds, saves subscriptions, and streams episode audio.

## Assignment 7 features

- Search podcasts through the iTunes Search API using Retrofit and Kotlin coroutines.
- Show network results in a RecyclerView with podcast artwork, author, genre, and episode count.
- Apply unusual criteria: regular-expression title matching, titles with five or more words, or fewest episodes first.
- Open a podcast details screen and download its RSS feed using OkHttp.
- Parse RSS/XML into playable episode models with `XmlPullParser`.
- Play, pause, and resume remote episode audio using Android `MediaPlayer`.
- Subscribe or unsubscribe from the results, details, and subscriptions screens.
- Preserve subscriptions locally with SharedPreferences and Gson.
- Show loading, empty, invalid-filter, feed, playback, and network error states.
- Preserve asynchronous screen state with ViewModel, StateFlow, and coroutines.
- Unit-test the advanced filtering rules.

The attached reference project also contains Assignment 8 features such as background checks, notifications, playback seeking, and speed controls. Those are intentionally left for the next assignment, as the Assignment 7 instructions say the app does not have to be fully polished or complete yet.

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
└── util
    └── PodcastFilter.kt
```

## Run in Android Studio

1. Extract the ZIP.
2. Open Android Studio and select **Open**.
3. Choose the extracted `AndroidApp4` folder, not the ZIP file.
4. Allow Gradle Sync to finish. The project uses JDK 17 and Android SDK 35.
5. Start an Android 7.0 (API 24) or newer emulator with internet access.
6. Select **Run > Run 'app'**.
7. Search for a topic such as `technology`, `business`, or `science`.

No API key is required. Search results come from Apple's public iTunes Search API, and episodes come from each publisher's RSS feed.

## Suggested demonstration

1. Search for `technology`.
2. Choose **Title has 5+ words**, then press **Apply**.
3. Choose the regex filter and try `daily|tech`.
4. Subscribe to one result.
5. Open the podcast and wait for its RSS episodes to load.
6. Play an episode, pause it, and resume it.
7. Open **Subscriptions** and confirm the saved show appears.

## GitHub submission

Create an empty GitHub repository named `AndroidApp4`, then run from the project folder:

```bash
git init
git add .
git commit -m "Complete Assignment 7 - Ahmad Wahidi"
git branch -M main
git remote add origin https://github.com/awahidi17/AndroidApp4.git
git push -u origin main
```

Submit this repository link to the instructor:

`https://github.com/awahidi17/AndroidApp4.git`

## Rubric mapping

| Rubric criterion | Evidence in this project |
| --- | --- |
| Features | API search, JSON conversion, RSS networking/parsing, lists, details, playback, subscriptions, unusual filters, and state/error handling |
| Functionality | Full search-to-playback flow, persistent subscriptions, lifecycle-aware state, and filter unit tests |
| Commenting | Each architectural class and non-obvious network, RSS, persistence, filtering, and playback block is explained in English |

## Notes

- A few publishers block or remove old RSS feeds; SuperPodcast displays an error instead of crashing.
- Podcast search requires an internet connection.
- Cleartext traffic is enabled because some public podcast feeds still redirect through HTTP URLs.
