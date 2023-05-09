package com.dutch.composevideoplayer.model

import androidx.compose.runtime.Stable
import com.google.android.exoplayer2.Player

import javax.annotation.concurrent.Immutable

@Immutable
@Stable
data class PlayerWrapper(
    val exoPlayer: Player
)
