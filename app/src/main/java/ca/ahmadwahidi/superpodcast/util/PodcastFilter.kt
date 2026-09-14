package ca.ahmadwahidi.superpodcast.util

import ca.ahmadwahidi.superpodcast.data.AdvancedFilter
import ca.ahmadwahidi.superpodcast.data.Podcast

/** Pure filtering logic is separated so it can be unit tested without Android. */
object PodcastFilter {
    fun apply(items: List<Podcast>, filter: AdvancedFilter, pattern: String = ""): List<Podcast> =
        when (filter) {
            AdvancedFilter.NONE -> items
            AdvancedFilter.FIVE_WORDS -> items.filter { wordCount(it.title) >= 5 }
            AdvancedFilter.FEWEST_EPISODES -> items.sortedBy { it.episodeCount }
            AdvancedFilter.REGEX -> {
                val regex = runCatching { Regex(pattern, RegexOption.IGNORE_CASE) }.getOrNull()
                if (regex == null) emptyList() else items.filter { regex.containsMatchIn(it.title) }
            }
        }

    private fun wordCount(text: String) = text.trim().split(Regex("\\s+")).count { it.isNotBlank() }
}
