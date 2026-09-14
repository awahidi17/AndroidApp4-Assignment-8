package ca.ahmadwahidi.superpodcast.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
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
    private var playerPrepared = false
    private val progressHandler = Handler(Looper.getMainLooper())
    private val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    private val progressUpdate = object : Runnable {
        override fun run() {
            val player = mediaPlayer
            val currentBinding = _binding
            if (player != null && playerPrepared && currentBinding != null) {
                currentBinding.playbackSeek.progress = player.currentPosition
                currentBinding.timeText.text =
                    "${formatTime(player.currentPosition)} / ${formatTime(player.duration)}"
                progressHandler.postDelayed(this, 500)
            }
        }
    }

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
            val subscribed = repository.toggleSubscription(podcast)
            if (subscribed) {
                // A known current episode prevents a false notification after subscribing.
                viewModel.state.value.episodes.firstOrNull()?.let {
                    repository.rememberLatestEpisode(podcast.id, it.audioUrl)
                }
            }
            binding.subscribeButton.text = subscriptionLabel()
        }

        adapter = EpisodeAdapter(::playOrPause)
        binding.episodeList.adapter = adapter
        setupPlaybackControls()

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

    /** Configures seek, play/pause, stop, and live playback-speed controls. */
    private fun setupPlaybackControls() = with(binding) {
        speedSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            speeds.map { "${it}x" }
        )
        speedSpinner.setSelection(1)
        speedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyPlaybackSpeed(speeds[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        playPauseButton.setOnClickListener { toggleCurrentPlayback() }
        stopButton.setOnClickListener { stopPlayback() }
        playbackSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && playerPrepared) {
                    timeText.text = "${formatTime(progress)} / ${formatTime(mediaPlayer?.duration ?: 0)}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                progressHandler.removeCallbacks(progressUpdate)
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (playerPrepared && seekBar != null) mediaPlayer?.seekTo(seekBar.progress)
                progressHandler.post(progressUpdate)
            }
        })
    }

    /** Uses MediaPlayer streaming; tapping the active episode pauses or resumes it. */
    private fun playOrPause(episode: Episode) {
        val current = mediaPlayer
        if (playingUrl == episode.audioUrl && current != null && playerPrepared) {
            toggleCurrentPlayback()
            return
        }

        releasePlayer()
        adapter.setLoading(episode.audioUrl)
        binding.playerCard.isVisible = true
        binding.nowPlaying.text = episode.title
        binding.playPauseButton.text = "Loading…"
        binding.playPauseButton.isEnabled = false
        binding.playbackSeek.progress = 0
        binding.timeText.text = "0:00 / 0:00"
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build()
            )
            setDataSource(episode.audioUrl)
            setOnPreparedListener {
                playingUrl = episode.audioUrl
                playerPrepared = true
                adapter.setLoading(null)
                adapter.setPlaying(episode.audioUrl)
                binding.playPauseButton.isEnabled = true
                binding.playPauseButton.text = "Pause"
                binding.playbackSeek.max = it.duration.coerceAtLeast(0)
                binding.timeText.text = "0:00 / ${formatTime(it.duration)}"
                applyPlaybackSpeed(speeds[binding.speedSpinner.selectedItemPosition])
                it.start()
                progressHandler.post(progressUpdate)
            }
            setOnCompletionListener { stopPlayback() }
            setOnErrorListener { _, _, _ ->
                adapter.setLoading(null)
                Snackbar.make(binding.root, "This episode could not be played", Snackbar.LENGTH_LONG).show()
                stopPlayback()
                true
            }
            prepareAsync()
        }
    }

    private fun toggleCurrentPlayback() {
        val player = mediaPlayer ?: return
        if (!playerPrepared) return
        if (player.isPlaying) player.pause() else player.start()
        binding.playPauseButton.text = if (player.isPlaying) "Pause" else "Resume"
        adapter.setPlaying(if (player.isPlaying) playingUrl else null)
    }

    private fun applyPlaybackSpeed(speed: Float) {
        val player = mediaPlayer ?: return
        if (!playerPrepared) return
        val wasPlaying = player.isPlaying
        runCatching {
            player.playbackParams = player.playbackParams.setSpeed(speed)
            // Some Android versions resume when playback parameters change.
            if (!wasPlaying) player.pause()
        }
    }

    private fun stopPlayback() {
        releasePlayer()
        if (_binding != null) binding.playerCard.isVisible = false
    }

    private fun releasePlayer() {
        progressHandler.removeCallbacks(progressUpdate)
        playerPrepared = false
        mediaPlayer?.release()
        mediaPlayer = null
        playingUrl = null
        if (::adapter.isInitialized) { adapter.setPlaying(null); adapter.setLoading(null) }
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = (milliseconds.coerceAtLeast(0) / 1000)
        return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }

    override fun onDestroyView() { releasePlayer(); super.onDestroyView(); _binding = null }

    companion object {
        private const val ARG_PODCAST = "podcast"
        fun newInstance(podcast: Podcast) = PodcastDetailsFragment().apply {
            arguments = Bundle().apply { putString(ARG_PODCAST, Gson().toJson(podcast)) }
        }
    }
}
