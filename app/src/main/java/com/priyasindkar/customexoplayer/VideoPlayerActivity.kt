package com.priyasindkar.customexoplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.priyasindkar.customexoplayer.ui.theme.CustomExoPlayerTheme

class VideoPlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CustomExoPlayerTheme {
                // can resume video playback from this last paused position
                val lastPaused = ""

                VideoPlayerScreen(
                    lastPaused,
                    returnToHomeScreen = {
                        // go back
                        finish()
                    },
                    showSurvey = {
                        // Playback finished do something
                    }
                )
            }
        }
    }
}

@Composable
fun VideoPlayerScreen(
    lastPaused: String,
    returnToHomeScreen: () -> Unit,
    showSurvey: () -> Unit
) {
    val viewModel: VideoPlayerViewModel = viewModel()
    val videoPlayerUiState by viewModel.videoPlayerUiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            VideoPlayer(
                context = LocalContext.current,
                modifier = Modifier
                    .fillMaxSize(),
                videoUrl = videoPlayerUiState.extractedVideoUrl,
                lastPaused = lastPaused,
                returnToHomeScreen = returnToHomeScreen,  // go back
                showSurvey = showSurvey
            )
        }
    }
}

@Composable
fun BackPressHandler(
    backPressedDispatcher: OnBackPressedDispatcher? =
        LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
    onBackPressed: () -> Unit
) {
    val currentOnBackPressed by rememberUpdatedState(newValue = onBackPressed)

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentOnBackPressed()
            }
        }
    }

    DisposableEffect(key1 = backPressedDispatcher) {
        backPressedDispatcher?.addCallback(backCallback)

        onDispose {
            backCallback.remove()
        }
    }
}
