package ca.ahmadwahidi.superpodcast.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ca.ahmadwahidi.superpodcast.data.Podcast
import ca.ahmadwahidi.superpodcast.databinding.ItemPodcastBinding

/** RecyclerView adapter uses DiffUtil to update only rows that changed. */
class PodcastAdapter(
    private val onPodcastClick: (Podcast) -> Unit,
    private val onSubscribeClick: (Podcast) -> Unit,
    private val isSubscribed: (Long) -> Boolean
) : ListAdapter<Podcast, PodcastAdapter.PodcastHolder>(PodcastDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PodcastHolder(
        ItemPodcastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: PodcastHolder, position: Int) = holder.bind(getItem(position))

    inner class PodcastHolder(private val binding: ItemPodcastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(podcast: Podcast) = with(binding) {
            title.text = podcast.title
            author.text = podcast.author
            metadata.text = "${podcast.genre ?: "Podcast"} • ${podcast.episodeCount} episodes"
            artwork.load(podcast.artworkUrl) { crossfade(true) }
            subscribeButton.text = if (isSubscribed(podcast.id)) "Subscribed" else "Subscribe"
            root.setOnClickListener { onPodcastClick(podcast) }
            subscribeButton.setOnClickListener { onSubscribeClick(podcast) }
        }
    }

    private object PodcastDiff : DiffUtil.ItemCallback<Podcast>() {
        override fun areItemsTheSame(old: Podcast, new: Podcast) = old.id == new.id
        override fun areContentsTheSame(old: Podcast, new: Podcast) = old == new
    }
}
