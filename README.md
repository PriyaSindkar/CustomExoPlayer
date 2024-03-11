# CustomExoPlayer

CustomExoPlayer is a customizable Android library built on top of [ExoPlayer from Media3 API](https://developer.android.com/media/media3/exoplayer), providing additional features and functionalities to enhance media playback in your Android applications using Jetpack Compose.

## Features

- **Custom UI Components:** Tailor the look and feel of the ExoPlayer UI according to your app's design.
- **Advanced Playback Controls:** Implement custom volume controls for a seamless user experience.
- **Show playback progress:** Show playback progress to users in center circular progress and in bottom slider.
- **Play video from mp4 link:** Play .mp4 media items from their URL.
- **Play video from last paused position:** Tailored to start playing the video from last paused position

## Getting Started

To integrate ExoPlayer into your Android project, follow these steps:

1. Add the library to your project dependencies:

   ```gradle
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.2.1")implementation 'com.github.PriyaSindkar:CustomExoPlayer:1.0.0'

2. Use just one call to build an entire customizable player

```
VideoPlayer(
                context = LocalContext.current,
                modifier = Modifier
                    .fillMaxSize(),
                videoUrl = videoPlayerUiState.extractedVideoUrl,
                lastPaused = lastPaused,
                returnToHomeScreen = returnToHomeScreen,  // go back
                showSurvey = showSurvey
            )
```

3. Build media item from URL

```
val mediaItem = remember(videoUrl) {
        MediaItem.Builder()
            .setUri(videoUrl)
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .build()
    }

    val player = remember(context) {
        ExoPlayer.Builder(context)
            .build()
    }.apply {
        setMediaItem(mediaItem)
        prepare()
        playWhenReady = true
    }
```

## Work in progress
- Manage orientation changes while playing the video

## References
- https://medium.com/@4get.prakhar/video-player-app-using-jetpack-compose-4007dc15051
- https://betterprogramming.pub/custom-exoplayer-controls-in-jetpack-compose-c4089def0106

