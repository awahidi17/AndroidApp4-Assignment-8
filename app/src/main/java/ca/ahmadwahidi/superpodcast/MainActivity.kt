package ca.ahmadwahidi.superpodcast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ca.ahmadwahidi.superpodcast.databinding.ActivityMainBinding
import ca.ahmadwahidi.superpodcast.ui.SearchFragment
import ca.ahmadwahidi.superpodcast.ui.SubscriptionsFragment

/** Hosts the two main areas of SuperPodcast. Created by Ahmad Wahidi. */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) showFragment(SearchFragment())

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> showFragment(SearchFragment())
                R.id.nav_subscriptions -> showFragment(SubscriptionsFragment())
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}
