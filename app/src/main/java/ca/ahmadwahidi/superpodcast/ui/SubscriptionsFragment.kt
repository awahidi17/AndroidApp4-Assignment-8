package ca.ahmadwahidi.superpodcast.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import ca.ahmadwahidi.superpodcast.R
import ca.ahmadwahidi.superpodcast.data.Podcast
import ca.ahmadwahidi.superpodcast.data.PodcastRepository
import ca.ahmadwahidi.superpodcast.databinding.FragmentSubscriptionsBinding

/** Displays subscriptions saved on the device, including after an app restart. */
class SubscriptionsFragment : Fragment() {
    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: PodcastRepository
    private lateinit var adapter: PodcastAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        repository = PodcastRepository(requireContext())
        adapter = PodcastAdapter(::openDetails, { podcast ->
            repository.toggleSubscription(podcast)
            refresh()
        }, repository::isSubscribed)
        binding.subscriptionList.adapter = adapter
        refresh()
    }

    private fun refresh() {
        val items = repository.subscriptions()
        adapter.submitList(items)
        binding.emptyText.isVisible = items.isEmpty()
    }

    private fun openDetails(podcast: Podcast) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PodcastDetailsFragment.newInstance(podcast))
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
