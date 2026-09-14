package ca.ahmadwahidi.superpodcast

import ca.ahmadwahidi.superpodcast.util.UpdateDetector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifies that the worker does not create first-run or duplicate alerts. */
class UpdateDetectorTest {
    @Test fun firstCheck_isOnlyABaseline() {
        assertFalse(UpdateDetector.isNewEpisode(null, "episode-one.mp3"))
    }

    @Test fun unchangedEpisode_doesNotNotify() {
        assertFalse(UpdateDetector.isNewEpisode("episode-one.mp3", "episode-one.mp3"))
    }

    @Test fun changedEpisode_notifies() {
        assertTrue(UpdateDetector.isNewEpisode("episode-one.mp3", "episode-two.mp3"))
    }
}
