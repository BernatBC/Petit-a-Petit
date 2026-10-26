package com.github.bernatbc.petitapetit

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import com.github.bernatbc.petitapetit.data.EpisodeRepository
import com.github.bernatbc.petitapetit.ui.components.AppTopBar
import com.github.bernatbc.petitapetit.ui.dictionary.DictionaryScreen
import com.github.bernatbc.petitapetit.ui.dictionary.DictionaryViewModel
import com.github.bernatbc.petitapetit.ui.home.HomeScreen
import com.github.bernatbc.petitapetit.ui.home.HomeViewModel
import com.github.bernatbc.petitapetit.ui.listen.ListenScreen
import com.github.bernatbc.petitapetit.ui.listen.ListenViewModel
import com.github.bernatbc.petitapetit.ui.prompt.PromptScreen
import com.github.bernatbc.petitapetit.ui.prompt.PromptViewModel
import com.github.bernatbc.petitapetit.ui.review.ReviewScreen
import com.github.bernatbc.petitapetit.ui.review.ReviewViewModel
import com.github.bernatbc.petitapetit.ui.settings.SettingsScreen
import com.github.bernatbc.petitapetit.ui.settings.SettingsViewModel
import com.github.bernatbc.petitapetit.ui.theme.PetitÀPetitTheme
import com.github.bernatbc.petitapetit.ui.write.WriteScreen
import com.github.bernatbc.petitapetit.ui.write.WriteViewModel

private object Routes {
    const val HOME = "home"
    const val DICTIONARY = "dictionary"
    const val SETTINGS = "settings"
    const val LISTEN = "listen"
    const val REVIEW = "review"
    const val WRITE = "write"
    const val PROMPT = "prompt"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetitÀPetitTheme {
                PetitÀPetitApp()
            }
        }
    }
}

@Composable
private fun PetitÀPetitApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val title = when (route) {
        Routes.DICTIONARY -> R.string.dictionary_title
        Routes.SETTINGS -> R.string.settings_title
        Routes.LISTEN -> R.string.step_listen
        Routes.REVIEW -> R.string.step_review
        Routes.WRITE -> R.string.step_write
        Routes.PROMPT -> R.string.step_speak
        else -> R.string.app_name
    }
    val step = when (route) {
        Routes.LISTEN -> 1
        Routes.REVIEW -> 2
        Routes.WRITE -> 3
        Routes.PROMPT -> 4
        else -> null
    }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        val reminderEnabled = (context.applicationContext as PetitApplication)
            .settingsRepository.reminder.first().enabled
        if (reminderEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationManagerCompat.from(context).areNotificationsEnabled()
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (route != Routes.HOME) {
                AppTopBar(
                    title = stringResource(title),
                    step = step,
                    onBack = if (route == Routes.DICTIONARY || route == Routes.SETTINGS) {
                        { navController.popBackStack() }
                    } else {
                        null
                    },
                )
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .imePadding()
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            enterTransition = { fadeIn(tween(220)) },
            exitTransition = { fadeOut(tween(120)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(120)) },
        ) {
            composable(Routes.HOME) {
                val viewModel: HomeViewModel = viewModel()
                HomeScreen(
                    onStartDaily = {
                        viewModel.startDaily { navController.navigate(Routes.LISTEN) }
                    },
                    onOpenDictionary = { navController.navigate(Routes.DICTIONARY) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    modifier = contentModifier,
                )
            }
            composable(Routes.SETTINGS) {
                val viewModel: SettingsViewModel = viewModel()
                val reminder by viewModel.reminder.collectAsStateWithLifecycle()
                SettingsScreen(
                    reminder = reminder,
                    onEnabledChange = viewModel::setEnabled,
                    onTimeChange = viewModel::setTime,
                    modifier = contentModifier,
                )
            }
            composable(Routes.DICTIONARY) {
                val viewModel: DictionaryViewModel = viewModel()
                val cards by viewModel.cards.collectAsStateWithLifecycle()
                DictionaryScreen(
                    cards = cards,
                    word = viewModel.word,
                    translation = viewModel.translation,
                    busy = viewModel.busy,
                    message = viewModel.message,
                    onWordChange = viewModel::onWordChange,
                    onTranslationChange = viewModel::onTranslationChange,
                    onTranslate = viewModel::translate,
                    onAdd = viewModel::add,
                    onDelete = viewModel::delete,
                    modifier = contentModifier,
                )
            }
            composable(Routes.REVIEW) {
                val viewModel: ReviewViewModel = viewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                ReviewScreen(
                    state = state,
                    onReveal = viewModel::reveal,
                    onGrade = viewModel::grade,
                    onContinue = {
                        navController.navigate(Routes.WRITE) {
                            popUpTo(Routes.REVIEW) { inclusive = true }
                        }
                    },
                    modifier = contentModifier,
                )
            }
            composable(Routes.LISTEN) {
                val viewModel: ListenViewModel = viewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                ListenScreen(
                    state = state,
                    onTogglePlayback = viewModel::togglePlayback,
                    onSeek = viewModel::seekTo,
                    onSkip = viewModel::skip,
                    onCycleSpeed = viewModel::cycleSpeed,
                    onWordTap = viewModel::translateWord,
                    onSaveWord = viewModel::saveCurrentWord,
                    onDismissTranslation = viewModel::dismissTranslation,
                    onRetry = viewModel::loadEpisode,
                    onOpenWebsite = { pageUrl ->
                        openUrl(context, pageUrl?.takeIf { it.isNotBlank() } ?: EpisodeRepository.WEBSITE_URL)
                    },
                    onDone = {
                        viewModel.pause()
                        navController.navigate(Routes.REVIEW)
                    },
                    modifier = contentModifier,
                )
            }
            composable(Routes.WRITE) {
                val viewModel: WriteViewModel = viewModel()
                WriteScreen(
                    topic = viewModel.topic,
                    summary = viewModel.summary,
                    wordCount = viewModel.wordCount,
                    canContinue = viewModel.canContinue,
                    onTopicChange = viewModel::onTopicChange,
                    onSummaryChange = viewModel::onSummaryChange,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate(Routes.PROMPT) },
                    modifier = contentModifier,
                )
            }
            composable(Routes.PROMPT) {
                val viewModel: PromptViewModel = viewModel()
                if (viewModel.loaded) {
                    PromptScreen(
                        prompt = viewModel.prompt,
                        onPromptChange = viewModel::onPromptChange,
                        onBack = { navController.popBackStack() },
                        onRestart = {
                            viewModel.restart {
                                navController.popBackStack(Routes.HOME, inclusive = false)
                            }
                        },
                        modifier = contentModifier,
                    )
                }
            }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: ActivityNotFoundException) {
        // No browser available.
    }
}
