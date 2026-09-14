package ca.ahmadwahidi.superpodcast.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.ahmadwahidi.superpodcast.data.Episode
import ca.ahmadwahidi.superpodcast.data.Podcast
import ca.ahmadwahidi.superpodcast.data.PodcastRepository
import ca.ahmadwahidi.superpodcast.databinding.FragmentPodcastDetailsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.launch

/** Shows episodes from RSS and plays their remote audio enclosure URLs. */
class PodcastDetailsFragment : Fragment() {
    private var _binding: FragmentPodcastDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var podcast: Podcast
    private lateinit var repository: PodcastRepository
    private lateinit var adapter: EpisodeAdapter
    private var mediaPlayer: MediaPlayer? = null
    private var playingUrl: String? = null

    private val viewModel: PodcastDetailsViewModel by viewModels {
        PodcastDetailsViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = PodcastRepository(requireContext())
        podcast = Gson().fromJson(requireArguments().getString(ARG_PODCAST), Podcast::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentPodcastDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.podcastTitle.text = podcast.title
        binding.podcastAuthor.text = podcast.author
        binding.subscribeButton.text = subscriptionLabel()
        binding.subscribeButton.setOnClickListener {
            repository.toggleSubscription(podcast)
            binding.subscribeButton.text = subscriptionLabel()
        }

        adapter = EpisodeAdapter(::playOrPause)
        binding.episodeList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { ui ->
                    binding.progress.isVisible = ui.loading
                    binding.statusText.isVisible = ui.message != null
                    binding.statusText.text = ui.message
                    adapter.submitList(ui.episodes)
                }
            }
        }
        viewModel.load(podcast.feedUrl)
    }

    private fun subscriptionLabel() = if (repository.isSubscribed(podcast.id)) "Subscribed" else "Subscribe"

    /** Uses Android MediaPlayer streaming; tapping the active episode pauses/resumes it. */
    private fun playOrPause(episode: Episode) {
        val current = mediaPlayer
        if (playingUrl == episode.audioUrl && current != null) {
            if (current.isPlaying) current.pause() else current.start()
            adapter.setPlaying(if (current.isPlaying) episode.audioUrl else null)
            return
        }

        releasePlayer()
        adapter.setLoading(episode.audioUrl)
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build()
            )
            setDataSource(episode.audioUrl)
            setOnPreparedListener {
                playingUrl = episode.audioUrl
                adapter.setLoading(null)
                adapter.setPlaying(episode.audioUrl)
                it.start()
            }
            setOnCompletionListener { adapter.setPlaying(null) }
            setOnErrorListener { _, _, _ ->
                adapter.setLoading(null)
                Snackbar.make(binding.root, "This episode could not be played", Snackbar.LENGTH_LONG).show()
                true
            }
            prepareAsync()
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        playingUrl = null
        if (::adapter.isInitialized) { adapter.setPlaying(null); adapter.setLoading(null) }
    }

    override fun onDestroyView() { releasePlayer(); super.onDestroyView(); _binding = null }

    companion object {
        private const val ARG_PODCAST = "podcast"
        fun newInstance(podcast: Podcast) = PodcastDetailsFragment().apply {
            arguments = Bundle().apply { putString(ARG_PODCAST, Gson().toJson(podcast)) }
        }
    }
}
