package ca.ahmadwahidi.superpodcast.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Repository keeps network and persistence details outside the user interface.
 * This mirrors the separation of concerns introduced in the PodPlay tutorial.
 */
class PodcastRepository(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences("subscriptions", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val httpClient = OkHttpClient()

    private val api: PodcastApi = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PodcastApi::class.java)

    suspend fun search(term: String): List<Podcast> = api.searchPodcasts(term).results

    /** Downloads a publisher RSS feed and parses up to 40 playable episodes. */
    suspend fun loadEpisodes(feedUrl: String): List<Episode> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(feedUrl).build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) error("Feed request failed (${response.code})")
        response.body?.byteStream()?.use(::parseRss) ?: emptyList()
    }

    fun subscriptions(): List<Podcast> {
        val json = preferences.getString(KEY, "[]") ?: "[]"
        return runCatching {
            gson.fromJson<List<Podcast>>(json, object : TypeToken<List<Podcast>>() {}.type)
        }.getOrDefault(emptyList())
    }

    fun isSubscribed(id: Long): Boolean = subscriptions().any { it.id == id }

    fun toggleSubscription(podcast: Podcast): Boolean {
        val list = subscriptions().toMutableList()
        val existingIndex = list.indexOfFirst { it.id == podcast.id }
        val nowSubscribed = existingIndex == -1
        if (nowSubscribed) list.add(podcast) else list.removeAt(existingIndex)
        preferences.edit().putString(KEY, gson.toJson(list)).apply()
        return nowSubscribed
    }

    /** Stores the most recent known enclosure URL for background update comparisons. */
    fun latestKnownEpisode(podcastId: Long): String? =
        preferences.getString("latest_episode_$podcastId", null)

    fun rememberLatestEpisode(podcastId: Long, audioUrl: String) {
        preferences.edit().putString("latest_episode_$podcastId", audioUrl).apply()
    }

    private fun parseRss(stream: java.io.InputStream): List<Episode> {
        val parser = XmlPullParserFactory.newInstance().newPullParser().apply {
            setInput(stream, null)
        }
        val episodes = mutableListOf<Episode>()
        var insideItem = false
        var title = ""
        var description = ""
        var audioUrl = ""
        var date = ""

        while (parser.eventType != XmlPullParser.END_DOCUMENT && episodes.size < 40) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> when (parser.name.lowercase()) {
                    "item" -> insideItem = true
                    "title" -> if (insideItem) title = parser.nextText().trim()
                    "description", "summary" -> if (insideItem && description.isBlank()) {
                        description = parser.nextText().replace(Regex("<[^>]*>"), "").trim()
                    }
                    "pubdate" -> if (insideItem) date = parser.nextText().trim()
                    "enclosure" -> if (insideItem) audioUrl = parser.getAttributeValue(null, "url") ?: ""
                }
                XmlPullParser.END_TAG -> if (parser.name.equals("item", true)) {
                    if (title.isNotBlank() && audioUrl.isNotBlank()) {
                        episodes += Episode(title, description, audioUrl, date)
                    }
                    insideItem = false
                    title = ""; description = ""; audioUrl = ""; date = ""
                }
            }
            parser.next()
        }
        return episodes
    }

    companion object { private const val KEY = "saved_podcasts" }
}
