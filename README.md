# Petit à Petit

Application Android (Kotlin, Jetpack Compose) pour améliorer son français en écoutant, en écrivant et en parlant.

## Écran d'accueil

Deux options : lancer le parcours du jour, ou ouvrir « Mon dictionnaire » (ajout manuel de mots avec traduction automatique modifiable, liste et suppression).

## Parcours quotidien

1. **Écouter** : dernier épisode du « Journal en français facile » de RFI (flux RSS), lecteur intégré (Media3, lecture en arrière-plan) et transcription synchronisée mot à mot. Toucher un mot affiche sa traduction en anglais (ML Kit hors ligne, sinon l'API MyMemory) dans une bulle, sans interrompre la lecture. L'icône « + » de la bulle ajoute le mot (avec la phrase d'origine) au dictionnaire.
2. **Réviser** : jusqu'à 10 mots du dictionnaire à réviser aujourd'hui (mots du jour et des jours précédents), les plus difficiles d'abord. Répétition espacée simplifiée (SM-2) : « À revoir », « Difficile », « Bien », « Facile ». La révision peut être passée.
3. **Écrire** : résumé d'une actualité (brouillon enregistré automatiquement, 30 mots minimum).
4. **Parler** : génération d'un prompt (modifiable) copié dans le presse-papiers, puis ouverture de l'application Gemini (ou de la version web) pour la correction et la discussion.

## Structure

- `data/` : flux RSS, transcription, traduction, brouillon (DataStore), dictionnaire (SQLite)
- `domain/` : `PromptBuilder`, transcription, `Scheduler` (révision espacée), `WordLookup`
- `player/` : `PlaybackService` (Media3 `MediaSessionService`)
- `ui/` : écrans Compose (`home`, `dictionary`, `listen`, `review`, `write`, `prompt`)

## Développement

```
./gradlew testDebugUnitTest assembleDebug
```
