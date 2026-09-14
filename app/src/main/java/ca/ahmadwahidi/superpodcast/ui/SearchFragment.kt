package ca.ahmadwahidi.superpodcast.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.ahmadwahidi.superpodcast.R
import ca.ahmadwahidi.superpodcast.data.AdvancedFilter
import ca.ahmadwahidi.superpodcast.data.PodcastRepository
import ca.ahmadwahidi.superpodcast.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch

/** Search screen with unusual filters requested by the assignment brief. */
class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: PodcastRepository
    private lateinit var adapter: PodcastAdapter

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModel.Factory(requireActivity().application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = PodcastRepository(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PodcastAdapter(
            onPodcastClick = { openDetails(it.id) },
            onSubscribeClick = { podcast ->
                repository.toggleSubscription(podcast)
                adapter.notifyDataSetChanged()
            },
            isSubscribed = repository::isSubscribed
        )
        binding.podcastList.adapter = adapter

        val filters = AdvancedFilter.entries
        binding.filterSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, filters.map { it.label }
        )

        binding.searchButton.setOnClickListener { submitSearch() }
        binding.searchInput.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEARCH) { submitSearch(); true } else false
        }
        binding.applyFilterButton.setOnClickListener {
            viewModel.applyFilter(selectedFilter(), binding.regexInput.text?.toString().orEmpty())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { ui ->
                    binding.progress.isVisible = ui.loading
                    binding.statusText.isVisible = ui.message != null
                    binding.statusText.text = ui.message
                    adapter.submitList(ui.podcasts)
                }
            }
        }
    }

    private fun submitSearch() = viewModel.search(
        binding.searchInput.text?.toString().orEmpty(),
        selectedFilter(),
        binding.regexInput.text?.toString().orEmpty()
    )

    private fun selectedFilter() = AdvancedFilter.entries[binding.filterSpinner.selectedItemPosition]

    private fun openDetails(podcastId: Long) {
        val podcast = viewModel.state.value.podcasts.firstOrNull { it.id == podcastId } ?: return
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PodcastDetailsFragment.newInstance(podcast))
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
