package ca.ahmadwahidi.superpodcast.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ca.ahmadwahidi.superpodcast.data.AdvancedFilter
import ca.ahmadwahidi.superpodcast.data.Podcast
import ca.ahmadwahidi.superpodcast.data.PodcastRepository
import ca.ahmadwahidi.superpodcast.util.PodcastFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val loading: Boolean = false,
    val podcasts: List<Podcast> = emptyList(),
    val message: String? = "Search for a podcast to begin"
)

/** Owns screen state so a rotation does not cancel or discard an active search. */
class SearchViewModel(
    application: Application,
    private val repository: PodcastRepository
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()
    private var unfilteredResults: List<Podcast> = emptyList()

    fun search(term: String, filter: AdvancedFilter, pattern: String) {
        if (term.isBlank()) {
            _state.value = SearchUiState(message = "Enter a podcast topic")
            return
        }
        viewModelScope.launch {
            _state.value = SearchUiState(loading = true)
            runCatching { repository.search(term.trim()) }
                .onSuccess { results ->
                    unfilteredResults = results
                    applyFilter(filter, pattern)
                }
                .onFailure { error ->
                    _state.value = SearchUiState(message = error.userMessage())
                }
        }
    }

    fun applyFilter(filter: AdvancedFilter, pattern: String) {
        val filtered = PodcastFilter.apply(unfilteredResults, filter, pattern)
        val message = if (filtered.isEmpty()) "No podcasts match these criteria" else null
        _state.value = SearchUiState(podcasts = filtered, message = message)
    }

    private fun Throwable.userMessage(): String = when {
        this is java.net.UnknownHostException -> "No internet connection"
        else -> message ?: "Unable to load podcasts"
    }

    class Factory(
        private val application: Application,
        private val repository: PodcastRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SearchViewModel(application, repository) as T
    }
}
