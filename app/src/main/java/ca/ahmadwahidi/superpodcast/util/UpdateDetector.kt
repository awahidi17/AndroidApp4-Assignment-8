package ca.ahmadwahidi.superpodcast.util

/** Pure comparison rule used by the worker and covered by local unit tests. */
object UpdateDetector {
    fun isNewEpisode(previousUrl: String?, latestUrl: String): Boolean =
        previousUrl != null && previousUrl != latestUrl
}
