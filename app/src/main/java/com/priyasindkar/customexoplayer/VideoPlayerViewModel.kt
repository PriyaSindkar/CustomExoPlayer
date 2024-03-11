package com.priyasindkar.customexoplayer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VideoPlayerViewModel : ViewModel() {
    private val _videoPlayerUiState = MutableStateFlow(VideoPlayerUiState())
    val videoPlayerUiState: StateFlow<VideoPlayerUiState> = _videoPlayerUiState.asStateFlow()

    // methods to fetch the video URLs from your server
}

data class VideoPlayerUiState(
    val extractedVideoUrl: String = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
)