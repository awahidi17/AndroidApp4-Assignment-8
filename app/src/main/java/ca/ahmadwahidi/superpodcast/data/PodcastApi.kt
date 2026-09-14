package ca.ahmadwahidi.superpodcast.data

import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit turns this interface into an implementation of the iTunes endpoint. */
interface PodcastApi {
    @GET("search")
    suspend fun searchPodcasts(
        @Query("term") term: String,
        @Query("media") media: String = "podcast",
        @Query("entity") entity: String = "podcast",
        @Query("country") country: String = "CA",
        @Query("limit") limit: Int = 50
    ): PodcastSearchResponse
}
