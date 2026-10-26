# Petit à Petit

An Android app (Kotlin, Jetpack Compose) that helps you improve your French by listening, writing and speaking. The app itself is in French.

## Home screen

- **Lancer le parcours du jour**: starts the daily workflow (below).
- **Mon dictionnaire**: your personal dictionary. Add words by hand (the English translation is filled in automatically and can be edited), browse them and delete them.
- **Paramètres**: settings, currently the daily reminder.

## Daily workflow

1. **Listen**: the latest episode of RFI's "Journal en français facile", played in the app (Media3, keeps playing in the background), with a link to the RFI website. The transcript is shown next to the player and highlights the word being spoken. Tapping a word shows its English translation in a popup without interrupting playback. The **+** button in the popup saves the word, together with the sentence it came from, to the dictionary.
2. **Review**: up to 10 dictionary words due today (words from today and from previous days), hardest first. It uses a simplified SM-2 spaced-repetition scheme with four answers: *À revoir*, *Difficile*, *Bien*, *Facile*. The review can be skipped.
3. **Write**: a summary of a piece of news from the episode (the draft is saved automatically, 30 words minimum). Each daily run starts with an empty summary.
4. **Speak**: a prompt (editable) is generated and copied to the clipboard, then the Gemini app (or its web version) is opened to correct the summary and start a conversation.

## Daily reminder

A notification reminds you to do your daily French. It is enabled by default at 18:00, and both the time and the on/off switch can be changed in the settings. On Android 13+ the app asks for the notification permission.

The reminder uses an inexact alarm, so it needs no special permission but may arrive a few minutes late when the device is in Doze mode. Alarms are scheduled again after a reboot or an app update.

## Data sources

- **Episodes**: the podcast RSS feed, `https://francaisfacile.rfi.fr/fr/podcasts/journal-en-français-facile/podcast`.
- **Transcript**: word-level timings from the JSON endpoint used by the RFI website player, `https://francaisfacile.rfi.fr/fr/_ws/media/editions/<episode id>/0`. This is not a documented API and may change.
- **Translation**: ML Kit on-device translation (French to English, about 30 MB model downloaded on first launch). While the model is not available, or if it fails, the MyMemory online API is used, which sends the tapped word to a third party.

## Project structure

- `data/`: RSS feed, transcript, translation, summary draft (DataStore), dictionary (SQLite), settings (DataStore)
- `domain/`: `PromptBuilder`, transcript timing, `Scheduler` (spaced repetition), `WordLookup`, reminder time
- `player/`: `PlaybackService` (Media3 `MediaSessionService`)
- `reminder/`: alarm scheduling, notification and receivers
- `ui/`: Compose screens (`home`, `dictionary`, `listen`, `review`, `write`, `prompt`, `settings`)

## Development

```
./gradlew testDebugUnitTest assembleDebug
```
