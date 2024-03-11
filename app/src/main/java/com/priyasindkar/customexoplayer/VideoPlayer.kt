package com.priyasindkar.customexoplayer

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.priyasindkar.customexoplayer.ui.theme.BlurTextFieldBg
import com.priyasindkar.customexoplayer.ui.theme.GreyColor
import com.priyasindkar.customexoplayer.utils.ButtonWithText
import com.priyasindkar.customexoplayer.utils.PlayPauseButton
import com.priyasindkar.customexoplayer.utils.fontDimensionResource
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun VideoPlayer(
    context: Context,
    modifier: Modifier,
    videoUrl: String,
    lastPaused: String,
    returnToHomeScreen: () -> Unit,
    showSurvey: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val width = configuration.screenWidthDp
    val height = configuration.screenHeightDp

    val exoPlayer = rememberExoVideoPlayer(context, videoUrl)

    var shouldShowControls by remember { mutableStateOf(true) }

    var showThumbnail by remember { mutableStateOf(true) }

    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }

    var totalDuration by remember { mutableLongStateOf(0L) }

    var currentTime by remember { mutableLongStateOf(0L) }

    var currentProgress by remember { mutableFloatStateOf(0f) }

    var buffer by remember { mutableLongStateOf(0L) }

    var playerVolume by remember { mutableFloatStateOf(exoPlayer.volume) }

    var playbackState by remember { mutableIntStateOf(exoPlayer.playbackState) }

    var isFirstTime = true

    var isPaused by remember { mutableStateOf(false) }

    var isBackPressed by remember { mutableStateOf(false) }

    var isPlaybackDone by remember { mutableStateOf(false) }

    val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (isFirstTime && playbackState == Player.STATE_READY && exoPlayer.playWhenReady) {
                isFirstTime = false
                shouldShowControls = false
                if (lastPaused.isNotEmpty()) {
                    currentTime = timeStringToMilliseconds(lastPaused) ?: 0L
                    exoPlayer.seekTo(currentTime)
                }
            }

            if (exoPlayer.isPlaying.not() && playbackState == STATE_ENDED) {
                // reached end of the video, go to survey
//                showSurvey()
                isPlaybackDone = true
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)
            currentTime = player.currentPosition.coerceAtLeast(0L)
            totalDuration = player.duration.coerceAtLeast(0L)
            buffer = player.bufferedPosition
            isPlaying = player.isPlaying
            playbackState = player.playbackState
        }
    }

    val onBackPressed = {
        // when user presses back button/system back ask user if they want to go back in middle of the video or not
        isBackPressed = true
        shouldShowControls = true
        isPaused = true
        exoPlayer.pause()
    }

    BackPressHandler(onBackPressed = onBackPressed)

    // update playback progress every sec
    LaunchedEffect(key1 = exoPlayer) {
        while (true) {
            if (isPlaying) {
                currentTime = exoPlayer.currentPosition.coerceAtLeast(0L)
                if (currentTime > 0) {
                    showThumbnail = false
                }
                currentProgress = (currentTime.toFloat() / totalDuration)
            }
            delay(1000)
        }
    }

    Box(modifier = modifier) {
        var size by remember { mutableStateOf(IntSize.Zero) }

        DisposableEffect(
            AndroidView(
                modifier = modifier
                    .background(color = Color.Black)
                    .onGloballyPositioned {
                        size = it.size
                    }
                    .clickable {
                        shouldShowControls = shouldShowControls.not()
                    },
                factory = { context ->
                    PlayerView(context).apply {
                        player = exoPlayer
                        layoutParams = FrameLayout.LayoutParams(width, height)
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        hideController()
                        useController = false
                        player?.addListener(listener)
                    }
                },
            )
        ) {
            onDispose {
                exoPlayer.removeListener(listener)
                exoPlayer.release()
            }
        }

        Column(
            modifier = Modifier
                .wrapContentWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._40dp)))

            ButtonWithText(
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen._16dp)),
                context = context,
                title = stringResource(id = R.string.back),
                contentDescription = stringResource(id = R.string.back_button_descripion),
                icon = R.drawable.ic_arrow_left,
                aContentColor = Color.White,
                aContainerColor = GreyColor
            ) {
                onBackPressed.invoke()
            }
        }

        ThumbnailImage { showThumbnail }

        PlayerControls(
            modifier = Modifier.fillMaxSize(),
            isVisible = { shouldShowControls },
            isPlaying = { isPlaying },
            isBackPressed = { isBackPressed },
            playbackState = { playbackState },
            onPauseToggle = {
                when {
                    exoPlayer.isPlaying -> {
                        // pause the video
                        isPaused = true
                        exoPlayer.pause()
                    }

                    exoPlayer.isPlaying.not() && playbackState == STATE_ENDED -> {
                        // reached end of the video, reset playback
                        exoPlayer.seekTo(0)
                        exoPlayer.playWhenReady = true
                    }

                    else -> {
                        // play the video, it's already paused
                        isPaused = false
                        if (isBackPressed) isBackPressed = false
                        exoPlayer.play()
                    }
                }
                isPlaying = isPlaying.not()
            },
            totalDuration = { totalDuration },
            currentTime = { currentTime },
            volume = { playerVolume },
            currentProgress = { currentProgress },
            isPlaybackDone = { isPlaybackDone },
            bufferedPercentage = { buffer },
            onSeekChanged = { timeMs: Float ->
                exoPlayer.seekTo(timeMs.toLong())
            },
            onVolumeSeekChanged = { volume: Float ->
                playerVolume = volume
                exoPlayer.volume = volume
            },
            returnHome = returnToHomeScreen
        )
    }
}

