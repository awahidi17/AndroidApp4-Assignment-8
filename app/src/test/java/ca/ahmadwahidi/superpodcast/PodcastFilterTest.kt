package ca.ahmadwahidi.superpodcast

import ca.ahmadwahidi.superpodcast.data.AdvancedFilter
import ca.ahmadwahidi.superpodcast.data.Podcast
import ca.ahmadwahidi.superpodcast.util.PodcastFilter
import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests verify the unusual criteria without requiring a device or network. */
class PodcastFilterTest {
    private val podcasts = listOf(
        podcast(1, "Daily Tech News Show", 500),
        podcast(2, "The Very Unusual Science Podcast Today", 80),
        podcast(3, "History Hour", 30)
    )

    @Test fun fiveWordFilter_keepsLongTitles() {
        val result = PodcastFilter.apply(podcasts, AdvancedFilter.FIVE_WORDS)
        assertEquals(listOf(2L), result.map { it.id })
    }

    @Test fun regexFilter_matchesIgnoringCase() {
        val result = PodcastFilter.apply(podcasts, AdvancedFilter.REGEX, "tech|history")
        assertEquals(listOf(1L, 3L), result.map { it.id })
    }

    @Test fun fewestEpisodes_sortsAscending() {
        val result = PodcastFilter.apply(podcasts, AdvancedFilter.FEWEST_EPISODES)
        assertEquals(listOf(3L, 2L, 1L), result.map { it.id })
    }

    @Test fun invalidRegex_returnsEmptyList() {
        assertEquals(emptyList<Podcast>(), PodcastFilter.apply(podcasts, AdvancedFilter.REGEX, "["))
    }

    private fun podcast(id: Long, title: String, count: Int) = Podcast(
        id, title, "Test Author", null, null, count, "Education"
    )
}
