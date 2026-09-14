package ca.ahmadwahidi.superpodcast.data

import com.google.gson.annotations.SerializedName

/** JSON response returned by the public iTunes Search API. */
data class PodcastSearchResponse(
    val resultCount: Int = 0,
    val results: List<Podcast> = emptyList()
)

/** Only the fields SuperPodcast needs are mapped from the larger iTunes object. */
data class Podcast(
    @SerializedName("collectionId") val id: Long,
    @SerializedName("collectionName") val title: String,
    @SerializedName("artistName") val author: String,
    @SerializedName("artworkUrl600") val artworkUrl: String?,
    @SerializedName("feedUrl") val feedUrl: String?,
    @SerializedName("trackCount") val episodeCount: Int = 0,
    @SerializedName("primaryGenreName") val genre: String? = null,
    @SerializedName("collectionPrice") val price: Double = 0.0
)

/** A playable podcast episode parsed from the publisher's RSS feed. */
data class Episode(
    val title: String,
    val description: String,
    val audioUrl: String,
    val publicationDate: String
)

enum class AdvancedFilter(val label: String) {
    NONE("No advanced filter"),
    REGEX("Title matches regular expression"),
    FIVE_WORDS("Title has 5+ words"),
    FEWEST_EPISODES("Fewest episodes first")
}
