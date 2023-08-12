package com.dutch.composevideoplayer.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player.STATE_ENDED
import com.dutch.composevideoplayer.R
import com.dutch.composevideoplayer.util.formatMinSec
import com.dutch.composevideoplayer.util.setLandscape
import com.dutch.composevideoplayer.util.setPortrait

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    isVisible: () -> Boolean,
    isPlaying: () -> Boolean,
    videoTimer: () -> Long,
    bufferedPercentage: () -> Int,
    playbackState: () -> Int,
    getTitle: () -> String,
    totalDuration: () -> Long,
    isFullScreen: Boolean,
    onPauseToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onForward: () -> Unit,
    onSeekChanged: (newValue: Float) -> Unit,
    onFullScreenToggle: (isFullScreen: Boolean) -> Unit
) {

    val visible = remember(isVisible()) { isVisible() }

    val playing = remember(isPlaying()) { isPlaying() }

    val duration = remember(totalDuration()) { totalDuration().coerceAtLeast(0) }

    val timer = remember(videoTimer()) { videoTimer() }

    val title = remember(getTitle()) { getTitle() }

    val buffer = remember(bufferedPercentage()) { bufferedPercentage() }

    val playerState = remember(playbackState()) {
        playbackState()
    }

    val context = LocalContext.current

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .testTag("PlayerControlsParent")
                .background(MaterialTheme.colors.background.copy(alpha = 0.6f))

        ) {

            Text(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .testTag("VideoTitle")
                    .animateEnterExit(
                        enter = slideInVertically(
                            initialOffsetY = { fullHeight: Int -> -fullHeight }
                        ),
                        exit = shrinkVertically()
                    ),
                text = title,
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.onBackground
            )

            val controlButtonModifier: Modifier = remember(isFullScreen) {
                if (isFullScreen) {
                    Modifier
                        .padding(horizontal = 8.dp)
                        .size(40.dp)
                } else {
                    Modifier.size(32.dp)
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .testTag("VideoControlParent"),
                horizontalArrangement = if (isFullScreen) {
                    Arrangement.Center
                } else {
                    Arrangement.SpaceEvenly
                }
            ) {
                IconButton(
                    modifier = controlButtonModifier,
                    onClick = onPrevious
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = R.drawable.ic_skip_previous),
                        contentDescription = stringResource(id = R.string.play_previous)
                    )
                }

                IconButton(
                    modifier = controlButtonModifier,
                    onClick = onReplay
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = R.drawable.ic_replay_10),
                        contentDescription = stringResource(id = R.string.rewind_5)
                    )
                }

                IconButton(
                    modifier = controlButtonModifier,
                    onClick = onPauseToggle
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(
                            id =
                            when {
                                playing -> {
                                    R.drawable.ic_pause_circle_filled
                                }
                                playing.not() && playerState == STATE_ENDED -> {
                                    R.drawable.ic_replay
                                }
                                else -> {
                                    R.drawable.ic_play_circle_filled
                                }
                            }
                        ),
                        contentDescription = stringResource(id = R.string.toggle_play)
                    )
                }

                IconButton(
                    modifier = controlButtonModifier,
                    onClick = onForward
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = R.drawable.ic_forward_10),
                        contentDescription = stringResource(id = R.string.forward_10)
                    )
                }

                IconButton(
                    modifier = controlButtonModifier,
                    onClick = onNext
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = R.drawable.ic_skip_next),//R.drawable.ic_skip_next
                        contentDescription = stringResource(id = R.string.play_next)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = if (isFullScreen) 32.dp else 16.dp)
                    .testTag("VideoSeek")
                    .animateEnterExit(
                        enter = slideInVertically(
                            initialOffsetY = { fullHeight: Int -> fullHeight }
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { fullHeight: Int -> fullHeight }
                        )
                    )
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = buffer.toFloat(),
                        enabled = false,
                        onValueChange = { /*do nothing*/ },
                        valueRange = 0f..100f,
                        colors =
                        SliderDefaults.colors(
                            disabledThumbColor = Color.Transparent,
                            disabledActiveTrackColor = Color.Gray
                        )
                    )

                    Slider(
                        value = timer.toFloat(),
                        onValueChange = {
                            onSeekChanged.invoke(it)
                        },
                        valueRange = 0f..duration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colors.onBackground,
                            activeTrackColor = MaterialTheme.colors.onBackground
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Text(
                            modifier = Modifier
                                .testTag("VideoTime")
                                .padding(start = 16.dp)
                                .animateEnterExit(
                                    enter = slideInVertically(
                                        initialOffsetY = { fullHeight: Int -> fullHeight }
                                    ),
                                    exit = slideOutVertically(
                                        targetOffsetY = { fullHeight: Int -> fullHeight }
                                    )
                                ),
                            text = timer.formatMinSec(),
                            color = MaterialTheme.colors.onBackground,
                            style = MaterialTheme.typography.subtitle2
                        )

                        Text(text = "/")

                        Text(
                            modifier = Modifier
                                .testTag("VideoTime")
                                .padding(start = 16.dp)
                                .animateEnterExit(
                                    enter = slideInVertically(
                                        initialOffsetY = { fullHeight: Int -> fullHeight }
                                    ),
                                    exit = slideOutVertically(
                                        targetOffsetY = { fullHeight: Int -> fullHeight }
                                    )
                                ),
                            text = duration.formatMinSec(),
                            color = MaterialTheme.colors.onBackground,
                            style = MaterialTheme.typography.subtitle2
                        )
                    }

                    IconButton(
                        modifier = Modifier
                            .testTag("FullScreenToggleButton")
                            .padding(end = 16.dp)
                            .size(24.dp)
                            .animateEnterExit(
                                enter = slideInVertically(
                                    initialOffsetY = { fullHeight: Int -> fullHeight }
                                ),
                                exit = slideOutVertically(
                                    targetOffsetY = { fullHeight: Int -> fullHeight }
                                )
                            ),
                        onClick = {
                            if (isFullScreen.not()) {
                                context.setLandscape()
                            } else {
                                context.setPortrait()
                            }.also {
                                onFullScreenToggle.invoke(isFullScreen.not())
                            }
                        }
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            painter = painterResource(
                                id = if (isFullScreen) {
                                    R.drawable.ic_fullscreen_exit
                                } else {
                                    R.drawable.ic_fullscreen
                                }
                            ),
                            contentDescription = stringResource(id = R.string.toggle_full_screen)
                        )
                    }
                }
            }
        }
    }

}