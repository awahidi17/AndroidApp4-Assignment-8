# AI Reflection – SuperPodcast Assignment 8

**Ahmad Wahidi**

## 1. How Did You Use AI in This Assignment?

I used AI for a strong starting point and for help adding the parts I had not used much before. I kept the general project structure, but I changed and organized the code to fit my SuperPodcast app instead of copying one tutorial screen exactly.

One prompt I used was asking how to compare the newest RSS episode with the last episode saved for each subscription. The suggested approach used WorkManager and SharedPreferences. I adapted it so the worker only runs with a network connection and the first check saves a baseline instead of immediately showing a false “new episode” notification. I also asked for help improving MediaPlayer. I added a SeekBar, time display, stop button, and speed choices from 0.75x to 2.0x.

WorkManager, notification channels, and MediaPlayer playback parameters went beyond the networking basics I was most comfortable with. I researched what each component was responsible for, read the code in small sections, and followed the flow from the scheduler to the worker and then to the notification.

## 2. How Did You Understand, Verify, and Adapt the Code?

I verified the code by tracing one feature at a time. For search, I followed the term from the search field to Retrofit, then the repository, ViewModel state, and RecyclerView. For episode playback, I followed the RSS enclosure URL into MediaPlayer and checked how prepared, paused, completed, and error states changed the controls. I also used unit tests for the regex, title-word, invalid-regex, and episode-count filters. XML and resource references were checked so every layout ID and drawable had a matching resource.

One important change was preventing duplicate background jobs. I used unique periodic work with the KEEP policy because opening the app several times should not schedule several identical feed checks. Another change was separating the first background check from a real update. Without that change, every existing episode could look new the first time the worker ran. I also kept playback cleanup in the Fragment lifecycle so the MediaPlayer and progress updater do not continue after leaving the screen.

## 3. What Did You Learn or Get Better At Through This Work?

I improved the most at understanding how separate Android components cooperate. The API gives podcast information, the RSS feed gives episodes, SharedPreferences stores subscriptions, MediaPlayer streams audio, and WorkManager handles delayed background checks. Before this project, I understood these mostly as separate examples. Now I understand how data moves through them in one application.

What went well was dividing the app into models, repository, ViewModels, Fragments, adapters, utilities, and workers. That made it easier to find problems and test the filter logic by itself. What did not go smoothly was dealing with real podcast feeds because publishers do not all format RSS content exactly the same way, and some feeds redirect or fail. Adding empty and error states helped the app handle those cases without crashing. The playback state also took extra attention because the buttons, SeekBar, timer, and episode row all had to agree on whether audio was loading, playing, paused, or stopped.