@Composable
private fun ThumbnailImage(
    isShowThumbnail: () -> Boolean,
) {
    val showThumbnail = remember(isShowThumbnail()) { isShowThumbnail() }

    if (showThumbnail) {
        // video thumbnail shown from network using coil
//        AsyncImage(
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Fit,
//            model = videoThumbnailURL,
//            contentDescription = "Video thumbnail image"
//        )
        Image(
            painter = painterResource(id = R.drawable.thumbnail),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            contentDescription = "Video thumbnail image"
        )
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun rememberExoVideoPlayer(
    context: Context,
    videoUrl: String,
): ExoPlayer {
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
    return player
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PlayerControls(
    modifier: Modifier = Modifier,
    isVisible: () -> Boolean,
    isPlaying: () -> Boolean,
    isBackPressed: () -> Boolean,
    volume: () -> Float,
    currentProgress: () -> Float,
    isPlaybackDone: () -> Boolean,
    onPauseToggle: () -> Unit,
    totalDuration: () -> Long,
    currentTime: () -> Long,
    bufferedPercentage: () -> Long,
    playbackState: () -> Int,
    onSeekChanged: (timeMs: Float) -> Unit,
    onVolumeSeekChanged: (timeMs: Float) -> Unit,
    returnHome: () -> Unit
) {

    val visible = remember(isVisible()) { isVisible() }

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.6f))) {

            CenterControls(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.Center),
                isPlaying = isPlaying,
                currentProgress = currentProgress,
                isPlaybackDone = isPlaybackDone,
                onPauseToggle = onPauseToggle,
                playbackState = playbackState,
                isBackPressed = isBackPressed,
                returnHome = returnHome,
            )

            BottomControls(
                modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .animateEnterExit(
                        enter =
                        slideInVertically(
                            initialOffsetY = { fullHeight: Int ->
                                fullHeight
                            }
                        ),
                        exit =
                        slideOutVertically(
                            targetOffsetY = { fullHeight: Int ->
                                fullHeight
                            }
                        )
                    ),
                totalDuration = totalDuration,
                currentTime = currentTime,
                volume = volume,
                bufferedPercentage = bufferedPercentage,
                onSeekChanged = onSeekChanged,
                onVolumeSeekChanged = onVolumeSeekChanged
            )
        }
    }
}

