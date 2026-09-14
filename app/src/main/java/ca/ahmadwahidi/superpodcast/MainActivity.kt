package ca.ahmadwahidi.superpodcast

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ca.ahmadwahidi.superpodcast.databinding.ActivityMainBinding
import ca.ahmadwahidi.superpodcast.ui.SearchFragment
import ca.ahmadwahidi.superpodcast.ui.SubscriptionsFragment
import ca.ahmadwahidi.superpodcast.worker.PodcastUpdateScheduler

/** Hosts the two main areas of SuperPodcast. Created by Ahmad Wahidi. */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    /** Android 13+ asks before the background worker can display notifications. */
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Search and playback continue normally if the user declines. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Schedule one unique network-constrained update check for saved podcasts.
        PodcastUpdateScheduler.schedule(this)
        requestNotificationPermissionIfNeeded()

        if (savedInstanceState == null) showFragment(SearchFragment())

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> showFragment(SearchFragment())
                R.id.nav_subscriptions -> showFragment(SubscriptionsFragment())
                else -> false
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}
