package ca.ahmadwahidi.superpodcast.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.ahmadwahidi.superpodcast.MainActivity
import ca.ahmadwahidi.superpodcast.R
import ca.ahmadwahidi.superpodcast.data.PodcastRepository
import ca.ahmadwahidi.superpodcast.util.UpdateDetector

/** Checks subscribed RSS feeds and notifies the user when the newest enclosure changes. */
class PodcastUpdateWorker(
    context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters) {
    private val repository = PodcastRepository(context)

    override suspend fun doWork(): Result {
        var temporaryFailure = false

        repository.subscriptions().forEach { podcast ->
            val feedUrl = podcast.feedUrl ?: return@forEach
            runCatching { repository.loadEpisodes(feedUrl).firstOrNull() }
                .onSuccess { latest ->
                    if (latest != null) {
                        val previousUrl = repository.latestKnownEpisode(podcast.id)
                        if (UpdateDetector.isNewEpisode(previousUrl, latest.audioUrl)) {
                            showNotification(podcast.title, latest.title, podcast.id.toInt())
                        }
                        // The first successful check becomes the baseline without a false alert.
                        repository.rememberLatestEpisode(podcast.id, latest.audioUrl)
                    }
                }
                .onFailure { temporaryFailure = true }
        }

        return if (temporaryFailure) Result.retry() else Result.success()
    }

    private fun showNotification(podcastTitle: String, episodeTitle: String, id: Int) {
        createChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val openApp = PendingIntent.getActivity(
            applicationContext,
            id,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New episode: $podcastTitle")
            .setContentText(episodeTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(episodeTitle))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(id, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "New podcast episodes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Alerts for new episodes from subscribed podcasts" }
            applicationContext.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    companion object { private const val CHANNEL_ID = "podcast_updates" }
}
