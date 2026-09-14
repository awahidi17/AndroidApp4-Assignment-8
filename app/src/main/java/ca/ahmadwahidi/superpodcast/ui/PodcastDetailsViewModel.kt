package ca.ahmadwahidi.superpodcast.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ca.ahmadwahidi.superpodcast.data.Episode
import ca.ahmadwahidi.superpodcast.data.PodcastRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailsUiState(
    val loading: Boolean = false,
    val episodes: List<Episode> = emptyList(),
    val message: String? = null
)

/** Retrieves and exposes the RSS episode list independently of the Fragment. */
class PodcastDetailsViewModel(private val repository: PodcastRepository) : ViewModel() {
    private val _state = MutableStateFlow(DetailsUiState())
    val state: StateFlow<DetailsUiState> = _state.asStateFlow()
    private var loadedFeed: String? = null

    fun load(feedUrl: String?) {
        if (feedUrl.isNullOrBlank()) {
            _state.value = DetailsUiState(message = "This podcast does not publish an RSS feed")
            return
        }
        if (loadedFeed == feedUrl) return
        loadedFeed = feedUrl
        viewModelScope.launch {
            _state.value = DetailsUiState(loading = true)
            runCatching { repository.loadEpisodes(feedUrl) }
                .onSuccess { episodes ->
                    _state.value = DetailsUiState(
                        episodes = episodes,
                        message = if (episodes.isEmpty()) "No playable episodes found" else null
                    )
                }
                .onFailure { _state.value = DetailsUiState(message = "Unable to load this podcast feed") }
        }
    }

    class Factory(private val repository: PodcastRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PodcastDetailsViewModel(repository) as T
    }
}