@Composable
private fun CenterControls(
    modifier: Modifier = Modifier,
    isPlaying: () -> Boolean,
    currentProgress: () -> Float,
    isPlaybackDone: () -> Boolean,
    playbackState: () -> Int,
    onPauseToggle: () -> Unit,
    isBackPressed: () -> Boolean,
    returnHome: () -> Unit
) {
    val isVideoPlaying = remember(isPlaying()) { isPlaying() }

    val playerState = remember(playbackState()) { playbackState() }

    val progress = remember(currentProgress()) { currentProgress() }

    val isSystemBackPressed = remember(isBackPressed()) { isBackPressed() }

    val isPlaybackFinished = remember(isPlaybackDone()) { isPlaybackDone() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (isSystemBackPressed) {
            Text(
                text = stringResource(
                    R.string.are_you_sure_you_want_to_go_back
                ),
                style = MaterialTheme.typography.headlineMedium.copy(
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._60dp)))
        }
        PlayPauseButton(
            isLoading = playerState != Player.STATE_READY,
            progress = progress,
            onClick = onPauseToggle,
            content = {
                if (isSystemBackPressed) {
                    Text(
                        text = stringResource(id = R.string.label_continue),
                        style = MaterialTheme.typography.labelMedium.copy(
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontSize = fontDimensionResource(id = R.dimen._24sp)
                        ),
                        textDecoration = TextDecoration.Underline
                    )
                } else {
                    Icon(
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen._90dp))
                            .height(dimensionResource(id = R.dimen._90dp)),
                        painter = when {
                            isVideoPlaying -> {
                                painterResource(id = R.drawable.ic_pause)
                            }

                            isPlaybackFinished -> {
                                painterResource(id = R.drawable.replay)
                            }

                            playerState == STATE_ENDED -> {
                                painterResource(id = R.drawable.ic_play)
                            }

                            else -> {
                                painterResource(id = R.drawable.ic_play)
                            }
                        },
                        tint = Color.White,
                        contentDescription = stringResource(R.string.play_pause_button)
                    )
                }
            }
        )

        if (isSystemBackPressed) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._60dp)))

            ButtonWithText(
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen._16dp)),
                context = LocalContext.current,
                title = stringResource(id = R.string.yes_back),
                contentDescription = stringResource(id = R.string.yes_back),
                icon = null,
                aContentColor = Color.White,
                aContainerColor = GreyColor
            ) {
                returnHome()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomControls(
    modifier: Modifier = Modifier,
    totalDuration: () -> Long,
    currentTime: () -> Long,
    volume: () -> Float,
    bufferedPercentage: () -> Long,
    onSeekChanged: (timeMs: Float) -> Unit,
    onVolumeSeekChanged: (timeMs: Float) -> Unit
) {

    val duration = remember(totalDuration()) { totalDuration() }

    val videoTime = remember(currentTime()) { currentTime() }

    val buffer = remember(bufferedPercentage()) { bufferedPercentage() }

    val playerVolume = remember(volume()) { volume() }

    var isVolumeSliderShown by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(bottom = dimensionResource(id = R.dimen._8dp)),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.05f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = isVolumeSliderShown) {
                Slider(
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = 270f
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(
                                Constraints(
                                    minWidth = constraints.minHeight,
                                    maxWidth = constraints.maxHeight,
                                    minHeight = constraints.minWidth,
                                    maxHeight = constraints.maxWidth,
                                )
                            )
                            layout(placeable.height, placeable.width) {
                                placeable.place(-placeable.width, 0)
                            }
                        }
                        .width(100.dp)
                        .height(50.dp),
                    value = playerVolume,
                    onValueChange = onVolumeSeekChanged,
                    valueRange = 0f..1f,
                    colors =
                    SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Yellow,
                        inactiveTrackColor = BlurTextFieldBg,
                    )
                )

            }
            Image(
                modifier = Modifier
                    .wrapContentSize()
//                    .padding(horizontal = dimensionResource(id = R.dimen._8dp))
                    .width(dimensionResource(id = R.dimen._20dp))
                    .height(dimensionResource(id = R.dimen._20dp))
                    .clickable {
                        if (isVolumeSliderShown.not()) {
                            isVolumeSliderShown = true
                        } else {
                            onVolumeSeekChanged(0f)
                        }
                    },
                painter = if (playerVolume == 0f) painterResource(id = R.drawable.volume_mute) else painterResource(
                    id = R.drawable.ic_volume
                ),
                contentDescription = stringResource(R.string.volume_icon)
            )

        }
//        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._16dp)))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = dimensionResource(id = R.dimen._8dp))
                .weight(0.7f)
        ) {
            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen._16dp)),
                value = buffer.toFloat(),
                onValueChange = onSeekChanged,
                valueRange = 0f..duration.toFloat(),
                colors =
                SliderDefaults.colors(
                    disabledThumbColor = Color.Transparent,
                    thumbColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTrackColor = Color.White,
                ),
                thumb = {}
            )
            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen._16dp)),
                value = videoTime.toFloat(),
                onValueChange = onSeekChanged,
                valueRange = 0f..duration.toFloat(),
                colors =
                SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Yellow,
                    inactiveTrackColor = BlurTextFieldBg,
                )
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = dimensionResource(id = R.dimen._8dp))
                .weight(0.2f),
            textAlign = TextAlign.Center,
            text = "${videoTime.formatMinSec()} / ${duration.formatMinSec()}",
            color = Color.White
        )
    }
}

fun Long.formatMinSec(): String {
    return if (this == 0L) {
        "..."
    } else {
        val hours = TimeUnit.MILLISECONDS.toHours(this)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this) -
                TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(this) -
                TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(this)
                )

        if (hours == 0L) {
            String.format("%02d:%02d", minutes, seconds)
        } else {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}

fun timeStringToMilliseconds(timeString: String): Long? {
    val parts = timeString.split(":")
    if (parts.size != 2) {
        return null
    }

    val minutes = parts[0].toLong()
    val seconds = parts[1].toLong()

    return (minutes * 60 + seconds) * 1000
}