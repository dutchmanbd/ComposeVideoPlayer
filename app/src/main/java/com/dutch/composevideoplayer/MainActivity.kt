package com.dutch.composevideoplayer

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Portrait
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dutch.composevideoplayer.model.PlayerWrapper
import com.dutch.composevideoplayer.presentation.PlayerView
import com.dutch.composevideoplayer.ui.theme.ComposeVideoPlayerTheme
import com.google.android.exoplayer2.C
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeVideoPlayerTheme {
                val viewModel = hiltViewModel<MainViewModel>()
                val videoItems by viewModel.videoItems.collectAsState()
                val selectVideoLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent(),
                    onResult = { uri ->
                        uri?.let(viewModel::addVideoUri)
                    }
                )

                var lifecycle by remember {
                    mutableStateOf(Lifecycle.Event.ON_CREATE)
                }
                val portrait = remember {
                    mutableStateOf(true)
                }
                val activity = LocalContext.current as Activity
                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        lifecycle = event
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    IconButton(onClick = {
                        selectVideoLauncher.launch("video/*")
                    }) {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = "Select Video"
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(onClick = {
                        activity.requestedOrientation = if (portrait.value) {
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        } else {
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }

                        // opposite the value of isPortrait
                        portrait.value = !portrait.value
                    }) {
                        Icon(
                            imageVector = if (portrait.value) Icons.Default.Landscape else Icons.Default.Portrait,
                            contentDescription = "Select Video"
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn {
                        items(videoItems) { item ->
                            Text(
                                text = item.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.playVideo(item.contentUri)
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }

//                    AndroidView(
//                        factory = { context ->
//                            PlayerView(context).also {
//                                it.player = viewModel.player
//                            }
//                        },
//                        update = {
//                            when (lifecycle) {
//                                Lifecycle.Event.ON_PAUSE -> {
//                                    it.onPause()
//                                    it.player?.pause()
//                                }
//                                Lifecycle.Event.ON_RESUME -> {
//                                    it.onResume()
//                                }
//                                else -> Unit
//                            }
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .aspectRatio(16 / 9f)
//                    )
                    val playerWrapper = PlayerWrapper(viewModel.player)
                    val configuration = LocalConfiguration.current

                    var playingIndex by remember {
                        mutableStateOf(0)
                    }

                    fun onTrailerChange(index: Int) {
                        playingIndex = index
                        playerWrapper.exoPlayer.seekTo(playingIndex, C.TIME_UNSET)
                        playerWrapper.exoPlayer.playWhenReady = true
                    }

                    when (configuration.orientation) {
                        Configuration.ORIENTATION_PORTRAIT -> {
                            PortraitView(
                                playerWrapper = playerWrapper,
                                playingIndex = playingIndex,
                                onTrailerChange = { index -> onTrailerChange(index) },
                                onFullScreenToggle = {},
                                navigateBack = {  }
                            )
                        }
                        else -> {
                            LandscapeView(
                                playerWrapper = playerWrapper,
                                onFullScreenToggle = {}
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                }

            }
        }
    }
}

@Composable
private fun PortraitView(
    playerWrapper: PlayerWrapper,
    playingIndex: Int,
    onTrailerChange: (Int) -> Unit,
    onFullScreenToggle: (isFullScreen: Boolean) -> Unit,
    navigateBack: () -> Unit
) {

    Column(modifier = Modifier) {
        PlayerView(
            modifier = Modifier.weight(1f, fill = true),
            playerWrapper = playerWrapper,
            isFullScreen = false,
            onTrailerChange = onTrailerChange,
            onFullScreenToggle = onFullScreenToggle,
            navigateBack = navigateBack
        )
        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            content = {
//                itemsIndexed(gameVideos.results) { index, trailer ->
//                    ShowTrailers(
//                        index = index,
//                        trailer = trailer,
//                        playingIndex = playingIndex,
//                        onTrailerClicked = { newIndex -> onTrailerChange(newIndex) })
//                }
            })
    }
}

@Composable
private fun LandscapeView(
    playerWrapper: PlayerWrapper,
    onFullScreenToggle: (isFullScreen: Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        PlayerView(
            modifier = Modifier.fillMaxSize(),
            playerWrapper = playerWrapper,
            isFullScreen = true,
            onFullScreenToggle = onFullScreenToggle
        )
    }
}
