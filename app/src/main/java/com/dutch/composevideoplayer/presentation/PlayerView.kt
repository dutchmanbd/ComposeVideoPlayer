package com.dutch.composevideoplayer.presentation

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player.STATE_ENDED
import com.dutch.composevideoplayer.model.PlayerWrapper
import com.dutch.composevideoplayer.util.Constants
import com.dutch.composevideoplayer.util.noRippleClickable
import com.dutch.composevideoplayer.util.setPortrait
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlinx.coroutines.delay

@Composable
fun PlayerView(
    modifier: Modifier = Modifier,
    playerWrapper: PlayerWrapper,
    isFullScreen: Boolean,
    onTrailerChange: ((Int) -> Unit)? = null,
    onFullScreenToggle: (isFullScreen: Boolean) -> Unit,
    navigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current

    BackHandler {
        if (isFullScreen) {
            context.setPortrait()
            onFullScreenToggle.invoke(false)
        } else {
            navigateBack?.invoke()
        }
    }

    Box(modifier = modifier) {

        var shouldShowControls by remember { mutableStateOf(false) }

        var isPlaying by remember { mutableStateOf(playerWrapper.exoPlayer.isPlaying) }

        var playbackState by remember { mutableStateOf(playerWrapper.exoPlayer.playbackState) }

        var title by remember {
            mutableStateOf(playerWrapper.exoPlayer.currentMediaItem?.mediaMetadata?.displayTitle.toString())
        }

        var videoTimer by remember { mutableStateOf(0L) }

        var totalDuration by remember { mutableStateOf(0L) }

        var bufferedPercentage by remember { mutableStateOf(0) }

        LaunchedEffect(key1 = shouldShowControls) {
            if (shouldShowControls) {
                delay(Constants.PLAYER_CONTROLS_VISIBILITY)
                shouldShowControls = false
            }
        }



        DisposableEffect(key1 = true) {
            val listener = object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    isPlaying = player.isPlaying
                    totalDuration = player.duration
                    videoTimer = player.contentPosition
                    bufferedPercentage = player.bufferedPercentage
                    playbackState = player.playbackState
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    onTrailerChange?.invoke(playerWrapper.exoPlayer.currentPeriodIndex)
                    title = mediaItem?.mediaMetadata?.displayTitle.toString()
                }
            }

            playerWrapper.exoPlayer.addListener(listener)

            onDispose {
                playerWrapper.exoPlayer.removeListener(listener)
            }
        }

        VideoPlayer(
            modifier = Modifier.fillMaxSize(),
            playerWrapper = playerWrapper,
            onPlayerClick = {
                shouldShowControls = shouldShowControls.not()
            }
        )

        PlayerControls(
            modifier = Modifier.fillMaxSize(),
            isVisible = { shouldShowControls },
            isPlaying = { isPlaying },
            playbackState = { playbackState },
            totalDuration = { totalDuration },
            bufferedPercentage = { bufferedPercentage },
            getTitle = { title },
            isFullScreen = isFullScreen,
            onPrevious = { playerWrapper.exoPlayer.seekToPrevious() },
            onNext = { playerWrapper.exoPlayer.seekToNext() },
            onReplay = { playerWrapper.exoPlayer.seekBack() },
            onForward = { playerWrapper.exoPlayer.seekForward() },
            onPauseToggle = {
                when {
                    playerWrapper.exoPlayer.isPlaying -> {
                        playerWrapper.exoPlayer.pause()
                    }
                    playerWrapper.exoPlayer.isPlaying.not() && playbackState == STATE_ENDED -> {
                        playerWrapper.exoPlayer.seekTo(0, 0)
                        playerWrapper.exoPlayer.playWhenReady = true
                    }
                    else -> {
                        playerWrapper.exoPlayer.play()
                    }
                }
                isPlaying = isPlaying.not()
            },
            onSeekChanged = { position -> playerWrapper.exoPlayer.seekTo(position.toLong()) },
            videoTimer = { videoTimer },
            onFullScreenToggle = onFullScreenToggle
        )
    }
}

@Composable
private fun VideoPlayer(
    modifier: Modifier = Modifier,
    playerWrapper: PlayerWrapper,
    onPlayerClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .background(MaterialTheme.colors.background)
            .testTag("VideoPlayerParent")
            .noRippleClickable {
                onPlayerClick.invoke()
            }
    ) {
        AndroidView(
            modifier = modifier
                .testTag("VideoPlayer"),
            factory = {
                StyledPlayerView(context).apply {
                    player = playerWrapper.exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            })
    }
}