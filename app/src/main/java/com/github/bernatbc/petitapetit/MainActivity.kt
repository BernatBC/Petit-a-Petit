package com.github.bernatbc.petitapetit

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.bernatbc.petitapetit.data.EpisodeRepository
import com.github.bernatbc.petitapetit.ui.listen.ListenScreen
import com.github.bernatbc.petitapetit.ui.listen.ListenViewModel
import com.github.bernatbc.petitapetit.ui.prompt.PromptScreen
import com.github.bernatbc.petitapetit.ui.prompt.PromptViewModel
import com.github.bernatbc.petitapetit.ui.theme.PetitÀPetitTheme
import com.github.bernatbc.petitapetit.ui.write.WriteScreen
import com.github.bernatbc.petitapetit.ui.write.WriteViewModel

private object Routes {
    const val LISTEN = "listen"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetitÀPetitApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val title = when (backStackEntry?.destination?.route) {
        Routes.WRITE -> R.string.step_write
        Routes.PROMPT -> R.string.step_speak
        else -> R.string.step_listen
    }
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(title)) }) },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        NavHost(navController = navController, startDestination = Routes.LISTEN) {
            composable(Routes.LISTEN) {
                val viewModel: ListenViewModel = viewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                ListenScreen(
                    state = state,
                    onTogglePlayback = viewModel::togglePlayback,
                    onSeek = viewModel::seekTo,
                    onSkip = viewModel::skip,
                    onCycleSpeed = viewModel::cycleSpeed,
                    onRetry = viewModel::loadEpisode,
                    onOpenWebsite = { pageUrl ->
                        openUrl(context, pageUrl?.takeIf { it.isNotBlank() } ?: EpisodeRepository.WEBSITE_URL)
                    },
                    onDone = { navController.navigate(Routes.WRITE) },
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
                                navController.popBackStack(Routes.LISTEN, inclusive = false)
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
