package com.github.bernatbc.petitapetit.ui.listen

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.github.bernatbc.petitapetit.data.Episode
import com.github.bernatbc.petitapetit.data.EpisodeRepository
import com.github.bernatbc.petitapetit.data.TranscriptRepository
import com.github.bernatbc.petitapetit.data.translation.FallbackTranslator
import com.github.bernatbc.petitapetit.data.translation.MlKitTranslator
import com.github.bernatbc.petitapetit.data.translation.MyMemoryTranslator
import com.github.bernatbc.petitapetit.domain.WordLookup
import com.github.bernatbc.petitapetit.domain.TranscriptSegment
import com.github.bernatbc.petitapetit.player.PlaybackService
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class WordTranslation(
    val word: String,
    val translation: String? = null,
    val failed: Boolean = false,
)

data class ListenUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val episode: Episode? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val speed: Float = 1f,
    val transcriptLoading: Boolean = false,
    val transcript: List<TranscriptSegment> = emptyList(),
    val wordTranslation: WordTranslation? = null,
)

class ListenViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EpisodeRepository()
    private val transcriptRepository = TranscriptRepository()
    private val mlKitTranslator = MlKitTranslator()
    private val translator = FallbackTranslator(mlKitTranslator, MyMemoryTranslator())
    private val translationCache = HashMap<String, String>()
    private var translationJob: Job? = null
    private val _state = MutableStateFlow(ListenUiState())
    val state: StateFlow<ListenUiState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var progressJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = syncFromPlayer(player)
    }

    init {
        connectController()
        loadEpisode()
    }

    fun loadEpisode() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.fetchEpisodes().first() }
                .onSuccess { episode ->
                    _state.update { it.copy(loading = false, episode = episode) }
                    controller?.let(::syncFromPlayer)
                    loadTranscript(episode)
                }
                .onFailure { _state.update { it.copy(loading = false, error = true) } }
        }
    }

    private fun loadTranscript(episode: Episode) {
        _state.update { it.copy(transcriptLoading = true, transcript = emptyList()) }
        viewModelScope.launch {
            val transcript = runCatching { transcriptRepository.fetchTranscript(episode.id) }
                .getOrDefault(emptyList())
            _state.update { it.copy(transcriptLoading = false, transcript = transcript) }
        }
    }

    fun togglePlayback() {
        val player = controller ?: return
        val episode = _state.value.episode ?: return
        if (player.isPlaying) {
            player.pause()
            return
        }
        prepare(player, episode)
        player.play()
    }

    private fun prepare(player: Player, episode: Episode, startPositionMs: Long = 0L) {
        if (player.currentMediaItem?.mediaId != episode.id) {
            player.setMediaItem(episode.toMediaItem(), startPositionMs)
            player.prepare()
        } else if (player.playbackState == Player.STATE_IDLE) {
            player.prepare()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = controller ?: return
        val episode = _state.value.episode ?: return
        if (player.currentMediaItem?.mediaId != episode.id) {
            prepare(player, episode, positionMs)
        } else {
            player.seekTo(positionMs)
        }
        _state.update { it.copy(positionMs = positionMs) }
    }

    fun skip(deltaMs: Long) {
        val current = _state.value
        val limit = current.durationMs.takeIf { it > 0 } ?: Long.MAX_VALUE
        seekTo((current.positionMs + deltaMs).coerceIn(0L, limit))
    }

    fun cycleSpeed() {
        val player = controller ?: return
        val next = SPEEDS[(SPEEDS.indexOf(player.playbackParameters.speed) + 1) % SPEEDS.size]
        player.setPlaybackSpeed(next)
        _state.update { it.copy(speed = next) }
    }

    fun translateWord(rawWord: String) {
        translationJob?.cancel()
        val word = WordLookup.normalize(rawWord)
        if (word.isEmpty()) {
            _state.update { it.copy(wordTranslation = null) }
            return
        }
        val cached = translationCache[word.lowercase()]
        if (cached != null) {
            _state.update { it.copy(wordTranslation = WordTranslation(word, cached)) }
            return
        }
        _state.update { it.copy(wordTranslation = WordTranslation(word)) }
        translationJob = viewModelScope.launch {
            val translation = try {
                WordLookup.matchCase(word, translator.translate(word))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
            if (translation != null) translationCache[word.lowercase()] = translation
            _state.update {
                it.copy(wordTranslation = WordTranslation(word, translation, failed = translation == null))
            }
        }
    }

    fun dismissTranslation() {
        translationJob?.cancel()
        _state.update { it.copy(wordTranslation = null) }
    }

    private fun connectController() {
        val application = getApplication<Application>()
        val token = SessionToken(application, ComponentName(application, PlaybackService::class.java))
        val future = MediaController.Builder(application, token).buildAsync()
        future.addListener({
            val connected = runCatching { future.get() }.getOrNull() ?: return@addListener
            controller = connected
            connected.addListener(playerListener)
            syncFromPlayer(connected)
        }, MoreExecutors.directExecutor())
    }

    private fun syncFromPlayer(player: Player) {
        val episodeId = _state.value.episode?.id
        val matches = episodeId != null && player.currentMediaItem?.mediaId == episodeId
        _state.update {
            it.copy(
                isPlaying = matches && player.isPlaying,
                positionMs = if (matches) player.currentPosition else it.positionMs,
                durationMs = if (matches && player.duration > 0) player.duration else it.durationMs,
                speed = player.playbackParameters.speed,
            )
        }
        if (matches && player.isPlaying) startProgressUpdates(player) else progressJob?.cancel()
    }

    private fun startProgressUpdates(player: Player) {
        if (progressJob?.isActive == true) return
        progressJob = viewModelScope.launch {
            while (isActive) {
                _state.update { it.copy(positionMs = player.currentPosition) }
                delay(PROGRESS_INTERVAL_MS)
            }
        }
    }

    override fun onCleared() {
        controller?.removeListener(playerListener)
        controller?.release()
        controller = null
        mlKitTranslator.close()
    }

    private fun Episode.toMediaItem(): MediaItem = MediaItem.Builder()
        .setMediaId(id)
        .setUri(audioUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist("RFI – Journal en français facile")
                .build(),
        )
        .build()

    private companion object {
        val SPEEDS = listOf(1f, 0.75f, 0.5f, 1.25f)
        const val PROGRESS_INTERVAL_MS = 100L
    }
}
