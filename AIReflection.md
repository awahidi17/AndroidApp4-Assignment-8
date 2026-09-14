# AI Reflection – SuperPodcast Assignment 7

**Ahmad Wahidi**

## 1. How did you use AI during this assignment?

I used AI as a development and learning assistant while creating SuperPodcast. It helped me organize the project, connect Retrofit to the iTunes Search API, understand the JSON fields returned for podcasts, parse RSS feeds, display dynamic results in RecyclerViews, save subscriptions, and stream episode audio. I also used it to design unusual search criteria such as regular-expression matching, long-title filtering, and sorting podcasts by their episode count.

## 2. How did you understand, verify, and adapt the suggestions?

I reviewed each class by responsibility instead of treating the generated code as one large block. I traced the user flow from a search term to the Retrofit request, the repository, the ViewModel state, and finally the RecyclerView. I used the same approach for RSS episodes and MediaPlayer. I adapted the examples to my own package name and SuperPodcast screens, then added unit tests for every advanced filter. I also included visible loading, empty, and error states so failed network requests do not crash the application.

## 3. What did you learn?

I learned how an Android app communicates with an external JSON service and how Retrofit maps a response into Kotlin data classes. I learned that podcast directories return an RSS feed URL, while a second network request is needed to retrieve actual episodes. Parsing XML helped me understand the relationship between RSS `item`, `title`, `description`, `pubDate`, and `enclosure` elements. I also improved my understanding of ViewModel, StateFlow, coroutines, RecyclerView, SharedPreferences, Gson, and MediaPlayer. Separating networking, state, interface code, and filter logic made the app easier to test and debug.
