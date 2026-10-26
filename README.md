# Petit à Petit

Application Android (Kotlin, Jetpack Compose) pour améliorer son français en écoutant, en écrivant et en parlant.

## Parcours quotidien

1. **Écouter** : lecture du dernier épisode du « Journal en français facile » de RFI (flux RSS `https://francaisfacile.rfi.fr/fr/podcasts/journal-en-français-facile/podcast`), avec lecteur intégré (Media3, lecture en arrière-plan) et lien vers le site de RFI.
2. **Écrire** : résumé d'une actualité (brouillon enregistré automatiquement, 30 mots minimum).
3. **Parler** : génération d'un prompt (modifiable) qui est copié dans le presse-papiers, puis ouverture de l'application Gemini (ou de la version web) pour la correction et la discussion.

## Structure

- `data/` : flux RSS, épisodes, brouillon (DataStore)
- `domain/` : `PromptBuilder`
- `player/` : `PlaybackService` (Media3 `MediaSessionService`)
- `ui/` : écrans Compose (`listen`, `write`, `prompt`)

## Développement

```
./gradlew testDebugUnitTest assembleDebug
```
