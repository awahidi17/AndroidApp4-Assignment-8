package ca.ahmadwahidi.superpodcast.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.ahmadwahidi.superpodcast.data.Episode
import ca.ahmadwahidi.superpodcast.databinding.ItemEpisodeBinding

/** Renders playable RSS episodes and reports play button clicks to the Fragment. */
class EpisodeAdapter(private val onPlayClick: (Episode) -> Unit) :
    ListAdapter<Episode, EpisodeAdapter.EpisodeHolder>(EpisodeDiff) {
    private var playingUrl: String? = null
    private var loadingUrl: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = EpisodeHolder(
        ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: EpisodeHolder, position: Int) = holder.bind(getItem(position))

    fun setPlaying(url: String?) { playingUrl = url; notifyDataSetChanged() }
    fun setLoading(url: String?) { loadingUrl = url; notifyDataSetChanged() }

    inner class EpisodeHolder(private val binding: ItemEpisodeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(episode: Episode) = with(binding) {
            episodeTitle.text = episode.title
            episodeDate.text = episode.publicationDate.ifBlank { "Date unavailable" }
            episodeDescription.text = episode.description.ifBlank { "No description available" }
            playButton.text = when (episode.audioUrl) {
                loadingUrl -> "Loading…"
                playingUrl -> "Pause"
                else -> "Play"
            }
            playButton.isEnabled = episode.audioUrl != loadingUrl
            playButton.setOnClickListener { onPlayClick(episode) }
        }
    }

    private object EpisodeDiff : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(old: Episode, new: Episode) = old.audioUrl == new.audioUrl
        override fun areContentsTheSame(old: Episode, new: Episode) = old == new
    }
}
